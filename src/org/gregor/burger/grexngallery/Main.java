package org.gregor.burger.grexngallery;

import android.app.Activity;
import android.content.ContentResolver;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class Main extends Activity implements OnItemClickListener {
    /** Called when the activity is first created. */
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ContentResolver cr = getContentResolver();
        GridView lv = (GridView) findViewById(R.id.list);
        
        lv.setFastScrollEnabled(true);
        lv.setAdapter(new LazyListAdapter(getApplicationContext(), cr));
        lv.setOnItemClickListener(this);
    }

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long id) {
		Log.v(this.toString(), "item " + id + " selected");
	}
}