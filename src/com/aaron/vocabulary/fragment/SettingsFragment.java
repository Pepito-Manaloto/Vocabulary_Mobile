package com.aaron.vocabulary.fragment;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.bean.Settings;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

/**
 * The application settings fragment.
 */
public class SettingsFragment extends Fragment
{
    public static final String EXTRA_SETTINGS = "com.aaron.vocabulary.fragment.settings";
    private Settings settings;

    /**
     * Initializes non-fragment user interface.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        if(savedInstanceState != null)
        {
            this.settings = (Settings) savedInstanceState.getSerializable(SettingsFragment.EXTRA_SETTINGS);
        }
        else
        {
            this.settings = new Settings();
        }

        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.menu_settings);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Initializes the fragment's user interface.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_settings, parent, false);
        
        Spinner foreignLanguageSpinner = (Spinner) view.findViewById(R.id.spinner_foreign_language);
        Spinner fontNameSpinner = (Spinner) view.findViewById(R.id.spinner_font_name);
        Spinner fontStyleSpinner = (Spinner) view.findViewById(R.id.spinner_font_style);
        Spinner fontSizeSpinner = (Spinner) view.findViewById(R.id.spinner_font_size);
        Spinner updateIntervalSpinner = (Spinner) view.findViewById(R.id.spinner_update_interval);
        
        foreignLanguageSpinner.setSelection(this.settings.getForeignLanguageIndex());
        fontNameSpinner.setSelection(this.settings.getFontNameIndex());
        fontStyleSpinner.setSelection(this.settings.getFontStyleIndex());
        fontSizeSpinner.setSelection(this.settings.getFontSize());
        updateIntervalSpinner.setSelection(this.settings.getUpdateIntervalIndex());

        return view;
    }

    @Override
    public void onPause()
    {
        super.onPause();

        Intent data = new Intent();

        
       // data.putExtra(EXTRA_SETTINGS, );
        getActivity().setResult(Activity.RESULT_OK, data);
    }
}