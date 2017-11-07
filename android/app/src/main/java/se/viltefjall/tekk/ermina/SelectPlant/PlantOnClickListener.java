package se.viltefjall.tekk.ermina.SelectPlant;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import se.viltefjall.tekk.ermina.ViewStatus.ViewStatusActivity;
import se.viltefjall.tekk.ermina.common.ErminaDevice;

class PlantOnClickListener implements View.OnClickListener {
    @SuppressWarnings("unused")
    private static final String ID = "PlantOnClickListener";

    private RecyclerView mRecyclerView;
    private Context      mContext;
    private Plant        mPlant;

    PlantOnClickListener(Context      context,
                         RecyclerView recyclerView,
                         Plant        plant) {
        mContext       = context;
        mPlant         = plant;
        mRecyclerView  = recyclerView;
    }

    @Override
    public void onClick(View view) {
    }
}
