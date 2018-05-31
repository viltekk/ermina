package se.viltefjall.tekk.ermina.common;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.util.Random;

public class DummyDevice implements ErminaDevice {
    @SuppressWarnings("unused")
    public static final String ID = "DummyDevice";

    private String mName;
    private String mAddress;
    private long   mBtCommTime;
    private Random mRng;
    private int    mThrLo;
    private int    mThrHi;

    @SuppressWarnings("WeakerAccess")
    DummyDevice(Parcel parcel) {
        mName       = parcel.readString();
        mAddress    = parcel.readString();
        mBtCommTime = 100;
        mRng        = new Random();
    }

    DummyDevice(String name, String address) {
        mName    = name;
        mAddress = address;
    }

    private void btComm() {
        SystemClock.sleep(mBtCommTime);
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getAddress() {
        return mAddress;
    }

    @Override
    public int getMoistureMin() throws IOException {
        return 1000;
    }

    @Override
    public int getMoistureMax() throws IOException {
        return 100;
    }

    @Override
    public int getMoistureThrLow() {
        btComm();
        mThrLo = mRng.nextInt(50);
        return mThrLo;
    }

    @Override
    public int getMoistureThrHigh() {
        btComm();
        mThrHi = mRng.nextInt(51) + 50;
        return mThrHi;
    }

    @Override
    public void setMoistureThr(int lo, int hi) throws IOException {
        btComm();
    }

    @Override
    public int getMoisture() {
        btComm();
        return mThrLo + mRng.nextInt(mThrHi - mThrLo);
    }

    @Override
    public int getWater() {
        btComm();
        return mRng.nextInt(101);
    }

    @Override
    public void calibrateDry() throws IOException {
        btComm();
    }

    @Override
    public void calibrateWet() throws IOException {
        btComm();
    }

    @Override
    public void connect() throws IOException {
        btComm();
    }

    @Override
    public void disconnect() {}

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mName);
        parcel.writeString(mAddress);
    }

    public static final Parcelable.Creator<DummyDevice> CREATOR = new Parcelable.Creator<DummyDevice>() {
        @Override
        public DummyDevice createFromParcel(Parcel parcel) {
            return new DummyDevice(parcel);
        }

        @Override
        public DummyDevice[] newArray(int size) {
            return new DummyDevice[size];
        }
    };
}
