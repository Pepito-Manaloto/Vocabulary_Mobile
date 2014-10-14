package com.aaron.vocabulary.bean;

import java.io.Serializable;

/**
 * Java bean for a vocabulary.
 */
public class Vocabulary implements Serializable
{
    private static final long serialVersionUID = 6591616622682725968L;
    public static final ForeignLanguage[] foreignLanguageArray = ForeignLanguage.values();

    /**
     * Enum for the list of available foreign languages.
     */
    public enum ForeignLanguage
    {
        Hokkien, 
        Japanese, 
        Mandarin,
    }

    /**
     * Enum for the json key.
     */
    public enum JsonKey
    {
        english_word, 
        foreign_word, 
    }

    private final String englishWord;
    private final String foreignWord;
    private final ForeignLanguage foreignLanguage;
    
    /**
     * Constructor with three arguments.
     */
    public Vocabulary(final String englishWord, final String foreignWord, final ForeignLanguage foreignLanguage)
    {
        this.englishWord = englishWord;
        this.foreignWord = foreignWord;
        this.foreignLanguage = foreignLanguage;
    }

    public String getEnglishWord()
    {
        return this.englishWord;
    }

    public String getForeignWord()
    {
        return this.foreignWord;
    }

    public ForeignLanguage getForeignLanguage()
    {
        return this.foreignLanguage;
    }

    /**
     * Checks all attribute for equality.
     * @param o Vocabulary to compare
     * @return true if equals, else false
     */
    @Override
    public boolean equals(Object o)
    {
        if(!(o instanceof Vocabulary)) // object being compared is not Vocabulary
        {
            return false;
        }
        else
        {
            Vocabulary that = (Vocabulary) o;
            
            return this.englishWord.equals(that.getEnglishWord()) && 
                   this.foreignWord.equals(that.getForeignWord()) &&
                   this.foreignLanguage.equals(that.getForeignLanguage());
        }
    }

    /**
     * Returns a unique hash code of the Vocabulary object.
     * @return int
     */
    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 47 * hash + this.englishWord.hashCode();
        hash = 47 * hash + this.foreignWord.hashCode();
        hash = 47 * hash + this.foreignLanguage.hashCode();

        return hash;
    }
    
    /**
     * Returns the content of the Vocabulary object in a formatted String.
     * @return String
     */
    @Override
    public String toString()
    {
        return "English: " + this.englishWord + " " + this.foreignLanguage.name() + ": " + this.foreignWord;
    }
}
