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
package io.romain.passport.logic.observables;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import io.romain.passport.data.City;
import io.romain.passport.data.Profile;
import io.romain.passport.data.sources.local.PassportContentProvider;
import io.romain.passport.utils.Dog;

public class DatabaseSaverObservable {

	public static City save(final Context context, City input) {
		ContentResolver resolver = context.getContentResolver();
		Dog.d("Inserted (Thread ID is : " + Thread.currentThread().getName() + ")");
		resolver.insert(PassportContentProvider.Cities.CONTENT_URI, input.asContentValues());

		return input;
	}

	public static boolean save(final Context context, Profile input) {
		ContentResolver resolver = context.getContentResolver();
		Dog.d("Inserted (Thread ID is : " + Thread.currentThread().getName() + ")");
		Uri uri = resolver.insert(PassportContentProvider.Cities.CONTENT_URI, input.asContentValue());

		return uri != null;
	}
}
