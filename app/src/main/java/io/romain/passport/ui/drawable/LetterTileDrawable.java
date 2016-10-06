/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.romain.passport.ui.drawable;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import io.romain.passport.R;

/**
 * A drawable that encapsulates all the functionality needed to display a letter tile to
 * represent a contact image.
 */
public class LetterTileDrawable extends Drawable {

	private final Paint mPaint;

	/**
	 * Letter tile
	 */
	private static TypedArray sColors;
	private static int sDefaultColor;
	private static int sTileFontColor;
	private static float sLetterToTileRatio;

	private static final float DEFAULT_PERSON_AVATAR_SCALE = 0.8f;
	private static Drawable DEFAULT_PERSON_AVATAR;

	/**
	 * Reusable components to avoid new allocations
	 */
	private static final Paint sPaint = new Paint();
	private static final Rect sRect = new Rect();
	private static final char[] sFirstChar = new char[1];

	private String mDisplayName;
	private String mIdentifier;

	public static LetterTileDrawable create(@NonNull final Resources res) {
		return create(res, null);
	}

	public static LetterTileDrawable create(@NonNull final Resources res, @Nullable String name) {
		LetterTileDrawable dwb = new LetterTileDrawable(res);
		dwb.setContactDetails(name);

		return dwb;
	}

	private LetterTileDrawable(final Resources res) {
		mPaint = new Paint();
		mPaint.setFilterBitmap(true);
		mPaint.setDither(true);

		if (sColors == null) {
			sColors = res.obtainTypedArray(R.array.letter_tile_colors);
			sDefaultColor = res.getColor(R.color.letter_tile_default_color);
			sTileFontColor = res.getColor(R.color.letter_tile_font_color);
			//noinspection ResourceType
			sLetterToTileRatio = res.getFraction(R.dimen.letter_to_tile_ratio, 1, 1);

			DEFAULT_PERSON_AVATAR = res.getDrawable(R.drawable.ic_person_white_24dp);

			sPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
			sPaint.setTextAlign(Align.CENTER);
			sPaint.setAntiAlias(true);
		}
	}

	@Override
	public void draw(final Canvas canvas) {
		final Rect bounds = getBounds();
		if (!isVisible() || bounds.isEmpty()) {
			return;
		}
		// Draw letter tile.
		drawLetterTile(canvas);
	}

	private void drawLetterTile(final Canvas canvas) {
		// Draw background color.
		sPaint.setColor(pickColor(mIdentifier));

		sPaint.setAlpha(mPaint.getAlpha());
		final Rect bounds = getBounds();
		final int minDimension = Math.min(bounds.width(), bounds.height());
		canvas.drawCircle(bounds.centerX(), bounds.centerY(), minDimension / 2, sPaint);

		// Draw letter/digit only if the first character is an english letter
		if (mDisplayName != null && isEnglishLetter(mDisplayName.charAt(0))) {
			// Draw letter or digit.
			sFirstChar[0] = Character.toUpperCase(mDisplayName.charAt(0));

			// Scale text by canvas bounds and user selected scaling factor
			sPaint.setTextSize(sLetterToTileRatio * minDimension);
			//sPaint.setTextSize(sTileLetterFontSize);
			sPaint.getTextBounds(sFirstChar, 0, 1, sRect);
			sPaint.setColor(sTileFontColor);

			// Draw the letter in the canvas, vertically shifted up or down by the user-defined
			// offset
			canvas.drawText(sFirstChar, 0, 1, bounds.centerX(),
					bounds.centerY() + sRect.height() / 2,
					sPaint);
		} else {
			// Draw the default image if there is no letter/digit to be drawn
			Drawable drawable = getDefaultBitmap();
			Rect current = getBounds();
			final int halfLength = (int) (DEFAULT_PERSON_AVATAR_SCALE * Math.min(current.width(), current.height()) / 2);
			drawable.setBounds(new Rect(
					current.centerX() - halfLength,
					current.centerY() - halfLength,
					current.centerX() + halfLength,
					current.centerY() + halfLength
			));
			drawable.draw(canvas);
		}
	}

	public int getColor() {
		return pickColor(mIdentifier);
	}

	/**
	 * Returns a deterministic color based on the provided contact identifier string.
	 */
	private int pickColor(final String identifier) {
		if (TextUtils.isEmpty(identifier)) {
			return sDefaultColor;
		}
		// String.hashCode() implementation is not supposed to change across java versions, so
		// this should guarantee the same email address always maps to the same color.
		// The email should already have been normalized by the ContactRequest.
		final int color = Math.abs(identifier.hashCode()) % sColors.length();
		return sColors.getColor(color, sDefaultColor);
	}

	private static Drawable getDefaultBitmap() {
		return DEFAULT_PERSON_AVATAR;
	}

	private static boolean isEnglishLetter(final char c) {
		return ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z');
	}

	@Override
	public void setAlpha(final int alpha) {
		mPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(final ColorFilter cf) {
		mPaint.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.OPAQUE;
	}

	public void setContactDetails(final String name) {
		mDisplayName = name;
		if (!TextUtils.isEmpty(mDisplayName)) {
			mIdentifier = String.valueOf(name.hashCode());
		}
	}
}
