/*
 * Alexandre Maros - D14128553
 * Dublin Institute of Technology
 * 2015
 */

package pridexs.roomie;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class HouseInfoAdapter extends CursorAdapter {

    public HouseInfoAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.house_info_row, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView houseMemberName = (TextView) view.findViewById(R.id.house_info_member_name);
        TextView isAdminText = (TextView) view.findViewById(R.id.house_info_is_admin);

        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
        int isAdmin = cursor.getInt(cursor.getColumnIndexOrThrow("isAdmin"));

        if (isAdmin == 1) {
            isAdminText.setText("Admin");
        }
        houseMemberName.setText(name);
    }
}
