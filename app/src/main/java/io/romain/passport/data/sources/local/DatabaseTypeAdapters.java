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
package io.romain.passport.data.sources.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.squareup.sqldelight.ColumnAdapter;

public class DatabaseTypeAdapters {

	public static final ColumnAdapter<Uri> URI_ADAPTER = new ColumnAdapter<Uri>() {
		@Override
		public Uri map(Cursor cursor, int columnIndex) {
			String value = cursor.getString(columnIndex);
			if(value != null) {
				return Uri.parse(cursor.getString(columnIndex));
			}

			return null;
		}

		@Override
		public void marshal(ContentValues values, String key, Uri value) {
			if (value != null) {
				values.put(key, value.toString());
			}
		}
	};


}
