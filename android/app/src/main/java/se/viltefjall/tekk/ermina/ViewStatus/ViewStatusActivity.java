package se.viltefjall.tekk.ermina.ViewStatus;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

import java.io.IOException;
import java.util.Random;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_view_status);
        build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!mDevice.isConnected()) {
            setTitle("Connecting to " + mDevice.getName());
            mAnimMgr.hideStatus();
            mAnimMgr.showConnecting();

            ConnectTask ct = new ConnectTask() {
                @Override
                public void onConnectComplete(ConnectResult result) {
                    if(result.mException == null) {
                        setTitle(mDevice.getName() + " @ " + mDevice.getAddress());
                        mAnimMgr.hideConnecting();
                        mAnimMgr.showStatus();
                    } else {
                        String e = getString(R.string.failedConnect) + " " + mDevice.getName();
                        mError.displayError(e, true);
                    }
                }
            };
            ct.execute(mDevice);
        }
    }

    @SuppressWarnings("unused")
    void tmp(View view) {
        Random r = new Random();

        // moisture
        int min, max, v;
        v   = r.nextInt(100) + 1;
        min = r.nextInt(50);
        max = r.nextInt(50) + 51;
        MoistureView mv = findViewById(R.id.moistureView);
        mv.setRange(min, max);
        mv.setCur(v);

        // water
        int lvl = r.nextInt(100) + 1;
        WaterView wv = findViewById(R.id.waterView);
        wv.setWater(lvl);
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
}
