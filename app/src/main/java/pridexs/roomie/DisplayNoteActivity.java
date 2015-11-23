/*
 * Alexandre Maros - D14128553
 * Dublin Institute of Technology
 * 2015
 */

package pridexs.roomie;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

/*
 * Simple activity just to display notes with more information.
 */
public class DisplayNoteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView createdByTextView = (TextView) findViewById(R.id.display_note_created_by);
        TextView lastUpdatedTextView = (TextView) findViewById(R.id.display_note_last_updated);
        TextView noteDescriptionTextView = (TextView) findViewById(R.id.display_note_description);

        Bundle b = getIntent().getExtras();

        createdByTextView.setText(b.getString("memberName"));
        lastUpdatedTextView.setText(b.getString("last_updated"));
        noteDescriptionTextView.setText(b.getString("description"));
    }

}
