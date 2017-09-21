package com.rjbalaji;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import h.thunderbird.phoenix.rjbalaji.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class FragmentWebview extends Fragment {

	public FragmentWebview() {
	}

	private WebView moCustomWebView;
	private ProgressBar moProgressbar;
	private String msWebsiteUrl;

	View rootView;
	// CatalogueService catalogueService;
	String title;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater
				.inflate(R.layout.fragment_webview, container, false);

		title = getArguments().getString("title");

		((BaseActivity) getActivity()).updateActionBar(true, title, true, true);

		msWebsiteUrl = getArguments().getString("url");
		moCustomWebView = (WebView) rootView.findViewById(R.id.wvCustom);
		moProgressbar = (ProgressBar) rootView.findViewById(R.id.progressbar);
		moCustomWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		loadUrl();

		AdView adView = (AdView) rootView.findViewById(R.id.ad);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);
		return rootView;
	}

	private void loadUrl() {
		moCustomWebView.getSettings().setJavaScriptEnabled(true);
		moProgressbar.setProgress(0);
		moCustomWebView.getSettings().setDomStorageEnabled(true);
		moCustomWebView.getSettings().setDatabaseEnabled(true);
		moCustomWebView.setWebViewClient(new WebViewClient());
		moCustomWebView.setBackgroundColor(0);
		moCustomWebView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				if (progress == 100) {
					moProgressbar.setVisibility(View.GONE);
				} else {
					moProgressbar.setVisibility(View.VISIBLE);
					moProgressbar.setProgress(progress);
				}
			}
		});
		moCustomWebView.loadUrl(msWebsiteUrl);
	}

}