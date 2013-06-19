package edu.uml.cs.isense.carphysicsv2;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import edu.uml.cs.isense.R;
import edu.uml.cs.isense.comm.RestAPI;

public class DataActivity extends Activity {

	private Button iSENSE_Button;
	private Button discard_Button;
	private int len, len2;
	private ProgressDialog dia;
	private RestAPI rapi;
	private String dateString;
	private Location loc;
	private String nameOfSession;
	private String firstName;
	private String lastInitial;
	private JSONArray dataSet;
	
	public void onCreate(Bundle savedInstanceBundle){
		super.onCreate(savedInstanceBundle);
		setContentView(R.layout.upload_or_trash);
		
		iSENSE_Button = (Button) findViewById(R.id.iSENSE_Button);
		discard_Button = (Button) findViewById(R.id.discard_Button);
		
		len = getIntent().getExtras().getInt("len");
		len2 = getIntent().getExtras().getInt("len2");
		dataSet = CarRampPhysicsV2.dataSet;
		firstName = getIntent().getExtras().getString("First Name");
		lastInitial = getIntent().getExtras().getString("Last Initial");
		loc = CarRampPhysicsV2.loc;
		
		iSENSE_Button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				rapi = RestAPI.getInstance();
				if (len == 0 || len2 == 0)
					Toast.makeText(DataActivity.this,
							"There are no data to upload!",
							Toast.LENGTH_LONG).show();

				else {
					if (rapi.isConnectedToInternet()){
						new Task().execute();
					}
					else
						Toast.makeText(
								DataActivity.this,
								"No connection to Internet found!  Please enable wifi/mobile connectivity.",
								Toast.LENGTH_LONG).show();
				}
				
				
			}
		});
		
		discard_Button.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Toast.makeText(DataActivity.this,
						"Data thrown away!",
						Toast.LENGTH_LONG).show();
				finish();
			}
		});
	}
	
	private Runnable uploader = new Runnable() {

		@Override
		public void run() {

			int sessionId = -1;
			String city = "", state = "", country = "";
			List<Address> address = null;

			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy, HH:mm:ss", Locale.ENGLISH);
			Date dt = new Date();
			dateString = sdf.format(dt);

			try {
				if (loc != null) {
					address = new Geocoder(DataActivity.this,
							Locale.getDefault()).getFromLocation(
							loc.getLatitude(), loc.getLongitude(), 1);
					if (address.size() > 0) {
						city = address.get(0).getLocality();
						state = address.get(0).getAdminArea();
						country = address.get(0).getCountryName();

					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			nameOfSession = firstName + " " + lastInitial + ". - " + dateString;

			String experimentNumber = new String();
			experimentNumber = CarRampPhysicsV2.experimentNumber;
			if (experimentNumber == null) {
				Log.d("Lolcat", "String is still null bro.");
			} else {
				Log.d("Lolcat", "Fixed da string problemz!");
			}
			if (address.size() <= 0 || address == null) {
				sessionId = rapi.createSession(experimentNumber , nameOfSession
						+ " (location not found)",
						"Automated Submission Through Android App", "", "", "");
			} else if (firstName.equals("") || lastInitial.equals("")) {
				sessionId = rapi.createSession(experimentNumber,
						"No Name Provided - " + dateString,
						"Automated Submission Through Android App", "", city
								+ ", " + state, country);
			} else {
				sessionId = rapi.createSession(experimentNumber, nameOfSession,
						"Automated Submission Through Android App", "", city
								+ ", " + state, country);
			}

			CarRampPhysicsV2.sessionUrl = CarRampPhysicsV2.baseSessionUrl + sessionId;

			rapi.putSessionData(sessionId, experimentNumber, dataSet);

		}

	};
	public int mediaCount;
	
	public class Task extends AsyncTask<Void, Integer, Void> {

		@Override
		protected void onPreExecute() {

			dia = new ProgressDialog(DataActivity.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Please wait while your data are uploaded to iSENSE...");
			dia.setCancelable(false);
			dia.show();

		}

		@Override
		protected Void doInBackground(Void... voids) {

			uploader.run();
			publishProgress(100);
			return null;

		}

		@Override
		protected void onPostExecute(Void voids) {

			dia.setMessage("Done");
			dia.dismiss();

			len = 0;
			len2 = 0;
			mediaCount = 0;

			Toast.makeText(DataActivity.this, "Data upload successful.",
					Toast.LENGTH_SHORT).show();
			
			finish();

		}
	}

}