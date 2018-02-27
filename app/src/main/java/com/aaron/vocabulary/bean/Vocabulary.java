package com.aaron.vocabulary.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Java bean for a vocabulary.
 */
public class Vocabulary implements Parcelable
{
    private String englishWord;
    private String foreignWord;
    private ForeignLanguage foreignLanguage;

    public Vocabulary()
    {
    }

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

    @JsonProperty("english_word")
    public void setEnglishWord(String englishWord)
    {
        this.englishWord = englishWord;
    }

    public String getForeignWord()
    {
        return this.foreignWord;
    }

    @JsonProperty("foreign_word")
    public void setForeignWord(String foreignWord)
    {
        this.foreignWord = foreignWord;
    }

    public ForeignLanguage getForeignLanguage()
    {
        return this.foreignLanguage;
    }

    public void setForeignLanguage(ForeignLanguage foreignLanguage)
    {
        this.foreignLanguage = foreignLanguage;
    }

    /**
     * Checks all attribute for equality.
     *
     * @param o
     *            Vocabulary to compare
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
     *
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
     *
     * @return String
     */
    @Override
    public String toString()
    {
        return "English: " + this.englishWord + " " + this.foreignLanguage.name() + ": " + this.foreignWord;
    }

    /**
     * Constructor that will be called in creating the parcel. Note: Reading the parcel should be the same order as writing the parcel!
     */
    private Vocabulary(Parcel in)
    {
        this.englishWord = in.readString();
        this.foreignWord = in.readString();
        this.foreignLanguage = ForeignLanguage.values()[in.readInt()];
    }

    /**
     * Flatten this object in to a Parcel.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.englishWord);
        dest.writeString(this.foreignWord);
        dest.writeInt(this.foreignLanguage != null ? this.foreignLanguage.ordinal() : 0);
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable instance's marshaled representation.
     */
    @Override
    public int describeContents()
    {
        return 0;
    }

    /**
     * Generates instances of your Parcelable class from a Parcel.
     */
    public static final Creator<Vocabulary> CREATOR = new Creator<Vocabulary>()
    {
        @Override
        public Vocabulary createFromParcel(Parcel in)
        {
            return new Vocabulary(in);
        }

        @Override
        public Vocabulary[] newArray(int size)
        {
            return new Vocabulary[size];
        }
    };
}
