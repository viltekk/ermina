package se.viltefjall.tekk.ermina.common;

import android.os.AsyncTask;

import java.io.IOException;

public abstract class ConnectTask extends AsyncTask<ErminaDevice, Integer, ConnectResult>
    implements ConnectIF {

    protected ConnectTask() {}

    @Override
    protected ConnectResult doInBackground(ErminaDevice... erminaDevices) {
        ConnectResult res = new ConnectResult();

        if(erminaDevices.length != 1) {
            res.mException = new Exception("failed to connect");
        } else {
            try {
                erminaDevices[0].connect();
            } catch (IOException e) {
                res.mException = e;
            }
        }

        return res;
    }

    protected void onPostExecute(ConnectResult res) {
        onConnectComplete(res);
    }

    @Override
    public abstract void onConnectComplete(ConnectResult result);
}
