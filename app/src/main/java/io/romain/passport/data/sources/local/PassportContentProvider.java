package io.romain.passport.data.sources.local;

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
import android.text.TextUtils;

import java.util.ArrayList;

import io.romain.passport.data.City;
import io.romain.passport.data.Comment;
import io.romain.passport.data.Profile;
import io.romain.passport.utils.SelectionBuilder;

public class PassportContentProvider extends ContentProvider {

	private static final String AUTHORITY = "io.romain.passport";
	private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	public interface Profiles {
		Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Profile.TABLE_NAME).build();

		String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + Profile.TABLE_NAME;
		String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + Profile.TABLE_NAME;
	}

	public interface Cities {
		Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(City.TABLE_NAME).build();

		String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + City.TABLE_NAME;
		String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + City.TABLE_NAME;
	}

	public interface Comments {
		Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(Comment.TABLE_NAME).build();

		String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + Comment.TABLE_NAME;
		String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + AUTHORITY + "/" + Comment.TABLE_NAME;
	}

	private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int CITIES = 0;
	private static final int CITY_BY_ID = 1;

	private static final int PROFILES = 2;
	private static final int PROFILE_BY_ID = 3;

	private static final int COMMENTS = 4;
	private static final int COMMENT_BY_ID = 5;

	static {
		MATCHER.addURI(AUTHORITY, City.TABLE_NAME, CITIES);
		MATCHER.addURI(AUTHORITY, City.TABLE_NAME + "/#", CITY_BY_ID);

		MATCHER.addURI(AUTHORITY, Profile.TABLE_NAME, PROFILES);
		MATCHER.addURI(AUTHORITY, Profile.TABLE_NAME + "/#", PROFILE_BY_ID);

		MATCHER.addURI(AUTHORITY, Comment.TABLE_NAME, COMMENTS);
		MATCHER.addURI(AUTHORITY, Comment.TABLE_NAME + "/#", COMMENT_BY_ID);
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
			case PROFILES:
				return Profiles.CONTENT_TYPE;
			case PROFILE_BY_ID:
				return Profiles.CONTENT_ITEM_TYPE;
			case COMMENTS:
				return Comments.CONTENT_TYPE;
			case COMMENT_BY_ID:
				return Comments.CONTENT_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}


	@Override
	public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
		final SQLiteDatabase db = mDatabase.getWritableDatabase();

		db.beginTransaction();
		try {
			int returnCount = 0;
			for (ContentValues value : values) {
				long _id;
				switch (MATCHER.match(uri)) {
					case CITIES:
						_id = db.insert(City.TABLE_NAME, null, value);
						break;
					case PROFILES:
						_id = db.insert(Profile.TABLE_NAME, null, value);
						break;
					case COMMENTS:
						_id = db.insert(Profile.TABLE_NAME, null, value);
						break;
					default:
						throw new IllegalArgumentException("Unknown URI " + uri);
				}

				if (_id != -1) {
					returnCount++;
				}

			}

			db.setTransactionSuccessful();
			getContext().getContentResolver().notifyChange(uri, null);
			return returnCount;
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
	public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String order) {
		final SQLiteDatabase db = mDatabase.getReadableDatabase();
		final Cursor cursor;

		SelectionBuilder builder = new SelectionBuilder();
		switch (MATCHER.match(uri)) {
			case CITY_BY_ID:
			case CITIES:
				builder.table(City.TABLE_NAME);
				break;
			case PROFILE_BY_ID:
			case PROFILES:
				builder.table(Profile.TABLE_NAME);
				break;
			case COMMENT_BY_ID:
			case COMMENTS:
				builder.table(Comment.TABLE_NAME);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}

		switch (MATCHER.match(uri)) {
			case PROFILE_BY_ID:
			case COMMENT_BY_ID:
			case CITY_BY_ID:
				builder.where(BaseColumns._ID + " = ?", String.valueOf(getId(uri)));
				break;
		}


		if (!TextUtils.isEmpty(selection)) {
			builder.where(selection, selectionArgs);
		}

		cursor = builder.query(db, projection, order);

		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public Uri insert(@NonNull Uri uri, ContentValues values) {
		final SQLiteDatabase db = mDatabase.getWritableDatabase();
		final long id;

		switch (MATCHER.match(uri)) {
			case CITIES:
				id = db.insertOrThrow(City.TABLE_NAME, null, values);
				break;
			case PROFILES:
				id = db.insertOrThrow(Profile.TABLE_NAME, null, values);
				break;
			case COMMENTS:
				id = db.insertOrThrow(Comment.TABLE_NAME, null, values);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}


		getContext().getContentResolver().notifyChange(uri, null);
		return ContentUris.withAppendedId(uri, id);
	}

	@Override
	public int update(@NonNull Uri uri, ContentValues values, String where, String[] whereArgs) {
		final SQLiteDatabase db = mDatabase.getWritableDatabase();
		final int count;

		SelectionBuilder builder = new SelectionBuilder();
		switch (MATCHER.match(uri)) {
			case CITY_BY_ID:
			case CITIES:
				builder.table(City.TABLE_NAME);
				break;
			case PROFILE_BY_ID:
			case PROFILES:
				builder.table(Profile.TABLE_NAME);
				break;
			case COMMENT_BY_ID:
			case COMMENTS:
				builder.table(Comment.TABLE_NAME);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}

		switch (MATCHER.match(uri)) {
			case PROFILE_BY_ID:
			case COMMENT_BY_ID:
			case CITY_BY_ID:
				builder.where(BaseColumns._ID + " = ?", String.valueOf(getId(uri)));
				break;
		}

		if (!TextUtils.isEmpty(where)) {
			builder.where(where, whereArgs);
		}

		count = builder.update(db, values);

		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

		return count;
	}

	@Override
	public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
		final SQLiteDatabase db = mDatabase.getWritableDatabase();
		final int count;

		SelectionBuilder builder = new SelectionBuilder();
		switch (MATCHER.match(uri)) {
			case CITY_BY_ID:
			case CITIES:
				builder.table(City.TABLE_NAME);
				break;
			case PROFILE_BY_ID:
			case PROFILES:
				builder.table(Profile.TABLE_NAME);
				break;
			case COMMENT_BY_ID:
			case COMMENTS:
				builder.table(Comment.TABLE_NAME);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}

		switch (MATCHER.match(uri)) {
			case PROFILE_BY_ID:
			case COMMENT_BY_ID:
			case CITY_BY_ID:
				builder.where(BaseColumns._ID + " = ?", String.valueOf(getId(uri)));
				break;
		}

		if (!TextUtils.isEmpty(where)) {
			builder.where(where, whereArgs);
		}

		count = builder.delete(db);

		if (count > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

	private static long getId(Uri uri) {
		return Long.parseLong(uri.getLastPathSegment());
	}
}
