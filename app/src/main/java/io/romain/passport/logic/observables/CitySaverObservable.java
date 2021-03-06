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

import io.romain.passport.model.City;
import io.romain.passport.model.database.PassportContentProvider;
import io.romain.passport.utils.Dog;

public class CitySaverObservable {

    public static City save(final Context context, City input) {
        ContentResolver resolver = context.getContentResolver();
        Dog.d("Inserted (Thread ID is : " + Thread.currentThread().getName() + ")");
        resolver.insert(PassportContentProvider.Cities.CONTENT_URI, new City.Marshal(input).asContentValues());

        return input;
    }
}
