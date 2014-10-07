package com.aaron.vocabulary.fragment;

import java.util.ArrayList;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.bean.Vocabulary;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

/**
 * The update dialog fragment that retrieves vocabulary list from the server. 
 */
public class UpdateFragment extends DialogFragment
{
    public static final String EXTRA_VOCABULARY_LIST = "com.aaron.vocabulary.fragment.vocabulary_list";

    /**
     * Creates a new UpdateFragment and sets its arguments.
     * @return UpdateFragment
     */
    public static UpdateFragment newInstance()
    {
        Bundle args = new Bundle();

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
         * Retrieves data from server then returns the data, encapsulated in the intent, to VocaublaryListFragment.
         */
        @Override
        protected String doInBackground(Void... arg0)
        {
            //TODO: GET VOCAB LIST FROM SERVER (JSON format), then save to list
            ArrayList<Vocabulary> list = new ArrayList<>();
            String response = "SUCCESS";

            // Update argument to preserve the list on rotation
            getArguments().putSerializable(EXTRA_VOCABULARY_LIST, list);
            this.sendResult(list, Activity.RESULT_OK);

            int listLength = list.size();
            String message = "";

            if("SUCCESS".equals(response))
            {
                if(listLength > 1)
                {
                    message = listLength + " new vocabularies added.";
                }
                else if(listLength == 1)
                {
                    message = listLength + " new vocabulary added.";
                }
                else
                {
                    message = "No new vocabularies available.";
                }
            }
            else
            {
                message = response;
            }

            return message;
        }
        
        /**
         * Removes the dialog from screen, and shows the result of the operation on toast.
         */
        @Override
        public void onPostExecute(String message)
        {
            dismiss();
            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        }
    }
}