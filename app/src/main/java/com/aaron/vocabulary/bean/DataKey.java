package com.aaron.vocabulary.bean;

/**
 * Holds all the keys used in Bundle and Intent-extra.
 */
public enum DataKey
{
    EXTRA_SETTINGS("com.aaron.vocabulary.fragment.settings"),
    EXTRA_VOCABULARY_LIST("com.aaron.vocabulary.fragment.vocabulary_list.list");

    private String value;

    DataKey(String value)
    {
        this.value = value;
    }

    @Override
    public String toString()
    {
        return this.value;
    }
}