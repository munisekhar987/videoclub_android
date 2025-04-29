package com.videoclub.utils;

import android.view.KeyEvent;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TVNavigationHelper {

    /**
     * Sets up key navigation between recycler views for TV remote control
     *
     * @param sourceView The source RecyclerView
     * @param targetView The target View for a specific direction
     * @param direction The direction key to handle (e.g., KeyEvent.KEYCODE_DPAD_RIGHT)
     */
    public static void setupRecyclerViewNavigation(RecyclerView sourceView, View targetView, int direction) {
        sourceView.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == direction) {
                    // If we're at the edge of the RecyclerView and want to navigate out
                    RecyclerView.LayoutManager layoutManager = sourceView.getLayoutManager();
                    if (layoutManager != null) {
                        View focusedChild = sourceView.getFocusedChild();
                        if (focusedChild != null) {
                            int position = sourceView.getChildAdapterPosition(focusedChild);
                            int itemCount = sourceView.getAdapter().getItemCount();

                            // Check if we're at the edge based on direction
                            boolean isAtEdge = false;

                            if (direction == KeyEvent.KEYCODE_DPAD_RIGHT) {
                                if (layoutManager instanceof GridLayoutManager) {
                                    GridLayoutManager gridManager = (GridLayoutManager) layoutManager;
                                    int spanCount = gridManager.getSpanCount();

                                    // Check if we're at the right edge of the grid
                                    isAtEdge = (position % spanCount == spanCount - 1) || (position == itemCount - 1);
                                } else if (layoutManager instanceof LinearLayoutManager) {
                                    LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                                    if (linearManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                                        // For horizontal list, check if we're at the rightmost item
                                        isAtEdge = position == itemCount - 1;
                                    }
                                }
                            } else if (direction == KeyEvent.KEYCODE_DPAD_LEFT) {
                                if (layoutManager instanceof GridLayoutManager) {
                                    GridLayoutManager gridManager = (GridLayoutManager) layoutManager;
                                    int spanCount = gridManager.getSpanCount();

                                    // Check if we're at the left edge of the grid
                                    isAtEdge = position % spanCount == 0;
                                } else if (layoutManager instanceof LinearLayoutManager) {
                                    LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                                    if (linearManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                                        // For horizontal list, check if we're at the leftmost item
                                        isAtEdge = position == 0;
                                    }
                                }
                            } else if (direction == KeyEvent.KEYCODE_DPAD_DOWN) {
                                if (layoutManager instanceof GridLayoutManager) {
                                    GridLayoutManager gridManager = (GridLayoutManager) layoutManager;
                                    int spanCount = gridManager.getSpanCount();
                                    int rowCount = (int) Math.ceil((double) itemCount / spanCount);

                                    // Check if we're at the bottom row
                                    int row = position / spanCount;
                                    isAtEdge = row == rowCount - 1;
                                } else if (layoutManager instanceof LinearLayoutManager) {
                                    LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                                    if (linearManager.getOrientation() == LinearLayoutManager.VERTICAL) {
                                        // For vertical list, check if we're at the bottom item
                                        isAtEdge = position == itemCount - 1;
                                    }
                                }
                            } else if (direction == KeyEvent.KEYCODE_DPAD_UP) {
                                if (layoutManager instanceof GridLayoutManager) {
                                    GridLayoutManager gridManager = (GridLayoutManager) layoutManager;
                                    int spanCount = gridManager.getSpanCount();

                                    // Check if we're at the top row
                                    int row = position / spanCount;
                                    isAtEdge = row == 0;
                                } else if (layoutManager instanceof LinearLayoutManager) {
                                    LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                                    if (linearManager.getOrientation() == LinearLayoutManager.VERTICAL) {
                                        // For vertical list, check if we're at the top item
                                        isAtEdge = position == 0;
                                    }
                                }
                            }

                            if (isAtEdge) {
                                targetView.requestFocus();
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        });
    }
}