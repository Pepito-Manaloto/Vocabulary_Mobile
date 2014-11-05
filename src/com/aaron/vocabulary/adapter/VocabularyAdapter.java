package com.aaron.vocabulary.adapter;

import java.util.ArrayList;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.bean.Settings;
import com.aaron.vocabulary.bean.Vocabulary;
import com.aaron.vocabulary.model.LogsManager;
import com.aaron.vocabulary.model.VocabularyManager;

import android.app.Activity;
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
    public static final String TAG = "VocabularyAdapter";
    private Activity activity;
    private ArrayList<Vocabulary> vocabularyList;
    private ArrayList<Vocabulary> vocabularyListTemporaryholder;
    private Settings settings;
    private VocabularyManager vocabularyManager;

    /**
     * Default constructor. 0 is passed to the resource id, because we will be creating our own custom layout.
     * @param context the current context
     * @param vocabularyList the vocabulary list
     */
    public VocabularyAdapter(final Activity context, final ArrayList<Vocabulary> vocabularyList, final Settings settings)
    {
        super(context, 0, vocabularyList);

        this.vocabularyManager = new VocabularyManager(context, settings.getForeignLanguage());

        this.activity = context;
        this.vocabularyList = vocabularyList;
        this.vocabularyListTemporaryholder = this.vocabularyManager.getVocabulariesFromDisk();
        this.settings = settings;
    }

    /**
     * Populates the ListView.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;

        if(convertView == null)
        {
            convertView = this.activity.getLayoutInflater().inflate(R.layout.fragment_vocabulary_list_row, parent, false);
            
            holder = new ViewHolder();
            holder.englishText = (TextView) convertView.findViewById(R.id.text_english_language);
            holder.foreignText = (TextView) convertView.findViewById(R.id.text_foreign_language);
            
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        Vocabulary vocabulary = getItem(position);

        holder.englishText.setText(vocabulary.getEnglishWord());
        holder.englishText.setTextSize(TypedValue.COMPLEX_UNIT_SP, this.settings.getFontSize());
        holder.englishText.setTypeface(this.settings.getTypeface());

        holder.foreignText.setText(vocabulary.getForeignWord());
        holder.foreignText.setTextSize(TypedValue.COMPLEX_UNIT_SP, this.settings.getFontSize());
        holder.foreignText.setTypeface(this.settings.getTypeface());

        return convertView;
    }

    /**
     * Filters the vocabulary list in the adapter with the given searched text. Only shows english vocabularies that starts with the searched text.
     * @param searched the searched word
     */
    public void filter(final String searched)
    {
        this.vocabularyList.clear();
        String searchedText = searched.trim();
        String englishWord;

        if(searchedText.length() == 0)
        {
            this.vocabularyList.addAll(this.vocabularyListTemporaryholder);
        }
        else
        {
            for(Vocabulary vocab: this.vocabularyListTemporaryholder)
            {
                englishWord = vocab.getEnglishWord();

                for(String word: englishWord.split(" / "))
                {
                    if(word.startsWith(searchedText))
                    {
                        this.vocabularyList.add(vocab);
                    }
                }
            }
        }

        Log.d(LogsManager.TAG, "VocabularyAdapter: filter. New list -> " + this.vocabularyList);
        LogsManager.addToLogs("VocabularyAdapter: filter. New list size -> " + this.vocabularyList.size());
    }

    /**
     * Helper class for storing view values. Ensures findViewById() will only be called ones if convertView is not null.
     */
    private static class ViewHolder
    {
        public TextView englishText;
        public TextView foreignText;
    }
}
