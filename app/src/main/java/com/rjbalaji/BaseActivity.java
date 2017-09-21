package com.rjbalaji;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rjbalaji.interfaces.Constants;

import h.thunderbird.phoenix.rjbalaji.R;

public class BaseActivity extends ActionBarActivity {

	protected DrawerLayout mDrawerLayout;
	protected ListView listView;
	LinearLayout drawerItems;
	private ActionBarDrawerToggle mDrawerToggle;
	protected boolean isDrawerMenu;
	protected boolean isActivity;
	protected String currentScreen;
	protected TextView tvTitle;
	ImageView iv_back;
	Handler handler;

	// ---------Need To change - START-------//
	// int[] DRAWABLES = { R.drawable.new_icon, R.drawable.new_icon,
	// R.drawable.new_icon, R.drawable.new_icon, R.drawable.new_icon };

	public static String URL_FACEBOOK = "https://www.facebook.com/pages/RJ-Balaji/131463890269083";
	public static String URL_TWITTER = "http://mobile.twitter.com/RJ_Balaji";
	public static String URL_PRIVPOL = "http://rjbalajiapp.blogspot.in/2017/03/rj-balaji-app-privacy-policy.html";
	public static String SERVER_TAG_NAME_TAKE_IT_EASY = "TIE";
	public static String SERVER_TAG_NAME_CROSS = "CTK";
	public static String SERVER_TAG_NAME_VIDEO = "VID";
	public static String SERVER_TAG_NAME_APP = "APP";
	public static String SERVER_TAG_NAME_SHARE = "SHARE";
	public static String SERVER_TAG_NAME_FACEBOOK = "facebook";
	public static String SERVER_TAG_NAME_TWITTER = "twitter";
	public static String SERVER_TAG_NAME_PRIVPOL = "privpol";

	public static String[] TITLES = { Constants.TITLE_TAKE_IT_EASY,
			Constants.TITLE_CROSS, Constants.TITLE_VIDEO,
			Constants.TITLE_FACEBOOK, Constants.TITLE_TWITTER,
			Constants.TITLE_SHARE_APP, Constants.TITLE_MORE_APPS, Constants.TITLE_PRIV_POL };
	public static String[] TAGS = { SERVER_TAG_NAME_TAKE_IT_EASY,
			SERVER_TAG_NAME_CROSS, SERVER_TAG_NAME_VIDEO,
			SERVER_TAG_NAME_FACEBOOK, SERVER_TAG_NAME_TWITTER,
			SERVER_TAG_NAME_SHARE, SERVER_TAG_NAME_APP, SERVER_TAG_NAME_PRIVPOL };

