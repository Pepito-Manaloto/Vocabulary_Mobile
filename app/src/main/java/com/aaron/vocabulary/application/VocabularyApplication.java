package com.aaron.vocabulary.application;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Aaron on 13/09/2017.
 */

public class VocabularyApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        if(LeakCanary.isInAnalyzerProcess(this))
        {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        // Normal app init code...
    }
}
