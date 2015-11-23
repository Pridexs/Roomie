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

import org.w3c.dom.Text;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * Adaptar for the Notes ListView.
 */
public class NotesAdapter extends CursorAdapter {

    public NotesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.notes_row, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView houseMemberName = (TextView) view.findViewById(R.id.note_member_name);
        TextView noteDescription = (TextView) view.findViewById(R.id.note_description);
        TextView noteDate = (TextView) view.findViewById(R.id.note_date);

        String name = cursor.getString(cursor.getColumnIndexOrThrow("memberName"));
        String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
        String date = cursor.getString(cursor.getColumnIndexOrThrow("last_updated"));
        date = date.substring(0,10);

        DateFormat formatter = new SimpleDateFormat("yyyy-MM-DD");
        try {
            Date myDate = (Date) formatter.parse(date);
            SimpleDateFormat newFormat = new SimpleDateFormat("DD-MM-yyyy");
            String finalDate = newFormat.format(myDate);
            noteDate.setText(finalDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        houseMemberName.setText(name);
        noteDescription.setText(description);
    }
}
