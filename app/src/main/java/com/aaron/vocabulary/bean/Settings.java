package com.aaron.vocabulary.bean;

import android.graphics.Typeface;
import android.os.Parcel;
import android.os.Parcelable;

import com.aaron.vocabulary.bean.Vocabulary.ForeignLanguage;

/**
 * Java bean for the application settings.
 */
public class Settings implements Parcelable
{
    /**
     * Enum for the list of default font name.
     */
    public enum FontName
    {
        Default, Serif, Sans_Serif, Monospace
    }

    /**
     * Enum for the list of default font style.
     */
    public enum FontStyle
    {
        Normal, Bold, Italic, Bold_Italic,
    }

    /**
     * Enum for the list of default update interval.
     */
    public enum UpdateInterval
    {
        Never,
    }

    private ForeignLanguage foreignLanguage;
    private FontName fontName;
    private FontStyle fontStyle;
    private int fontSize;
    private UpdateInterval updateInterval;
    private String serverURL;

    /**
     * Default constructor, initializes with default values.
     */
    public Settings()
    {
        this.foreignLanguage = ForeignLanguage.Hokkien;
        this.fontName = FontName.Default;
        this.fontStyle = FontStyle.Normal;
        this.fontSize = 14;
        this.updateInterval = UpdateInterval.Never;
        this.serverURL = "";
    }

    /**
     * Getter for ForeignLanguage.
     *
     * @return ForeignLanguage
     */
    public ForeignLanguage getForeignLanguage()
    {
        return this.foreignLanguage;
    }

    /**
     * Getter for fontSize.
     *
     * @return int
     */
    public int getFontSize()
    {
        return this.fontSize;
    }

    /**
     * Getter for UpdateInterval.
     *
     * @return UpdateInterval
     */
    public UpdateInterval getUpdateInterval()
    {
        return this.updateInterval;
    }

    /**
     * Getter for ForeignLanguage's index.
     *
     * @return ForeignLanguage index
     */
    public int getForeignLanguageIndex()
    {
        return this.foreignLanguage.ordinal();
    }

    /**
     * Getter for FontName's index.
     *
     * @return FontName index
     */
    public int getFontNameIndex()
    {
        return this.fontName.ordinal();
    }

    /**
     * Getter for FontStyle's index.
     *
     * @return FontStyle index
     */
    public int getFontStyleIndex()
    {
        return this.fontStyle.ordinal();
    }

    /**
     * Getter for fontSize's index.
     *
     * @return FontSize index
     */
    public int getFontSizeIndex()
    {
        switch(this.fontSize)
        {
            case 14:
                return 0;
            case 15:
                return 1;
            case 16:
                return 2;
            case 17:
                return 3;
            case 18:
                return 4;
            case 19:
                return 5;
            case 20:
                return 6;
            default:
                throw new AssertionError("Unknown Font Size");
        }
    }

    /**
     * Getter for UpdateInterval's index.
     *
     * @return UpdateInterval index
     */
    public int getUpdateIntervalIndex()
    {
        return this.updateInterval.ordinal();
    }

    /**
     * Getter for Server URL.
     *
     * @return Server URL
     */
    public String getServerURL()
    {
        return this.serverURL;
    }

    /**
     * Returns the content of the Settings object in a formatted String.
     *
     * @return String
     */
    @Override
    public String toString()
    {
        return "Foreign language: " + this.foreignLanguage +
                " Font name: " + this.fontName +
                " Font style: " + this.fontStyle +
                " Font size: " + this.fontSize +
                " Update interval: " + this.updateInterval +
                " Server URL: " + this.serverURL;
    }

    /**
     * Returns the typeface of this vocabulary.
     *
     * @return Typeface
     */
    public Typeface getTypeface()
    {
        Typeface family;

        switch(this.fontName)
        {
            case Serif:
                family = Typeface.SERIF;
                break;
            case Sans_Serif:
                family = Typeface.SANS_SERIF;
                break;
            case Monospace:
                family = Typeface.MONOSPACE;
                break;

            default:
                family = Typeface.DEFAULT;
        }

        return Typeface.create(family, this.getFontStyleIndex());
    }

    /**
     * Sets the foreignLanguage new value.
     *
     * @param foreignLanguage
     *            the foreign language to set
     * @return the settings object being updated
     */
    public Settings setForeignLanguage(final ForeignLanguage foreignLanguage)
    {
        this.foreignLanguage = foreignLanguage;
        return this;
    }

    /**
     * Sets the fontName new value.
     *
     * @param fontName
     *            the font name to set
     * @return the settings object being updated
     */
    public Settings setFontName(final FontName fontName)
    {
        this.fontName = fontName;
        return this;
    }

    /**
     * Sets the fontStyle new value.
     *
     * @param fontStyle
     *            the font style to set
     * @return the settings object being updated
     */
    public Settings setFontStyle(final FontStyle fontStyle)
    {
        this.fontStyle = fontStyle;
        return this;
    }

    /**
     * Sets the fontSize new value.
     *
     * @param fontSize
     *            the font size to set
     * @return the settings object being updated
     */
    public Settings setFontSize(final int fontSize)
    {
        this.fontSize = fontSize;
        return this;
    }

    /**
     * Sets the updateInterval new value.
     *
     * @param updateInterval
     *            the update interval
     * @return the settings object being updated
     */
    public Settings setUpdateInterval(final UpdateInterval updateInterval)
    {
        this.updateInterval = updateInterval;
        return this;
    }

    /**
     * Sets the serverURL new value.
     *
     * @param serverURL
     *            the server url
     * @return the settings object being updated
     */
    public Settings setServerURL(final String serverURL)
    {
        this.serverURL = serverURL;
        return this;
    }

    /**
     * Constructor that will be called in creating the parcel. Note: Reading the parcel should be the same order as writing the parcel!
     */
    private Settings(Parcel in)
    {
        this.foreignLanguage = ForeignLanguage.values()[in.readInt()];
        this.fontName = FontName.values()[in.readInt()];
        this.fontStyle = FontStyle.values()[in.readInt()];
        this.fontSize = in.readInt();
        this.updateInterval = UpdateInterval.values()[in.readInt()];
        this.serverURL = in.readString();
    }

    /**
     * Flatten this object in to a Parcel.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(this.foreignLanguage != null ? this.foreignLanguage.ordinal() : 0);
        dest.writeInt(this.fontName != null ? this.fontName.ordinal() : 0);
        dest.writeInt(this.fontStyle != null ? this.fontStyle.ordinal() : 0);
        dest.writeInt(this.fontSize);
        dest.writeInt(this.updateInterval != null ? this.updateInterval.ordinal() : 0);
        dest.writeString(this.serverURL);
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
    public static final Creator<Settings> CREATOR = new Creator<Settings>()
    {
        @Override
        public Settings createFromParcel(Parcel in)
        {
            return new Settings(in);
        }

        @Override
        public Settings[] newArray(int size)
        {
            return new Settings[size];
        }
    };
}
