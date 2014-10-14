package com.aaron.vocabulary.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database model in creating the database(if file name in constructor does not exist) and updating the database(if stored version is lower than the version in the constructor).
 * http://www.vogella.com/tutorials/AndroidSQLite/article.html
 */
public class MySQLiteHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "vocabulary.db";
    private static final int DATABASE_VERSION = 1;

    public MySQLiteHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database)
    {
        // TODO create db

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
    {
        /**
         * TODO (1) store db contents in temp
         *      (2) drop db 
         *      (3) create new db 
         *      (4) insert temp data in new db
         */ 
    }

}
