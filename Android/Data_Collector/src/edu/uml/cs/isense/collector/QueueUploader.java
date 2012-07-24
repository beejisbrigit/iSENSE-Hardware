package edu.uml.cs.isense.collector;

import java.util.LinkedList;
import java.util.Queue;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.uml.cs.isense.collector.objects.DataSet;
import edu.uml.cs.isense.waffle.Waffle;

public class QueueUploader extends Activity implements OnClickListener {

	private static Context mContext;
	private LinearLayout scrollQueue;
	private Runnable sdUploader;
	private ProgressDialog dia;
	private Queue<DataSet> mirrorQueue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.queueprompt);
		super.onCreate(savedInstanceState);

		mContext = this;

		Button upload = (Button) findViewById(R.id.upload);
		upload.setOnClickListener(this);

		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(this);

		scrollQueue = (LinearLayout) findViewById(R.id.scrollqueue);
		fillScrollQueue(scrollQueue);
	}

	// Works through list of data to be uploaded and creates the list of blocks
	private void fillScrollQueue(LinearLayout scrollQueue) {
		String previous = "";

		for (final DataSet ds : DataCollector.uploadQueue) {
			switch (ds.type) {
			case DATA:
				View data = View.inflate(mContext, R.layout.queueblock_data,
						null);

				makeBlock(data, ds);
				previous = checkPrevious(previous, scrollQueue,
						(String) ds.getName());

				scrollQueue.addView(data);
				ds.setUploadable(true);

				data.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						CheckedTextView ctv = (CheckedTextView) v
								.findViewById(R.id.name);
						ctv.toggle();

						if (ctv.isChecked())
							ctv.setCheckMarkDrawable(R.drawable.bluechecksmall);
						else
							ctv.setCheckMarkDrawable(R.drawable.red_x);

						ds.setUploadable(ctv.isChecked());

					}

				});
				break;

			case PIC:
				View pic = View
						.inflate(mContext, R.layout.queueblock_pic, null);

				makeBlock(pic, ds);
				previous = checkPrevious(previous, scrollQueue,
						(String) ds.getName());

				scrollQueue.addView(pic);
				pic.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						CheckedTextView ctv = (CheckedTextView) v
								.findViewById(R.id.name);
						ctv.toggle();

						if (ctv.isChecked())
							ctv.setCheckMarkDrawable(R.drawable.bluechecksmall);
						else
							ctv.setCheckMarkDrawable(R.drawable.red_x);

						ds.setUploadable(ctv.isChecked());

					}

				});
				break;
			}
			Log.d("just filled array", "uploadQueue: "
					+ DataCollector.uploadQueue.size());
		}

	}

	// Adds empty space after experiment groups
	private String checkPrevious(String previous, LinearLayout scrollQueue,
			String ds) {

		LinearLayout space = new LinearLayout(mContext);
		space.setPadding(0, 10, 0, 10);

		if ((!previous.equals(ds)) && (!previous.equals("")))
			scrollQueue.addView(space);

		return ds;
	}

	// Fills the text fields in the list element blocks
	private void makeBlock(View view, DataSet ds) {
		CheckedTextView ctv = (CheckedTextView) view.findViewById(R.id.name);
		ctv.setText(ds.getName() + " - " + ds.getType());

		TextView eid = (TextView) view.findViewById(R.id.experimentid);
		eid.setText(ds.getEID());

		TextView desc = (TextView) view.findViewById(R.id.description);
		desc.setText(ds.getDesc());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.check_layout:

			CheckedTextView ctv = (CheckedTextView) v.findViewById(R.id.name);
			ctv.toggle();

			if (ctv.isChecked())
				ctv.setCheckMarkDrawable(R.drawable.bluechecksmall);
			else
				ctv.setCheckMarkDrawable(R.drawable.red_x);

			break;

		case R.id.upload:
			Log.d("IM HERE!!", "Yay I've arrived!");
			DataCollector.getUploadQueue();

			mirrorQueue = new LinkedList<DataSet>();
			mirrorQueue.addAll(DataCollector.uploadQueue);

			Log.d("Q_SIZE", "SIZE: " + DataCollector.uploadQueue.size());

			new UploadSDTask().execute();
			break;

		case R.id.cancel:
			setResult(RESULT_CANCELED);
			finish();
			break;
		}
	}

	// Control task for uploading data from SD card
	class UploadSDTask extends AsyncTask<Void, Integer, Void> {
		@Override
		protected void onPreExecute() {
			DataSet ds = mirrorQueue.remove();
			createRunnable(ds);
			Log.d("UPLOADING :)", "calling upload task");
			dia = new ProgressDialog(QueueUploader.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Please wait while your data are uploaded to iSENSE...");
			dia.setCancelable(false);
			dia.show();
		}

		@Override
		protected Void doInBackground(Void... voids) {
			sdUploader.run();
			dia.setProgress(100);
			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {
			Waffle w = new Waffle(QueueUploader.mContext);
			w.make("Upload Success", Toast.LENGTH_SHORT, "check");
			dia.dismiss();

			if (mirrorQueue.isEmpty()) {
				setResult(RESULT_OK);
				finish();
				return;
			} else
				continueUploading();
		}
	}

	void createRunnable(final DataSet ds) {
		sdUploader = new Runnable() {

			@Override
			public void run() {
				if (ds.isUploadable()) {
					boolean success = ds.upload();
					if (success)
						DataCollector.uploadQueue.remove(ds);
				}
			}

		};

	}

	private void continueUploading() {
		new UploadSDTask().execute();
	}

}