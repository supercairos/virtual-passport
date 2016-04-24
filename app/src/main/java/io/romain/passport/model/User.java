package io.romain.passport.model;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import io.romain.passport.logic.helpers.AccountHelper;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import rx.Observable;

// This is not a persistable model since it will be saved onto the account manager;
@AutoValue
public abstract class User implements Parcelable {
	// api endpoint
	private static final String MODULE = "/users";

	// endpoints
	private static final String POST_FILE = "/pictures/upload";
	private static final String POST_REGISTER = MODULE + "/register";
	private static final String GET_LOGIN = MODULE + "/login";
	private static final String PUT_GCM = MODULE + "/gcm/{gcm_token}";

	// Account manager
	public static final String KEY_NAME = "account_name";
	public static final String KEY_PROFILE_PICTURE = "account_profile_picture";
	public static final String KEY_SERVER_ID = "account_server_id";

	@Nullable
	@SerializedName("_id")
	public abstract String id();

	@SerializedName("name")
	public abstract String name();

	@SerializedName("email")
	public abstract String email();

	@Nullable
	@SerializedName("picture")
	public abstract Uri picture();

	@Nullable
	@SerializedName("token")
	public abstract String token();

	public static String getServerId(AccountManager manager) {
		return getServerId(AccountHelper.getAccount(manager), manager);
	}

	private static String getServerId(Account account, AccountManager manager) {
		if (account != null) {
			return manager.getUserData(account, KEY_SERVER_ID);
		} else {
			return null;
		}
	}

	public static String getDisplayName(AccountManager manager) {
		return getDisplayName(AccountHelper.getAccount(manager), manager);
	}

	private static String getDisplayName(Account account, AccountManager manager) {
		if (account != null) {
			return manager.getUserData(account, KEY_NAME);
		} else {
			return null;
		}
	}

	public static String getEmail(AccountManager manager) {
		return getEmail(AccountHelper.getAccount(manager), manager);
	}

	private static String getEmail(Account account, AccountManager manager) {
		if (account != null) {
			return account.name;
		} else {
			return null;
		}
	}

	public static Uri getPicture(AccountManager manager) {
		return getPicture(AccountHelper.getAccount(manager), manager);
	}

	private static Uri getPicture(Account account, AccountManager manager) {
		if (account != null) {
			String s = manager.getUserData(account, KEY_PROFILE_PICTURE);
			if (!TextUtils.isEmpty(s)) {
				return Uri.parse(s);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public static User load(AccountManager manager) {
		Account account = AccountHelper.getAccount(manager);
		if (account != null) {
			return new AutoValue_User(
					getServerId(account, manager),
					getDisplayName(account, manager),
					getEmail(account, manager),
					getPicture(account, manager),
					null
			);
		}

		return null;
	}

	public static User create(String name, String email, Uri picture) {
		return new AutoValue_User(null, name, email, picture, null);
	}

	public static TypeAdapter<User> fromJson(Gson gson) {
		return new AutoValue_User.GsonTypeAdapter(gson);
	}

	public interface UserService {

		@POST(POST_REGISTER)
		Observable<User> register(@Body User user);

		@GET(GET_LOGIN)
		Observable<User> login(@Header("Authorization") String basic);

		@GET(GET_LOGIN)
		Call<User> login(@Header("Authorization") String basic, boolean async);

		@Multipart
		@POST(POST_FILE)
		Observable<User> upload(@Header("Authorization") String auth, @Part("file\"; filename=\"image.png\"") RequestBody file);

		@PUT(PUT_GCM)
		Call<User> registerGcm(@Path("gcm_token") String token);
	}
}
