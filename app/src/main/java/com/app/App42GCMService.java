/**
 * 
 */
package com.app;

import h.thunderbird.phoenix.rjbalaji.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.rjbalaji.BaseActivity;
import com.rjbalaji.MainScreen;
import com.rjbalaji.Common.Common;
import com.rjbalaji.db.dbHelper;
import com.rjbalaji.interfaces.Constants;
import com.shephertz.app42.paas.sdk.android.App42API;
import com.shephertz.app42.paas.sdk.android.App42CallBack;
import com.shephertz.app42.paas.sdk.android.App42Log;

/**
 * @author Ajay Tiwari
 * 
 */
public class App42GCMService extends GCMBaseIntentService {
	public static String PROJECT_NUMBER = Constants.Project_Number;// "142905585869";
	static final String LARGE_IMAGE_URL = "<YOUR IMAGE URL>";
	static int msgCount = 0;

	/**
	 * Intent used to display a message in the screen.
	 */
	static final String DISPLAY_MESSAGE_ACTION = "com.app.DISPLAY_MESSAGE";

	/**
	 * Intent's extra that contains the message to be displayed.
	 */
	static final String EXTRA_MESSAGE = "message";

	public App42GCMService() {
		super(PROJECT_NUMBER);
	}

	@Override
	protected void onError(Context arg0, String msg) {
		Log.i(TAG, "onError " + msg);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.i(TAG, "Received message "
				+ intent.getExtras().getString("message"));
		String message = intent.getExtras().getString("message");
		if (message != null && !message.equals("")) {
			String[] parseMessage = message.split(Constants.SEPARATOR);
			if (parseMessage != null && parseMessage.length > 0) {
				if (parseMessage[0].equals("DBR")) {
					// Call service
					// DBR;Market url
					String link = parseMessage[1];
					new readFileFromServer(link).execute();
				} else {
					boolean isApp;
					String messageToDisplay;
					if (parseMessage[0] != null
							&& parseMessage[0].equals("APP")) {
						isApp = true;
						messageToDisplay = "Our new app " + parseMessage[1]
								+ " has been published.";
					} else {
						isApp = false;
						messageToDisplay = parseMessage[1]
								+ " has been added in "
								+ getString(R.string.app_name);
					}   
					dbHelper loDb = new dbHelper(context);
					if (!loDb.isRecordAvailable(parseMessage)) {
						generateNotification(context, messageToDisplay,
								message, isApp);
						// if (!isApp) {

						Common.insertTrackInDb(message, loDb,
								String.valueOf(new Date()));
						mHandler.post(run);
					}
					// }
				}
			}
			// Intent intent1 = new Intent(getBaseContext(),
			// NotificationActivity.class);
			// intent1.putExtra("msg", message);
			// intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// getApplication().startActivity(intent1);
		}
	}

