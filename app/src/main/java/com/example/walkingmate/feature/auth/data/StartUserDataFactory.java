package com.example.walkingmate.feature.auth.data;

import android.text.TextUtils;

import com.example.walkingmate.feature.user.data.UserData;
import com.google.firebase.firestore.DocumentSnapshot;

public final class StartUserDataFactory {
    private StartUserDataFactory() {
    }

    public static UserData createFromDocument(
            DocumentSnapshot document,
            String fallbackNickname,
            String fallbackName,
            String fallbackAge,
            String fallbackGender,
            String fallbackBirthyear
    ) {
        String userId = document.getId();
        String nickname = firstNonBlank(document.getString("nickname"), fallbackNickname);
        String name = firstNonBlank(document.getString("name"), fallbackName);
        String age = firstNonBlank(document.getString("age"), fallbackAge);
        String gender = firstNonBlank(document.getString("gender"), fallbackGender);
        String birthyear = firstNonBlank(document.getString("birthyear"), fallbackBirthyear);
        String profileImagebig = safeTrim(document.getString("profileImagebig"));
        String profileImagesmall = safeTrim(document.getString("profileImagesmall"));
        String appname = resolveAppName(userId, document.getString("appname"), nickname, name);
        String title = firstNonBlank(document.getString("title"), "없음");
        Double reliabilityValue = document.getDouble("reliability");
        double reliability = reliabilityValue != null ? reliabilityValue : 0.0;

        return new UserData(
                userId,
                profileImagebig,
                profileImagesmall,
                appname,
                nickname,
                name,
                age,
                gender,
                birthyear,
                title,
                reliability
        );
    }

    public static String resolveAppName(String userId, String appname, String nickname, String name) {
        String normalizedAppName = safeTrim(appname);
        if (!TextUtils.isEmpty(normalizedAppName)) {
            return normalizedAppName;
        }

        String normalizedNickname = safeTrim(nickname);
        if (!TextUtils.isEmpty(normalizedNickname)) {
            return normalizedNickname;
        }

        String normalizedName = safeTrim(name);
        if (!TextUtils.isEmpty(normalizedName)) {
            return normalizedName;
        }

        String normalizedUserId = safeTrim(userId);
        if (!TextUtils.isEmpty(normalizedUserId)) {
            return normalizedUserId;
        }

        return "워킹메이트";
    }

    private static String firstNonBlank(String primary, String fallback) {
        String normalizedPrimary = safeTrim(primary);
        if (!TextUtils.isEmpty(normalizedPrimary)) {
            return normalizedPrimary;
        }
        return safeTrim(fallback);
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
