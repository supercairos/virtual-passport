package io.romain.passport.ui.adaptater;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import io.romain.passport.MyApplication;
import io.romain.passport.R;
import io.romain.passport.model.City;
import io.romain.passport.utils.Dog;
import retrofit.Call;
import retrofit.Response;
import retrofit.Retrofit;

public class CitySearchAdapter extends ArrayAdapter<City> implements Filterable {

	@Inject
	Retrofit mRetrofit;

	private final LayoutInflater mLayoutInflater;
	private final Filter mFilter = new TownFilter();

	public CitySearchAdapter(Context context) {
		super(context, 0);
		mLayoutInflater = LayoutInflater.from(context);
		MyApplication.getApplication(context).getApplicationComponent().inject(this);
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if (view == null) {
			view = mLayoutInflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false);
		}

		City city = getItem(position);
		((TextView) view).setText(getContext().getString(R.string.add_city_dropdown, city.name, city.country));

		return view;
	}

	@Override
	public Filter getFilter() {
		return mFilter;
	}

	private class TownFilter extends Filter {

		@Override
		@NonNull
		protected FilterResults performFiltering(@Nullable CharSequence constraint) {
			FilterResults results = new FilterResults();
			Call<List<City>> call = mRetrofit.create(City.CityService.class).search((String) constraint);
			try {
				Response<List<City>> response = call.execute();
				if(response.isSuccess()) {
					List<City> cities = response.body();
					results.values = cities;
					results.count = cities != null ? cities.size() : 0;
				}
			} catch (IOException e) {
				Dog.e(e, "IOException");
			}

			return results;
		}

		@Override
		protected void publishResults(@Nullable CharSequence constraint, @NonNull FilterResults results) {
			//noinspection unchecked
			List<City> towns = (List<City>) results.values;
			clear();
			if (towns != null && towns.size() > 0) {
				addAll(towns);
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}
