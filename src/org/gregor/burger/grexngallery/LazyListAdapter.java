package org.gregor.burger.grexngallery;

import java.util.LinkedList;
import java.util.Queue;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class LazyListAdapter extends BaseAdapter {

	static final Uri URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	static final String[] projection = { 
		MediaStore.Images.ImageColumns._ID, 
		MediaStore.Images.ImageColumns.DATE_TAKEN };
	private ContentResolver cr;
	private Cursor c;
	private Context context;

	private Handler handler = new Handler();
	private ThumbLoader loader;

	private class ThumbLoader implements Runnable {
		private Queue<Pair<Integer, ImageView>> q = new LinkedList<Pair<Integer,ImageView>>();
		public boolean running = true;
		
		public void enqueue(Pair<Integer, ImageView> item) {
			synchronized (q) {
				q.add(item);
				q.notify();
			}
		}
		
		@Override
		public void run() {
			while (running) {
				synchronized (q) {
					if (q.isEmpty())
						try {
							q.wait();
						} catch (InterruptedException e) {
							Log.e(this.toString(), e.getMessage());
						}
				}
				Pair<Integer, ImageView> item = q.remove();
				c.moveToPosition(item.first);
				long id = c.getLong(0);
				Bitmap bm = Thumbnails.getThumbnail(cr, id, Thumbnails.MICRO_KIND, null);
				if (bm == null)
					Log.e(this.toString(), "could not get thumbnail");
				handler.post(new ImageSetter(item.second, bm));
			}
		}
	}

	private class ImageSetter implements Runnable { 
		ImageView view;
		Bitmap bm;
		
		public ImageSetter(ImageView view, Bitmap bm) {
			super();
			this.view = view;
			this.bm = bm;
		}

		@Override
		public void run() {
			view.setImageBitmap(bm);
		}
	}

	public LazyListAdapter(Context context, ContentResolver cr) {
		this.cr = cr;
		this.context = context;
		c = cr.query(URI, projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN);
		loader = new ThumbLoader();
		new Thread(loader).start();
	}

	@Override
	public int getCount() {
		return c.getCount();
	}

	@Override
	public Object getItem(int i) {
		return 0;
	}

	@Override
	public long getItemId(int position) {
		Log.v(this.toString(), "getItemId("+position+")");
		c.moveToPosition(position);
		return c.getLong(0);
	}

	@Override
	public View getView(int id, View convert, ViewGroup arg2) {
		ImageView iv = (ImageView) (convert != null ? convert : new ImageView(context));
		iv.setImageBitmap(null);
		iv.setMaxHeight(96);
		iv.setMaxWidth(96);
		iv.setMinimumHeight(96);
		iv.setMinimumWidth(96);
		loader.enqueue(Pair.create(id, iv));
		return iv;
	}
}
