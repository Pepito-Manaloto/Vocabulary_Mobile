package com.aaron.vocabulary.adapter;

import java.util.ArrayList;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.bean.Settings;
import com.aaron.vocabulary.bean.Vocabulary;
import com.aaron.vocabulary.model.LogsManager;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * ListView adapter for vocabulary list.
 */
public class VocabularyAdapter extends ArrayAdapter<Vocabulary>
{
    private static final String CLASS_NAME = VocabularyAdapter.class.getSimpleName();
    private Activity activity;
    private ArrayList<Vocabulary> vocabularyList;
    private ArrayList<Vocabulary> vocabularyListTemp;
    private Settings settings;

    /**
     * Default constructor. 0 is passed to the resource id, because we will be creating our own custom layout.
     * 
     * @param activity
     *            the current activity
     * @param vocabularyList
     *            the vocabulary list
     */
    public VocabularyAdapter(final Activity activity, final ArrayList<Vocabulary> vocabularyList, final Settings settings)
    {
        super(activity, 0, vocabularyList);

        this.activity = activity;
        this.vocabularyList = vocabularyList;
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

        if(convertView == null)
        {
            convertView = this.activity.getLayoutInflater().inflate(R.layout.fragment_vocabulary_list_row, parent, false);

            holder = new ViewHolder();
            holder.englishText = convertView.findViewById(R.id.text_english_language);
            holder.foreignText = convertView.findViewById(R.id.text_foreign_language);

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        Vocabulary vocabulary = getItem(position);
        holder.setVocabularyView(vocabulary, this.settings);

        return convertView;
    }

    /**
     * Filters the vocabulary list in the adapter with the given searched text. Only shows english vocabularies that starts with the searched text.
     * 
     * @param searched
     *            the searched word
     */
    public void filter(final String searched)
    {
        this.vocabularyList.clear();
        String searchedText = searched.trim();
        String englishWord;

        if(searchedText.length() == 0)
        {
            this.vocabularyList.addAll(this.vocabularyListTemp);
        }
        else
        {
            for(Vocabulary vocab : this.vocabularyListTemp)
            {
                englishWord = vocab.getEnglishWord();

                for(String word : englishWord.split(" / "))
                {
                    if(word.startsWith(searchedText))
                    {
                        this.vocabularyList.add(vocab);
                    }
                }
            }
        }

        Log.d(LogsManager.TAG, CLASS_NAME + ": filter. New list -> " + this.vocabularyList);
        LogsManager.addToLogs(CLASS_NAME + ": filter. New list size -> " + this.vocabularyList.size());
    }

    /**
     * Helper class for storing view values. Ensures findViewById() will only be called ones if convertView is not null.
     */
    private static class ViewHolder
    {
        TextView englishText;
        TextView foreignText;

        void setVocabularyView(Vocabulary vocabulary, Settings settings)
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
