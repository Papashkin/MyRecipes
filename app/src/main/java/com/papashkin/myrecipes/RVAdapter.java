package com.papashkin.myrecipes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PlaceViewHolder> {
    private ArrayList<String> recipeTitles;
    private ArrayList<String> imageURLs;
    private ArrayList<Long> IDs;
    private static Context appContext;

    private int scrHeight;
    private int scrWidth;

    RVAdapter(ArrayList<String> recipes, ArrayList<String> imgUrls, ArrayList<Long> ids){
        this.recipeTitles = recipes;
        this.IDs = ids;
        this.imageURLs = imgUrls;
    }

    private void checkScrSize() {
        WindowManager wm = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
        assert wm != null;
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        scrWidth = size.x;
        scrHeight = size.y;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        appContext = viewGroup.getContext();
        checkScrSize();
        View v = LayoutInflater.from(viewGroup.getContext()).
                inflate(R.layout.cardview_recipe, viewGroup, false);
        PlaceViewHolder pvh = new PlaceViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceViewHolder placeViewHolder, int i) {
        placeViewHolder.title.setText(recipeTitles.get(i));
        placeViewHolder.itemView.setId(IDs.get(i).intValue());
        Picasso.with(appContext)
                .load(imageURLs.get(i))
                .resize(scrWidth/4,scrHeight/8)
                .placeholder(R.drawable.drawable_placeholder)
                .into(placeViewHolder.img);

        placeViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                final int anId = v.getId();
                TextView tv = v.findViewById(R.id.recipe_title);
                String title = recipeTitles.get(anId);

                final Dialog dialog = new Dialog(appContext);
                dialog.setTitle("New name of recipe");
                dialog.setContentView(R.layout.dialog_newtitle);
                final EditText et = dialog.findViewById(R.id.edittext_dialog);
                Button btnOK = dialog.findViewById(R.id.dialog_ok);
                et.setText(title);
                et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        Boolean isOk = false;
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            String str = et.getEditableText().toString();
                            if (!str.equals("")) {
                               recipeTitles.set(anId, str);
                               updTitle(str, (long) anId);
                               isOk = true;
                            } else {
                                isOk =false;
                            }
                        } else {
                            isOk = false;
                        }
                        return isOk;
                    }
                });

                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String str = et.getEditableText().toString();
                        if (str.equals("")) {
                            Toast.makeText(
                                    appContext,
                                    "Please insert new title of the recipe",
                                    Toast.LENGTH_SHORT)
                                    .show();
                        } else dialog.dismiss();
                    }
                });
                dialog.show();
                return true;
            }
        });

        placeViewHolder.itemView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                int id = IDs.indexOf((long)v.getId());
                String title = recipeTitles.get(id);
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
                intent.putExtra("NAME", title);
                AppCompatActivity activity = (AppCompatActivity) context;
                activity.startActivity(intent);
            }
        });
    }

    private void updTitle(String title, Long id) {
        AsyncTask task_newTitle = new Task_newTitle();
        Object[][] params = {{title, id, appContext}};
        task_newTitle.execute(params);
        Boolean isReady = null;
        try {
            isReady = ((Task_newTitle) task_newTitle).get();
        } catch (Exception e) {
            isReady = false;
            e.printStackTrace();
        }
        if (isReady){
            Toast.makeText(
                    appContext, "Title was changed", Toast.LENGTH_SHORT)
                    .show();
            this.notifyDataSetChanged();
        }
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
        EditText editText;

        PlaceViewHolder(View itemView){
            super(itemView);
            cv = itemView.findViewById(R.id.cardview_recipe);
            title = itemView.findViewById(R.id.recipe_title);
            img = itemView.findViewById(R.id.recipe_photo);
            editText = itemView.findViewById(R.id.recipe_changetext);
        }
    }
}
