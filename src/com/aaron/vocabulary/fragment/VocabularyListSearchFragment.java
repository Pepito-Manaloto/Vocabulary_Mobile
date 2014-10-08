package com.aaron.vocabulary.fragment;

import java.util.ArrayList;

import android.app.ListFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.adapter.VocabularyAdapter;
import com.aaron.vocabulary.bean.Settings;
import com.aaron.vocabulary.bean.Vocabulary;

/**
 * Main view fragment containing the vocabulary list with search bar.
 */
public class VocabularyListSearchFragment extends ListFragment
{
    private ArrayList<Vocabulary> list;
    private Settings settings;

    /**
     * Initializes non-fragment user interface.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        if(savedInstanceState != null)
        {
            this.settings = (Settings) savedInstanceState.getSerializable(SettingsFragment.EXTRA_SETTINGS);
        }
        else
        {
            this.settings = new Settings();
        }

        this.list = null; // retrieve from db
        VocabularyAdapter vocabularyAdapter = new VocabularyAdapter(getActivity(), this.list);
        setListAdapter(vocabularyAdapter);

        setHasOptionsMenu(true);
    }

    /**
     * Initializes the fragment's user interface.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_vocabulary_list, parent, false);
        
        return view;
    }

    /**
     * Inflates the menu items in the action bar.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.vocabulary_search, menu);
        
        /** Get the action view of the menu item whose id is edittext_search_field */
        View view = (View) menu.findItem(R.id.search_actionbar).getActionView();
        
        /** Get the edit text from the action view */
        EditText searchTextfield = (EditText) view.findViewById(R.id.edittext_search_field);
        
        searchTextfield.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void afterTextChanged(Editable arg0)
                {
                    // TODO Auto-generated method stub
                    
                }
    
                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
                {
                    // TODO Auto-generated method stub
                    
                }
    
                @Override
                public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
                {
                    // TODO Auto-generated method stub
                    
                }
            });
    }
}
