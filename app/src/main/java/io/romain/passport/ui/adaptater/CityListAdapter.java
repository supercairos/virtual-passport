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
import android.database.Cursor;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.romain.passport.R;
import io.romain.passport.model.City;
import io.romain.passport.ui.views.FourThreeImageView;
import io.romain.passport.utils.glide.CityItemTarget;

public class CityListAdapter extends CursorRecyclerAdapter<CityListAdapter.CityListViewHolder> {

    private final OnCityClicked mListener;

    public interface OnCityClicked {
        void onCityClicked(View v, City city);

        void onCityRemoved(View v, City city);
    }

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
                mListener.onCityClicked(v, city);
            });
            remove.setOnClickListener(v -> {
                City city = City.fromCursor((Cursor) getItem(getAdapterPosition()));
                mListener.onCityRemoved(v, city);
            });
//            card.setOnTouchListener((v, event) -> {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        v.animate().z(v.getResources().getDimension(R.dimen.z_card_view_pressed)).start();
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        v.animate().z(v.getResources().getDimension(R.dimen.z_card_view)).start();
//                        break;
//                }
//
//                return false;
//            });
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
    public void onBindViewHolder(CityListViewHolder view, Cursor cursor) {
        City city = City.fromCursor(cursor);
        view.name.setText(getContext().getString(R.string.city_concat_name_country, city.name, city.country));
        Glide.with(getContext())
                .load(city.picture)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fallback(R.drawable.no_picture_found)
                .error(R.drawable.no_picture_found)
                .centerCrop()
                .into(new CityItemTarget(view.picture, view.name, view.loading, view.remove));
    }
}
