package com.rjbalaji;

import h.thunderbird.phoenix.rjbalaji.R;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.rjbalaji.Common.Common;
import com.rjbalaji.db.dbHelper;
import com.rjbalaji.player.PlayerService;
import com.rjbalaji.player.PlayerService.Error;
import com.rjbalaji.player.PlayerService.PlayerStateListener;
import com.rjbalaji.rjbalajipackage.AlbumSongDetailPackage;
import com.rjbalaji.util.Logger;
import com.rjbalaji.util.Settings;

public class FragmentMain extends Fragment implements PlayerStateListener {

	private ListView lv;
	// public static String MUSIC1 = "Music1";
	String MUSIC_TAG;
	ArrayList<AlbumSongDetailPackage> moAudioList;
	AudioAdapter audioAdapter1;
	String currentPlayingPos = "";

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public FragmentMain() {
	}

	View rootView;

	// CatalogueService catalogueService;
	String title;
	dbHelper db;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		db = new dbHelper(getActivity());
		moAudioList = new ArrayList<AlbumSongDetailPackage>();

		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			lv = (ListView) rootView.findViewById(R.id.lvAudioes);
			// new GetGeneratorList(getActivity(), currentIndex,
			// true).execute();
			AdView adView = (AdView) rootView.findViewById(R.id.ad);
			AdRequest adRequest = new AdRequest.Builder().build();
			adView.loadAd(adRequest);
		} else {
			// ((ViewGroup) get).removeAllViews();
			// // container.addView(rootView);
			final ViewParent parent = rootView.getParent();
			if (parent != null && parent instanceof ViewGroup)
				((ViewGroup) parent).removeView(rootView);
		}

		MUSIC_TAG = getArguments().getString("tag");
		title = getArguments().getString("title");
		moAudioList = db.getSelectedAlbumData(MUSIC_TAG);
		audioAdapter1 = new AudioAdapter();
		lv.setAdapter(audioAdapter1);

		((BaseActivity) getActivity()).updateActionBar(true, title, true, true);
		getCurrentPlayingPos();
		getCategoryData();
		registerReceiver();
		handler.postDelayed(run, 1000);
		return rootView;
	}

	private void registerReceiver() {
		IntentFilter refreshDbIntent = new IntentFilter();
		refreshDbIntent.addAction("refreshDB");
		getActivity().registerReceiver(refreshDbReceiver, refreshDbIntent);
	}

	ProgressDialog mProgress;
	BroadcastReceiver refreshDbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// boolean isStarted =
					// intent.getExtras().getBoolean("isStarted");
					// if (isStarted) {
					// mProgress = new ProgressDialog(getActivity());
					// mProgress.setMessage("Please wait...");
					// mProgress.setCancelable(false);
					// mProgress.show();
					// } else {
					// if (mProgress != null && mProgress.isShowing()) {
					// mProgress.dismiss();
					// }
					moAudioList = db.getSelectedAlbumData(MUSIC_TAG);
					audioAdapter1.notifyDataSetChanged();
					// }

				}
			});

		}
	};
	Handler handler = new Handler();
	Runnable run = new Runnable() {
		@Override
		public void run() {
			PlayerService.service
					.registerPlayerStateListener(FragmentMain.this);
		}
	};

	private int getCurrentPlayingPos() {
		Settings setting = Settings.getInstance(getActivity());
		String albumName = setting.getAlbumName();
		int newPosition = -1;
		// if (Common.isInternetAvailable(getActivity())) {
		if (albumName != null && !albumName.equals("")
				&& albumName.equals(title)) {
			if (PlayerService.service != null) {
				AlbumSongDetailPackage details = PlayerService.service
						.getCurrentPlayingTrack();
				if (details != null) {
					for (int i = 0; i < moAudioList.size(); i++)
						if (moAudioList.get(i).getMusic_id()
								.equals(details.getMusic_id())) {
							newPosition = i;
							currentPlayingPos = i + "";
							audioAdapter1.notifyDataSetChanged();
							break;
						}
				}
			} else {
				currentPlayingPos = null;
				audioAdapter1.notifyDataSetChanged();
			}
		}
		// }
		return newPosition;
	}

	private void getCategoryData() {
	}

	public class AudioAdapter extends BaseAdapter {

		private LayoutInflater inflater;

		public AudioAdapter() {
			inflater = LayoutInflater.from(getActivity());
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {

			TextView loTvName = null;
			Button loBtnPlay;
			RelativeLayout rlItem;
			// ProgressBar progressBar1;
			LinearLayout llProgressBar1;
			ImageView ivOnlineOffline;
			AlbumSongDetailPackage details = moAudioList.get(position);
			if (convertView == null) {
				// LayoutInflater inflater = (LayoutInflater) getActivity()
				// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.item_audio, null);
				loTvName = (TextView) convertView.findViewById(R.id.tvAudio);
				loBtnPlay = (Button) convertView.findViewById(R.id.btnPlay);
				rlItem = (RelativeLayout) convertView.findViewById(R.id.rlItem);
				// progressBar1 = (ProgressBar) convertView
				// .findViewById(R.id.progressBar1);
				llProgressBar1 = (LinearLayout) convertView
						.findViewById(R.id.llProgressBar);
				ivOnlineOffline = (ImageView) convertView
						.findViewById(R.id.ivOnlineOffline);
				convertView.setTag(new ViewHolder(loTvName, loBtnPlay, rlItem,
						llProgressBar1, ivOnlineOffline));
			} else {
				ViewHolder viewHolder = (ViewHolder) convertView.getTag();
				loTvName = viewHolder.getTextView();
				loBtnPlay = viewHolder.getBtnPlay();
				rlItem = viewHolder.getRlItem();
				llProgressBar1 = viewHolder.getLlProgressBar();
				ivOnlineOffline = viewHolder.getOnlineOfflineImg();
				// progressBar1 = viewHolder.getProgressBar();
			}
			if (isStartLoading) {
				if (currentPlayingPos != null
						&& currentPlayingPos.equals(String.valueOf(position))) {
					llProgressBar1.setVisibility(View.VISIBLE);
					loBtnPlay.setVisibility(View.GONE);
				} else {
					llProgressBar1.setVisibility(View.GONE);
					loBtnPlay.setBackgroundResource(R.drawable.list_play);
				}
			} else {
				if (currentPlayingPos != null
						&& currentPlayingPos.equals(String.valueOf(position))) {
					loBtnPlay.setBackgroundResource(R.drawable.list_pause);
				} else {
					loBtnPlay.setBackgroundResource(R.drawable.list_play);
				}
				loBtnPlay.setVisibility(View.VISIBLE);
				llProgressBar1.setVisibility(View.GONE);
			}

			if (MUSIC_TAG.equals(BaseActivity.SERVER_TAG_NAME_VIDEO)) {
				ivOnlineOffline.setVisibility(View.GONE);
			} else if (MUSIC_TAG.equals(BaseActivity.SERVER_TAG_NAME_APP)) {
				ivOnlineOffline.setVisibility(View.GONE);
				loBtnPlay.setVisibility(View.GONE);
			} else {
				String fileName = details.getSong_name() + ".mp3";

				File filePath = new File(Common.getBasePath(getActivity())
						+ fileName);
				if (filePath.exists()) {
					ivOnlineOffline.setImageResource(R.drawable.offline);
				} else {
					ivOnlineOffline.setImageResource(R.drawable.online);
				}
				ivOnlineOffline.setVisibility(View.VISIBLE);
			}
			loTvName.setText(details.getSong_name());
			rlItem.setTag(position);
			rlItem.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Settings setting = Settings.getInstance(getActivity());
					String albumName = setting.getAlbumName();
					if (albumName != null && !albumName.equals("")
							&& !albumName.equals(title)) {
						PlayerService.service.clearPlayingQueue();
						PlayerService.service.stop();
					}
					if (MUSIC_TAG.equals(BaseActivity.SERVER_TAG_NAME_VIDEO)) {
						String videoId = getVideoId(moAudioList.get(
								(Integer) v.getTag()).getMusic_link());
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri
								.parse("vnd.youtube:" + videoId));
						intent.putExtra("VIDEO_ID", videoId);
						getActivity().startActivity(intent);
					} else if (MUSIC_TAG
							.equals(BaseActivity.SERVER_TAG_NAME_APP)) {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri
								.parse(moAudioList.get((Integer) v.getTag())
										.getMusic_link()));
						getActivity().startActivity(intent);
					} else {

						if (PlayerService.service != null
								&& PlayerService.service
										.isCurrrentAudioPlaying()
								&& !currentPlayingPos.equals(((Integer) v
										.getTag()) + "")) {
							PlayerService.service.stop();
						}
						currentPlayingPos = null;
						Bundle bundle = new Bundle();
						bundle.putInt("selectedPos", (Integer) v.getTag());
						bundle.putString("tag", MUSIC_TAG);
						bundle.putString("albumName", title);
						FragmentPlayer fragment = new FragmentPlayer();
						FragmentTransaction ft = getActivity()
								.getSupportFragmentManager().beginTransaction();
						fragment.setArguments(bundle);
						ft.setCustomAnimations(R.anim.slide_right_in,
								R.anim.slide_right_out, R.anim.slide_left_in,
								R.anim.slide_left_out);
						ft.addToBackStack(null);
						ft.replace(R.id.fragmant_container, fragment,
								fragment.getClass().getSimpleName()).commit();
					}
				}
			});
			return convertView;
		}

		public Object getItem(int position) {
			return null;
		}

		public int getCount() {
			return moAudioList.size();
		}

		public long getItemId(int position) {
			return position;
		}
	}

	private static class ViewHolder {
		private TextView textView;
		private Button btnPlay;
		private RelativeLayout rlItem;
		private LinearLayout llProgressBar1;
		private ImageView ivOnlineOffline;

		public ViewHolder(TextView textView, Button foBtnPlay,
				RelativeLayout rlItem, LinearLayout llProgressBar1,
				ImageView ivOnlineOffline) {
			this.textView = textView;
			btnPlay = foBtnPlay;
			this.rlItem = rlItem;
			this.llProgressBar1 = llProgressBar1;
			this.ivOnlineOffline = ivOnlineOffline;
		}

		public ImageView getOnlineOfflineImg() {
			return ivOnlineOffline;
		}

		public LinearLayout getLlProgressBar() {
			return llProgressBar1;
		}

		// public ProgressBar getProgressBar() {
		// return progressBar1;
		// }

		public TextView getTextView() {
			return textView;
		}

		public Button getBtnPlay() {
			return btnPlay;
		}

		public RelativeLayout getRlItem() {
			return rlItem;
		}
	}

	boolean isStartLoading = false;

	@Override
	public void onStartLoadingTrack(AlbumSongDetailPackage track) {
		isStartLoading = true;
		int pos = getCurrentPlayingPos();
		if (pos == -1)
			updateListPlayPos(null);
		else
			updateListPlayPos(pos + "");
		Logger.iLog("Music CallBack", "Music CallBack: onStartLoadingTrack");
	}

	@Override
	public void onTrackLoadingBufferUpdated(AlbumSongDetailPackage track,
			int percent) {

	}

	@Override
	public void onStartPlayingTrack(AlbumSongDetailPackage track) {
		isStartLoading = false;
		// if (PlayerService.service.currentAlbumId != null
		// && jsonDetails.getApp_id().equals(
		// PlayerService.service.currentAlbumId)) {
		Logger.iLog("Music CallBack", "Music CallBack: onStartPlayingTrack");

		int pos = getCurrentPlayingPos();
		if (pos == -1)
			updateListPlayPos(null);
		else
			updateListPlayPos(pos + "");
	}

	@Override
	public void onFinishPlayingTrack(AlbumSongDetailPackage track) {
		// if (PlayerService.service.currentAlbumId != null
		// && jsonDetails.getApp_id().equals(
		// PlayerService.service.currentAlbumId)) {
		Logger.iLog("Music CallBack", "Music CallBack: onFinishPlayingTrack");
		updateListPlayPos(null);
		if (PlayerService.service != null)
			PlayerService.service.cancelNotification();
		isStartLoading = false;
		int pos = getCurrentPlayingPos();
		if (pos == -1)
			updateListPlayPos(null);
		else
			updateListPlayPos(pos + "");

	}

	@Override
	public void onFinishPlayingQueue() {
		// if (PlayerService.service.currentAlbumId != null
		// && jsonDetails.getApp_id().equals(
		// PlayerService.service.currentAlbumId)) {
		Logger.iLog("Music CallBack", "Music CallBack: onFinishPlayingQueue");
		updateListPlayPos(null);
		// }
	}

	@Override
	public void onSleepModePauseTrack(AlbumSongDetailPackage track) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(refreshDbReceiver);
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
		updateListPlayPos(null);
		// }
	}

	private void updateListPlayPos(String pos) {
		currentPlayingPos = pos;
		audioAdapter1.notifyDataSetChanged();
	}

	private String getVideoId(String url) {
		String pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";

		Pattern compiledPattern = Pattern.compile(pattern);
		Matcher matcher = compiledPattern.matcher(url);
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}
}