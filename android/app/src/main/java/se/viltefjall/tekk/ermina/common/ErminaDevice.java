package se.viltefjall.tekk.ermina.common;

import android.os.Parcelable;

import java.io.IOException;

public interface ErminaDevice extends Parcelable {
    int OK        = 0;
    int NOK       = 999;
    int INIT_COMM = 100;
    int RD_MIN    = 101;
    int RD_MAX    = 102;
    int RD_LO     = 103;
    int RD_HI     = 104;
    int RD_MLVL   = 105;
    int RD_WLVL   = 106;
    int RD_PID    = 107;
    int WR_CFG    = 200;
    int CALIB     = 201;

    String getName();
    String getAddress();
    int getMoistureMin() throws IOException;
    int getMoistureMax() throws IOException;
    int getMoistureThrLow() throws IOException;
    int getMoistureThrHigh() throws IOException;
    void setMoistureThr(int lo, int hi) throws IOException;
    int getMoisture() throws IOException;
    int getWater() throws IOException;
    void calibrateDry() throws IOException;
    void calibrateWet() throws IOException;
    void connect() throws IOException;
    void disconnect();
    boolean isConnected();
}
