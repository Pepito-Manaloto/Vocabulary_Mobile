package com.aaron.vocabulary.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.bean.Vocabulary;

/**
 * Handles the web call to retrieve vocabularies in JSON object representation.
 * Handles the data storage of vocabularies.
 */
public class VocabularyManager
{
    private final String url;

    public VocabularyManager(final Activity activity)
    {
        this.url = "http://" + activity.getString(R.string.url_address) + activity.getString(R.string.url_resource);
    }

    public List<Vocabulary> getVocabularies() throws IOException
    {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(new HttpGet(this.url));
        StatusLine statusLine = response.getStatusLine();

        if(statusLine.getStatusCode() == HttpStatus.SC_OK)
        {
            HttpEntity httpEntity = response.getEntity();

            String responseString = EntityUtils.toString(httpEntity);

            //TODO: (1) save to database. (2) return list depending on language selected. 
            System.out.println(responseString);
        }
        else
        {
            // Closes the connection.
            response.getEntity().getContent().close();
            throw new IOException(statusLine.getReasonPhrase());
        }
    }
}
