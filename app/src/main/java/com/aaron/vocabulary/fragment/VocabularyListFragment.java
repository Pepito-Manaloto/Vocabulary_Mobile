package com.aaron.vocabulary.fragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.activity.AboutActivity;
import com.aaron.vocabulary.activity.LogsActivity;
import com.aaron.vocabulary.activity.SettingsActivity;
import com.aaron.vocabulary.adapter.VocabularyAdapter;
import com.aaron.vocabulary.bean.SearchType;
import com.aaron.vocabulary.bean.Settings;
import com.aaron.vocabulary.bean.Vocabulary;
import com.aaron.vocabulary.bean.Vocabulary.ForeignLanguage;
import com.aaron.vocabulary.fragment.listener.ShowHideFastScrollListener;
import com.aaron.vocabulary.fragment.listener.VocabularySearchListener;
import com.aaron.vocabulary.model.LogsManager;
import com.aaron.vocabulary.model.VocabularyManager;

import java.util.ArrayList;

import static com.aaron.vocabulary.fragment.SettingsFragment.EXTRA_SETTINGS;

/**
 * Main view fragment containing the vocabulary list with main menu bar.
 */
public class VocabularyListFragment extends ListFragment
{
    private enum MenuRequest
    {
        UPDATE(0), SETTINGS(1), ABOUT(2), LOGS(3);

        private int code;

        MenuRequest(int code)
        {
            this.code = code;
        }

        int getCode()
        {
            return code;
        }
    }

    public static final String CLASS_NAME = VocabularyListFragment.class.getSimpleName();
    public static final String EXTRA_VOCABULARY_LIST = "com.aaron.vocabulary.fragment.vocabulary_list.list";
    private static final String DIALOG_UPDATE = "update";

    private ArrayList<Vocabulary> list;
    private VocabularyAdapter vocabularyAdapter;

    private Settings settings;
    private VocabularyManager vocabularyManager;
    private EditText searchEditText;
    private VocabularySearchListener searchListener;
    private SearchType selectedSearchType;

    /**
     * Initializes non-fragment user interface.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        vocabularyManager = new VocabularyManager(getActivity().getApplicationContext());

        initializeSettings(savedInstanceState);
        initializeVocabularyList(savedInstanceState);

        vocabularyAdapter = new VocabularyAdapter(getActivity(), list, settings);
        setListAdapter(vocabularyAdapter);

        setHasOptionsMenu(true);
        selectedSearchType = SearchType.ENGLISH;
        searchListener = new VocabularySearchListener(vocabularyAdapter, selectedSearchType);

        Log.d(LogsManager.TAG, CLASS_NAME + ": onCreate. settings=" + settings + " list=" + list);
        LogsManager.addToLogs(CLASS_NAME + ": onCreate. settings=" + settings + " list_size=" + list.size());
    }

    private void initializeSettings(Bundle savedInstanceState)
    {
        if(savedInstanceState != null)
        {
            settings = savedInstanceState.getParcelable(EXTRA_SETTINGS);
        }

        if(settings == null)
        {
            settings = new Settings();
        }
    }

    private void initializeVocabularyList(Bundle savedInstanceState)
    {
        if(savedInstanceState != null)
        {
            list = savedInstanceState.getParcelableArrayList(EXTRA_VOCABULARY_LIST);
        }

        if(list == null)
        {
            list = vocabularyManager.getVocabulariesFromDisk(settings.getForeignLanguage());
        }
    }

    /**
     * Initializes the fragment's user interface.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_vocabulary_list, parent, false);
        vocabularyAdapter.update(list);
        Log.d(LogsManager.TAG, CLASS_NAME + ": onCreateView.");

        return view;
    }

    /**
     * Called after onCreateView(), sets the action listeners of the UI.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Log.d(LogsManager.TAG, CLASS_NAME + ": onActivityCreated.");

        getListView().setOnScrollListener(new ShowHideFastScrollListener());
    }

    /**
     * Changed the title of the application.
     */
    @Override
    public void onResume()
    {
        super.onResume();

        String language = getString(R.string.app_name, settings.getForeignLanguage().name());
        getActivity().setTitle(language);

        Log.d(LogsManager.TAG, CLASS_NAME + ": onResume");
    }

