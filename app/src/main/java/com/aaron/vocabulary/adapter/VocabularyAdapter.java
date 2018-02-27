package com.aaron.vocabulary.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.bean.SearchType;
import com.aaron.vocabulary.bean.Settings;
import com.aaron.vocabulary.bean.Vocabulary;
import com.aaron.vocabulary.model.LogsManager;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * ListView adapter for vocabulary list.
 */
public class VocabularyAdapter extends ArrayAdapter<Vocabulary>
{
    private static final String CLASS_NAME = VocabularyAdapter.class.getSimpleName();
    private static final String ENGLISH_WORD_SEPARATOR = " / ";

    private ArrayList<Vocabulary> vocabularyListTemp;
    private Settings settings;

    /**
     * Default constructor. 0 is passed to the resource id, because we will be creating our own custom layout.
     *
     * @param context
     *            the current context
     * @param vocabularyList
     *            the vocabulary list
     * @param settings
     *            the current settings
     */
    public VocabularyAdapter(final Context context, final ArrayList<Vocabulary> vocabularyList, final Settings settings)
    {
        super(context, 0, vocabularyList);

        this.vocabularyListTemp = new ArrayList<>(vocabularyList);
        this.settings = settings;
    }

    /**
     * Populates the ListView.
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent)
    {
        ViewHolder holder;
        View listRowView;

        if(convertView == null)
        {
            listRowView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_vocabulary_list_row, parent, false);

            holder = new ViewHolder();
            holder.englishText = listRowView.findViewById(R.id.text_english_language);
            holder.foreignText = listRowView.findViewById(R.id.text_foreign_language);

            listRowView.setTag(holder);
        }
        else
        {
            listRowView = convertView;
            holder = (ViewHolder) convertView.getTag();
        }

        Vocabulary vocabulary = getItem(position);
        holder.setVocabularyView(vocabulary, this.settings);

        return listRowView;
    }

    /**
     * Filters the vocabulary list in the adapter with the given searched text. Only shows english vocabularies that starts with the searched text.
     *
     * @param searched
     *            the searched word
     * 
     * @param searchType
     *            the language to search/filter
     */
    public void filter(final String searched, SearchType searchType)
    {
        clear();
        String searchedText = searched.trim();

        if(searchedText.length() == 0)
        {
            addAll(this.vocabularyListTemp);
        }
        else
        {
            boolean isEnglish = SearchType.ENGLISH.equals(searchType);
            vocabularyListTemp.forEach(vocabulary ->
            {
                if(isEnglish)
                {
                    filterEnglish(vocabulary, searchedText);
                }
                else
                {
                    filterForeign(vocabulary, searchedText);
                }
            });
        }

        notifyDataSetChanged();

        LogsManager.log(CLASS_NAME, "filter", "New list size = " + getCount());
    }

    private void filterEnglish(Vocabulary vocabulary, String searchedText)
    {
        String englishWord = vocabulary.getEnglishWord();
        boolean searchedTextFound = Arrays.stream(englishWord.split(ENGLISH_WORD_SEPARATOR)).anyMatch(word -> word.startsWith(searchedText));
        if(searchedTextFound)
        {
            add(vocabulary);
        }
    }

    private void filterForeign(Vocabulary vocabulary, String searchedText)
    {
        String foreignWord = vocabulary.getForeignWord();
        if(foreignWord.startsWith(searchedText))
        {
            add(vocabulary);
        }
    }

    /**
     * Updates the vocabulary list.
     *
     * @param list
     *            the list to replace the current
     */
    public void update(ArrayList<Vocabulary> list)
    {
        if(list != null)
        {
            clear();

            // If user deletes vocabulary list in AboutFragment
            if(!list.isEmpty())
            {
                addAll(list);
            }

            notifyDataSetChanged();
        }
    }

    /**
     * Helper class for storing view values. Ensures findViewById() will only be called ones if convertView is not null.
     */
    private static class ViewHolder
    {
        private TextView englishText;
        private TextView foreignText;

        private void setVocabularyView(Vocabulary vocabulary, Settings settings)
        {
            this.englishText.setText(vocabulary.getEnglishWord());
            this.englishText.setTextSize(TypedValue.COMPLEX_UNIT_SP, settings.getFontSize());
            this.englishText.setTypeface(settings.getTypeface());
            this.foreignText.setText(vocabulary.getForeignWord());
            this.foreignText.setTextSize(TypedValue.COMPLEX_UNIT_SP, settings.getFontSize());
            this.foreignText.setTypeface(settings.getTypeface());
        }
    }
}
