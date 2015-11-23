/*
 * Alexandre Maros - D14128553
 * Dublin Institute of Technology
 * 2015
 */

package pridexs.roomie;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
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
 * Activity to edit a specific note.
 */
public class EditNoteActivity extends AppCompatActivity {

    private int mNoteId;
    private String mNoteDescription;
    private TextView mNoteDescriptionView;
    private DBManager mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        mNoteId = extras.getInt("noteId");
        mNoteDescription = extras.getString("description");

        mDB = DBManager.getInstance(this);

        mNoteDescriptionView = (TextView) findViewById(R.id.note_description_text);
        mNoteDescriptionView.setText(mNoteDescription);

        Button editNoteButton = (Button) findViewById(R.id.edit_note_edit);
        Button cancelEdit = (Button) findViewById(R.id.edit_note_cancel);

        editNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppController.getInstance().isNetworkAvailable()) {
                    editNote();
                } else {
                    Toast.makeText(getApplicationContext(), "No Network Connection.", Toast.LENGTH_LONG).show();
                }
            }
        });

        cancelEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void editNote() {
        final int noteId = mNoteId;
        final String description = mNoteDescriptionView.getText().toString();

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
        String tag_string_req = "req_edit_note";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_EDIT_NOTE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        mDB.open();

                        JSONObject jNote = jObj.getJSONObject("note");
                        String last_updated = jNote.getString("last_updated");
                        String name = jNote.getString("name");

                        mDB.updateNote(noteId, name, description, last_updated);
                        Toast.makeText(getApplicationContext(), "Note edited.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("api_key", api_key);
                params.put("note_id", Integer.toString(noteId));
                params.put("description", description);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

}
