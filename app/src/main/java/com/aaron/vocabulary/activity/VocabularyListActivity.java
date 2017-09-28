package com.aaron.vocabulary.activity;

import com.aaron.vocabulary.fragment.VocabularyListFragment;

import android.app.Fragment;

/**
 * VocabularyListFragment activity.
 */
public class VocabularyListActivity extends SingleFragmentActivity
{
    private VocabularyListFragment fragment;

    /**
     * Returns a vocabulary list fragment.
     * 
     * @return a fragment to be added.
     */
    @Override
    protected Fragment createFragment()
    {
        if(this.fragment == null)
        {
            this.fragment = new VocabularyListFragment();
        }

        return this.fragment;
    }
}
