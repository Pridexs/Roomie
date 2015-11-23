/*
 * Alexandre Maros - D14128553
 * Dublin Institute of Technology
 * 2015
 */

package pridexs.roomie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
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

public class SettingsActivity extends AppCompatActivity {

    private DBManager mDB;
    private SessionManager mSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDB = DBManager.getInstance(this);
        mSession = new SessionManager(this);

        Button leaveHouseButton = (Button) findViewById(R.id.leave_house);
        leaveHouseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppController.getInstance().isNetworkAvailable()) {
                    leaveHouse();
                } else {
                    Toast.makeText(getApplicationContext(), "No Network Connection.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void leaveHouse() {
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
                AppConfig.URL_LEAVE_HOUSE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        mDB.open();

                        logoutUser();
                        Intent intent = new Intent(SettingsActivity.this, NoHouseActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();

                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("api_key", api_key);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void logoutUser() {
        mSession.setLogin(false, "none");
        try {
            mDB.open();
            mDB.deleteUsers();
            mDB.deleteHouse();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
