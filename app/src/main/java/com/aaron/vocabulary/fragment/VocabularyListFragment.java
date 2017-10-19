package com.aaron.vocabulary.fragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
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
import com.aaron.vocabulary.model.LogsManager;
import com.aaron.vocabulary.model.VocabularyManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.aaron.vocabulary.fragment.SettingsFragment.EXTRA_SETTINGS;

/**
 * Main view fragment containing the vocabulary list with main menu bar.
 */
public class VocabularyListFragment extends ListFragment
{
    public static final String CLASS_NAME = VocabularyListFragment.class.getSimpleName();
    public static final String EXTRA_VOCABULARY_LIST = "com.aaron.vocabulary.fragment.vocabulary_list.list";

    private static final String DIALOG_UPDATE = "update";
    private static final int REQUEST_UPDATE = 0;
    private static final int REQUEST_SETTINGS = 1;
    private static final int REQUEST_ABOUT = 2;
    private static final int REQUEST_LOGS = 3;

    private ArrayList<Vocabulary> list;
    private VocabularyAdapter vocabularyAdapter;

    private Settings settings;
    private VocabularyManager vocabularyManager;
    private EditText searchTextfield;
    private SearchListener searchListener;
    private SearchType selectedSearchType;

    /**
     * Initializes non-fragment user interface.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null)
        {
            this.settings = savedInstanceState.getParcelable(EXTRA_SETTINGS);

            // But we are sure of its type
            this.list = savedInstanceState.getParcelableArrayList(EXTRA_VOCABULARY_LIST);
        }

        if(this.settings == null)
        {
            this.settings = new Settings();
        }

        this.vocabularyManager = new VocabularyManager(getActivity().getApplicationContext());

        if(this.list == null)
        {
            this.list = this.vocabularyManager.getVocabulariesFromDisk(this.settings.getForeignLanguage());
        }

        this.vocabularyAdapter = new VocabularyAdapter(getActivity(), this.list, this.settings);
        this.setListAdapter(this.vocabularyAdapter);
        // this.updateListOnUiThread(this.list);
        setHasOptionsMenu(true);
        this.selectedSearchType = SearchType.ENGLISH;
        this.searchListener = new SearchListener(this.vocabularyAdapter, this.selectedSearchType);

        Log.d(LogsManager.TAG, CLASS_NAME + ": onCreate. settings=" + this.settings + " list=" + this.list);
        LogsManager.addToLogs(CLASS_NAME + ": onCreate. settings=" + this.settings + " list_size=" + this.list.size());
    }

    /**
     * Initializes the fragment's user interface.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_vocabulary_list, parent, false);

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

        String language = getString(R.string.app_name, this.settings.getForeignLanguage().name());
        getActivity().setTitle(language);

        this.vocabularyAdapter.notifyDataSetChanged();

        Log.d(LogsManager.TAG, CLASS_NAME + ": onResume");
    }

    /**
     * Saves current state and settings in memory. For screen rotation.
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putParcelable(EXTRA_SETTINGS, this.settings);
        outState.putParcelableArrayList(EXTRA_VOCABULARY_LIST, this.list);

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

        // Update action bar menu processing result
        if(requestCode == REQUEST_UPDATE && data != null && data.hasExtra(UpdateFragment.EXTRA_VOCABULARY_LIST))
        {
            // But we are sure of its type
            @SuppressWarnings("unchecked")
            ArrayList<Vocabulary> list = data.getParcelableArrayListExtra(UpdateFragment.EXTRA_VOCABULARY_LIST);

            // Handles occasional NullPointerException.
            if(list != null && !list.isEmpty())
            {
                this.list = list;
            }
            else
            {
                this.list = this.vocabularyManager.getVocabulariesFromDisk(this.settings.getForeignLanguage());
            }

            this.updateListOnUiThread(this.list);
        }
        else if((requestCode == REQUEST_SETTINGS || requestCode == REQUEST_ABOUT || requestCode == REQUEST_LOGS) && (data != null && data.hasExtra(EXTRA_SETTINGS)))
        {
            this.settings = data.getParcelableExtra(EXTRA_SETTINGS);

            ForeignLanguage language = ForeignLanguage.Hokkien;
            if(this.settings != null)
            {
                language = this.settings.getForeignLanguage();
            }

            this.list = this.vocabularyManager.getVocabulariesFromDisk(language);
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

        // Get the action view of the menu item whose id is edittext_search_field
        View view = menu.findItem(R.id.menu_search).getActionView();

        // Get the edit text from the action view
        this.searchTextfield = view.findViewById(R.id.edittext_search_field);
        this.searchTextfield.setHint(R.string.hint_vocabulary);
        this.searchTextfield.addTextChangedListener(this.searchListener);
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

                if(updateDialog.isUpdating())
                {
                    Toast.makeText(getActivity(), getActivity().getString(R.string.dialog_already_updating_message), Toast.LENGTH_LONG).show();
                }
                else
                {
                    updateDialog.setTargetFragment(this, REQUEST_UPDATE);
                    updateDialog.show(fm, DIALOG_UPDATE);
                }

                return true;
            }
            case R.id.menu_settings:
            {
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                intent.putExtra(EXTRA_SETTINGS, this.settings);
                startActivityForResult(intent, REQUEST_SETTINGS);

                return true;
            }
            case R.id.menu_search_type:
            {
                this.toggleSearchTypeMenuItem(item);

                return true;
            }
            case R.id.menu_about:
            {
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                intent.putExtra(EXTRA_SETTINGS, this.settings);
                startActivityForResult(intent, REQUEST_ABOUT);

                return true;
            }
            case R.id.menu_logs:
            {
                Intent intent = new Intent(getActivity(), LogsActivity.class);
                intent.putExtra(EXTRA_SETTINGS, this.settings);
                startActivityForResult(intent, REQUEST_LOGS);

                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    /**
     * Toggles the search type menu item between 'english' and 'foreign'.
     *
     * @param item
     *            the search type menu item
     */
    private void toggleSearchTypeMenuItem(MenuItem item)
    {
        if(SearchType.FOREIGN.equals(this.selectedSearchType))
        {
            this.selectedSearchType = SearchType.ENGLISH;
            item.setTitle(getString(R.string.menu_search_english));
        }
        else
        {
            this.selectedSearchType = SearchType.FOREIGN;
            item.setTitle(getString(R.string.menu_search_foreign));
        }

        this.searchListener.setSearchType(this.selectedSearchType);

        String searched = this.searchTextfield.getText().toString();
        if(searched.length() > 0)
        {
            this.vocabularyAdapter.filter(searched, this.selectedSearchType);
        }
    }

