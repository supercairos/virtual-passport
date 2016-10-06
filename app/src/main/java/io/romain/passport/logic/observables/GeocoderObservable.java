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

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

public class GeocoderObservable implements Observable.OnSubscribe<Address> {

    private final Context mContext;

    private static final int MAX_RESULTS = 1;
    private final String mInput;

    public static Observable<Address> create(Context context, String input) {
        return Observable.create(new GeocoderObservable(context, input));
    }

    private GeocoderObservable(Context mContext, String input) {
        this.mContext = mContext;
        this.mInput = input;
    }

    @Override
    public void call(Subscriber<? super Address> subscriber) {
        Geocoder geocoder = new Geocoder(mContext);
        subscriber.onStart();
        try {
            List<Address> addresses = geocoder.getFromLocationName(mInput, MAX_RESULTS);
            if(addresses != null && !addresses.isEmpty()) {
                subscriber.onNext(addresses.get(0));
            } else {
                subscriber.onNext(null);
            }
            subscriber.onCompleted();
        } catch (IOException e) {
            subscriber.onError(e);
        }
    }
}
