package com.aaron.vocabulary.fragment;

import com.aaron.vocabulary.R;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutFragment extends Fragment
{
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

        String threeTabs = "            ";

        buildNumberTextView.setText("");
        lastUpdatedTextView.setText("");
        hokkienCountTextView.setText("Hokkien" + threeTabs);
        japaneseCountTextView.setText("Japanese" + threeTabs);
        mandarinCountTextView.setText("Mandarin" + threeTabs);

        return view;
    }
}
