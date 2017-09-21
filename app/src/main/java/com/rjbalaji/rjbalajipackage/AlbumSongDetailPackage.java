package com.rjbalaji.rjbalajipackage;

import java.io.Serializable;
import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class AlbumSongDetailPackage implements Parcelable, Serializable {
	private static final long serialVersionUID = 1L;
	String album_id;
	String music_id;
	String song_name;
	String artist_name;
	String movie_name;
	String music_link;
	String price;
	String bypass_status;
	String ringtone_link;
	String ringtone_size;
	String music_size;
	
	public AlbumSongDetailPackage() {
	}

	public AlbumSongDetailPackage(Parcel in) {
		readFromParcel(in);
	}

	public String getMusic_size() {
		return music_size;
	}

	public void setMusic_size(String music_size) {
		this.music_size = music_size;
	}

	public String getRingtone_size() {
		return ringtone_size;
	}

	public void setRingtone_size(String ringtone_size) {
		this.ringtone_size = ringtone_size;
	}

	public String getRingtone_link() {
		return ringtone_link;
	}

	public void setRingtone_link(String ringtone_link) {
		this.ringtone_link = ringtone_link;
	}

	public void setBypass_status(String bypass_status) {
		this.bypass_status = bypass_status;
	}

	public String getBypass_status() {
		return bypass_status;
	}

	public void setMusic_id(String music_id) {
		this.music_id = music_id;
	}

	public String getMusic_id() {
		return music_id;
	}

	public void setSong_name(String song_name) {
		this.song_name = song_name;
	}

	public String getSong_name() {
		return song_name;
	}

	public void setMusic_link(String music_link) {
		this.music_link = music_link;
	}

	public String getMusic_link() {
		return music_link;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getPrice() {
		return price;
	}

	public void setAlbum_id(String album_id) {
		this.album_id = album_id;
	}

	public String getAlbum_id() {
		return album_id;
	}

	public void setArtist_name(String artist_name) {
		this.artist_name = artist_name;
	}

	public String getArtist_name() {
		return artist_name;
	}

	public void setMovie_name(String movie_name) {
		this.movie_name = movie_name;
	}

	public String getMovie_name() {
		return movie_name;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flag) {
		
		
		dest.writeString(album_id);
		dest.writeString(music_id);
		dest.writeString(song_name);
		dest.writeString(artist_name);
		dest.writeString(movie_name);
		dest.writeString(music_link);
		dest.writeString(price);
		dest.writeString(bypass_status);
		dest.writeString(ringtone_link);
		dest.writeString(ringtone_size);
		dest.writeString(music_size);

	}

	/**
	 * * * Called from the constructor to create this * object from a parcel. *
	 * 
	 * @param in
	 *            parcel from which to re-create object
	 */
	private void readFromParcel(Parcel in) {
		album_id = in.readString();
		music_id = in.readString();
		song_name = in.readString();
		artist_name = in.readString();
		movie_name = in.readString();
		music_link = in.readString();
		price = in.readString();
		bypass_status = in.readString();
		ringtone_link = in.readString();
		ringtone_size = in.readString();
		music_size = in.readString();
	}

	
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public AlbumSongDetailPackage createFromParcel(Parcel in) {
			return new AlbumSongDetailPackage(in);
		}

		public AlbumSongDetailPackage[] newArray(int size) {
			return new AlbumSongDetailPackage[size];
		}
	};

	public AlbumSongDetailPackage newCopy() {
		AlbumSongDetailPackage albumdetail = new AlbumSongDetailPackage();
		albumdetail.setAlbum_id(album_id);
		albumdetail.setMusic_id(music_id);
		albumdetail.setSong_name(song_name);
		albumdetail.setArtist_name(artist_name);
		albumdetail.setMovie_name(movie_name);
		albumdetail.setMusic_link(music_link);
		albumdetail.setPrice(price);
		albumdetail.setBypass_status(bypass_status);
		albumdetail.setRingtone_link(ringtone_link);
		albumdetail.setRingtone_size(ringtone_size);
		albumdetail.setMusic_size(music_size);
		return albumdetail;
	}
}
