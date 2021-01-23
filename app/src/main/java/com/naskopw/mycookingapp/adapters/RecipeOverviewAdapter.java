package com.naskopw.mycookingapp.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.naskopw.mycookingapp.ui.MainActivity;
import com.naskopw.mycookingapp.R;
import com.naskopw.mycookingapp.models.Recipe;

import java.util.ArrayList;

public class RecipeOverviewAdapter extends ArrayAdapter {
    private final String[] titles;
    private final String[] imagesIds;
    private final Activity context;
    private final ArrayList<Recipe> recipes;
    private String TAG = MainActivity.class.getSimpleName();

    public RecipeOverviewAdapter(Activity context, String[] titles, String[] thumbnails, ArrayList<Recipe> recipes) {
        super(context, R.layout.recipe_item_row, titles);
        this.context = context;
        this.titles = titles;
        this.recipes = recipes;
        this.imagesIds = thumbnails;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        LayoutInflater inflater = context.getLayoutInflater();
        if (convertView == null)
            row = inflater.inflate(R.layout.recipe_item_row, null, true);

        ImageView recipeThumbnail = row.findViewById(R.id.recipeThumbnail);
        TextView recipeTitle = row.findViewById(R.id.recipeTitle);
        TextView recipeTime = row.findViewById(R.id.recipeTime);
        recipeTitle.setText(titles[position]);
        try {
            recipeTime.setText(recipes.get(position).getCookingTime().toString());
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            recipeTime.setVisibility(View.INVISIBLE);
        }
        Glide.with(context).load(imagesIds[position]).into(recipeThumbnail);
        return row;
    }
}