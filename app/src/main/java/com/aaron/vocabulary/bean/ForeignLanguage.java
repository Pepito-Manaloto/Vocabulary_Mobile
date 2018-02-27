package com.aaron.vocabulary.bean;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum for the list of available foreign languages.
 */
public enum ForeignLanguage
{
    Hokkien, Japanese, Mandarin;

    @JsonValue
    public String getLanguage()
    {
        return toString();
    }
}