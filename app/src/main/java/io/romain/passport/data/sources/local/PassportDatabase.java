package io.romain.passport.data.sources.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.romain.passport.data.City;
import io.romain.passport.data.Comment;
import io.romain.passport.data.Profile;

public class PassportDatabase extends SQLiteOpenHelper {

	// Version 2 : Use SQLBright
	// Version 3 : Add the favorite feature
	// Version 4 : Add the Profile table
	// Version 5 : Add the Comment table
	private static final int DATABASE_VERSION = 5;
	private static final String DATABASE_NAME = "passport.db";

	private static final String V3_ADD_FAVORITE_COLLUMN = "ALTER TABLE cities ADD favorite INTEGER NOT NULL DEFAULT 0;";

	private static final String V4_CREATE_PROFILE_TABLE = ""
			+ "CREATE TABLE profiles (\r\n"
			+ "    _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\r\n"
			+ "    server_id TEXT,\r\n"
			+ "    name TEXT NOT NULL,\r\n"
			+ "    email TEXT NOT NULL,\r\n"
			+ "    picture TEXT\r\n"
			+ ")";

	private static final String V5_CREATE_COMMENT_TABLE = "CREATE TABLE comment (\r\n"
			+ "    _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\r\n"
			+ "    server_id TEXT NOT NULL,\r\n"
			+ "    profile_id TEXT NOT NULL,\r\n"
			+ "    city_id TEXT NOT NULL,\r\n"
			+ "    text TEXT NOT NULL,\r\n"
			+ "    longitude REAL NOT NULL,\r\n"
			+ "    latitude REAL NOT NULL\r\n"
			+ ")";

	private static volatile PassportDatabase instance;

	private PassportDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static PassportDatabase getInstance(Context context) {
		if (instance == null) {
			synchronized (PassportDatabase.class) {
				if (instance == null) {
					instance = new PassportDatabase(context);
				}
			}
		}

		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(Profile.CREATE_TABLE);
		db.execSQL(City.CREATE_TABLE);
		db.execSQL(Comment.CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		if (oldVersion < 2) {
			reset(db);
			onCreate(db);
			return;
		}

		if (oldVersion <= 2) {
			db.execSQL(V3_ADD_FAVORITE_COLLUMN);
		}

		if (oldVersion <= 3) {
			db.execSQL(V4_CREATE_PROFILE_TABLE);
		}

		if (oldVersion <= 4) {
			db.execSQL(V5_CREATE_COMMENT_TABLE);
		}
	}

	private void reset(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + Profile.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Comment.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + City.TABLE_NAME);
	}
}
