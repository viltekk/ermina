package se.viltefjall.tekk.ermina.common;

import android.os.Parcelable;

import java.io.IOException;

/**
 * Created by danial on 2017-07-19.
 */

public interface ErminaDevice extends Parcelable {
    String getName();
    String getAddress();
    void connect() throws IOException;
    void disconnect() throws IOException;
    boolean isConnected();
}
