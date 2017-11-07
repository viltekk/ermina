package se.viltefjall.tekk.ermina.SelectPlant;

import java.util.ArrayList;

public class Plants {
    @SuppressWarnings("unused")
    private static final String ID = "Plants";

    private ArrayList<Plant> mPlants;

    Plants() {
        mPlants = new ArrayList<>();
    }

    void addPlant(Plant p) {
        mPlants.add(p);
    }

    Plant get(int pos) {
        return mPlants.get(pos);
    }

    public int size() {
        return mPlants.size();
    }
}
