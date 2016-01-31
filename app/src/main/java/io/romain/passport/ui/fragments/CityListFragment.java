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

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.romain.passport.R;
import io.romain.passport.model.City;
import io.romain.passport.model.database.PassportContentProvider;
import io.romain.passport.ui.CityDetailActivity;
import io.romain.passport.ui.MainActivity;
import io.romain.passport.ui.adaptater.CityListAdapter;

public class CityListFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 5489;

    private LayoutInflater mLayoutInflater;
    private RecyclerView mRecyclerView;

    private CityListAdapter mAdapter;
    private String mQuery;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayoutInflater = LayoutInflater.from(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return mLayoutInflater.inflate(R.layout.fragment_city_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = (RecyclerView) view;

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
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
            mAdapter = new CityListAdapter(getActivity(), data, new CityListAdapter.OnCityClicked() {
                @Override
                public void onCityClicked(View view, final City city) {
                    Intent intent = new Intent(getContext(), CityDetailActivity.class);
                    intent.putExtra(CityDetailActivity.EXTRA_CITY, city);

//                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
//                            Pair.create(view, getContext().getString(R.string.transition_picture)),
//                            Pair.create(view, getContext().getString(R.string.transition_detail_background))
//                    );

                    getContext().startActivity(intent); // , options.toBundle());
                }

                @Override
                public void onCityRemoved(View view, final City city) {
                    getContext().getContentResolver().delete(
                            PassportContentProvider.Cities.CONTENT_URI,
                            City.CityColumns._ID + " = ?",
                            new String[]{String.valueOf(city.id)}
                    );

                    Snackbar.make(((MainActivity) getActivity()).getCoordinatorLayout(), R.string.city_removed, Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo, v -> {
                                getContext().getContentResolver().insert(PassportContentProvider.Cities.CONTENT_URI, city.toContentValues());
                            })
                            .show();
                }
            });
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
}
