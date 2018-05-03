package com.aaron.vocabulary.fragment;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.bean.Settings;
import com.aaron.vocabulary.bean.Settings.FontName;
import com.aaron.vocabulary.bean.Settings.FontStyle;
import com.aaron.vocabulary.bean.Settings.UpdateInterval;
import com.aaron.vocabulary.bean.ForeignLanguage;
import com.aaron.vocabulary.fragment.listener.BackButtonListener;
import com.aaron.vocabulary.model.LogsManager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import org.apache.commons.lang3.StringUtils;

import static com.aaron.vocabulary.bean.DataKey.EXTRA_SETTINGS;

/**
 * The application settings fragment.
 */
public class SettingsFragment extends Fragment implements Backable
{
    public static final String CLASS_NAME = SettingsFragment.class.getSimpleName();
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
    public static SettingsFragment newInstance(SettingsFragment fragment, final Settings settings)
    {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_SETTINGS.toString(), settings);

        SettingsFragment settingsFragment;
        if(fragment != null)
        {
            settingsFragment = fragment;
        }
        else
        {
            settingsFragment = new SettingsFragment();
        }

        settingsFragment.setArguments(args);

        Log.d(LogsManager.TAG, CLASS_NAME + ": newInstance. settings=" + settings);

        return settingsFragment;
    }

    /**
     * Initializes non-fragment user interface.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        settings = getArguments().getParcelable(EXTRA_SETTINGS.toString());

        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.menu_settings);

        ActionBar actionBar = getActivity().getActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        languageAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, ForeignLanguage.values());
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        LogsManager.log(CLASS_NAME, "onCreate", "settings = " + settings);
    }

    /**
     * Initializes the fragment's user interface.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        BackButtonListener backButtonListener = new BackButtonListener(this);

        View view = inflater.inflate(R.layout.fragment_settings, parent, false);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(backButtonListener);

        foreignLanguageSpinner = view.findViewById(R.id.spinner_foreign_language);
        foreignLanguageSpinner.setAdapter(languageAdapter);
        foreignLanguageSpinner.setSelection(settings.getForeignLanguageIndex());

        fontNameSpinner = view.findViewById(R.id.spinner_font_name);
        fontNameSpinner.setSelection(settings.getFontNameIndex());

        fontStyleSpinner = view.findViewById(R.id.spinner_font_style);
        fontStyleSpinner.setSelection(settings.getFontStyleIndex());

        fontSizeSpinner = view.findViewById(R.id.spinner_font_size);
        fontSizeSpinner.setSelection(settings.getFontSizeIndex());

        updateIntervalSpinner = view.findViewById(R.id.spinner_update_interval);
        updateIntervalSpinner.setSelection(settings.getUpdateIntervalIndex());

        serverURLEditText = view.findViewById(R.id.edittext_server_url);
        String serverUrl = settings.getServerURL();
        if(StringUtils.isBlank(serverUrl))
        {
            serverUrl = getActivity().getString(R.string.url_address_default);
        }
        serverURLEditText.setText(serverUrl);

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
                setFragmentActivityResult();
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

        updateSettingsBasedOnSelection();

        data.putExtra(EXTRA_SETTINGS.toString(), settings);
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();

        LogsManager.log(CLASS_NAME, "setFragmentActivityResult", "New settings = " + settings);
    }

    private void updateSettingsBasedOnSelection()
    {
        ForeignLanguage foreignLanguage = ForeignLanguage.valueOf(foreignLanguageSpinner.getSelectedItem().toString());
        FontName fontName = FontName.valueOf(fontNameSpinner.getSelectedItem().toString());
        FontStyle fontStyle = FontStyle.valueOf(fontStyleSpinner.getSelectedItem().toString());
        int fontSize = Integer.parseInt(fontSizeSpinner.getSelectedItem().toString());
        UpdateInterval updateInterval = UpdateInterval.valueOf(updateIntervalSpinner.getSelectedItem().toString());
        String serverURL = serverURLEditText.getText().toString();

        settings.setForeignLanguage(foreignLanguage)
                .setFontName(fontName)
                .setFontStyle(fontStyle)
                .setFontSize(fontSize)
                .setUpdateInterval(updateInterval)
                .setServerURL(serverURL);
    }

    @Override
    public void setActivityResultOnBackEvent()
    {
        setFragmentActivityResult();
    }
}