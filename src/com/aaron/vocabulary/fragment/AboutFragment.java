package com.aaron.vocabulary.fragment;

import java.util.HashMap;

import com.aaron.vocabulary.R;
import com.aaron.vocabulary.bean.Vocabulary.ForeignLanguage;
import com.aaron.vocabulary.model.VocabularyManager;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.aaron.vocabulary.bean.Vocabulary.ForeignLanguage.*;

public class AboutFragment extends Fragment
{
    private VocabularyManager vocabularyManager;

    /**
     * Initializes non-fragment user interface.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        getActivity().setTitle(R.string.menu_about);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        
        this.vocabularyManager = new VocabularyManager(getActivity());
    }

    /**
     * Initializes about fragment user interface.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_about, parent, false);

        final TextView buildNumberTextView = (TextView) view.findViewById(R.id.text_build_number);
        TextView lastUpdatedTextView = (TextView) view.findViewById(R.id.text_last_updated);
        TextView hokkienCountTextView = (TextView) view.findViewById(R.id.text_hokkien_count);
        TextView japaneseCountTextView = (TextView) view.findViewById(R.id.text_japanese_count);
        TextView mandarinCountTextView = (TextView) view.findViewById(R.id.text_mandarin_count);

        String buildNumber = getActivity().getString(R.string.build_num);
        String lastUpdated = this.vocabularyManager.getLastUpdated();
        HashMap<ForeignLanguage, Integer> vocabularyCount = this.vocabularyManager.getVocabulariesCount();

        buildNumberTextView.setText(buildNumber);
        lastUpdatedTextView.setText(lastUpdated);
        hokkienCountTextView.setText("Hokkien               " + vocabularyCount.get(Hokkien));
        japaneseCountTextView.setText("Japanese            " + vocabularyCount.get(Japanese));
        mandarinCountTextView.setText("Mandarin             " + vocabularyCount.get(Mandarin));

        /**
         * TODO: ON PRESS.
         * this.vocabularyManager.deleteVocabularyFromDisk();
         * this.list.clear();
         * ((VocabularyAdapter) getListAdapter()).notifyDataSetChanged();
         */
        return view;
    }
}
