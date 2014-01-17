/***************************************************************************************************/
/***************************************************************************************************/
/**                                                                                               **/
/**      IIIIIIIIIIIII            General Purpose Amusement Park Application      SSSSSSSSS       **/
/**           III                                                               SSS               **/
/**           III                    Original Creator: John Fertita            SSS                **/
/**           III                    Optimized By:     Jeremy Poulin,           SSS               **/
/**           III                                      Michael Stowell           SSSSSSSSS        **/
/**           III                    Faculty Advisor:  Fred Martin                      SSS       **/
/**           III                    Special Thanks:   Don Rhine                         SSS      **/
/**           III                    Group:            ECG, iSENSE                      SSS       **/
/**      IIIIIIIIIIIII               Property:         UMass Lowell              SSSSSSSSS        **/
/**                                                                                               **/
/***************************************************************************************************/
/***************************************************************************************************/

package edu.uml.cs.isense.canobiev2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.credentials.Login;
import edu.uml.cs.isense.dfm.DataFieldManager;
import edu.uml.cs.isense.dfm.Fields;
import edu.uml.cs.isense.proj.Setup;
import edu.uml.cs.isense.queue.QDataSet;
import edu.uml.cs.isense.queue.QueueLayout;
import edu.uml.cs.isense.queue.UploadQueue;
import edu.uml.cs.isense.supplements.ObscuredSharedPreferences;
import edu.uml.cs.isense.sync.SyncTime;
import edu.uml.cs.isense.waffle.Waffle;

