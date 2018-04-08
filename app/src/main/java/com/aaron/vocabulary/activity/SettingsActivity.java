package com.aaron.vocabulary.activity;

import com.aaron.vocabulary.bean.Settings;
import com.aaron.vocabulary.fragment.SettingsFragment;
import android.app.Fragment;

import static com.aaron.vocabulary.bean.DataKey.EXTRA_SETTINGS;

/**
 * SettingsFragment activity.
 */
public class SettingsActivity extends SingleFragmentActivity
{
    private SettingsFragment fragment;

    /**
     * Returns a settings fragment.
     * 
     * @return a fragment to be added.
     */
    @Override
    protected Fragment createFragment()
    {
        Settings settings = this.getIntent().getParcelableExtra(EXTRA_SETTINGS.toString());
        this.fragment = SettingsFragment.newInstance(this.fragment, settings);
        return this.fragment;
    }
}