	private class readFileFromServer extends
			AsyncTask<String, Integer, Boolean> {
		String link = "";

		public readFileFromServer(String link) {
			this.link = link;
		}

		@Override
		protected void onPreExecute() {
			Intent refreshIntent = new Intent();
			refreshIntent.setAction("refreshDB");
			refreshIntent.putExtra("start", true);
			sendBroadcast(refreshIntent);
		}

		@Override
		protected Boolean doInBackground(String... fsParams) {
			try {
				dbHelper loDb = new dbHelper(getApplicationContext());
				HttpGet httpGet = new HttpGet(link);
				HttpParams httpParameters = new BasicHttpParams();
				int timeoutConnection = 3000;
				HttpConnectionParams.setConnectionTimeout(httpParameters,
						timeoutConnection);
				int timeoutSocket = 5000;
				HttpConnectionParams
						.setSoTimeout(httpParameters, timeoutSocket);
				DefaultHttpClient httpClient = new DefaultHttpClient(
						httpParameters);
				HttpResponse response = httpClient.execute(httpGet);
				HttpEntity ht = response.getEntity();

				BufferedHttpEntity buf = new BufferedHttpEntity(ht);

				InputStream is = buf.getContent();

				BufferedReader r = new BufferedReader(new InputStreamReader(is));
				String date = String.valueOf(new Date());
				int count = 0;
				String line;
				while ((line = r.readLine()) != null) {
					if (count == 0) {
						int isDeleted = loDb.deleteMusicData();
						count++;
					}
					Common.insertTrackInDb(line, loDb, date);
				}
				mHandler.post(run);
				return true;
			} catch (Exception e) {
				Log.e("Error", "Error:" + e);
				// TODO: handle exception
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
		}
	}

	Handler mHandler = new Handler();
	Runnable run = new Runnable() {

		@Override
		public void run() {
			Intent refreshIntent = new Intent();
			refreshIntent.setAction("refreshDB");
			refreshIntent.putExtra("start", false);
			sendBroadcast(refreshIntent);
		}
	};

	static void setSenderId(String senderId) {
		PROJECT_NUMBER = senderId;
	}

	@Override
	protected void onRegistered(Context arg0, String regId) {
		Log.i(TAG, "Device registered: regId = " + regId);
		registerWithApp42(regId);
	}

	private void registerWithApp42(String regId) {
		App42Log.debug(" Registering on Server ....");
		App42API.buildPushNotificationService().storeDeviceToken(
				App42API.getLoggedInUser(), regId, new App42CallBack() {
					@Override
					public void onSuccess(Object paramObject) {
						// TODO Auto-generated method stub
						App42Log.debug(" ..... Registeration Success ....");
						GCMRegistrar.setRegisteredOnServer(App42API.appContext,
								true);
					}

					@Override
					public void onException(Exception paramException) {
						App42Log.debug(" ..... Registeration Failed ....");
						App42Log.debug("storeDeviceToken :  Exception : on start up "
								+ paramException);

					}
				});

	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {

		Log.i(TAG, "Device unRegistered: regId = " + arg1);

	}

	/**
	 * Notifies UI to display a message.
	 * <p>
	 * This method is defined in the common helper because it's used both by the
	 * UI and the background service.
	 * 
	 * @param context
	 *            application's context.
	 * @param message
	 *            message to be displayed.
	 */

	public static void resetMsgCount() {
		msgCount = 0;
	}

	/**
	 * Issues a notification to inform the user that server has sent a message.
	 * 
	 * @param isApp
	 * 
	 * @param message2
	 */
	public void generateNotification(Context context, String messageToDisplay,
			String message1, boolean isApp1) {
		String[] splitMsg = message1.split(Constants.SEPARATOR);

		int icon = R.drawable.icon;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		String title = context.getString(R.string.app_name);
		Intent notificationIntent;
		// if (isApp) {
		// notificationIntent = new Intent(Intent.ACTION_VIEW,
		// Uri.parse(splitMsg[2]));
		// } else {
		if (!isAppOpen(context)) {
			notificationIntent = new Intent(context, MainScreen.class);
			notificationIntent.putExtra("screenName",
					getScreenNameFromTag(splitMsg[0]));
			notificationIntent.putExtra("serverTagName", splitMsg[0]);
		} else {
			notificationIntent = new Intent();
		}
		// }
		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		Notification notification = new NotificationCompat.Builder(context)
				.setContentTitle(title).setContentText(messageToDisplay)
				.setContentIntent(intent).setSmallIcon(icon).setWhen(when)
				.setNumber(++msgCount)
				.setLargeIcon(getBitmapFromURL(LARGE_IMAGE_URL))
				.setLights(Color.YELLOW, 1, 2).setAutoCancel(true).build();

		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notificationManager.notify(0, notification);
	}

	public static Bitmap getBitmapFromURL(String src) {
		try {
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);
			return myBitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private boolean isAppOpen(Context context) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> procInfos = activityManager
				.getRunningAppProcesses();
		if (procInfos.size() > 0) {
			if (procInfos.get(0).processName.equals(context.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	private String getScreenNameFromTag(String tag) {
		String name = BaseActivity.TITLES[0];
		for (int i = 0; i < BaseActivity.TAGS.length; i++) {
			if (BaseActivity.TAGS[i].equals(tag)) {
				return BaseActivity.TITLES[i];
			}
		}
		return name;
	}

}
