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

import com.fernandocejas.frodo.annotation.RxLogObservable;

import io.romain.passport.model.City;
import io.romain.passport.model.database.PassportContentProvider;
import io.romain.passport.utils.Dog;
import rx.Observable;
import rx.Subscriber;

public class CitySaverObservable implements Observable.OnSubscribe<Uri> {

    private final Context mContext;
    private final City mCity;

    @RxLogObservable
    public static Observable<Uri> create(final Context context, City input) {
        return Observable.create(new CitySaverObservable(context, input));
    }

    public CitySaverObservable(Context context, City input) {
        mContext = context.getApplicationContext();
        mCity = input;
    }

    @Override
    public void call(Subscriber<? super Uri> subscriber) {
        ContentResolver resolver = mContext.getContentResolver();
        Dog.d("Inserted (Thread ID is : " + Thread.currentThread().getId() + ")");
        subscriber.onNext(resolver.insert(PassportContentProvider.Cities.CONTENT_URI, mCity.toContentValues()));
        subscriber.onCompleted();
    }
}
