package io.romain.passport.ui.views;

import android.content.Context;
import android.util.AttributeSet;

public class FourThreeImageView extends ForegroundImageView {

	public FourThreeImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		int fourThreeHeight = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthSpec) * 3 / 4, MeasureSpec.EXACTLY);
		super.onMeasure(widthSpec, fourThreeHeight);
	}
}
