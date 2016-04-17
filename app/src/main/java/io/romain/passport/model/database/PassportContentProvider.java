package io.romain.passport.model.database;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import io.romain.passport.model.City;
import io.romain.passport.utils.SelectionBuilder;

public class PassportContentProvider extends ContentProvider {

	public static final String AUTHORITY = "io.romain.passport";
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	public interface Cities {
		Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(City.TABLE_NAME).build();
		String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + City.TABLE_NAME;
		String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + City.TABLE_NAME;
	}

	private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int CITIES = 0;
	private static final int CITY_BY_ID = 1;

	static {
		MATCHER.addURI(AUTHORITY, City.TABLE_NAME, CITIES);
		MATCHER.addURI(AUTHORITY, City.TABLE_NAME + "/#", CITY_BY_ID);
	}

	private SQLiteOpenHelper mDatabase;

	@Override
	public boolean onCreate() {
		mDatabase = PassportDatabase.getInstance(getContext());

		return mDatabase != null;
	}

	@Override
	public String getType(@NonNull Uri uri) {
		switch (MATCHER.match(uri)) {
			case CITIES:
				return Cities.CONTENT_TYPE;
			case CITY_BY_ID:
				return Cities.CONTENT_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}


	@Override
	@SuppressWarnings("ConstantConditions")
	public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
		final SQLiteDatabase db = mDatabase.getWritableDatabase();

		db.beginTransaction();
		try {
			switch (MATCHER.match(uri)) {
				case CITIES:

					int returnCount = 0;
					for (ContentValues value : values) {
						long _id = db.insert(City.TABLE_NAME, null, value);
						if (_id != -1) {
							returnCount++;
						}
					}

					db.setTransactionSuccessful();
					getContext().getContentResolver().notifyChange(uri, null);
					return returnCount;
				default:
					return super.bulkInsert(uri, values);
			}
		} finally {
			db.endTransaction();
		}
	}

	@NonNull
	@Override
	public ContentProviderResult[] applyBatch(@NonNull ArrayList<ContentProviderOperation> ops) throws OperationApplicationException {
		ContentProviderResult[] results;
		final SQLiteDatabase db = mDatabase.getWritableDatabase();

		db.beginTransaction();
		try {
			results = super.applyBatch(ops);
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		return results;
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String order) {
		final SQLiteDatabase db = mDatabase.getReadableDatabase();
		final Cursor cursor;

		switch (MATCHER.match(uri)) {
			case CITIES:
				cursor = new SelectionBuilder()
						.table(City.TABLE_NAME)
						.where(selection, selectionArgs)
						.query(db, projection, order);

				break;
			case CITY_BY_ID:
				cursor = new SelectionBuilder()
						.table(City.TABLE_NAME)
						.where(BaseColumns._ID + " = ?", new String[]{String.valueOf(getId(uri))})
						.where(selection, selectionArgs)
						.query(db, projection, order);

				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}

		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public Uri insert(@NonNull Uri uri, ContentValues values) {
		final SQLiteDatabase db = mDatabase.getWritableDatabase();
		final long id;

		switch (MATCHER.match(uri)) {
			case CITIES:
				id = db.insertOrThrow(City.TABLE_NAME, null, values);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}


		getContext().getContentResolver().notifyChange(uri, null);
		return ContentUris.withAppendedId(uri, id);
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public int update(@NonNull Uri uri, ContentValues values, String where, String[] whereArgs) {
		final SQLiteDatabase db = mDatabase.getWritableDatabase();
		final int count;

		switch (MATCHER.match(uri)) {
			case CITIES:
				count = new SelectionBuilder()
						.where(where, whereArgs)
						.table(City.TABLE_NAME)
						.update(db, values);

				break;
			case CITY_BY_ID:
				count = new SelectionBuilder()
						.where(BaseColumns._ID + " = ?", new String[]{String.valueOf(getId(uri))})
						.where(where, whereArgs)
						.table(City.TABLE_NAME)
						.delete(db);

				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);

		}

		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return count;
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
		final SQLiteDatabase db = mDatabase.getWritableDatabase();
		final int count;

		switch (MATCHER.match(uri)) {
			case CITIES:
				count = new SelectionBuilder()
						.where(where, whereArgs)
						.table(City.TABLE_NAME)
						.delete(db);

				break;
			case CITY_BY_ID:
				count = new SelectionBuilder()
						.where(BaseColumns._ID + " = ?", new String[]{String.valueOf(getId(uri))})
						.where(where, whereArgs)
						.table(City.TABLE_NAME)
						.delete(db);

				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);

		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	public static long getId(Uri uri) {
		return Long.parseLong(uri.getLastPathSegment());
	}
}
