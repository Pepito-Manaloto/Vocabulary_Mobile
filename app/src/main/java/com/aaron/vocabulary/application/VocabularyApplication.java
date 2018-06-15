package com.aaron.vocabulary.application;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.jakewharton.threetenabp.AndroidThreeTen;
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

        // Access in Google Chrome url via -> chrome://inspect
        Stetho.initializeWithDefaults(this);

        // Normal app init code...
        AndroidThreeTen.init(this);
    }
}
