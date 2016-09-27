package com.aaron.vocabulary.model;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.aaron.vocabulary.bean.ResponseVocabulary;
import com.aaron.vocabulary.bean.Vocabulary;
import com.aaron.vocabulary.bean.Vocabulary.ForeignLanguage;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

import static com.aaron.vocabulary.bean.Vocabulary.FOREIGN_LANGUAGE_ARRAY;
import static com.aaron.vocabulary.bean.Vocabulary.JsonKey.english_word;
import static com.aaron.vocabulary.bean.Vocabulary.JsonKey.foreign_word;
import static com.aaron.vocabulary.bean.Vocabulary.JsonKey.recently_added_count;
import static com.aaron.vocabulary.model.MySQLiteHelper.COLUMN_COUNT;
import static com.aaron.vocabulary.model.MySQLiteHelper.Column;
import static com.aaron.vocabulary.model.MySQLiteHelper.TABLE_VOCABULARY;

/**
 * Handles the web call to retrieve vocabularies in JSON object representation.
 * Handles the data storage of vocabularies.
 */
public class VocabularyManager
{
    public static final String CLASS_NAME = VocabularyManager.class.getSimpleName();

    public static final String DATE_FORMAT_LONG = "MMMM d, yyyy hh:mm:ss a";
    public static final String DATE_FORMAT_SHORT_24 = "yyyy-MM-dd HH:mm:ss";
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT_LONG, Locale.getDefault());
    private static final List<HttpClient.Header> HEADERS;

    private MySQLiteHelper dbHelper;
    private Date curDate;
    private HttpClient httpClient;

    static
    {
        HEADERS = new ArrayList<>(0);
        HEADERS.add(new HttpClient.Header("Authorization", new String(Hex.encodeHex(DigestUtils.md5("aaron")))));
    }

    /**
     * Constructor initializes the url.
     *
     * @param activity the caller activity
     */
    public VocabularyManager(final Activity activity)
    {
        this.dbHelper = new MySQLiteHelper(activity);
        this.curDate = new Date();
        this.httpClient = new HttpClient();
    }

    /**
     * Does the following logic.
     * (1) Retrieves the vocabularies from the server.
     * (2) Saves the vocabularies in local disk.
     *
     * @param url the url of the vocabulary web service
     * @return ResponseVocabulary
     */
    public ResponseVocabulary getVocabulariesFromWeb(String url)
    {
        ResponseVocabulary response = new ResponseVocabulary();
        Exception ex = null;

        try
        {
            String query = "?last_updated=" + URLEncoder.encode(this.getLastUpdated(DATE_FORMAT_SHORT_24), "UTF-8");

            Log.d(LogsManager.TAG, CLASS_NAME + ": getVocabulariesFromWeb. url=" + url + query);
            LogsManager.addToLogs(CLASS_NAME + ": getVocabulariesFromWeb. url=" + url + query);

            response = this.httpClient.get(url, query, HEADERS);

            if(response.getStatusCode() == HttpURLConnection.HTTP_OK)
            {
                if(StringUtils.isBlank(response.getBody())) // Response body empty
                {
                    return response;
                }

                JSONObject jsonObject = new JSONObject(response.getBody()); // Response body in JSON object

                int recentlyAddedCount = this.parseRecentlyAddedCountFromJsonObject(jsonObject);
                response.setRecentlyAddedCount(recentlyAddedCount);
                if(recentlyAddedCount <= 0) // No need to save to disk, because there are no new data entries.
                {
                    return response;
                }

                response.setVocabularyMap(this.parseVocabulariesFromJsonObject(jsonObject));
                response.setText("Success");
                return response;
            }
        }
        catch(final IOException | JSONException e)
        {
            response.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
            response.setText(e.getMessage());
            ex = e;
        }
        catch(final NumberFormatException e)
        {
            response.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
            response.setText("Error parsing json response: recently_added_count is not a number.");
            ex = e;
        }
        catch(final IllegalArgumentException e)
        {
            response.setStatusCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
            response.setText(url + " is not a valid host name.");
            ex = e;
        }
        finally
        {
            if(ex == null)
            {
                Log.d(LogsManager.TAG, CLASS_NAME + ": getVocabulariesFromWeb. responseText=" + response.getText() +
                        " responseCode=" + response.getStatusCode());
                LogsManager.addToLogs(CLASS_NAME + ": getVocabulariesFromWeb. responseText=" + response.getText() +
                        " responseCode=" + response.getStatusCode());
            }
            else
            {
                Log.e(LogsManager.TAG, CLASS_NAME + ": getVocabulariesFromWeb. " + ex.getClass().getSimpleName() + ": " + ex.getMessage(), ex);
                LogsManager.addToLogs(CLASS_NAME + ": getVocabulariesFromWeb. Exception=" + ex.getClass().getSimpleName() + " trace=" + ex.getStackTrace());
            }
        }

        return response;
    }

    /**
     * Parse the given jsonObject and returns the recently added count.
     *
     * @param jsonObject the jsonObject to be parsed
     * @return int
     * @throws NumberFormatException
     */
    private int parseRecentlyAddedCountFromJsonObject(final JSONObject jsonObject) throws NumberFormatException
    {
        return Integer.parseInt(String.valueOf(jsonObject.remove(recently_added_count.name())));
    }

    /**
     * Parse the given jsonObject containing the list of vocabularies retrieved from the web call.
     *
     * @param jsonObject the jsonObject to be parsed
     * @return jsonObject converted into an EnumMap, wherein the key is the foreign language and values are list of vocabularies
     * @throws JSONException
     */
    private EnumMap<ForeignLanguage, ArrayList<Vocabulary>> parseVocabulariesFromJsonObject(final JSONObject jsonObject) throws JSONException
    {
        // Ensure the json string only contains recipes
        if(jsonObject.has(recently_added_count.name()))
        {
            jsonObject.remove(recently_added_count.name());
        }

        EnumMap<ForeignLanguage, ArrayList<Vocabulary>> map = new EnumMap<>(ForeignLanguage.class); // Map containing the parsed result

        // Loop each language
        for(ForeignLanguage foreignLanguage : FOREIGN_LANGUAGE_ARRAY)
        {
            JSONArray jsonLangArray = jsonObject.getJSONArray(foreignLanguage.name()); // JSON array, for each language
            JSONObject jsonLangValues; // JSON items of the array of each language

            int jsonLangArrayLength = jsonLangArray.length();
            ArrayList<Vocabulary> listTemp = new ArrayList<>(jsonLangArrayLength);

            // Loop each values of the language
            for(int i = 0; i < jsonLangArrayLength; i++)
            {
                jsonLangValues = jsonLangArray.getJSONObject(i);
                listTemp.add(new Vocabulary(jsonLangValues.getString(english_word.name()), jsonLangValues.getString(foreign_word.name()), foreignLanguage));
            }

            map.put(foreignLanguage, listTemp);
        }

        Log.d(LogsManager.TAG, CLASS_NAME + ": parseVocabulariesFromJsonObject. map=" + map);
        LogsManager.addToLogs(CLASS_NAME + ": parseVocabulariesFromJsonObject. json_length=" + jsonObject.length());

        return map;
    }

    /**
     * Saves the given lists of vocabularies to the local database.
     *
     * @param vocabularyLists the recipe lists to be stored
     * @return true on success, else false
     */
    public boolean saveRecipesToDisk(final Collection<ArrayList<Vocabulary>> vocabularyLists)
    {
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        dateFormatter.applyPattern(DATE_FORMAT_LONG);

        try
        {
            db.beginTransaction();
            // Delete vocabularies. To ensure no duplicates, if existing vocabularies are modified in the server.
            db.delete(TABLE_VOCABULARY, null, null);

            // Loop each language
            for(ArrayList<Vocabulary> vocabularyList : vocabularyLists)
            {
                // Loop contents of the language
                for(Vocabulary vocabulary : vocabularyList)
                {
                    values.put(Column.english_word.name(), vocabulary.getEnglishWord());
                    values.put(Column.foreign_word.name(), vocabulary.getForeignWord());
                    values.put(Column.foreign_language.name(), vocabulary.getForeignLanguage().name());
                    values.put(Column.date_in.name(), dateFormatter.format(this.curDate));

                    db.insert(TABLE_VOCABULARY, null, values);
                }
            }

            db.setTransactionSuccessful();
        }
        finally
        {
            db.endTransaction();
            db.close();
            this.dbHelper.close();
        }

        Log.d(LogsManager.TAG, CLASS_NAME + ": saveToDisk.");
        LogsManager.addToLogs(CLASS_NAME + ": saveToDisk.");

        return true;
    }

    /**
     * Does the following logic. (1) Retrieves the vocabularies from the local disk. (2) Returns the vocabulary list of the selected language.
     *
     * @return ArrayList<Vocabulary>
     */
    public ArrayList<Vocabulary> getVocabulariesFromDisk(final ForeignLanguage selectedLanguage)
    {
        ArrayList<Vocabulary> list;

        try(SQLiteDatabase db = this.dbHelper.getReadableDatabase())
        {
            String[] columns = new String[] {Column.english_word.name(), Column.foreign_word.name(), Column.foreign_language.name()};
            String whereClause = "foreign_language = ?";
            String[] whereArgs = new String[] {selectedLanguage.name()};

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

        Log.d(LogsManager.TAG, CLASS_NAME + ": getVocabulariesFromDisk. list=" + list);
        LogsManager.addToLogs(CLASS_NAME + ": getVocabulariesFromDisk. list_size=" + list.size());

        return list;
    }

    /**
     * Retrieves the vocabulary from the cursor.
     *
     * @param cursor the cursor resulting from a query
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
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        String whereClause = "foreign_language = ?";
        Cursor cursor;

        for(ForeignLanguage language : FOREIGN_LANGUAGE_ARRAY)
        {
            cursor = db.query(TABLE_VOCABULARY, COLUMN_COUNT, whereClause, new String[] {language.name()}, null, null, null);

            if(cursor.moveToFirst())
            {
                map.put(language, cursor.getInt(0));
            }
        }

        Log.d(LogsManager.TAG, CLASS_NAME + ": getVocabulariesCount. keys=" + map.keySet() + " values=" + map.values());
        LogsManager.addToLogs(CLASS_NAME + ": getVocabulariesCount. keys=" + map.keySet() + " values_size=" + map.values().size());

        return map;
    }

    /**
     * Gets the latest date_in of the vocabularies.
     *
     * @param format the date format used in formatting the last_updated date
     * @return String
     */
    public String getLastUpdated(final String format)
    {
        String lastUpdatedDate = "1950-01-01 00:00:00";
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        String[] columns = new String[] {Column.date_in.name(),};
        String orderBy = "date_in DESC";
        String limit = "1";

        Cursor cursor = db.query(TABLE_VOCABULARY, columns, null, null, null, null, orderBy, limit);

        if(cursor.moveToFirst())
        {
            lastUpdatedDate = cursor.getString(0);
        }
        else
        {
            return lastUpdatedDate;
        }

        try
        {
            dateFormatter.applyPattern(DATE_FORMAT_LONG);
            Date date = dateFormatter.parse(lastUpdatedDate); // Parse String to Date, to be able to format properly.

            dateFormatter.applyPattern(format);
            lastUpdatedDate = dateFormatter.format(date);
        }
        catch(ParseException e)
        {
            Log.e(LogsManager.TAG, CLASS_NAME + ": getLastUpdated. " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            LogsManager.addToLogs(CLASS_NAME + ": getLastUpdated. Exception=" + e.getClass().getSimpleName() + " trace=" + e.getStackTrace());
        }

        Log.d(LogsManager.TAG, CLASS_NAME + ": getLastUpdated. lastUpdatedDate=" + lastUpdatedDate);
        LogsManager.addToLogs(CLASS_NAME + ": getLastUpdated. lastUpdatedDate=" + lastUpdatedDate);

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
