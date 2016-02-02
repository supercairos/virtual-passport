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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;

import rx.Observable;
import rx.Subscriber;

public class GoogleApiObservable implements Observable.OnSubscribe<LocationSettingsResult> {

    private final LocationRequest mRequest;
    private final GoogleApiClient mClient;

    public GoogleApiObservable(GoogleApiClient client, LocationRequest request) {
        mClient = client;
        mRequest = request;
    }

    @Override
    public void call(Subscriber<? super LocationSettingsResult> subscriber) {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mRequest);

        PendingResult<LocationSettingsResult> pending = LocationServices.SettingsApi.checkLocationSettings(mClient, builder.build());
        pending.setResultCallback(result -> {
            subscriber.onNext(result);
            subscriber.onCompleted();
        });
    }

    public static Observable<LocationSettingsResult> checkLocation(GoogleApiClient client, LocationRequest request) {
        return Observable.create(new GoogleApiObservable(client, request));
    }
}
