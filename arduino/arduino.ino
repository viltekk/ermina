#include <EEPROM.h>
#include <SoftwareSerial.h>
#include <Adafruit_NeoPixel.h>

/* ---------------------------------------------------------------------- LED */
#define LED_COUNT ( 1)
#define PIN_LED   (12)

// 0xRRGGBB
#define LED_MIN   (0xff0000) // completely red
//#define LED_MAX   (0x1599f6) // completely green
#define LED_MAX   (0x0000ff) // completely green

/* --------------------------------------------------------------------- pump */
#define PIN_PUMP (   5)
#define PUMP_MAX ( 300) // max amount of seconds to run pump
#define PID_Kp   ( 0.4)
#define PID_Ki   (0.02)
#define PID_Kd   ( 0.2)

/* -------------------------------------------------------------- water level */
#define PIN_TRIG (10)
#define PIN_ECHO (11)

#define WLVL_MIN ( 10.0) // cm
#define WLVL_MAX (  3.0) // cm
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
#define NOK     (999)
#define INIT    (100)
#define RD_MIN  (101)
#define RD_MAX  (102)
#define RD_LO   (103)
#define RD_HI   (104)
#define RD_MLVL (105)
#define RD_WLVL (106)
#define RD_PID  (107)
#define WR_CFG  (200)
#define CALIB   (201)
#define WR_Kp   (202)
#define WR_Ki   (203)
#define WR_Kd   (204)

#define BTCHK(e) if((e) == NOK) { TRACELN("BTCHK"); btnok(); return; }

SoftwareSerial bt(BT_TX, BT_RX);

/* ------------------------------------------------------------ configuration */
#define CFG_ADDRESS (0)

typedef struct {
  uint16_t moist_min; // 0.. 0xffff
  uint16_t moist_max; // 0.. 0xffff
  uint16_t moist_lo;  // 0.. 0xffff
  uint16_t moist_hi;  // 0.. 0xffff
} cfg_t;
cfg_t _cfg;

/* -------------------------------------------------------------------- debug */
#define DEBUG

#ifdef DEBUG
#define TRACE(S) Serial.print((S));
#define TRACELN(S) Serial.println((S))
#else
#define TRACE(S)
#define TRACELN(S)
#endif

// debug commands
#define DBG      (255)
#define PUMP_ON  (254)
#define PUMP_OFF (253)

/* -------------------------------------------------------------- global vars */
#define TIME_BTW_MEASURE ((15*60)) // seconds to wait between measurements

Adafruit_NeoPixel _led = Adafruit_NeoPixel(LED_COUNT,
                                           PIN_LED,
                                           NEO_GRB + NEO_KHZ800);

float    _wlvl         = 0;
uint16_t _mlvl         = 0;
uint16_t _prev_measure = 0;
bool     _force_pump   = false;

// debug
#define HIST_SZ (20)
int16_t hist_p[HIST_SZ];
int16_t hist_i[HIST_SZ];
int16_t hist_d[HIST_SZ];
uint8_t hist_c = 0;

