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

import java.io.IOException;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;

public class ResponseBodyObservable implements Observable.OnSubscribe<String>{

	private final ResponseBody mResponseBody;

	public static Observable<String> flatten(ResponseBody body) {
		return Observable.create(new ResponseBodyObservable(body));
	}

	private ResponseBodyObservable(ResponseBody body) {
		this.mResponseBody = body;
	}

	@Override
	public void call(Subscriber<? super String> subscriber) {
		subscriber.onStart();
		try {
			subscriber.onNext(mResponseBody.string());
			subscriber.onCompleted();
		} catch (IOException e) {
			subscriber.onError(e);
		}
	}
}
