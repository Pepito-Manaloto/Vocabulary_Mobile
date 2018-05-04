package com.aaron.vocabulary.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.aaron.vocabulary.bean.ForeignLanguage;
import com.aaron.vocabulary.bean.Vocabulary;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

import static com.aaron.vocabulary.model.MySQLiteHelper.COLUMN_COUNT;
import static com.aaron.vocabulary.model.MySQLiteHelper.Column;
import static com.aaron.vocabulary.model.MySQLiteHelper.TABLE_VOCABULARY;

/**
 * Handles the web call to retrieve vocabularies in JSON object representation. Handles the data storage of vocabularies.
 */
public class VocabularyManager
{
    private static final String CLASS_NAME = VocabularyManager.class.getSimpleName();

    public static final String DATE_FORMAT_DATABASE = "MMMM d, yyyy hh:mm:ss a";
    public static final String DATE_FORMAT_WEB = "yyyy-MM-dd HH:mm:ss";

    private MySQLiteHelper dbHelper;
    private LocalDateTime now;

    /**
     * Default constructor
     *
     * @param context  the current context
     */
    public VocabularyManager(Context context)
    {
        this.dbHelper = new MySQLiteHelper(context);
        this.now = LocalDateTime.now();

    }

    /**
     * Saves the given lists of vocabularies to the local database.
     *
     * @param vocabularyMap the vocabularies to be stored
     * @return true on success, else false
     */
    public boolean saveRecipesToDisk(final EnumMap<ForeignLanguage, ArrayList<Vocabulary>> vocabularyMap)
    {
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();

        try
        {
            db.beginTransaction();
            // Delete vocabularies. To ensure no duplicates, if existing vocabularies are modified in the server.
            db.delete(TABLE_VOCABULARY, null, null);

            for(Map.Entry<ForeignLanguage, ArrayList<Vocabulary>> entry : vocabularyMap.entrySet())
            {
                ForeignLanguage language = entry.getKey();
                ArrayList<Vocabulary> vocabularyList = entry.getValue();

                insertVocabularyListPerCategoryToDatabase(language, vocabularyList, db);
            }

            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
            db.close();
            this.dbHelper.close();
        }

        LogsManager.log(CLASS_NAME, "saveToDisk", "");

        return true;
    }

    private void insertVocabularyListPerCategoryToDatabase(ForeignLanguage language, ArrayList<Vocabulary> vocabularyList, SQLiteDatabase db)
    {
        for(Vocabulary vocabulary : vocabularyList)
        {
            insertVocabularyToDatabase(language, vocabulary, db);
        }
    }

    private void insertVocabularyToDatabase(ForeignLanguage language, Vocabulary vocabulary, SQLiteDatabase db)
    {
        ContentValues values = new ContentValues();
        values.put(Column.english_word.name(), vocabulary.getEnglishWord());
        values.put(Column.foreign_word.name(), vocabulary.getForeignWord());
        values.put(Column.foreign_language.name(), language.getLanguage());
        values.put(Column.date_in.name(), now.format(DateTimeFormatter.ofPattern(DATE_FORMAT_DATABASE)));

        db.insert(TABLE_VOCABULARY, null, values);
    }

    /**
     * Does the following logic:
     * (1) Retrieves the vocabularies from the local disk.
     * (2) Returns the vocabulary list of the selected language.
     *
     * @return ArrayList<Vocabulary>
     */
    public ArrayList<Vocabulary> getVocabulariesFromDisk(final ForeignLanguage selectedLanguage)
    {
        ArrayList<Vocabulary> list;
        try(SQLiteDatabase db = this.dbHelper.getReadableDatabase())
        {
            String[] columns = new String[] { Column.english_word.name(), Column.foreign_word.name(), Column.foreign_language.name() };
            String whereClause = "foreign_language = ?";
            String[] whereArgs = new String[] { selectedLanguage.name() };

            Cursor cursor = db.query(TABLE_VOCABULARY, columns, whereClause, whereArgs, null, null, null);
            list = new ArrayList<>(cursor.getCount());

            if(cursor.moveToFirst())
            {
                do
                {
                    list.add(this.cursorToVocabulary(cursor));
                } while(cursor.moveToNext());
            }
        }

        LogsManager.log(CLASS_NAME, "getVocabulariesFromDisk", "list_size=" + list.size());

        return list;
    }

