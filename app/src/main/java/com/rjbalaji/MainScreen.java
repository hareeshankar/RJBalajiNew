package com.rjbalaji;

import h.thunderbird.phoenix.rjbalaji.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.app.UserEmailFetcher;
import com.app.Util;
import com.google.android.gcm.GCMRegistrar;
import com.rjbalaji.interfaces.Constants;
import com.rjbalaji.player.PlayerService;
import com.rjbalaji.util.Settings;
import com.shephertz.app42.paas.sdk.android.App42API;

public class MainScreen extends BaseActivity {

	private FragmentManager mFragmentManager;
	public static boolean isMainScreen = false;

	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_main);
		startService(new Intent(getBaseContext(), PlayerService.class));
		if (savedInstanceState == null) {
			currentScreen = Constants.TITLE_TAKE_IT_EASY;
			String serverTagName = SERVER_TAG_NAME_TAKE_IT_EASY;
			Bundle bundle1 = getIntent().getExtras();
			if (bundle1 != null) {
				currentScreen = bundle1.getString("screenName");
				serverTagName = bundle1.getString("serverTagName");
			}
			super.setIds(true, true, getString(R.string.app_name), handler);
			if (tvTitle != null)
				tvTitle.setText(currentScreen);
			Settings setting = Settings.getInstance(MainScreen.this);
			setting.setType(0);
			Fragment fr;
			Bundle bundle = new Bundle();
			bundle.putString("tag", serverTagName);
			bundle.putString("title", currentScreen);
			mFragmentManager = this.getSupportFragmentManager();
			fr = new FragmentMain();
			fr.setArguments(bundle);
			FragmentTransaction fragmentTransaction = mFragmentManager
					.beginTransaction();
			fragmentTransaction.add(R.id.fragmant_container, fr);
			fragmentTransaction.commitAllowingStateLoss();
		} else {
			super.setIds(true, true, getString(R.string.app_name), handler);
			Settings setting = Settings.getInstance(MainScreen.this);
			if (tvTitle != null)
				tvTitle.setText(TITLES[setting.getType()]);
			currentScreen = TITLES[setting.getType()];
		}
		registerGCM();
	}

	private void registerGCM() {
		new Thread() {
			public void run() {
				String GCMRegId = GCMRegistrar
						.getRegistrationId(MainScreen.this);
				if (GCMRegId == null || GCMRegId.equals("")) {
					String emailId = "";
					try {
						emailId = UserEmailFetcher.getEmail(MainScreen.this);
					} catch (SecurityException e) {
						e.printStackTrace();
					}
					if (emailId != null && !emailId.equals("")) {
						App42API.initialize(MainScreen.this,
								Constants.SHEPHERTZ_API_KEY,
								Constants.SHEPHERTZ_SECRET_KEY);
						App42API.setLoggedInUser(emailId);
						Util.registerWithApp42(Constants.Project_Number);
					}
				}
			};
		}.start();
	}

	private final Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			int type = msg.what;
			Settings setting = Settings.getInstance(MainScreen.this);
			Fragment newFragment = null;
			setting.setType(type);
			if (type == 0 || type == 1 || type == 2 || type == 6) {
				// TIE,CrossTalk,Video,MoreApps
				newFragment = new FragmentMain();
				currentScreen = TITLES[type];
				if (newFragment != null) {
					Bundle bundle = new Bundle();
					bundle.putString("tag", TAGS[type]);
					bundle.putString("title", currentScreen);
					newFragment.setArguments(bundle);
					FragmentTransaction transaction = getSupportFragmentManager()
							.beginTransaction();
					transaction.replace(R.id.fragmant_container, newFragment);
					transaction.commit();
					if (tvTitle != null)
						tvTitle.setText(currentScreen);
				}
			} else if (type == 3 || type == 4 || type == 7) {// Facebook and Twitter
				if (!currentScreen.equals(TITLES[type])) {
					FragmentWebview fragment = new FragmentWebview();
					currentScreen = TITLES[type];
					Bundle bundle = new Bundle();
					bundle.putString("title", currentScreen);
					if (type == 3)
						bundle.putString("url", BaseActivity.URL_FACEBOOK);
					if (type == 4 )
						bundle.putString("url", BaseActivity.URL_TWITTER);
					if (type == 7)
						bundle.putString("url",BaseActivity.URL_PRIVPOL);
					fragment.setArguments(bundle);
					FragmentTransaction transaction = getSupportFragmentManager()
							.beginTransaction();
					transaction.replace(R.id.fragmant_container, fragment);
					transaction.commit();
					if (tvTitle != null)
						tvTitle.setText(currentScreen);
				}
			}
		}
	};
	private boolean isIntroDisplayed;

	public void onBackPressed() {
		if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					MainScreen.this);

			// set title
			alertDialogBuilder.setTitle(getString(R.string.app_name));

			// set dialog message
			alertDialogBuilder
					.setMessage("Do you really want to Exit?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									if (PlayerService.service != null) {
										PlayerService.service
												.clearPlayingQueue();
										PlayerService.service.stop();
									}
									finish();
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		} else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
			getSupportFragmentManager().popBackStack();
		} else if (mDrawerLayout != null && mDrawerLayout.isShown()) {
			mDrawerLayout.closeDrawer(drawerItems);
		}
	};
}