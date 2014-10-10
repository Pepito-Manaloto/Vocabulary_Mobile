package com.aaron.vocabulary.activity;

import com.aaron.vocabulary.fragment.VocabularyListFragment;

import android.app.Fragment;
import android.content.Intent;

/**
 * VocabularyListFragment activity.
 */
public class VocabularyListActivity extends SingleFragmentActivity
{
    /**
     * Returns a vocabulary list fragment.
     * @return a fragment to be added.
     */
    @Override
    protected Fragment createFragment()
    {
        
        return new VocabularyListFragment();
    }
}
