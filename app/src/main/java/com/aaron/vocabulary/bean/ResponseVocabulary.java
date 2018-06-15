package com.aaron.vocabulary.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.EnumMap;

/**
 * Bean that represents the http response from Vocabulary request.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseVocabulary
{
    // Each Vocabulary's foreignLanguage is not set upon Web API request, and is determined by Map's key upon database insert.
    private EnumMap<ForeignLanguage, ArrayList<Vocabulary>> vocabularyMap;
    private int recentlyAddedCount;

    public ResponseVocabulary()
    {
    }

    public int getRecentlyAddedCount()
    {
        return this.recentlyAddedCount;
    }

    @JsonProperty("recently_added_count")
    public void setRecentlyAddedCount(int recentlyAddedCount)
    {
        this.recentlyAddedCount = recentlyAddedCount;
    }

    public EnumMap<ForeignLanguage, ArrayList<Vocabulary>> getVocabularyMap()
    {
        return this.vocabularyMap;
    }

    @JsonProperty("languages")
    public void setVocabularyMap(EnumMap<ForeignLanguage, ArrayList<Vocabulary>> vocabularyMap)
    {
        this.vocabularyMap = vocabularyMap;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(o == null || getClass() != o.getClass())
        {
            return false;
        }

        ResponseVocabulary that = (ResponseVocabulary) o;

        return recentlyAddedCount == that.recentlyAddedCount && (vocabularyMap != null ? vocabularyMap.equals(that.vocabularyMap) : that.vocabularyMap == null);
    }

    @Override
    public int hashCode()
    {
        int result = vocabularyMap != null ? vocabularyMap.hashCode() : 0;
        result = 31 * result + recentlyAddedCount;
        return result;
    }

    @Override
    public String toString()
    {
        return "ResponseVocabulary{" +
                "vocabularyMap=" + vocabularyMap +
                ", recentlyAddedCount=" + recentlyAddedCount +
                '}';
    }
}
