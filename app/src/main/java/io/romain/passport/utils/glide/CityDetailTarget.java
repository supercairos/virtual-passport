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
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import io.romain.passport.R;
import io.romain.passport.utils.ColorUtils;

public class CityDetailTarget extends GlideDrawableImageViewTarget implements Palette.PaletteAsyncListener {

	private static final float SCRIM_ADJUSTMENT = 0.075f;

	private final CollapsingToolbarLayout mCollapsingToolbarLayout;
	private final Toolbar mToolbar;
	private final Context mContext;

	public CityDetailTarget(ImageView view, CollapsingToolbarLayout text, Toolbar toolbar) {
		super(view);
		mCollapsingToolbarLayout = text;
		mToolbar = toolbar;
		mContext = view.getContext();
	}

	@Override
	public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
		super.onResourceReady(resource, animation);
		if (resource instanceof GlideBitmapDrawable) {
			Bitmap image = ((GlideBitmapDrawable) resource).getBitmap();
			Palette.from(image).clearFilters().generate(this);
		} else if (resource instanceof GifDrawable) {
			Bitmap image = ((GifDrawable) resource).getFirstFrame();
			Palette.from(image).clearFilters().generate(this);
		}
	}

	@Override
	public void onLoadStarted(Drawable placeholder) {
		super.onLoadStarted(placeholder);
		mCollapsingToolbarLayout.setExpandedTitleColor(getColor(mContext, R.color.text_primary_dark));
		mCollapsingToolbarLayout.setCollapsedTitleTextColor(getColor(mContext, R.color.text_primary_dark));
	}

	@Override
	public void onGenerated(Palette palette) {
		int color;
		switch (ColorUtils.isDark(palette)) {
			default:
			case ColorUtils.LIGHTNESS_UNKNOWN:
			case ColorUtils.IS_DARK:
				mCollapsingToolbarLayout.setExpandedTitleColor(getColor(mContext, R.color.text_primary_light));
				mCollapsingToolbarLayout.setCollapsedTitleTextColor(getColor(mContext, R.color.text_primary_light));
				color = getColor(mContext, android.R.color.white);
				break;
			case ColorUtils.IS_LIGHT:
				mCollapsingToolbarLayout.setExpandedTitleColor(getColor(mContext, R.color.text_primary_dark));
				mCollapsingToolbarLayout.setCollapsedTitleTextColor(getColor(mContext, R.color.text_primary_dark));
				color = getColor(mContext, android.R.color.black);
				break;
		}

		Drawable dwb = mContext.getDrawable(R.drawable.ic_arrow_back_24dp);
		if (dwb != null) {
			dwb.setTint(color);
			mToolbar.setNavigationIcon(dwb);
		}

		Palette.Swatch colors = ColorUtils.getMostPopulousSwatch(palette);
		if (colors != null) {
			mCollapsingToolbarLayout.setContentScrimColor(colors.getRgb());
			mCollapsingToolbarLayout.setStatusBarScrimColor(ColorUtils.scrimify(colors.getRgb(), SCRIM_ADJUSTMENT));
		}

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
