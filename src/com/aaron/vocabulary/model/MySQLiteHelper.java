package com.aaron.vocabulary.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Model for creating and updating the database.
 */
public class MySQLiteHelper extends SQLiteOpenHelper
{
    public static final String TAG = "MySQLiteHelper";
    private static final String DATABASE_NAME = "aaron_vocabulary.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_VOCABULARY = "vocabulary";
    public static final String[] COLUMN_COUNT = new String[]{"COUNT(*)",};

    /**
     * The database's column names.
     */
    public enum Column
    {
        id,
        english_word,
        foreign_word,
        foreign_language,
        date_in,
    }

    private static final String CREATE_TABLE_VOCABULARY = "CREATE TABLE " + TABLE_VOCABULARY +
                                               "(" + 
                                               Column.id.name() + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                               Column.english_word.name() + " TEXT NOT NULL, " +
                                               Column.foreign_word.name() + " TEXT NOT NULL, " +
                                               Column.foreign_language.name() + " TEXT NOT NULL, " +
                                               Column.date_in.name() + " TEXT NOT NULL, " +
                                               "UNIQUE(" + Column.english_word.name() + ", " + Column.foreign_word.name() + ")" +
                                               ");";

    /**
     * Default constructor.
     */     
    public MySQLiteHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called if the database name given in the constructor does not exists.
     */
    @Override
    public void onCreate(SQLiteDatabase database)
    {
        Log.d(LogsManager.TAG, "MySQLiteHelper: onCreate. query=" + CREATE_TABLE_VOCABULARY);

        database.execSQL(CREATE_TABLE_VOCABULARY);
    }

    /**
     * Called if the version given in the constructor is higher than the existing database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
    {
        /**
         * TODO (1) store db contents in temp --- NOT YET
         *      (2) drop db --- IMPLEMENTED
         *      (3) create new db --- IMPLEMENTED 
         *      (4) insert temp data in new db --- NOT YET
         */ 
        database.execSQL("DROP IF TABLE EXISTS " + TABLE_VOCABULARY);
        this.onCreate(database);
    }

}
