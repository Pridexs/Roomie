/*
 * Alexandre Maros - D14128553
 * Dublin Institute of Technology
 * 2015
 */

package pridexs.roomie;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/*
 * Manages a session. Checks if the user is logged in or not.
 */
public class SessionManager {
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;
    Editor editor;
    Context _context;

    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "RoomieLogin";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    private static final String KEY_EMAIL = "email";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn, String email) {
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        editor.putString(KEY_EMAIL, email);
        editor.commit();
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }

    public String getLogin() {
        return pref.getString(KEY_EMAIL, "none");
    }
}