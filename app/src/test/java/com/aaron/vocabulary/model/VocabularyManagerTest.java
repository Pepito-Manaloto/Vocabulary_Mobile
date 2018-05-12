package com.aaron.vocabulary.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.aaron.vocabulary.RobolectricTest;
import com.aaron.vocabulary.bean.ForeignLanguage;
import com.aaron.vocabulary.bean.Vocabulary;

import org.junit.Before;
import org.junit.Test;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.EnumMap;

import static com.aaron.vocabulary.bean.ForeignLanguage.Hokkien;
import static com.aaron.vocabulary.model.MySQLiteHelper.TABLE_VOCABULARY;
import static com.aaron.vocabulary.model.VocabularyManager.DATE_FORMAT_DATABASE;
import static com.aaron.vocabulary.model.VocabularyManager.DATE_FORMAT_WEB;
import static com.aaron.vocabulary.model.VocabularyManager.DEFAULT_LAST_UPDATED;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomUtils.nextInt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VocabularyManagerTest extends RobolectricTest
{
    private MySQLiteHelper dbHelperTest;
    private VocabularyManager vocabularyManager;
    private LocalDateTime dateIn;
    private int vocabularyCountPerLanguage;

    @Before
    public void init()
    {
        dbHelperTest = new MySQLiteHelper(getContext());
        vocabularyManager = new VocabularyManager(getContext());
        dateIn = LocalDateTime.now();
        vocabularyCountPerLanguage = nextInt(5, 10);
    }

    @Test
    public void givenVocabularyMap_whenReplaceVocabulariesInDisk_thenVocabulariesShouldBeSavedToDatabase()
    {
        EnumMap<ForeignLanguage, ArrayList<Vocabulary>> vocabularyMap = givenVocabularyMap();

        boolean result = vocabularyManager.replaceVocabulariesInDisk(vocabularyMap);

        assertTrue(result);
        thenVocabulariesShouldBeSavedToDatabase(vocabularyMap);
    }

    /**
     * Note: Long method names throws "SQLiteCantOpenDatabaseException: Cannot open SQLite connection" while using Robolectric context.
     * - Max 118 characters.
     * - Checked using getContext().getDatabasePath("aaron_vocabulary.db");
     */
    @Test
    public void givenVocabulariesInDisk_whenGetVocabulariesFromDiskOfForeignLang_thenVocabulariesOfForeignLangShouldBeReturned()
    {
        givenVocabulariesOfEachForeignLanguageInDisk();

        ArrayList<Vocabulary> vocabulariesFromDisk = vocabularyManager.getVocabulariesFromDisk(Hokkien);

        assertFalse(vocabulariesFromDisk.isEmpty());
        vocabulariesFromDisk.forEach(vocabulary -> assertEquals(Hokkien, vocabulary.getForeignLanguage()));
    }

    @Test
    public void givenVocabulariesInDisk_whenGetVocabulariesCount_thenVocabulariesCountGroupByForeignLanguagesFromDiskShouldBeReturned()
    {
        givenVocabulariesOfEachForeignLanguageInDisk();

        EnumMap<ForeignLanguage, Integer> countMap = vocabularyManager.getVocabulariesCount();

        thenVocabulariesCountGroupByForeignLanguagesFromDiskShouldBeReturned(countMap);
    }

    @Test
    public void givenVocabulariesInDiskAndDateFormat_whenGetLastUpdated_thenFormattedLastUpdatedShouldBeReturned()
    {
        givenVocabulariesOfEachForeignLanguageInDisk();

        String lastUpdated = vocabularyManager.getLastUpdated(DATE_FORMAT_WEB);
        assertEquals(DateTimeFormatter.ofPattern(DATE_FORMAT_WEB).format(dateIn), lastUpdated);

        lastUpdated = vocabularyManager.getLastUpdated(DATE_FORMAT_DATABASE);
        assertEquals(DateTimeFormatter.ofPattern(DATE_FORMAT_DATABASE).format(dateIn), lastUpdated);
    }

    @Test
    public void givenEmptyDiskAndDateFormat_whenGetLastUpdated_thenDefaultLastUpdatedShouldBeReturned()
    {
        String lastUpdated = vocabularyManager.getLastUpdated(DATE_FORMAT_DATABASE);

        assertEquals(DEFAULT_LAST_UPDATED, lastUpdated);
    }

    @Test
    public void givenVocabulariesInDisk_whenDeleteVocabulariesFromDisk_thenVocabulariesFromDiskAreDeleted()
    {
        givenVocabulariesOfEachForeignLanguageInDisk();

        vocabularyManager.deleteVocabulariesFromDisk();
        thenVocabulariesFromDiskAreDeleted();
    }

    private EnumMap<ForeignLanguage, ArrayList<Vocabulary>> givenVocabularyMap()
    {
        EnumMap<ForeignLanguage, ArrayList<Vocabulary>> vocabularyMap = new EnumMap<>(ForeignLanguage.class);

        for(ForeignLanguage language : ForeignLanguage.values())
        {
            int count = vocabularyCountPerLanguage * (language.ordinal() + 1);
            ArrayList<Vocabulary> list = new ArrayList<>(count);

            for(int i = 0; i < count; i++)
            {
                list.add(new Vocabulary(randomAlphabetic(5), randomAlphabetic(5), language));
            }

            vocabularyMap.put(language, list);
        }

        return vocabularyMap;
    }

    private void givenVocabulariesOfEachForeignLanguageInDisk()
    {
        EnumMap<ForeignLanguage, ArrayList<Vocabulary>> vocabularyMap = givenVocabularyMap();

        try(SQLiteDatabase db = dbHelperTest.getWritableDatabase())
        {
            for(ArrayList<Vocabulary> vocabularies : vocabularyMap.values())
            {
                for(Vocabulary vocabulary : vocabularies)
                {
                    String sql = String.format("INSERT INTO %s(english_word, foreign_word, foreign_language, date_in) VALUES('%s', '%s', '%s', '%s')",
                            TABLE_VOCABULARY, vocabulary.getEnglishWord(), vocabulary.getForeignWord(), vocabulary.getForeignLanguage(),
                            dateIn.format(DateTimeFormatter.ofPattern(DATE_FORMAT_DATABASE)));
                    db.execSQL(sql);
                }
            }
        }
    }

    private void thenVocabulariesShouldBeSavedToDatabase(EnumMap<ForeignLanguage, ArrayList<Vocabulary>> vocabularyMap)
    {
        vocabularyMap.forEach(this::assertVocabulariesInDatabaseIsEqualToGivenVocabularies);
    }

    private void assertVocabulariesInDatabaseIsEqualToGivenVocabularies(ForeignLanguage foreignLanguage, ArrayList<Vocabulary> expected)
    {
        // Warning: Dependent on the object being tested
        ArrayList<Vocabulary> actual = vocabularyManager.getVocabulariesFromDisk(foreignLanguage);
        assertEquals(expected, actual);
    }

    private void thenVocabulariesCountGroupByForeignLanguagesFromDiskShouldBeReturned(EnumMap<ForeignLanguage, Integer> countMap)
    {
        countMap.forEach(((foreignLanguage, count) -> assertEquals(vocabularyCountPerLanguage * (foreignLanguage.ordinal() + 1), count.intValue())));
    }

    private void thenVocabulariesFromDiskAreDeleted()
    {
        try(SQLiteDatabase db = dbHelperTest.getReadableDatabase())
        {
            try(Cursor cursor = db.rawQuery(String.format("SELECT * FROM %s", TABLE_VOCABULARY), null))
            {
                assertEquals(0, cursor.getCount());
            }
        }
    }
}