/*
 * Alexandre Maros - D14128553
 * Dublin Institute of Technology
 * 2015
 */

package pridexs.roomie;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
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
import java.util.Map;

/*
 * Activity to Register a House
 */
public class RegisterHouseActivity extends Activity {

    private DBManager mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_house);

        mDB = DBManager.getInstance(this);
        Button registerHouseButton = (Button) findViewById(R.id.register_house_button);

        registerHouseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(AppController.getInstance().isNetworkAvailable()) {
                    EditText nameView = (EditText) findViewById(R.id.text_house_name);
                    EditText passwordView = (EditText) findViewById(R.id.text_house_password);
                    String houseName = nameView.getText().toString().trim();
                    String housePass = passwordView.getText().toString().trim();

                    registerHouse(houseName, housePass);
                } else {
                    Toast.makeText(getApplicationContext(), "No Network Connection.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /*
     * Overriding the 'back' key since the NoHouseActivity was finished.
     * (If the user sucessfuly registers, he will be brought to the
     * HomeActivity instead)
     * P.s: This could've been done with a flag in the Intent.
     */
    @Override
    public void onBackPressed()  {
        Intent intent = new Intent(RegisterHouseActivity.this, NoHouseActivity.class);
        startActivity(intent);
        finish();
    }

    private void registerHouse(final String houseName, final String housePassword)
    {
        HashMap<String, String> user = new HashMap<>();
        try {
            mDB.open();
            user = mDB.getUserDetails();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        final String email = user.get("email");
        final String api_key = user.get("api_key");


        String tag_string_req = "req_create_house";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_REGISTER_HOUSE, new Response.Listener<String>() {

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
                        String last_updated = jHouse.getString("last_updated");
                        mDB.addHouse(house_id, house_name, last_updated);
                        mDB.addHouseMember(house_id, email, 1);


                        Intent intent = new Intent(RegisterHouseActivity.this, HomeActivity.class);
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
                Log.e("REGISTERHOUSE", "Login Error: " + error.getMessage());
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
                params.put("house_name", houseName);
                params.put("house_password", housePassword);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

}