/*---------------------------------------------------------------------- btwt */
uint16_t btwt(uint16_t to) {
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
uint8_t btrd(uint16_t *v, uint16_t to) {
  uint16_t ret;
  uint8_t  er;

  er = btwt(to);
  if(er == 0) {
    *v = (uint16_t)bt.parseInt();
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

/* ---------------------------------------------------------------------- led */
void led() {
  uint8_t min_r;
  uint8_t min_g;
  uint8_t min_b;

  uint8_t max_r;
  uint8_t max_g;
  uint8_t max_b;

  uint8_t w;
  uint8_t r;
  uint8_t g;
  uint8_t b;

  float k0;
  float k1;

  min_r = (LED_MIN >> 16) & 0xff;
  min_g = (LED_MIN >>  8) & 0xff;
  min_b = (LED_MIN      ) & 0xff;

  max_r = (LED_MAX >> 16) & 0xff;
  max_g = (LED_MAX >>  8) & 0xff;
  max_b = (LED_MAX      ) & 0xff;

  w  = round((100.0*(WLVL_MIN-_wlvl)) / (WLVL_MIN-WLVL_MAX));
  k0 = w/100.0;
  k1 = (100-w)/100.0;
  r  = k0*max_r + k1*min_r;
  g  = k0*max_g + k1*min_g;
  b  = k0*max_b + k1*min_b;

  _led.setPixelColor(0, r, g, b);
  _led.show();


  TRACE("w: "); TRACE(w); TRACE(", ");
  TRACE("r: "); TRACE(r); TRACE(", ");
  TRACE("g: "); TRACE(g); TRACE(", ");
  TRACE("b: "); TRACELN(b);
}

/* -------------------------------------------------------------------- level */
void level() {
  uint16_t t0;
  uint16_t t1;
  uint16_t pw;

  digitalWrite(PIN_TRIG, HIGH);
  delayMicroseconds(10);
  digitalWrite(PIN_TRIG, LOW);

  while( digitalRead(PIN_ECHO) == 0 );
  t0 = micros();

  while( digitalRead(PIN_ECHO) == 1 );
  t1 = micros();

  delay(60);

  pw    = t1 - t0;
  _wlvl = pw / 58.0;
  
  TRACE("OF: "); TRACE(pw >= PW_MAX ? "yes" : "no"); TRACE("\t");
  TRACE("wlvl: "); TRACELN(_wlvl);

  if(_wlvl < WLVL_MAX) {
    _wlvl = WLVL_MAX;
  }
  if(_wlvl > WLVL_MIN) {
    _wlvl = WLVL_MIN;
  }

  led();

  TRACE("water level: "); TRACELN(_wlvl);
}

/* -------------------------------------------------------------------- moist */
bool _moist_sat = false;
void moist() {
  digitalWrite(PIN_MOIST_PWR, HIGH);
  delay(10);
  _mlvl = analogRead(PIN_MOIST_DAT);
  digitalWrite(PIN_MOIST_PWR, LOW);
  
  if(!_moist_sat) {
    _moist_sat = _mlvl <= _cfg.moist_hi;
  } else {
    _moist_sat = !(_mlvl >= _cfg.moist_lo);
  }

  TRACE("moisture level: "); TRACELN(_mlvl);
}

/* ---------------------------------------------------------------------- pid */
int16_t  _pid_e       = 0;
int16_t  _pid_i       = 0;
uint8_t  _pid_sp      = 0;
uint16_t _pump_t      = 0;
uint16_t _prev_pump_t = 0;

void pid() {
  int16_t e;
  int16_t p;
  int16_t i;
  int16_t d;
  int16_t t;

  e = _mlvl - _pid_sp;
  _pid_i += e;

  p = PID_Kp * e;
  i = PID_Ki * _pid_i;
  d = PID_Kd * (_pid_e - e);
  t = p + i + d;

  TRACE("e = "); TRACE(e); TRACE("\t");
  TRACE("p = "); TRACE(p); TRACE("\t");
  TRACE("i = "); TRACE(i); TRACE("\t");
  TRACE("d = "); TRACE(d); TRACE("\t");
  TRACE("t = "); TRACELN(t);

  _prev_pump_t = _pump_t;
  if(t < 0) {
    _pump_t = 0;
  } else {
    if(t > PUMP_MAX) {
      _pump_t = PUMP_MAX;
    } else {
      _pump_t = t;
    }
  }

  _pid_e = e;

  hist_p[hist_c] = p;
  hist_i[hist_c] = i;
  hist_d[hist_c] = d;
  hist_c++;
  if(hist_c > HIST_SZ) {
    hist_c = 0;
  }
}

/* ------------------------------------------------------------------ pump_on */
void pump_on() {
  TRACELN("force pump on");
  _force_pump = true;
}

/* ------------------------------------------------------------------ pump_off */
void pump_off() {
  TRACELN("force pump off");
  _force_pump = false;
}

/* --------------------------------------------------------------------- pump */
bool     _pump_running = false;
uint16_t _pump_start   = 0;

void pump() {
  uint8_t v;

  if(_pump_running) {
    _pump_running = millis()/1000 - _pump_start < _pump_t;
    if(!_pump_running) {
      _pump_t = 0;
    }
  } else {
    if(!_moist_sat && _pump_t > 0) {
      _pump_start   = millis()/1000;
      _pump_running = true;
      TRACE("running pump for "); TRACE(_pump_t); TRACELN(" seconds");
    }
  }

  v = _pump_running||_force_pump ? HIGH : LOW;
  digitalWrite(PIN_PUMP, v);
}

/*--------------------------------------------------------------------- rdpid */
void rdpid() {
  TRACE("Read PID: ");
  TRACE(PID_Kp); TRACE(" "); TRACE(PID_Ki); TRACE(" "); TRACELN(PID_Kd);
  bt.println(PID_Kp);
  bt.println(PID_Ki);
  bt.println(PID_Kd);
}

/*--------------------------------------------------------------------- rdmin */
void rdmin() {
  TRACE("Read min: "); TRACELN(_cfg.moist_min);
  bt.println(_cfg.moist_min);
}

/*--------------------------------------------------------------------- rdmax */
void rdmax() {
  TRACE("Read max: "); TRACELN(_cfg.moist_max);
  bt.println(_cfg.moist_max);
}

/*---------------------------------------------------------------------- rdlo */
void rdlo() {
  TRACE("Read lo: "); TRACELN(_cfg.moist_lo);
  bt.println(_cfg.moist_lo);
}

/*---------------------------------------------------------------------- rdhi */
void rdhi() {
  TRACE("Read hi: "); TRACELN(_cfg.moist_hi);
  bt.println(_cfg.moist_hi);
}

/*-------------------------------------------------------------------- rdwlvl */
void rdwlvl() {
  int rsp;
  level();
  rsp = round((100.0*(WLVL_MIN-_wlvl)) / (WLVL_MIN-WLVL_MAX));
  TRACE("Read water level: "); TRACELN(rsp);
  bt.println(rsp);
}

/*-------------------------------------------------------------------- rdmlvl */
void rdmlvl() {
  moist();
    
  if(_mlvl < _cfg.moist_max) {
    _mlvl = _cfg.moist_max;
  }
  if(_mlvl > _cfg.moist_min) {
    _mlvl = _cfg.moist_min;
  }

  TRACE("Read moisture level: "); TRACELN(_mlvl);
  bt.println(_mlvl);
}

/*--------------------------------------------------------------------- wrcfg */
void wrcfg() {
  uint8_t  e;
  uint16_t moist_lo;
  uint16_t moist_hi;

  e = btrd(&moist_lo, BT_TO); BTCHK(e); btok();
  e = btrd(&moist_hi, BT_TO); BTCHK(e);

  TRACE("lo: "); TRACELN(moist_lo);
  TRACE("hi: "); TRACELN(moist_hi);

  if(moist_hi > moist_lo) {
    btnok();
  } else {
    _cfg.moist_lo = moist_lo;
    _cfg.moist_hi = moist_hi;
    EEPROM.put(CFG_ADDRESS, _cfg);
    btok();
  }

  TRACE("cfg.moist_lo = "); TRACELN(_cfg.moist_lo);
  TRACE("cfg.moist_hi = "); TRACELN(_cfg.moist_hi);
}

/*--------------------------------------------------------------------- calib */
void calib() {
  uint16_t t;
  uint16_t v;
  uint16_t min;
  uint16_t max;
  uint8_t  e;

  t = 0;
  for(int i = 0; i < (1 << CALIB_STEPS); i++) {
    moist();
    t += _mlvl;
  }
  min = t >> CALIB_STEPS;
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
  max = t >> CALIB_STEPS;

  // avoid division by zero in rdmlvl
  if(max == min) {
    max--;
  }

  _cfg.moist_min = min;
  _cfg.moist_max = max;
  EEPROM.put(CFG_ADDRESS, _cfg);

  btok();

  TRACE("moist min = "); TRACELN(_cfg.moist_min);
  TRACE("moist max = "); TRACELN(_cfg.moist_max);
}

/* ---------------------------------------------------------------------- dbg */
void dbg() {
  #if 0
  for(int i = 0; i < HIST_SZ; i++) {
    bt.print("p["); bt.print(i); bt.print("] = ");
    bt.println(hist_p[i]);

    Serial.print("p["); Serial.print(i); Serial.print("] = ");
    Serial.println(hist_p[i]);
  }
  bt.println();
  Serial.println();

  for(int i = 0; i < HIST_SZ; i++) {
    bt.print("i["); bt.print(i); bt.print("] = ");
    bt.println(hist_i[i]);

    Serial.print("i["); Serial.print(i); Serial.print("] = ");
    Serial.println(hist_i[i]);
  }
  bt.println();
  Serial.println();
  
  for(int i = 0; i < HIST_SZ; i++) {
    bt.print("d["); bt.print(i); bt.print("] = ");
    bt.println(hist_d[i]);

    Serial.print("d["); Serial.print(i); Serial.print("] = ");
    Serial.println(hist_d[i]);
  }
  #endif

  Serial.print("cfg.min: "); Serial.println(_cfg.moist_min);
  Serial.print("cfg.max: "); Serial.println(_cfg.moist_max);

  bt.print("cfg.min: "); bt.println(_cfg.moist_min);
  bt.print("cfg.max: "); bt.println(_cfg.moist_max);

  Serial.print("cfg.lo: "); Serial.println(_cfg.moist_lo);
  Serial.print("cfg.hi: "); Serial.println(_cfg.moist_hi);

  bt.print("cfg.lo: "); bt.println(_cfg.moist_lo);
  bt.print("cfg.hi: "); bt.println(_cfg.moist_hi);

  Serial.print("time til measure: ");
  Serial.println(TIME_BTW_MEASURE - (millis()/1000 - _prev_measure));
  
  bt.print("time til measure: ");
  bt.println(TIME_BTW_MEASURE - (millis()/1000 - _prev_measure));

  Serial.print("_pump_running: "); Serial.println(_pump_running);
  Serial.print("_force_pump: "); Serial.println(_force_pump);

  bt.print("_pump_running: "); bt.println(_pump_running);
  bt.print("_force_pump: "); bt.println(_force_pump);

  Serial.print("_pump_t: "); Serial.println(_pump_t);
  Serial.print("_prev_pump_t: "); Serial.println(_prev_pump_t);

  bt.print("_pump_t: "); bt.println(_pump_t);
  bt.print("_prev_pump_t: "); bt.println(_prev_pump_t);

  Serial.print("_moist_sat: "); Serial.println(_moist_sat);
  Serial.print("_moist_sat: "); Serial.println(_moist_sat);

  bt.print("_moist_sat: "); bt.println(_moist_sat);
  bt.print("_moist_sat: "); bt.println(_moist_sat);
}

/* ---------------------------------------------------------------- bluetooth */
void bluetooth() {
  uint8_t  e;
  uint16_t v;
  
  if( bt.available() ) {
    e = btrd(&v, BT_TO);

    if(e == OK && v == INIT) {
      btok();
      e = btrd(&v, BT_TO); BTCHK(e);

      switch(v) {
      case RD_MIN  : btok(); rdmin   (); break;
      case RD_MAX  : btok(); rdmax   (); break;
      case RD_LO   : btok(); rdlo    (); break;
      case RD_HI   : btok(); rdhi    (); break;
      case RD_MLVL : btok(); rdmlvl  (); break;
      case RD_WLVL : btok(); rdwlvl  (); break;
      case RD_PID  : btok(); rdpid   (); break;
      case WR_CFG  : btok(); wrcfg   (); break;
      case CALIB   : btok(); calib   (); break;
      case DBG     : btok(); dbg     (); break;
      case PUMP_ON : btok(); pump_on (); break;
      case PUMP_OFF: btok(); pump_off(); break;
      default      : btnok   (); break;
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
  pinMode(PIN_PUMP     , OUTPUT);

  pinMode(PIN_MOIST_DAT,  INPUT);
  pinMode(PIN_ECHO     ,  INPUT);
  
  digitalWrite(PIN_MOIST_PWR, LOW);
  digitalWrite(PIN_TRIG     , LOW);
  digitalWrite(PIN_PUMP     , LOW);
  
  _led.begin();
  _led.show();

  EEPROM.get(CFG_ADDRESS, _cfg);
  _pid_sp = _cfg.moist_max;

  for(int i = 0; i < HIST_SZ; i++) {
    hist_p[i] = 0;
    hist_i[i] = 0;
    hist_d[i] = 0;
  }

  _prev_measure = millis() / 1000;

  TRACELN("========= Configuration =========");
  TRACE("lo:  "); TRACELN(_cfg.moist_lo );
  TRACE("hi:  "); TRACELN(_cfg.moist_hi );
  TRACE("min: "); TRACELN(_cfg.moist_min);
  TRACE("max: "); TRACELN(_cfg.moist_max);

  level();
}

/* --------------------------------------------------------------------- loop */
void loop() {
  uint16_t t;
  uint16_t m;

  bluetooth();  
  t = millis()/1000;
  m = t - _prev_measure;
  
  if(m >= TIME_BTW_MEASURE) {
    _prev_measure = t;
    moist();
    level();
    pid();
  }
  
  pump();
}
