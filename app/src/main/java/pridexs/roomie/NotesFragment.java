/*
 * Alexandre Maros - D14128553
 * Dublin Institute of Technology
 * 2015
 */

package pridexs.roomie;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/*
 * Fragment that handles all the Note tab.
 */
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

    @Override
    public void onResume() {
        super.onResume();
        updateCursor();
    }

    public void updateCursor() {
        try {
            mDB.open();
            Cursor c = mDB.getCursorNotes();
            if (c.moveToFirst()) {
                mAdapter.changeCursor(c);
                mNotes.setVisibility(View.VISIBLE);
                mWarning.setVisibility(View.GONE);
            } else {
                mNotes.setVisibility(View.GONE);
                mWarning.setVisibility(View.VISIBLE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDialogEditClick(DialogFragment dialog) {
        HashMap<String, String> user = new HashMap<>();
        try {
            mDB.open();
            user = mDB.getUserDetails();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        final String email = user.get("email");

        if (mDB.isUserAdmin(email)) {
            int noteId = Integer.parseInt(mSelectedNote.get("noteId"));
            String description = mSelectedNote.get("description");
            Intent intent = new Intent(getActivity(), EditNoteActivity.class);
            Bundle extras = new Bundle();
            extras.putInt("noteId", noteId);
            extras.putString("description", description);
            intent.putExtras(extras);
            startActivity(intent);
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "You are not an admin!", Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void onDialogDeleteClick(DialogFragment dialog) {
        HashMap<String, String> user = new HashMap<>();
        try {
            mDB.open();
            user = mDB.getUserDetails();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        final String email = user.get("email");

        if (mDB.isUserAdmin(email)) {

            int noteId = Integer.parseInt(mSelectedNote.get("noteId"));
            if (AppController.getInstance().isNetworkAvailable()) {
                deleteNote(noteId);
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "No Network Connection.", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "You are not an admin!", Toast.LENGTH_LONG).show();
        }

    }

    public void deleteNote(final int noteId) {
        HashMap<String, String> user = new HashMap<>();
        try {
            mDB.open();
            user = mDB.getUserDetails();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        final String email = user.get("email");
        final String api_key = user.get("api_key");

        // Tag used to cancel the request
        String tag_string_req = "req_home_activity";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_DELETE_NOTE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        boolean valid_house = jObj.getBoolean("valid_house");
                        mDB.open();
                        if (valid_house) {
                            // It is safe to delete the note
                            mDB.deleteNote(noteId);
                            updateCursor();
                            Toast.makeText(getActivity().getApplicationContext(), "Note deleted.", Toast.LENGTH_SHORT).show();
                        } else {
                            mDB.deleteHouse();
                            Intent intent = new Intent(getActivity(), NoHouseActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getActivity().getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getActivity().getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity().getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("api_key", api_key);
                params.put("note_id", Integer.toString(noteId));

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
