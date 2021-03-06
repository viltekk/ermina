package se.viltefjall.tekk.ermina.ViewStatus;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import java.lang.ref.WeakReference;

import se.viltefjall.tekk.ermina.Calibrate.CalibrateActivity;
import se.viltefjall.tekk.ermina.R;
import se.viltefjall.tekk.ermina.SelectDevice.SelectDeviceActivity;
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
    int              mMin;
    int              mMax;
    int              mThrLo;
    int              mThrHi;
    int              mMoist;
    int              mWater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(ID, "onCreate");
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_view_status);
        build();
        setTitle(mDevice.getName() + " @ " + mDevice.getAddress());
        connectAndReload();
    }

    @Override
    public void onBackPressed() {
        if(mDevice != null) {
            mDevice.disconnect();
        }
        Intent intent = new Intent(this, SelectDeviceActivity.class);
        this.startActivity(intent);
    }

    public void reload(View view) {
        connectAndReload();
    }

    @SuppressWarnings("SpellCheckingInspection")
    public void calib(View view) {
        Intent intent = new Intent(this, CalibrateActivity.class);
        intent.putExtra(CalibrateActivity.SELECTED_DEVICE, mDevice);
        this.startActivity(intent);
    }

    public void settings(View view) {
        Intent intent = new Intent(this, SelectPlantActivity.class);
        intent.putExtra(SelectPlantActivity.SELECTED_DEVICE, mDevice);
        this.startActivity(intent);
    }

    void connectAndReload() {
        Log.d(ID, "connectAndReload");
        mAnimMgr.animReload(AnimationManager.SHOW);
        if(!mDevice.isConnected()) {
            Log.d(ID, "not connected");
            new ViewStatusConnectTask(this).execute(mDevice);
        } else {
            Log.d(ID, "connected");
            new GetDataTask(this).execute(mDevice);
        }
    }

    void setValues() {
        MoistureView mv = findViewById(R.id.moistureView);
        WaterView    wv = findViewById(R.id.waterView);

        float range = mMin - mMax;
        float lo      = 100-(((float)mThrLo-mMax)/range)*100f;
        float hi      = 100-(((float)mThrHi-mMax)/range)*100f;
        float moist   = 100-(((float)mMoist-mMax)/range)*100f;

        Log.e(ID, "range: " + range);
        Log.e(ID, "lo: " + lo);
        Log.e(ID, "hi: " + hi);
        Log.e(ID, "moist: " + moist);

        mv.setRange(lo, hi);
        mv.setCur((int)moist);
        wv.setWater(mWater);
    }

    @Override
    protected void onStop() {
        super.onStop();
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
                    res.mMin   = erminaDevices[0].getMoistureMin();
                    res.mMax   = erminaDevices[0].getMoistureMax();
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
                a.mMin   = result.mMin;
                a.mMax   = result.mMax;
                a.mThrHi = result.mThrHi;
                a.mThrLo = result.mThrLo;
                a.mWater = result.mWater;
                a.mMoist = result.mMoist;

                result.toLog();

                a.mAnimMgr.animReload(AnimationManager.HIDE);
                a.setValues();
            } else {
                Log.e(ID, result.mException.getMessage());
                a.mError.displayError("failed to get values", true);
            }
        }
    }

    private static class DataResult {
        Exception mException;
        int mMin;
        int mMax;
        int mThrHi;
        int mThrLo;
        int mWater;
        int mMoist;

        public void toLog() {
            Log.e(ID, "mMin: " + mMin);
            Log.e(ID, "mMax: " + mMax);
            Log.e(ID, "mThrHi: " + mThrHi);
            Log.e(ID, "mThrLo: " + mThrLo);
            Log.e(ID, "mWater: " + mWater);
            Log.e(ID, "mMoist: " + mMoist);
        }
    }
}
