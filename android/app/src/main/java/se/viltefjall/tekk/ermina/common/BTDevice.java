package se.viltefjall.tekk.ermina.common;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.UUID;

public class BTDevice implements Parcelable, ErminaDevice {
    public static final  String          ID      = "BTDevice";
    private static final String          BT_UUID = "00001101-0000-1000-8000-00805f9b34fb";
    private static       BluetoothSocket mSocket;

    private long            mLastConnect;
    private String          mName;
    private String          mAddr;
    private BluetoothDevice mDevice;

    private InputStream     mInputStream;
    private OutputStream    mOutputStream;
    private PrintStream     mPrintStream;
    private BufferedReader  mBufferedReader;

    BTDevice(String name, String addr, BluetoothDevice device) {
        mName        = name;
        mAddr        = addr;
        mDevice      = device;
        mLastConnect = 0;
    }

    BTDevice(Parcel parcel) {
        mLastConnect = parcel.readLong();
        mName        = parcel.readString();
        mAddr        = parcel.readString();
        mDevice      = parcel.readParcelable(BluetoothDevice.class.getClassLoader());
    }

    public String getName() {
        return mName;
    }

    public String getAddress() {
        return mAddr;
    }

    @Override
    public int getMoistureMin() throws IOException {
        int rsp;
        setStreams();
        mPrintStream.println(ErminaDevice.INIT_COMM); chk();
        mPrintStream.println(ErminaDevice.RD_MIN); chk();
        rsp = Integer.parseInt( mBufferedReader.readLine() );
        Log.e(ID, "getMoistureMin");
        return rsp;
    }

    @Override
    public int getMoistureMax() throws IOException {
        int rsp;
        setStreams();
        mPrintStream.println(ErminaDevice.INIT_COMM); chk();
        mPrintStream.println(ErminaDevice.RD_MAX); chk();
        rsp = Integer.parseInt( mBufferedReader.readLine() );
        Log.e(ID, "getMoistureMax");
        return rsp;
    }

    @Override
    public int getMoistureThrLow() throws IOException {
        int rsp;
        setStreams();
        mPrintStream.println(ErminaDevice.INIT_COMM); chk();
        mPrintStream.println(ErminaDevice.RD_LO); chk();
        rsp = Integer.parseInt( mBufferedReader.readLine() );
        Log.e(ID, "getMoistureThrLow");
        return rsp;
    }

    @Override
    public int getMoistureThrHigh() throws IOException {
        int rsp;
        setStreams();
        mPrintStream.println(ErminaDevice.INIT_COMM); chk();
        mPrintStream.println(ErminaDevice.RD_HI); chk();
        rsp = Integer.parseInt( mBufferedReader.readLine() );
        Log.e(ID, "getMoistureThrHigh");
        return rsp;
    }

    @Override
    public void setMoistureThr(int lo, int hi) throws IOException {
        setStreams();
        mPrintStream.println(ErminaDevice.INIT_COMM); chk();
        mPrintStream.println(ErminaDevice.WR_CFG); chk();
        mPrintStream.println(lo); chk();
        mPrintStream.println(hi); chk();
    }

    @Override
    public int getMoisture() throws IOException {
        int rsp;
        setStreams();
        mPrintStream.println(ErminaDevice.INIT_COMM); chk();
        mPrintStream.println(ErminaDevice.RD_MLVL); chk();
        rsp = Integer.parseInt( mBufferedReader.readLine() );
        return rsp;
    }

    @Override
    public int getWater() throws IOException {
        int rsp;
        setStreams();
        mPrintStream.println(ErminaDevice.INIT_COMM); chk();
        mPrintStream.println(ErminaDevice.RD_WLVL); chk();
        rsp = Integer.parseInt( mBufferedReader.readLine() );
        return rsp;
    }

    @Override
    public void calibrateDry() throws IOException {
        setStreams();
        mPrintStream.println(ErminaDevice.INIT_COMM); chk();
        mPrintStream.println(ErminaDevice.CALIB); chk();
    }

    @Override
    public void calibrateWet() throws IOException {
        setStreams();
        mPrintStream.println(ErminaDevice.CALIB); chk();
    }

    public synchronized void connect() throws IOException {
        UUID uuid;

        if( !isConnected() ) {
            uuid = UUID.fromString(BT_UUID);
            mSocket = mDevice.createRfcommSocketToServiceRecord(uuid);
            mSocket.connect();
        }
        setStreams();
    }

    private void setStreams() throws IOException {
        if( isConnected() ) {
            if (mInputStream    == null) mInputStream    = mSocket.getInputStream();
            if (mOutputStream   == null) mOutputStream   = mSocket.getOutputStream();
            if (mPrintStream    == null) mPrintStream    = new PrintStream(mOutputStream);
            if (mBufferedReader == null) mBufferedReader = new BufferedReader(
                    new InputStreamReader(mInputStream)
            );
        }
    }

    public void disconnect() {
        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
            if (mOutputStream != null) {
                mOutputStream.close();
            }
            if (mSocket != null) {
                mSocket.close();
            }
        } catch(Exception e) {
            Log.e(ID, e.getMessage());
        }
    }

    public boolean isConnected() {
        return mSocket != null && mSocket.isConnected();
    }

    @SuppressWarnings("unused")
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(mLastConnect);
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

    private void chk() throws IOException {
        int rsp;
        rsp = Integer.parseInt( mBufferedReader.readLine() );
        if(rsp == ErminaDevice.NOK) {
            throw new IOException("Server replied NOK");
        }
    }
}
