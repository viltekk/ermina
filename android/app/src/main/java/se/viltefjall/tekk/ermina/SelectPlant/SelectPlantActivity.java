package se.viltefjall.tekk.ermina.SelectPlant;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;

import se.viltefjall.tekk.ermina.R;
import se.viltefjall.tekk.ermina.common.ErrorDialog;

public class SelectPlantActivity extends Activity {
    @SuppressWarnings("unused")
    public static final String ID = "SelectPlantActivity";

    ErrorDialog                mError;
    RecyclerView               mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    PlantAdapter               mPlantAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_select_plant);
        setTitle(R.string.SelectPlantTitle);
        mError = new ErrorDialog(this);
        loadPlants();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRecyclerView  = null;
        mLayoutManager = null;
        mPlantAdapter  = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlants();
    }

    private void loadPlants() {
        PlantsXMLParser parser = new PlantsXMLParser(getString(R.string.PlantsURL));
        (new DownloadXmlTask(this)).execute(parser);
    }

    void build(Plants plants) {
        mRecyclerView  = findViewById(R.id.RecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mPlantAdapter  = new PlantAdapter(plants, this, mRecyclerView);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mPlantAdapter);
    }
}
