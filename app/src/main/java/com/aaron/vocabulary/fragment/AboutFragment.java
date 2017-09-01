package com.aaron.vocabulary.fragment;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.bean.Settings;
import com.aaron.vocabulary.bean.Vocabulary.ForeignLanguage;
import com.aaron.vocabulary.model.LogsManager;
import com.aaron.vocabulary.model.VocabularyManager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import static com.aaron.vocabulary.model.VocabularyManager.*;
import static com.aaron.vocabulary.fragment.SettingsFragment.EXTRA_SETTINGS;

/**
 * The application about fragment.
 */
public class AboutFragment extends Fragment
{
    public static final String CLASS_NAME = AboutFragment.class.getSimpleName();
    private VocabularyManager vocabularyManager;
    private Settings settings;

    /**
     * Initializes non-fragment user interface.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.settings = (Settings) getActivity().getIntent().getSerializableExtra(EXTRA_SETTINGS);

        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.menu_about);

        ActionBar actionBar = getActivity().getActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        this.vocabularyManager = new VocabularyManager(getActivity());

        Log.d(LogsManager.TAG, CLASS_NAME + ": onCreate.");
    }

    /**
     * Initializes about fragment user interface.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_about, parent, false);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener()
        {
            /**
             * Handles back button.
             */
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event)
            {
                // For back button
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
                {
                    setFragmentActivityResult();
                    return true;
                }
                else
                {
                    return false;
                }
            }
        });

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


        buildNumberTextView.setText(buildNumber);
        lastUpdatedTextView.setText(lastUpdated);

        // Create layout and UI for foreign languages count.
        GridLayout grid = (GridLayout) view.findViewById(R.id.gridlayout_count);
        grid.setColumnCount(2);

        EnumMap<ForeignLanguage, Integer> vocabularyCount = this.vocabularyManager.getVocabulariesCount();
        Set<Map.Entry<ForeignLanguage, Integer>> entrySet = vocabularyCount.entrySet();
        grid.setRowCount(entrySet.size());

        int ctr = 0;
        for(Map.Entry<ForeignLanguage, Integer> entry : entrySet)
        {
            // Label
            GridLayout.LayoutParams layoutParamLabel = new GridLayout.LayoutParams(GridLayout.spec(ctr, GridLayout.LEFT), GridLayout.spec(0, GridLayout.LEFT));

            TextView label = new TextView(getActivity());
            label.setText(entry.getKey().name());

            if(Build.VERSION.SDK_INT < 23)
            {
                label.setTextAppearance(getActivity(), R.style.TextView_sub_about);
            }
            else
            {
                label.setTextAppearance(R.style.TextView_sub_about);
            }

            // Count
            GridLayout.LayoutParams layoutParamCount = new GridLayout.LayoutParams(GridLayout.spec(ctr, GridLayout.LEFT), GridLayout.spec(1, GridLayout.LEFT));
            layoutParamCount.setMargins(75, 0, 0, 0);
            TextView count = new TextView(getActivity());
            count.setText(String.valueOf(entry.getValue()));
            if(Build.VERSION.SDK_INT < 23)
            {
                count.setTextAppearance(getActivity(), R.style.TextView_sub_about);
            }
            else
            {
                count.setTextAppearance(R.style.TextView_sub_about);
            }

            grid.addView(label, layoutParamLabel);
            grid.addView(count, layoutParamCount);

            ctr++;
        }

        Log.d(LogsManager.TAG, CLASS_NAME + ": onCreateView.");

        return view;
    }

    /**
     * This method is called when a user selects an item in the menu bar. Home button.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
            {
                this.setFragmentActivityResult();
                return true;
            }
            default:
            {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    /**
     * Pops-up a prompt dialog with 'yes' or 'no' button.
     * Selecting 'yes' will delete all vocabularies from disk.
     */
    private void promptUserOnDelete()
    {
        Log.d(LogsManager.TAG, CLASS_NAME + ": promptUserOnDelete.");
        LogsManager.addToLogs(CLASS_NAME + ": promptUserOnDelete.");

        AlertDialog.Builder prompt = new AlertDialog.Builder(getActivity());
        prompt.setMessage("Delete vocabularies from disk?");

        prompt.setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                Log.d(LogsManager.TAG, CLASS_NAME + ": promptUserOnDelete. Yes selected.");
                LogsManager.addToLogs(CLASS_NAME + ": promptUserOnDelete. Yes selected.");

                vocabularyManager.deleteVocabulariesFromDisk();
                setFragmentActivityResult();
            }
        });
        prompt.setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                Log.d(LogsManager.TAG, CLASS_NAME + ": promptUserOnDelete. No selected.");
                LogsManager.addToLogs(CLASS_NAME + ": promptUserOnDelete. No selected.");

                dialog.cancel();
            }
        });

        prompt.create().show();
    }

    /**
     * Sets the current settings and sends it to the main activity fragment.
     */
    private void setFragmentActivityResult()
    {
        Intent data = new Intent();

        data.putExtra(EXTRA_SETTINGS, this.settings);
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();

        Log.d(LogsManager.TAG, CLASS_NAME + ": setFragmentAcivityResult. Current settings -> " + this.settings);
        LogsManager.addToLogs(CLASS_NAME + ": setFragmentAcivityResult. Current settings -> " + this.settings);
    }
}