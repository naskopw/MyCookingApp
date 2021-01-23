package com.naskopw.mycookingapp.util;

import com.naskopw.mycookingapp.ui.RecipeCategories;

public class RecipeCategoryMapping {
    public static String getName(int selectedTab) {
        if (selectedTab == RecipeCategories.Bulgarian.getValue())
            return RecipeCategories.Bulgarian.name();
        else if (selectedTab == RecipeCategories.American.getValue())
            return RecipeCategories.American.name();
        else if (selectedTab == RecipeCategories.Asian.getValue())
            return RecipeCategories.Asian.name();
        if (selectedTab == RecipeCategories.Vegan.getValue())
            return RecipeCategories.Vegan.name();
        throw new RuntimeException("Unknown recipe category");
    }
}
