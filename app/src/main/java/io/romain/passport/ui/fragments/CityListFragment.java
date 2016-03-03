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
package io.romain.passport.ui.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import io.romain.passport.R;
import io.romain.passport.model.City;
import io.romain.passport.model.database.PassportContentProvider;
import io.romain.passport.ui.CityDetailActivity;
import io.romain.passport.ui.MainActivity;
import io.romain.passport.ui.adaptater.CityListAdapter;
import io.romain.passport.ui.adaptater.CityListItemAnimator;
import io.romain.passport.ui.views.EmptyRecyclerView;

public class CityListFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>, CityListAdapter.OnCityClicked {

	private static final int LOADER_ID = 5489;

	private EmptyRecyclerView mRecyclerView;

	private CityListAdapter mAdapter;
	private String mQuery;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_city_list, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mRecyclerView = (EmptyRecyclerView) view.findViewById(android.R.id.list);

		// use this setting to improve performance if you know that changes
		// in content do not change the layout size of the RecyclerView
		mRecyclerView.setHasFixedSize(true);

		// use a linear layout manager
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		mRecyclerView.setItemAnimator(new CityListItemAnimator());

		mRecyclerView.setEmptyView(view.findViewById(android.R.id.empty));
		new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.END) {

			@Override
			public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder holder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
				if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
					float width = (float) holder.itemView.getWidth();
					float alpha = 1.0f - Math.abs(dX) / width;

					holder.itemView.setAlpha(alpha);
					holder.itemView.setTranslationX(dX);
				} else {
					super.onChildDraw(c, recyclerView, holder, dX, dY, actionState, isCurrentlyActive);
				}
			}

			@Override
			public boolean isLongPressDragEnabled() {
				return false;
			}

			@Override
			public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
				return false;
			}

			@Override
			public void onSwiped(RecyclerView.ViewHolder holder, int direction) {
				if (mAdapter != null) {
					onCityRemoved(holder.itemView, City.fromCursor((Cursor) mAdapter.getItem(holder.getAdapterPosition())));
				}
			}
		}).attachToRecyclerView(mRecyclerView);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(LOADER_ID, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (TextUtils.isEmpty(mQuery)) {
			return new CursorLoader(getContext(), PassportContentProvider.Cities.CONTENT_URI, City._PROJECTION, null, null, null);
		} else {
			return new CursorLoader(
					getContext(),
					PassportContentProvider.Cities.CONTENT_URI,
					City._PROJECTION,
					City.CityColumns.NAME + " LIKE '" + mQuery + "%'",
					null,
					null
			);
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (mAdapter != null) {
			mAdapter.swapCursor(data);
		} else {
			mAdapter = new CityListAdapter(getActivity(), data, this);
			mRecyclerView.setAdapter(mAdapter);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (mAdapter != null) {
			mAdapter.swapCursor(null);
		}
	}

	public static Fragment newInstance() {
		return new CityListFragment();
	}

	public void setQuery(String query) {
		mQuery = query;
		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}


	@Override
	public void onCityClicked(CardView card, ImageView image, final City city) {
		Intent intent = new Intent(getContext(), CityDetailActivity.class);
		intent.putExtra(CityDetailActivity.EXTRA_CITY, city);
		ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
//				Pair.create((View) image, getContext().getString(R.string.transition_picture)),
				Pair.create((View) card, getContext().getString(R.string.transition_detail_background))
		);

		getContext().startActivity(intent, options.toBundle());
	}

	@Override
	public void onCityRemoved(View view, final City city) {
		getContext().getContentResolver().delete(
				PassportContentProvider.Cities.CONTENT_URI,
				City.CityColumns._ID + " = ?",
				new String[]{String.valueOf(city.id)}
		);

		Snackbar.make(((MainActivity) getActivity()).getCoordinatorLayout(), getString(R.string.city_removed_successfully, city.name), Snackbar.LENGTH_SHORT)
				.setAction(R.string.undo, v -> {
					getContext().getContentResolver().insert(PassportContentProvider.Cities.CONTENT_URI, city.toContentValues());
				})
				.show();
	}
}
