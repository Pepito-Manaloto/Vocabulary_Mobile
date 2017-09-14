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
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.Toast;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.activity.AboutActivity;
import com.aaron.vocabulary.activity.LogsActivity;
import com.aaron.vocabulary.activity.SettingsActivity;
import com.aaron.vocabulary.adapter.VocabularyAdapter;
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
    private static final String DIALOG_UPDATE = "update";
    private static final int REQUEST_UPDATE = 0;
    private static final int REQUEST_SETTINGS = 1;
    private static final int REQUEST_ABOUT = 2;
    private static final int REQUEST_LOGS = 3;

    private ArrayList<Vocabulary> list;
    private Settings settings;
    private VocabularyManager vocabularyManager;

    public static final String EXTRA_VOCABULARY_LIST = "com.aaron.vocabulary.fragment.vocabulary_list.list";

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
            this.settings = (Settings) savedInstanceState.getSerializable(EXTRA_SETTINGS);

            // But we are sure of its type
            this.list = (ArrayList<Vocabulary>) savedInstanceState.getSerializable(EXTRA_VOCABULARY_LIST);
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

        this.updateListOnUiThread(this.list);

        setHasOptionsMenu(true);

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

        ((VocabularyAdapter) this.getListAdapter()).notifyDataSetChanged();

        Log.d(LogsManager.TAG, CLASS_NAME + ": onResume");
    }

    /**
     * Saves current state and settings in memory. For screen rotation.
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putSerializable(EXTRA_SETTINGS, this.settings);
        outState.putSerializable(EXTRA_VOCABULARY_LIST, this.list);

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
            ArrayList<Vocabulary> list = (ArrayList<Vocabulary>) data.getSerializableExtra(UpdateFragment.EXTRA_VOCABULARY_LIST);

            // Handles occasional NullPointerException.
            if(list != null && list.size() > 0)
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
            this.settings = (Settings) data.getSerializableExtra(EXTRA_SETTINGS);

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
        final EditText searchTextfield = view.findViewById(R.id.edittext_search_field);
        searchTextfield.setHint(R.string.hint_vocabulary);

        searchTextfield.addTextChangedListener(new SearchListener((VocabularyAdapter) getListAdapter()));
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
     * Updates the list view on UI thread.
     *
     */
    private void updateListOnUiThread(ArrayList<Vocabulary> list)
    {
        this.getActivity().runOnUiThread(new UpdateAdapterRunnable(this, list));
    }

    public Settings getSettings()
    {
        return this.settings;
    }

    /**
     * Helper class for ListView's scroll listener.
     */
    private static class ShowHideFastScrollListener implements OnScrollListener, Runnable
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

    private static class SearchListener implements TextWatcher
    {
        private VocabularyAdapter adapter;

        SearchListener(VocabularyAdapter adapter)
        {
            this.adapter = adapter;
        }

        /**
         * Handles search on text update.
         */
        @Override
        public void afterTextChanged(Editable textField)
        {
            String searched = textField.toString();
            this.adapter.filter(searched);
            this.adapter.notifyDataSetChanged();

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
    }

    private static class UpdateAdapterRunnable implements Runnable
    {
        private WeakReference<VocabularyListFragment> fragmentRef;
        private final ArrayList<Vocabulary> list;

        UpdateAdapterRunnable(VocabularyListFragment fragment, ArrayList<Vocabulary> list)
        {
            this.fragmentRef = new WeakReference<>(fragment);
            this.list = list;
        }

        @Override
        public void run()
        {
            VocabularyListFragment fragment = this.fragmentRef.get();

            if(fragment != null)
            {
                Settings settings = fragment.getSettings();

                VocabularyAdapter vocabularyAdapter = new VocabularyAdapter(fragment.getActivity(), list, settings);
                fragment.setListAdapter(vocabularyAdapter);

                Log.d(LogsManager.TAG, CLASS_NAME + ": updateListOnUiThread(run). settings=" + settings + " list=" + list);
                LogsManager.addToLogs(CLASS_NAME + ": updateListOnUiThread(run). settings=" + settings + " list_size=" + list.size());
            }

        }

    }
}