	// ---------Need To change - END-------//

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getWindow().requestFeature(Window.FEATURE);
		// supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
	}

	protected void setCurrentScreen(String currentScreen) {
		this.currentScreen = currentScreen;
	}

	protected void setIds(boolean isDrawerMenu, boolean isActivity,
			final String title, Handler handler) {
		this.isDrawerMenu = isDrawerMenu;
		this.isActivity = isActivity;
		this.handler = handler;
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerItems = (LinearLayout) findViewById(R.id.drawerItems);
		listView = (ListView) findViewById(R.id.left_drawer);

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);

		DrawerListAdapter drawerListAdapter = new DrawerListAdapter(
				BaseActivity.this);
		listView.setAdapter(drawerListAdapter);

		listView.setOnItemClickListener(new DrawerItemClickListener());
		listView.setCacheColorHint(0);
		listView.setScrollingCacheEnabled(false);
		listView.setScrollContainer(false);
		listView.setFastScrollEnabled(true);
		listView.setSmoothScrollbarEnabled(true);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setIcon(android.R.color.transparent);

		getSupportActionBar().setDisplayUseLogoEnabled(false);
		Drawable drawable = new ColorDrawable(getResources().getColor(
				R.color.dark_orange));
		drawable = new ColorDrawable(Color.argb(100, 7, 56, 83));
		getSupportActionBar().setBackgroundDrawable(
				getResources().getDrawable(R.drawable.header1));
		getSupportActionBar().setTitle(title);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, // nav menu toggle icon
				R.string.app_name, // nav drawer open - description for
									// accessibility
				R.string.app_name) {
			public void onDrawerClosed(View view) {
				// getSupportActionBar().setTitle(title);
				supportInvalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				// getSupportActionBar().setTitle("Menu");
				// calling onPrepareOptionsMenu() to hide action bar icons
				supportInvalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		ActionBar actionBar = getSupportActionBar();
		// add the custom view to the action bar
		actionBar.setCustomView(R.layout.actionbar_view);
		tvTitle = (TextView) actionBar.getCustomView().findViewById(
				R.id.tvTitle);
		iv_back = (ImageView) actionBar.getCustomView().findViewById(
				R.id.iv_back);
		iv_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackBtnClick(v);
			}
		});
		if (isDrawerMenu) {
			if (iv_back != null)
				iv_back.setVisibility(View.GONE);
			mDrawerToggle.syncState();
		} else {
			if (iv_back != null)
				iv_back.setVisibility(View.VISIBLE);
			// mDrawerToggle.setDrawerIndicatorEnabled(false);
		}
		// Typeface tf = Typeface
		// .createFromAsset(this.getAssets(), "alger.TTF" /* "neuropol.ttf" */);
		// tvTitle.setTypeface(tf);

		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
				| ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP);

	}

	protected void refreshAds(Context context) {
		// Intent refreshIntent = new Intent();
		// refreshIntent.setAction(MainScreen.ACTION_REFRESH_ADS);
		// context.sendBroadcast(refreshIntent);
	}

	protected void updateActionBar(boolean isDrawerMenu, String title,
			boolean shareEnable, boolean moreEnable) {
		this.isDrawerMenu = isDrawerMenu;
		// Typeface tf = Typeface
		// .createFromAsset(this.getAssets(), "alger.TTF" /* "neuropol.ttf" */);
		// tvTitle.setTypeface(tf);
		tvTitle.setText(title);
		if (isDrawerMenu) {
			if (iv_back != null)
				iv_back.setVisibility(View.GONE);
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			mDrawerToggle.syncState();
			mDrawerToggle.setDrawerIndicatorEnabled(true);
			getSupportActionBar().setHomeButtonEnabled(true); 
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
		} else {
			if (iv_back != null) 
				iv_back.setVisibility(View.VISIBLE);
			mDrawerLayout
					.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			mDrawerToggle.setDrawerIndicatorEnabled(false);
			getSupportActionBar().setHomeButtonEnabled(false); // disable the
																// button
			getSupportActionBar().setDisplayHomeAsUpEnabled(false); // remove
																	// the left
																	// caret
			getSupportActionBar().setDisplayShowHomeEnabled(false); // remove
																	// the icon
		}
		tvTitle.setVisibility(View.VISIBLE);
	}

	class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Toast.makeText(BaseActivity.this, "Coming soon..",
					Toast.LENGTH_SHORT).show();
			if (mDrawerLayout != null)
				mDrawerLayout.closeDrawer(drawerItems);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (!isDrawerMenu) {
			if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
				getSupportFragmentManager().popBackStack();
			}
			return true;
		}
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		if (isDrawerMenu)
			mDrawerToggle.syncState();

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	private class DrawerListAdapter extends BaseAdapter {

		Context context;
		ViewHolder viewHolder;

		public DrawerListAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return TITLES.length;// fbFrndList.size();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		synchronized public View getView(final int position, View convertView,
				ViewGroup parent) {
			viewHolder = new ViewHolder();
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.list_item_drawer, null);

				viewHolder.tvItemName = (TextView) convertView
						.findViewById(R.id.tvItemName);

				viewHolder.rlItem = (RelativeLayout) convertView
						.findViewById(R.id.rlItem);

				convertView.setTag(viewHolder);
			} else
				viewHolder = (ViewHolder) convertView.getTag();

			viewHolder.tvItemName.setText(TITLES[position]);
			viewHolder.rlItem.setTag(position);
			viewHolder.rlItem.setOnClickListener(onClick);
			return convertView;
		}

		class ViewHolder {
			TextView tvItemName;
			RelativeLayout rlItem;

		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		OnClickListener onClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mDrawerLayout != null)
					mDrawerLayout.closeDrawer(drawerItems);
				// if ((Integer) v.getTag() == 3) {
				// Intent browserIntent = new Intent(
				// "android.intent.action.VIEW",
				// Uri.parse(URL_FACEBOOK));
				// startActivity(browserIntent);
				// } else if ((Integer) v.getTag() == 4) {
				// Intent browserIntent = new Intent(
				// "android.intent.action.VIEW",
				// Uri.parse(URL_TWITTER));
				// startActivity(browserIntent);
				// } else
				if ((Integer) v.getTag() == 5) {
					String appPackageName = getPackageName();
					generatorShare(getString(R.string.app_name)
							+ " - http://play.google.com/store/apps/details?id="
							+ appPackageName);
				} else {
					Message msg = Message.obtain(handler, (Integer) v.getTag(),
							null);
					handler.dispatchMessage(msg);
				}
			}
		};
	}

	public void onBackBtnClick(View v) {
		if (getSupportFragmentManager().getBackStackEntryCount() > 0)
			getSupportFragmentManager().popBackStack();
	}

	protected void generatorShare(String shareText) {
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent
				.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
		shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
		startActivity(Intent.createChooser(shareIntent, "Share with"));
	}
}