package com.aaron.vocabulary.fragment.listener;

import android.os.Handler;
import android.widget.AbsListView;

/**
 * Helper class for ListView's scroll listener.
 */
public class ShowHideFastScrollListener implements AbsListView.OnScrollListener, Runnable
{
    private static final int DELAY = 1000;
    private AbsListView view;
    private final Handler handler = new Handler();

    /**
     * Runnable for handler object.
     */
    @Override
    public void run()
    {
        this.view.setFastScrollAlwaysVisible(false);
        this.view = null;
    }

    /**
     * Show fast-scroll thumb if scrolling, and hides fast-scroll thumb if not scrolling.
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        if(scrollState != SCROLL_STATE_IDLE)
        {
            view.setFastScrollAlwaysVisible(true);

            // Removes the runnable from the message queue.
            // Stops the handler from hiding the fast-scroll.
            this.handler.removeCallbacks(this);
        }
        else
        {
            this.view = view;

            // Adds the runnable to the message queue, will run after the DELAY.
            // Hides the fast-scroll after two seconds of no scrolling.
            this.handler.postDelayed(this, DELAY);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        // No action
    }
}