package se.viltefjall.tekk.ermina.CustomPlant;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.lang.ref.WeakReference;

import se.viltefjall.tekk.ermina.R;
import se.viltefjall.tekk.ermina.ViewStatus.ViewStatusActivity;
import se.viltefjall.tekk.ermina.common.ConnectResult;
import se.viltefjall.tekk.ermina.common.ConnectTask;
import se.viltefjall.tekk.ermina.common.ErminaDevice;
import se.viltefjall.tekk.ermina.common.ErrorDialog;
import se.viltefjall.tekk.ermina.common.MoistureView;

public class CustomPlantActivity extends Activity {
    @SuppressWarnings("unused")
    private static final String ID = "CustomPlantActivity";

    @SuppressWarnings("unused")
    public static final String  SELECTED_DEVICE = "se.viltefjall.tekk.ermina.SELECTED_DEVICE";

    AnimationManager mAnimMgr;
    ErminaDevice     mDevice;
    ErrorDialog      mError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_plant);
        mDevice  = getIntent().getParcelableExtra(SELECTED_DEVICE);
        mError   = new ErrorDialog(this);
        mAnimMgr = new AnimationManager(this);
    }

    public void setCustom(View v) {
        mAnimMgr.setCustom();
    }

    @SuppressWarnings("unused")
    void runTasks() {
        int lo, hi;
        MoistureView v = findViewById(R.id.moistureView);

        hi = (int) v.getThrHi();
        lo = (int) v.getThrLo();

        if(!mDevice.isConnected()) {
            new CustomPlantConnectTask(this, hi, lo).execute(mDevice);
        } else {
            new SetThrTask(this, hi, lo).execute(mDevice);
        }
    }

    static class CustomPlantConnectTask extends ConnectTask {
        private WeakReference<CustomPlantActivity> mActivity;
        private int mHi;
        private int mLo;

        CustomPlantConnectTask(CustomPlantActivity activity, int hi, int lo) {
            mActivity = new WeakReference<>(activity);
            mHi       = hi;
            mLo       = lo;
        }

        @Override
        public void onConnectComplete(ConnectResult result) {
            CustomPlantActivity a = mActivity.get();
            if(result.mException == null) {
                new SetThrTask(a, mHi, mLo).execute(a.mDevice);
            } else {
                String e = a.getString(R.string.failedConnect) + " " + a.mDevice.getName();
                a.mError.displayError(e, false);
            }
        }
    }

    private static class SetThrTask extends AsyncTask<ErminaDevice, Integer, Exception> {
        private static final String ID = "DownloadXmlTask";

        private WeakReference<CustomPlantActivity> mActivity;
        private AnimationManager                   mAnimMgr;
        private ErminaDevice                       mDevice;
        private int                                mHi;
        private int                                mLo;

        SetThrTask(CustomPlantActivity activity, int hi, int lo) {
            mActivity = new WeakReference<>(activity);
            mAnimMgr  = activity.mAnimMgr;
            mLo       = lo;
            mHi       = hi;
        }

        @Override
        protected Exception doInBackground(ErminaDevice... devices) {
            Exception ret = null;
            if(devices.length == 1) {
                try {
                    mDevice = devices[0];
                    devices[0].setThr(mLo, mHi);
                } catch (Exception e) {
                    ret = e;
                    Log.d(ID, e.toString());
                }
            } else {
                ret = new Exception("length = " + devices.length);
            }

            return ret;
        }

        @Override
        protected void onPostExecute(Exception result) {
            CustomPlantActivity a = mActivity.get();
            if(result == null) {
                Intent intent = new Intent(a, ViewStatusActivity.class);
                intent.putExtra(ViewStatusActivity.SELECTED_DEVICE, mDevice);
                a.startActivity(intent);
            } else {
                a.mError.displayError(result.getMessage(), false);
                mAnimMgr.showButton();
            }
        }
    }
}
