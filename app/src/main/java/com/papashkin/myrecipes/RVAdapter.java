package com.papashkin.myrecipes;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
//import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PlaceViewHolder> {
    private ArrayList<String> recipeTitles;
    private static ArrayList<String> imageURLs;
    private static ArrayList<Integer> IDs;
    private static Context appContext;

    RVAdapter(ArrayList<String> recipes, ArrayList<String> imgUrls, ArrayList<Integer> ids){
        this.recipeTitles = recipes;
        imageURLs = imgUrls;
        IDs = ids;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        appContext = viewGroup.getContext();
        View v = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.cardview_recipe, viewGroup, false);
        PlaceViewHolder pvh = new PlaceViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder placeViewHolder, int i) {
        placeViewHolder.title.setText(recipeTitles.get(i));
        placeViewHolder.itemView.setId(IDs.get(i));
        if (!imageURLs.get(i).isEmpty()){
            Picasso.with(appContext)
                    .load(imageURLs.get(i))
                    .placeholder(R.drawable.drawable_placeholder)
                    .into(placeViewHolder.img);
        }

        placeViewHolder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                AsyncTask urlGetter = new Task_getAddressById();
                Object[][] list = {{(long) v.getId(), context}};
                String url = "";
                try {
                    urlGetter.execute(list);
                    url = String.valueOf(urlGetter.get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(context, WebBrowserRecipe.class);
                intent.putExtra("URL", url);
                intent.putExtra("NAME", "just recipe");
                AppCompatActivity activity = (AppCompatActivity) context;
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return recipeTitles.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class PlaceViewHolder extends RecyclerView.ViewHolder{
        CardView cv;
        TextView title;
        ImageView img;

        PlaceViewHolder(View itemView){
            super(itemView);
            cv = itemView.findViewById(R.id.cardview_recipe);
            title = itemView.findViewById(R.id.recipe_title);
            img = itemView.findViewById(R.id.recipe_photo);
        }
    }
}
