package com.aaron.vocabulary.activity;

import com.aaron.vocabulary.bean.Settings;
import com.aaron.vocabulary.fragment.SettingsFragment;
import android.app.Fragment;

/**
 * SettingsFragment activity.
 */
public class SettingsActivity extends SingleFragmentActivity
{
    /**
     * Returns a settings fragment.
     * 
     * @return a fragment to be added.
     */
    @Override
    protected Fragment createFragment()
    {
        Settings settings = (Settings) this.getIntent().getSerializableExtra(SettingsFragment.EXTRA_SETTINGS);

        return SettingsFragment.newInstance(settings);
    }

}
