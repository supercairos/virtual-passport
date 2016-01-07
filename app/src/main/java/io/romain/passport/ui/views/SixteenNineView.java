package io.romain.passport.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class SixteenNineView extends FrameLayout {

	public SixteenNineView(Context context) {
		super(context);
	}

	public SixteenNineView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SixteenNineView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//   let the default measuring occur, then force the desired aspect ratio
		//   on the view (not the drawable).
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = getMeasuredWidth();
		//force a 16:9 aspect ratio
		int height = Math.round(width * (9f / 16f));
		setMeasuredDimension(width, height);
	}
}
