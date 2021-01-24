package com.naskopw.mycookingapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class RecipeDetailsActivity extends AppCompatActivity {
    private Recipe recipe;
    private String recipeCategory;
    private Integer recipeId;
    private int selectedTab;
    boolean isSideMenuVisible = false;
    private FloatingActionButton[] sideMenuItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);


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
        if (recipeCategory.isEmpty() || recipeCategory == null) {
            Toast.makeText(this, getResources().getString(R.string.errorOccurred), Toast.LENGTH_LONG).show();
            Log.e(RecipeDetailsActivity.class.getSimpleName(), String.format("Invalid recipe passed to details activity, category: %s, id: %d", recipeCategory, recipeId));
        }
        TabLayout detailsTabLayout = findViewById(R.id.detailsTabLayout);
        detailsTabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        detailsTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        new FetchRecipes().execute();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite:
                Intent intent = new Intent(getApplicationContext(), FavoritesActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
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

    public void onShareClick(View view) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "App link");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.shareRecipeContent));
        startActivity(Intent.createChooser(sharingIntent, "Share via"));
    }

    public void onFavoriteClick(View view) {
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_NAME;
        Cursor cursor = database.rawQuery(query, null);

        if (cursor.moveToNext()) {
            String deleteQuery = String.format("DELETE FROM %s WHERE %s='%s' AND %s=%d", DatabaseHelper.TABLE_NAME, DatabaseHelper.RECIPE_CATEGORY, recipeCategory, DatabaseHelper.RECIPE_ID, recipeId);
            database.execSQL(deleteQuery);
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.removedFromFavorites), Toast.LENGTH_LONG).show();
        } else {
            values.put(DatabaseHelper.RECIPE_CATEGORY, recipeCategory);
            values.put(DatabaseHelper.RECIPE_ID, recipeId);
            database.insert(DatabaseHelper.TABLE_NAME, null, values);
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.addedToFavorites), Toast.LENGTH_LONG).show();
        }
        database.close();
        dbHelper.close();
    }

    public void onTimerClick(View view) {

        DialogFragment newFragment = new TimePickerFragment(recipe.getRecipeName(), recipe.getCookingTime());
        newFragment.show(getSupportFragmentManager(), "timePicker");
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
                        JSONArray ingredients = c.getJSONArray("ingredients");
                        r.setIngredients(new String[ingredients.length()]);
                        for (int j = 0; j < ingredients.length(); j++) {
                            r.getIngredients()[j] = ingredients.get(j).toString();
                        }
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
            TextView titleTextView = findViewById(R.id.titleTextView);
            TextView timerTextView = findViewById(R.id.sideTimerTextView);
            timerTextView.setText(recipe.getCookingTime().toString());
            titleTextView.setText(recipe.getRecipeName());
            ImageView detailsImage = findViewById(R.id.detailsImageView);
            StringBuilder ingredients = new StringBuilder();
            for (String ingredient : recipe.getIngredients()) {
                ingredients.append("\u2022");
                ingredients.append(ingredient + "\n");
            }
            Glide.with(getApplicationContext()).load(recipe.getImage()).into(detailsImage);

            if (selectedTab == 0) {
                content.setText(ingredients.toString());
            }
            if (selectedTab == 1) {
                content.setText(recipe.getSteps());
            }
        }
    }
}