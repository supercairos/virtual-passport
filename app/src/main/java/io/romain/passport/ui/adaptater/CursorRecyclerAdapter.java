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
/*
 * Copyright (C) 2006 The Android Open Source Project
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

package io.romain.passport.ui.adaptater;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Adapter that exposes data from a {@link android.database.Cursor Cursor} to a
 * {@link android.widget.ListView ListView} widget.
 * <p>
 * The Cursor must include a column named "_id" or this class will not work.
 * Additionally, using {@link android.database.MergeCursor} with this class will
 * not work if the merged Cursors have overlapping values in their "_id"
 * columns.
 */
public abstract class CursorRecyclerAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

	private boolean mDataValid;
	private Cursor mCursor;

	private Context mContext;

	Context getContext() {
		return mContext;
	}

	/**
	 * This field should be made private, so it is hidden from the SDK.
	 */
	private int mRowIDColumn;
	/**
	 * This field should be made private, so it is hidden from the SDK.
	 */
	private ChangeObserver mChangeObserver;
	/**
	 * This field should be made private, so it is hidden from the SDK.
	 */
	private DataSetObserver mDataSetObserver;

	/**
	 * If set the adapter will register a content observer on the cursor and will call
	 * {@link #onContentChanged()} when a notification comes in.  Be careful when
	 * using this flag: you will need to unset the current Cursor from the adapter
	 * to avoid leaks due to its registered observers.  This flag is not needed
	 * when using a CursorAdapter with a
	 * {@link android.content.CursorLoader}.
	 */
	private static final int FLAG_REGISTER_CONTENT_OBSERVER = 0x02;

	/**
	 * Recommended Constructor
	 *
	 * @param c       The cursor from which to get the data.
	 * @param context The context
	 */
	CursorRecyclerAdapter(Context context, Cursor c) {
		init(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
	}

	private void init(Context context, Cursor c, int flags) {
		boolean cursorPresent = c != null;
		mCursor = c;
		mDataValid = cursorPresent;
		mContext = context;
		mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;
		if ((flags & FLAG_REGISTER_CONTENT_OBSERVER) == FLAG_REGISTER_CONTENT_OBSERVER) {
			mChangeObserver = new ChangeObserver();
			mDataSetObserver = new MyDataSetObserver();
		} else {
			mChangeObserver = null;
			mDataSetObserver = null;
		}

		if (cursorPresent) {
			if (mChangeObserver != null) c.registerContentObserver(mChangeObserver);
			if (mDataSetObserver != null) c.registerDataSetObserver(mDataSetObserver);
		}
	}

	/**
	 * Returns the cursor.
	 *
	 * @return the cursor.
	 */
	public Cursor getCursor() {
		return mCursor;
	}

	/**
	 * @see android.widget.ListAdapter#getCount()
	 */
	public int getCount() {
		if (mDataValid && mCursor != null) {
			return mCursor.getCount();
		} else {
			return 0;
		}
	}

	/**
	 * @see android.widget.ListAdapter#getItem(int)
	 */
	public Object getItem(int position) {
		if (mDataValid && mCursor != null) {
			mCursor.moveToPosition(position);
			return mCursor;
		} else {
			return null;
		}
	}

	/**
	 * @see android.widget.ListAdapter#getItemId(int)
	 */
	public long getItemId(int position) {
		if (mDataValid && mCursor != null) {
			if (mCursor.moveToPosition(position)) {
				return mCursor.getLong(mRowIDColumn);
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	@Override
	public int getItemCount() {
		if (mCursor != null) {
			return mCursor.getCount();
		} else {
			return 0;
		}
	}

	@Override
	public void setHasStableIds(boolean hasStableIds) {
		super.setHasStableIds(true);
	}

	@Override
	public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

	@Override
	public final void onBindViewHolder(VH holder, int position) {
		if (!mDataValid) {
			throw new IllegalStateException("this should only be called when the cursor is valid");
		}
		if (!mCursor.moveToPosition(position)) {
			throw new IllegalStateException("couldn't move cursor to position " + position);
		}

		onBindViewHolder(holder, mCursor);
	}

	/**
	 * Bind an existing view to the data pointed to by cursor
	 *
	 * @param view   Existing view, returned earlier by newView
	 * @param cursor The cursor from which to get the data. The cursor is already
	 *               moved to the correct position.
	 */
	protected abstract void onBindViewHolder(VH view, Cursor cursor);

	/**
	 * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
	 * closed.
	 *
	 * @param cursor The new cursor to be used
	 */
	public void changeCursor(Cursor cursor) {
		Cursor old = swapCursor(cursor);
		if (old != null) {
			old.close();
		}
	}

	/**
	 * Swap in a new Cursor, returning the old Cursor.  Unlike
	 * {@link #changeCursor(Cursor)}, the returned old Cursor is <em>not</em>
	 * closed.
	 *
	 * @param newCursor The new cursor to be used.
	 * @return Returns the previously set Cursor, or null if there wasa not one.
	 * If the given new Cursor is the same instance is the previously set
	 * Cursor, null is also returned.
	 */
	public Cursor swapCursor(Cursor newCursor) {
		if (newCursor == mCursor) {
			return null;
		}
		Cursor oldCursor = mCursor;
		if (oldCursor != null) {
			if (mChangeObserver != null) oldCursor.unregisterContentObserver(mChangeObserver);
			if (mDataSetObserver != null) oldCursor.unregisterDataSetObserver(mDataSetObserver);
		}
		mCursor = newCursor;
		if (newCursor != null) {
			if (mChangeObserver != null) newCursor.registerContentObserver(mChangeObserver);
			if (mDataSetObserver != null) newCursor.registerDataSetObserver(mDataSetObserver);
			mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
			mDataValid = true;
			// notify the observers about the new cursor
			notifyDataSetChanged();
		} else {
			mRowIDColumn = -1;
			mDataValid = false;
			// notify the observers about the lack of a data set
			notifyDataSetChanged();
		}
		return oldCursor;
	}

	/**
	 * <p>Converts the cursor into a CharSequence. Subclasses should override this
	 * method to convert their results. The default implementation returns an
	 * empty String for null values or the default String representation of
	 * the value.</p>
	 *
	 * @param cursor the cursor to convert to a CharSequence
	 * @return a CharSequence representing the value
	 */
	public CharSequence convertToString(Cursor cursor) {
		return cursor == null ? "" : cursor.toString();
	}

	/**
	 * Called when the {@link ContentObserver} on the cursor receives a change notification.
	 * The default implementation provides the auto-requery logic, but may be overridden by
	 * sub classes.
	 *
	 * @see ContentObserver#onChange(boolean)
	 */
	private void onContentChanged() {
		// Overwrite
	}

	private class ChangeObserver extends ContentObserver {
		public ChangeObserver() {
			super(new Handler());
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			onContentChanged();
		}
	}

	private class MyDataSetObserver extends DataSetObserver {
		@Override
		public void onChanged() {
			mDataValid = true;
			notifyDataSetChanged();
		}

		@Override
		public void onInvalidated() {
			mDataValid = false;
			notifyDataSetChanged();
		}
	}

}

