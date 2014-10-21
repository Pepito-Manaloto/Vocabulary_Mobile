package com.aaron.vocabulary.model;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.bean.Settings;
import com.aaron.vocabulary.bean.Vocabulary;
import com.aaron.vocabulary.bean.Vocabulary.ForeignLanguage;

import static com.aaron.vocabulary.bean.Vocabulary.JsonKey.*;
import static com.aaron.vocabulary.model.MySQLiteHelper.*;

/**
 * Handles the web call to retrieve vocabularies in JSON object representation.
 * Handles the data storage of vocabularies.
 */
public class VocabularyManager
{
    private int responseCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
    private String responseText;
    private ForeignLanguage languageSelected;
    private int recentlyAddedCount;

    private final String url;
    private static final String AUTH_KEY = "449a36b6689d841d7d27f31b4b7cc73a";

    public static final String TAG = "VocabularyManager";
    private static final String DATE_FORMAT_SHORT = "yyyy-MM-dd";
    private static final String DATE_FORMAT_LONG = "MMMM d, yyyy";
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT_SHORT, Locale.getDefault());

    private MySQLiteHelper dbHelper;
    private Date curDate;

    /**
     * Constructor initializes the url.
     * @param Activity the caller activity
     */
    public VocabularyManager(final Activity activity)
    {
        this.url = "http://" + activity.getString(R.string.url_address) + activity.getString(R.string.url_resource);
        this.dbHelper = new MySQLiteHelper(activity);
        this.curDate = new Date();
    }

    /**
     * Constructor initializes the url and the current application settings.
     * @param activity the caller activity
     * @param settings the current settings
     */
    public VocabularyManager(final Activity activity, final Settings settings)
    {
        this(activity);
        this.languageSelected = settings.getForeignLanguage();
    }

    /**
     * Does the following logic.
     * (1) Retrieves the vocabularies from the server.
     * (2) Saves the vocabularies in local disk.
     * (3) Returns the vocabulary list of the current selected language.
     * @return ArrayList<Vocabulary>
     */
    public ArrayList<Vocabulary> getVocabulariesFromWeb() 
    {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
        HttpConnectionParams.setSoTimeout(httpParams, 10000);

        HttpClient httpclient = new DefaultHttpClient(httpParams);

        HttpGet httpGet = new HttpGet(this.url);
        httpGet.addHeader("Authorization", AUTH_KEY);

        try
        {
            HttpResponse response = httpclient.execute(httpGet);
            this.responseCode = response.getStatusLine().getStatusCode();

            if(this.responseCode == HttpStatus.SC_OK)
            {
                HttpEntity httpEntity = response.getEntity();

                String responseString = EntityUtils.toString(httpEntity); // Response body

                JSONObject jsonObject = new JSONObject(responseString); // Response body in JSON object

                HashMap<ForeignLanguage, ArrayList<Vocabulary>> map = this.parseJsonObject(jsonObject);

                boolean saveToDiskSuccess = this.saveToDisk(map);
                
                if(!saveToDiskSuccess)
                {
                    this.responseCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                    this.responseText = "Failed saving to disk.";
                    
                    return new ArrayList<>(0);
                }

                this.responseText = "Success";

                // Entity is already consumed by EntityUtils; thus is already closed.

                return map.get(this.languageSelected);
            }

            // Closes the connection/ Consume the entity.
            response.getEntity().getContent().close();
        }
        catch(final IOException | JSONException e)
        {
            Log.e(LogsManager.TAG, "VocabularyManager: getVocabulariesFromWeb. " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            LogsManager.addToLogs("VocabularyManager: getVocabulariesFromWeb. Exception=" + e.getClass().getSimpleName() + " trace=" + e.getStackTrace());

            this.responseText = e.getMessage();
        }
        finally
        {
            Log.d(LogsManager.TAG, "VocabularyManager: getVocabulariesFromWeb. responseText=" + this.responseText +
                                   " responseCode=" + this.responseCode + " languageSelected=" + this.languageSelected);
            LogsManager.addToLogs("VocabularyManager: getVocabulariesFromWeb. responseText=" + this.responseText +
                                  " responseCode=" + this.responseCode + " languageSelected=" + this.languageSelected);
        }

        return new ArrayList<>(0);
    }

    /**
     * Parse the given jsonObject containing the list of vocabularies retrieved from the web call.
     * @param jsonObject the jsonObject to be parsed
     * @throws JSONException
     * @return jsonObject converted into a hashmap
     */
    private HashMap<ForeignLanguage, ArrayList<Vocabulary>> parseJsonObject(final JSONObject jsonObject) throws JSONException
    {
        Vocabulary vocabulary;
        HashMap<ForeignLanguage, ArrayList<Vocabulary>> map = new HashMap<>(); // Map containing the parsed result

        // Loop each language
        for(ForeignLanguage foreignLanguage: ForeignLanguage.values())
        {
            JSONArray jsonLangArray = jsonObject.getJSONArray(foreignLanguage.name()); // JSON array, for each language
            JSONObject jsonLangValues; // JSON items of the array of each language

            int jsonLangArrayLength = jsonLangArray.length();
            ArrayList<Vocabulary> listTemp = new ArrayList<>(jsonLangArrayLength);
            
            // Loop each values of the language
            for(int i = 0; i < jsonLangArrayLength; i++)
            {
                jsonLangValues = jsonLangArray.getJSONObject(i);
                vocabulary = new Vocabulary(jsonLangValues.getString(english_word.name()), jsonLangValues.getString(foreign_word.name()), foreignLanguage);
                listTemp.add(vocabulary);
            }

            map.put(foreignLanguage, listTemp);
        }

        Log.d(LogsManager.TAG, "VocabularyManager: parseJsonObject. json=" + jsonObject);
        LogsManager.addToLogs("VocabularyManager: parseJsonObject. json_length=" + jsonObject.length());

        return map;
    }

    /**
     * Saves the given vocabulary map in the local database.
     * @param vocabularyMap the vocabulary map to be stored
     * @return true on success, else false
     */
    private boolean saveToDisk(final HashMap<ForeignLanguage, ArrayList<Vocabulary>> vocabularyMap)
    {
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        ArrayList<Vocabulary> listTemp;
        ContentValues values;
        long result;

        dateFormatter.applyPattern(DATE_FORMAT_SHORT);
        db.beginTransaction();

        try
        {
            // Loop each language
            for(ForeignLanguage foreignLanguage: vocabularyMap.keySet())
            {
                listTemp = vocabularyMap.get(foreignLanguage);
                values = new ContentValues();
    
                // Loop contents of the language
                for(Vocabulary vocabulary: listTemp)
                {
                    values.put(Column.english_word.name(), vocabulary.getEnglishWord());
                    values.put(Column.foreign_word.name(), vocabulary.getForeignWord());
                    values.put(Column.foreign_language.name(), foreignLanguage.name());
                    values.put(Column.date_in.name(), dateFormatter.format(this.curDate));

                    result = db.insert(TABLE_VOCABULARY, null, values);

                    if(result > -1)
                    {
                        this.recentlyAddedCount++;
                    }
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

        Log.d(LogsManager.TAG, "VocabularyManager: saveToDisk.");

        return true;
    }

    /**
     * Returns the string representation of the status code returned by the last web call.
     * Internal Server Error is returned if the class does not have a previous web call.
     * @return String 
     */
    public String getStatusText()
    {
        switch(this.responseCode)
        {
            case 200:
                return "Ok";
            case 400:
                return "Bad Request";
            case 401:
                return "Unauthorized Access";
            case 500:
                return "Internal Server Error";
            default:
                return "Status Code Unknown";
        }
    }

    /**
     * Returns the response text by the last web call.
     * Empty text is returned if the class does not have a previous web call.
     * @return String
     */
    public String getResponseText()
    {
        return this.responseText;
    }

    /**
     * Returns the number of vocabularies that are new.
     * @return int
     */
    public int getRecentlyAddedCount()
    {
        return this.recentlyAddedCount;
    }

    /**
     * Does the following logic.
     * (1) Retrieves the vocabularies from the local disk.
     * (2) Returns the vocabulary list of the selected language.
     * @return ArrayList<Vocabulary>
     */
    public ArrayList<Vocabulary> getVocabulariesFromDisk()
    {
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();

        String[] columns = new String[]{Column.english_word.name(),
                                        Column.foreign_word.name(),
                                        Column.foreign_language.name()};
        String whereClause = "foreign_language = ?";
        String[] whereArgs = new String[]{this.languageSelected.name()};

        Cursor cursor = db.query(TABLE_VOCABULARY, columns, whereClause, whereArgs, null, null, null);
        ArrayList<Vocabulary> list = new ArrayList<>(cursor.getCount());

        if(cursor.moveToFirst())
        {
            do
            {
                list.add(cursorToVocabulary(cursor));
            } 
            while(cursor.moveToNext());
        }

        db.close();
        this.dbHelper.close();

        Log.d(LogsManager.TAG, "VocabularyManager: getVocabulariesFromDisk. list=" + list);
        LogsManager.addToLogs("VocabularyManager: getVocabulariesFromDisk. list_size=" + list.size());

        return list;
    }

    /**
     * Retrieves the vocabulary from the cursor.
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
     * Sets the language selected.
     * @param settings the current application settings
     */
    public void setLanguageSelected(final Settings settings)
    {
        if(settings != null)
        {
            this.languageSelected = settings.getForeignLanguage();
        }
    }

    /**
     * Gets the current vocabulary count per foreign languages, and returns them as a hashmap.
     * @return HashMap<ForeignLanguage, Integer>
     */
    public HashMap<ForeignLanguage, Integer> getVocabulariesCount()
    {
        HashMap<ForeignLanguage, Integer> map = new HashMap<>();
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        String whereClause = "foreign_language = ?";
        Cursor cursor;
        
        for(ForeignLanguage language: ForeignLanguage.values())
        {
            cursor = db.query(TABLE_VOCABULARY, COLUMN_COUNT, whereClause, new String[]{language.name()}, null, null, null);
            
            if(cursor.moveToFirst())
            {
                map.put(language, cursor.getInt(0));
            }
        }

        Log.d(LogsManager.TAG, "VocabularyManager: getVocabulariesCount. keys=" + map.keySet() + " values=" + map.values());
        LogsManager.addToLogs("VocabularyManager: getVocabulariesCount. keys=" + map.keySet() + " values_size=" + map.values().size());

        return map;
    }

    /**
     * Gets the latest date_in of the vocabularies.
     * @return String
     */
    public String getLastUpdated()
    {
        String lastUpdatedDate = "";
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        String[] columns = new String[]{Column.date_in.name(),};
        String orderBy = "date_in DESC";
        String limit = "1";

        Cursor cursor = db.query(TABLE_VOCABULARY, columns, null, null, null, null, orderBy, limit);

        if(cursor.moveToFirst())
        {
            lastUpdatedDate = cursor.getString(0);
        }

        if(lastUpdatedDate.length() <= 0)
        {
            return lastUpdatedDate;
        }

        try
        {
            dateFormatter.applyPattern(DATE_FORMAT_SHORT);
            Date date = dateFormatter.parse(lastUpdatedDate);
            dateFormatter.applyPattern(DATE_FORMAT_LONG);
            lastUpdatedDate = dateFormatter.format(date);
        }
        catch(ParseException e)
        {
            Log.e(LogsManager.TAG, "VocabularyManager: getLastUpdated. " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            LogsManager.addToLogs("VocabularyManager: getLastUpdated. Exception=" + e.getClass().getSimpleName() + " trace=" + e.getStackTrace());
        }

        Log.d(LogsManager.TAG, "VocabularyManager: getLastUpdated. lastUpdatedDate=" + lastUpdatedDate);
        LogsManager.addToLogs("VocabularyManager: getLastUpdated. lastUpdatedDate=" + lastUpdatedDate);

        return lastUpdatedDate;
    }

    /**
     * Deletes the vocabulary from disk.
     * Warning: this action cannot be reverted
     */
    public void deleteVocabularyFromDisk()
    {
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        String whereClause = "1";
        String[] whereArgs = null;

        int result = db.delete(TABLE_VOCABULARY, whereClause, whereArgs);

        db.close();
        this.dbHelper.close();

        Log.d(LogsManager.TAG, "VocabularyManager: deleteVocabularyFromDisk. affected=" + result);
    }

}
