package com.aaron.vocabulary.fragment;

import java.util.ArrayList;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.activity.AboutActivity;
import com.aaron.vocabulary.activity.SettingsActivity;
import com.aaron.vocabulary.adapter.VocabularyAdapter;
import com.aaron.vocabulary.bean.Settings;
import com.aaron.vocabulary.bean.Vocabulary;
import com.aaron.vocabulary.model.LogManager;
import com.aaron.vocabulary.model.VocabularyManager;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

/**
 * Main view fragment containing the vocabulary list with main menu bar.
 */
public class VocabularyListFragment extends ListFragment
{
    private static final String DIALOG_UPDATE = "update";

    private static final int REQUEST_UPDATE = 0;
    private static final int REQUEST_SETTINGS = 1;

    public static final String EXTRA_LIST = "com.aaron.vocabulary.fragment.list";

    private ArrayList<Vocabulary> list;
    private Settings settings;
    private VocabularyManager vocabularyManager;

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

            // But we are sure of its type
            @SuppressWarnings("unchecked")
            ArrayList<Vocabulary> listTemp = (ArrayList<Vocabulary>) savedInstanceState.getSerializable(EXTRA_LIST);
            this.list = listTemp;
        }

        if(this.settings == null)
        {
            this.settings = new Settings();
        }

        this.vocabularyManager = new VocabularyManager(getActivity(), this.settings);

        if(this.list == null)
        {
            this.list = this.vocabularyManager.getVocabulariesFromDisk();
        }

        this.updateListOnUiThread(this.list);

        setHasOptionsMenu(true);

        Log.d(LogManager.TAG, "VocabularyListFragment: onCreate");
    }

    /**
     * Initializes the fragment's user interface.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_vocabulary_list, parent, false);
        Log.d(LogManager.TAG, "VocabularyListFragment: onCreateView");

        return view;
    }

    /**
     * Changed the title of the application.
     */
    @Override
    public void onResume()
    {
        super.onResume();

        String language = getString(R.string.app_name, this.settings.getForeignLanguage().name());
        getActivity().setTitle(language);

        ((VocabularyAdapter) this.getListAdapter()).notifyDataSetChanged();

        Log.d(LogManager.TAG, "VocabularyListFragment: onResume");
    }

    /**
     * Saves current state and settings in memory. For screen rotation.
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putSerializable(SettingsFragment.EXTRA_SETTINGS, this.settings);
        outState.putSerializable(EXTRA_LIST, this.list);

        Log.d(LogManager.TAG, "VocabularyListFragment: onSaveInstanceState");
    }

    /**
     * Receives the result data from the previous fragment. Updates the
     * application's state depending on the data received.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode != Activity.RESULT_OK)
        {
            return;
        }

        Log.d(LogManager.TAG, "VocabularyListFragment: onActivityResult. requestCode=" + requestCode + " resultCode=" + resultCode);

        // Update action bar menu processing result
        if(requestCode == REQUEST_UPDATE && data.hasExtra(UpdateFragment.EXTRA_VOCABULARY_LIST))
        {
            // But we are sure of its type
            @SuppressWarnings("unchecked")
            ArrayList<Vocabulary> list = (ArrayList<Vocabulary>) data.getSerializableExtra(UpdateFragment.EXTRA_VOCABULARY_LIST);

            this.list = list;
            this.updateListOnUiThread(this.list);
        }
        else if(requestCode == REQUEST_SETTINGS && data.hasExtra(SettingsFragment.EXTRA_SETTINGS))
        {
            this.settings = (Settings) data.getSerializableExtra(SettingsFragment.EXTRA_SETTINGS);
            this.vocabularyManager.setLanguageSelected(this.settings);

            this.list = this.vocabularyManager.getVocabulariesFromDisk();
            this.updateListOnUiThread(this.list);
        }
    }

    /**
     * Inflates the menu items in the action bar.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.vocabulary, menu);

        /** Get the action view of the menu item whose id is edittext_search_field */
        View view = (View) menu.findItem(R.id.menu_search).getActionView();
        
        /** Get the edit text from the action view */
        final EditText searchTextfield = (EditText) view.findViewById(R.id.edittext_search_field);

        searchTextfield.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void afterTextChanged(Editable arg0)
                {
                    String searched = searchTextfield.getText().toString();
                    VocabularyAdapter vocabularyAdapter = (VocabularyAdapter) getListAdapter();
                    vocabularyAdapter.filter(searched);
                    vocabularyAdapter.notifyDataSetChanged();
                }
    
                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
                {
                }
    
                @Override
                public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
                {
                }
            });
    }

    /**
     * This method is called when a user selects an item in the menu bar. Opens the fragment of selected item.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        FragmentManager fm = getActivity().getFragmentManager();

        switch(item.getItemId())
        {
            case R.id.menu_search:
            {
                return true;
            }
            case R.id.menu_update:
            {
                UpdateFragment updateDialog = UpdateFragment.newInstance(this.settings);
                updateDialog.setTargetFragment(this, REQUEST_UPDATE);
                updateDialog.show(fm, DIALOG_UPDATE);

                return true;
            }
            case R.id.menu_settings:
            {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                intent.putExtra(SettingsFragment.EXTRA_SETTINGS, this.settings);
                startActivityForResult(intent, REQUEST_SETTINGS);

                return true;
            }
            case R.id.menu_about:
            {
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                startActivity(intent);
                return true;
            }
            case R.id.menu_logs:
            {

                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    /**
     * Updates the list view on UI thread.
     * @param list the new list
     */
    private void updateListOnUiThread(final ArrayList<Vocabulary> list)
    {
        this.getActivity().runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                //((VocabularyAdapter) getListAdapter()).notifyDataSetChanged();
                VocabularyAdapter vocabularyAdapter = new VocabularyAdapter(getActivity(), list, settings);
                setListAdapter(vocabularyAdapter);
            }
        });
    }
}
