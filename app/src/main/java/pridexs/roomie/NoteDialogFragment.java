/*
 * Alexandre Maros - D14128553
 * Dublin Institute of Technology
 * 2015
 */

package pridexs.roomie;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/*
 * Creates a dialog asking if the user wants to edit or remove a specific Note.
 */
public class NoteDialogFragment extends DialogFragment {

    // Implementation taken from the official Android website
    // http://developer.android.com/guide/topics/ui/dialogs.html

    public interface NoteDialogListener {
        public void onDialogEditClick(DialogFragment dialog);
        public void onDialogDeleteClick(DialogFragment dialog);
    }

    NoteDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (NoteDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.pick_option)
                .setItems(R.array.notes_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which) {
                            case 0:
                                mListener.onDialogEditClick(NoteDialogFragment.this);
                                break;
                            case 1:
                            default:
                                mListener.onDialogDeleteClick(NoteDialogFragment.this);
                        }
                    }
                });
        return builder.create();
    }

}
