#include <EEPROM.h>
#include <SoftwareSerial.h>
#include <Adafruit_NeoPixel.h>

/* ---------------------------------------------------------------------- LED */
#define LED_COUNT (1)
#define PIN_LED   (4)

/* --------------------------------------------------------------------- pump */
#define PIN_PUMP (3)

/* -------------------------------------------------------------- water level */
#define PIN_TRIG (10)
#define PIN_ECHO (11)

#define WLVL_MIN (   10) // cm
#define WLVL_MAX (    3) // cm
#define PW_MAX   (23200) // pulse width for 400 cm, max for HC-SR04

/* ----------------------------------------------------------------- moisture */
#define PIN_MOIST_DAT ( A0)
#define PIN_MOIST_PWR (  2)
#define MOIST_MIN     (  0) // percentage
#define MOIST_MAX     (100) // percentage
#define CALIB_TO      ( 60) // seconds
#define CALIB_STEPS   (  4) // 1 << CALIB_STEPS

/* ---------------------------------------------------------------- bluetooth */
#define BT_RX (   9)
#define BT_TX (   8)
#define BT_TO (1000)
#define BT_WT (  10)

#define OK      (  0)
#define NOK     (  1)
#define INIT    (100)
#define RD_LO   (101)
#define RD_HI   (102)
#define RD_MLVL (103)
#define RD_WLVL (104)
#define WR_CFG  (200)
#define CALIB   (201)

#define BTCHK(e) if((e) == NOK) { TRACELN("BTCHK"); btnok(); return; }

SoftwareSerial bt(BT_TX, BT_RX);

/* ------------------------------------------------------------ configuration */
#define CFG_ADDRESS (0)

typedef struct {
  uint16_t moist_min; // 0.. 0xffff
  uint16_t moist_max; // 0.. 0xffff
  uint8_t  moist_lo; // 0.. 100
  uint8_t  moist_hi; // 0.. 100
} cfg_t;
cfg_t _cfg;

/* -------------------------------------------------------------------- debug */
#define DEBUG

#ifdef DEBUG
#define TRACE(S) Serial.print((S))
#define TRACELN(S) Serial.println((S))
#else
#define TRACE(S)
#define TRACELN(S)
#endif

/* -------------------------------------------------------------- global vars */
#define TIME_BTW_MEASURE (15) // minutes to wait between measurements

Adafruit_NeoPixel _led = Adafruit_NeoPixel(LED_COUNT,
                                           PIN_LED,
                                           NEO_GRB + NEO_KHZ800);

unsigned _wlvl = 0;
unsigned _mlvl = 0;

/*---------------------------------------------------------------------- btwt */
int btwt(unsigned long to) {
  int er;

  er = OK;
  while( !bt.available() ) {
    delay(BT_WT);
    to -= BT_WT;
    if(to <= 0) {
      TRACELN("bt timeout");
      er = NOK;
      break;
    }
  }

  return er;
}

/*---------------------------------------------------------------------- btrd */
int btrd(uint8_t *v, unsigned long to) {
  int ret;
  int er;

  er = btwt(to);
  if(er == 0) {
    *v = (uint8_t)bt.parseInt();
    er = btwt(to);
    if(er == OK) {
      if( bt.read() == 0xA ) {
        er = OK;
      } else {
        er = NOK;
      }
    }
  }
  return er;
}

/*---------------------------------------------------------------------- btok */
void btok() {
  bt.println(OK);
}

/*--------------------------------------------------------------------- btnok */
void btnok() {
  bt.println(NOK);
}

/* -------------------------------------------------------------------- level */
void level() {
  unsigned long t0;
  unsigned long t1;
  unsigned long pw;
  float         cm;

  digitalWrite(PIN_TRIG, HIGH);
  delayMicroseconds(10);
  digitalWrite(PIN_TRIG, LOW);

  while( digitalRead(PIN_ECHO) == 0 );
  t0 = micros();

  while( digitalRead(PIN_ECHO) == 1 );
  t1 = micros();

  pw = t1 - t0;
  cm = pw / 58.0;

  TRACE("OF: "); TRACE(pw >= PW_MAX ? "yes" : "no"); TRACE("\t");
  TRACE("t0: "); TRACE(t0); TRACE("\t");
  TRACE("t1: "); TRACE(t1); TRACE("\t");
  TRACE("cm: "); TRACELN(cm);

  _wlvl = cm;
}

/* -------------------------------------------------------------------- moist */
void moist() {
  digitalWrite(PIN_MOIST_PWR, HIGH);
  delay(500);
  _mlvl = analogRead(PIN_MOIST_DAT);
  digitalWrite(PIN_MOIST_PWR, LOW);
  
  TRACE("moisture level: "); TRACELN(_mlvl);
}

/* --------------------------------------------------------------------- pump */
void pump() {

}

/*--------------------------------------------------------------------- rdcfg */
void rdlo() {
  TRACELN("Read lo");
  bt.println(_cfg.moist_lo);
}

