package io.romain.passport.utils.validators;

import android.text.TextUtils;

public class PasswordValidator {

    public static final String SPECIAL_CHARACTERS = "!@#$%^&*()~`-=_+[]{}|:\";',./<>?";
    public static final int MIN_PASSWORD_LENGTH = 4;
    public static final int MAX_PASSWORD_LENGTH = 64;

    public static boolean isAcceptablePassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return false;
        }

        password = password.trim();
        int len = password.length();
        if (len < MIN_PASSWORD_LENGTH || len > MAX_PASSWORD_LENGTH) {
            return false;
        }

        char[] aC = password.toCharArray();
        boolean hasUpper= false, hasLower = false, hasDigit = false, hasSpecial =false;
        for (char c : aC) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (SPECIAL_CHARACTERS.contains(String.valueOf(c))) {
                hasSpecial = true;
            } else {
                return false;
            }
        }

        return true; //hasDigit && hasLower && hasUpper && hasSpecial;
    }
}
