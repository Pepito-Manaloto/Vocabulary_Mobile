package com.aaron.vocabulary.model;

import android.util.Log;

import com.aaron.vocabulary.bean.ResponseVocabulary;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * The model class for making http requests.
 */
public class HttpClient
{
    private static final String CLASS_NAME = HttpClient.class.getSimpleName();

    private static final int DEFAUT_TIMEOUT = 10;
    private static final String AUTHORIZATION = "Authorization";
    private static final String AUTHORIZATION_VALUE = new String(Hex.encodeHex(DigestUtils.md5("aaron")));

    private static OkHttpClient okHttpClient;
    private static VocabularyService service;

    public HttpClient(String hostname)
    {
        initializeRetrofit(hostname);
    }

    private void initializeRetrofit(String hostname)
    {
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAUT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(DEFAUT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(DEFAUT_TIMEOUT, TimeUnit.SECONDS)
                .pingInterval(DEFAUT_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(this::authorizationHeaderInterceptor)
                .build();

        reinitializeRetrofit(hostname);
    }

    /**
     * Sets the retrofit http client.
     */
    public static void reinitializeRetrofit(String hostname)
    {
        String baseUrl = String.format(VocabularyService.BASE_URL, hostname);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient).addConverterFactory(JacksonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        service = retrofit.create(VocabularyService.class);

        Log.d(LogsManager.TAG, CLASS_NAME + ": reinitializeRetrofit. new BaseUrl=" + baseUrl);
    }

    private Response authorizationHeaderInterceptor(Interceptor.Chain chain) throws IOException
    {
        Request request = chain.request().newBuilder()
                .addHeader(AUTHORIZATION, AUTHORIZATION_VALUE)
                .build();

        return chain.proceed(request);
    }

    /**
     * Get all vocabularies that are greater than or equal with the given last updated.
     *
     * @param lastUpdated the last updated filter
     * @return ResponseVocabulary
     */
    public Single<ResponseVocabulary> getVocabularies(String lastUpdated)
    {
        return service.getAllVocabularies(lastUpdated);
    }
}