/*--------------------------------------------------------------------- rdcfg */
void rdhi() {
  TRACELN("Read hi");
  bt.println(_cfg.moist_hi);
}

/*-------------------------------------------------------------------- rdwlvl */
void rdwlvl() {
  TRACELN("Read water level");
  level();
  bt.println(_wlvl);
}

/*-------------------------------------------------------------------- rdmlvl */
void rdmlvl() {
  TRACELN("Read moisture level");
  moist();
  bt.println(_mlvl);
}

/*--------------------------------------------------------------------- wrcfg */
void wrcfg() {
  int     e;
  uint8_t moist_lo;
  uint8_t moist_hi;

  // moist_lo value
  e = btrd(&moist_lo, BT_TO); BTCHK(e);
  if(moist_lo > 100) {
    btnok();
    return;
  } else {
    btok();
  }

  // moist_hi value
  e = btrd(&moist_hi, BT_TO); BTCHK(e);
  if(moist_hi > 100) {
    btnok();
    return;
  }

  _cfg.moist_lo = moist_lo;
  _cfg.moist_hi = moist_hi;
  //EEPROM.put(CFG_ADDRESS, _cfg);
  btok();

  TRACE("cfg.moist_lo = "); TRACELN(_cfg.moist_lo);
  TRACE("cfg.moist_hi = "); TRACELN(_cfg.moist_hi);
}

/*--------------------------------------------------------------------- calib */
void calib() {
  uint16_t t;
  uint8_t  v;
  int      e;

  t = 0;
  for(int i = 0; i < (1 << CALIB_STEPS); i++) {
    moist();
    t += _mlvl;
  }
  _cfg.moist_min = t >> CALIB_STEPS;
  btok();
  
  e = btrd(&v, CALIB_TO*1000);
  if(e == NOK || v != CALIB) {
    btnok();
    return;
  }

  t = 0;
  for(int i = 0; i < (1 << CALIB_STEPS); i++) {
    moist();
    t += _mlvl;
  }
  _cfg.moist_max = t >> CALIB_STEPS;
  btok();

  TRACE("moist min = "); TRACELN(_cfg.moist_min);
  TRACE("moist max = "); TRACELN(_cfg.moist_max);
}

/* ---------------------------------------------------------------- bluetooth */
void bluetooth() {
  int     e;
  uint8_t v;
  
  if( bt.available() ) {
    TRACELN("Detected bluetooth communication");
    e = btrd(&v, BT_TO);

    if(e == OK && v == INIT) {
      btok(); TRACELN("BT initiated");
      e = btrd(&v, BT_TO); BTCHK(e);

      switch(v) {
      case RD_LO  : rdlo  (); break;
      case RD_HI  : rdhi  (); break;
      case RD_MLVL: rdmlvl(); break;
      case RD_WLVL: rdwlvl(); break;
      case WR_CFG : wrcfg (); break;
      case CALIB  : calib (); break;
      default     : btnok (); break;
      }
    } else {
      btnok();
    }
  }
}

/* -------------------------------------------------------------------- setup */
void setup() {
  Serial.begin(9600);
  bt.begin(9600);

  pinMode(PIN_MOIST_PWR, OUTPUT);
  pinMode(PIN_TRIG     , OUTPUT);
  pinMode(PIN_ECHO     ,  INPUT);
  
  digitalWrite(PIN_MOIST_PWR, LOW);
  digitalWrite(PIN_TRIG     , LOW);
  analogWrite (PIN_PUMP     ,   0);
  
  _led.begin();
  _led.show();

  EEPROM.get(CFG_ADDRESS, _cfg);

  TRACELN("Setup complete");
}

/* --------------------------------------------------------------------- loop */
unsigned long prev_measure = 0;
void loop() {
  unsigned t;
  unsigned m;

  // --------- handle bluetooth communications
  bluetooth();
  
  t = millis();
  m = (t - prev_measure) / 1000 / 60;
  if(m >= TIME_BTW_MEASURE) {
    TRACELN("running loop");
    prev_measure = t;

    // --------- run moisture process
    moist();

    // --------- run water level process
    level();
    
    // --------- run pump process
    pump();
  }

  /** --------------- playground --------------- **/
  #if 0
  int r = 0;
  int st = 0;
  
  _led.setPixelColor(0, r, 0, 0);
  _led.show();
  
  if(st == 0) {
    r++;
  } else {
    r--;
  }
  if(r > 255) { st = 1; r = 255; }
  if(r <   0) { st = 0; r = 0;   }
  
  delay(2);
  #endif

  #if 0
  digitalWrite(PIN_MOIST_PWR, HIGH);
  delay(500);
  m = analogRead(PIN_MOIST_DAT);
  digitalWrite(PIN_MOIST_PWR, LOW);
  
  TRACE("moisture level: "); TRACELN(m);
  delay(500);
  #endif
}
