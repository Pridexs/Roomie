package pridexs.roomie;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.sql.SQLException;


public class NotesFragment extends android.support.v4.app.Fragment {

    TextView        mWarning;
    ListView        mNotes;
    NotesAdapter    mAdapter;
    DBManager       mDB;


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


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
