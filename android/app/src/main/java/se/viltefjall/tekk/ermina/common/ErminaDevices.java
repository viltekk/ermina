package se.viltefjall.tekk.ermina.common;

import java.util.List;

public interface ErminaDevices {
    public ErminaDevice get(int pos);
    public List<ErminaDevice> getDevices();
    public void remove(int pos);
    public int size();
}
