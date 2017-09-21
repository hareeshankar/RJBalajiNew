package com.rjbalaji;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import h.thunderbird.phoenix.rjbalaji.R;

public class DownloadProcess extends AsyncTask<Void, Integer, Boolean> {
	private Context mContext;
	private ProgressDialog mProgress;
	private String fileURL = "";
	private String fileName = "";
	private ImageView iv_download;

	public DownloadProcess(Context context, int progressDialog, File file,
			String fileName, String fileURL, ImageView iv_download) {
		this.mContext = context;
		this.fileURL = fileURL;
		this.file = file;
		this.fileName = fileName;
		this.iv_download = iv_download;
		// this.mProgressDialog = progressDialog;
	}

	File file;

	@Override
	public void onPreExecute() {

		mProgress = new ProgressDialog(mContext);
		mProgress.setMessage("Downloading \nPlease wait...");
		// if (mProgressDialog == ProgressDialog.STYLE_HORIZONTAL) {

		mProgress.setIndeterminate(false);
		mProgress.setMax(100);
		mProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgress.setCancelable(false);
		// }
		mProgress.setCancelable(false);
		mProgress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (!isCancelled()) {
							cancel(true);
						}
						dialog.dismiss();
					}
				});
		mProgress.show();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		// if (mProgressDialog == ProgressDialog.STYLE_HORIZONTAL) {
		mProgress.setProgress(values[0]);
		// }
	}

	@Override
	protected Boolean doInBackground(Void... values) {
		try {
			// connecting to url
			// URL u = new URL(fileURL);
			// HttpURLConnection c = (HttpURLConnection) u.openConnection();
			// c.setRequestMethod("GET");
			// c.setDoOutput(true);
			// c.connect();
			URL u = new URL(fileURL);
			URLConnection c = u.openConnection();
			c.setConnectTimeout(60000);
			c.connect();
			int lenghtOfFile = c.getContentLength();
			InputStream in = u.openStream();

			// lenghtOfFile is used for calculating download progress
			deleteFile();
			if (!file.exists())
				file.createNewFile();
			// this is where the file will be seen after the download
			FileOutputStream f = new FileOutputStream(file);
			// file input is from the url
			// InputStream in = c.openS
			byte[] buffer = new byte[1024];
			int len1 = 0;
			long total = 0;

			while ((len1 = in.read(buffer)) > 0) {
				if (!isCancelled()) {
					total += len1; // total = total + len1
					int percentage = (int) ((total * 100) / lenghtOfFile);
					publishProgress(percentage);
					f.write(buffer, 0, len1);
				} else {
					deleteFile();
					break;
				}
			}
			f.close();
			return true;
		} catch (Exception e) {
			deleteFile();
			Log.d("LOG_TAG", e.getMessage());
		}
		return false;

	}

	@Override
	protected void onPostExecute(Boolean result) {
		mProgress.dismiss();
		if (result) {
			iv_download.setImageResource(R.drawable.offline);
			Toast.makeText(mContext, "Download successful.", Toast.LENGTH_LONG)
					.show();
		} else {
			Toast.makeText(mContext,
					"Error in downloading. Please try again later.",
					Toast.LENGTH_LONG).show();
		}
	}

	private void deleteFile() {
		if (file != null && file.exists())
			file.delete();
	}
}