package com.rjbalaji.db;

import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.rjbalaji.rjbalajipackage.AlbumSongDetailPackage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class dbHelper extends SQLiteOpenHelper {

	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "RJBalajiDb";

	// Settings table name
	static final String TABLE_MUSICS = "AudioData";
	// Settings Table Columns names
	static final String MUSIC_ID = "MusicId";
	static final String MUSIC_ID_STR = "MusicIdStr";
	static final String MUSIC_NAME = "MusicName";
	static final String ALBUM_CATALOGE = "AlbumCataLoge";
	static final String LINK = "Link";
	static final String DATE_TIME = "DateTime";

	public dbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {

		String CREATE_JOB_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_MUSICS
				+ "(" + MUSIC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ MUSIC_ID_STR + " TEXT," + MUSIC_NAME + " TEXT," + LINK
				+ " INTEGER," + ALBUM_CATALOGE + " TEXT," + DATE_TIME
				+ " DATETIME)";
		db.execSQL(CREATE_JOB_TABLE);

	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_MUSICS);
		// Create tables again
		onCreate(db);
	}

	public long addNewAudio(String music_id_str, String name, String album_cat,
			String link, String date) {
		SQLiteDatabase db = this.getWritableDatabase();
		String lsJobId = null;
		ContentValues values = new ContentValues();
		values.put(MUSIC_ID, lsJobId);
		values.put(MUSIC_ID_STR, music_id_str);
		values.put(MUSIC_NAME, name);
		values.put(ALBUM_CATALOGE, album_cat);
		values.put(LINK, link);
		values.put(DATE_TIME, date);
		// Inserting Row
		long liJobId = db.insert(TABLE_MUSICS, null, values);
		db.close();
		return liJobId;
	}

	public int getCount() {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor loCursor = db.query(TABLE_MUSICS, new String[] { "*" }, null,
				null, null, null, null);
		loCursor.moveToFirst();
		int totalCount = 0;
		if (loCursor != null) {
			totalCount = loCursor.getCount();
			loCursor.close();
		}
		SQLiteDatabase.releaseMemory();

		close();

		return totalCount;
	}

	public ArrayList<AlbumSongDetailPackage> getSelectedAlbumData(String tag) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor loCursor = db.query(TABLE_MUSICS, new String[] { "*" },
				ALBUM_CATALOGE + "=?", new String[] { tag }, null, null,
				DATE_TIME + " DESC");
		ArrayList<AlbumSongDetailPackage> list = new ArrayList<AlbumSongDetailPackage>();
		loCursor.moveToFirst();
		if (loCursor != null) {
			for (int i = 0; i < loCursor.getCount(); i++) {
				AlbumSongDetailPackage detail = new AlbumSongDetailPackage();
				detail.setAlbum_id("1");
				// detail.setMusic_id(loCursor.getString(loCursor
				// .getColumnIndex(MUSIC_ID)));
				detail.setMusic_id(String.valueOf(i));
				detail.setSong_name(loCursor.getString(loCursor
						.getColumnIndex(MUSIC_NAME)));
				detail.setMusic_link(loCursor.getString(loCursor
						.getColumnIndex(LINK)));
				detail.setMovie_name(loCursor.getString(loCursor
						.getColumnIndex(ALBUM_CATALOGE)));
				list.add(detail);
				loCursor.moveToNext();

			}
			loCursor.close();
		}
		SQLiteDatabase.releaseMemory();

		close();

		return list;
	}

	public int deleteMusicData() {
		SQLiteDatabase db = this.getReadableDatabase();
		int isDeleted = db.delete(TABLE_MUSICS, null, null);
		close();
		return isDeleted;
	}

	public boolean isRecordAvailable(String[] parseMessage) {
		String TAG = parseMessage[0];
		String Name = parseMessage[1];
		String Link = parseMessage[2];
		boolean isRecordAvailable = false;
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor loCursor = db.query(TABLE_MUSICS, new String[] { "*" },
				ALBUM_CATALOGE + "=? AND " + MUSIC_NAME + "=? AND " + LINK
						+ "=?", new String[] { TAG, Name, Link }, null, null,
				DATE_TIME + " DESC");
		ArrayList<AlbumSongDetailPackage> list = new ArrayList<AlbumSongDetailPackage>();
		loCursor.moveToFirst();
		if (loCursor != null) {
			if (loCursor.getCount() > 0) {
				isRecordAvailable = true;
			}
			loCursor.close();
		}
		SQLiteDatabase.releaseMemory();
		close();
		return isRecordAvailable;
	}

}
