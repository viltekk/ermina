package se.viltefjall.tekk.ermina.SelectPlant;

import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;

public class DownloadXmlTask extends AsyncTask<PlantsXMLParser, Integer, Plants> {
    private static final String ID = "DownloadXmlTask";

    private WeakReference<SelectPlantActivity> mActivity;

    DownloadXmlTask(SelectPlantActivity activity) {
        mActivity = new WeakReference<>(activity);
    }

    @Override
    protected Plants doInBackground(PlantsXMLParser... plantsXMLParsers) {
        Plants plants = null;
        if(plantsXMLParsers.length == 1) {
            try {
                plants = plantsXMLParsers[0].getPlants();
            } catch (Exception e) {
                Log.d(ID, e.toString());
                plants = null;
            }
        } else {
            Log.d(ID, "length = " + plantsXMLParsers.length);
        }

        return plants;
    }

    @Override
    protected void onPostExecute(Plants result) {
        SelectPlantActivity a = mActivity.get();
        if(result != null) {
            a.build(result);
        } else {
            a.mError.displayError("Failed to get plants", true);
        }
    }
}
