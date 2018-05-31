package se.viltefjall.tekk.ermina.SelectDevice;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import se.viltefjall.tekk.ermina.R;
import se.viltefjall.tekk.ermina.common.ErminaDevice;
import se.viltefjall.tekk.ermina.common.ErminaDevices;

class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    @SuppressWarnings("unused")
    public static final String  ID = "DeviceAdapter";

    private ErminaDevices mDevices;
    private Context       mContext;
    private RecyclerView  mRecyclerView;


    DeviceAdapter(ErminaDevices devices, Context context, RecyclerView recyclerView) {
        mDevices      = devices;
        mContext      = context;
        mRecyclerView = recyclerView;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @SuppressWarnings("unused")
        public static final String ID = "ViewHolder";

        CardView            mCardView;
        TextView            mDevName;
        TextView            mDevAddr;
        ImageView           mDevImg;

        ViewHolder(View view) {
            super(view);
            mCardView = view.findViewById(R.id.devCard);
            mDevName  = view.findViewById(R.id.devName);
            mDevAddr  = view.findViewById(R.id.devAddress);
            mDevImg   = view.findViewById(R.id.devImg);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.card_select, parent, false
        );
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        DeviceOnClickListener onClickListener;
        final ErminaDevice    dev;

        dev             = mDevices.get(position);
        onClickListener = new DeviceOnClickListener(mContext, mDevices.get(position));

        holder.mDevName.setText(dev.getName());
        holder.mDevAddr.setText(dev.getAddress());
        holder.mDevImg.setImageResource(android.R.drawable.stat_sys_data_bluetooth);
        holder.mDevImg.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        holder.mCardView.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }
}
