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

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.Subscriptions;

public class LocationUpdatesObservable implements Observable.OnSubscribe<Location> {

    private final GoogleApiClient mClient;
    private final LocationRequest mRequest;

    public static Observable<Location> create(GoogleApiClient client, LocationRequest request) {
        return Observable.create(new LocationUpdatesObservable(client, request));
    }

    public LocationUpdatesObservable(GoogleApiClient client, LocationRequest request) {
        mClient = client;
        mRequest = request;
    }

    @Override
    public void call(Subscriber<? super Location> subscriber) {
        subscriber.onStart();
        LastKnownLocationObservable
                .create(mClient)
                .subscribe(subscriber::onNext, subscriber::onError);

        if (ContextCompat.checkSelfPermission(mClient.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(mClient.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            final LocationListener listener = subscriber::onNext;

            LocationServices.FusedLocationApi.requestLocationUpdates(mClient, mRequest, listener);
            subscriber.add(Subscriptions.create(() -> {
                if (mClient.isConnected()) {
                    LocationServices.FusedLocationApi.removeLocationUpdates(mClient, listener);
                }
            }));
        } else {
            subscriber.onError(new SecurityException("You don't have the permission..."));
        }
    }
}
