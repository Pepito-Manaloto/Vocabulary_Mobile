package com.aaron.vocabulary.activity;

import android.app.Fragment;

import com.aaron.vocabulary.fragment.VocabularyListFragment;
import com.aaron.vocabulary.fragment.VocabularyListSearchFragment;

/**
 * VocabularyListSearchActivity activity.
 */
public class VocabularyListSearchActivity extends SingleFragmentActivity
{
    /**
     * Returns a vocabulary list search fragment.
     * @return a fragment to be added.
     */
    @Override
    protected Fragment createFragment()
    {
        return new VocabularyListSearchFragment();
    }
}
