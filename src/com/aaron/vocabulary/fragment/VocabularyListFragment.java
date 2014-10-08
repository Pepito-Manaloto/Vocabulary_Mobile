package com.aaron.vocabulary.fragment;

import java.util.ArrayList;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.activity.SettingsActivity;
import com.aaron.vocabulary.activity.VocabularyListSearchActivity;
import com.aaron.vocabulary.adapter.VocabularyAdapter;
import com.aaron.vocabulary.bean.Settings;
import com.aaron.vocabulary.bean.Vocabulary;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * Main view fragment containing the vocabulary list with main menu bar.
 */
public class VocabularyListFragment extends ListFragment
{
    private static final String DIALOG_UPDATE = "update";

    private static final int REQUEST_UPDATE = 0;
    private static final int REQUEST_SETTINGS = 1;

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
     * Updates the application. We do not update in onCreate() because it might not be called.
     */
    @Override
    public void onResume()
    {
        super.onResume();

        String language = getString(R.string.app_name, this.settings.getForeignLanguage().name());
        getActivity().setTitle(language);

        ((VocabularyAdapter) this.getListAdapter()).notifyDataSetChanged();
    }

    /**
     * Saves current state and settings in memory. This save is temporary.
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putSerializable(SettingsFragment.EXTRA_SETTINGS, this.settings);
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

        // Update action bar menu processing result
        if(requestCode == REQUEST_UPDATE)
        {
            @SuppressWarnings("unchecked")
            // But we are sure of its type
            ArrayList<Vocabulary> list = (ArrayList<Vocabulary>) data.getSerializableExtra(UpdateFragment.EXTRA_VOCABULARY_LIST);

            // TODO: update the list view
        }
        else if(requestCode == REQUEST_SETTINGS)
        {
            this.settings = (Settings) data.getSerializableExtra(SettingsFragment.EXTRA_SETTINGS);
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
    }

    /**
     * This method is called when a user selects an item in the menu bar. Opens
     * the fragment of selected item.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        FragmentManager fm = getActivity().getFragmentManager();

        switch(item.getItemId())
        {
            case R.id.menu_search:
            {
                Intent intent = new Intent(getActivity(), VocabularyListSearchActivity.class);
                startActivity(intent);

                return true;
            }
            case R.id.menu_update:
            {
                UpdateFragment updateDialog = UpdateFragment.newInstance();
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
}
