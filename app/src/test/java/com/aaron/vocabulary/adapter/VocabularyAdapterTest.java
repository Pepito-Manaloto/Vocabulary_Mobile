package com.aaron.vocabulary.adapter;

import com.aaron.vocabulary.RobolectricTest;
import com.aaron.vocabulary.bean.ForeignLanguage;
import com.aaron.vocabulary.bean.SearchType;
import com.aaron.vocabulary.bean.Vocabulary;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VocabularyAdapterTest extends RobolectricTest
{
    private VocabularyAdapter adapter;

    @Test
    public void givenVocabularyListAndSearchTextAndEnglishSearchType_whenFilter_thenVocabularyListShouldOnlyIncludeEnglishVocabulariesMatchingTheSearchText()
    {
        ArrayList<Vocabulary> vocabularyList = givenVocabularyList();
        String searchText = "bo";
        SearchType type = SearchType.ENGLISH;

        adapter = initializeVocabularyAdapter(vocabularyList);
        adapter.filter(searchText, type);

        thenVocabularyListShouldOnlyIncludeEnglishVocabulariesMatchingTheSearchText(vocabularyList, searchText);
    }

    @Test
    public void givenVocabularyListAndSearchTextAndForeignSearchType_whenFilter_thenVocabularyListShouldOnlyIncludeForeignVocabulariesMatchingTheSearchText()
    {
        ArrayList<Vocabulary> vocabularyList = givenVocabularyList();
        String searchText = "phá";
        SearchType type = SearchType.FOREIGN;

        adapter = initializeVocabularyAdapter(vocabularyList);

        adapter.filter(searchText, type);

        thenVocabularyListShouldOnlyIncludeForeignVocabulariesMatchingTheSearchText(vocabularyList, searchText);
    }

    @Test
    public void givenVocabularyListAndEmptySearchTextAndEnglishSearchType_whenFilter_thenVocabularyListShouldIncludeAll()
    {
        ArrayList<Vocabulary> vocabularyList = givenVocabularyList();
        ArrayList<Vocabulary> originalList = new ArrayList<>(vocabularyList);
        String searchText = "";
        SearchType type = SearchType.ENGLISH;

        adapter = initializeVocabularyAdapter(vocabularyList);
        adapter.filter(searchText, type);

        assertEquals(originalList, vocabularyList);
    }

    @Test
    public void givenNullList_whenUpdate_thenShouldDoNothing()
    {
        ArrayList<Vocabulary> adapterList = givenVocabularyList();
        ArrayList<Vocabulary> originalList = new ArrayList<>(adapterList);

        adapter = initializeVocabularyAdapter(adapterList);

        adapter.update(null);

        assertEquals(originalList, adapterList);
    }

    @Test
    public void givenEmptyVocabularyList_whenUpdate_thenAdapterListShouldBeCleared()
    {
        ArrayList<Vocabulary> adapterList = givenVocabularyList();

        adapter = initializeVocabularyAdapter(adapterList);

        adapter.update(new ArrayList<>(0));

        assertTrue(adapterList.isEmpty());
    }

    @Test
    public void givenVocabularyList_whenUpdate_thenAdapterListShouldBeReplacedWithTheGivenList()
    {
        ArrayList<Vocabulary> adapterList = new ArrayList<>(0);
        adapter = initializeVocabularyAdapter(adapterList);

        ArrayList<Vocabulary> vocabularyList = givenVocabularyList();

        adapter.update(vocabularyList);

        assertEquals(vocabularyList, adapterList);
    }

    private VocabularyAdapter initializeVocabularyAdapter(ArrayList<Vocabulary> vocabularyList)
    {
        return new VocabularyAdapter(getContext(), vocabularyList, null);
    }

    private ArrayList<Vocabulary> givenVocabularyList()
    {
        ArrayList<Vocabulary> list = new ArrayList<>();

        list.add(new Vocabulary("ugly", "asdasdⁿ pházxcⁿ / pháiⁿ khoàⁿ", ForeignLanguage.Hokkien));
        list.add(new Vocabulary("below / beneath / bottom / underneath", "tē-bīn / ē-tóe", ForeignLanguage.Hokkien));
        list.add(new Vocabulary("boss / employer / master", "thâu-ke", ForeignLanguage.Hokkien));
        list.add(new Vocabulary("bad", "pháiⁿ", ForeignLanguage.Hokkien));
        list.add(new Vocabulary("beat / hit / strike", "phah", ForeignLanguage.Hokkien));

        return list;
    }

    private void thenVocabularyListShouldOnlyIncludeEnglishVocabulariesMatchingTheSearchText(ArrayList<Vocabulary> vocabularyList, String searchText)
    {
        assertFalse(vocabularyList.isEmpty());
        vocabularyList.stream().map(Vocabulary::getEnglishWord).forEach(english -> assertVocabularyMatchesSearchText(english, searchText));
    }

    private void thenVocabularyListShouldOnlyIncludeForeignVocabulariesMatchingTheSearchText(ArrayList<Vocabulary> vocabularyList, String searchText)
    {
        vocabularyList.stream().map(Vocabulary::getForeignWord).forEach(english -> assertVocabularyMatchesSearchText(english, searchText));
    }

    private void assertVocabularyMatchesSearchText(String vocabulary, String search)
    {
        boolean matched = Arrays.stream(vocabulary.split("/")).map(String::trim).anyMatch(word -> word.startsWith(search));

        String errorMessage = String.format("'%s' should match search text '%s'", vocabulary, search);
        assertTrue(errorMessage, matched);
    }
}
