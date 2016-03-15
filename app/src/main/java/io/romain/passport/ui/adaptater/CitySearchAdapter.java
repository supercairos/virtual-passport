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
import io.romain.passport.model.CityAutocomplete;
import io.romain.passport.utils.Dog;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CitySearchAdapter extends ArrayAdapter<CityAutocomplete> implements Filterable {

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

		((TextView) view).setText(getItem(position).name);

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
			Call<List<CityAutocomplete>> call = mRetrofit.create(CityAutocomplete.CityAutocompleteService.class).complete((String) constraint);
			try {
				Response<List<CityAutocomplete>> response = call.execute();
				if(response.isSuccessful()) {
					List<CityAutocomplete> cities = response.body();
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
			List<CityAutocomplete> towns = (List<CityAutocomplete>) results.values;
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
