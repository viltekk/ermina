package se.viltefjall.tekk.ermina.common;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import java.io.IOException;

public class DummyDevice implements ErminaDevice {
    @SuppressWarnings("unused")
    public static final String ID = "DummyDevice";

    private String mName;
    private String mAddress;
    private long   mBtCommTime;

    public DummyDevice(Parcel parcel) {
        mName       = parcel.readString();
        mAddress    = parcel.readString();
        mBtCommTime = 1000;
    }

    public DummyDevice(String name, String address) {
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
    public void connect() throws IOException {
        btComm();
    }

    @Override
    public void disconnect() throws IOException {}

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
