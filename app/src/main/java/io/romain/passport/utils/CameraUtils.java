/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.romain.passport.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Camera related utilities.
 */
@SuppressWarnings("deprecation")
public final class CameraUtils {

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	/**
	 * @return the default rear/back facing camera on the device. Returns null if camera is not
	 * available.
	 */
	public static int getDefaultBackFacingCameraId() {
		return getDefaultCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
	}

	/**
	 * @return the default front facing camera on the device. Returns null if camera is not
	 * available.
	 */
	public static int getDefaultFrontFacingCameraId() {
		return getDefaultCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
	}

	/**
	 * @param position Physical position of the camera i.e Camera.CameraInfo.CAMERA_FACING_FRONT
	 *                 or Camera.CameraInfo.CAMERA_FACING_BACK.
	 * @return the default camera on the device. Returns null if camera is not available.
	 */
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static int getDefaultCamera(int position) {
		// Find the total number of cameras available
		int mNumberOfCameras = Camera.getNumberOfCameras();

		// Find the ID of the back-facing ("default") camera
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
		for (int i = 0; i < mNumberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == position) {
				return i;

			}
		}

		return 0;
	}


	/**
	 * A safe way to get an instance of the Camera object.
	 */
	public static Camera getCameraInstance(int id) {
		Camera c = null;
		try {
			c = Camera.open(id); // attempt to get a Camera instance
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			Dog.v(e, "Error");
		}

		return c; // returns null if camera is unavailable
	}

	/**
	 * Iterate over supported camera preview sizes to see which one best fits the
	 * dimensions of the given view while maintaining the aspect ratio. If none can,
	 * be lenient with the aspect ratio.
	 *
	 * @param sizes Camera sizes.
	 * @param w     The width of the view.
	 * @param h     The height of the view.
	 * @return Best match camera preview size to fit in the view.
	 */
	public static Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = w > h ? (double) w / h : (double) h / w;

		if (sizes == null) return null;

		Camera.Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		// Try to find an size match aspect ratio and size
		for (Camera.Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
			if (Math.abs(size.height - h) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - h);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Camera.Size size : sizes) {
				if (Math.abs(size.height - h) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - h);
				}
			}
		}

		return optimalSize;
	}

	public static Camera.Size getBestPictureSize(List<Camera.Size> sizes) {
		return Collections.max(sizes, new CompareSizesByArea());
	}

	/**
	 * Creates a media file in the {@code Environment.DIRECTORY_PICTURES} directory. The directory
	 * is persistent and available to other applications like gallery.
	 *
	 * @param type Media type. Can be video or image.
	 * @return A file object pointing to the newly created file.
	 */
	public static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.
		if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
			return null;
		}

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "WeProov");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Dog.d("failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"IMG_" + timeStamp + ".jpg");
		} else {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"VID_" + timeStamp + ".mp4");
		}

		return mediaFile;
	}

	/**
	 * Check if this device has a camera
	 */
	public static boolean checkCameraHardware(Context context) {
		return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	public static void sendMediaScannerBroadcast(Context context, File file) {
		MediaScannerConnection.scanFile(context.getApplicationContext(), new String[]{file.toString()}, null, new MyMediaScannerListener());
	}


	/**
	 * Compares two {@code Size}s based on their areas.
	 */
	private static class CompareSizesByArea implements Comparator<Camera.Size> {

		@Override
		public int compare(Camera.Size lhs, Camera.Size rhs) {
			// We cast here to ensure the multiplications won't overflow
			return Long.signum((long) lhs.height - (long) rhs.height);
		}

	}

	private static class MyMediaScannerListener implements MediaScannerConnection.OnScanCompletedListener {

		/**
		 * Called to notify the client when the media scanner has finished
		 * scanning a file.
		 *
		 * @param path the path to the file that has been scanned.
		 * @param uri  the Uri for the file if the scanning operation succeeded
		 */
		@Override
		public void onScanCompleted(String path, Uri uri) {
			Dog.i("Scanned: %s", path);
			Dog.i("-> uri=%s", uri);
		}
	}

	public static boolean isSupported(String value, List<String> supported) {
		return supported != null && supported.indexOf(value) >= 0;
	}
}
