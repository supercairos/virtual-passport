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
package io.romain.passport.model;

import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import io.romain.passport.model.database.DatabaseTypeAdapters;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

@AutoValue
public abstract class City implements Parcelable, CityTableModel {

	// api endpoint
	private static final String MODULE = "/cities";

	// endpoints
	private static final String GET_RESOLVE_ID = MODULE + "/resolve/{id}";
	private static final String GET_PICTURE = MODULE + "/picture/{latitude}/{longitude}";

	public abstract long _id();

	@SerializedName("name")
	public abstract String name();

	@SerializedName("country")
	public abstract String country();

	@SerializedName("longitude")
	public abstract double longitude();

	@SerializedName("latitude")
	public abstract double latitude();

	@SerializedName("picture")
	@Nullable
	public abstract Uri picture();

	public static final Mapper<City> MAPPER = new Mapper<>(City::create, DatabaseTypeAdapters.URI_ADAPTER);

	public interface CityService {
		@GET(GET_RESOLVE_ID)
		Observable<City> resolve(@Path("id") String id);

		@GET(GET_PICTURE)
		Observable<ResponseBody> picture(@Path("latitude") double latitude, @Path("longitude") double longitude);
	}

	public static City create(String name, String country, double longitude, double latitude) {
		return create(-1, name, false, country, longitude, latitude, null);
	}

	public static City create(String name, boolean favorite, String country, double longitude, double latitude) {
		return create(-1, name, favorite, country, longitude, latitude, null);
	}

	public static City create(String name, String country, double longitude, double latitude, Uri picture) {
		return create(-1, name, false, country, longitude, latitude, picture);
	}

	public static City create(String name, boolean favorite, String country, double longitude, double latitude, Uri picture) {
		return create(-1, name, favorite, country, longitude, latitude, picture);
	}

	public static City create(long id, String name, boolean favorite, String country, double longitude, double latitude, Uri picture) {
		return new AutoValue_City(favorite, id, name, country, longitude, latitude, picture);
	}

	public static TypeAdapter<City> fromJson(Gson gson) {
		return new AutoValue_City.GsonTypeAdapter(gson);
	}

	public static final class Marshal extends CityTableMarshal<Marshal> {
		public Marshal() {
			super(DatabaseTypeAdapters.URI_ADAPTER);
		}

		public Marshal(City city) {
			super(city, DatabaseTypeAdapters.URI_ADAPTER);
			// Make sure to not save the ID;
			contentValues.remove(_ID);
		}
	}
}
