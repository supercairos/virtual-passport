package io.romain.passport.utils.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator {

    private final Pattern mPattern;

    private static EmailValidator sSingleton = null;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public static EmailValidator getInstance() {
        if(sSingleton == null){
            sSingleton = new EmailValidator();
        }

        return sSingleton;
    }

    private EmailValidator() {
        mPattern = Pattern.compile(EMAIL_PATTERN);
    }

    /**
     * Validate hex with regular expression
     *
     * @param hex hex for validation
     * @return true valid hex, false invalid hex
     */
    public boolean validate(final String hex) {
        Matcher matcher = mPattern.matcher(hex);
        return matcher.matches();
    }
}
