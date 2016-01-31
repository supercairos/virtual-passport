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
package io.romain.passport.ui.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

public class TouchDrawableEditText extends EditText {

//	public interface OnDrawableTouched {
//		void onDrawableTouched(EditText text);
//	}
//
//	private OnDrawableTouched mListener;
//
//	public void setDrawableListener(OnDrawableTouched mListener) {
//		this.mListener = mListener;
//	}
//
//	public OnDrawableTouched getListener() {
//		return mListener;
//	}

	public TouchDrawableEditText(Context context) {
		super(context);
	}

	public TouchDrawableEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TouchDrawableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (isDrawableTouched(event)) {
					int start = getSelectionStart();
					int stop = getSelectionEnd();
					setTransformationMethod(HideReturnsTransformationMethod.getInstance());
					setSelection(start, stop);
//					if (mListener != null) {
//						mListener.onDrawableTouched(this);
//					}
					return true;
				}
			case MotionEvent.ACTION_UP:
				if (isDrawableTouched(event)) {
					int start = getSelectionStart();
					int stop = getSelectionEnd();
					setTransformationMethod(PasswordTransformationMethod.getInstance());
					setSelection(start, stop);
//					if (mListener != null) {
//						mListener.onDrawableTouched(this);
//					}
					return true;
				}
		}

		return super.onTouchEvent(event);
	}

	private boolean isDrawableTouched(MotionEvent event) {
		final Drawable[] drawables = getCompoundDrawables();
		final int DRAWABLE_LEFT = 0;
		final int DRAWABLE_TOP = 1;
		final int DRAWABLE_RIGHT = 2;
		final int DRAWABLE_BOTTOM = 3;

		if (drawables[DRAWABLE_RIGHT] != null && (event.getX() >= (getRight() - drawables[DRAWABLE_RIGHT].getBounds().width()))) {
			return true;
		} else if (drawables[DRAWABLE_LEFT] != null && (event.getX() <= drawables[DRAWABLE_LEFT].getBounds().width())) {
			return true;
		} else if (drawables[DRAWABLE_TOP] != null && (event.getY() <= drawables[DRAWABLE_TOP].getBounds().height())) {
			return true;
		} else if (drawables[DRAWABLE_BOTTOM] != null && (event.getY() >= (getBottom() - drawables[DRAWABLE_BOTTOM].getBounds().height()))) {
			return true;
		}

		return false;
	}
}
