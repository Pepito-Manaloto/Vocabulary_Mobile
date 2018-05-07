package com.aaron.vocabulary;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.app.Application;
import android.content.Context;

import com.aaron.vocabulary.application.VocabularyApplication;

/**
 * Created by Aaron on 02/01/2018.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public abstract class RobolectricTest
{
    protected Context getContext()
    {
        return RuntimeEnvironment.application.getApplicationContext();
    }
}
