package pridexs.roomie;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import java.sql.SQLException;

public class NoHouseActivity extends Activity {

    private DBManager mDB;
    private SessionManager mSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_house);

        Button createHouseButton = (Button) findViewById(R.id.buttonCreateHouse);
        Button joinHouseButton = (Button) findViewById(R.id.buttonJoinHouse);
        Button logoutButton = (Button) findViewById(R.id.no_house_logout);

        mDB = DBManager.getInstance(this);
        mSession = new SessionManager(this);

        createHouseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NoHouseActivity.this, RegisterHouseActivity.class);
                startActivity(intent);
                finish();
            }
        });

        joinHouseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NoHouseActivity.this, JoinHouseActivity.class);
                startActivity(intent);
                finish();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutUser();
                Intent intent = new Intent(NoHouseActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

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

