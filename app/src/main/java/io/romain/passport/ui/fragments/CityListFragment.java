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
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import io.romain.passport.R;
import io.romain.passport.data.City;
import io.romain.passport.data.sources.local.PassportContentProvider;
import io.romain.passport.ui.AddCityActivity;
import io.romain.passport.ui.CityDetailActivity;
import io.romain.passport.ui.MainActivity;
import io.romain.passport.ui.adaptater.CityListAdapter;
import io.romain.passport.ui.views.EmptyRecyclerView;

public class CityListFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>, CityListAdapter.OnCityClicked {

	private static final int LOADER_ID = 5489;

	@SuppressWarnings("WeakerAccess")
	@BindView(android.R.id.list)
	EmptyRecyclerView mRecyclerView;
	@SuppressWarnings("WeakerAccess")
	@BindView(android.R.id.empty)
	TextView mEmptyView;

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
		// use this setting to improve performance if you know that changes
		// in content do not change the layout size of the RecyclerView
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		mRecyclerView.setEmptyView(mEmptyView);
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(LOADER_ID, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (TextUtils.isEmpty(mQuery)) {
			return new CursorLoader(
					getContext(),
					PassportContentProvider.Cities.CONTENT_URI,
					null,
					null,
					null,
					null
			);
		} else {
			return new CursorLoader(
					getContext(),
					PassportContentProvider.Cities.CONTENT_URI,
					null,
					City.NAME + " LIKE " + DatabaseUtils.sqlEscapeString(mQuery + "%"),
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

		mEmptyView.setText(TextUtils.isEmpty(mQuery) ? R.string.city_list_empty_text : R.string.city_list_empty_not_found);
	}

	@OnClick(android.R.id.empty)
	public void onEmptyViewClicked() {
		if(!TextUtils.isEmpty(mQuery)) {
			startActivity(new Intent(getActivity(), AddCityActivity.class).putExtra(AddCityActivity.EXTRA_INITIAL_TEXT, mQuery));
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
	public void onCityClicked(final View v, final City city) {
		Intent intent = new Intent(getContext(), CityDetailActivity.class);
		intent.putExtra(CityDetailActivity.EXTRA_CITY, city);
		ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
//				Pair.create((View) image, getContext().getString(R.string.transition_picture)),
				(Pair<View, String>) Pair.create(v, getContext().getString(R.string.transition_detail_background))
		);

		getContext().startActivity(intent, options.toBundle());
	}

	@Override
	public void onCityFavorited(View v, City city) {
		ContentValues cv = new ContentValues();
		cv.put(City.FAVORITE, city.favorite() ? 0 : 1);

		getContext().getContentResolver().update(
				PassportContentProvider.Cities.CONTENT_URI,
				cv,
				City._ID + " = ?",
				new String[]{String.valueOf(city._id())}
		);
	}

	@Override
	public void onCityRemoved(final View v, final City city) {
		getContext().getContentResolver().delete(
				PassportContentProvider.Cities.CONTENT_URI,
				City._ID + " = ?",
				new String[]{String.valueOf(city._id())}
		);

		Snackbar.make(((MainActivity) getActivity()).getCoordinatorLayout(), getString(R.string.city_removed_successfully, city.name()), Snackbar.LENGTH_SHORT)
				.setAction(R.string.undo, view -> getContext().getContentResolver().insert(PassportContentProvider.Cities.CONTENT_URI, city.asContentValues()))
				.show();
	}
}
