package se.viltefjall.tekk.ermina.common;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DummyDevices implements ErminaDevices {
    public static final String ID = "DummyDevices";
    private ArrayList<ErminaDevice> mDevices;

    public DummyDevices() {
        mDevices = new ArrayList<>();
    }

    @Override
    public ErminaDevice get(int pos) {
        return mDevices.get(pos);
    }

    @Override
    public List<ErminaDevice> getDevices() {
        return mDevices;
    }

    @Override
    public void remove(int pos) {
        mDevices.remove(pos);
    }

    @Override
    public int size() {
        return mDevices.size();
    }

    public void populate(RecyclerView.Adapter adapter) {
        for(int i = 1; i < 20; i++) {
            mDevices.add(new DummyDevice("bt device " + i, "address " + (i*2)));
            adapter.notifyItemInserted(mDevices.size()-1);
        }
    }
}
