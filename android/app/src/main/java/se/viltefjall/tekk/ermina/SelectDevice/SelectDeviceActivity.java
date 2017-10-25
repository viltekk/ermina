package se.viltefjall.tekk.ermina.SelectDevice;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;

import se.viltefjall.tekk.ermina.R;
import se.viltefjall.tekk.ermina.common.DummyDevices;

public class SelectDeviceActivity extends Activity {
    @SuppressWarnings("unused")
    public static final String ID = "SelectDeviceActivity";

    RecyclerView               mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    DeviceAdapter              mDeviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_select_device);
        setTitle(R.string.SelectDeviceTitle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        build();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRecyclerView  = null;
        mLayoutManager = null;
        mDeviceAdapter = null;
    }

    public void build() {
        //BTDevices devices = new BTDevices();
        DummyDevices devices = new DummyDevices();
        mRecyclerView     = findViewById(R.id.RecyclerView);
        mLayoutManager    = new LinearLayoutManager(this);
        mDeviceAdapter    = new DeviceAdapter(devices, this, mRecyclerView);

        devices.populate(mDeviceAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mDeviceAdapter);
    }
}
