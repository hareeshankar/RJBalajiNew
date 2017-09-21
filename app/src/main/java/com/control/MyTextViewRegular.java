package com.control;

import com.rjbalaji.interfaces.Constants;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class MyTextViewRegular extends TextView {
	private static Typeface typeface;

	public MyTextViewRegular(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (typeface == null)
			typeface = Typeface.createFromAsset(context.getAssets(),
					Constants.FONT_NAME);
		setTypeface(typeface);
		// setTextSize(CommonFunctions.dpToPx((int) context.getResources()
		// .getDimension(R.dimen.txtSizeNew), context));
	}

	public MyTextViewRegular(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (typeface == null)
			typeface = Typeface.createFromAsset(context.getAssets(),
					Constants.FONT_NAME);
		setTypeface(typeface);
		// setTextSize(CommonFunctions.dpToPx((int) context.getResources()
		// .getDimension(R.dimen.txtSizeNew), context));
	}

	public MyTextViewRegular(Context context) {
		super(context);
		if (typeface == null)
			typeface = Typeface.createFromAsset(context.getAssets(),
					Constants.FONT_NAME);
		setTypeface(typeface);
		// setTextSize(CommonFunctions.dpToPx((int) context.getResources()
		// .getDimension(R.dimen.txtSizeNew), context));
	}

}
