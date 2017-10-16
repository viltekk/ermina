package se.viltefjall.tekk.ermina.ViewStatus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.Window;

import se.viltefjall.tekk.ermina.R;
import se.viltefjall.tekk.ermina.common.ErminaDevice;

public class ViewStatusActivity extends Activity {

    public static final String SELECTED_DEVICE = "se.viltefjall.tekk.ermina.SELECTED_DEVICE";

    ErminaDevice mDevice;
    View         mMoisture;
    View         mWater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_view_status);

        mDevice = getIntent().getParcelableExtra(SELECTED_DEVICE);
        setTitle(mDevice.getName() + " @ " + mDevice.getAddress());

        mMoisture = findViewById(R.id.moistureViewTop);
        mWater    = findViewById(R.id.moistureViewBtm);
    }
}
