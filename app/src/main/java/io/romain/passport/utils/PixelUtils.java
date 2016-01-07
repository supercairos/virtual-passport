package io.romain.passport.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

@SuppressWarnings("unused")
public final class PixelUtils {

	/**
	 * This method converts dp unit to equivalent pixels, depending on device density.
	 *
	 * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
	 * @return A float value to represent px equivalent to dp depending on device density
	 */
	public static int convertDpToPixel(Context context, float dp) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		return (int) (dp * (metrics.densityDpi / 160f));
	}

	/**
	 * This method converts device specific pixels to density independent pixels.
	 *
	 * @param px A value in px (pixels) unit. Which we need to convert into db
	 * @return A float value to represent dp equivalent to px value
	 */
	public static int convertPixelsToDp(Context context, float px) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		return (int) (px / (metrics.densityDpi / 160f));
	}
}
