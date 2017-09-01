package com.aaron.vocabulary.fragment;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.bean.Settings;
import com.aaron.vocabulary.bean.Settings.FontName;
import com.aaron.vocabulary.bean.Settings.FontStyle;
import com.aaron.vocabulary.bean.Settings.UpdateInterval;
import com.aaron.vocabulary.bean.Vocabulary.ForeignLanguage;
import com.aaron.vocabulary.model.LogsManager;

import static com.aaron.vocabulary.bean.Vocabulary.*;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.apache.commons.lang3.StringUtils;

/**
 * The application settings fragment.
 */
public class SettingsFragment extends Fragment
{
    public static final String CLASS_NAME = SettingsFragment.class.getSimpleName();
    public static final String EXTRA_SETTINGS = "com.aaron.vocabulary.fragment.settings";
    private Settings settings;

    private ArrayAdapter<ForeignLanguage> languageAdapter;

    private Spinner foreignLanguageSpinner;
    private Spinner fontNameSpinner;
    private Spinner fontStyleSpinner;
    private Spinner fontSizeSpinner;
    private Spinner updateIntervalSpinner;

    private EditText serverURLEditText;

    /**
     * Returns a new SettingsFragment with the given settings as arguments.
     */
    public static SettingsFragment newInstance(final Settings settings)
    {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_SETTINGS, settings);

        SettingsFragment fragment = new SettingsFragment();
        fragment.setArguments(args);

        Log.d(LogsManager.TAG, CLASS_NAME + ": newInstance. settings=" + settings);

        return fragment;
    }

    /**
     * Initializes non-fragment user interface.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.settings = (Settings) getArguments().getSerializable(SettingsFragment.EXTRA_SETTINGS);

        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.menu_settings);

        ActionBar actionBar = getActivity().getActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        this.languageAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, FOREIGN_LANGUAGE_ARRAY);
        this.languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Log.d(LogsManager.TAG, CLASS_NAME + ": onCreate. settings=" + this.settings);
        LogsManager.addToLogs(CLASS_NAME + ": onCreate. settings=" + this.settings);
    }

    /**
     * Initializes the fragment's user interface.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_settings, parent, false);

        this.foreignLanguageSpinner = view.findViewById(R.id.spinner_foreign_language);
        this.foreignLanguageSpinner.setAdapter(this.languageAdapter);

        this.fontNameSpinner = view.findViewById(R.id.spinner_font_name);
        this.fontStyleSpinner = view.findViewById(R.id.spinner_font_style);
        this.fontSizeSpinner = view.findViewById(R.id.spinner_font_size);
        this.updateIntervalSpinner = view.findViewById(R.id.spinner_update_interval);
        this.serverURLEditText = view.findViewById(R.id.edittext_server_url);

        this.foreignLanguageSpinner.setSelection(this.settings.getForeignLanguageIndex());
        this.fontNameSpinner.setSelection(this.settings.getFontNameIndex());
        this.fontStyleSpinner.setSelection(this.settings.getFontStyleIndex());
        this.fontSizeSpinner.setSelection(this.settings.getFontSizeIndex());
        this.updateIntervalSpinner.setSelection(this.settings.getUpdateIntervalIndex());

        String serverUrl = this.settings.getServerURL();

        if(StringUtils.isBlank(serverUrl))
        {
            serverUrl = getActivity().getString(R.string.url_address_default);
        }

        this.serverURLEditText.setText(serverUrl);

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new BackButtonListener());
        this.serverURLEditText.setOnKeyListener(new BackButtonListener());

        Log.d(LogsManager.TAG, CLASS_NAME + ": onCreateView");

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
     * Sets the new settings and sends it to the main activity fragment.
     */
    private void setFragmentActivityResult()
    {
        Intent data = new Intent();

        ForeignLanguage foreignLanguage = ForeignLanguage.valueOf(this.foreignLanguageSpinner.getSelectedItem().toString());
        FontName fontName = FontName.valueOf(this.fontNameSpinner.getSelectedItem().toString());
        FontStyle fontStyle = FontStyle.valueOf(this.fontStyleSpinner.getSelectedItem().toString());
        int fontSize = Integer.parseInt(this.fontSizeSpinner.getSelectedItem().toString());
        UpdateInterval updateInterval = UpdateInterval.valueOf(this.updateIntervalSpinner.getSelectedItem().toString());
        String serverURL = this.serverURLEditText.getText().toString();

        this.settings.setForeignLanguage(foreignLanguage).setFontName(fontName).setFontStyle(fontStyle).setFontSize(fontSize).setUpdateInterval(updateInterval).setServerURL(serverURL);

        data.putExtra(EXTRA_SETTINGS, this.settings);
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();

        Log.d(LogsManager.TAG, CLASS_NAME + ": setFragmentActivityResult. New settings -> " + this.settings);
        LogsManager.addToLogs(CLASS_NAME + ": setFragmentActivityResult. New settings -> " + this.settings);
    }

    private class BackButtonListener implements View.OnKeyListener
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
    }
}