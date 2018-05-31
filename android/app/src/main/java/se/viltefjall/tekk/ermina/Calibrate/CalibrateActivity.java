package se.viltefjall.tekk.ermina.Calibrate;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.lang.ref.WeakReference;

import se.viltefjall.tekk.ermina.R;
import se.viltefjall.tekk.ermina.ViewStatus.ViewStatusActivity;
import se.viltefjall.tekk.ermina.common.ConnectResult;
import se.viltefjall.tekk.ermina.common.ConnectTask;
import se.viltefjall.tekk.ermina.common.ErminaDevice;
import se.viltefjall.tekk.ermina.common.ErrorDialog;

public class CalibrateActivity extends Activity {

    public  static final String SELECTED_DEVICE = "se.viltefjall.tekk.ermina.SELECTED_DEVICE";
    private static final String ID              = "CalibrateActivity";

    private int              mStep;
    private ErminaDevice     mDevice;
    private AnimationManager mAnimMgr;
    private ErrorDialog      mError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate);

        mDevice  = getIntent().getParcelableExtra(SELECTED_DEVICE);
        mStep    = 0;
        mAnimMgr = new AnimationManager(this);
        mError   = new ErrorDialog(this);
    }

    public void next(View v) {
        switch (mStep) {
            case 0:
                mAnimMgr.step0();
                mStep = 1;
                break;
            case 1:
                mAnimMgr.step1();
                new CalibrateConnectTask(this).execute(mDevice);
                mStep = 2;
                break;
            case 2:
                mAnimMgr.step2();
                new WetTask(this).execute(mDevice);
                mStep = 0;
                break;
        }
    }

    private static class CalibrateConnectTask extends ConnectTask {
        private WeakReference<CalibrateActivity> mActivity;

        CalibrateConnectTask(CalibrateActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void onConnectComplete(ConnectResult result) {
            CalibrateActivity a = mActivity.get();
            if(result.mException == null) {
                new DryTask(a).execute(a.mDevice);
            } else {
                String e = a.getString(R.string.failedConnect) + " " + a.mDevice.getName();
                a.mError.displayError(e, true);
            }
        }
    }

    private static class DryTask extends AsyncTask<ErminaDevice, Integer, CalibrateActivity.DataResult> {
        private WeakReference<CalibrateActivity> mActivity;

        DryTask(CalibrateActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        protected CalibrateActivity.DataResult doInBackground(ErminaDevice... erminaDevices) {
            CalibrateActivity.DataResult res = new DataResult();
            res.mException = null;

            if(erminaDevices.length != 1) {
                res.mException = new Exception("failed to calibrate");
            } else {
                try {
                    erminaDevices[0].calibrateDry();
                } catch (Exception e) {
                    res.mException = e;
                }
            }

            return res;
        }

        @Override
        protected void onPostExecute(CalibrateActivity.DataResult result) {
            CalibrateActivity a = mActivity.get();
            if(result.mException == null) {
                a.mAnimMgr.step1Done();
            } else {
                a.mError.displayError(result.mException.getMessage(), true);
            }
        }
    }

    private static class WetTask extends AsyncTask<ErminaDevice, Integer, CalibrateActivity.DataResult> {
        private WeakReference<CalibrateActivity> mActivity;

        WetTask(CalibrateActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        protected CalibrateActivity.DataResult doInBackground(ErminaDevice... erminaDevices) {
            CalibrateActivity.DataResult res = new DataResult();
            res.mException = null;

            if(erminaDevices.length != 1) {
                res.mException = new Exception("failed to calibrate");
            } else {
                try {
                    erminaDevices[0].calibrateWet();
                } catch (Exception e) {
                    res.mException = e;
                }
            }

            return res;
        }

        @Override
        protected void onPostExecute(CalibrateActivity.DataResult result) {
            CalibrateActivity a = mActivity.get();

            if(result.mException == null) {
                Intent intent = new Intent(a, ViewStatusActivity.class);
                intent.putExtra(ViewStatusActivity.SELECTED_DEVICE, a.mDevice);
                a.startActivity(intent);
            } else {
                a.mError.displayError(result.mException.getMessage(), true);
            }
        }
    }

    private static class DataResult {
        Exception mException;
    }
}
