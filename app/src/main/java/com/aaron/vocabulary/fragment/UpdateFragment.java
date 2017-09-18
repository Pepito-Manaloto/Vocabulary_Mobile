package com.aaron.vocabulary.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.bean.ResponseVocabulary;
import com.aaron.vocabulary.bean.Settings;
import com.aaron.vocabulary.bean.Vocabulary;
import com.aaron.vocabulary.model.LogsManager;
import com.aaron.vocabulary.model.VocabularyManager;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The update dialog fragment that retrieves vocabulary list from the server.
 */
public class UpdateFragment extends DialogFragment
{
    public static final String CLASS_NAME = UpdateFragment.class.getSimpleName();
    public static final String EXTRA_VOCABULARY_LIST = "com.aaron.vocabulary.fragment.update.list";
    private VocabularyManager vocabularyManager;
    private Settings settings;
    private String url;
    private static final AtomicBoolean isUpdating = new AtomicBoolean(false);

    /**
     * Creates a new UpdateFragment and sets its arguments.
     *
     * @return UpdateFragment
     */
    public static UpdateFragment newInstance(final Settings settings)
    {
        Bundle args = new Bundle();
        args.putParcelable(SettingsFragment.EXTRA_SETTINGS, settings);
        UpdateFragment fragment = new UpdateFragment();
        fragment.setArguments(args);

        Log.d(LogsManager.TAG, CLASS_NAME + ": newInstance. settings=" + settings);

        return fragment;
    }

    /**
     * Creates the update dialog box.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Activity activity = getActivity();
        // TODO: Replace with ProgressBar
        ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle(getString(R.string.dialog_update_title));
        progressDialog.setMessage(getString(R.string.dialog_update_message));
        progressDialog.setIndeterminate(true);

        this.settings = getArguments().getParcelable(SettingsFragment.EXTRA_SETTINGS);
        if(settings != null && settings.getServerURL() != null && !settings.getServerURL().isEmpty())
        {
            this.url = "http://" + settings.getServerURL() + activity.getString(R.string.url_resource);
        }
        else
        {
            this.url = "http://" + activity.getString(R.string.url_address_default) + activity.getString(R.string.url_resource);
        }

        this.vocabularyManager = new VocabularyManager(activity.getApplicationContext());

        Log.d(LogsManager.TAG, CLASS_NAME + ": onCreateDialog. settings=" + settings);
        LogsManager.addToLogs(CLASS_NAME + ": onCreateDialog. settings=" + settings);

        return progressDialog;
    }

    /**
     * Start the retriever thread.
     */
    @Override
    public void onStart()
    {
        super.onStart();

        if(!isUpdating())
        {
            VocabularyRetrieverThread vocabularyRetrieverThread = new VocabularyRetrieverThread(this);
            vocabularyRetrieverThread.execute();
            isUpdating.set(true);
        }

        Log.d(LogsManager.TAG, CLASS_NAME + ": onStart");
    }

    /**
     * Returns true if VocabularyRetrieverThread is already executing update.
     *
     * @return boolean
     */
    public boolean isUpdating()
    {
        return isUpdating.get();
    }

    public VocabularyManager getVocabularyManager()
    {
        return this.vocabularyManager;
    }

    public String getUrl()
    {
        return this.url;
    }

    public Settings getSettings()
    {
        return this.settings;
    }

    /**
     * Helper thread class that does the retrieval of the vocabulary list from the server.
     */
    private static class VocabularyRetrieverThread extends AsyncTask<Void, Void, String>
    {
        private WeakReference<UpdateFragment> fragmentRef;
        private static final ArrayList<Vocabulary> EMPTY_LIST = new ArrayList<>(0);

        VocabularyRetrieverThread(UpdateFragment fragment)
        {
            this.fragmentRef = new WeakReference<>(fragment);
        }

        /**
         * Encapsulates the vocabulary list and response to an intent and sends the intent + resultCode to VocaublaryListFragment.
         *
         * @param vocabList
         *            the retrieved vocabulary list
         * @param resultCode
         *            the result of the operation
         */
        private void sendResult(final ArrayList<Vocabulary> vocabList, final int resultCode)
        {
            UpdateFragment fragment = this.fragmentRef.get();
            if(fragment != null)
            {
                Fragment targetFragment = fragment.getTargetFragment();
                if(targetFragment == null)
                {
                    return;
                }

                Intent data = new Intent();
                data.putExtra(EXTRA_VOCABULARY_LIST, vocabList);
                targetFragment.onActivityResult(fragment.getTargetRequestCode(), resultCode, data);

                Log.d(LogsManager.TAG, CLASS_NAME + "(VocabularyRetrieverThread): sendResult. list=" + vocabList);
                LogsManager.addToLogs(CLASS_NAME + "(VocabularyRetrieverThread): sendResult. list_size=" + vocabList.size());
            }

        }

        /**
         * Retrieves data from server (also save to local disk) then returns the data, encapsulated in the intent, to VocaublaryListFragment.
         */
        @Override
        protected String doInBackground(Void... arg0)
        {
            String message = "Fragment no longer exists.";

            UpdateFragment fragment = this.fragmentRef.get();
            if(fragment != null)
            {
                VocabularyManager vocabularyManager = fragment.getVocabularyManager();
                String url = fragment.getUrl();
                Settings settings = fragment.getSettings();

                ResponseVocabulary response = vocabularyManager.getVocabulariesFromWeb(url);

                if(response.getStatusCode() == HttpURLConnection.HTTP_OK)
                {
                    EnumMap<Vocabulary.ForeignLanguage, ArrayList<Vocabulary>> map = response.getVocabularyMap();
                    if(map == null || map.isEmpty())
                    {
                        message = "No new vocabularies available.";
                    }
                    else
                    {
                        boolean saveToDiskSuccess = vocabularyManager.saveRecipesToDisk(map.values());
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

                            // Get recipes to be returned to VocabularyListFragment
                            ArrayList<Vocabulary> list = vocabularyManager.getVocabulariesFromMap(map, settings.getForeignLanguage());
                            this.sendResult(list, Activity.RESULT_OK);
                        }
                        else
                        {
                            message = "Failed saving to disk.";
                        }
                    }
                }
                else
                {
                    message = response.getStatusCode() + ". " + response.getText();
                }

                this.sendResult(EMPTY_LIST, Activity.RESULT_OK);
            }

            return message;
        }

        /**
         * Removes the dialog from screen, shows the result of the operation on toast, and sets the isUpdating flag to false..
         */
        @Override
        public void onPostExecute(String message)
        {
            UpdateFragment fragment = this.fragmentRef.get();
            if(fragment != null)
            {
                fragment.dismiss();
                isUpdating.set(false);

                Activity activity = fragment.getActivity();
                if(activity != null)
                {
                    Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                }
            }
        }

        /**
         * Sets the isUpdating flag to false.
         */
        @Override
        protected void onCancelled(String message)
        {
            super.onCancelled();
            isUpdating.set(false);
        }
    }
}
