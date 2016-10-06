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
package io.romain.passport.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

@AutoValue
public abstract class Comment implements Parcelable, CommentTableModel {

	@SerializedName("local_id")
	public abstract long _id();

	@SerializedName("_id")
	@NonNull
	public abstract String server_id();

	@NonNull
	@SerializedName("profile_id")
	public abstract String profile_id();

	@NonNull
	public abstract String city_id();

	@NonNull
	@SerializedName("text")
	public abstract String text();

	@SerializedName("longitude")
	public abstract double longitude();

	@SerializedName("latitude")
	public abstract double latitude();

	private static final Factory<Comment> FACTORY = new Factory<>(Comment::create);
	private static final Mapper<Comment> MAPPER = new Mapper<>(FACTORY);

	public static Comment create(long id, String server, String profile, String city, String text, double longitude, double latitude) {
		return new AutoValue_Comment(id, server, profile, city, text, longitude, latitude);
	}

	public static TypeAdapter<Comment> getTypeAdapter(Gson gson) {
		return new AutoValue_Comment.GsonTypeAdapter(gson);
	}

	public ContentValues asContentValue() {
		return FACTORY.marshal(this).asContentValues();
	}

	public static Comment fromCursor(Cursor cursor) {
		return MAPPER.map(cursor);
	}
}
