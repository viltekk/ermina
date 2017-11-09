package se.viltefjall.tekk.ermina.SelectPlant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;

import se.viltefjall.tekk.ermina.CustomPlant.CustomPlantActivity;
import se.viltefjall.tekk.ermina.R;
import se.viltefjall.tekk.ermina.ViewStatus.ViewStatusActivity;
import se.viltefjall.tekk.ermina.common.ErminaDevice;
import se.viltefjall.tekk.ermina.common.ErrorDialog;

public class SelectPlantActivity extends Activity {
    @SuppressWarnings("unused")
    public static final String SELECTED_DEVICE = "se.viltefjall.tekk.ermina.SELECTED_DEVICE";

    @SuppressWarnings("unused")
    public static final String ID = "SelectPlantActivity";

    ErminaDevice               mDevice;
    ErrorDialog                mError;
    AnimationManager           mAnimMgr;
    RecyclerView               mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    PlantAdapter               mPlantAdapter;
    PlantsXMLParser            mParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        setContentView(R.layout.activity_select_plant);
        setTitle(R.string.SelectPlantTitle);

        mRecyclerView  = null;
        mLayoutManager = null;
        mAnimMgr       = new AnimationManager(this);
        mError         = new ErrorDialog(this);
        mParser        = new PlantsXMLParser(getString(R.string.PlantsURL));
        mDevice        = getIntent().getParcelableExtra(SELECTED_DEVICE);

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

    public void custom(View v) {
        Intent intent = new Intent(this, CustomPlantActivity.class);
        intent.putExtra(CustomPlantActivity.SELECTED_DEVICE, mDevice);
        startActivity(intent);
    }

    public void reload(View v) {
        Log.d(ID, "reload");
        mAnimMgr.hideList();
        mAnimMgr.showConnecting();
    }

    void loadPlants() {
        Log.d(ID, "loadPlants");
        (new DownloadXmlTask(this)).execute(mParser);
    }

    void build(Plants plants) {
        Log.d(ID, "build");
        if(mLayoutManager == null) {
            mLayoutManager = new LinearLayoutManager(this);
        }

        if(mRecyclerView == null) {
            mRecyclerView = findViewById(R.id.plants);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(mLayoutManager);
        }
        mPlantAdapter = new PlantAdapter(plants, this, mRecyclerView);

        mRecyclerView.setAdapter(mPlantAdapter);
        mAnimMgr.hideConnecting();
        mAnimMgr.showList();
    }
}
