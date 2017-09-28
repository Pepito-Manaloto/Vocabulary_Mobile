package com.aaron.vocabulary.activity;

import com.aaron.vocabulary.bean.Settings;
import com.aaron.vocabulary.fragment.SettingsFragment;
import android.app.Fragment;

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
        Settings settings = this.getIntent().getParcelableExtra(SettingsFragment.EXTRA_SETTINGS);
        this.fragment = SettingsFragment.newInstance(this.fragment, settings);
        return this.fragment;
    }

}
