package com.aaron.vocabulary.fragment.listener;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.aaron.vocabulary.fragment.LogsFragment;
import com.aaron.vocabulary.model.LogsManager;

import java.lang.ref.WeakReference;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class LogsSearchListener implements TextWatcher
{
    private WeakReference<LogsFragment> fragmentRef;
    private LogsManager logsManager;

    public LogsSearchListener(LogsFragment fragment, LogsManager logsManager)
    {
        this.fragmentRef = new WeakReference<>(fragment);
        this.logsManager = logsManager;
    }

    /**
     * Handles search on text update.
     */
    @Override
    public void afterTextChanged(Editable textField)
    {
        String searched = textField.toString();

        LogsFragment fragment = this.fragmentRef.get();

        if(isBlank(searched))
        {
            fragment.setTextAreaText(this.logsManager.getLogs());
        }
        else
        {
            fragment.setTextAreaText(this.logsManager.getLogs(searched));
        }

        Log.d(LogsManager.TAG, LogsFragment.CLASS_NAME + ": onCreateOptionsMenu(afterTextChanged). searched=" + searched);
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
    {
        // No action
    }

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
    {
        // No action
    }
}