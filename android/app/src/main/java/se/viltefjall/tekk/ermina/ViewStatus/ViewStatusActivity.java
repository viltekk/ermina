package se.viltefjall.tekk.ermina.ViewStatus;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import java.io.IOException;
import java.lang.ref.WeakReference;

import se.viltefjall.tekk.ermina.R;
import se.viltefjall.tekk.ermina.SelectPlant.SelectPlantActivity;
import se.viltefjall.tekk.ermina.common.ConnectResult;
import se.viltefjall.tekk.ermina.common.ConnectTask;
import se.viltefjall.tekk.ermina.common.ErminaDevice;
import se.viltefjall.tekk.ermina.common.ErrorDialog;
import se.viltefjall.tekk.ermina.common.MoistureView;
import se.viltefjall.tekk.ermina.common.WaterView;

public class ViewStatusActivity extends Activity {

    public static final String  SELECTED_DEVICE = "se.viltefjall.tekk.ermina.SELECTED_DEVICE";
    private static final String ID              = "ViewStatusActivity";

    AnimationManager mAnimMgr;
    ErminaDevice     mDevice;
    ErrorDialog      mError;
    int              mThrLo;
    int              mThrHi;
    int              mMoist;
    int              mWater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_view_status);
        build();
        setTitle(mDevice.getName() + " @ " + mDevice.getAddress());
        connectAndReload();
    }

    public void reload(View view) {
        mAnimMgr.hideStatus();
        mAnimMgr.showConnecting();
    }

    void connectAndReload() {
        if(!mDevice.isConnected()) {
            new ViewStatusConnectTask(this).execute(mDevice);
        } else {
            new GetDataTask(this).execute(mDevice);
        }
    }

    public void settings(View view) {
        Intent intent = new Intent(this, SelectPlantActivity.class);
        intent.putExtra(ViewStatusActivity.SELECTED_DEVICE, mDevice);
        startActivity(intent);
    }

    void setValues() {
        MoistureView mv = findViewById(R.id.moistureView);
        WaterView    wv = findViewById(R.id.waterView);

        mv.setRange(mThrLo, mThrHi);
        mv.setCur(mMoist);
        wv.setWater(mWater);
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            mDevice.disconnect();
        } catch (IOException e) {
            Log.d(ID, e.getMessage());
        }
    }

    private void build() {
        mDevice  = getIntent().getParcelableExtra(SELECTED_DEVICE);
        mAnimMgr = new AnimationManager(this);
        mError   = new ErrorDialog(this);
    }

    private static class ViewStatusConnectTask extends ConnectTask {
        private WeakReference<ViewStatusActivity> mActivity;

        ViewStatusConnectTask(ViewStatusActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void onConnectComplete(ConnectResult result) {
            ViewStatusActivity a = mActivity.get();
            if(result.mException == null) {
                new GetDataTask(a).execute(a.mDevice);
            } else {
                String e = a.getString(R.string.failedConnect) + " " + a.mDevice.getName();
                a.mError.displayError(e, true);
            }
        }
    }

    private static class GetDataTask extends AsyncTask<ErminaDevice, Integer, DataResult> {
        private WeakReference<ViewStatusActivity> mActivity;

        GetDataTask(ViewStatusActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        protected DataResult doInBackground(ErminaDevice... erminaDevices) {
            DataResult res = new DataResult();
            res.mException = null;

            if(erminaDevices.length != 1) {
                res.mException = new Exception("failed to connectAndReload");
            } else {
                try {
                    res.mThrHi = erminaDevices[0].getMoistureThrHigh();
                    res.mThrLo = erminaDevices[0].getMoistureThrLow();
                    res.mWater = erminaDevices[0].getWater();
                    res.mMoist = erminaDevices[0].getMoisture();
                } catch (Exception e) {
                    res.mException = e;
                }
            }

            return res;
        }

        @Override
        protected void onPostExecute(DataResult result) {
            ViewStatusActivity a = mActivity.get();

            if(result.mException == null) {
                a.mThrHi = result.mThrHi;
                a.mThrLo = result.mThrLo;
                a.mWater = result.mWater;
                a.mMoist = result.mMoist;

                a.mAnimMgr.hideConnecting();
                a.mAnimMgr.showStatus();
            } else {
                String e = "failed to get values";
                a.mError.displayError(e, true);
            }
        }
    }

    private static class DataResult {
        Exception mException;
        int mThrHi;
        int mThrLo;
        int mWater;
        int mMoist;
    }
}
