package com.aaron.vocabulary.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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

import com.aaron.vocabulary.R;
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

    private MySQLiteHelper dbHelper;

    /**
     * Constructor initializes the url.
     * @param Activity the caller activity
     */
    public VocabularyManager(final Activity activity)
    {   
        String parsedTitle = activity.getTitle().toString();
        parsedTitle = parsedTitle.substring(12, parsedTitle.length() - 1); // Extract selected language from the title

        this.languageSelected = ForeignLanguage.valueOf(parsedTitle);
        this.url = "http://" + activity.getString(R.string.url_address) + activity.getString(R.string.url_resource);
        //this.url = "http://10.11.3.106/test/get.php";
        this.dbHelper = new MySQLiteHelper(activity);
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
        HttpConnectionParams.setConnectionTimeout(httpParams, 15000);
        HttpConnectionParams.setSoTimeout(httpParams, 15000);

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

                return map.get(this.languageSelected);
            }

            // Closes the connection.
            response.getEntity().getContent().close();
        }
        catch(final IOException | JSONException e)
        {
            //TODO: log error
            this.responseText = e.getMessage();
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
            ArrayList<Vocabulary> listTemp = new ArrayList<>();
            
            // Loop each values of the language
            for(int i = 0; i < jsonLangArrayLength; i++)
            {
                jsonLangValues = jsonLangArray.getJSONObject(i);
                vocabulary = new Vocabulary(jsonLangValues.getString(english_word.name()), jsonLangValues.getString(foreign_word.name()), foreignLanguage);
                listTemp.add(vocabulary);
            }

            map.put(foreignLanguage, listTemp);
        }
        
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
                    values.put(Column.foreign_word.name(), foreignLanguage.name());

                    result = db.insert(TABLE_VOCABULARY, null, values);
                    
                    if(result <= -1)
                    {
                        return false;
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
        String whereClause = "foreign_language = ?";
        String[] whereArgs = new String[]{this.languageSelected.name()};

        Cursor cursor = db.query(TABLE_VOCABULARY, columnStringArray, whereClause, whereArgs, null, null, null);
        ArrayList<Vocabulary> list = new ArrayList<>();

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
}
