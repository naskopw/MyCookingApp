package com.naskopw.mycookingapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.naskopw.mycookingapp.R;
import com.naskopw.mycookingapp.adapters.RecipeOverviewAdapter;
import com.naskopw.mycookingapp.dao.DatabaseHelper;
import com.naskopw.mycookingapp.dao.HttpHandler;
import com.naskopw.mycookingapp.models.Recipe;
import com.naskopw.mycookingapp.settings.GlobalSettings;
import com.naskopw.mycookingapp.util.RecipeCategoryMapping;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();
    private int selectedTab;
    ArrayList<Recipe> recipes;

    private void switchTabImage() {
        TabLayout mainTabLayout = findViewById(R.id.mainTabLayout);
        mainTabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        mainTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        ImageView mainImage = findViewById(R.id.mainImageView);
        mainTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
                if (selectedTab == RecipeCategories.Bulgarian.getValue())
                    mainImage.setImageResource(R.drawable.bulgarian);
                else if (selectedTab == RecipeCategories.American.getValue())
                    mainImage.setImageResource(R.drawable.american);
                else if (selectedTab == RecipeCategories.Asian.getValue())
                    mainImage.setImageResource(R.drawable.asian);
                if (selectedTab == RecipeCategories.Vegan.getValue())
                    mainImage.setImageResource(R.drawable.vegan);
                new FetchRecipes().execute();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void createFavoritesDB() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recipes = new ArrayList<>();
        setContentView(R.layout.activity_main);
        createFavoritesDB();

        final ListView recipeList = findViewById(R.id.recipeList);
        recipeList.setOnItemClickListener((parent, view, position, id) -> {
            if (recipes.size() > 0) {
                Intent intent = new Intent(MainActivity.this, RecipeDetailsActivity.class);
                Recipe clickedRecipe = recipes.get(position);
                intent.putExtra("category_name", clickedRecipe.getCategory());
                intent.putExtra("id", clickedRecipe.getId());
                startActivity(intent);
            }

        });
        new FetchRecipes().execute();
        switchTabImage();
    }


    private class FetchRecipes extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            recipes.clear();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            String jsonStr = sh.makeServiceCall(GlobalSettings.API_URL);
            if (jsonStr != null) {
                try {
                    JSONArray recipesJsonObj = new JSONArray(jsonStr);
                    for (int i = 0; i < recipesJsonObj.length(); i++) {
                        Recipe r = new Recipe();

                        JSONObject c = recipesJsonObj.getJSONObject(i);
                        r.setRecipeName(c.getString("recipe_name"));
                        r.setCategory(c.getString("category"));
                        r.setCookingTime(c.getInt("cooking_time"));
                        r.setId(i);
                        r.setImage(c.getString("image"));
                        if (r.getCategory().equals(RecipeCategoryMapping.getName(selectedTab))) {
                            recipes.add(r);
                        }

                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            String[] titles = new String[recipes.size()];
            String[] thumbnails = new String[recipes.size()];
            int i = 0;
            for (Recipe r : recipes) {
                titles[i] = r.getRecipeName();
                thumbnails[i] = r.getImage();
                i++;
            }
            ListView recipeList = findViewById(R.id.recipeList);
            if (recipes.size() == 0) {
                titles = new String[]{getResources().getString(R.string.empty_recipe_tab_text)};
                thumbnails = new String[]{getResources().getString(R.string.empty_recipe_tab_image)};
                TextView recipeTime = findViewById(R.id.recipeTime);
                recipeTime.setVisibility(View.INVISIBLE);
            }
            RecipeOverviewAdapter itemsAdapter = new RecipeOverviewAdapter(MainActivity.this, titles, thumbnails, recipes);
            recipeList.setAdapter(itemsAdapter);

        }
    }
}

