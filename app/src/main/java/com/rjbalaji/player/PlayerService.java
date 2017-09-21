package com.rjbalaji.player;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.rjbalaji.Common.Common;
import com.rjbalaji.interfaces.Constants;
import com.rjbalaji.rjbalajipackage.AlbumSongDetailPackage;
import com.rjbalaji.util.Logger;
import com.rjbalaji.util.Utilities;

public class PlayerService extends Service implements
		OnAudioFocusChangeListener, MediaPlayer.OnCompletionListener,
		MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnErrorListener {
	private static final String LOG = "PlayerUpdateWidgetService";
	private static final String EXTRA_START = "start";
	private static final String EXTRA_COMMAND = "command";
	private static final String EXTRA_STOP = "stop";
	private static final String EXTRA_CANCEL = "cancel";

	private static final String EXTRA_PREVIOUS = "previous";
	private static final String EXTRA_NEXT = "next";
	private static final String TAG = "PlayerService";
	public static final String TRACK_FINISHED = "track_finished";
	private int pause_pos = 0;

	private boolean isErrorOccured = false;
	public String currentAlbumId;
	public String currentAlbumName;
	public String cate_id;

	public enum State {

		/**
		 * When the Player is created and no {@link Track} is registered to be
		 * played.
		 */
		IDLE,

		/**
		 * When the selected {@link Track} is in the state of being loaded
		 * before playing.
		 */
		INTIALIZED,

		/**
		 * When the selected {@link Track} has been loaded, prepared and is
		 * ready to be played.
		 */
		PREPARED,

		/**
		 * When the selected {@link Track} is being played.
		 */
		PLAYING,

		/**
		 * When the selected {@link Track} is being paused.
		 */
		PAUSED,

		/**
		 * When the selected {@link Track} is being stopped and the whole
		 * process of loading this / new track is required.
		 */
		STOPPED,

		/**
		 * When the selected {@link Track} has been done playing due to
		 * completion of the track.
		 */
		COMPLETED,

		/**
		 * When the selected {@link PlayingQueue} has been done playing.
		 */
		COMPLETED_QUEUE;
	}

	public static PlayerService service;

	public enum LoopMode {
		OFF, ON, REAPLAY_SONG
	}

	public enum Error implements Serializable {
		NO_CONNECTIVITY(1), SERVER_ERROR(2), DATA_ERROR(3), TRACK_SKIPPED(4);

		private final int id;

		Error(int id) {
			this.id = id;
		}

		public int getId() {
			return this.id;
		}

		public static final Error getErrorById(int id) {
			if (id == NO_CONNECTIVITY.getId()) {
				return NO_CONNECTIVITY;
			} else if (id == SERVER_ERROR.getId()) {
				return SERVER_ERROR;
			} else if (id == TRACK_SKIPPED.getId()) {
				return TRACK_SKIPPED;
			} else {
				return DATA_ERROR;
			}
		}

	}

	// @Override
	// public void onStart(Intent intentq, int startId) {
	//
	// Logger.i(LOG, " onStart Called");
	// super.onStart(intentq, startId);
	//
	// }

	// private void update() {
	// if (PlayerService.service != null) {
	// AlbumSongDetailPackage track = PlayerService.service
	// .getCurrentPlayingTrack();
	// RemoteViews remoteViewNotification = new RemoteViews(this
	// .getApplicationContext().getPackageName(),
	// R.layout.player_widget_notification);
	// if (track != null) {
	// remoteViewNotification.setTextViewText(
	// R.id.player_widget_song_title,
	// "" + track.getSong_name());
	// remoteViewNotification.setTextViewText(
	// R.id.player_widget_song_detail,
	// "" + track.getMovie_name());
	// if (PlayerService.service.getState() == State.STOPPED) {
	// needNotToShowNotification = true;
	// } else {
	// needNotToShowNotification = false;
	// }
	// } else {
	// needNotToShowNotification = true;
	// }
	//
	// if (PlayerService.service.getState() != State.PAUSED
	// && (PlayerService.service.isPlaying() || PlayerService.service
	// .isLoading())) {
	// // Player is in playing or loading state.
	// remoteViewNotification.setViewVisibility(
	// R.id.player_widget_button_play, View.GONE);
	// remoteViewNotification.setViewVisibility(
	// R.id.player_widget_button_pause, View.VISIBLE);
	// Log.e("AppWidgetManager", "1");
	// } else {
	// remoteViewNotification.setViewVisibility(
	// R.id.player_widget_button_play, View.VISIBLE);
	// remoteViewNotification.setViewVisibility(
	// R.id.player_widget_button_pause, View.GONE);
	// Log.e("AppWidgetManager", "2");
	// }
	//
	// Notification notification;
	//
	// // notification = new Notification.Builder(getBaseContext())
	// // .setContent(remoteViewNotification).setAutoCancel(false)
	// // .setOngoing(true).build();
	// notification = new NotificationCompat.Builder(getBaseContext())
	// .setContent(remoteViewNotification).setAutoCancel(false)
	// .setOngoing(true).build();
	//
	// notification.icon = R.drawable.ic_launcher_m;
	// // notification.setSmallIcon(R.drawable.icon_launcher);
	//
	// // if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
	// notification = new Notification();
	// notification.contentView = remoteViewNotification;
	// notification.flags |= Notification.FLAG_ONGOING_EVENT;
	// notification.icon = R.drawable.ic_launcher_m;
	//
	// try {
	// NotificationManager manager = (NotificationManager)
	// getSystemService(NOTIFICATION_SERVICE);
	// if (!needNotToShowNotification) {
	//
	// Intent playIntent = new Intent(this, PlayerService.class);
	// playIntent.putExtra(EXTRA_COMMAND, EXTRA_START);
	// PendingIntent pendingIntent = PendingIntent.getService(
	// getApplicationContext(), 5555, playIntent,
	// PendingIntent.FLAG_UPDATE_CURRENT);
	// remoteViewNotification.setOnClickPendingIntent(
	// R.id.player_widget_button_play, pendingIntent);
	//
	// Intent pauseclickIntent = new Intent(getBaseContext(),
	// PlayerService.class);
	// pauseclickIntent.putExtra(EXTRA_COMMAND, EXTRA_STOP);
	// pendingIntent = PendingIntent.getService(
	// getApplicationContext(), 5556, pauseclickIntent,
	// PendingIntent.FLAG_UPDATE_CURRENT);
	// remoteViewNotification.setOnClickPendingIntent(
	// R.id.player_widget_button_pause, pendingIntent);
	//
	// Intent cancelIntent = new Intent(this, PlayerService.class);
	// cancelIntent.putExtra(EXTRA_COMMAND, EXTRA_CANCEL);
	// PendingIntent pendingIntentCancel = PendingIntent
	// .getService(getApplicationContext(), 5557,
	// cancelIntent,
	// PendingIntent.FLAG_UPDATE_CURRENT);
	// remoteViewNotification.setOnClickPendingIntent(
	// R.id.player_widget_button_cancel,
	// pendingIntentCancel);
	//
	// Intent startHomeIntent = new Intent(this,
	// HomeActivity.class);
	//
	// startHomeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
	// | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	// startHomeIntent.putExtra("donothing", true);
	// PendingIntent startHomePendingIntent = PendingIntent
	// .getActivity(this, NOTIFICATION_PLAYING_CODE,
	// startHomeIntent, 0);
	//
	// notification.flags |= Notification.FLAG_NO_CLEAR;
	// notification.contentIntent = startHomePendingIntent;
	// manager.notify(NOTIFICATION_PLAYING_CODE, notification);
	// // Log.e("Notification Notify", "@@@@@@@@@@@");
	// } else {
	// manager.cancel(NOTIFICATION_PLAYING_CODE);
	// // Log.e("Notification cancel", "@@@@@@@@@@@");
	// }
	// } catch (Exception e) {
	// Logger.printStackTrace(e);
	// }
	// }
	//
	// }

	/**
	 * Interface definition to be invoked when the state of the player has been
	 * changed.
	 */
	public interface PlayerStateListener {

		public void onStartLoadingTrack(AlbumSongDetailPackage track);

		public void onTrackLoadingBufferUpdated(AlbumSongDetailPackage track,
				int precent);

		public void onStartPlayingTrack(AlbumSongDetailPackage track);

		public void onFinishPlayingTrack(AlbumSongDetailPackage track);

		public void onFinishPlayingQueue();

		public void onSleepModePauseTrack(AlbumSongDetailPackage track);

		public void onErrorHappened(PlayerService.Error error);

	}

	public class PlayerSericeBinder extends Binder {
		public PlayerService getService() {
			return PlayerService.this;
		}
	}

	private Context mContext;
	// identification of the service in the system.
	private int mServiceStartId;

	// binder for controlling the service from other components.
	private final IBinder mPlayerSericeBinder = new PlayerSericeBinder();

	// audio handler members:
	private AudioManager mAudioManager;
	private WakeLock mWakeLock;

	private MediaPlayer mMediaPlayer;
	private volatile State mCurrentState;
	private volatile AlbumSongDetailPackage mCurrentTrack;

	private Thread mMediaLoaderWorker = null;
	private MediaLoaderHandler mMediaLoaderHandler = null;

	private ServiceHandler mServiceHandler;
	private Set<PlayerStateListener> mOnPlayerStateChangedListeners = new HashSet<PlayerService.PlayerStateListener>();

	// Event logging fields.
	// xtpl
	// private static final SimpleDateFormat sSimpleDateFormat = new
	// SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'",Locale.ENGLISH);//Changes by
	// Hungama
	// xtpl

	// playing mode, identifies if playing music or radio. Deafult is Music.
	private volatile LoopMode mLoopMode = LoopMode.OFF;

	// private SleepReciever mSleepReciever;
	private volatile boolean mShouldPauseAfterLoading = false;

	// shuffling - every day :)
	private boolean mIsShuffling = false;

	private PlayingQueue mPlayingQueue = null;
	private PlayingQueue mOriginalPlayingQueue = null;

	public static final int TIME_REPORT_BADGES_MILLIES = 120000;

	private boolean mIsPausedByAudiofocusLoss = false;

	private volatile boolean mIsExplicitMarkedExit = false;

	// Calculate bandwidth
	private boolean firstEntry = true;
	private boolean lastEntry = true;
	private int percentStart;
	private long startTimeToCalculateBitrate;
	private long endTimeToCalculateBitrate;
	/*
	 * The Media Handle of any playing track should be updated after 30 minutes.
	 */
	// in playing updater.
	private PlayerProgressCounter mPlayerProgressCounter;

	private PlayerBarUpdateListener mOnPlayerBarUpdateListener;

	// ======================================================
	// Service life cycle.
	// ======================================================

	boolean restored;

	@Override
	public void onCreate() {
		super.onCreate();

		// creates binder to the service to interface between other controlling
		// components.
		service = this;
		mContext = getApplicationContext();

		mPlayingQueue = new PlayingQueue(null, 0);
		// mPlayingQueue = mDataManager
		// .getStoredPlayingQueue(mApplicationConfigurations);
		// if (mPlayingQueue.size() > 0) {
		// mPlayMode = PlayMode.MUSIC;
		// restored = true;
		// }

		// initializing the audio manager to gain audio focus.
		mAudioManager = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);

		// creates a lock on the CPU to avoid the OS stops playing in state of
		// idling.
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this
				.getClass().getName());
		mWakeLock.setReferenceCounted(false);
		mWakeLock.acquire();

		// initializes the service's handler.
		mServiceHandler = new ServiceHandler();

		// initializes the media player.
		initializeMediaPlayer();

		// registers a receiver for sleep requests.
		// mSleepReciever = new SleepReciever(this);
		// IntentFilter sleepFilter = new IntentFilter(
		// SleepModeManager.COUNT_DOWN_TIMER_FINISH_INTENT);
		// registerReceiver(mSleepReciever, sleepFilter);

		IntentFilter callFilter = new IntentFilter(
				TelephonyManager.ACTION_PHONE_STATE_CHANGED);
		registerReceiver(callReceiver, callFilter);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		System.out.println(" ::::::::::::: onStartCommand :::::::::::: "
				+ mCurrentState);
		mServiceStartId = startId;
		// onStart(intent, startId);
		// if (mCurrentState == State.IDLE) {
		// play();
		// }
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		try {
			unregisterReceiver(callReceiver);
			callReceiver = null;
		} catch (Exception e) {
		}

		// stops any playing / loading track.
		stop();

		// unregisters the receiver for sleep requests.
		// unregisterReceiver(mSleepReciever);
		// mSleepReciever = null;

		// destroy the service's handler.
		mServiceHandler.removeCallbacksAndMessages(null);
		mServiceHandler = null;

		// destroy the media player.
		destroyMediaPlayer();

		// release the lock on the CPU.
		mWakeLock.release();
		mWakeLock = null;

		dismissNotification();
		service = null;
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mPlayerSericeBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if (isAllowSelfTermination()) {
			stopSelf(mServiceStartId);
		}
		return false;
	}

	// ======================================================
	// Playing Listeners and Callbacks.
	// ======================================================

	private static final String MESSAGE_VALUE = "message_value";
	private static final String MESSAGE_ERROR_VALUE = "message_error_value";

	/*
	 * starts loading track's media handle from CM servers to get the playing
	 * URL / internal path to play.
	 */
	private static final int MESSAGE_START_LOADING_TRACK = 1;
	/*
	 * indication for updating the buffer of the loading track before / while it
	 * been played.
	 */
	private static final int MESSAGE_LOADING_TRACK_BUFFER_UPDATE = 2;
	/*
	 * Indication of that the track that is been initially been loaded and ready
	 * to been played.
	 */
	private static final int MESSAGE_LOADING_TRACK_PREPARED = 3;
	/*
	 * Indication that the current loading track process has been cancelled,
	 * generally to play another track.
	 */
	private static final int MESSAGE_LOADING_TRACK_CANCELLED = 4;
	/*
	 * The track has been finished to being played, generally moving to the next
	 * track in the queue.
	 */
	private static final int MESSAGE_FINISH_PLAYING_TRACK = 5;
	/*
	 * Done playing all the queue of tracks.
	 */
	private static final int MESSAGE_FINISH_PLAYING_QUEUE = 6;
	/*
	 * Another application temporarlly requests the focus on the audio.
	 */
	private static final int MESSAGE_AUDIOFOCUS_LOSS_TRANSIENT = 7;
	/*
	 * The audio focus gained back to the player.
	 */
	private static final int MESSAGE_AUDIOFOCUS_GAIN = 8;
	/*
	 * No more audio focus to the player.
	 */
	private static final int MESSAGE_AUDIOFOCUS_LOSS = 9;
	/*
	 * An error has occurred.
	 */
	private static final int MESSAGE_ERROR = 10;

	private static final int MESSAGE_SKIP_CURRENT_TRACK = 11;

	/**
	 * Handles all the service's components messages and performs the logic
	 * business.
	 */

	// AlbumSongDetailPackage loadingTrack;

	private class ServiceHandler extends Handler {

		@Override
		public void handleMessage(Message message) {
			try {
				int what = message.what;
				switch (what) {
				case MESSAGE_START_LOADING_TRACK:
					mCurrentState = State.INTIALIZED;

					AlbumSongDetailPackage loadingTrack = (AlbumSongDetailPackage) message
							.getData().getSerializable(MESSAGE_VALUE);
					for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
						listener.onStartLoadingTrack(loadingTrack);
					}
					break;

				case MESSAGE_LOADING_TRACK_BUFFER_UPDATE:
					for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
						listener.onTrackLoadingBufferUpdated(null, message.arg1);
					}
					break;
				case MESSAGE_LOADING_TRACK_PREPARED:

					/*
					 * Checks if we are pending to exit the application.
					 */
					if (mIsExplicitMarkedExit) {
						/*
						 * get out of here, the service will handle the Media
						 * Player's state.
						 */
						return;
					}

					if (CallInProgress)
						return;

					// starts playing the track.
					mCurrentState = State.PLAYING;
					mAudioManager.requestAudioFocus(PlayerService.this,
					// Use the music stream.
							AudioManager.STREAM_MUSIC,
							// Request permanent focus.
							AudioManager.AUDIOFOCUS_GAIN);
					mMediaPlayer.start();
					updatewidget();

					// stores the timestamp

					AlbumSongDetailPackage preparedTrack = (AlbumSongDetailPackage) message
							.getData().getSerializable(MESSAGE_VALUE);
					for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
						listener.onStartPlayingTrack(preparedTrack);
					}

					/*
					 * The loading and preparing was while we received the sleep
					 * message, pauses the playing right away.
					 * 
					 * Also letting the client that we paused from the service.
					 */
					if (mShouldPauseAfterLoading) {
						// resets the flag.
						mShouldPauseAfterLoading = false;
						// pauses the the playing.
						pause();

						for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
							listener.onSleepModePauseTrack(preparedTrack);
						}
					}
					break;

				case MESSAGE_LOADING_TRACK_CANCELLED:
					// TODO: currently does nothing, check this.
					// mCurrentState = State.STOPPED;
					// mMediaPlayer.stop();
					// mMediaPlayer.reset();

					isErrorOccured = true;
					mMediaPlayer.stop();
					mCurrentState = State.STOPPED;
					for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
						listener.onFinishPlayingTrack(mCurrentTrack);
					}
					break;

				case MESSAGE_FINISH_PLAYING_TRACK:
					mCurrentState = State.COMPLETED;
					AlbumSongDetailPackage finishedTrack = (AlbumSongDetailPackage) message
							.getData().getSerializable(MESSAGE_VALUE);
					for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
						listener.onFinishPlayingTrack(finishedTrack);
					}

					// play next track.
					Logger.iLog("Debug", "MESSAGE_FINISH_PLAYING_TRACK");

					if (mLoopMode == LoopMode.REAPLAY_SONG) {
						stop();
						play();
					} else {
						stop();
						next();
					}

					break;

				case MESSAGE_FINISH_PLAYING_QUEUE:
					mCurrentState = State.COMPLETED_QUEUE;

					for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
						listener.onFinishPlayingQueue();
					}

					if (mLoopMode == LoopMode.ON) {
						// recreates the queue and starts to play from the
						// begging.
						List<AlbumSongDetailPackage> playedQueue = mPlayingQueue
								.getCopy();
						mPlayingQueue = new PlayingQueue(playedQueue, 0);

						play();
					}

					break;

				case MESSAGE_AUDIOFOCUS_LOSS_TRANSIENT:
					// is it playing or loading to play?
					if (mCurrentState == State.PLAYING
							|| mCurrentState == State.INTIALIZED
							|| mCurrentState == State.PREPARED) {
						// Pause playback
						Logger.e(TAG, "AUDIOFOCUS LOSS TRANSIENT - pausing");
						pause();
						mIsPausedByAudiofocusLoss = true;
					}
					break;

				case MESSAGE_AUDIOFOCUS_GAIN:
					// Resume playback
					if (mIsPausedByAudiofocusLoss) {
						mIsPausedByAudiofocusLoss = false;
						Logger.e(TAG, "AUDIOFOCUS GAIN - resuming play.");
						play();
					}
					break;

				case MESSAGE_AUDIOFOCUS_LOSS:
					// is it playing or loading to play?
					if (mCurrentState == State.PLAYING
							|| mCurrentState == State.INTIALIZED
							|| mCurrentState == State.PREPARED) {
						// Pause playback
						Logger.e(TAG, "AUDIOFOCUS LOSS - stop playing.");
						pause();
						mIsPausedByAudiofocusLoss = true;
					}
					break;

				case MESSAGE_ERROR:
					int errorId = message.getData().getInt(MESSAGE_ERROR_VALUE);
					PlayerService.Error error = PlayerService.Error
							.getErrorById(errorId);
					Logger.e(TAG, "Player Error: " + error.toString());
					isErrorOccured = true;
					// resets the media player.
					mMediaPlayer.reset();
					mCurrentState = State.PAUSED;
					for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
						listener.onErrorHappened(error);
					}
					Toast.makeText(getApplicationContext(),
							Constants.MSG_CONNECTION_ERROR, 1).show();
					break;
				case MESSAGE_SKIP_CURRENT_TRACK:
					mCurrentState = State.COMPLETED;

					finishedTrack = (AlbumSongDetailPackage) message.getData()
							.getSerializable(MESSAGE_VALUE);
					for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
						listener.onFinishPlayingTrack(finishedTrack);
					}

					// play next track.

					// if (mPlayMode == PlayMode.MUSIC
					// && mLoopMode == LoopMode.REAPLAY_SONG) {
					// stop();
					// play();
					//
					// } else {
					stop();
					next();
					if (!mPlayingQueue.hasNext()) {
						for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
							listener.onErrorHappened(Error.TRACK_SKIPPED);
						}
					}
					// }
					break;
				}
			} catch (Exception e) {
				Logger.e(getClass().getName() + ":584", e.toString());
			}
		}
	}

	/**
	 * Listens for changing in the playing volume focus.
	 */
	@Override
	public void onAudioFocusChange(int focusChange) {
		try {
			if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
				Message message = Message.obtain(mServiceHandler,
						MESSAGE_AUDIOFOCUS_LOSS_TRANSIENT);
				message.sendToTarget();

			} else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
				Message message = Message.obtain(mServiceHandler,
						MESSAGE_AUDIOFOCUS_GAIN);
				message.sendToTarget();

			} else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
				Message message = Message.obtain(mServiceHandler,
						MESSAGE_AUDIOFOCUS_LOSS);
				message.sendToTarget();
			}
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":605", e.toString());
		}
	}

	/**
	 * Listens for completion of the current playing track.
	 */
	@Override
	public void onCompletion(MediaPlayer mp) {

		if (mCurrentTrack != null) {

			Message message = Message.obtain(mServiceHandler,
					MESSAGE_FINISH_PLAYING_TRACK);
			Bundle data = new Bundle();
			Intent finishPlay = new Intent();
			finishPlay.setAction(TRACK_FINISHED);
			mContext.sendBroadcast(finishPlay);
			data.putSerializable(MESSAGE_VALUE, (Serializable) mCurrentTrack);
			message.setData(data);
			message.sendToTarget();
		}
	}

	/**
	 * Listens for buffering updates in the current playing track that is being
	 * prepared.
	 */
	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// if(isAdPlaying){
		// return;
		// }
		long bandwidth = 0;
		if (firstEntry) {
			firstEntry = false;
			startTimeToCalculateBitrate = System.currentTimeMillis();
			percentStart = percent;
			Logger.i(TAG, "Percent = " + percent + " Start Time = "
					+ startTimeToCalculateBitrate);
		} else if (percent == 100 && lastEntry) {
			lastEntry = false;
			endTimeToCalculateBitrate = System.currentTimeMillis();
			Logger.i(TAG, "Percent = " + percent + " End Time = "
					+ endTimeToCalculateBitrate);
			long dataPercent = (percent - percentStart);
			if (startTimeToCalculateBitrate != 0
					&& endTimeToCalculateBitrate != 0) {

				// float timeDiff = endTimeToCalculateBitrate
				// - startTimeToCalculateBitrate;
				//
				// long fileSizeInBits = mFileSize * 8;
				//
				// float per = dataPercent / 100f;
				//
				// bandwidth = (long) (((fileSizeInBits * per) / 1024f) /
				// (timeDiff / 1000));
				// Logger.i(TAG, "BANDWIDTH = " + bandwidth);

			}
		}

		Message message = Message.obtain(mServiceHandler,
				MESSAGE_LOADING_TRACK_BUFFER_UPDATE);
		message.arg1 = percent;
		message.sendToTarget();
	}

	/**
	 * Listens for errors when trying to prepare the current track.
	 */
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		isErrorOccured = true;
		mp.reset();

		switch (what) {
		case MediaPlayer.MEDIA_ERROR_UNKNOWN:
		case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
			Message message = Message.obtain(mServiceHandler, MESSAGE_ERROR);
			Bundle data = new Bundle();
			data.putInt(MESSAGE_ERROR_VALUE, Error.SERVER_ERROR.getId());
			message.setData(data);
			message.sendToTarget();
			return true;

		default:
			break;
		}
		return false;
	}

	// ======================================================
	// Service public controlling methods.
	// ======================================================

	public void setPlayingQueue(PlayingQueue playingQueue) {
		mPlayingQueue = playingQueue;
		// resets the swapper queues.
		mOriginalPlayingQueue = null;

		/*
		 * if the new playing queue is empty, removes any ongoing playing
		 * notification from the foreground.
		 */
		if (mPlayingQueue.size() == 0) {
			mCurrentState = State.STOPPED;
			dismissNotification();
		}
	}

	public void registerPlayerStateListener(PlayerStateListener listner) {
		try {
			mOnPlayerStateChangedListeners.add(listner);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void unregisterPlayerStateListener(PlayerStateListener listner) {
		try {
			mOnPlayerStateChangedListeners.remove(listner);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Stops the player if playing and adds to the queue the given tracks after
	 * it stops and continue to play.
	 * 
	 * @param tracks
	 */
	public void playNow(List<AlbumSongDetailPackage> tracks) {
		// adds the tracks to be next to the current position.
		if (mPlayingQueue.size() > 0) {
			mPlayingQueue.addNext(tracks);
			// starts playing the next.
			next();
		} else {
			mPlayingQueue.addToQueue(tracks);
			play();
		}
	}

	private boolean isPlayNowSelected = false;

	/**
	 * Stops the player if playing and adds to the queue the given tracks after
	 * it stops and continue to play.
	 * 
	 * @param tracks
	 */
	public void playNowFromPosition(List<AlbumSongDetailPackage> tracks,
			int trackPosition) {
		// checks if it's playing in other mode.
		stop();

		isPlayNowSelected = true;
		mPlayingQueue.goTo(trackPosition);
		play();
		// // adds the tracks to be next to the current position.
		// if (mPlayingQueue.size() > 0) {
		// mPlayingQueue.addNext(tracks);
		// // starts playing the next.
		// next();
		// } else {
		// mPlayingQueue.addToQueue(tracks);
		// play();
		// }
	}

	/**
	 * Adds the given tracks to the play queue after this current playing track.
	 * 
	 * @param tracks
	 */
	public void playNext(List<AlbumSongDetailPackage> tracks) {

		// checks if it's playing in other mode.

		if (mPlayingQueue.size() > 0) {
			mPlayingQueue.addNext(tracks);
		} else {
			playNow(tracks);
		}
	}

	/**
	 * Adds these tracks to the end of the queue to been played.
	 * 
	 * @param tracks
	 */
	public void addToQueue(List<AlbumSongDetailPackage> tracks) {

		// checks if it's playing in other mode.

		if (mPlayingQueue.size() > 0) {
			mPlayingQueue.addToQueue(tracks);
		} else {
			/*
			 * The user has added tracks to the queue when it was empty. Adds
			 * the tracks to the queue, and force him to presents it.
			 */
			mPlayingQueue.addToQueue(tracks);
			mCurrentTrack = mPlayingQueue.getCurrentTrack();
			// play();
			// fake invocation to make the client updates its text.
			for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
				listener.onStartLoadingTrack(mCurrentTrack);
			}
		}
	}

	public List<AlbumSongDetailPackage> getQueueList() {
		return mPlayingQueue.getQueue();
	}

	public boolean isQueueEmpty() {
		try {
			return mPlayingQueue.size() == 0;
		} catch (Exception e) {
			return true;
		}
	}

	/**
	 * Plays the current track if it was paused, if not, Plays a new one (loads
	 * and everything..) .
	 */
	public void play() {
		if (CallInProgress)
			return;

		isErrorOccured = false;
		updatewidget();
		if (mPlayingQueue.size() > 0) {
			mAudioManager.requestAudioFocus(this,
			// Use the music stream.
					AudioManager.STREAM_MUSIC,
					// Request permanent focus.
					AudioManager.AUDIOFOCUS_GAIN);

			// checks if it's currently playing.
			if (mCurrentState == State.PAUSED && !isPlayNowSelected) {
				mCurrentState = State.PLAYING;

				mMediaPlayer.start();

			} else {
				isPlayNowSelected = false;
				/*
				 * Starts the loading of the track, when it will be prepared, it
				 * will be played automatically.
				 */
				mCurrentTrack = mPlayingQueue.getCurrentTrack();
				// if (!mApplicationConfigurations.isUserHasSubscriptionPlan()
				// &&
				// adSkipCount % 3 == 0 && mPlayMode == PlayMode.MUSIC
				// && placementAudioAd != null && placementAudioAd.getMp3Audio()
				// !=
				// null && placementAudioAd.getMp3Audio().length() > 0) {
				// new Thread(new LoadAd()).start();
				// } else {
				startLoadingTrack();

				// }
			}
			updateNotificationForTrack(mCurrentTrack);
		}

	}

	Boolean needNotToShowNotification = false;

	public void updatewidget() {
		// startService(new Intent(getBaseContext(),
		// NotificationService.class));
		// NotificationManager nm;
		// nm.cancel(0);
	}

	public void playFromPosition(int newPosition) {
		if (mPlayingQueue.size() > 0
				&& mPlayingQueue.getCurrentPosition() != newPosition) {
			stop();
			mPlayingQueue.goTo(newPosition);
			play();
		}
	}

	public void playFromPositionWithID(String id) {

		int newPosition = -1;
		List<AlbumSongDetailPackage> tracks = mPlayingQueue.getCopy();
		if (mPlayingQueue.size() > 0) {
			for (int i = 0; i < tracks.size(); i++)
				if (tracks.get(i).getMusic_id().equals(id)) {
					newPosition = i;
					break;
				}

			if (newPosition != -1) {
				// playFromPosition(newPosition);
				stop();
				mPlayingQueue.goTo(newPosition);
				play();
			}

		}
	}

	// class LoadAd implements Runnable {
	// public void run() {
	// try {
	// Logger.s("start playing ad :::::: ");
	// isAdPlaying = true;
	// mMediaPlayer.reset();
	// mMediaPlayer.setDataSource(placementAudioAd.getMp3Audio());
	// mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
	// mMediaPlayer.prepare();
	// Message message = Message.obtain(mServiceHandler,
	// MESSAGE_LOADING_TRACK_PREPARED);
	// message.sendToTarget();
	// } catch (IllegalArgumentException e) {
	// isAdPlaying = false;
	// e.printStackTrace();
	// } catch (SecurityException e) {
	// isAdPlaying = false;
	// e.printStackTrace();
	// } catch (IllegalStateException e) {
	// isAdPlaying = false;
	// e.printStackTrace();
	// } catch (IOException e) {
	// isAdPlaying = false;
	// e.printStackTrace();
	// }
	//
	// }
	// };

	/**
	 * Stops playing current track.
	 */
	public void stop() {
		try {
			if (mPlayingQueue.size() > 0) {
				// stop loading if any.
				stopLoadingTrack();

				mAudioManager.abandonAudioFocus(this);
				// System.out.println("Current state ::: " +
				// mCurrentState.toString());
				// stop playing if any.
				if (mCurrentState == State.PREPARED
						|| mCurrentState == State.PAUSED
						|| mCurrentState == State.PLAYING
						|| mCurrentState == State.COMPLETED
						|| mCurrentState == State.COMPLETED_QUEUE) {
					// Logging Events are only for music.

					mCurrentState = State.STOPPED;

					mMediaPlayer.stop();
					mMediaPlayer.reset();
				}
				//
				// resets the reporting flag for hungama.
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		setPausePos(0);
		updatewidget();
	}

	public void stopPreviousQueue() {
		try {
			if (mPlayingQueue.size() > 0) {
				// stop loading if any.
				stopLoadingTrack();

				mAudioManager.abandonAudioFocus(this);
				isErrorOccured = true;
				mMediaPlayer.stop();
				mCurrentState = State.STOPPED;
				for (PlayerStateListener listener : mOnPlayerStateChangedListeners) {
					listener.onFinishPlayingTrack(mCurrentTrack);
				}
				//
				// resets the reporting flag for hungama.
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		setPausePos(0);
		// updatewidget();
	}

	public void explicitStop() {
		Logger.i(TAG,
				"################# explicit stopping the service #################");

		// this state is been questioned in the service's handler before
		// playing.
		mIsExplicitMarkedExit = true;
		stopProgressUpdater();
		// stops playing.
		stop();
		// dismisses the notification.
		dismissNotification();

		// bye bye dear service.
		stopSelf();
	}

	/**
	 * Pauses the current track if it was playing
	 */
	public void pause() {
		// checks if it's currently playing.
		if (mCurrentState == State.PLAYING) {
			mCurrentState = State.PAUSED;
			mMediaPlayer.pause();
			setPausePos(mMediaPlayer.getCurrentPosition());
			dismissNotification();
			// dismisses the notification.
		}
	}

	/**
	 * Plays the next track in the queue.
	 */
	public void next() {
		// stops any playing / loading.
		stop();

		if (mPlayingQueue.hasNext()) {
			mCurrentTrack = mPlayingQueue.next();
			play();
		} else {
			mServiceHandler
					.sendEmptyMessage(PlayerService.MESSAGE_FINISH_PLAYING_QUEUE);
		}
	}

	/**
	 * Restarts the player and starts replaying the queue.
	 */
	public void replay() {
		// stops playing.
		stop();
		// resets the queue and Rock & Roll.
		mPlayingQueue = new PlayingQueue(mPlayingQueue.getCopy(), 0);
		play();

		// resets the report list.
	}

	/**
	 * Sets the next track to being played without playing it.
	 * 
	 * @return Track to be played.
	 */
	public AlbumSongDetailPackage fakeNext() {
		// stops any playing / loading.
		stop();

		mCurrentTrack = mPlayingQueue.next();

		return mCurrentTrack;
	}

	/**
	 * Plays the previous track in the queue.
	 */
	public void previous() {
		// stops any playing / loading.
		stop();

		mCurrentTrack = mPlayingQueue.previous();

		if (mCurrentTrack != null) {
			play();
		} else {
			mServiceHandler
					.sendEmptyMessage(PlayerService.MESSAGE_FINISH_PLAYING_QUEUE);
		}
	}

	/**
	 * Sets the previous track to being played without playing it.
	 * 
	 * @return Track to be played.
	 */
	public AlbumSongDetailPackage fakePrevious() {
		// stops any playing / loading.
		stop();

		mCurrentTrack = mPlayingQueue.previous();

		return mCurrentTrack;
	}

	public boolean hasNext() {
		return mPlayingQueue.hasNext();
	}

	public boolean hasPrevious() {
		return mPlayingQueue.hasPrevious();
	}

	public AlbumSongDetailPackage getCurrentPlayingTrack() {

		if (mCurrentTrack != null) {
			mCurrentTrack = mPlayingQueue.getCurrentTrack();
		}

		return mCurrentTrack;
	}

	public int getDuration() {
		if (isErrorOccured)
			return 0;
		if (mCurrentState == State.PLAYING || mCurrentState == State.PAUSED)
			return mMediaPlayer.getDuration();
		else
			return 0;
	}

	public int getCurrentPlayingPosition() {
		if (isErrorOccured)
			return 0;
		if (mCurrentState == State.PLAYING || mCurrentState == State.PAUSED) {
			if (mMediaPlayer.getCurrentPosition() < mMediaPlayer.getDuration())
				return mMediaPlayer.getCurrentPosition();
		}
		return 0;

	}

	public int getPausePos() {
		return pause_pos;
	}

	public void setPausePos(int pausePos) {
		this.pause_pos = pausePos;
	}

	public State getState() {
		return mCurrentState;
	}

	public void seekTo(int timeMilliseconds) {
		mMediaPlayer.seekTo(timeMilliseconds);
	}

	/**
	 * Determines if the player is in the middle of loading of preparing a
	 * {@link Track} before playing.
	 * 
	 * @return true if the player is in the state of {@code State.INTIALIZED} or
	 *         {@code State.PREPARED}, false otherwise.
	 */
	public boolean isLoading() {
		if (mCurrentState == State.INTIALIZED
				|| mCurrentState == State.PREPARED) {
			return true;
		}
		return false;
	}

	/**
	 * Determines if the player is in the middle of playing a {@link Track}. or
	 * the played track is paused it will return true too.
	 * 
	 * @return true if the player is in the state of {@code State.PLAYING} or
	 *         {@code State.PAUSED}, false otherwise.
	 */
	public boolean isPlaying() {
		if (mCurrentState == State.PLAYING || mCurrentState == State.PAUSED
				|| mCurrentState == State.COMPLETED_QUEUE) {
			return true;
		}
		return false;
	}

	public boolean isCurrrentAudioPlaying() {
		if (mCurrentState == State.PLAYING
				|| mCurrentState == State.COMPLETED_QUEUE) {
			return true;
		}
		return false;
	}

	public boolean isQueueCompleted() {
		if (mCurrentState == State.COMPLETED_QUEUE) {
			return true;
		}
		return false;
	}

	public boolean isPlayingForExit() {
		if (mCurrentState == State.PLAYING) {
			return true;
		}
		return false;
	}

	public List<AlbumSongDetailPackage> getPlayingQueue() {
		if (mPlayingQueue != null) {
			return mPlayingQueue.getCopy();
		}
		return null;
	}

	public AlbumSongDetailPackage getNextTrack() {
		if (mPlayingQueue != null) {
			return mPlayingQueue.getNextTrack();
		}
		return null;
	}

	public AlbumSongDetailPackage getPreviousTrack() {
		if (mPlayingQueue != null) {
			return mPlayingQueue.getPreviousTrack();
		}
		return null;
	}

	public int getCurrentQueuePosition() {
		if (mPlayingQueue != null && (isPlaying() || isLoading())) {
			return mPlayingQueue.getCurrentPosition();
		}
		return PlayingQueue.POSITION_NOT_AVAILABLE;
	}

	public AlbumSongDetailPackage removeFrom(int position) {
		if (mPlayingQueue != null) {
			// System.out.println(position + " ::::::: " +
			// mPlayingQueue.getCurrentPosition());
			if (position == mPlayingQueue.getCurrentPosition()) {
				stop();
				AlbumSongDetailPackage lastTrack = mPlayingQueue
						.removeFrom(position);
				if (position <= 0)
					mCurrentState = State.STOPPED;
				return lastTrack;

			} else {
				return mPlayingQueue.removeFrom(position);
			}
		}
		return null;
	}

	public void setLoopMode(LoopMode loopMode) {
		mLoopMode = loopMode;
	}

	public LoopMode getLoopMode() {
		return mLoopMode;
	}

	public void startShuffle() {
		mIsShuffling = true;

		// swap the queue.
		mOriginalPlayingQueue = mPlayingQueue;
		mPlayingQueue = PlayingQueue.createShuffledQueue(mOriginalPlayingQueue);
		if (mCurrentTrack != null)
			mPlayingQueue.updateCurrentPlayingPos(mCurrentTrack);
	}

	public void stopShuffle() {
		try {
			mIsShuffling = false;
			// xtpl
			mOriginalPlayingQueue.setCurrentTrack(mPlayingQueue
					.getCurrentTrack().getMusic_id());
			// xtpl
			// revert to the original queue.
			mPlayingQueue = mOriginalPlayingQueue;

			if (mCurrentTrack != null)
				mPlayingQueue.updateCurrentPlayingPos(mCurrentTrack);

		} catch (Exception e) {
			Logger.e(getClass().getName() + ":1211", e.toString());
		}
	}

	public boolean isShuffling() {
		return mIsShuffling;
	}

	public boolean isAllowSelfTermination() {
		if (mCurrentState == State.INTIALIZED
				|| mCurrentState == State.PREPARED
				|| mCurrentState == State.PLAYING
				|| mCurrentState == State.PAUSED
				|| mCurrentState == State.COMPLETED_QUEUE) {

			return false;
		}

		return true;
	}

	// ======================================================
	// Private helper methods.
	// ======================================================

	private void initializeMediaPlayer() {
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnBufferingUpdateListener(this);
		mMediaPlayer.setOnCompletionListener(this);
		mMediaPlayer.setOnErrorListener(this);
		mMediaPlayer
				.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mp) {
						if (CallInProgress) {
							mCurrentState = State.PAUSED;
							mMediaPlayer.pause();
							dismissNotification();
							return;
						}
					}
				});
		mMediaPlayer.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK);
		mMediaPlayer.reset();
		mCurrentState = State.IDLE;
	}

	private void destroyMediaPlayer() {
		if (mMediaPlayer != null) {
			mMediaPlayer.setOnBufferingUpdateListener(null);
			mMediaPlayer.setOnCompletionListener(null);
			mMediaPlayer.setOnErrorListener(null);
			mMediaPlayer.setOnPreparedListener(null);
			mMediaPlayer.release();
		}
		mCurrentState = State.IDLE;
	}

	private void startLoadingTrack() {
		stopLoadingTrack();
		mMediaLoaderHandler = new MediaLoaderHandler();
		isErrorOccured = false;

		mMediaLoaderWorker = new Thread(new MusicTrackLoaderTask(
				mMediaLoaderHandler, mCurrentTrack));

		mMediaLoaderWorker.start();
	}

	public void stopLoadingTrack() {
		if (mMediaLoaderWorker != null && mMediaLoaderWorker.isAlive()) {
			// mMediaLoaderHandler.removeCallbacksAndMessages(null);
			mMediaLoaderWorker.interrupt();
			mMediaLoaderHandler.removeCallbacksAndMessages(null);

			mMediaLoaderWorker = null;
		}
	}

	// ======================================================
	// Media handle background operation.
	// ======================================================

	private class MediaLoaderHandler extends Handler {

		public static final int MESSAGE_INITIALIZED = 1;
		public static final int MESSAGE_LOADED = 2;
		public static final int MESSAGE_PREPARED = 3;
		public static final int MESSAGE_ERROR = 4;
		public static final int MESSAGE_CANCELLED = 5;
		public static final int MESSAGE_SKIP_CURRENT = 6;

		@Override
		public void handleMessage(Message msg) {
			// resets the data. before obtaining the message.
			Bundle args = msg.getData();
			Message message = Message.obtain(msg);

			if (args != null) {
				message.setData(args);
			}

			switch (msg.what) {

			case MESSAGE_INITIALIZED:
				message.what = PlayerService.MESSAGE_START_LOADING_TRACK;
				if (mServiceHandler != null) {
					mServiceHandler.sendMessage(message);
				}
				break;

			case MESSAGE_LOADED:
				// nothing happened here, the general process of playing a track
				// doesn't care about this.
				break;

			case MESSAGE_PREPARED:
				message.what = PlayerService.MESSAGE_LOADING_TRACK_PREPARED;
				if (mServiceHandler != null) {
					mServiceHandler.sendMessage(message);
				}
				break;

			case MESSAGE_ERROR:
				message.what = PlayerService.MESSAGE_ERROR;
				if (mServiceHandler != null) {
					mServiceHandler.sendMessage(message);
				}
				break;

			case MESSAGE_CANCELLED:
				message.what = PlayerService.MESSAGE_LOADING_TRACK_CANCELLED;
				if (mServiceHandler != null) {
					mServiceHandler.sendMessage(message);
				}
				break;
			case MESSAGE_SKIP_CURRENT:
				message.what = PlayerService.MESSAGE_SKIP_CURRENT_TRACK;
				if (mCurrentTrack != null) {
					AlbumSongDetailPackage trackCopy = mCurrentTrack.newCopy();
					if (trackCopy != null) {
						// Bundle data = new Bundle();
						Intent finishPlay = new Intent();
						finishPlay.setAction(TRACK_FINISHED);
						mContext.sendBroadcast(finishPlay);
						// data.putSerializable(MESSAGE_VALUE,
						// (Serializable) trackCopy);
						// message.setData(data);
						// message.sendToTarget();
					}
				}
				if (mServiceHandler != null) {
					mServiceHandler.sendMessage(message);
				}
				break;
			}

		}

	}

	private abstract class MediaLoaderTask implements Runnable {

		protected final Handler handler;
		protected final AlbumSongDetailPackage track;

		public MediaLoaderTask(Handler handler, AlbumSongDetailPackage track) {
			this.handler = handler;
			this.track = track;
		}

		protected void obtainMessage(int what) {
			if (what == MediaLoaderHandler.MESSAGE_CANCELLED
					&& mCurrentTrack != null && track != null) {
				try {
					Logger.i(
							"",
							mCurrentTrack.getMusic_id()
									+ " Cancelled loading track ..... "
									+ track.getMusic_id());
					if (mCurrentTrack.getMusic_id().equals(track.getMusic_id())) {
						return;
					}
				} catch (Exception e) {
					Logger.e("PlayerService:1362", e.toString());
				}
			}
			try {
				Message message = Message.obtain(handler, what);
				Bundle data = new Bundle();
				data.putSerializable(MESSAGE_VALUE, (Serializable) track);
				message.setData(data);
				message.sendToTarget();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		protected void obtainErrorMessage(PlayerService.Error error) {
			try {
				Message message = Message.obtain(handler,
						MediaLoaderHandler.MESSAGE_ERROR);
				Bundle data = new Bundle();
				data.putSerializable(MESSAGE_VALUE, (Serializable) track);
				data.putInt(MESSAGE_ERROR_VALUE, error.getId());
				message.setData(data);
				message.sendToTarget();
			} catch (Exception e) {
			}
		}

		protected void skipCurrentTrack(int what) {
			Message message = Message.obtain(handler, what);
			Bundle data = new Bundle();
			data.putSerializable(MESSAGE_VALUE, (Serializable) track);
			message.setData(data);
			message.sendToTarget();
		}
	}

	/*
	 * Task that loads the tracks playing properties and prepares it to play
	 * Music.
	 */
	private class MusicTrackLoaderTask extends MediaLoaderTask {

		public MusicTrackLoaderTask(Handler handler,
				AlbumSongDetailPackage track) {
			super(handler, track);
		}

		@Override
		public void run() {
			try {
				// start loading data.
				obtainMessage(MediaLoaderHandler.MESSAGE_INITIALIZED);

				if (Thread.currentThread().isInterrupted()) {
					obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
					return;
				}

				/*
				 * Track's media handle should only been updated if it doesn't
				 * hold one, or it's been obsolete after 30 minutes.
				 */

				if (Thread.currentThread().isInterrupted()) {
					obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
					return;
				}

				// media properties are loaded, prepare.
				obtainMessage(MediaLoaderHandler.MESSAGE_LOADED);

				// if (!mApplicationConfigurations.isUserHasSubscriptionPlan()
				// && adSkipCount % 4 == 0 && mPlayMode == PlayMode.MUSIC
				// && !mApplicationConfigurations.getSaveOfflineMode()) {
				// if (placementAudioAd == null) {
				// CampaignsManager mCampaignsManager = CampaignsManager
				// .getInstance(getBaseContext());
				// placementAudioAd = mCampaignsManager
				// .getPlacementOfType(PlacementType.AUDIO_AD);
				// }
				//
				// if (placementAudioAd != null
				// && placementAudioAd.getMp3Audio() != null
				// && placementAudioAd.getMp3Audio().length() > 0) {
				// try {
				// isAdPlaying = true;
				// mMediaPlayer.reset();
				// mMediaPlayer.setDataSource(placementAudioAd
				// .getMp3Audio());
				// mMediaPlayer
				// .setAudioStreamType(AudioManager.STREAM_MUSIC);
				// mMediaPlayer.prepare();
				// obtainMessage(MediaLoaderHandler.MESSAGE_PREPARED);
				// return;
				// } catch (Exception e) {
				// }
				// }
				// }
				// adSkipCount++;
				if (!TextUtils.isEmpty(track.getMusic_link())) {

					if (Thread.currentThread().isInterrupted()) {
						obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
						return;
					}
					try {
						mMediaPlayer.reset();

						// xtpl
						if (Thread.currentThread().isInterrupted()) {
							obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
							return;
						}

						String path = track.getMusic_link();
						Logger.e("path is service " + path);
						try {

							// String play_url = getFileUrl();
							String fname = "";// track.getSong_name();//new
							// File(path).getName();

							// String ext = new File(track.getMusic_link())
							// .getName();
							// Logger.e("ext " + ext);
							// ext = ext.substring(ext.lastIndexOf('.'));
							fname = track.getSong_name() + ".mp3";

							File folder = new File(
									Common.getBasePath(getBaseContext())
											+ fname);
							Logger.errorLog("path:playSong "
									+ folder.getAbsolutePath());
							if (folder.exists()) {
								path = folder.getAbsolutePath();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						try {
							mMediaPlayer.setDataSource(path);
							mMediaPlayer
									.setAudioStreamType(AudioManager.STREAM_MUSIC);
						} catch (IllegalStateException e) {
							e.printStackTrace();
							if (Thread.currentThread().isInterrupted()) {
								obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
								return;
							}
							mMediaPlayer.setDataSource(path);
							mMediaPlayer
									.setAudioStreamType(AudioManager.STREAM_MUSIC);
						}

						// mMediaPlayer.reset();
						// mMediaPlayer.setDataSource(track.getMusic_link());
						// mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
						if (Thread.currentThread().isInterrupted()) {
							obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
							return;
						}
						// prepare the media to play the track.
						try {
							mMediaPlayer.prepare();
						} catch (InterruptedIOException exception) {
							obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
							return;
						}
						// xtpl
						// if (isCancelled() || Thread.interrupted()) {
						// obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
						// return false;
						// }
						// // xtpl
						obtainMessage(MediaLoaderHandler.MESSAGE_PREPARED);

						if (Thread.currentThread().isInterrupted()) {
							obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
							return;
						}

					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						obtainErrorMessage(PlayerService.Error.SERVER_ERROR);
						return;
					} catch (SecurityException e) {
						e.printStackTrace();
						obtainErrorMessage(PlayerService.Error.SERVER_ERROR);
						return;
					} catch (IllegalStateException e) {
						e.printStackTrace();
						// if (isCancelled() || Thread.interrupted()) {
						// obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
						// return false;
						// }
						obtainMessage(MediaLoaderHandler.MESSAGE_ERROR);
						return;
					} catch (IOException e) {
						e.printStackTrace();
						// if (isCancelled() || Thread.interrupted()) {
						// obtainMessage(MediaLoaderHandler.MESSAGE_CANCELLED);
						// return false;
						// }
						obtainErrorMessage(PlayerService.Error.NO_CONNECTIVITY);
						return;
					}

				} else {
					// no uri for loading data.
					Logger.e(
							TAG,
							"No loading uri for media item: "
									+ track.getMusic_id());
					obtainErrorMessage(PlayerService.Error.DATA_ERROR);
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/*
	 * Task that loads the tracks playing ad-hoc tracks from URL to Web Radio.
	 */

	/*
	 * Prefetches media handles for the prev and the next of the current playing
	 * track.
	 */

	// ======================================================
	// Sleep Receiver.
	// ======================================================

	// private static final class SleepReciever extends BroadcastReceiver {
	//
	// private final WeakReference<PlayerService> playerServiceReference;
	//
	// SleepReciever(PlayerService playerService) {
	// this.playerServiceReference = new WeakReference<PlayerService>(
	// playerService);
	// }
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// if (intent.getAction().equalsIgnoreCase(
	// SleepModeManager.COUNT_DOWN_TIMER_FINISH_INTENT)) {
	// // gets the instance of the player and contolls it.
	// PlayerService playerService = playerServiceReference.get();
	// if (playerService != null) {
	// if (playerService.isPlaying()) {
	// playerService.pause();
	// } else if (playerService.isLoading()) {
	// playerService.mShouldPauseAfterLoading = true;
	// }
	// }
	// }
	// }
	//
	// }

	// ======================================================
	// Notification helper methods.
	// ======================================================

	private static final int NOTIFICATION_PLAYING_CODE = 123456;

	private void updateNotificationForTrack(AlbumSongDetailPackage track) {
		// Notification notification = new Notification(R.drawable.ic_launcher,
		// null, System.currentTimeMillis());
		//
		// Intent startHomeIntent = new Intent(this, HomeActivity.class);
		// // if (mApplicationConfigurations.getSaveOfflineMode()) {
		// // startHomeIntent = new Intent(this, GoOfflineActivity.class);
		// // } else if (getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
		// // || getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
		// // startHomeIntent = new Intent(this, RadioActivity.class);
		// // }
		// startHomeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
		// | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		// startHomeIntent.putExtra("donothing", true);
		// PendingIntent startHomePendingIntent =
		// PendingIntent.getActivity(this,
		// NOTIFICATION_PLAYING_CODE, startHomeIntent, 0);
		//
		// // sets the artist /+ album text.
		// String artistalbum = "";
		// if (track != null) {
		// if (!TextUtils.isEmpty(track.getArtist_name())) {
		// artistalbum = track.getArtist_name();
		// if (!TextUtils.isEmpty(track.getMovie_name())) {
		// artistalbum = artistalbum + " - " + track.getMovie_name();
		// }
		// } else {
		// if (!TextUtils.isEmpty(track.getMovie_name())) {
		// artistalbum = track.getMovie_name();
		// }
		// }
		//
		// notification.setLatestEventInfo(this, track.getSong_name(),
		// artistalbum, startHomePendingIntent);
		// notification.flags |= Notification.FLAG_NO_CLEAR;
		//
		// // startForeground(NOTIFICATION_PLAYING_CODE, notification);
		// } else {
		// Logger.i(TAG, "Track is null - no notification visible");
		// }
		updatewidget();
	}

	private void dismissNotification() {
		// stopForeground(true);
		updatewidget();
	}

	/*
	 * Updater for the progress bar and the current playing time.
	 */
	public static class PlayerProgressCounter extends
			AsyncTask<Void, Void, Void> {
		// TODO: Leak FIX.
		private WeakReference<PlayerService> playerServiceReference = null;

		// private Context context;
		// private boolean isCallIdel;

		PlayerProgressCounter(PlayerService playerService) {
			playerServiceReference = new WeakReference<PlayerService>(
					playerService);
		}

		// public void setContext(Context context){
		// this.context = context;
		// isCallIdel = true;
		// TelephonyManager tm = (TelephonyManager)
		// context.getSystemService(Context.TELEPHONY_SERVICE);
		// tm.listen(new CallListener(), PhoneStateListener.LISTEN_CALL_STATE);
		// }

		@Override
		protected Void doInBackground(Void... params) {
			while (!isCancelled()) {
				try {
					publishProgress();
					if (isCancelled()) {
						break;
					}
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Logger.e(TAG, "Cancelling playing progress update.");
					break;
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// Logger.s("---------------onProgressUpdate--------------");
			final PlayerService playerService = playerServiceReference.get();
			if (playerService != null) {
				State state = playerService.getState();
				// System.out.println("Call State ::::::: " +
				// tm.getCallState());
				// if(!isCallIdel && state == State.PLAYING)
				// playerService.mCurrentState = State.PAUSED;
				// System.out.println("isCallIdel ::::::: " + isCallIdel);
				if (state == State.PLAYING
						&& playerService.mMediaPlayer.isPlaying()) {
					// gets the values.
					final int progress = (int) (((float) playerService
							.getCurrentPlayingPosition() / playerService
							.getDuration()) * 100);
					final String label = Utilities
							.secondsToString(playerService
									.getCurrentPlayingPosition() / 1000)
							+ " / ";
					// updates the views.
					/*
					 * Seems on some devices it might crash for some reason,
					 * seems the device default's AsyncTask implementations are
					 * broken for some OEMs.
					 */
					if (playerService.mOnPlayerBarUpdateListener != null) {
						playerService.mOnPlayerBarUpdateListener
								.OnPlayerBarUpdate(progress, label);
					}
					for (PlayerBarUpdateListener listener : playerService.mOnPlayerUpdateListeners) {
						listener.OnPlayerBarUpdate(progress, label);
					}
				}
			} else {
				cancel(true);
			}
		}
	}

	// @SuppressLint("NewApi")
	public void startProgressUpdater() {
		mPlayerProgressCounter = new PlayerProgressCounter(this);
		// mPlayerProgressCounter.setContext(mContext);
		// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		// mPlayerProgressCounter
		// .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		// } else {
		mPlayerProgressCounter.execute();
		Logger.i(TAG, "EXECUTED - Build VERSION LESS THAN HONEYCOMB");
		// }
	}

	public void stopProgressUpdater() {
		if (mPlayerProgressCounter != null)
			Logger.e("----stopProgressUpdater --- "
					+ mPlayerProgressCounter.getStatus().toString());
		if (mPlayerProgressCounter != null
				&& (mPlayerProgressCounter.getStatus() == AsyncTask.Status.PENDING || mPlayerProgressCounter
						.getStatus() == AsyncTask.Status.RUNNING)) {

			mPlayerProgressCounter.cancel(true);
			mPlayerProgressCounter = null;
		}
	}

	private Set<PlayerBarUpdateListener> mOnPlayerUpdateListeners = new HashSet<PlayerService.PlayerBarUpdateListener>();

	public void registerPlayerUpdateListeners(PlayerBarUpdateListener listner) {
		mOnPlayerUpdateListeners.add(listner);
	}

	public void unregisterPlayerUpdateListeners(PlayerBarUpdateListener listner) {
		mOnPlayerUpdateListeners.remove(listner);
	}

	public interface PlayerBarUpdateListener {

		public void OnPlayerBarUpdate(int progress, String label);
	}

	public void setPlayerBarUpdateListener(PlayerBarUpdateListener listener) {
		mOnPlayerBarUpdateListener = listener;
	}

	public void updateTrack(AlbumSongDetailPackage track) {
		mPlayingQueue.updateTrack(track);
	}

	public int getAudioSessionId() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			try {
				return mMediaPlayer.getAudioSessionId();
			} catch (Exception e) {
				Logger.e(TAG + ":2044", e.toString());
				return 0;
			}
		} else {
			return 0;
		}
	}

	// public void setAudioAd(Placement audioAd) {
	// placementAudioAd = audioAd;
	// }

	public boolean isCurrentPlayingTrack(AlbumSongDetailPackage track) {
		try {
			if (mCurrentTrack != null) {
				return (track == mCurrentTrack);
			}
		} catch (Exception e) {
		}
		return false;
	}

	public int getCurrentPlayingTrackPosition() {
		return mPlayingQueue.getCurrentPosition();
	}

	public AlbumSongDetailPackage trackDragAndDrop(int from, int to) {
		if (mPlayingQueue != null) {
			mPlayingQueue.trackDragAndDrop(from, to);
		}
		return null;
	}

	public void updateNotificationForOffflineMode() {
		if (mCurrentTrack != null && mCurrentState == State.PLAYING) {
			updateNotificationForTrack(mCurrentTrack);
		}
	}

	boolean PausebyCall = false;
	boolean CallInProgress = false;

	BroadcastReceiver callReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService(TELEPHONY_SERVICE);

			if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
				CallInProgress = false;
				if (PausebyCall)
					play();
				PausebyCall = false;
			} else {
				try {
					if (mMediaPlayer.isPlaying()) {
						pause();
						PausebyCall = true;
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
				CallInProgress = true;
			}

		}
	};

	public void cancelNotification() {
		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		manager.cancel(NOTIFICATION_PLAYING_CODE);
	}

	public void clearPlayingQueue() {
		stopPreviousQueue();
		mPlayingQueue.clearQueue();

	}

	public void reset() {
		if (mMediaPlayer != null) {
			mMediaPlayer.reset();
		}
	}

	public void stopMusicFromVideo() {
		if (PlayerService.service != null) {
			if (PlayerService.service.isPlaying()
					|| PlayerService.service.isCurrrentAudioPlaying()) {
				PlayerService.service.pause();
			}
		}
	}
}