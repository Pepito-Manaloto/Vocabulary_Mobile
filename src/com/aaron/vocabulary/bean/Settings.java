package com.aaron.vocabulary.bean;

import java.io.Serializable;

import com.aaron.vocabulary.bean.Vocabulary.ForeignLanguage;

/**
 * Java bean for the application settings.
 */
public class Settings implements Serializable
{
    private static final long serialVersionUID = 4213847540393030207L;

    /**
     * Enum for the list of default font name.
     */
    public enum FontName
    {
        Normal,
        Serif,
        Sans_Serif,
        Monospace,
    }

    /**
     * Enum for the list of default font style.
     */
    public enum FontStyle
    {
        Plain,
        Bold,
        Italic,
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

    /**
     * Default constructor, initializes with default values.
     */
    public Settings()
    {
        this.foreignLanguage = ForeignLanguage.Hokkien;
        this.fontName = FontName.Normal;
        this.fontStyle = FontStyle.Plain;
        this.fontSize = 14;
        this.updateInterval = UpdateInterval.Never;
    }

    /**
     * Getter for ForeignLanguage.
     * @return ForeignLanguage
     */
    public ForeignLanguage getForeignLanguage()
    {
        return this.foreignLanguage;
    }

    /**
     * Getter for FontName.
     * @return FontName
     */
    public FontName getFontName()
    {
        return this.fontName;
    }

    /**
     * Getter for FontStyle.
     * @return FontStyle
     */
    public FontStyle getFontStyle()
    {
        return this.fontStyle;
    }

    /**
     * Getter for fontSize.
     * @return int
     */
    public int getFontSize()
    {
        return this.fontSize;
    }

    /**
     * Getter for UpdateInterval.
     * @return UpdateInterval
     */
    public UpdateInterval getUpdateInterval()
    {
        return this.updateInterval;
    }

    /**
     * Getter for ForeignLanguage's index.
     * @return ForeignLanguage index
     */
    public int getForeignLanguageIndex()
    {
        return this.foreignLanguage.ordinal();
    }

    /**
     * Getter for FontName's index.
     * @return FontName index
     */
    public int getFontNameIndex()
    {
        return this.fontName.ordinal();
    }

    /**
     * Getter for FontStyle's index.
     * @return FontStyle index
     */
    public int getFontStyleIndex()
    {
        return this.fontStyle.ordinal();
    }

    /**
     * Getter for fontSize's index.
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
                throw new AssertionError();
        }
    }

    /**
     * Getter for UpdateInterval's index.
     * @return UpdateInterval index
     */
    public int getUpdateIntervalIndex()
    {
        return this.updateInterval.ordinal();
    }
}