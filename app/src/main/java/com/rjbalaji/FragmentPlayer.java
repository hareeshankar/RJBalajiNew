package com.rjbalaji;

import h.thunderbird.phoenix.rjbalaji.R;

import java.io.File;
import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.rjbalaji.Common.Common;
import com.rjbalaji.db.dbHelper;
import com.rjbalaji.interfaces.Constants;
import com.rjbalaji.player.PlayerService;
import com.rjbalaji.player.PlayerService.Error;
import com.rjbalaji.player.PlayerService.LoopMode;
import com.rjbalaji.player.PlayerService.PlayerStateListener;
import com.rjbalaji.rjbalajipackage.AlbumSongDetailPackage;
import com.rjbalaji.util.Logger;
import com.rjbalaji.util.Settings;
import com.rjbalaji.util.Utilities;

public class FragmentPlayer extends Fragment implements PlayerStateListener,
		OnClickListener, SeekBar.OnSeekBarChangeListener {

	private int screenWidth;
	private ImageView img_play_full;
	private ImageView img_prev_full;
	private ImageView img_next_full;
	private SeekBar songProgressBar;
	private TextView songTotalDurationLabel;
	private TextView songCurrentDurationLabel;
	private List<AlbumSongDetailPackage> musicList;
	private AlbumSongDetailPackage selectedSong;
	private ImageView iv_download;
	private int currentPos;
	private String MUSIC_TAG;

	public FragmentPlayer() {
	}

	View rootView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		dbHelper loDb = new dbHelper(getActivity());
		try {
			currentPos = getArguments().getInt("selectedPos");
			MUSIC_TAG = getArguments().getString("tag");
		} catch (Exception e) {
		}
		musicList = loDb.getSelectedAlbumData(MUSIC_TAG);
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.player_screen, container,
					false);
		} else {
			final ViewParent parent = rootView.getParent();
			if (parent != null && parent instanceof ViewGroup)
				((ViewGroup) parent).removeView(rootView);
		}
		((BaseActivity) getActivity()).updateActionBar(false,
				musicList.get(currentPos).getSong_name(), false, false);
		AdView adView = (AdView) rootView.findViewById(R.id.ad);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);
		initializeComponents();
		updatePlayerButtons();
		return rootView;
	}

	private void initializeComponents() {
		iv_download = (ImageView) rootView.findViewById(R.id.iv_download);
		// Settings setting = Settings.getInstance(getActivity());
		// String response = setting.getData(Settings.MUSIC_ALBUM);
		// if (response != null && !response.equals("")) {
		// // albumRecords = FragmentNewest.parseData((Object) response,
		// // MUSIC_TAG);
		// } else {
		// Toast.makeText(getActivity(),
		// "Please check your internet connection.",
		// Toast.LENGTH_SHORT).show();
		// }
		songProgressBar = (SeekBar) rootView.findViewById(R.id.songProgressBar);
		songProgressBar.setOnSeekBarChangeListener(this); // Important
		songProgressBar.setProgress(0);
		songProgressBar.setMax(100);
		songProgressBar.setSecondaryProgress(0);
		songTotalDurationLabel = (TextView) rootView
				.findViewById(R.id.songTotalDurationLabel);
		songCurrentDurationLabel = (TextView) rootView
				.findViewById(R.id.songCurrentDurationLabel);
		// new ArrayList<AlbumSongDetailPackage>();
		PlayerService.service.registerPlayerStateListener(this);

		// ///////---------Assign Variable--------//
		img_play_full = (ImageView) rootView.findViewById(R.id.img_play_full);
		img_prev_full = (ImageView) rootView.findViewById(R.id.img_prev_full);
		img_next_full = (ImageView) rootView.findViewById(R.id.img_next_full);

		if (musicList != null && musicList.size() > 0) {
			selectedSong = musicList.get(currentPos);
			updateMusicTitle(selectedSong);
			// ((TextView)
			// musicDetailView.findViewById(R.id.txt_bottom_sub_title))
			// .setText(selectedSong.getArtist_name());
			if (musicList.size() - 1 > currentPos) {
				img_next_full.setEnabled(true);
				img_next_full.setBackgroundResource(R.drawable.next);
			}
			if (currentPos == musicList.size() - 1) {
				img_next_full.setEnabled(false);
				img_next_full.setBackgroundResource(R.drawable.next_disable);
			}
			if (currentPos == 0) {
				img_prev_full.setEnabled(false);
				img_prev_full.setBackgroundResource(R.drawable.prev_disable);
			}
		} else {
			img_prev_full.setEnabled(false);
			img_prev_full.setBackgroundResource(R.drawable.prev_disable);
			img_play_full.setEnabled(false);
			img_next_full.setEnabled(false);
			img_next_full.setBackgroundResource(R.drawable.next_disable);
		}
		img_play_full.setOnClickListener(this);
		img_next_full.setOnClickListener(this);
		img_prev_full.setOnClickListener(this);
		rootView.findViewById(R.id.iv_download).setOnClickListener(this);
	}

	private void updatePlayerButtons() {
		if (PlayerService.service != null
				&& PlayerService.service.isCurrrentAudioPlaying()) {
			if (PlayerService.service.isCurrrentAudioPlaying()) {
				updateMusicButtons(false);
			} else {
				updateMusicButtons(true);
			}
			updateProgressBar();

			selectedSong = PlayerService.service.getCurrentPlayingTrack();
			if (selectedSong != null)
				updateMusicTitle(selectedSong);

			if (PlayerService.service.isShuffling()) {
				updateNextPrevButton();
				// btnShuffle
				// .setBackgroundResource(R.drawable.ic_music_suffle_selected);
				// btnRepeat.setBackgroundResource(R.drawable.ic_music_repeat);
			}
			if (PlayerService.service.getLoopMode() == LoopMode.REAPLAY_SONG) {
				updateNextPrevButton();
				// btnRepeat
				// .setBackgroundResource(R.drawable.ic_music_repeat_selected);
				// btnShuffle.setBackgroundResource(R.drawable.ic_music_suffle);
			}
		}
	}

	public void updateMusicButtons(boolean isPause) {
		if (isPause) {
			img_play_full.setBackgroundResource(R.drawable.play);
		} else {
			img_play_full.setBackgroundResource(R.drawable.pause);
		}
	}

	private void updateNextPrevButton() {

		int current_pos = PlayerService.service
				.getCurrentPlayingTrackPosition();
		if (current_pos < musicList.size()) {
			if (current_pos == musicList.size() - 1) {
				img_next_full.setEnabled(false);
				img_next_full.setBackgroundResource(R.drawable.next_disable);
				img_prev_full.setEnabled(true);
				img_prev_full.setBackgroundResource(R.drawable.prev);
			} else if (current_pos == 0) {
				img_next_full.setEnabled(true);
				img_next_full.setBackgroundResource(R.drawable.next);
				img_prev_full.setEnabled(false);
				img_prev_full.setBackgroundResource(R.drawable.prev_disable);
			} else {
				img_next_full.setEnabled(true);
				img_next_full.setBackgroundResource(R.drawable.next);
				img_prev_full.setEnabled(true);
				img_prev_full.setBackgroundResource(R.drawable.prev);
			}
		}
		currentPos = current_pos;
	}

	private void nextPrevMusic(boolean isNext) {
		// stopOtherPlayers();

		if (Utilities.isListEmpty(PlayerService.service.getPlayingQueue())) {
			// PlayerService.service.addToQueue(albumRecords);
		}
		updateMusicButtons(false);
		if (isNext)
			PlayerService.service.next();
		else
			PlayerService.service.previous();
		// musicTrack.updateList(String.valueOf(PlayerService.service
		// .getCurrentPlayingTrackPosition()));
	}

	private void updateMusicTitle(AlbumSongDetailPackage selectedSong1) {
		selectedSong = selectedSong1;
		((TextView) rootView.findViewById(R.id.txt_bottom_song_name))
				.setText(selectedSong.getSong_name());
		((BaseActivity) getActivity()).updateActionBar(false,
				musicList.get(currentPos).getSong_name(), false, false);
		if (Common.isFileExist(selectedSong.getSong_name() + ".mp3",
				getActivity())) {
			iv_download.setImageResource(R.drawable.offline);
		} else {
			iv_download.setImageResource(R.drawable.online);
		}
		// ((TextView) musicDetailView.findViewById(R.id.txt_bottom_sub_title))
		// .setText(selectedSong.getArtist_name());

	}

	boolean isPressed = false;

	private void onNextPreviousClick(boolean isNext) {
		Logger.e("isPressed " + isPressed);
		if (!isPressed) {
			Logger.e("isPressed inner" + isPressed);
			isPressed = true;
			progress_buffering = 0;
			clearQueue();
			nextPrevMusic(isNext);
			updateNextPrevButton();
		}
	}

	ProgressDialog progressDialog;

	private void startBufferingDialog() {
		try {
			if (progressDialog == null
					|| (progressDialog != null && !progressDialog.isShowing() && !getActivity()
							.isFinishing())) {
				progressDialog = new ProgressDialog(getActivity());
				progressDialog.setMessage("Buffering...");
				progressDialog.setCancelable(false);
				progressDialog.show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void dismissBufferingDialog() {
		if (progressDialog != null && progressDialog.isShowing())
			progressDialog.dismiss();
		isPressed = false;
	}

	int progress_buffering;

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		Logger.errorLog("progress: " + progress);
		// if (!isFromDownload)

		if (rootView.findViewById(R.id.progress_buffer) != null)
			if (progress_buffering != 0) {
				dismissBufferingDialog();

				if (progress_buffering != 100 && progress_buffering <= progress) {
					rootView.findViewById(R.id.progress_buffer).setVisibility(
							View.VISIBLE);
				} else
					rootView.findViewById(R.id.progress_buffer).setVisibility(
							View.GONE);
			} else {
				// startBufferingDialog();
			}
	}

	private void clearQueue() {

		// if (PlayerService.service.currentAlbumId == null
		// || (PlayerService.service.currentAlbumId != null && !jsonDetails
		// .getApp_id().equals(
		// PlayerService.service.currentAlbumId))) {
		// // checkShuffleAndReplay();
		// PlayerService.service.clearPlayingQueue();
		// progress_buffering = 0;
		// PlayerService.service.setPausePos(0);
		// }
		// PlayerService.service.currentAlbumName = jsonDetails.getApp_name();
		// PlayerService.service.cate_id = jsonDetails.getCategory_id();
		// PlayerService.service.currentAlbumId = jsonDetails.getApp_id();
		// Settings setting = Settings.getInstance(act);
		// setting.setAlbumID(PlayerService.service.currentAlbumId);
		// setting.setCateID(PlayerService.service.cate_id);
	}

	// //--------------Music Callbacks-----------------//
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		try {
			if (PlayerService.service.isCurrrentAudioPlaying()) {
				mHandler.removeCallbacks(mUpdateTimeTask);
				int totalDuration = PlayerService.service.getDuration();
				int currentPosition = Common.progressToTimer(
						seekBar.getProgress(), totalDuration);
				// forward or backward to certain seconds
				PlayerService.service.seekTo(currentPosition);
				// update timer progress again
				updateProgressBar();
			} else {
				updateProgressBar();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	public void updateProgressBar() {
		mHandler.post(mUpdateTimeTask);
	}

	@Override
	public void onStartLoadingTrack(AlbumSongDetailPackage track) {
		selectedSong = track;
		if (progress_buffering == 0) {
			String fname = track.getSong_name() + ".mp3";
			File folder = new File(Common.getBasePath(getActivity()) + fname);
			if (!folder.exists())
				startBufferingDialog();
		} else
			rootView.findViewById(R.id.progress_buffer).setVisibility(
					View.VISIBLE);
		// if (PlayerService.service.currentAlbumId != null
		// && jsonDetails.getApp_id().equals(
		// PlayerService.service.currentAlbumId)) {
		updateMusicTitle(track);
		// ((TextView) musicDetailView
		// .findViewById(R.id.txt_bottom_sub_title)).setText(track
		// .getArtist_name());
		// }

		Logger.iLog("Music CallBack", "Music CallBack: onStartLoadingTrack");

	}

	@Override
	public void onTrackLoadingBufferUpdated(AlbumSongDetailPackage track,
			int percent) {
		selectedSong = track;
		// if (PlayerService.service.currentAlbumId != null
		// && jsonDetails.getApp_id().equals(
		// PlayerService.service.currentAlbumId)) {
		progress_buffering = percent;
		songProgressBar.setSecondaryProgress(percent);
		// }
	}

	@Override
	public void onStartPlayingTrack(AlbumSongDetailPackage track) {
		// if (PlayerService.service.currentAlbumId != null
		// && jsonDetails.getApp_id().equals(
		// PlayerService.service.currentAlbumId)) {
		selectedSong = track;
		Logger.iLog("Music CallBack", "Music CallBack: onStartPlayingTrack");
		rootView.findViewById(R.id.progress_buffer).setVisibility(View.GONE);
		updateMusicButtons(false);
		// updateMusicTitle(track);
		dismissBufferingDialog();
		updateProgressBar();
		// }
	}

	@Override
	public void onFinishPlayingTrack(AlbumSongDetailPackage track) {
		// if (PlayerService.service.currentAlbumId != null
		// && jsonDetails.getApp_id().equals(
		// PlayerService.service.currentAlbumId)) {
		selectedSong = track;
		Logger.iLog("Music CallBack", "Music CallBack: onFinishPlayingTrack");
		progress_buffering = 0;
		rootView.findViewById(R.id.progress_buffer).setVisibility(View.GONE);
		songProgressBar.setSecondaryProgress(0);
		updateMusicButtons(true);
		// }
		if (PlayerService.service != null)
			PlayerService.service.cancelNotification();
	}

	@Override
	public void onFinishPlayingQueue() {
		// if (PlayerService.service.currentAlbumId != null
		// && jsonDetails.getApp_id().equals(
		// PlayerService.service.currentAlbumId)) {
		Logger.iLog("Music CallBack", "Music CallBack: onFinishPlayingQueue");
		rootView.findViewById(R.id.progress_buffer).setVisibility(View.GONE);
		mHandler.removeCallbacks(mUpdateTimeTask);
		songProgressBar.setSecondaryProgress(0);
		updateMusicButtons(true);
		// }
	}

	@Override
	public void onSleepModePauseTrack(AlbumSongDetailPackage track) {
		// TODO Auto-generated method stub
		selectedSong = track;

	}

	File rootDir = Environment.getExternalStorageDirectory();

	@Override
	public void onClick(View v) {
		// if (Common.isInternetAvailable(getActivity())) {
		switch (v.getId()) {
		case R.id.iv_download:
			if (selectedSong == null)
				selectedSong = PlayerService.service.getCurrentPlayingTrack();
			if (!Common.isFileExist(selectedSong.getSong_name() + ".mp3",
					getActivity())) {
				if (Common.isInternetAvailable(getActivity())) {
					String fileURL = selectedSong.getMusic_link();
					String fileName = ((TextView) rootView
							.findViewById(R.id.txt_bottom_song_name)).getText()
							.toString()
							+ ".mp3";
					File file = new File(rootDir + "/"
							+ getActivity().getString(R.string.app_name) + "/",
							fileName);
					File folder = new File(
							Environment.getExternalStorageDirectory()
									+ "/"
									+ getActivity()
											.getString(R.string.app_name));
					if (!folder.exists()) {
						boolean success = folder.mkdir();
					}
					if (PlayerService.service.isPlaying()) {
						PlayerService.service.clearPlayingQueue();
						PlayerService.service.stop();
					}
					new DownloadProcess(getActivity(), 0, file, fileName,
							fileURL, iv_download).execute();
				} else {
					Toast.makeText(getActivity(),
							Constants.MSG_CONNECTION_ERROR, Toast.LENGTH_SHORT)
							.show();
				}
			} else {
				Toast.makeText(getActivity(), "File already downloaded.",
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.img_play_full:
			Settings setting = Settings.getInstance(getActivity());
			setting.setAlbumName(getArguments().getString("albumName"));
			playFromPosition(currentPos);
			break;
		case R.id.img_prev_full:
			setting = Settings.getInstance(getActivity());
			setting.setAlbumName(getArguments().getString("albumName"));
			onNextPreviousClick(false);

			break;
		case R.id.img_next_full:
			setting = Settings.getInstance(getActivity());
			setting.setAlbumName(getArguments().getString("albumName"));
			onNextPreviousClick(true);
			break;
		default:
			break;
		}
		// } else {
		// Toast.makeText(getActivity(), "Please check internet connections",
		// Toast.LENGTH_SHORT).show();
		// }
	}

	private void startMusic() {
		updateMusicButtons(false);
		PlayerService.service.playNow(musicList);
	}

	private void pauseMusic() {
		mHandler.removeCallbacks(mUpdateTimeTask);
		// TODO Auto-generated method stub
		updateMusicButtons(true);
		PlayerService.service.pause();
		// mediaPausePos = PlayerService.service.getCurrentPlayingPosition();
		PlayerService.service.setPausePos(PlayerService.service
				.getCurrentPlayingPosition());
	}

	private void playFromPosition(int arg2) {
		clearQueue();

		if (Utilities.isListEmpty(PlayerService.service.getPlayingQueue())
				|| !musicList
						.get(arg2)
						.getMusic_id()
						.equals(PlayerService.service.getCurrentPlayingTrack()
								.getMusic_id())) {
			Logger.logger("onItemClick 1");
			if (Utilities.isListEmpty(PlayerService.service.getPlayingQueue())) {
				PlayerService.service.addToQueue(musicList);
			}
			progress_buffering = 0;
			PlayerService.service.playFromPositionWithID(musicList.get(arg2)
					.getMusic_id());
			// PlayerService.service.playFromPosition(currentPos);
			updateMusicButtons(false);
		} else {
			if (PlayerService.service != null
					&& PlayerService.service.isCurrrentAudioPlaying()
					&& !PlayerService.service.isQueueCompleted()) {
				Logger.logger("onItemClick 2");
				pauseMusic();
			} else if (PlayerService.service != null
					&& !PlayerService.service.isCurrrentAudioPlaying()
					&& PlayerService.service.getCurrentPlayingPosition() != 0) {
				Logger.logger("onItemClick 3");
				resumeMusic();
			} else if (PlayerService.service.isQueueCompleted()) {
				Logger.logger("onItemClick 4");
				playFromPos();
			} else {
				Logger.logger("onItemClick 5");
				play();
			}
		}
	}

	private void playFromPos() {
		// TODO Auto-generated method stub
		PlayerService.service.playFromPositionWithID(PlayerService.service
				.getCurrentPlayingTrack().getMusic_id());
		updateMusicButtons(false);
	}

	private void resumeMusic() {
		updateMusicButtons(false);
		PlayerService.service.seekTo(PlayerService.service.getPausePos());
		PlayerService.service.play();
		updateProgressBar();
	}

	public void play() {
		progress_buffering = 0;
		img_play_full.setBackgroundResource(R.drawable.pause);
		PlayerService.service.play();
	}

	Handler mHandler = new Handler();
	Runnable mUpdateTimeTask = new Runnable() {
		public void run() {

			try {
				if (PlayerService.service != null) {
					long totalDuration = PlayerService.service.getDuration();
					long currentDuration = PlayerService.service
							.getCurrentPlayingPosition();
					// Displaying Total Duration time
					songTotalDurationLabel.setText(""
							+ Common.milliSecondsToTimer(totalDuration));
					// Displaying time completed playing
					songCurrentDurationLabel.setText(""
							+ Common.milliSecondsToTimer(currentDuration));

					// Updating progress bar
					int progress = (int) (Common.getProgressPercentage(
							currentDuration, totalDuration));
					// Log.d("Progress", ""+progress);
					songProgressBar.setProgress(progress);

					// Running this thread after 100 milliseconds
					mHandler.postDelayed(this, 100);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	};

	void killMediaPlayer() {
		mHandler.removeCallbacks(mUpdateTimeTask);
		try {
			if (PlayerService.service != null) {
				PlayerService.service.stop();
				PlayerService.service.reset();
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		// PlayerService.service = null;
		img_play_full.setBackgroundResource(R.drawable.play);
	}

	// @Override
	// public void onBackPressed() {
	// killMediaPlayer();
	// super.onBackPressed();
	// }

	@Override
	public void onDestroy() {
		PlayerService.service.unregisterPlayerStateListener(this);
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onErrorHappened(Error error) {
		// if (PlayerService.service.currentAlbumId != null
		// && jsonDetails.getApp_id().equals(
		// PlayerService.service.currentAlbumId)) {
		Logger.iLog("Music CallBack", "Music CallBack: onErrorHappened");
		rootView.findViewById(R.id.progress_buffer).setVisibility(View.GONE);
		dismissBufferingDialog();
		updateMusicButtons(true);
		// }
	}

}