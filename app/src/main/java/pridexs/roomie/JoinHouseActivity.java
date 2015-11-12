package pridexs.roomie;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JoinHouseActivity extends Activity {

    DBManager mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_house);

        mDB = DBManager.getInstance(this);
        Button joinHouseButton = (Button) findViewById(R.id.joinHouseButton);

        joinHouseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText idView = (EditText) findViewById(R.id.text_id_join);
                EditText passwordView = (EditText) findViewById(R.id.text_password_join);

                int houseId = Integer.parseInt(idView.getText().toString());
                String housePass = passwordView.getText().toString().trim();

                joinHouse(houseId, housePass);
            }
        });
    }

    @Override
    public void onBackPressed()  {
        Intent intent = new Intent(JoinHouseActivity.this, NoHouseActivity.class);
        startActivity(intent);
        finish();
    }

    private void joinHouse(final int houseId, final String housePassword) {
        HashMap<String, String> user = new HashMap<>();
        try {
            mDB.open();
            user = mDB.getUserDetails();
            mDB.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        final String email = user.get("email");
        final String api_key = user.get("api_key");

        String tag_string_req = "req_join_house";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_JOIN_HOUSE, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        mDB.open();
                        JSONObject jHouse   = jObj.getJSONObject("house");
                        int house_id        = jHouse.getInt("house_id");
                        String house_name   = jHouse.getString("house_name");
                        mDB.addHouse(house_id, house_name);

                        JSONObject jMembers = jObj.getJSONObject("members");
                        Iterator<?> keys = jMembers.keys();
                        while (keys.hasNext()) {
                            String key = (String)keys.next();
                            if ( jMembers.get(key) instanceof JSONObject ) {
                                JSONObject jMem = jMembers.getJSONObject(key);
                                String memberEmail  = jMem.getString("email");
                                int isAdmin         = jMem.getInt("isAdmin");
                                mDB.addHouseMember(house_id, memberEmail, isAdmin);
                            }
                        }

                        mDB.close();

                        Intent intent = new Intent(JoinHouseActivity.this, HomeActivity.class);
                        startActivity(intent);
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
                Log.e("JOINHOUSEACTIVITY", "Login Error: " + error.getMessage());
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
                params.put("house_id", Integer.toString(houseId));
                params.put("house_password", housePassword);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}

