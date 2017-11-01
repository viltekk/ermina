package se.viltefjall.tekk.ermina.ViewStatus;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import java.io.IOException;
import java.lang.ref.WeakReference;

import se.viltefjall.tekk.ermina.R;
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
        setTitle("Connecting to " + mDevice.getName());
        connect();
    }

    public void reload(View view) {
        setTitle("Reloading " + mDevice.getName());
        mAnimMgr.hideStatus();
        mAnimMgr.showConnecting();
    }

    void connect() {
        new ViewStatusConnectTask(this).execute(mDevice);
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
                a.setTitle(a.mDevice.getName() + " @ " + a.mDevice.getAddress());

                a.mThrLo = a.mDevice.getMoistureThrLow();
                a.mThrHi = a.mDevice.getMoistureThrHigh();
                a.mMoist = a.mDevice.getMoisture();
                a.mWater = a.mDevice.getWater();

                a.mAnimMgr.hideConnecting();
                a.mAnimMgr.showStatus();
            } else {
                String e = a.getString(R.string.failedConnect) + " " + a.mDevice.getName();
                a.mError.displayError(e, true);
            }
        }
    }
}
