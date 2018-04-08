package com.aaron.vocabulary.fragment;

import android.app.Activity;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.activity.AboutActivity;
import com.aaron.vocabulary.activity.LogsActivity;
import com.aaron.vocabulary.activity.SettingsActivity;
import com.aaron.vocabulary.adapter.VocabularyAdapter;
import com.aaron.vocabulary.bean.ForeignLanguage;
import com.aaron.vocabulary.bean.ResponseVocabulary;
import com.aaron.vocabulary.bean.SearchType;
import com.aaron.vocabulary.bean.Settings;
import com.aaron.vocabulary.bean.Vocabulary;
import com.aaron.vocabulary.fragment.listener.ShowHideFastScrollListener;
import com.aaron.vocabulary.fragment.listener.VocabularySearchListener;
import com.aaron.vocabulary.model.HttpClient;
import com.aaron.vocabulary.model.LogsManager;
import com.aaron.vocabulary.model.VocabularyManager;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

import static com.aaron.vocabulary.bean.DataKey.EXTRA_SETTINGS;
import static com.aaron.vocabulary.bean.DataKey.EXTRA_VOCABULARY_LIST;
import static com.aaron.vocabulary.model.VocabularyManager.DATE_FORMAT_WEB;

/**
 * Main view fragment containing the vocabulary list with main menu bar.
 */
public class VocabularyListFragment extends ListFragment
{
    private enum MenuRequest
    {
        SETTINGS(0), ABOUT(1), LOGS(2);

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
    private static final Pattern IP_ADDRESS_PATTERN = Pattern.compile("(\\d{1,3}\\.){3}\\d{1,3}");
    private static final AtomicBoolean IS_UPDATING = new AtomicBoolean(false);

    private ArrayList<Vocabulary> list;
    private VocabularyAdapter vocabularyAdapter;

    private Settings settings;
    private EditText searchEditText;
    private ProgressBar updateProgressBar;
    private VocabularySearchListener searchListener;
    private SearchType selectedSearchType;
    private ForeignLanguage selectedForeignLanguage = ForeignLanguage.Hokkien;

    private VocabularyManager vocabularyManager;
    private HttpClient httpClient;
    private CompositeDisposable compositeDisposable;

