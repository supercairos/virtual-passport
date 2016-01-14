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

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

import java.util.List;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

public class City implements Parcelable {

	// api endpoint
	private static final String MODULE = "/cities";

	// endpoints
	private static final String GET_SEARCH = MODULE + "/search";

	public long id = -1;

	@Expose
	@SerializedName("name")
	public String name;
	@Expose
	@SerializedName("country")
	public String country;

	@Expose
	@SerializedName("longitude")
	public double longitude;
	@Expose
	@SerializedName("latitude")
	public double latitude;

	@Expose
	@SerializedName("picture")
	public Uri picture;

	public interface CityService {
		@GET(GET_SEARCH)
		Call<List<City>> search(@Query("query") String sort);
	}

	public interface CityColumns {

		@DataType(DataType.Type.INTEGER)
		@PrimaryKey
		@AutoIncrement
		String _ID = BaseColumns._ID;

		@DataType(DataType.Type.TEXT)
		@NotNull
		String NAME = "name";

		@DataType(DataType.Type.TEXT)
		@NotNull
		String COUNTRY = "country";

		@DataType(DataType.Type.REAL)
		@NonNull
		String LATITUDE = "latitude";

		@DataType(DataType.Type.REAL)
		@NotNull
		String LONGITUDE = "longitude";

		@DataType(DataType.Type.TEXT)
		String PICTURE = "picture";
	}

	public static final String[] _PROJECTION = new String[]{
			CityColumns._ID,
			CityColumns.NAME,
			CityColumns.COUNTRY,
			CityColumns.LONGITUDE,
			CityColumns.LATITUDE,
			CityColumns.PICTURE,
	};

	public static final int ID = 0;
	public static final int NAME = 1;
	public static final int COUNTRY = 2;
	public static final int LONGITUDE = 3;
	public static final int LATITUDE = 4;
	public static final int PICTURE = 5;

	public static City fromCursor(Cursor cursor) {
		City town = new City();
		town.id = cursor.getLong(ID);
		town.name = cursor.getString(NAME);
		town.country = cursor.getString(COUNTRY);

		town.longitude = cursor.getDouble(LONGITUDE);
		town.latitude = cursor.getDouble(LATITUDE);

		String p = cursor.getString(PICTURE);
		if (!TextUtils.isEmpty(p)) {
			town.picture = Uri.parse(p);
		}

		return town;
	}

	public Object[] toArray() {
		Object[] objects = new Object[_PROJECTION.length];
		objects[ID] = id;
		objects[NAME] = name;
		objects[COUNTRY] = country;
		objects[LONGITUDE] = longitude;
		objects[LATITUDE] = latitude;
		objects[PICTURE] = picture;

		return objects;
	}

	public ContentValues toContentValues() {
		ContentValues cv = new ContentValues();
		if (id > 0) {
			cv.put(CityColumns._ID, id);
		}

		if (!TextUtils.isEmpty(name)) {
			cv.put(CityColumns.NAME, name);
		}

		if (!TextUtils.isEmpty(country)) {
			cv.put(CityColumns.COUNTRY, country);
		}

		cv.put(CityColumns.LONGITUDE, longitude);
		cv.put(CityColumns.LATITUDE, latitude);

		if (picture != null) {
			cv.put(CityColumns.PICTURE, picture.toString());
		}

		return cv;
	}

	@Override
	public String toString() {
		return name + ", " + country;
	}

	// ## GENERATED PARCELABLE
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.id);
		dest.writeString(this.name);
		dest.writeString(this.country);
		dest.writeDouble(this.longitude);
		dest.writeDouble(this.latitude);
		dest.writeParcelable(this.picture, 0);
	}

	public City() {
		super();
	}

	protected City(Parcel in) {
		this.id = in.readLong();
		this.name = in.readString();
		this.country = in.readString();
		this.longitude = in.readDouble();
		this.latitude = in.readDouble();
		this.picture = in.readParcelable(Uri.class.getClassLoader());
	}

	public static final Creator<City> CREATOR = new Creator<City>() {
		public City createFromParcel(Parcel source) {
			return new City(source);
		}

		public City[] newArray(int size) {
			return new City[size];
		}
	};
}
