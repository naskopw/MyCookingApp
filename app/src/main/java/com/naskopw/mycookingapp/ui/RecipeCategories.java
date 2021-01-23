package com.naskopw.mycookingapp.ui;

public enum RecipeCategories {
    Bulgarian(0),
    American(1),
    Asian(2),
    Vegan(3);

    private final int value;
    RecipeCategories(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}