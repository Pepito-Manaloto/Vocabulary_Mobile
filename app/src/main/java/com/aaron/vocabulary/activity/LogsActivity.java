package com.aaron.vocabulary.activity;

import com.aaron.vocabulary.fragment.LogsFragment;

import android.app.Fragment;

/**
 * Logs activity.
 */
public class LogsActivity extends SingleFragmentActivity
{
    private LogsFragment fragment;

    /**
     * Returns a logs fragment.
     * 
     * @return a fragment to be added.
     */
    @Override
    protected Fragment createFragment()
    {
        if(this.fragment == null)
        {
            this.fragment = new LogsFragment();
        }

        return this.fragment;
    }

}
