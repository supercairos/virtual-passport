package io.romain.passport.model.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.romain.passport.model.City;

public class PassportDatabase extends SQLiteOpenHelper {

	// Version 2 : Use SQLBright
	// Version 3 : Add the favorite feature
	private static final int DATABASE_VERSION = 3;
	private static final String DATABASE_NAME = "passport.db";

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
		db.execSQL(City.CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

		if (oldVersion < 2) {
			db.execSQL("DROP TABLE IF EXISTS " + City.TABLE_NAME);
			onCreate(db);
			return;
		}

		if(oldVersion <= 2) {
			db.execSQL(City.ADD_FAVORITE_COLUMN);
		}
	}
}
