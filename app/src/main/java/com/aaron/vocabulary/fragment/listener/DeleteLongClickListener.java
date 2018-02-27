package com.aaron.vocabulary.fragment.listener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;

import com.aaron.vocabulary.fragment.AboutFragment;
import com.aaron.vocabulary.function.Action;
import com.aaron.vocabulary.model.LogsManager;
import com.aaron.vocabulary.model.VocabularyManager;

import java.lang.ref.WeakReference;

/**
 * Handles the deleting of all Vocabularies upon long click/press.
 */
public class DeleteLongClickListener implements View.OnLongClickListener
{
    private VocabularyManager vocabularyManager;
    private WeakReference<AboutFragment> fragmentRef;
    private Action postDeleteAction;

    /**
     * Default Constructor.
     *
     * @param fragmentRef the fragment where the delete event is
     * @param vocabularyManager the model that handles the deletion of vocabularies
     * @param postDeleteAction the function that executes after the delete, which is to navigate to the next activity
     */
    public DeleteLongClickListener(final AboutFragment fragmentRef, VocabularyManager vocabularyManager, Action postDeleteAction)
    {
        this.fragmentRef = new WeakReference<>(fragmentRef);
        this.vocabularyManager = vocabularyManager;
        this.postDeleteAction = postDeleteAction;
    }

    /**
     * Pops-up a prompt dialog with 'yes' or 'no' button. Selecting 'yes' will delete all vocabularies from disk.
     */
    @Override
    public boolean onLongClick(View view)
    {
        LogsManager.log(AboutFragment.CLASS_NAME, "onLongClick", "Delete vocabularies attempted.");

        final AboutFragment fragment = this.fragmentRef.get();

        if(fragment != null)
        {
            AlertDialog.Builder prompt = new AlertDialog.Builder(fragment.getActivity());
            prompt.setMessage("Delete vocabularies from disk?");

            prompt.setPositiveButton("Yes", this::yesButtonAction);
            prompt.setNegativeButton("No", this::noButtonAction);

            prompt.create().show();
        }

        return true;
    }

    private void yesButtonAction(DialogInterface dialog, int id)
    {
        logDialogAction("Yes");

        vocabularyManager.deleteVocabulariesFromDisk();
        postDeleteAction.execute();
    }

    private void noButtonAction(DialogInterface dialog, int id)
    {
        logDialogAction("No");

        dialog.cancel();
    }

    private void logDialogAction(String action)
    {
        LogsManager.log(AboutFragment.CLASS_NAME, "promptUserOnDelete", action + "  selected.");
    }
}