package com.aaron.vocabulary.adapter;

import java.util.ArrayList;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.bean.Vocabulary;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * ListView adapter for vocabulary list.
 */
public class VocabularyAdapter extends ArrayAdapter<Vocabulary>
{
    private Activity activity;
    /**
     * Default constructor. 0 is passed to the resource id, because we will be creating our own custom layout.
     * @param context the current context
     * @param vocabularyList the vocabulary list
     */
    public VocabularyAdapter(final Activity context, final ArrayList<Vocabulary> vocabularyList)
    {
        super(context, 0, vocabularyList);
        
        this.activity = context;
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
            convertView = this.activity.getLayoutInflater().inflate(R.layout.list_item_vocabulary, null);
            
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
        holder.foreignText.setText(vocabulary.getForeignWord());

        return convertView;
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
