package io.romain.passport.logic.observables.auth;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutionException;

import io.romain.passport.R;
import rx.Observable;
import rx.Subscriber;

public class FirebaseUploadObservable implements Observable.OnSubscribe<Uri> {

	private final Activity mContext;
	private final Uri mFile;
	private final String mReference;

	public static Observable<Uri> create(Activity context, Uri file, String ref) {
		return Observable.create(new FirebaseUploadObservable(context, file, ref));
	}

	private FirebaseUploadObservable(Activity context, Uri file, String ref) {
		this.mContext = context;
		this.mFile = file;
		this.mReference = ref;
	}

	@Override
	public void call(Subscriber<? super Uri> subscriber) {
		subscriber.onStart();
		Bitmap bitmap;
		try {
			bitmap = Glide.with(mContext)
					.load(mFile)
					.asBitmap()
					.centerCrop()
					.into(512, 512)
					.get();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
			byte[] data = baos.toByteArray();


			FirebaseStorage.getInstance()
					.getReferenceFromUrl("gs://" + mContext.getString(R.string.firebase_storage_bucket))
					.child(mReference)
					.putBytes(data, new StorageMetadata.Builder()
							.setContentType("image/jpg")
							.build())
					.addOnFailureListener(subscriber::onError)
					.addOnSuccessListener(snapshot -> {
						subscriber.onNext(snapshot.getDownloadUrl());
						subscriber.onCompleted();
					});

		} catch (InterruptedException | ExecutionException e) {
			subscriber.onError(e);
		}
	}
}
