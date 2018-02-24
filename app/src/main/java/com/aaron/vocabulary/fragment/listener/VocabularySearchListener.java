package com.aaron.vocabulary.fragment.listener;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.aaron.vocabulary.adapter.VocabularyAdapter;
import com.aaron.vocabulary.bean.SearchType;
import com.aaron.vocabulary.model.LogsManager;

import static com.aaron.vocabulary.fragment.VocabularyListFragment.CLASS_NAME;

/**
 * Helper class for search listener.
 */
public class VocabularySearchListener implements TextWatcher
{
    private VocabularyAdapter adapter;
    private SearchType searchType;

    public VocabularySearchListener(VocabularyAdapter adapter, SearchType searchType)
    {
        this.adapter = adapter;
        this.searchType = searchType;
    }

    /**
     * Handles search on text update.
     */
    @Override
    public void afterTextChanged(Editable textField)
    {
        String searched = textField.toString();
        this.adapter.filter(searched, this.searchType);

        Log.d(LogsManager.TAG, CLASS_NAME + ": afterTextChanged. searched=" + searched);
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
    {
        // No action
    }

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
    {
        // No action
    }

    public void setSearchType(SearchType searchType)
    {
        this.searchType = searchType;
    }
}