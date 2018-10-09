package com.papashkin.myrecipes;

public interface RecipeTouchAdapter {
    void onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);
}
