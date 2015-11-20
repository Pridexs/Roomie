package pridexs.roomie;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

public class NewNoteActivity extends AppCompatActivity {

    TextView mNoteDescriptionView;
    DBManager mDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button addNote = (Button) findViewById(R.id.new_note_add);
        Button cancelNote = (Button) findViewById(R.id.new_note_cancel);
        mNoteDescriptionView = (TextView) findViewById(R.id.note_description_text);
        mDB = DBManager.getInstance(this);

        addNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AppController.getInstance().isNetworkAvailable()) {
                    addNote();
                } else {
                    Toast.makeText(getApplicationContext(), "No Network Connection.", Toast.LENGTH_LONG).show();
                }
            }
        });

        cancelNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    private void addNote() {
        HashMap<String, String> user = new HashMap<>();
        try {
            mDB.open();
            user = mDB.getUserDetails();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        final String email = user.get("email");
        final String api_key = user.get("api_key");
        final String noteDescription = mNoteDescriptionView.getText().toString();

        String tag_string_req = "req_new_note";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_ADD_NOTE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        mDB.open();
                        JSONObject jNote        = jObj.getJSONObject("note");
                        int noteId              = jNote.getInt("id");
                        int houseId             = jNote.getInt("houseId");
                        String noteName         = jNote.getString("name");
                        String noteCreatedAt    = jNote.getString("created_at");
                        String noteLastUpdated  = jNote.getString("last_updated");
                        mDB.addNote(noteId, noteName, noteDescription, email, noteCreatedAt,
                                noteLastUpdated, houseId);
                        Toast.makeText(getApplicationContext(), "Note added!", Toast.LENGTH_LONG).show();
                        finish();

                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("api_key", api_key);
                params.put("note_description", noteDescription);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

}
