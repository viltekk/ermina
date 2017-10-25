package se.viltefjall.tekk.ermina.common;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.UUID;

public class BTDevice implements Parcelable, ErminaDevice {
    public static final String  ID      = "BTDevice";
    private static final String BT_UUID = "00001101-0000-1000-8000-00805f9b34fb";

    private String          mName;
    private String          mAddr;
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    private InputStream     mInputStream;
    private OutputStream    mOutputStream;
    private PrintStream     mPrintStream;
    private BufferedReader  mBufferedReader;

    BTDevice(String name, String addr, BluetoothDevice device) {
        mName   = name;
        mAddr   = addr;
        mDevice = device;
    }

    BTDevice(Parcel parcel) {
        mName   = parcel.readString();
        mAddr   = parcel.readString();
        mDevice = parcel.readParcelable(BluetoothDevice.class.getClassLoader());
    }

    public String getName() {
        return mName;
    }

    public String getAddress() {
        return mAddr;
    }

    @Override
    public int getMoistureMin() {
        return 0;
    }

    @Override
    public int getMoistureMax() {
        return 0;
    }

    @Override
    public int getMoistureThrLow() {
        return 0;
    }

    @Override
    public int getMoistureThrHigh() {
        return 0;
    }

    @Override
    public int getWaterMin() {
        return 0;
    }

    @Override
    public int getWaterMax() {
        return 0;
    }

    @Override
    public int getMoisture() {
        return 0;
    }

    @Override
    public int getWater() {
        return 0;
    }

    public void connect() throws IOException {
        UUID uuid;

        uuid    = UUID.fromString(BT_UUID);
        mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
        mSocket.connect();

        mInputStream    = mSocket.getInputStream();
        mOutputStream   = mSocket.getOutputStream();
        mPrintStream    = new PrintStream(mOutputStream);
        mBufferedReader = new BufferedReader(new InputStreamReader(mInputStream));
    }

    public void disconnect() throws IOException {
        if(mInputStream != null)  { mInputStream.close();  }
        if(mOutputStream != null) { mOutputStream.close(); }
        if(mSocket != null)       { mSocket.close();       }
    }

    public boolean isConnected() {
        return mSocket != null && mSocket.isConnected();
    }

    @SuppressWarnings("unused")
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mName);
        parcel.writeString(mAddr);
        parcel.writeParcelable(mDevice, flags);
    }

    public static final Parcelable.Creator<BTDevice> CREATOR = new Parcelable.Creator<BTDevice>() {
        @Override
        public BTDevice createFromParcel(Parcel parcel) {
            return new BTDevice(parcel);
        }

        @Override
        public BTDevice[] newArray(int size) {
            return new BTDevice[size];
        }
    };
}
