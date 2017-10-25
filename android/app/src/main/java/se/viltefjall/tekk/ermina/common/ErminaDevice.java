package se.viltefjall.tekk.ermina.common;

import android.os.Parcelable;

import java.io.IOException;

/**
 * Created by danial on 2017-07-19.
 */

public interface ErminaDevice extends Parcelable {
    String getName();
    String getAddress();
    int getMoistureMin();
    int getMoistureMax();
    int getMoistureThrLow();
    int getMoistureThrHigh();
    int getWaterMin();
    int getWaterMax();
    int getMoisture();
    int getWater();
    void connect() throws IOException;
    void disconnect() throws IOException;
    boolean isConnected();
}
