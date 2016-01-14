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

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.romain.passport.R;
import io.romain.passport.model.City;
import io.romain.passport.model.database.PassportContentProvider;
import io.romain.passport.ui.CityDetailActivity;
import io.romain.passport.ui.views.FourThreeImageView;
import io.romain.passport.utils.glide.CityItemTarget;

public class CityListAdapter extends CursorRecyclerAdapter<CityListAdapter.CityListViewHolder> {

	public class CityListViewHolder extends RecyclerView.ViewHolder {

		@Bind(R.id.item_city_list_picture)
		FourThreeImageView picture;
		@Bind(R.id.item_city_list_name)
		TextView name;
		@Bind(R.id.loading)
		ProgressBar loading;
		@Bind(R.id.item_city_list_card_view)
		CardView card;
		@Bind(R.id.item_city_list_remove)
		ImageView remove;

		public CityListViewHolder(final View view) {
			super(view);
			ButterKnife.bind(this, view);
			card.setOnClickListener(v -> {
				City city = City.fromCursor((Cursor) getItem(getAdapterPosition()));

				Intent intent = new Intent(getContext(), CityDetailActivity.class);
				intent.putExtra(CityDetailActivity.EXTRA_CITY, city);

//				ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(mActivity,
//						Pair.create(view, getContext().getString(R.string.transition_picture))
//						Pair.create(view, getContext().getString(R.string.transition_detail_background))
//				);

				getContext().startActivity(intent); // , options.toBundle());
			});
			remove.setOnClickListener(v -> {
				City city = City.fromCursor((Cursor) getItem(getAdapterPosition()));
				getContext().getContentResolver().delete(
						PassportContentProvider.Cities.CONTENT_URI,
						City.CityColumns._ID + " = ?",
						new String[]{String.valueOf(city.id)}
				);
			});
			card.setOnTouchListener((v, event) -> {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
						v.animate().z(v.getResources().getDimension(R.dimen.z_card_view_pressed)).start();
						break;
					case MotionEvent.ACTION_UP:
						v.animate().z(v.getResources().getDimension(R.dimen.z_card_view)).start();
						break;
				}

				return false;
			});
		}
	}

	private final Activity mActivity;
	private final LayoutInflater mLayoutInflater;

	public CityListAdapter(Activity activity, Cursor c) {
		super(activity, c);
		mActivity = activity;
		mLayoutInflater = LayoutInflater.from(activity);
	}

	@Override
	public CityListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new CityListViewHolder(mLayoutInflater.inflate(R.layout.item_city_list, parent, false));
	}

	@Override
	public void onBindViewHolder(CityListViewHolder view, Cursor cursor) {
		City city = City.fromCursor(cursor);
		view.name.setText(getContext().getString(R.string.add_city_dropdown, city.name, city.country));
		Glide.with(getContext())
				.load(city.picture)
				.crossFade()
//				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.fallback(R.drawable.city_list_placeholder)
				.error(R.drawable.city_list_placeholder)
				.centerCrop()
				.into(new CityItemTarget(view.picture, view.name, view.loading, view.remove));
	}
}
