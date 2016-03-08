/*
 *    Copyright 2016 Romain
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package io.romain.passport.utils.glide;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import io.romain.passport.R;
import io.romain.passport.ui.views.FourThreeImageView;
import io.romain.passport.utils.ColorUtils;
import io.romain.passport.utils.ViewUtils;

public class CityItemTarget extends GlideDrawableImageViewTarget implements Palette.PaletteAsyncListener {

	private final TextView mTextView;
	private final ProgressBar mProgressBar;
	private final ImageView mRemoveImageView;

	public CityItemTarget(FourThreeImageView view, TextView text, ProgressBar loading, ImageView remove) {
		super(view);
		mTextView = text;
		mProgressBar = loading;
		mRemoveImageView = remove;
	}

	@Override
	public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
		super.onResourceReady(resource, animation);
		mProgressBar.setVisibility(View.GONE);
		if (resource instanceof GlideBitmapDrawable) {
			Bitmap image = ((GlideBitmapDrawable) resource).getBitmap();
			Palette.from(image)
//                    .setRegion(0, (int) (image.getHeight() * 0.66), image.getWidth() - 1, image.getHeight() - 1)
					.clearFilters()
					.generate(this);
		} else if (resource instanceof GifDrawable) {
			Bitmap image = ((GifDrawable) resource).getFirstFrame();
			Palette.from(image)
//                    .setRegion(0, 2 * (image.getHeight() / 3), image.getWidth() - 1, image.getHeight() - 1)
					.clearFilters()
					.generate(this);
		}
	}

	@Override
	public void onLoadStarted(Drawable placeholder) {
		super.onLoadStarted(placeholder);
		mProgressBar.setVisibility(View.VISIBLE);
		mTextView.setTextColor(getColor(mTextView.getContext(), R.color.text_primary_dark));
		mRemoveImageView.setImageTintList(ColorStateList.valueOf(getColor(mRemoveImageView.getContext(), android.R.color.black)));
	}

	@Override
	public void onLoadFailed(Exception e, Drawable errorDrawable) {
		super.onLoadFailed(e, errorDrawable);
		mProgressBar.setVisibility(View.GONE);
	}

	@Override
	public void onGenerated(Palette palette) {
		Context context = getView().getContext();
		//noinspection RedundantCast
		((FourThreeImageView) getView()).setForeground(
				ViewUtils.createRipple(palette, 0.25f, 0.5f, ContextCompat.getColor(context, R.color.mid_grey), true)
		);

		int color;
		switch (ColorUtils.isDark(palette)) {
			default:
			case ColorUtils.LIGHTNESS_UNKNOWN:
			case ColorUtils.IS_DARK:
				color = getColor(context, android.R.color.white);
				mTextView.setTextColor(getColor(context, R.color.text_primary_light));
				break;
			case ColorUtils.IS_LIGHT:
				color = getColor(context, android.R.color.black);
				mTextView.setTextColor(getColor(context, R.color.text_primary_dark));
				break;
		}

		mRemoveImageView.setImageResource(R.drawable.ic_close_36dp);
		mRemoveImageView.setImageTintList(ColorStateList.valueOf(color));
	}

	@SuppressWarnings("deprecation")
	@ColorInt
	private int getColor(Context context, @ColorRes int res) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return context.getColor(res);
		} else {
			return context.getResources().getColor(res);
		}
	}
}
