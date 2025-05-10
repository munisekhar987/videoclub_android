package com.videoclub.utils;

import androidx.leanback.widget.VerticalGridView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Utility class to extend functionality of GridView components
 */
public class GridViewExtensions {

    /**
     * Get the position of the first visible item in a VerticalGridView
     *
     * @param gridView The VerticalGridView to check
     * @return The position of the first visible item, or 0 if not available
     */
    public static int getFirstVisiblePosition(VerticalGridView gridView) {
        if (gridView == null) {
            return 0;
        }

        RecyclerView.LayoutManager layoutManager = gridView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        }

        return 0;
    }

    /**
     * Get the position of the last visible item in a VerticalGridView
     *
     * @param gridView The VerticalGridView to check
     * @return The position of the last visible item, or 0 if not available
     */
    public static int getLastVisiblePosition(VerticalGridView gridView) {
        if (gridView == null) {
            return 0;
        }

        RecyclerView.LayoutManager layoutManager = gridView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
        }

        return 0;
    }
}