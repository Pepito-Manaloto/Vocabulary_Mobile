package com.aaron.vocabulary.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.bean.Settings;
import com.aaron.vocabulary.bean.Vocabulary;
import com.aaron.vocabulary.model.LogManager;
import com.aaron.vocabulary.model.VocabularyManager;

/**
 * The update dialog fragment that retrieves vocabulary list from the server. 
 */
public class UpdateFragment extends DialogFragment
{
    public static final String EXTRA_VOCABULARY_LIST = "com.aaron.vocabulary.fragment.vocabulary_list";
    private VocabularyManager vocabularyManager;

    /**
     * Creates a new UpdateFragment and sets its arguments.
     * @return UpdateFragment
     */
    public static UpdateFragment newInstance(final Settings settings)
    {
        Bundle args = new Bundle();
        args.putSerializable(SettingsFragment.EXTRA_SETTINGS, settings);
        UpdateFragment fragment = new UpdateFragment();
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Creates the update dialog box.
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getString(R.string.dialog_update_title));
        progressDialog.setMessage(getString(R.string.dialog_update_message));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);

        Settings settings = (Settings) this.getArguments().getSerializable(SettingsFragment.EXTRA_SETTINGS);
        this.vocabularyManager = new VocabularyManager(getActivity(), settings);

        Log.d(LogManager.TAG, "UpdateFragment: onCreateDialog");
        return progressDialog;
    }

    /**
     * Start the retriever thread.
     */
    @Override
    public void onStart()
    {
        super.onStart();
        new VocabularyRetrieverThread().execute();
        Log.d(LogManager.TAG, "UpdateFragment: onStart");
    }

    /**
     * Helper thread class that does the retrieval of the vocabulary list from the server.
     */
    private class VocabularyRetrieverThread extends AsyncTask<Void, Void, String>
    {
        /**
         * Encapsulates the vocabulary list and response to an intent and sends the intent + resultCode to VocaublaryListFragment.
         * @param vocabList the retrieved vocabulary list
         * @param response the response of the web call
         * @param resultCode the result of the operation
         */
        private void sendResult(final ArrayList<Vocabulary> vocabList, final int resultCode)
        {
            if(getTargetFragment() == null)
            {
                return;
            }

            Intent data = new Intent();
            data.putExtra(EXTRA_VOCABULARY_LIST, vocabList);
            getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, data);
        }

        /**
         * Retrieves data from server (also save to local disk) then returns the data, encapsulated in the intent, to VocaublaryListFragment.
         */
        @Override
        protected String doInBackground(Void... arg0)
        {
            ArrayList<Vocabulary> list = new ArrayList<>();

            list = vocabularyManager.getVocabulariesFromWeb();
            String responseCode = vocabularyManager.getStatusText();
            String responseText = vocabularyManager.getResponseText();

            this.sendResult(list, Activity.RESULT_OK);

            int newCount = vocabularyManager.getRecentlyAddedCount();
            String message = "";

            if("Ok".equals(responseCode))
            {
                if(!"Success".equals(responseText))
                {
                    message = responseText;
                }
                else if(newCount > 1)
                {
                    message = newCount + " new vocabularies added.";
                }
                else if(newCount == 1)
                {
                    message = newCount + " new vocabulary added.";
                }
                else
                {
                    message = "No new vocabularies available.";
                }
            }
            else
            {
                message = responseCode + ". " + responseText;
            }

            return message;
        }

        /**
         * Removes the dialog from screen, and shows the result of the operation on toast.
         */
        @Override
        public void onPostExecute(String message)
        {
            UpdateFragment.this.dismiss();
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }
    }
}
