package com.naskopw.mycookingapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

public class FavoritesActivity extends AppCompatActivity {
    private String TAG = MainActivity.class.getSimpleName();
    private ArrayList<Recipe> favorites = new ArrayList<>();
    private ArrayList<Recipe> recipes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        DatabaseHelper dbDatabaseHelper = new DatabaseHelper(getApplicationContext());
        SQLiteDatabase db = dbDatabaseHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            Recipe currentRecipe = new Recipe();
            currentRecipe.setId(cursor.getInt(cursor.getColumnIndex(DatabaseHelper.RECIPE_ID)));
            currentRecipe.setCategory(cursor.getString(cursor.getColumnIndex(DatabaseHelper.RECIPE_CATEGORY)));
            favorites.add(currentRecipe);
        }
        cursor.close();
        new FetchRecipes().execute();
        final ListView recipeList = findViewById(R.id.favoritesList);
        recipeList.setOnItemClickListener((parent, view, position, id) -> {
            if (recipes.size() > 0) {
                Intent intent = new Intent(FavoritesActivity.this, RecipeDetailsActivity.class);
                Recipe clickedRecipe = recipes.get(position);
                intent.putExtra("category_name", clickedRecipe.getCategory());
                intent.putExtra("id", clickedRecipe.getId());
                startActivity(intent);
            }

        });
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
                        if (favorites.contains(r)) {
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
            if (recipes.size() > 0) {
                ListView recipeList = findViewById(R.id.favoritesList);
                RecipeOverviewAdapter itemsAdapter = new RecipeOverviewAdapter(FavoritesActivity.this, titles, thumbnails, recipes);
                recipeList.setAdapter(itemsAdapter);
            }
        }
    }
}
