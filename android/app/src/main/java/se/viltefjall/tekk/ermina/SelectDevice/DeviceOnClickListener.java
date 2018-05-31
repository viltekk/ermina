package se.viltefjall.tekk.ermina.SelectDevice;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import se.viltefjall.tekk.ermina.ViewStatus.ViewStatusActivity;
import se.viltefjall.tekk.ermina.common.ErminaDevice;

class DeviceOnClickListener implements View.OnClickListener {
    @SuppressWarnings("unused")
    private static final String ID = "DeviceOnClickListener";

    private Context      mContext;
    private ErminaDevice mDevice;

    DeviceOnClickListener(Context context, ErminaDevice device) {
        mContext = context;
        mDevice  = device;
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(mContext, ViewStatusActivity.class);
        intent.putExtra(ViewStatusActivity.SELECTED_DEVICE, mDevice);
        mContext.startActivity(intent);
    }
}
