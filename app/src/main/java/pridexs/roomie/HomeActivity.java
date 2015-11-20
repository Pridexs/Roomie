package pridexs.roomie;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {


    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private FloatingActionButton    mFab;
    private ImageButton             mRefreshButton;
    private DBManager               mDB;
    private SessionManager          mSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSession = new SessionManager(this);
        if (!mSession.isLoggedIn()) {
            logoutUser();
        }

        mFab = (FloatingActionButton) findViewById(R.id.fab_action_add);
        mDB = DBManager.getInstance(this);
        mRefreshButton = (ImageButton) findViewById(R.id.refresh_house);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                return;
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mFab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(HomeActivity.this, NewNoteActivity.class);
                                startActivity(i);
                            }
                        });
                        break;
                    case 1:
                        mFab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Snackbar.make(view, "Section not yet implemented", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                return;
            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(HomeActivity.this, NewNoteActivity.class);
                startActivity(i);
            }
        });

        updateHouse();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);

        mRefreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateHouse();
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent i;
        //noinspection SimplifiableIfStatement
        switch(id) {
            case R.id.action_settings:
                return true;
            case R.id.action_logout:
                logoutUser();
                i = new Intent(HomeActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
                return true;
            case R.id.action_house_info:
                i = new Intent(HomeActivity.this, HouseInfoActivity.class);
                startActivity(i);
                return true;
        }

        return super.onOptionsItemSelected(item);
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

    private void updateHouse() {
        if (AppController.getInstance().isNetworkAvailable()) {
            HashMap<String, String> user = new HashMap<>();
            HashMap<String, String> house = new HashMap<>();
            try {
                mDB.open();
                user = mDB.getUserDetails();
                house = mDB.getHouseDetails();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            final String email = user.get("email");
            final String api_key = user.get("api_key");
            final String last_updated = user.get("last_updated");


            // Tag used to cancel the request
            String tag_string_req = "req_home_activity";

            StringRequest strReq = new StringRequest(Request.Method.POST,
                    AppConfig.URL_GET_HOUSE_INFO, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");

                        // Check for error node in json
                        if (!error) {

                            // Counters
                            int newNotes = 0;
                            int updatedNotes = 0;
                            int deletedNotes = 0;

                            boolean valid_house = jObj.getBoolean("valid_house");

                            mDB.open();

                            if (valid_house) {
                                int house_id            = jObj.getInt("house_id");
                                String house_name       = jObj.getString("house_name");
                                String h_last_updated   = jObj.getString("last_updated");
                                boolean requires_sync   = jObj.getBoolean("requires_sync");

                                if (requires_sync) {

                                    mDB.updateHouse(house_id, house_name, h_last_updated);
                                    JSONArray jMembers = jObj.getJSONArray("members");
                                    for (int i = 0; i < jMembers.length(); i++) {
                                        JSONObject jMem     = jMembers.getJSONObject(i);
                                        String memberEmail  = jMem.getString("email");
                                        String memberName   = jMem.getString("name");
                                        int isAdmin         = jMem.getInt("isAdmin");
                                        if (mDB.isUserOnDb(memberEmail))
                                        {
                                            mDB.updateHouseMember(memberEmail, isAdmin);
                                            mDB.updateUser(memberEmail, memberName);
                                        } else {
                                            mDB.addHouseMember(house_id, memberEmail, isAdmin);
                                            if (!memberEmail.equals(email)) {
                                                mDB.addUser(memberName, memberEmail);
                                            }
                                        }
                                    }
                                }

                                if (jObj.has("notes")) {
                                    JSONArray jNotes = jObj.getJSONArray("notes");
                                    for (int i = 0; i < jNotes.length(); i++) {
                                        JSONObject jNote    = jNotes.getJSONObject(i);
                                        int noteId          = jNote.getInt("noteID");
                                        int wasDeleted      = jNote.getInt("was_deleted");
                                        String name         = jNote.getString("name");
                                        String description  = jNote.getString("description");
                                        String createdBy    = jNote.getString("createdBy");
                                        String created_at   = jNote.getString("created_at");
                                        String last_updated = jNote.getString("last_updated");
                                        if (mDB.isNoteOnDb(noteId)) {
                                            if (wasDeleted == 1) {
                                                mDB.deleteNote(noteId);
                                                deletedNotes++;
                                            } else {
                                                mDB.updateNote(noteId, name, description, last_updated);
                                                updatedNotes++;
                                            }
                                        } else {
                                            mDB.addNote(noteId, name, description, createdBy, created_at
                                                    , last_updated, house_id);
                                            newNotes++;
                                        }
                                        Toast.makeText(getApplicationContext(),
                                                newNotes + " new, " + updatedNotes + " updated, " + deletedNotes + " deleted.",
                                                Toast.LENGTH_SHORT).show();
                                        NotesFragment frag = (NotesFragment) mSectionsPagerAdapter.getRegisteredFragment(0);
                                        frag.updateCursor();
                                    }
                                    mDB.updateLastUpdated();
                                }
                            } else {
                                mDB.deleteHouse();
                                Intent intent = new Intent(HomeActivity.this, NoHouseActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            String errorMsg = jObj.getString("error_msg");
                            Toast.makeText(getApplicationContext(),
                                    errorMsg, Toast.LENGTH_LONG).show();
                            logoutUser();
                        }
                    } catch (JSONException e) {
                        // JSON error
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        logoutUser();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        logoutUser();
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
                    params.put("last_updated", last_updated);

                    return params;
                }

            };

            // Adding request to request queue
            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        } else {
            Toast.makeText(getApplicationContext(), "No Network Connection.", Toast.LENGTH_LONG).show();
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        //  Code from http://stackoverflow.com/questions/8785221/retrieve-a-fragment-from-a-viewpager
        SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return NotesFragment.newInstance();
                case 1:
                default:
                    return PlaceholderFragment.newInstance();

            }

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "NOTES";
                case 1:
                    return "EXPENSES";
            }
            return null;
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }
    }

    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";


        public static PlaceholderFragment newInstance() {
            PlaceholderFragment fragment = new PlaceholderFragment();

            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_expenses, container, false);
            return rootView;
        }
    }
}
