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
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

public class LocationAutocompleteTextView extends AutoCompleteTextView {

	public interface OnDrawableTouched {
		boolean onDrawableTouched(EditText text);
	}

	private OnDrawableTouched mListener;

	public void setListener(OnDrawableTouched mListener) {
		this.mListener = mListener;
	}

	public OnDrawableTouched getListener() {
		return mListener;
	}

	public LocationAutocompleteTextView(Context context) {
		super(context);
		init();
	}

	public LocationAutocompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LocationAutocompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private GestureDetector mGestureDetector;
	private final GestureDetector.SimpleOnGestureListener mGestureDetectorListener = new GestureDetector.SimpleOnGestureListener() {
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			if (mListener != null) {
				if (isDrawableTouched(e)) {
					return mListener.onDrawableTouched(LocationAutocompleteTextView.this);
				}
			}
			return false;
		}
	};

	private void init() {
		mGestureDetector = new GestureDetector(getContext(), mGestureDetectorListener);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mGestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
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