    /**
     * Initializes non-fragment user interface.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        vocabularyManager = new VocabularyManager(getActivity().getApplicationContext());
        httpClient = new HttpClient(getString(R.string.url_address_default));
        compositeDisposable = new CompositeDisposable();

        initializeSettings(savedInstanceState);
        initializeVocabularyList(savedInstanceState);

        vocabularyAdapter = new VocabularyAdapter(getActivity(), list, settings);
        setListAdapter(vocabularyAdapter);

        setHasOptionsMenu(true);
        selectedSearchType = SearchType.ENGLISH;
        searchListener = new VocabularySearchListener(vocabularyAdapter, selectedSearchType);

        LogsManager.log(CLASS_NAME, "onCreate", "settings=" + settings + " list_size=" + list.size());
    }

    private void initializeSettings(Bundle savedInstanceState)
    {
        if(savedInstanceState != null && savedInstanceState.containsKey(EXTRA_SETTINGS.toString()))
        {
            settings = savedInstanceState.getParcelable(EXTRA_SETTINGS.toString());
        }

        if(settings == null)
        {
            settings = new Settings();
        }
        else
        {
            selectedForeignLanguage = settings.getForeignLanguage();
        }
    }

    private void initializeVocabularyList(Bundle savedInstanceState)
    {
        if(savedInstanceState != null && savedInstanceState.containsKey(EXTRA_VOCABULARY_LIST.toString()))
        {
            list = savedInstanceState.getParcelableArrayList(EXTRA_VOCABULARY_LIST.toString());
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

        updateProgressBar = view.findViewById(R.id.progress_bar_update);

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

        outState.putParcelable(EXTRA_SETTINGS.toString(), settings);
        outState.putParcelableArrayList(EXTRA_VOCABULARY_LIST.toString(), list);

        Log.d(LogsManager.TAG, CLASS_NAME + ": onSaveInstanceState");
    }

    /**
     * Receives the result data from the previous fragment. Updates the application's state depending on the data received.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        doneUpdating(); // Ensures this flag is set to false upon Activity transition while updating.

        if(resultCode != Activity.RESULT_OK)
        {
            return;
        }

        LogsManager.log(CLASS_NAME, "onActivityResult", "requestCode=" + requestCode + " resultCode=" + resultCode);

        boolean requestResultFromSettingsOrAboutOrLogs = requestCode == MenuRequest.SETTINGS.getCode() || requestCode == MenuRequest.ABOUT.getCode()
                || requestCode == MenuRequest.LOGS.getCode();
        boolean hasExtraSettingsData = data != null && data.hasExtra(EXTRA_SETTINGS.toString());
        if(requestResultFromSettingsOrAboutOrLogs && hasExtraSettingsData)
        {
            settings = data.getParcelableExtra(EXTRA_SETTINGS.toString());

            if(settings != null)
            {
                boolean foreignLanguageChanged = !selectedForeignLanguage.equals(settings.getForeignLanguage());

                selectedForeignLanguage = settings.getForeignLanguage();
                list = vocabularyManager.getVocabulariesFromDisk(selectedForeignLanguage);

                if(foreignLanguageChanged)
                {
                    reinitializeAdapterAndSearchListener();
                }
                else
                {
                    updateVocabularyAdapterInUiThread();
                }

                updateRetrofitBaseUrl(requestCode);
            }
        }
    }

    private void reinitializeAdapterAndSearchListener()
    {
        vocabularyAdapter = new VocabularyAdapter(getActivity(), list, settings);
        setListAdapter(vocabularyAdapter);
        searchListener.setVocabularyAdapter(vocabularyAdapter);
    }

    private void updateRetrofitBaseUrl(int requestCode)
    {
        if(requestCode == MenuRequest.SETTINGS.getCode() && isValidURL(settings.getServerURL()))
        {
            HttpClient.reinitializeRetrofit(settings.getServerURL());
        }
    }

    private boolean isValidURL(String newBaseUrl)
    {
        return IP_ADDRESS_PATTERN.matcher(newBaseUrl).matches();
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

    @Override
    public void onStop()
    {
        super.onStop();

        searchEditText.getText().clear();

        if(!this.compositeDisposable.isDisposed())
        {
            this.compositeDisposable.dispose();
        }

        doneUpdating();
    }

    /**
     * This method is called when a user selects an item in the menu bar. Opens the fragment of selected item.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.menu_search:
            {
                return true;
            }
            case R.id.menu_update:
            {
                if(!IS_UPDATING.get())
                {
                    Log.d(LogsManager.TAG, CLASS_NAME + ": onOptionsItemSelected. Updating vocabularies.");
                    preUpdating();

                    Disposable disposable = httpClient.getVocabularies(vocabularyManager.getLastUpdated(DATE_FORMAT_WEB))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doFinally(this::doneUpdating)
                            .subscribeWith(updateVocabulariesFromWebObserver());

                    compositeDisposable.add(disposable);
                }
                else
                {
                    Toast.makeText(getContext(), getString(R.string.toast_already_updating), Toast.LENGTH_SHORT).show();
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

    private void preUpdating()
    {
        IS_UPDATING.set(true);
        updateProgressBar.setVisibility(View.VISIBLE);
    }

    private void doneUpdating()
    {
        IS_UPDATING.set(false);
        updateProgressBar.setVisibility(View.INVISIBLE);
    }

    private DisposableSingleObserver<ResponseVocabulary> updateVocabulariesFromWebObserver()
    {
        return new DisposableSingleObserver<ResponseVocabulary>()
        {
            @Override
            public void onSuccess(ResponseVocabulary response)
            {
                String message;
                EnumMap<ForeignLanguage, ArrayList<Vocabulary>> map = response.getVocabularyMap();
                if(map == null || map.isEmpty())
                {
                    message = "No new vocabularies available.";
                }
                else
                {
                    boolean saveToDiskSuccess = vocabularyManager.saveRecipesToDisk(map);
                    if(saveToDiskSuccess)
                    {
                        int newCount = response.getRecentlyAddedCount();
                        if(newCount > 1)
                        {
                            message = newCount + " new vocabularies added.";
                        }
                        else
                        {
                            message = newCount + " new vocabulary added.";
                        }

                        list = vocabularyManager.getVocabulariesFromMap(map, settings.getForeignLanguage());
                    }
                    else
                    {
                        message = "Failed saving to disk.";
                    }
                }

                updateVocabularyAdapterInUiThread();

                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(Throwable e)
            {
                Toast.makeText(getContext(), "Error updating vocabularies: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.d(LogsManager.TAG, CLASS_NAME + ": onError. Error updating vocabularies. " + e.getMessage());
            }
        };
    }

    private void startActivityWithExtraSettings(Class<? extends Activity> activityToStart, MenuRequest menuRequestOrigin)
    {
        Intent intent = new Intent(getActivity(), activityToStart);
        intent.putExtra(EXTRA_SETTINGS.toString(), settings);
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
            LogsManager.log(CLASS_NAME, "applyVocabularyFilter", "searchText=" + searchText + " selectedSearchType=" + selectedSearchType);

            vocabularyAdapter.filter(searchText, selectedSearchType);
        }
    }

    private void updateVocabularyAdapterInUiThread()
    {
        getActivity().runOnUiThread(() -> vocabularyAdapter.update(list));
    }
}
