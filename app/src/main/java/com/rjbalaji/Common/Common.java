package com.rjbalaji.Common;

import h.thunderbird.phoenix.rjbalaji.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import com.rjbalaji.db.dbHelper;
import com.rjbalaji.interfaces.Constants;

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public class Common {
	static String TAG = "Common";

	public static double[] getCurrentLatLong(Context foContext) {
		double[] loLatLong = { 0.0, 0.0 };
		LocationManager loLm = (LocationManager) foContext
				.getSystemService(Context.LOCATION_SERVICE);
		String lsProvider = null;
		if (loLm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			lsProvider = LocationManager.GPS_PROVIDER;
		} else if (loLm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			lsProvider = LocationManager.NETWORK_PROVIDER;
		}
		if (lsProvider != null) {
			Location location = loLm.getLastKnownLocation(lsProvider);
			if (location != null) {
				loLatLong[0] = location.getLatitude();
				loLatLong[1] = location.getLongitude();

				if (loLatLong[0] == 0.0 && loLatLong[1] == 0.0) {
					if (loLm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
						lsProvider = LocationManager.NETWORK_PROVIDER;
						location = loLm.getLastKnownLocation(lsProvider);
						if (location != null) {
							loLatLong[0] = location.getLatitude();
							loLatLong[1] = location.getLongitude();
						}
					}
				}
			} else {
				if (loLatLong[0] == 0.0 && loLatLong[1] == 0.0) {
					if (loLm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
						lsProvider = LocationManager.NETWORK_PROVIDER;
						location = loLm.getLastKnownLocation(lsProvider);
						if (location != null) {
							loLatLong[0] = location.getLatitude();
							loLatLong[1] = location.getLongitude();
						}
					}
				}
			}
		}
		Log.i(TAG, "Location provider: " + lsProvider);
		Log.i(TAG, "Driver Location:" + loLatLong[0] + " , " + loLatLong[1]);
		return loLatLong;
	}

	public static boolean isInternetAvailable(Context foContext) {
		NetworkInfo loNetInfo = ((ConnectivityManager) foContext
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();

		if (loNetInfo != null)
			if (loNetInfo.isAvailable())
				if (loNetInfo.isConnected())
					return true;

		return false;
	}

	public static String doHttpPost(String fsPostURL,
			List<NameValuePair> foNameValuePairs) {
		HttpClient loHttpClient = new DefaultHttpClient();
		HttpPost loHttpPost = new HttpPost(fsPostURL);
		try {
			loHttpPost.setEntity(new UrlEncodedFormEntity(foNameValuePairs));
			HttpResponse loResponse = loHttpClient.execute(loHttpPost);
			InputStream in = loResponse.getEntity().getContent();
			return getString(in);
		} catch (IOException e) {
		}
		return null;
	}

	public static String doHttpGet(String fsGetURL) {
		try {
			fsGetURL = fsGetURL.replace(" ", "%20");
			Log.i("URL", "GET URL:" + fsGetURL);
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet(fsGetURL);
			HttpResponse responseGet = client.execute(get);
			InputStream in = responseGet.getEntity().getContent();
			return getString(in);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getFileExtension(String fsFileName) {
		// String lsInfo[] = fsFileName.split("\\.(?=[^\\.]+$)");
		String lsfileName = fsFileName
				.substring(fsFileName.lastIndexOf('/') + 1);
		return lsfileName;
	}

	public static String getFileNameFromUrl(String fsFileName) {
		// String lsInfo[] = fsFileName.split("\\.(?=[^\\.]+$)");
		String lsfileName = fsFileName
				.substring(fsFileName.lastIndexOf('/') + 1);
		return lsfileName;
	}

	/**
	 * Get string from the InputStream
	 * 
	 * @return string from the InputStream
	 */
	public static String getString(InputStream foInStream) throws IOException {
		BufferedReader loBr = new BufferedReader(new InputStreamReader(
				foInStream));
		StringBuffer loSb = new StringBuffer();
		char[] inputLine = new char[2048];
		int count = loBr.read(inputLine);
		while (count > 0) {
			loSb.append(inputLine, 0, count);
			count = loBr.read(inputLine);
		}
		return loSb.toString();
	}

	public static String getCurrentTimeString() {
		Date loToday = new Date(System.currentTimeMillis());
		try {

			String lsTimeZone = Calendar.getInstance().getTimeZone()
					.getDisplayName(false, TimeZone.LONG);
			String lsTime = new SimpleDateFormat("hh:mm aa", Locale.ENGLISH)
					.format(loToday);
			return lsTime + " " + lsTimeZone;
		} catch (Exception e) {
			return loToday.toString();
		}
	}

	public static String getCurrentDateString() {
		Date loToday = new Date(System.currentTimeMillis());
		try {
			return new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH)
					.format(loToday);
		} catch (Exception e) {
			return loToday.toString();
		}
	}

	public static String getCurrentDateTimeString() {
		Date loToday = new Date(System.currentTimeMillis());
		try {
			// return new SimpleDateFormat("MM/dd/yyyy'T'hh:mm:ss.SSS",
			// Locale.ENGLISH).format(loToday);
			return new SimpleDateFormat("dd-MMM-yyyy  hh:mm aa", Locale.ENGLISH)
					.format(loToday);
		} catch (Exception e) {
			return loToday.toString();
		}
	}

	public static String getBasePath(Context act) {
		try {
			File root;
			root = Environment.getExternalStorageDirectory();
			return root + "/RJ Balaji/";
		} catch (Exception e) {
		}
		return null;
	}

	public static String getScreenSize(Context foContext) {
		DisplayMetrics loDisplayMetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) foContext
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(loDisplayMetrics);
		return loDisplayMetrics.widthPixels + "X"
				+ loDisplayMetrics.heightPixels;
	}

	public static int getScreenWidth(Context foContext) {
		DisplayMetrics loDisplayMetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) foContext
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(loDisplayMetrics);

		int liWidthInDP = loDisplayMetrics.widthPixels;

		return liWidthInDP;
	}

	public static float convertPixelsToDp(float foPx, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float loDp = foPx / (metrics.densityDpi / 160f);
		return loDp;
	}

	public static int convertDpToPixel(float fsDp, Context foContext) {
		DisplayMetrics metrics = foContext.getResources().getDisplayMetrics();

		// float fpixels = metrics.density * dp;
		int pixels = (int) (metrics.density * fsDp + 0.5f);
		return pixels;
	}

	public static boolean getLocationStatus(Context foContext) {
		LocationManager locationManager = (LocationManager) foContext
				.getSystemService(Context.LOCATION_SERVICE);

		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
			return true;
		else
			return false;
	}

	public static String decodeHtmlString(String fsEncodedString) {
		String lsDecodedHTMLSctring = null;
		try {

			lsDecodedHTMLSctring = Html.fromHtml(fsEncodedString).toString();
		} catch (Exception e) {
			// TODO: handle exception
		}

		return lsDecodedHTMLSctring;
	}

	public static String getConvertedDate(String fsDate, String fsDateFormat) {
		String lsNewDate = null;

		SimpleDateFormat loFromUser = new SimpleDateFormat(fsDateFormat);
		SimpleDateFormat loMyFormat = new SimpleDateFormat("dd-MMM-yyyy");

		try {
			lsNewDate = loMyFormat.format(loFromUser.parse(fsDate));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return lsNewDate;
	}

	public static String getConvertedDate_Appointment(String fsDate,
			String fsDateFormat) {
		String lsNewDate = null;

		SimpleDateFormat loFromUser = new SimpleDateFormat(fsDateFormat);
		SimpleDateFormat loMyFormat = new SimpleDateFormat("MMMM/dd/yyyy");

		try {
			lsNewDate = loMyFormat.format(loFromUser.parse(fsDate));
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return lsNewDate;
	}

	public static boolean isStringAvailable(String fsString, String fsFindString) {
		if (fsString != null && !fsString.equals("")) {
			if (fsString.contains(fsFindString))
				return true;
			else
				return false;
		} else
			return false;
	}

	public static int progressToTimer(int progress, int totalDuration) {
		int currentDuration = 0;
		totalDuration = (int) (totalDuration / 1000);
		currentDuration = (int) ((((double) progress) / 100) * totalDuration);

		// return current duration in milliseconds
		return currentDuration * 1000;
	}

	public static String milliSecondsToTimer(long milliseconds) {
		String finalTimerString = "";
		String secondsString = "";
		String MinuteString = "";
		String HoursString = "";

		// Convert total duration into time
		int hours = (int) (milliseconds / (1000 * 60 * 60));
		int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
		int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
		// Add hours if there

		// Prepending 0 to seconds if it is one digit
		if (seconds < 10) {
			secondsString = "0" + seconds;
		} else {
			secondsString = "" + seconds;
		}

		if (minutes < 10) {
			MinuteString = "0" + minutes;
		} else {
			MinuteString = "" + minutes;
		}

		if (hours < 10) {
			HoursString = "0" + hours;
		} else {
			HoursString = "" + hours;
		}

		if (hours > 0) {
			finalTimerString = HoursString + ":";
		}

		finalTimerString = finalTimerString + MinuteString + ":"
				+ secondsString;

		// return timer string
		return finalTimerString;
	}

	public static int getProgressPercentage(long currentDuration,
			long totalDuration) {
		Double percentage = (double) 0;

		long currentSeconds = (int) (currentDuration / 1000);
		long totalSeconds = (int) (totalDuration / 1000);

		// calculating percentage
		percentage = (((double) currentSeconds) / totalSeconds) * 100;

		// return percentage
		return percentage.intValue();
	}

	public static boolean isFileExist(String fileName, Context context) {
		File filePath = new File(Common.getBasePath(context) + fileName);
		return filePath.exists();
	}

	public static void insertTrackInDb(String line, dbHelper loDb, String date) {
		if (line != null && !line.equals("")) {
			String[] details = line.split(Constants.SEPARATOR);
			if (details != null && details.length == 3) {
				String albumName = details[0];
				String trackName = details[1];
				String trackLink = details[2];
				long id = loDb.addNewAudio("1", trackName, albumName,
						trackLink, date);
				Log.i("Id", "ID:" + id);
			}
		}
	}
}