public class AmusementPark extends Activity implements SensorEventListener,
		LocationListener {

	/* Default Constants */
	private final String DEFAULT_USERNAME = "mobile.fake@example.com";
	private final String DEFAULT_PASSWORD = "mobile";
	private final String ACTIVITY_NAME = "canobielake";
	private final String TIME_OFFSET_PREFS_ID = "time_offset";
	private final String TIME_OFFSET_KEY = "timeOffset";

	/* UI Handles */
	public static EditText experimentInput;
	public static TextView rideName;
	private TextView time;
	private TextView values;
	private Button startStop;
	public static EditText dataset; 
	public static EditText project;
	public static CheckBox projectLater;
	public static EditText sampleRate;
	public static EditText studentNumber;
	public static CheckBox isCanobie;
	
	/*Values obtained from Configuration*/
	public static int projectNum = -1;
	public static String dataName = "";
	public static String rate = "";
	public static String rideNameString = "NOT SET";
	public static String stNumber = "1";
	public static Boolean projectLaterChecked = false;
	public static Boolean canobieChecked = true;
	
	/* Managers and Their Variables */
	public static DataFieldManager dfm;
	private SensorManager mSensorManager;
	private LocationManager mLocationManager;
	private Location loc;
	private Timer recordingTimer;
	private UploadQueue uq;
	private Vibrator vibrator;

	/* Other Important Objects */
	private LinkedList<String> acceptedFields;
	private Fields f;
	private String dataToBeWrittenToFile;
	private MediaPlayer mMediaPlayer;
	public static ArrayList<File> pictures;
	public static ArrayList<File> videos;
	private API api;
	public static Context mContext;
	private Waffle w;

	/* Work Flow Variables */
	private boolean isRunning = false;
	private boolean uploadSuccessful = false;
	private static boolean useMenu = true;
	public static boolean setupDone = false;

	/* Recording Constants */
	private final int SAMPLE_INTERVAL = 50;

	/* Recording Variables */
	private float rawAccel[];
	private float rawMag[];
	private float accel[];
	private float orientation[];
	private float mag[];
	private String temperature = "";
	private String pressure = "";
	private String light = "";
	private long srate = SAMPLE_INTERVAL;

	/* Menu Items */
	private final int MENU_ITEM_SETUP = 0;
	private final int MENU_ITEM_LOGIN = 1;
	private final int MENU_ITEM_UPLOAD = 2;
	private final int MENU_ITEM_TIME = 3;
	private final int MENU_ITEM_MEDIA = 4;

	/* Start Activity Codes*/
	private final int QUEUE_UPLOAD_REQUESTED = 1;
	private final int CHOOSE_SENSORS_REQUESTED = 3;
	private final int SYNC_TIME_REQUESTED = 4;
	private final int SETUP_REQUESTED = 5;
	private final int LOGIN_REQUESTED = 6;

	private int dataPointCount = 0, elapsedSecs;

	/* Used with Sync Time */
	private long currentTime = 0;
	private long timeOffset = 0;	
	
	/* Used to set time elapsed */
	private String sec = "";
	private int min = 0;




	public static JSONArray dataSet;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		// Think pointer to this activity
		mContext = this;

		// Initialize everything you're going to need
		initVars();

		// Main Layout Button for Recording Data
		startStop.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				if ((!setupDone)) {
					w.make("You must setup before recording data.",
							Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);

					isRunning = false;
					return isRunning;

				} else {

					// Vibrate and Beep
					vibrator.vibrate(300);
					mMediaPlayer.setLooping(false);
					mMediaPlayer.start();

					

					// Stop the recording and reset UI if running
					if (isRunning) {
						isRunning = false;
						useMenu = true;
						setupDone = false;
						

						// Unregister sensors to save battery
						mSensorManager.unregisterListener(AmusementPark.this);

						// Update the main button
						enableMainButton(false);

						// Reset main UI
						time.setText(getResources().getString(
								R.string.timeElapsed));
						rideName.setText("Ride/St#: NOT SET");
						values.setText("X: " + "\nY: " + "\nZ: ");
								

						// Cancel the recording timer
						recordingTimer.cancel();
						
						//Add data to Queue to be uploaded
						new AddToQueueTask().execute(); 
						

						return isRunning;

						// Start the recording
					} else {
						srate = Integer.parseInt(rate);
						
						//new SensorCheckTask().execute();
						
						isRunning = true;

						// Check to see if a valid project was chosen. If not,
						// (projectNum is -1) enable all fields for recording.
						
						if (projectNum == -1) {
							enableAllFields();
						}

						// Create a file so that we can write results to the
						// sdCard
						prepWriteToSDCard(new Date());

						registerSensors();

						dataSet = new JSONArray();
						elapsedSecs = 0;
						dataPointCount = 0;

						currentTime = getUploadTime();

						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							w.make("Data recording interrupted! Time values may be inconsistent.",
									Waffle.LENGTH_SHORT, Waffle.IMAGE_X);
							e.printStackTrace();
						}

						useMenu = false;

						if (mSensorManager != null) {
							mSensorManager
									.registerListener(
											AmusementPark.this,
											mSensorManager
													.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
											SensorManager.SENSOR_DELAY_FASTEST);
							mSensorManager
									.registerListener(
											AmusementPark.this,
											mSensorManager
													.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
											SensorManager.SENSOR_DELAY_FASTEST);
						}

						dataToBeWrittenToFile = "Accel-X, Accel-Y, Accel-Z, Accel-Total, Mag-X, Mag-Y, Mag-Z, Mag-Total"
								+ "Latitude, Longitude, Time\n";
						startStop.setText(getResources().getString(
								R.string.stopString));
						startStop
								.setBackgroundResource(R.drawable.button_rsense_stop);
						startStop.setTextColor(Color.parseColor("#FFFFFF"));
						
						new TimeElapsedTask().execute();
						//TODO
						recordingTimer = new Timer();
						recordingTimer.scheduleAtFixedRate(new TimerTask() {
							public void run() {
								recordData();
							}
						}, srate, srate);
					}
					
					return isRunning;
				}
			}

		});

	}

	private void enableMainButton(boolean enable) {
		if (enable) {

		} else {
			startStop.setText(getResources().getString(R.string.startString));
			startStop.setBackgroundResource(R.drawable.button_rsense);
			startStop.setTextColor(Color.parseColor("#0066FF"));
		}
	}

	/**
	 * Returns a nicely formatted date.
	 * 
	 * @param date
	 *            Date you wish to convert
	 * @return The date in string form: MM/dd/yyyy, HH:mm:ss
	 */
	String getNiceDateString(Date date) {

		SimpleDateFormat niceFormat = new SimpleDateFormat(
				"MM/dd/yyyy, HH:mm:ss", Locale.US);

		return niceFormat.format(date);
	}

	/**
	 * Prepares a file for writing date to the sdCard.
	 * 
	 * @param date
	 *            Time stamp for the file name
	 * @return Newly created file on sdCard
	 */
	private File prepWriteToSDCard(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy--HH-mm-ss",
				Locale.US);

		String dateString = sdf.format(date);

		File folder = new File(Environment.getExternalStorageDirectory()
				+ "/iSENSE");

		if (!folder.exists()) {
			folder.mkdir();
		}
		
		//TODO rides.getSelectedItem()
		String sdFileName = rideName + "-" + stNumber + "-"
				+ dateString + ".csv";
		File sdFile = new File(folder, sdFileName);

		return sdFile;

	}

	/**
	 * Attempts to write out a string to a file.
	 * 
	 * @param data
	 *            The data in string form
	 * @param sdFile
	 *            The file to write the data to
	 * @return True if success, false if failed with exception.
	 */
	private boolean writeToSDCard(String data, File sdFile) {
		try {
			// Prepare the writers
			FileWriter gpxwriter = new FileWriter(sdFile);
			BufferedWriter out = new BufferedWriter(gpxwriter);

			// Write out the data
			out.write(data);

			// Close the output stream
			if (out != null)
				out.close();
			if (gpxwriter != null)
				gpxwriter.close();

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	@Override
	public void onPause() {
		super.onPause();

		// Stop the current sensors to save battery.
		mLocationManager.removeUpdates(AmusementPark.this);
		mSensorManager.unregisterListener(AmusementPark.this);

		// Cancel the recording timer
		if (recordingTimer != null)
			recordingTimer.cancel();
	}

	@Override
	public void onResume() {
		super.onResume();

		// Silently logs in the user to iSENSE
		login(false);

		// Rebuilds the upload queue
		if (uq != null)
			uq.buildQueueFromFile();

	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}
	

	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!useMenu) {
			menu.getItem(MENU_ITEM_SETUP).setEnabled(false);
			menu.getItem(MENU_ITEM_LOGIN).setEnabled(false);
			menu.getItem(MENU_ITEM_UPLOAD).setEnabled(false);
			menu.getItem(MENU_ITEM_TIME).setEnabled(false);
			menu.getItem(MENU_ITEM_MEDIA).setEnabled(false);
		} else {
			menu.getItem(MENU_ITEM_SETUP).setEnabled(true);
			menu.getItem(MENU_ITEM_LOGIN).setEnabled(true);
			menu.getItem(MENU_ITEM_UPLOAD).setEnabled(true);
			menu.getItem(MENU_ITEM_TIME).setEnabled(true);
			menu.getItem(MENU_ITEM_MEDIA).setEnabled(true);
		}
		return true;
	}

	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.MENU_ITEM_SETUP:
			//startStop.setEnabled(false);
			Intent iSetup = new Intent(AmusementPark.this, Configuration.class);
			startActivityForResult(iSetup, SETUP_REQUESTED);
			return true;
		case R.id.MENU_ITEM_LOGIN:
			startActivityForResult(new Intent(getApplicationContext(),
					Login.class), LOGIN_REQUESTED);
			login(true);
			return true;
		case R.id.MENU_ITEM_UPLOAD:
			manageUploadQueue();
			return true;
		case R.id.MENU_ITEM_TIME:
			startActivityForResult(new Intent(getApplicationContext(),
					SyncTime.class), SYNC_TIME_REQUESTED);
			return true;
		case R.id.MENU_ITEM_MEDIA:
			Intent iMedia = new Intent(AmusementPark.this, MediaManager.class);
			startActivity(iMedia);
			return true;
		}
		return false;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	/**
	 * Stores some data into our global objects as quickly as we get new points.
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		DecimalFormat toThou = new DecimalFormat("######0.000");
		DecimalFormat threeDigit = new DecimalFormat("#,##0.000");
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			if (dfm.enabledFields[Fields.ACCEL_X]
					|| dfm.enabledFields[Fields.ACCEL_Y]
					|| dfm.enabledFields[Fields.ACCEL_Z]
					|| dfm.enabledFields[Fields.ACCEL_TOTAL]) {

				rawAccel = event.values.clone();
				accel[0] = event.values[0];
				accel[1] = event.values[1];
				accel[2] = event.values[2];

				String xPrepend = accel[0] > 0 ? "+" : "";
				String yPrepend = accel[1] > 0 ? "+" : "";
				String zPrepend = accel[2] > 0 ? "+" : "";

				if (isRunning) {
					values.setText("X: " + xPrepend
							+ threeDigit.format(accel[0]) + "\nY: " + yPrepend
							+ threeDigit.format(accel[1]) + "\nZ: " + zPrepend
							+ threeDigit.format(accel[2]));
				}

				accel[3] = (float) Math.sqrt((float) ((Math.pow(accel[0], 2)
						+ Math.pow(accel[1], 2) + Math.pow(accel[2], 2))));

			}

		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			if (dfm.enabledFields[Fields.MAG_X]
					|| dfm.enabledFields[Fields.MAG_Y]
					|| dfm.enabledFields[Fields.MAG_Z]
					|| dfm.enabledFields[Fields.MAG_TOTAL]
					|| dfm.enabledFields[Fields.HEADING_DEG]
					|| dfm.enabledFields[Fields.HEADING_RAD]) {

				rawMag = event.values.clone();

				mag[0] = event.values[0];
				mag[1] = event.values[1];
				mag[2] = event.values[2];

				float rotation[] = new float[9];

				if (SensorManager.getRotationMatrix(rotation, null, rawAccel,
						rawMag)) {
					orientation = new float[3];
					SensorManager.getOrientation(rotation, orientation);
				}
			}

		} else if (event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
			if (dfm.enabledFields[Fields.TEMPERATURE_C]
					|| dfm.enabledFields[Fields.TEMPERATURE_F]
					|| dfm.enabledFields[Fields.TEMPERATURE_K])
				temperature = toThou.format(event.values[0]);
		} else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
			if (dfm.enabledFields[Fields.PRESSURE])
				pressure = toThou.format(event.values[0]);
		} else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
			if (dfm.enabledFields[Fields.LIGHT])
				light = toThou.format(event.values[0]);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		loc = location;
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	// Performs tasks after returning to main UI from previous activities
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == SYNC_TIME_REQUESTED) {
			if (resultCode == RESULT_OK) {
				timeOffset = data.getExtras().getLong("offset");
				SharedPreferences mPrefs = getSharedPreferences(
						TIME_OFFSET_PREFS_ID, 0);
				SharedPreferences.Editor mEditor = mPrefs.edit();
				mEditor.putLong(TIME_OFFSET_KEY, timeOffset);
				mEditor.commit();
			}
		} else if (requestCode == QUEUE_UPLOAD_REQUESTED) {
			boolean success = uq.buildQueueFromFile();
			if (!success) {
				w.make("Could not re-build queue from file!", Waffle.IMAGE_X);
			}
		} else if (requestCode == CHOOSE_SENSORS_REQUESTED) {
			startStop.setEnabled(true);
			/* TODO fieldMatching.acceptedFields */
			// acceptedFields = fieldMatcher.acceptedFields;
			dfm.setEnabledFields(acceptedFields);
			
		} else if (requestCode == SETUP_REQUESTED) {
				rideName.setText("Ride/St#: " + rideNameString +"/" + stNumber);
				
				SharedPreferences mPrefs = getSharedPreferences(
						Setup.PROJ_PREFS_ID, 0);
				final SharedPreferences.Editor mEdit = mPrefs.edit();
				mEdit.putString(Setup.PROJECT_ID, Integer.toString(projectNum));
				mEdit.commit();
				
				
		} else if (requestCode == LOGIN_REQUESTED) {
			if (resultCode == Activity.RESULT_OK) {
				
				w.make("Login successful", Waffle.LENGTH_SHORT, Waffle.IMAGE_CHECK);
				
			} else if (resultCode == Login.RESULT_ERROR) {
				
				startActivityForResult(new Intent(mContext, Login.class), LOGIN_REQUESTED);

			}
			
		}

	}

	// Assists with differentiating between displays for dialogues
	private int getApiLevel() {
		return android.os.Build.VERSION.SDK_INT;
	}

	// Calls the rapi primitives for actual uploading
	private Runnable uploader = new Runnable() {

		@Override
		public void run() {

			// Create a time stamp for the dataSet
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy, HH:mm:ss",
					Locale.US);
			Date dt = new Date();
			String dateString = sdf.format(dt);

			// Create name from time stamp
			String name = dataName + " - " + dateString;

			// Retrieve project id
			SharedPreferences mPrefs = getSharedPreferences(Setup.PROJ_PREFS_ID, 0);
			String projId = mPrefs.getString(Setup.PROJECT_ID, "");

			// Make sure the user is logged in
			if (api.getCurrentUser() == null) {
				login(false);
			}

			// Creates a new JSONObject that wraps the data and changes it from row major to column major
			JSONObject data = new JSONObject();
			try {
				data.put("data", dataSet);
				data = api.rowsToCols(data);
			} catch (JSONException e) {
				e.printStackTrace();
			}

//			// Tries to upload the data set
//			int dataSetId = api.uploadDataSet(Integer.parseInt(projId), data, name);
//			uploadSuccessful = (dataSetId <= 0) ? true : false;
			
			// Saves data to queue for later upload
			if (!uploadSuccessful) {
				QDataSet ds = new QDataSet(name, getResources().getString(R.id.description), QDataSet.Type.DATA,
						dataSet.toString(), null, projId, null);
				
				uq.addDataSetToQueue(ds);
			}

			// Empties the picture array
			pictures.clear();
			videos.clear();

		}

	};

	/**
	 * Uploads data to iSENSE or something.
	 * 
	 * @author jpoulin
	 */
	private class AddToQueueTask extends AsyncTask<String, Void, String> {

		ProgressDialog dia;

		@Override
		protected String doInBackground(String... strings) {
			uploader.run();
			return null; //strings[0];
		}

		@Override
		protected void onPreExecute() {

			dia = new ProgressDialog(AmusementPark.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Please wait while your data and media saved to Queue");
			dia.setCancelable(false);
			dia.show();

		}

		@Override
		protected void onPostExecute(String sdFileName) {

			dia.setMessage("Done");
			dia.dismiss();

			MediaManager.mediaCount = 0;

			if (uploadSuccessful) {
				w.make("Data was not uploaded - saved instead",
						Waffle.LENGTH_LONG, Waffle.IMAGE_WARN);
			} else {
				w.make("Upload Success", Waffle.LENGTH_SHORT,
						Waffle.IMAGE_CHECK);
				manageUploadQueue();
			}

			Date date = new Date();
			showSummary(date, sdFileName);

		}
	}

	/**
	 * Writes the passed in time to the main screen.
	 * 
	 * @param seconds
	 */
	public void setTime(int seconds) {
		elapsedSecs = seconds;
		
		min = seconds / 60;
		int secInt = seconds % 60;

		sec = "";
		if (secInt <= 9)
			sec = "0" + secInt;
		else
			sec = "" + secInt;
		

		runOnUiThread(new Runnable() {
		    public void run() {
		    	time.setText("Time Elapsed: " + min + ":" + sec);
		    }
		});

	}
	
	private class TimeElapsedTask extends AsyncTask<Void, Integer, Void> {
		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Void doInBackground(Void... voids) {
			for(int time = 0; isRunning; time++ ) {
				setTime(time);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {
			
		}
	}
	
	
	

	/**
	 * Everything needed to be initialized for onCreate in one helpful function.
	 */
	private void initVars() {

		api = API.getInstance();
		api.useDev(true);

		// Get the last stored username and password from Encrypted Shared
		// Preferences
		final SharedPreferences mPrefs = new ObscuredSharedPreferences(
				AmusementPark.mContext,
				AmusementPark.mContext.getSharedPreferences(Login.PREFERENCES_KEY_OBSCURRED_USER_INFO,
						Context.MODE_PRIVATE));

		// If the is no previously store username/password, write in the default
		// credentials
		if (mPrefs.getString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, DEFAULT_USERNAME).equals(DEFAULT_USERNAME)
				|| mPrefs.getString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD, DEFAULT_PASSWORD).equals("")) {
			final SharedPreferences.Editor mEdit = mPrefs.edit();
			mEdit.putString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, DEFAULT_USERNAME);
			mEdit.putString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD, DEFAULT_PASSWORD);
			mEdit.commit();
		}

		// Login to iSENSE
		login(false);

		// Create a new upload queue
		uq = new UploadQueue(ACTIVITY_NAME, mContext, api);

		// These store our media objects
		pictures = new ArrayList<File>();
		videos = new ArrayList<File>();
		
		
		// OMG a button!
		startStop = (Button) findViewById(R.id.startStop);

		// Have some TVs. TextViews I mean.
		values = (TextView) findViewById(R.id.values);
		time = (TextView) findViewById(R.id.time);
		rideName = (TextView) findViewById(R.id.ridename);
		rideName.setText("Ride/St#: " + rideNameString);

		// Start some managers
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// Waffles ares UI messages, and fields are used in recording
		w = new Waffle(this);
		f = new Fields();

		// These are arrays that store values we may record. Who knows, maybe we'll use them.
		accel = new float[4];
		orientation = new float[3];
		rawAccel = new float[3];
		rawMag = new float[3];
		mag = new float[3];
		
		// Fire up the GPS chip (not literally)
		Criteria c = new Criteria();
		c.setAccuracy(Criteria.ACCURACY_FINE);
		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			mLocationManager.requestLocationUpdates(
					mLocationManager.getBestProvider(c, true), 0, 0,
					AmusementPark.this);
		} else {
			// TODO Ask user to turn on GPS
		}

		// This is the location we will get back from GPS
		loc = new Location(mLocationManager.getBestProvider(c, true));

		// Most important feature. Makes the button beep.
		mMediaPlayer = MediaPlayer.create(this, R.raw.beep);
	}

	/**
	 * Logs the user into iSENSE with stored credentials.
	 * 
	 * @param enterNewCredentials
	 *            True if you want to enter new user credentials.
	 */
	void login(boolean enterNewCredentials) {

		if (enterNewCredentials) {
			// TODO call the login activity
		} else {

			new LoginTask().execute();

			// login to iSENSE
			//TODO
			
		}
	}
	
	private class LoginTask extends AsyncTask<Void, Integer, Void> {
		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Void doInBackground(Void... voids) {
			// Get user info from encrypted preferences
			final SharedPreferences mPrefs = new ObscuredSharedPreferences(
					AmusementPark.mContext,
					AmusementPark.mContext.getSharedPreferences("USER_INFO",
							Context.MODE_PRIVATE));
			
			api.createSession(mPrefs.getString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_USERNAME, DEFAULT_USERNAME),
					mPrefs.getString(Login.PREFERENCES_OBSCURRED_USER_INFO_SUBKEY_PASSWORD, DEFAULT_PASSWORD));
			
			return null;
		}

		@Override
		protected void onPostExecute(Void voids) {
		}
	}
	

	/**
	 * Returns the milliseconds elapsed since the Epoch. This includes the time
	 * offset from SyncTime.
	 * 
	 * @return Did you read the description?
	 */
	private long getUploadTime() {
		Calendar c = Calendar.getInstance();
		SharedPreferences mPrefs = getSharedPreferences(TIME_OFFSET_PREFS_ID, 0);
		timeOffset = mPrefs.getLong(TIME_OFFSET_KEY, 0);

		return (((long) c.getTimeInMillis()) + timeOffset);
	}

	/**
	 * Task for checking sensor availability along with enabling/disabling
	 * 
	 * @author jpoulin
	 */
	private class SensorCheckTask extends AsyncTask<Void, Integer, Void> {

		ProgressDialog dia;

		@Override
		protected void onPreExecute() {

			dia = new ProgressDialog(AmusementPark.this);
			dia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dia.setMessage("Gathering project fields...");
			dia.setCancelable(false);
			dia.show();

		}

		@Override
		protected Void doInBackground(Void... voids) {
			//TODO
			SharedPreferences mPrefs = getSharedPreferences(Setup.PROJ_PREFS_ID, 0);
			String eidInput = mPrefs.getString(Setup.PROJECT_ID, "");

			dfm = new DataFieldManager(Integer.parseInt(eidInput), api,
					mContext, f);
			dfm.getOrderWithExternalAsyncTask();
			
			getEnabledFields();
			
			publishProgress(100);
			return null;

		}

		@Override
		protected void onPostExecute(Void voids) {
			dia.setMessage("Done");
			dia.cancel();
		}
	}

	/**
	 * Set all dfm's fields to enabled. 
	 */
	private void enableAllFields() {
		setUpDFMWithAllFields();
	}
	
	private void setUpDFMWithAllFields() {
		SharedPreferences mPrefs = getSharedPreferences(Setup.PROJ_PREFS_ID, 0);
		SharedPreferences.Editor mEdit = mPrefs.edit();
		mEdit.putString(Setup.PROJECT_ID, "-1").commit();

		dfm = new DataFieldManager(Integer.parseInt(mPrefs.getString(
				Setup.PROJECT_ID, "-1")), api, mContext, f);
		dfm.getOrder();

		dfm.enableAllFields();

		String acceptedFields = getResources().getString(R.string.time) + ","
				+ getResources().getString(R.string.accel_x) + ","
				+ getResources().getString(R.string.accel_y) + ","
				+ getResources().getString(R.string.accel_z) + ","
				+ getResources().getString(R.string.accel_total) + ","
				+ getResources().getString(R.string.latitude) + ","
				+ getResources().getString(R.string.longitude) + ","
				+ getResources().getString(R.string.magnetic_x) + ","
				+ getResources().getString(R.string.magnetic_y) + ","
				+ getResources().getString(R.string.magnetic_z) + ","
				+ getResources().getString(R.string.magnetic_total) + ","
				+ getResources().getString(R.string.heading_deg) + ","
				+ getResources().getString(R.string.heading_rad) + ","
				+ getResources().getString(R.string.temperature_c) + ","
				+ getResources().getString(R.string.pressure) + ","
				+ getResources().getString(R.string.altitude) + ","
				+ getResources().getString(R.string.luminous_flux) + ","
				+ getResources().getString(R.string.temperature_f) + ","
				+ getResources().getString(R.string.temperature_k);

		mEdit.putString("accepted_fields", acceptedFields).commit();
	}

	private void initDfm() {

		SharedPreferences mPrefs = getSharedPreferences(Setup.PROJ_PREFS_ID, 0);
		String projectInput = mPrefs.getString(Setup.PROJECT_ID, "");

		if (projectInput.equals("-1")) {
			setUpDFMWithAllFields();
		} else {
			dfm = new DataFieldManager(Integer.parseInt(projectInput), api,
					mContext, f);
			dfm.getOrder();

			String fields = mPrefs.getString("accepted_fields", "");
			getFieldsFromPrefsString(fields);
			getEnabledFields();

		}
	}

	// (currently 2 of these methods exist - one also in step1setup)
		private void getEnabledFields() {

			try {
				for (String s : acceptedFields) {
					if (s.length() != 0)
						break;
				}
			} catch (NullPointerException e) {
				SharedPreferences mPrefs = getSharedPreferences("PROJID", 0);
				String fields = mPrefs.getString("accepted_fields", "");
				getFieldsFromPrefsString(fields);
			}

			for (String s : acceptedFields) {
				if (s.equals(getString(R.string.time)))
					dfm.enabledFields[Fields.TIME] = true;

				else if (s.equals(getString(R.string.accel_x)))
					dfm.enabledFields[Fields.ACCEL_X] = true;

				else if (s.equals(getString(R.string.accel_y)))
					dfm.enabledFields[Fields.ACCEL_Y] = true;

				else if (s.equals(getString(R.string.accel_z)))
					dfm.enabledFields[Fields.ACCEL_Z] = true;

				else if (s.equals(getString(R.string.accel_total)))
					dfm.enabledFields[Fields.ACCEL_TOTAL] = true;

				else if (s.equals(getString(R.string.latitude)))
					dfm.enabledFields[Fields.LATITUDE] = true;

				else if (s.equals(getString(R.string.longitude)))
					dfm.enabledFields[Fields.LONGITUDE] = true;

				else if (s.equals(getString(R.string.magnetic_x)))
					dfm.enabledFields[Fields.MAG_X] = true;

				else if (s.equals(getString(R.string.magnetic_y)))
					dfm.enabledFields[Fields.MAG_Y] = true;

				else if (s.equals(getString(R.string.magnetic_z)))
					dfm.enabledFields[Fields.MAG_Z] = true;

				else if (s.equals(getString(R.string.magnetic_total)))
					dfm.enabledFields[Fields.MAG_TOTAL] = true;

				else if (s.equals(getString(R.string.heading_deg)))
					dfm.enabledFields[Fields.HEADING_DEG] = true;

				else if (s.equals(getString(R.string.heading_rad)))
					dfm.enabledFields[Fields.HEADING_RAD] = true;

				else if (s.equals(getString(R.string.temperature_c)))
					dfm.enabledFields[Fields.TEMPERATURE_C] = true;

				else if (s.equals(getString(R.string.temperature_f)))
					dfm.enabledFields[Fields.TEMPERATURE_F] = true;

				else if (s.equals(getString(R.string.temperature_k)))
					dfm.enabledFields[Fields.TEMPERATURE_K] = true;

				else if (s.equals(getString(R.string.pressure)))
					dfm.enabledFields[Fields.PRESSURE] = true;

				else if (s.equals(getString(R.string.altitude)))
					dfm.enabledFields[Fields.ALTITUDE] = true;

				else if (s.equals(getString(R.string.luminous_flux)))
					dfm.enabledFields[Fields.LIGHT] = true;

			}
		}

		private void getFieldsFromPrefsString(String fieldList) {

			String[] fields = fieldList.split(",");
			acceptedFields = new LinkedList<String>();

			for (String f : fields) {
				acceptedFields.add(f);
			}
		}
	

	/**
	 * Prompts the user to upload the rest of their content
	 * upon successful upload of data.
	 */
	private void manageUploadQueue() {
		if (!uq.emptyQueue()) {
			Intent i = new Intent().setClass(mContext, QueueLayout.class);
			i.putExtra(QueueLayout.PARENT_NAME, uq.getParentName());
			startActivityForResult(i, QUEUE_UPLOAD_REQUESTED);
		} else {
			w.make("No data to upload.", Waffle.IMAGE_CHECK);
		}
	}
	
	/**
	 * Turns elapsedSecs into readable strings.
	 * 
	 * @author jpoulin
	 */
	private class ElapsedTime {
		private String elapsedSeconds;
		private String elapsedMinutes;
		
		/**
		 * Everybody likes strings.
		 * 
		 * @param seconds
		 */
		ElapsedTime(int seconds) {
			int minutes;
			
		
			minutes = seconds / 60;
			seconds %= 60;
			
			if (seconds < 10) {
				elapsedSeconds = "0" + seconds;
			} else {
				elapsedSeconds = "" + seconds;
			}


			if (minutes < 10) {
				elapsedMinutes = "0" + minutes;
			} else {
				elapsedMinutes = "" + minutes;
			}		
		}
		
	}

	/**
	 * Makes a summary dialog.
	 * @param date Time of upload
	 * @param sdFileName Name of the written csv
	 */
	private void showSummary(Date date, String sdFileName) {

		ElapsedTime time = new ElapsedTime(elapsedSecs);
		
		Intent iSummary = new Intent(mContext, Summary.class);
		iSummary.putExtra("seconds", time.elapsedSeconds)
				.putExtra("minutes", time.elapsedMinutes)
				.putExtra("append", "Filename: \n" + sdFileName)
				.putExtra("date", getNiceDateString(date))
				.putExtra("points", "" + dataPointCount);

		startActivity(iSummary);

	}

	/**
	 * Turns on only the sensors you need to record data.
	 */
	@SuppressLint("InlinedApi")
	private void registerSensors() {
		if (mSensorManager != null && setupDone && dfm != null) {

			if (dfm.enabledFields[Fields.ACCEL_X]
					|| dfm.enabledFields[Fields.ACCEL_Y]
					|| dfm.enabledFields[Fields.ACCEL_Z]
					|| dfm.enabledFields[Fields.ACCEL_TOTAL]) {
				mSensorManager.registerListener(AmusementPark.this,
						mSensorManager
								.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
						SensorManager.SENSOR_DELAY_FASTEST);
			}

			if (dfm.enabledFields[Fields.MAG_X]
					|| dfm.enabledFields[Fields.MAG_Y]
					|| dfm.enabledFields[Fields.MAG_Z]
					|| dfm.enabledFields[Fields.MAG_TOTAL]
					|| dfm.enabledFields[Fields.HEADING_DEG]
					|| dfm.enabledFields[Fields.HEADING_RAD]) {
				mSensorManager.registerListener(AmusementPark.this,
						mSensorManager
								.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
						SensorManager.SENSOR_DELAY_FASTEST);
			}

			if (dfm.enabledFields[Fields.TEMPERATURE_C]
					|| dfm.enabledFields[Fields.TEMPERATURE_F]
					|| dfm.enabledFields[Fields.TEMPERATURE_K]
					|| dfm.enabledFields[Fields.ALTITUDE]) {
				if (getApiLevel() >= 14) {
					mSensorManager
							.registerListener(
									AmusementPark.this,
									mSensorManager
											.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE),
									SensorManager.SENSOR_DELAY_FASTEST);
				}
			}

			if (dfm.enabledFields[Fields.PRESSURE]
					|| dfm.enabledFields[Fields.ALTITUDE]) {
				mSensorManager.registerListener(AmusementPark.this,
						mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE),
						SensorManager.SENSOR_DELAY_FASTEST);
			}

			if (dfm.enabledFields[Fields.LIGHT]) {
				mSensorManager.registerListener(AmusementPark.this,
						mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),
						SensorManager.SENSOR_DELAY_FASTEST);
			}

		}
	}

	/**
	 * Records a data point, puts it into the dataSet object, and writes it to a csv string.
	 */
	private void recordData() {
		 dataPointCount++;
         elapsedSecs += srate;
         
         DecimalFormat toThou = new DecimalFormat("######0.000");

         if (dfm.enabledFields[Fields.ACCEL_X])
                 f.accel_x = toThou.format(accel[0]);
         if (dfm.enabledFields[Fields.ACCEL_Y])
                 f.accel_y = toThou.format(accel[1]);
         if (dfm.enabledFields[Fields.ACCEL_Z])
                 f.accel_z = toThou.format(accel[2]);
         if (dfm.enabledFields[Fields.ACCEL_TOTAL])
                 f.accel_total = toThou.format(accel[3]);
         if (dfm.enabledFields[Fields.LATITUDE])
                 f.latitude = loc.getLatitude();
         if (dfm.enabledFields[Fields.LONGITUDE])
                 f.longitude = loc.getLongitude();
         if (dfm.enabledFields[Fields.HEADING_DEG])
                 f.angle_deg = toThou.format(orientation[0]);
         if (dfm.enabledFields[Fields.HEADING_RAD])
                 f.angle_rad = ""
                                 + (Double.parseDouble(f.angle_deg) * (Math.PI / 180));
         if (dfm.enabledFields[Fields.MAG_X])
                 f.mag_x = "" + mag[0];
         if (dfm.enabledFields[Fields.MAG_Y])
                 f.mag_y = "" + mag[1];
         if (dfm.enabledFields[Fields.MAG_Z])
                 f.mag_z = "" + mag[2];
         if (dfm.enabledFields[Fields.MAG_TOTAL])
                 f.mag_total = ""
                                 + Math.sqrt(Math.pow(Double.parseDouble(f.mag_x), 2)
                                                 + Math.pow(Double.parseDouble(f.mag_y), 2)
                                                 + Math.pow(Double.parseDouble(f.mag_z), 2));
         if (dfm.enabledFields[Fields.TIME])
                 f.timeMillis = currentTime + elapsedSecs;
         if (dfm.enabledFields[Fields.TEMPERATURE_C])
                 f.temperature_c = temperature;
         if (dfm.enabledFields[Fields.TEMPERATURE_F])
                 if (temperature.equals(""))
                         f.temperature_f = temperature;
                 else
                         f.temperature_f = ""
                                         + ((Double.parseDouble(temperature) * 1.8) + 32);
         if (dfm.enabledFields[Fields.TEMPERATURE_K])
                 if (temperature.equals(""))
                         f.temperature_k = temperature;
                 else
                         f.temperature_k = ""
                                         + (Double.parseDouble(temperature) + 273.15);
         if (dfm.enabledFields[Fields.PRESSURE])
                 f.pressure = pressure;
         if (dfm.enabledFields[Fields.ALTITUDE])
                 f.altitude = "" + loc.getAltitude();
         if (dfm.enabledFields[Fields.LIGHT])
                 f.lux = light;

         dataSet.put(dfm.putData());
         dataToBeWrittenToFile = dfm.writeSdCardLine();

	}

}
