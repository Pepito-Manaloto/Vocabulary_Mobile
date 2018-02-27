package com.aaron.vocabulary.model;

import com.aaron.vocabulary.bean.ResponseVocabulary;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Aaron on 25/02/2018.
 */
public interface VocabularyService
{
    String BASE_URL = "http://%s/Vocabulary/";

    /**
     * Retrieves all vocabularies given the last updated date.
     *
     * @param lastUpdated filter get request with last updated date
     * @return {@code Call<ResponseVocabulary>} response Vocabulary
     */
    @GET("vocabularies")
    Single<ResponseVocabulary> getAllVocabularies(@Query("last_updated") String lastUpdated);
}