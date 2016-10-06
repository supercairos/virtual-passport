package io.romain.passport.data.sources;

import io.romain.passport.data.City;
import rx.Observable;

/**
 * Created by Romain on 04/08/2016.
 */

public interface CityDataSource {

	Observable<City> get();

	Observable<City> find(String query);

	void save(final City city);

	void favorite(final City city);
}
