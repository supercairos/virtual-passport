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
package io.romain.passport.model.database;

import android.content.ContentResolver;
import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

import io.romain.passport.model.City;

@ContentProvider(authority = PassportContentProvider.AUTHORITY, database = PassportDatabase.class)
public class PassportContentProvider {

	static final String AUTHORITY = "io.romain.passport";
	private static final Uri BASE_CONTENT_URI = new Uri.Builder()
			.scheme(ContentResolver.SCHEME_CONTENT)
			.authority(AUTHORITY)
			.build();

	@TableEndpoint(table = PassportDatabase.CITY_TABLE_NAME)
	public static class Cities {

		@ContentUri(
				path = PassportDatabase.CITY_TABLE_NAME,
				type = "vnd.android.cursor.dir/" + PassportDatabase.CITY_TABLE_NAME,
				defaultSort = City.CityColumns._ID + " ASC")
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
				.appendPath(PassportDatabase.CITY_TABLE_NAME)
				.build();
	}

}
