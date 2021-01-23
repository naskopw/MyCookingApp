package com.naskopw.mycookingapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.naskopw.mycookingapp.R;
import com.naskopw.mycookingapp.adapters.RecipeOverviewAdapter;
import com.naskopw.mycookingapp.dao.HttpHandler;
import com.naskopw.mycookingapp.models.Recipe;
import com.naskopw.mycookingapp.settings.GlobalSettings;
import com.naskopw.mycookingapp.util.RecipeCategoryMapping;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RecipeDetails extends AppCompatActivity {
    private Recipe recipe;
    private String recipeCategory;
    private Integer recipeId;
    private int selectedTab;
    boolean isSideMenuVisible = true;
    private FloatingActionButton[] sideMenuItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        sideMenuItems = new FloatingActionButton[]{
                findViewById(R.id.shareButton),
                findViewById(R.id.timerButton),
                findViewById(R.id.favoriteButton)
        };
        recipeId = null;
        recipeCategory = null;
        Bundle extras = getIntent().getExtras();
        recipeCategory = extras.getString("category_name");
        recipeId = extras.getInt("id");
        TabLayout detailsTabLayout = findViewById(R.id.detailsTabLayout);
        detailsTabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        detailsTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        detailsTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                selectedTab = tab.getPosition();
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

    public void showOrHideSideMenu(View view) {
        if (isSideMenuVisible)
            for (FloatingActionButton b : sideMenuItems)
                b.setVisibility(View.INVISIBLE);
        else
            for (FloatingActionButton b : sideMenuItems)
                b.setVisibility(View.VISIBLE);
        isSideMenuVisible = !isSideMenuVisible;
    }

    private class FetchRecipes extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
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
                        r.setSteps(c.getString("steps"));
                        r.setId(i);
                        r.setImage(c.getString("image"));
                        if (r.getCategory().equals(recipeCategory) && r.getId() == recipeId) {
                            recipe = r;
                        }

                    }
                } catch (final JSONException e) {
                    Log.e(MainActivity.class.getSimpleName(), "Json parsing error: " + e.getMessage());
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
                Log.e(MainActivity.class.getSimpleName(), "Couldn't get json from server.");
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
            TextView content = findViewById(R.id.textView);
            if (selectedTab == 0)
            {
                content.setText(recipe.getRecipeName());
            }
            if (selectedTab == 1)
            {
                content.setText(recipe.getSteps());
            }
        }
    }
}