package com.papashkin.myrecipes;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PlaceViewHolder>
        implements Filterable {
    private ArrayList<Recipe> recipeList;
    private ArrayList<Recipe> recipeListCopy;
    private static Context appContext;

//    private int scrHeight;
//    private int scrWidth;

    RVAdapter(ArrayList<Recipe> recipes){
        recipeList = recipes;
        recipeListCopy = recipes;
    }

    private void checkScrSize() {
        WindowManager wm = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
        assert wm != null;
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        int orient = Resources.getSystem().getConfiguration().orientation;
        display.getSize(size);
        if (orient == Configuration.ORIENTATION_PORTRAIT){
//            scrWidth = size.x;
//            scrHeight = size.y;
        } else if (orient == Configuration.ORIENTATION_LANDSCAPE){
//            scrWidth = size.y;
//            scrHeight = size.x;
        }
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        appContext = viewGroup.getContext();
        checkScrSize();
        View v = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.cardview_recipe, viewGroup, false);
//        PlaceViewHolder pvh = new PlaceViewHolder(v);
//        return pvh;
        return new PlaceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder placeViewHolder, int i) {
//        final int index = i;
        placeViewHolder.title.setText(recipeList.get(i).getName());
        placeViewHolder.itemView.setId(recipeList.get(i).getId().intValue());
        Picasso.with(appContext)
                .load(recipeList.get(i).getImageUrl())
                .placeholder(R.drawable.drawable_placeholder)
                .error(R.drawable.food_2_64dp)
                .fit()
                .into(placeViewHolder.img);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public void addItem(Recipe recipe) {
        recipeList.add(recipe);
        recipeListCopy.add(recipe);
        notifyItemInserted(recipeList.size());
    }

    public void removeItem(int position) {
        recipeList.remove(position);
        recipeListCopy.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, recipeList.size());
    }

    public void changeTitle(String newTitle, int pos){
        recipeList.get(pos).setName(newTitle);
        recipeListCopy.get(pos).setName(newTitle);
        notifyDataSetChanged();
    }

    public Long getId(){
        return this.getId();
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

    @Override
    public Filter getFilter(){
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString().toLowerCase();
                if (charString.isEmpty()) {
                    recipeList = recipeListCopy;
                } else {
                    ArrayList<Recipe> filteredList = new ArrayList<>();
                    for (Recipe row : recipeListCopy) {
                        if (row.getName().toLowerCase().contains(charString)) {
                            filteredList.add(row);
                        }
                    }
                    recipeList = filteredList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = recipeList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                recipeList = (ArrayList<Recipe>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}