package se.viltefjall.tekk.ermina.SelectPlant;

class Plant {
    String mName;
    String mLatinName;
    int    mThrHi;
    int    mThrLo;

    Plant(String name,
                 String latinName,
                 int thrHi,
                 int thrLo) {
        mName      = name;
        mLatinName = latinName;
        mThrHi     = thrHi;
        mThrLo     = thrLo;
    }
}
