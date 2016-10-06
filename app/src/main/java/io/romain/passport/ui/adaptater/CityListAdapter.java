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
package io.romain.passport.ui.adaptater;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.ColorMatrixColorFilter;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.romain.passport.R;
import io.romain.passport.data.City;
import io.romain.passport.ui.views.FourThreeImageView;
import io.romain.passport.utils.AnimUtils;
import io.romain.passport.utils.Dog;
import io.romain.passport.utils.ObservableColorMatrix;
import io.romain.passport.utils.glide.CityItemTarget;

public class CityListAdapter extends CursorRecyclerAdapter<CityListAdapter.CityListViewHolder> {

	private final OnCityClicked mListener;
	private final HashSet<Uri> mUriFaded = new HashSet<>();

	public interface OnCityClicked {
		void onCityClicked(View v, City city);

		void onCityRemoved(View v, City city);

		void onCityFavorited(View v, City city);
	}

	public class CityListViewHolder extends RecyclerView.ViewHolder {

		@BindView(R.id.item_city_list_content)
		public LinearLayout content;
		@BindView(R.id.item_city_list_picture)
		public FourThreeImageView picture;
		@BindView(R.id.item_city_list_name)
		public TextView name;
		@BindView(R.id.item_city_list_favorite)
		public ImageView favorite;
		@BindView(R.id.item_city_list_loading)
		public ProgressBar loading;
		@BindView(R.id.item_city_list_card_view)
		public CardView card;
		@BindView(R.id.item_city_list_remove)
		public ImageView remove;

		public CityListViewHolder(final View view) {
			super(view);
			ButterKnife.bind(this, view);
			card.setOnClickListener(v -> mListener.onCityClicked(card, City.MAPPER.map((Cursor) getItem(getAdapterPosition()))));
			remove.setOnClickListener(v -> mListener.onCityRemoved(remove, City.MAPPER.map((Cursor) getItem(getAdapterPosition()))));
			favorite.setOnClickListener(v -> mListener.onCityFavorited(favorite, City.MAPPER.map((Cursor) getItem(getAdapterPosition()))));
		}
	}

	private final LayoutInflater mLayoutInflater;

	public CityListAdapter(Activity activity, Cursor c, OnCityClicked listener) {
		super(activity, c);
		mLayoutInflater = LayoutInflater.from(activity);
		mListener = listener;
	}

	@Override
	public CityListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new CityListViewHolder(mLayoutInflater.inflate(R.layout.item_city_list, parent, false));
	}

	@Override
	public void onBindViewHolder(CityListViewHolder holder, Cursor cursor) {
		City city = City.MAPPER.map(cursor);
		holder.name.setText(getContext().getString(R.string.city_concat_name_country, city.name(), city.country()));
		holder.favorite.setImageResource(city.favorite() ? R.drawable.ic_star_24dp : R.drawable.ic_star_border_24dp);
		Dog.d("Url : " + city.picture());
		Glide.with(getContext())
				.load(city.picture())
				.listener(new RequestListener<Uri, GlideDrawable>() {

					@Override
					public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
						return false;
					}

					@Override
					public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
						if (!mUriFaded.contains(model)) {
							holder.picture.setHasTransientState(true);
							final ObservableColorMatrix cm = new ObservableColorMatrix();
							final ObjectAnimator saturation = ObjectAnimator.ofFloat(cm, ObservableColorMatrix.SATURATION, 0f, 1f);
							saturation.addUpdateListener(valueAnimator -> {
								holder.picture.setColorFilter(new ColorMatrixColorFilter(cm));
							});
							saturation.setDuration(2000L);
							saturation.setInterpolator(AnimUtils.getFastOutSlowInInterpolator(getContext()));
							saturation.addListener(new AnimatorListenerAdapter() {
								@Override
								public void onAnimationEnd(Animator animation) {
									holder.picture.clearColorFilter();
									holder.picture.setHasTransientState(false);
									mUriFaded.add(model);
								}
							});
							saturation.start();
						}

						return false;
					}
				})
//				.diskCacheStrategy(DiskCacheStrategy.ALL)
//				.fallback(R.drawable.no_picture_found)
//				.error(R.drawable.no_picture_found)
				.centerCrop()
//				.into(holder.picture);
				.into(new CityItemTarget(city, holder));
	}
}
