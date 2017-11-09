package se.viltefjall.tekk.ermina.common;

import android.os.Parcelable;

import java.io.IOException;

public interface ErminaDevice extends Parcelable {
    String getName();
    String getAddress();
    int getMoistureThrLow() throws IOException;
    int getMoistureThrHigh() throws IOException;
    void setThr(int lo, int hi) throws IOException;
    int getMoisture() throws IOException;
    int getWater() throws IOException;
    void connect() throws IOException;
    void disconnect() throws IOException;
    boolean isConnected();
}
