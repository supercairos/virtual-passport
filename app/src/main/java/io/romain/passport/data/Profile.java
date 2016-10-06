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
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import io.romain.passport.data.sources.local.DatabaseTypeAdapters;
import io.romain.passport.utils.annotations.Redacted;

@AutoValue
public abstract class Profile implements Parcelable, ProfileTableModel {

	public abstract long _id();

	public abstract String server_id();

	@NonNull
	public abstract String name();

	@NonNull
	@Redacted
	public abstract String email();

	@Nullable
	public abstract Uri picture();

	private static final Factory<Profile> FACTORY = new Factory<>(Profile::create, DatabaseTypeAdapters.URI_ADAPTER);
	private static final Mapper<Profile> MAPPER = new Mapper<>(FACTORY);

	public static Profile create(String name, String email) {
		return create(-1, null, name, email, null);
	}

	public static Profile create(String name, String email, Uri uri) {
		return create(-1, null, name, email, uri);
	}

	public static Profile create(long id, String server, String name, String email, Uri uri) {
		return new AutoValue_Profile(id, server, name, email, uri);
	}

	public ContentValues asContentValue() {
		ContentValues cv =  FACTORY.marshal(this).asContentValues();
		if(_id() <= 0) {
			cv.remove(ProfileTableModel._ID);
		}

		return cv;
	}

	public static Profile fromCursor(Cursor cursor) {
		return MAPPER.map(cursor);
	}
}
