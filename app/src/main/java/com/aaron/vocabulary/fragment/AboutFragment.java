package com.aaron.vocabulary.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.TextViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.bean.Settings;
import com.aaron.vocabulary.bean.ForeignLanguage;
import com.aaron.vocabulary.fragment.listener.BackButtonListener;
import com.aaron.vocabulary.fragment.listener.DeleteLongClickListener;
import com.aaron.vocabulary.model.LogsManager;
import com.aaron.vocabulary.model.VocabularyManager;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static com.aaron.vocabulary.fragment.SettingsFragment.EXTRA_SETTINGS;
import static com.aaron.vocabulary.model.VocabularyManager.DATE_FORMAT_DATABASE;

/**
 * The application about fragmentRef.
 */
public class AboutFragment extends Fragment implements Backable
{
    public static final String CLASS_NAME = AboutFragment.class.getSimpleName();

    private static final int FOREIGN_LANGUAGES_LABEL_COLUMN = 0;
    private static final int FOREIGN_LANGUAGES_COUNT_COLUMN = 1;
    private static final int FOREIGN_LANGUAGES_COUNT_LEFT_MARGIN = 75;
    private static final int FOREIGN_LANGUAGES_COLUMN_COUNT = 2;

    private VocabularyManager vocabularyManager;
    private Settings settings;

    /**
     * Initializes non-fragmentRef user interface.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.settings = getActivity().getIntent().getParcelableExtra(EXTRA_SETTINGS);

        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.menu_about);

        ActionBar actionBar = getActivity().getActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        this.vocabularyManager = new VocabularyManager(getActivity().getApplicationContext());

        Log.d(LogsManager.TAG, CLASS_NAME + ": onCreate.");
    }

    /**
     * Initializes about fragmentRef user interface.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_about, parent, false);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new BackButtonListener(this));
        view.setOnLongClickListener(new DeleteLongClickListener(this, this.vocabularyManager, this::setFragmentActivityResult));

        final TextView buildNumberTextView = view.findViewById(R.id.text_build_number);
        String buildNumber = getActivity().getString(R.string.build_num);
        buildNumberTextView.setText(buildNumber);

        TextView lastUpdatedTextView = view.findViewById(R.id.text_last_updated);
        String lastUpdated = this.vocabularyManager.getLastUpdated(DATE_FORMAT_DATABASE);
        lastUpdatedTextView.setText(lastUpdated);

        initializeForeignLanguagesCountView(view);

        Log.d(LogsManager.TAG, CLASS_NAME + ": onCreateView.");

        return view;
    }

    private void initializeForeignLanguagesCountView(View view)
    {
        GridLayout gridLayout = view.findViewById(R.id.gridlayout_count);
        gridLayout.setColumnCount(FOREIGN_LANGUAGES_COLUMN_COUNT);

        EnumMap<ForeignLanguage, Integer> vocabularyCount = this.vocabularyManager.getVocabulariesCount();
        Set<Map.Entry<ForeignLanguage, Integer>> entrySet = vocabularyCount.entrySet();
        gridLayout.setRowCount(entrySet.size());

        int row = 0;
        for(Map.Entry<ForeignLanguage, Integer> entry : entrySet)
        {
            setForeignLanguagesCountGridRow(row, gridLayout, entry.getKey(), entry.getValue());
            row++;
        }
    }

    private void setForeignLanguagesCountGridRow(int row, GridLayout grid, ForeignLanguage language, Integer count)
    {
        GridLayout.LayoutParams layoutParamLabel = new GridLayout.LayoutParams(GridLayout.spec(row, GridLayout.LEFT),
                GridLayout.spec(FOREIGN_LANGUAGES_LABEL_COLUMN, GridLayout.LEFT));
        TextView label = new TextView(getActivity());
        label.setText(language.name());
        TextViewCompat.setTextAppearance(label, R.style.TextView_sub_about);

        GridLayout.LayoutParams layoutParamCount = new GridLayout.LayoutParams(GridLayout.spec(row, GridLayout.LEFT),
                GridLayout.spec(FOREIGN_LANGUAGES_COUNT_COLUMN, GridLayout.LEFT));
        layoutParamCount.setMargins(FOREIGN_LANGUAGES_COUNT_LEFT_MARGIN, 0, 0, 0);
        TextView countTextView = new TextView(getActivity());
        countTextView.setText(String.valueOf(count));
        TextViewCompat.setTextAppearance(countTextView, R.style.TextView_sub_about);

        grid.addView(label, layoutParamLabel);
        grid.addView(countTextView, layoutParamCount);
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

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    /**
     * Sets the current settings and sends it to the main activity fragmentRef.
     */
    private void setFragmentActivityResult()
    {
        Intent data = new Intent();

        data.putExtra(EXTRA_SETTINGS, this.settings);
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();

        LogsManager.log(CLASS_NAME, "setFragmentAcivityResult", "Current settings: " + this.settings);
    }

    @Override
    public void setActivityResultOnBackEvent()
    {
        setFragmentActivityResult();
    }

}
