package com.aaron.vocabulary.model;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.bean.Settings;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;

import static com.aaron.vocabulary.bean.Vocabulary.FOREIGN_LANGUAGE_ARRAY;
import static com.aaron.vocabulary.bean.Vocabulary.JsonKey.english_word;
import static com.aaron.vocabulary.bean.Vocabulary.JsonKey.foreign_word;
import static com.aaron.vocabulary.bean.Vocabulary.JsonKey.recently_added_count;
import static com.aaron.vocabulary.model.MySQLiteHelper.COLUMN_COUNT;
import static com.aaron.vocabulary.model.MySQLiteHelper.Column;
import static com.aaron.vocabulary.model.MySQLiteHelper.TABLE_VOCABULARY;

/**
 * Handles the web call to retrieve vocabularies in JSON object representation. Handles the data storage of vocabularies.
 */
public class VocabularyManager
{
    private int responseCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
    private String responseText = "Success";
    private ForeignLanguage selectedLanguage;
    private int recentlyAddedCount;

    private String url;
    private static final String AUTH_KEY = new String(Hex.encodeHex(DigestUtils.md5("aaron")));

    public static final String TAG = "VocabularyManager";

    public static final String DATE_FORMAT_LONG = "MMMM d, yyyy hh:mm:ss a";
    public static final String DATE_FORMAT_SHORT_24 = "yyyy-MM-dd HH:mm:ss";
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT_LONG, Locale.getDefault());

    private MySQLiteHelper dbHelper;
    private Date curDate;

    /**
     * Constructor initializes the url.
     * 
     * @param activity
     *            the caller activity
     */
    public VocabularyManager(final Activity activity)
    {
        this.url = "http://" + activity.getString(R.string.url_address_default) + activity.getString(R.string.url_resource);

        this.dbHelper = new MySQLiteHelper(activity);
        this.curDate = new Date();
        this.selectedLanguage = ForeignLanguage.Hokkien;
    }

    /**
     * Constructor initializes the url and the current application settings.
     * 
     * @param activity
     *            the caller activity
     * @param settings
     *            the current settings
     */
    public VocabularyManager(final Activity activity, final Settings settings)
    {
        this(activity);
        this.selectedLanguage = settings.getForeignLanguage();

        if(settings.getServerURL() != null && !settings.getServerURL().isEmpty())
        {
            this.url = "http://" + settings.getServerURL() + activity.getString(R.string.url_resource);
        }
    }

    /**
     * Does the following logic. (1) Retrieves the vocabularies from the server. (2) Saves the vocabularies in local disk. (3) Returns the vocabulary
     * list of the current selected language.
     * 
     * @return ArrayList<Vocabulary>
     */
    public ArrayList<Vocabulary> getVocabulariesFromWeb()
    {
        HttpURLConnection con = null;

        try
        {
            String params = "?last_updated=" + URLEncoder.encode(this.getLastUpdated(DATE_FORMAT_SHORT_24), "UTF-8");

            Log.d(LogsManager.TAG, "VocabularyManager: getVocabulariesFromWeb. params=" + params);
            LogsManager.addToLogs("VocabularyManager: getVocabulariesFromWeb. params=" + params);

            URL getURL = new URL(this.url + params);
            con = (HttpURLConnection) getURL.openConnection();
            con.setConnectTimeout(10_000);
            con.setReadTimeout(10_000);
            con.addRequestProperty("Authorization", AUTH_KEY);

            this.responseCode = con.getResponseCode();

            if(this.responseCode == HttpURLConnection.HTTP_OK)
            {
                String responseString = getResponseRecipeBodyFromStream(con.getInputStream());// Response body

                if(StringUtils.isBlank(responseString)) // Response is empty
                {
                    return new ArrayList<>(0);
                }

                JSONObject jsonObject = new JSONObject(responseString); // Response body in JSON object

                EnumMap<ForeignLanguage, ArrayList<Vocabulary>> map = this.parseJsonObject(jsonObject);

                if(this.recentlyAddedCount <= 0) // No need to save to disk, because there are no new data entries.
                {

                    return new ArrayList<>(0);
                }

                boolean saveToDiskSuccess = this.saveToDisk(map);

                if(!saveToDiskSuccess)
                {
                    this.responseCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
                    this.responseText = "Failed saving to disk.";

                    return new ArrayList<>(0);
                }

                // Entity is already consumed by EntityUtils; thus is already closed.

                return map.get(this.selectedLanguage);
            }
        }
        catch(final IOException | IllegalArgumentException | JSONException e)
        {
            Log.e(LogsManager.TAG, "VocabularyManager: getVocabulariesFromWeb. " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            LogsManager.addToLogs("VocabularyManager: getVocabulariesFromWeb. Exception=" + e.getClass().getSimpleName() + " trace=" + e.getStackTrace());

            if(e instanceof IllegalArgumentException)
            {
                this.responseText = this.url + " is not a valid host name.";
            }
            else
            {
                this.responseText = e.getMessage();
            }
        }
        finally
        {
            if(con != null)
            {
                con.disconnect();
            }

            Log.d(LogsManager.TAG, "VocabularyManager: getVocabulariesFromWeb. responseText=" + this.responseText +
                    " responseCode=" + this.responseCode + " SelectedLanguage=" + this.selectedLanguage);
            LogsManager.addToLogs("VocabularyManager: getVocabulariesFromWeb. responseText=" + this.responseText +
                    " responseCode=" + this.responseCode + " SelectedLanguage=" + this.selectedLanguage);
        }

        return new ArrayList<>(0);
    }

    /**
     * Converts the inputStream to String.
     *
     * @param inputStream HttpURLConnection response
     * @return String
     */
    protected String getResponseRecipeBodyFromStream(final InputStream inputStream) throws IOException
    {
        String response = IOUtils.toString(inputStream, Charsets.UTF_8);
        IOUtils.closeQuietly(inputStream);

        return response;
    }

    /**
     * Parse the given jsonObject containing the list of vocabularies retrieved from the web call.
     * 
     * @param jsonObject
     *            the jsonObject to be parsed
     * @throws JSONException
     * @return jsonObject converted into a hashmap
     */
    private EnumMap<ForeignLanguage, ArrayList<Vocabulary>> parseJsonObject(final JSONObject jsonObject) throws JSONException
    {
        Vocabulary vocabulary;
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
                vocabulary = new Vocabulary(jsonLangValues.getString(english_word.name()), jsonLangValues.getString(foreign_word.name()), foreignLanguage);
                listTemp.add(vocabulary);
            }

            map.put(foreignLanguage, listTemp);
        }

        this.recentlyAddedCount = jsonObject.getInt(recently_added_count.name());

        Log.d(LogsManager.TAG, "VocabularyManager: parseJsonObject. json=" + jsonObject);
        LogsManager.addToLogs("VocabularyManager: parseJsonObject. json_length=" + jsonObject.length());

        return map;
    }

    /**
     * Saves the given vocabulary map to the local database.
     * 
     * @param vocabularyMap
     *            the vocabulary map to be stored
     * @return true on success, else false
     */
    private boolean saveToDisk(final EnumMap<ForeignLanguage, ArrayList<Vocabulary>> vocabularyMap)
    {
        SQLiteDatabase db = this.dbHelper.getWritableDatabase();
        ArrayList<Vocabulary> listTemp;
        ContentValues values = new ContentValues();

        dateFormatter.applyPattern(DATE_FORMAT_LONG);
        db.beginTransaction();

        try
        {
            // Delete vocabularies. To ensure no duplicates, if existing vocabularies are modified in the server.
            db.delete(TABLE_VOCABULARY, "1", null);

            // Loop each language
            for(ForeignLanguage foreignLanguage : vocabularyMap.keySet())
            {
                listTemp = vocabularyMap.get(foreignLanguage);

                // Loop contents of the language
                for(Vocabulary vocabulary : listTemp)
                {
                    values.put(Column.english_word.name(), vocabulary.getEnglishWord());
                    values.put(Column.foreign_word.name(), vocabulary.getForeignWord());
                    values.put(Column.foreign_language.name(), foreignLanguage.name());
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

        Log.d(LogsManager.TAG, "VocabularyManager: saveToDisk.");
        LogsManager.addToLogs("VocabularyManager: saveToDisk.");

        return true;
    }

    /**
     * Returns the string representation of the status code returned by the last web call. Internal Server Error is returned if the class does not
     * have a previous web call.
     * 
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
     * Returns the response text by the last web call. Empty text is returned if the class does not have a previous web call.
     * 
     * @return String
     */
    public String getResponseText()
    {
        return this.responseText;
    }

    /**
     * Returns the number of vocabularies that are new.
     * 
     * @return int
     */
    public int getRecentlyAddedCount()
    {
        return this.recentlyAddedCount;
    }

    /**
     * Does the following logic. (1) Retrieves the vocabularies from the local disk. (2) Returns the vocabulary list of the selected language.
     * 
     * @return ArrayList<Vocabulary>
     */
    public ArrayList<Vocabulary> getVocabulariesFromDisk()
    {
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();

        String[] columns = new String[] { Column.english_word.name(),
                Column.foreign_word.name(),
                Column.foreign_language.name() };
        String whereClause = "foreign_language = ?";
        String[] whereArgs = new String[] { this.selectedLanguage.name() };

        Cursor cursor = db.query(TABLE_VOCABULARY, columns, whereClause, whereArgs, null, null, null);
        ArrayList<Vocabulary> list = new ArrayList<>(cursor.getCount());

        if(cursor.moveToFirst())
        {
            do
            {
                list.add(this.cursorToVocabulary(cursor));
            } while(cursor.moveToNext());
        }

        db.close();
        this.dbHelper.close();

        Log.d(LogsManager.TAG, "VocabularyManager: getVocabulariesFromDisk. list=" + list);
        LogsManager.addToLogs("VocabularyManager: getVocabulariesFromDisk. list_size=" + list.size());

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
     * Sets the language selected.
     * 
     * @param language
     *            the language selected
     */
    public void setSelectedLanguage(final ForeignLanguage language)
    {
        this.selectedLanguage = language;
    }

    /**
     * Gets the current vocabulary count per foreign languages, and returns them as a hashmap.
     * 
     * @return HashMap<ForeignLanguage, Integer>
     */
    public HashMap<ForeignLanguage, Integer> getVocabulariesCount()
    {
        HashMap<ForeignLanguage, Integer> map = new HashMap<>();
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        String whereClause = "foreign_language = ?";
        Cursor cursor;

        for(ForeignLanguage language : FOREIGN_LANGUAGE_ARRAY)
        {
            cursor = db.query(TABLE_VOCABULARY, COLUMN_COUNT, whereClause, new String[] { language.name() }, null, null, null);

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
     * 
     * @param format
     *            the date format used in formatting the last_updated date
     * @return String
     */
    public String getLastUpdated(final String format)
    {
        String lastUpdatedDate = "1950-01-01 00:00:00";
        SQLiteDatabase db = this.dbHelper.getReadableDatabase();
        String[] columns = new String[] { Column.date_in.name(), };
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
            Log.e(LogsManager.TAG, "VocabularyManager: getLastUpdated. " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
            LogsManager.addToLogs("VocabularyManager: getLastUpdated. Exception=" + e.getClass().getSimpleName() + " trace=" + e.getStackTrace());
        }

        Log.d(LogsManager.TAG, "VocabularyManager: getLastUpdated. lastUpdatedDate=" + lastUpdatedDate);
        LogsManager.addToLogs("VocabularyManager: getLastUpdated. lastUpdatedDate=" + lastUpdatedDate);

        return lastUpdatedDate;
    }

    /**
     * Deletes the vocabulary from disk. Warning: this action cannot be reverted
     */
    public void deleteVocabulariesFromDisk()
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
