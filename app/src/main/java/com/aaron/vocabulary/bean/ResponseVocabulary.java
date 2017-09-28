package com.aaron.vocabulary.bean;

import java.util.ArrayList;
import java.util.EnumMap;

/**
 * Bean that represents the http response from Vocabulary request.
 */
public class ResponseVocabulary
{
    private int statusCode;
    private String text;
    private String body;
    private int recentlyAddedCount;
    private EnumMap<Vocabulary.ForeignLanguage, ArrayList<Vocabulary>> vocabularyMap;

    public ResponseVocabulary()
    {
    }

    public ResponseVocabulary(int statusCode)
    {
        this.statusCode = statusCode;
    }

    public int getStatusCode()
    {
        return this.statusCode;
    }

    public void setStatusCode(int statusCode)
    {
        this.statusCode = statusCode;
    }

    public String getText()
    {
        return this.text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public String getBody()
    {
        return this.body;
    }

    public void setBody(String body)
    {
        this.body = body;
    }

    public int getRecentlyAddedCount()
    {
        return this.recentlyAddedCount;
    }

    public void setRecentlyAddedCount(int recentlyAddedCount)
    {
        this.recentlyAddedCount = recentlyAddedCount;
    }

    public EnumMap<Vocabulary.ForeignLanguage, ArrayList<Vocabulary>> getVocabularyMap()
    {
        return this.vocabularyMap;
    }

    public void setVocabularyMap(EnumMap<Vocabulary.ForeignLanguage, ArrayList<Vocabulary>> vocabularyMap)
    {
        this.vocabularyMap = vocabularyMap;
    }

    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof ResponseVocabulary))
        {
            return false;
        }
        else
        {
            ResponseVocabulary that = (ResponseVocabulary) o;

            return statusCode != that.statusCode || recentlyAddedCount != that.recentlyAddedCount ||
                    text != null ? !text.equals(that.text) : that.text != null || body != null ? !body.equals(that.body) : that.body != null || vocabularyMap != null ? vocabularyMap.equals(that.vocabularyMap) : that.vocabularyMap == null;
        }
    }

    @Override
    public int hashCode()
    {
        int result = statusCode;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        result = 31 * result + (body != null ? body.hashCode() : 0);
        result = 31 * result + recentlyAddedCount;
        result = 31 * result + (vocabularyMap != null ? vocabularyMap.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "statusCode: " + statusCode +
                ", text: " + text + ", body: " + body +
                ", recentlyAddedCount: " + recentlyAddedCount +
                ", vocabularyMap: " + vocabularyMap;
    }

}
