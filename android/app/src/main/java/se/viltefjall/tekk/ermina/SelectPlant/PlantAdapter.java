package se.viltefjall.tekk.ermina.SelectPlant;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import se.viltefjall.tekk.ermina.R;

class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.ViewHolder> {
    @SuppressWarnings("unused")
    public static final String  ID = "PlantAdapter";

    private Plants mPlants;
    private Context       mContext;
    private RecyclerView  mRecyclerView;


    PlantAdapter(Plants plants, Context context, RecyclerView recyclerView) {
        mPlants       = plants;
        mContext      = context;
        mRecyclerView = recyclerView;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        @SuppressWarnings("unused")
        public static final String ID = "ViewHolder";

        CardView            mCardView;
        TextView            mDevName;
        TextView            mDevAddr;
        ImageView           mDevImg;

        ViewHolder(View view) {
            super(view);
            mCardView = view.findViewById(R.id.devCard);
            mDevName  = view.findViewById(R.id.devName);
            mDevAddr  = view.findViewById(R.id.devAddress);
            mDevImg   = view.findViewById(R.id.devImg);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.card_select, parent, false
        );
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        PlantOnClickListener onClickListener;
        final Plant          plant;

        plant           = mPlants.get(position);
        onClickListener = new PlantOnClickListener(mContext,
                                                   mRecyclerView,
                                                   mPlants.get(position));

        holder.mDevName.setText(plant.mName);
        holder.mDevAddr.setText(plant.mLatinName);
        holder.mDevImg.setImageResource(R.mipmap.plant);
        holder.mDevImg.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        holder.mCardView.setOnClickListener(onClickListener);
    }

    @Override
    public int getItemCount() {
        return mPlants.size();
    }
}