    /**
     * Saves current state and settings in memory. For screen rotation.
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putParcelable(EXTRA_SETTINGS, settings);
        outState.putParcelableArrayList(EXTRA_VOCABULARY_LIST, list);

        Log.d(LogsManager.TAG, CLASS_NAME + ": onSaveInstanceState");
    }

    /**
     * Receives the result data from the previous fragment. Updates the application's state depending on the data received.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode != Activity.RESULT_OK)
        {
            return;
        }

        Log.d(LogsManager.TAG, CLASS_NAME + ": onActivityResult. requestCode=" + requestCode + " resultCode=" + resultCode);
        LogsManager.addToLogs(CLASS_NAME + ": onActivityResult. requestCode=" + requestCode + " resultCode=" + resultCode);

        boolean requestResultFromUpdate = requestCode == MenuRequest.UPDATE.getCode();
        boolean hasExtraVocabularyListData = data != null && data.hasExtra(UpdateFragment.EXTRA_VOCABULARY_LIST);
        if(requestResultFromUpdate && hasExtraVocabularyListData)
        {
            ArrayList<Vocabulary> list = data.getParcelableArrayListExtra(UpdateFragment.EXTRA_VOCABULARY_LIST);

            // Handles occasional NullPointerException.
            if(list != null && !list.isEmpty())
            {
                this.list = list;
            }
            else
            {
                list = vocabularyManager.getVocabulariesFromDisk(settings.getForeignLanguage());
            }

            vocabularyAdapter.update(list);

            return;
        }

        boolean requestResultFromSettingsOrAboutOrLogs = requestCode == MenuRequest.SETTINGS.getCode() || requestCode == MenuRequest.ABOUT.getCode()
                || requestCode == MenuRequest.LOGS.getCode();
        boolean hasExtraSettingsData = data != null && data.hasExtra(EXTRA_SETTINGS);
        if(requestResultFromSettingsOrAboutOrLogs && hasExtraSettingsData)
        {
            settings = data.getParcelableExtra(EXTRA_SETTINGS);

            ForeignLanguage language = ForeignLanguage.Hokkien;
            if(settings != null)
            {
                language = settings.getForeignLanguage();
            }

            list = vocabularyManager.getVocabulariesFromDisk(language);
            vocabularyAdapter.update(list);
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

        // Get the action view of the menu item whose id is edittext_search_field
        View view = menu.findItem(R.id.menu_search).getActionView();
        initializeSearchEditText(view);
    }

    private void initializeSearchEditText(View view)
    {
        searchEditText = view.findViewById(R.id.edittext_search_field);
        searchEditText.setHint(R.string.hint_vocabulary);
        searchEditText.addTextChangedListener(searchListener);
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
                UpdateFragment updateDialog = UpdateFragment.newInstance(settings);

                if(updateDialog.isUpdating())
                {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.dialog_already_updating_message), Toast.LENGTH_LONG).show();
                }
                else
                {
                    updateDialog.setTargetFragment(this, MenuRequest.UPDATE.getCode());
                    updateDialog.show(fm, DIALOG_UPDATE);
                }

                return true;
            }
            case R.id.menu_settings:
            {
                startActivityWithExtraSettings(SettingsActivity.class, MenuRequest.SETTINGS);
                return true;
            }
            case R.id.menu_search_type:
            {
                toggleSearchTypeMenuItem(item);

                return true;
            }
            case R.id.menu_about:
            {
                startActivityWithExtraSettings(AboutActivity.class, MenuRequest.ABOUT);
                return true;
            }
            case R.id.menu_logs:
            {
                startActivityWithExtraSettings(LogsActivity.class, MenuRequest.LOGS);
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void startActivityWithExtraSettings(Class<? extends Activity> activityToStart, MenuRequest menuRequestOrigin)
    {
        Intent intent = new Intent(getActivity(), activityToStart);
        intent.putExtra(EXTRA_SETTINGS, settings);
        startActivityForResult(intent, menuRequestOrigin.getCode());
    }

    /**
     * Toggles the search type menu item between 'english' and 'foreign'.
     *
     * @param item
     *            the search type menu item
     */
    private void toggleSearchTypeMenuItem(MenuItem item)
    {
        if(SearchType.FOREIGN.equals(selectedSearchType))
        {
            selectedSearchType = SearchType.ENGLISH;
            item.setTitle(getString(R.string.menu_search_english));
        }
        else
        {
            selectedSearchType = SearchType.FOREIGN;
            item.setTitle(getString(R.string.menu_search_foreign));
        }

        searchListener.setSearchType(selectedSearchType);

        String searchText = searchEditText.getText().toString();
        applyVocabularyFilter(searchText);
    }

    private void applyVocabularyFilter(String searchText)
    {
        if(searchText.length() > 0)
        {
            Log.d(LogsManager.TAG, CLASS_NAME + ": applyVocabularyFilter. searchText=" + searchText + " selectedSearchType=" + selectedSearchType);
            LogsManager.addToLogs(CLASS_NAME + ": applyVocabularyFilter. searchText=" + searchText + " selectedSearchType=" + selectedSearchType);

            vocabularyAdapter.filter(searchText, selectedSearchType);
        }
    }
}