    /**
     * Retrieves the vocabulary from the cursor.
     *
     * @param cursor
     *            the cursor resulting from a query
     * @return Vocabulary
     */
    private Vocabulary cursorToVocabulary(final Cursor cursor)
    {
        String englishWord = cursor.getString(0);
        String foreignWord = cursor.getString(1);
        ForeignLanguage foreignLanguage = ForeignLanguage.valueOf(cursor.getString(2));

        return new Vocabulary(englishWord, foreignWord, foreignLanguage);
    }

    /**
     * Gets the current vocabulary count per foreign languages, and returns them as an EnumMap.
     *
     * @return {@code EnumMap<ForeignLanguage, Integer>}
     */
    public EnumMap<ForeignLanguage, Integer> getVocabulariesCount()
    {
        EnumMap<ForeignLanguage, Integer> map = new EnumMap<>(ForeignLanguage.class);

        try(SQLiteDatabase db = this.dbHelper.getReadableDatabase())
        {
            String whereClause = "foreign_language = ?";

            for(ForeignLanguage language : ForeignLanguage.values())
            {
                try(Cursor cursor = db.query(TABLE_VOCABULARY, COLUMN_COUNT, whereClause, new String[] { language.name() }, null, null, null))
                {
                    if(cursor.moveToFirst())
                    {
                        map.put(language, cursor.getInt(0));
                    }
                }
            }
        }
        LogsManager.log(CLASS_NAME, "getVocabulariesCount", "keys=" + map.keySet() + " values_size=" + map.values().size());

        return map;
    }

    /**
     * Gets the latest date_in of the vocabularies.
     *
     * @param format
     *            the date format used in formatting the last_updated date
     * @return String
     */
    public String getLastUpdated(final String format)
    {
        String lastUpdatedDate = "1950-01-01 00:00:00";
        try(SQLiteDatabase db = this.dbHelper.getReadableDatabase())
        {
            String[] columns = new String[] { Column.date_in.name(), };
            String orderBy = "date_in DESC";
            String limit = "1";

            try(Cursor cursor = db.query(TABLE_VOCABULARY, columns, null, null, null, null, orderBy, limit))
            {
                if(cursor.moveToFirst())
                {
                    lastUpdatedDate = cursor.getString(0);
                }
                else
                {
                    LogsManager.log(CLASS_NAME, "getLastUpdated", "lastUpdatedDate=" + lastUpdatedDate);
                    return lastUpdatedDate;
                }
            }
        }
        // Parse String to LocalDateTime, to be able to format properly.
        LocalDateTime date = LocalDateTime.parse(lastUpdatedDate, DateTimeFormatter.ofPattern(DATE_FORMAT_DATABASE));
        lastUpdatedDate = DateTimeFormatter.ofPattern(format).format(date);

        LogsManager.log(CLASS_NAME, "getLastUpdated", "lastUpdatedDate=" + lastUpdatedDate);

        return lastUpdatedDate;
    }

    /**
     * Deletes the vocabulary from disk. Warning: this action cannot be reverted
     */
    public void deleteVocabulariesFromDisk()
    {
        try(SQLiteDatabase db = this.dbHelper.getWritableDatabase())
        {
            int result = db.delete(TABLE_VOCABULARY, null, null);
            Log.d(LogsManager.TAG, CLASS_NAME + ": deleteVocabularyFromDisk. affected=" + result);
        }
    }

    public ArrayList<Vocabulary> getVocabulariesFromMap(EnumMap<ForeignLanguage, ArrayList<Vocabulary>> vocabularyMap, ForeignLanguage foreignLanguage)
    {
        return vocabularyMap.get(foreignLanguage);
    }
}