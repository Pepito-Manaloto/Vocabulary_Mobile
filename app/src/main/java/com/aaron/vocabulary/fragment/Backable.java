package com.aaron.vocabulary.fragment;

/**
 * Created by Aaron on 22/02/2018.
 * Defines an Activity or Fragment that is capable of handling back button event.
 */
public interface Backable
{
    /**
     * Finishes the current activity and navigate to the next one.
     */
    void setActivityResultOnBackEvent();
}
