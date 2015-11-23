/*
 * Alexandre Maros - D14128553
 * Dublin Institute of Technology
 * 2015
 */

package pridexs.roomie;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
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

public class HousePasswordDialog extends android.support.v4.app.DialogFragment {

    private DBManager mDB;
    private HouseInfoActivity mActivity;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mActivity = (HouseInfoActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_change_h_password, null))
                // Add action buttons
                .setPositiveButton(R.string.action_change_password, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mDB = DBManager.getInstance(mActivity);
                        EditText pass =  (EditText) getDialog().findViewById(R.id.house_new_password);
                        String password = pass.getText().toString();
                        changePassword(password);
                    }
                })
                .setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        HousePasswordDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

    private void changePassword(final String password) {
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
        String tag_string_req = "req_new_pass";

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_CHANGE_HOUSE_PASS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        Toast.makeText(mActivity.getApplicationContext(), "Password changed!", Toast.LENGTH_LONG).show();
                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getDialog().getContext().getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(mActivity.getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mActivity.getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("api_key", api_key);
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

}
