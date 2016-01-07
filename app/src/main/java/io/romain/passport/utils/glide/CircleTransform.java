/*
 * Copyright 2015 Romain
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

package io.romain.passport.utils.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

/**
 * Created by http://stackoverflow.com/a/25806229/409481
 */
public class CircleTransform extends BitmapTransformation {

	private BitmapPool mBitmapPool;

	public CircleTransform(Context context) {
		this(Glide.get(context).getBitmapPool());
	}

	public CircleTransform(BitmapPool pool) {
		super(pool);
		this.mBitmapPool = pool;
	}

	@Override
	public Bitmap transform(BitmapPool pool, Bitmap source, int outWidth, int outHeight) {
		int size = Math.min(source.getWidth(), source.getHeight());

		int width = (source.getWidth() - size) / 2;
		int height = (source.getHeight() - size) / 2;

		Bitmap bitmap = pool.get(size, size, Bitmap.Config.ARGB_8888);
		if (bitmap == null) {
			bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		}

		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		BitmapShader shader =
				new BitmapShader(source, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
		if (width != 0 || height != 0) {
			// source isn't square, move viewport to center
			Matrix matrix = new Matrix();
			matrix.setTranslate(-width, -height);
			shader.setLocalMatrix(matrix);
		}
		paint.setShader(shader);
		paint.setAntiAlias(true);

		float r = size / 2f;
		canvas.drawCircle(r, r, r, paint);

		return bitmap;
	}

	@Override
	public String getId() {
		return getClass().getName();
	}
}
