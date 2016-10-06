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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.view.Gravity;
import android.view.View;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import java.util.HashMap;

import io.romain.passport.R;
import io.romain.passport.data.City;
import io.romain.passport.ui.adaptater.CityListAdapter;
import io.romain.passport.ui.views.FourThreeImageView;
import io.romain.passport.utils.ColorUtils;
import io.romain.passport.utils.Dog;
import io.romain.passport.utils.ScrimUtil;
import io.romain.passport.utils.ViewUtils;

public class CityItemTarget extends GlideDrawableImageViewTarget implements Palette.PaletteAsyncListener {

	private static final HashMap<Uri, Palette> sUrlPaletteCache = new HashMap<>();

	private final CityListAdapter.CityListViewHolder mViewHolder;
	private final City mCity;

	public CityItemTarget(City city, CityListAdapter.CityListViewHolder holder) {
		super(holder.picture);
		mViewHolder = holder;
		mCity = city;
	}

	@Override
	public void onLoadStarted(Drawable placeholder) {
		super.onLoadStarted(placeholder);
		mViewHolder.loading.setVisibility(View.VISIBLE);
		mViewHolder.content.setVisibility(View.GONE);

		mViewHolder.name.setTextColor(getColor(mViewHolder.name.getContext(), R.color.text_primary_dark));
		mViewHolder.remove.setImageTintList(ColorStateList.valueOf(getColor(mViewHolder.remove.getContext(), android.R.color.black)));
	}

	@Override
	public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
		super.onResourceReady(resource, animation);
		mViewHolder.loading.setVisibility(View.GONE);
		mViewHolder.content.setVisibility(View.VISIBLE);

		final Bitmap image;
		if (resource instanceof GlideBitmapDrawable) {
			image = ((GlideBitmapDrawable) resource).getBitmap();
		} else if (resource instanceof GifDrawable) {
			image = ((GifDrawable) resource).getFirstFrame();
		} else {
			image = null;
		}

		if (image != null) {
			Palette palette = sUrlPaletteCache.get(mCity.picture());
			if (palette != null) {
				mViewHolder.content.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
					@Override
					public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
						mViewHolder.content.removeOnLayoutChangeListener(this);
						Palette.from(image)
								.clearFilters()
								.setRegion(mViewHolder.content.getLeft(), mViewHolder.content.getTop(), mViewHolder.content.getRight(), mViewHolder.content.getBottom())
//								.maximumColorCount(10)
								.generate(CityItemTarget.this);
					}
				});
			} else {
				onGenerated(null);
			}
		}
	}

	@Override
	public void onLoadFailed(Exception e, Drawable errorDrawable) {
		super.onLoadFailed(e, errorDrawable);
		mViewHolder.loading.setVisibility(View.GONE);
		mViewHolder.content.setVisibility(View.VISIBLE);

		if (e != null) {
			Dog.e(e, "Ex");
		}
	}

	@SuppressWarnings("RedundantCast")
	@Override
	@TargetApi(21)
	public void onGenerated(@Nullable Palette palette) {
		if(palette != null) {
			sUrlPaletteCache.put(mCity.picture(), palette);
		}

		Context context = getView().getContext();
		((FourThreeImageView) getView()).setForeground(
				new LayerDrawable(
						new Drawable[]{
								ViewUtils.createRipple(palette, 0.25f, 0.5f, ContextCompat.getColor(context, R.color.mid_grey), true),
								ScrimUtil.makeCubicGradientScrimDrawable(ContextCompat.getColor(context, R.color.scrim), 5, Gravity.BOTTOM)
						}
				)
		);

		int color;
		switch (ColorUtils.isDark(palette)) {
			default:
			case ColorUtils.LIGHTNESS_UNKNOWN:
			case ColorUtils.IS_DARK:
				color = getColor(context, android.R.color.white);
				mViewHolder.name.setTextColor(getColor(context, R.color.text_primary_light));
				break;
			case ColorUtils.IS_LIGHT:
				color = getColor(context, android.R.color.black);
				mViewHolder.name.setTextColor(getColor(context, R.color.text_primary_dark));
				break;
		}

		mViewHolder.favorite.setImageTintList(ColorStateList.valueOf(color));
		mViewHolder.remove.setImageTintList(ColorStateList.valueOf(color));
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
