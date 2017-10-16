package se.viltefjall.tekk.ermina.SelectBTDevice;

import android.animation.Animator;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import se.viltefjall.tekk.ermina.R;
import se.viltefjall.tekk.ermina.common.BTDevice;

class DeviceOnClickListener implements View.OnClickListener {
    private static final String ID = "DeviceOnClickListener";

    private RecyclerView  mRecyclerView;
    private Context       mContext;
    private BTDevice      mDevice;

    DeviceOnClickListener(Context      context,
                          RecyclerView recyclerView,
                          BTDevice     device) {
        mContext       = context;
        mDevice        = device;
        mRecyclerView  = recyclerView;
    }

    @Override
    public void onClick(View view) {
        final int           start;
        int                 end;
        LinearLayoutManager llm;

        llm      = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        start    = llm.findFirstVisibleItemPosition();
        end      = llm.findLastVisibleItemPosition();

        Log.d(ID, "start: " + start + ", end: " + end);
        
        for(int i = end; i >= start; i--) {
            final int               finalI;
            int                     delay;
            int                     delayOff;
            int                     delayCount;
            RecyclerView.ViewHolder vh;
            final View              v;

            vh         = mRecyclerView.findViewHolderForAdapterPosition(i);
            v          = vh.itemView;
            delayCount = i-end;
            delayOff   = mContext.getResources().getInteger(R.integer.DeviceAnimationDelay);
            delay      = (end-1) * delayOff;
            finalI     = i;

            v.animate()
                    .alpha(0.0f)
                    .translationY(v.getHeight())
                    .setStartDelay(delay - delayCount*delayOff)
                    .setListener(
                            new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    v.setVisibility(View.GONE);
                                    //mDevices.remove(finalI);
                                    //mDeviceAdapter.notifyItemRemoved(finalI);

                                    /*if(finalI == start) {
                                        Intent intent = new Intent(mContext,
                                                ControlDeviceActivity.class);
                                        intent.putExtra(SelectDevice.SELECTED_DEVICE, mDevice);
                                        mContext.startActivity(intent);
                                    }*/
                                }

                                @Override public void onAnimationStart(Animator animator) {}
                                @Override public void onAnimationCancel(Animator animator) {}
                                @Override public void onAnimationRepeat(Animator animator) {}
                            }
                    );
        }
    }
}
