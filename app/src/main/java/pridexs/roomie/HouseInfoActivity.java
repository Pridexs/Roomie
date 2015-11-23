/*
 * Alexandre Maros - D14128553
 * Dublin Institute of Technology
 * 2015
 */

package pridexs.roomie;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;

public class HouseInfoActivity extends AppCompatActivity  {

    private DBManager       mDB;
    private SessionManager  mSession;
    private TextView        mHouseName;
    private TextView        mHouseId;
    private Button          mChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDB         = DBManager.getInstance(this);
        mSession    = new SessionManager(this);
        mHouseName  = (TextView) findViewById(R.id.house_info_name);
        mHouseId    = (TextView) findViewById(R.id.house_info_id);
        mChangePassword = (Button) findViewById(R.id.house_info_password_button);

        mChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> user = new HashMap<>();
                try {
                    mDB.open();
                    user = mDB.getUserDetails();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                final String email = user.get("email");

                if (mDB.isUserAdmin(email)) {
                    if (AppController.getInstance().isNetworkAvailable()) {
                        DialogFragment dialog = new HousePasswordDialog();
                        dialog.show(getSupportFragmentManager(), "house_pass");
                    } else {
                        Toast.makeText(getApplicationContext(), "No Network Connection.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(HouseInfoActivity.this, "You are not an admin!", Toast.LENGTH_LONG).show();
                }
            }
        });

        loadHouseInfo();

    }

    private void loadHouseInfo() {
        try {
            mDB.open();

            HashMap<String, String> house = mDB.getHouseDetails();

            String houseName = house.get("name");
            String houseId = house.get("houseID");

            mHouseName.setText(houseName);
            mHouseId.setText(houseId);

            ListView membersList = (ListView) findViewById(R.id.members_list_view);
            HouseInfoAdapter adapter = new HouseInfoAdapter(this, mDB.getCursorHouseMembers(), 0);
            membersList.setAdapter(adapter);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
