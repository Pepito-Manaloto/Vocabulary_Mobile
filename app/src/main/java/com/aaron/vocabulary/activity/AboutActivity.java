package com.aaron.vocabulary.activity;

import com.aaron.vocabulary.fragment.AboutFragment;

import android.app.Fragment;

/**
 * About activity.
 */
public class AboutActivity extends SingleFragmentActivity
{
    private AboutFragment fragment;

    /**
     * Returns a about fragment.
     * 
     * @return a fragment to be added.
     */
    @Override
    protected Fragment createFragment()
    {
        if(this.fragment == null)
        {
            this.fragment = new AboutFragment();
        }

        return this.fragment;
    }

}
