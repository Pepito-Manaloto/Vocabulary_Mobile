package com.aaron.vocabulary.fragment;

import java.util.HashMap;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.bean.Vocabulary.ForeignLanguage;
import com.aaron.vocabulary.model.LogsManager;
import com.aaron.vocabulary.model.VocabularyManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import static com.aaron.vocabulary.model.VocabularyManager.*;

/**
 * The application about fragment.
 */
public class AboutFragment extends Fragment
{
    public static final String TAG = "AboutFragment";
    private VocabularyManager vocabularyManager;

    /**
     * Initializes non-fragment user interface.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.menu_about);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

        this.vocabularyManager = new VocabularyManager(getActivity());

        Log.d(LogsManager.TAG, "AboutFragment: onCreate.");
    }

    /**
     * Initializes about fragment user interface.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_about, parent, false);

        view.setOnLongClickListener(new OnLongClickListener()
            {
                /**
                 * If yes is selected, vocabularies on disk will be deleted.
                 */
                @Override
                public boolean onLongClick(View arg0)
                {
                    promptUserOnDelete();
                    return true;
                }
            });

        final TextView buildNumberTextView = (TextView) view.findViewById(R.id.text_build_number);
        TextView lastUpdatedTextView = (TextView) view.findViewById(R.id.text_last_updated);

        String buildNumber = getActivity().getString(R.string.build_num);
        String lastUpdated = this.vocabularyManager.getLastUpdated(DATE_FORMAT_LONG);
        HashMap<ForeignLanguage, Integer> vocabularyCount = this.vocabularyManager.getVocabulariesCount();

        buildNumberTextView.setText(buildNumber);
        lastUpdatedTextView.setText(lastUpdated);

        GridLayout grid = (GridLayout) view.findViewById(R.id.gridlayout_count);
        grid.setColumnCount(2);
        grid.setRowCount(vocabularyCount.keySet().size());

        int ctr = 0;
        for(ForeignLanguage key: vocabularyCount.keySet())
        {
            // Label
            GridLayout.LayoutParams layoutParamLabel = new GridLayout.LayoutParams(GridLayout.spec(ctr, GridLayout.LEFT),
                                                                                   GridLayout.spec(0, GridLayout.LEFT));

            TextView label = new TextView(getActivity());
            label.setText(key.name());
            label.setTextAppearance(getActivity(), R.style.TextView_sub_about);

            // Count
            GridLayout.LayoutParams layoutParamCount = new GridLayout.LayoutParams(GridLayout.spec(ctr, GridLayout.LEFT),
                                                                                   GridLayout.spec(1, GridLayout.LEFT));
            layoutParamCount.setMargins(75, 0, 0, 0);
            TextView count = new TextView(getActivity());
            count.setText(String.valueOf(vocabularyCount.get(key)));
            count.setTextAppearance(getActivity(), R.style.TextView_sub_about);

            grid.addView(label, layoutParamLabel);
            grid.addView(count, layoutParamCount);
            
            ctr++;
        }

        Log.d(LogsManager.TAG, "AboutFragment: onCreateView.");

        return view;
    }

    /**
     * Pops-up a prompt dialog with 'yes' or 'no' button.
     * Selecting 'yes' will delete all vocabularies from disk. 
     */
    private void promptUserOnDelete()
    {
        Log.d(LogsManager.TAG, "AboutFragment: promptUserOnDelete.");
        LogsManager.addToLogs("AboutFragment: promptUserOnDelete.");

        AlertDialog.Builder prompt = new AlertDialog.Builder(getActivity());
        prompt.setMessage("Delete vocabularies from disk?");

        prompt.setPositiveButton("Yes", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    Log.d(LogsManager.TAG, "AboutFragment: promptUserOnDelete. Yes selected.");
                    LogsManager.addToLogs("AboutFragment: promptUserOnDelete. Yes selected.");

                    vocabularyManager.deleteVocabulariesFromDisk();
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                }
            });
        prompt.setNegativeButton("No", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    Log.d(LogsManager.TAG, "AboutFragment: promptUserOnDelete. No selected.");
                    LogsManager.addToLogs("AboutFragment: promptUserOnDelete. No selected.");

                    dialog.cancel();
                }
            });

        prompt.create()
              .show();
    }
}
