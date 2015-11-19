package pridexs.roomie;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.HashMap;


public class NotesFragment extends android.support.v4.app.Fragment
                                implements NoteDialogFragment.NoteDialogListener {

    TextView        mWarning;
    ListView        mNotes;
    NotesAdapter    mAdapter;
    DBManager       mDB;

    HashMap<String, String> mSelectedNote;

    private OnFragmentInteractionListener mListener;

    public static NotesFragment newInstance() {
        NotesFragment fragment = new NotesFragment();
        return fragment;
    }

    public NotesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectedNote = new HashMap<>();
        mDB = DBManager.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notes, container, false);

        mNotes = (ListView) rootView.findViewById(R.id.notes_list_view);
        mWarning = (TextView) rootView.findViewById(R.id.notes_warning);

        try {
            mDB.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Cursor notesCursor = mDB.getCursorNotes();
        if (notesCursor.moveToFirst())
        {
            mAdapter = new NotesAdapter(getActivity(), notesCursor, 0);
            mNotes.setAdapter(mAdapter);

            mNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Cursor cursor = (Cursor) mNotes.getItemAtPosition(position);
                    String createdBy = cursor.getString(cursor.getColumnIndexOrThrow("memberName"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    String lastUpdated = cursor.getString(cursor.getColumnIndexOrThrow("last_updated"));
                    Intent i = new Intent(getActivity(), DisplayNoteActivity.class);
                    Bundle b = new Bundle();
                    b.putString("memberName", createdBy);
                    b.putString("description", description);
                    b.putString("last_updated", lastUpdated);
                    i.putExtras(b);
                    startActivity(i);
                }
            });

            mNotes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Cursor cursor = (Cursor) mNotes.getItemAtPosition(position);
                    int noteId = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
                    String createdBy = cursor.getString(cursor.getColumnIndexOrThrow("memberName"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    String lastUpdated = cursor.getString(cursor.getColumnIndexOrThrow("last_updated"));
                    DialogFragment dialog = new NoteDialogFragment();
                    mSelectedNote.clear();
                    mSelectedNote.put("noteId", Integer.toString(noteId));
                    mSelectedNote.put("description", description);
                    mSelectedNote.put("memberName", createdBy);
                    mSelectedNote.put("last_updated", lastUpdated);
                    dialog.setTargetFragment(NotesFragment.this, 0);
                    dialog.show(getActivity().getSupportFragmentManager(), "note_options");

                    return true;
                }
            });



        } else {
            mNotes.setVisibility(View.GONE);
            mWarning.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void updateCursor() {
        try {
            mDB.open();
            mAdapter.changeCursor(mDB.getCursorNotes());
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDialogEditClick(DialogFragment dialog) {
        Toast.makeText(getActivity().getApplicationContext(),
                "Clicked Edit",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDialogDeleteClick(DialogFragment dialog) {
        Toast.makeText(getActivity().getApplicationContext(),
                "Clicked Delete",
                Toast.LENGTH_LONG).show();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
