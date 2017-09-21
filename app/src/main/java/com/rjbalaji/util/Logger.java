package com.rjbalaji.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import android.util.Log;

/**
 * class for view_footer log.
 * 
 */

public class Logger {
	/** method for print log. */
	private static final boolean needToLog = false;

	public static void logger(String msg) {
		if (needToLog)
			Log.e("app store", msg);
	}

	public static void logger(String tag, String msg) {
		if (needToLog)
			Log.e(tag, msg);
	}

	public static void systemLog(String message) {
		if (needToLog)
			System.out.println(message);
	}

	public static void errorLog(String message) {
		if (needToLog)
			Log.e("app store", message);
	}

	public static void e(String message) {
		if (needToLog)
			Log.e(" app store", message);
	}

	public static void e(String tag, String message) {
		if (needToLog)
			Log.e(" app store:" + tag, message);
	}

	public static void i(String tag, String message) {
		if (needToLog)
			Log.i(" app store:" + tag, message);
	}

	public static void iLog(String message) {
		if (needToLog)
			Log.i("app store", message);
	}

	public static void iLog(String tag, String message) {
		if (needToLog)
			Log.i(tag, message);
	}

	public static void printStackTrace(Exception e) {
		if (needToLog)
			e.printStackTrace();
	}

	public static void printStackTrace(Error e) {
		if (needToLog)
			e.printStackTrace();
	}

}