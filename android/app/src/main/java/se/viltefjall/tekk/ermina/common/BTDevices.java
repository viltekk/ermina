package se.viltefjall.tekk.ermina.common;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BTDevices {
    public static final String ID = "BTDevices";
    private ArrayList<BTDevice> mDevices;

    public BTDevices() {
        mDevices = new ArrayList<>();
    }

    public void populate(RecyclerView.Adapter adapter) {
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> btSet = btAdapter.getBondedDevices();

        for(BluetoothDevice dev : btSet) {
            mDevices.add(new BTDevice(dev.getName(), dev.getAddress(), dev));
            adapter.notifyItemInserted(mDevices.size()-1);
        }

    }

    public BTDevice get(int pos) {
        return mDevices.get(pos);
    }

    public List<BTDevice> getDevices() {
        return mDevices;
    }

    public void remove(int pos) {
        mDevices.remove(pos);
    }

    public int size() {
        return mDevices.size();
    }
}