    /**
     * Updates the list view on UI thread.
     */
    private void updateListOnUiThread(final ArrayList<Vocabulary> list)
    {
        if(list != null)
        {
            this.getActivity().runOnUiThread(new UpdateAdapterRunnable(this.vocabularyAdapter, this.list));
        }
    }

    /**
     * Helper class for ListView's scroll listener.
     */
    private static class ShowHideFastScrollListener implements AbsListView.OnScrollListener, Runnable
    {
        private static final int DELAY = 1000;
        private AbsListView view;
        private final Handler handler = new Handler();

        /**
         * Runnable for handler object.
         */
        @Override
        public void run()
        {
            this.view.setFastScrollAlwaysVisible(false);
            this.view = null;
        }

        /**
         * Show fast-scroll thumb if scrolling, and hides fast-scroll thumb if not scrolling.
         */
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState)
        {
            if(scrollState != SCROLL_STATE_IDLE)
            {
                view.setFastScrollAlwaysVisible(true);

                // Removes the runnable from the message queue.
                // Stops the handler from hiding the fast-scroll.
                this.handler.removeCallbacks(this);
            }
            else
            {
                this.view = view;

                // Adds the runnable to the message queue, will run after the DELAY.
                // Hides the fast-scroll after two seconds of no scrolling.
                this.handler.postDelayed(this, DELAY);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
        {
        }
    }

    /**
     * Helper class for search listener.
     */
    private static class SearchListener implements TextWatcher
    {
        private VocabularyAdapter adapter;
        private SearchType searchType;

        SearchListener(VocabularyAdapter adapter, SearchType searchType)
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

            Log.d(LogsManager.TAG, CLASS_NAME + ": onCreateOptionsMenu(afterTextChanged). searched=" + searched);
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
        {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
        {
        }

        private void setSearchType(SearchType searchType)
        {
            this.searchType = searchType;
        }

        private void setVocabularyAdapter(VocabularyAdapter vocabularyAdapter)
        {
            this.adapter = vocabularyAdapter;
        }
    }

    /**
     * Helper class that updates the list on UI thread.
     */
    private static class UpdateAdapterRunnable implements Runnable
    {
        private WeakReference<VocabularyAdapter> vocabularyAdapterRef;
        private final ArrayList<Vocabulary> list;

        UpdateAdapterRunnable(VocabularyAdapter adapter, ArrayList<Vocabulary> list)
        {
            this.vocabularyAdapterRef = new WeakReference<>(adapter);
            this.list = list;
        }

        @Override
        public void run()
        {
            VocabularyAdapter adapter = this.vocabularyAdapterRef.get();
            if(adapter != null)
            {
                adapter.update(this.list);
            }
        }
    }
}
