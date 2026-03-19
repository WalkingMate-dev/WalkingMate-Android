package com.example.walkingmate.feature.auth.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.example.walkingmate.BuildConfig;
import com.example.walkingmate.R;
import com.example.walkingmate.core.security.PasswordSecurity;
import com.example.walkingmate.feature.auth.data.NaverUserClient;
import com.example.walkingmate.feature.auth.data.StartUserDataFactory;
import com.example.walkingmate.feature.auth.model.NaverUserModel;
import com.example.walkingmate.feature.feed.ui.WalkingHomeActivity;
import com.example.walkingmate.feature.user.data.UserData;
import com.example.walkingmate.feature.user.ui.SettingProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StartActivity extends AppCompatActivity {

    TextView StartText, permlist;
    boolean checkback = false;
    Button permissionbtn, loginbtn, signupbtn;
    EditText username, password;
    String reqperm;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String TAG = "NaverLoginActivity";
    // [보안 리팩토링] OAuth 키는 local.properties -> BuildConfig로 주입
    private static final String OAUTH_CLIENT_ID = BuildConfig.NAVER_OAUTH_CLIENT_ID;
    private static final String OAUTH_CLIENT_SECRET = BuildConfig.NAVER_OAUTH_CLIENT_SECRET;
    private static final String OAUTH_CLIENT_NAME = BuildConfig.NAVER_OAUTH_CLIENT_NAME;
    private static OAuthLogin mOAuthLoginInstance;
    private static Context mContext;
    private NaverUserModel model;
    private final NaverUserClient naverUserClient = new NaverUserClient();
    private OAuthLoginButton mOAuthLoginButton;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String PREF_USERID = "UserId";
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        StartText = findViewById(R.id.StartText);
        permlist = findViewById(R.id.permlist);
        permissionbtn = findViewById(R.id.permission);
        loginbtn = findViewById(R.id.login);
        signupbtn = findViewById(R.id.signup);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        mOAuthLoginButton = findViewById(R.id.buttonOAuthLoginImg);
        YoYo.with(Techniques.FadeIn).duration(1000).repeat(0).playOn(StartText);
        mContext = this;

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // [자동로그인 리팩토링] 권한 상태와 무관하게 저장된 세션이 있으면 먼저 진입
        String savedUserId = sharedPreferences.getString(PREF_USERID, null);
        if (TextUtils.isEmpty(savedUserId)) {
            UserData localUser = UserData.loadData(this);
            if (localUser != null && !TextUtils.isEmpty(localUser.userid)) {
                savedUserId = localUser.userid;
            }
        }
        if (!TextUtils.isEmpty(savedUserId)) {
            checkLoginStatus();
            return;
        }

        // 권한 요청
        String[] Permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACTIVITY_RECOGNITION
        };
        ActivityCompat.requestPermissions(this, Permissions, 1);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {

            mOAuthLoginButton.setVisibility(View.INVISIBLE);
            loginbtn.setVisibility(View.INVISIBLE);
            signupbtn.setVisibility(View.INVISIBLE);
            username.setVisibility(View.INVISIBLE);
            password.setVisibility(View.INVISIBLE);

            permissionbtn.setVisibility(View.VISIBLE);
            permlist.setVisibility(View.VISIBLE);
            checkback = true;
        } else {
            checkLoginStatus();
        }

        reqperm = "";
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            reqperm += "위치권한: 항상 허용으로 설정\n";
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            reqperm += "신체활동 권한: 허용으로 설정\n";
        }
        permlist.setText(reqperm);

        permissionbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent recogIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:com.example.walkingmate"));
                startActivity(recogIntent);
            }
        });



        // 로그인 버튼 클릭 이벤트
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputUsername = username.getText().toString();
                String inputPassword = password.getText().toString();

                if (inputUsername.isEmpty() || inputPassword.isEmpty()) {
                    Toast.makeText(StartActivity.this, "아이디와 비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 사용자 인증 처리
                db.collection("users").document(inputUsername).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String storedPassword = document.getString("password");
                                // [보안 리팩토링] 해시/평문(레거시) 모두 검증하고, 평문 계정은 로그인 시 해시로 마이그레이션.
                                boolean isPasswordValid = PasswordSecurity.verifyPassword(inputPassword, storedPassword);
                                if (isPasswordValid) {
                                    if (!PasswordSecurity.isHashedPassword(storedPassword)) {
                                        db.collection("users").document(inputUsername)
                                                .update("password", PasswordSecurity.hashPassword(inputPassword));
                                    }
                                    saveLoginSession(inputUsername, "default");

                                    UserData userData = StartUserDataFactory.createFromDocument(
                                            document,
                                            "",
                                            "",
                                            "",
                                            "",
                                            ""
                                    );
                                    String rawAppname = document.getString("appname");
                                    if (TextUtils.isEmpty(safeTrim(rawAppname))) {
                                        db.collection("users").document(inputUsername).update("appname", userData.appname);
                                    }

                                    UserData.saveData(userData, StartActivity.this);

                                    if (!TextUtils.isEmpty(userData.profileImagebig)) {
                                        Intent intent = new Intent(StartActivity.this, WalkingHomeActivity.class);
                                        intent.putExtra("userid", inputUsername);
                                        intent.putExtra("appname", userData.appname);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Intent setprofile = new Intent(StartActivity.this, SettingProfileActivity.class);
                                        setprofile.putExtra("nickname", userData.nickname);
                                        setprofile.putExtra("name", userData.name);
                                        setprofile.putExtra("birthyear", userData.birthyear);
                                        setprofile.putExtra("userid", inputUsername);
                                        setprofile.putExtra("age", userData.age);
                                        setprofile.putExtra("gender", userData.gender);
                                        setprofile.putExtra("password", inputPassword);
                                        startActivity(setprofile);
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(StartActivity.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(StartActivity.this, "아이디가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(StartActivity.this, "로그인 실패. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });




        // 회원가입 버튼 클릭 이벤트
        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mOAuthLoginButton.setVisibility(View.INVISIBLE);
            loginbtn.setVisibility(View.INVISIBLE);
            signupbtn.setVisibility(View.INVISIBLE);
            username.setVisibility(View.INVISIBLE);
            password.setVisibility(View.INVISIBLE);
            permissionbtn.setVisibility(View.VISIBLE);
            permlist.setVisibility(View.VISIBLE);
        } else {
            if (checkback) {
                mOAuthLoginButton.setVisibility(View.VISIBLE);
                loginbtn.setVisibility(View.VISIBLE);
                signupbtn.setVisibility(View.VISIBLE);
                username.setVisibility(View.VISIBLE);
                password.setVisibility(View.VISIBLE);
            }

            permissionbtn.setVisibility(View.GONE);
            permlist.setVisibility(View.GONE);
        }
        reqperm = "";
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            reqperm += "위치권한: 항상 허용으로 설정\n";
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            reqperm += "신체활동 권한: 허용으로 설정";
        }
        permlist.setText(reqperm);
    }

    private void initData() {
        if (TextUtils.isEmpty(OAUTH_CLIENT_ID) || TextUtils.isEmpty(OAUTH_CLIENT_SECRET) || TextUtils.isEmpty(OAUTH_CLIENT_NAME)) {
            Log.w(TAG, "Naver OAuth config is missing. Check local.properties secrets.");
            mOAuthLoginButton.setVisibility(View.GONE);
            return;
        }
        mOAuthLoginInstance = OAuthLogin.getInstance();
        mOAuthLoginInstance.init(mContext, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME);

        mOAuthLoginButton = findViewById(R.id.buttonOAuthLoginImg);
        mOAuthLoginButton.setOAuthLoginHandler(mOAuthLoginHandler);
    }

    private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if (success) {
                String accessToken = mOAuthLoginInstance.getAccessToken(mContext);
                getUser(accessToken);
            } else {
                String errorCode = mOAuthLoginInstance.getLastErrorCode(mContext).getCode();
                String errorDesc = mOAuthLoginInstance.getLastErrorDesc(mContext);
                Toast.makeText(mContext, "errorCode:" + errorCode + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void getUser(String token) {
        ioExecutor.execute(() -> {
            try {
                NaverUserModel naverUser = naverUserClient.fetchUser(token);
                mainHandler.post(() -> handleNaverUserResponse(naverUser));
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to fetch Naver user", e);
                mainHandler.post(() ->
                        Toast.makeText(StartActivity.this, "네이버 로그인 요청에 실패했습니다.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void handleNaverUserResponse(NaverUserModel userModel) {
        model = userModel;
        if (model == null) {
            Toast.makeText(this, "네이버 사용자 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(model.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()) {
                    saveLoginSession(model.getId(), "naver");

                    permlist.setVisibility(View.VISIBLE);
                    permlist.setText("접속중...");
                    DocumentSnapshot document = task.getResult();
                    String savedAppname = document.getString("appname");
                    UserData userDatatmp = StartUserDataFactory.createFromDocument(
                            document,
                            model.getNickname(),
                            model.getName(),
                            model.getAge(),
                            model.getGender(),
                            model.getBirthyear()
                    );
                    UserData.saveData(userDatatmp, StartActivity.this);
                    if (TextUtils.isEmpty(safeTrim(savedAppname))) {
                        db.collection("users").document(model.getId()).update("appname", userDatatmp.appname);
                    }

                    ioExecutor.execute(() -> {
                        UserData.saveBitmapToJpeg(
                                UserData.GetBitmapfromURL(document.getString("profileImagebig")),
                                UserData.GetBitmapfromURL(document.getString("profileImagesmall")),
                                StartActivity.this
                        );
                        db.collection("challenge").document(model.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (!task.getResult().exists()) {
                                    Map<String, Object> cha = new HashMap<>();
                                    cha.put("step", 0);
                                    cha.put("feedseq", 0);
                                    cha.put("meet", 0);
                                    db.collection("challenge").document(model.getId()).set(cha);
                                }
                            }
                        });
                    });

                    db.collection("users").document(userDatatmp.userid).set(UserData.getHashmap(userDatatmp)).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            startActivity(new Intent(StartActivity.this, WalkingHomeActivity.class));
                            finish();
                        }
                    });

                } else {
                    Intent setprofile = new Intent(StartActivity.this, SettingProfileActivity.class);
                    setprofile.putExtra("nickname", model.getNickname());
                    setprofile.putExtra("name", model.getName());
                    setprofile.putExtra("birthyear", model.getBirthyear());
                    setprofile.putExtra("userid", model.getId());
                    setprofile.putExtra("age", model.getAge());
                    setprofile.putExtra("gender", model.getGender());

                    saveLoginSession(model.getId(), "naver");

                    startActivity(setprofile);
                    finish();
                }
            }
        });
    }

    private void checkLoginStatus() {
        String userId = sharedPreferences.getString(PREF_USERID, null);

        // [보안 리팩토링] SharedPreferences 누락 시 로컬 UserData를 fallback으로 사용
        if (TextUtils.isEmpty(userId)) {
            UserData localUser = UserData.loadData(this);
            if (localUser != null && !TextUtils.isEmpty(localUser.userid)) {
                userId = localUser.userid;
            }
        }

        if (!TextUtils.isEmpty(userId)) {
            // [자동로그인 리팩토링] 로컬 세션(UserId) 존재 시 즉시 진입하고, 서버 검증 실패로 로그인 화면 회귀하지 않음.
            validateSessionInBackground(userId);
            Intent intent = new Intent(StartActivity.this, WalkingHomeActivity.class);
            startActivity(intent);
            finish();
            return;
        } else {
            showLoginScreen();
        }
    }

    // [자동로그인 리팩토링] 필요 시 백그라운드 서버 검증 확장 포인트(현재는 로그인 UX 안정성 우선)
    private void validateSessionInBackground(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        db.collection("users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!(task.isSuccessful() && task.getResult().exists())) {
                    // 세션이 실제로 무효하면 다음 실행에서 로그인 화면이 뜨도록 정리
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove(PREF_USERID);
                    editor.remove("LoginType");
                    editor.putBoolean("ForceLoggedOut", true);
                    editor.apply();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Session validation skipped due network issue");
            }
        });
    }


    private void showLoginScreen() {
        mOAuthLoginButton.setVisibility(View.VISIBLE);
        loginbtn.setVisibility(View.VISIBLE);
        signupbtn.setVisibility(View.VISIBLE);
        username.setVisibility(View.VISIBLE);
        password.setVisibility(View.VISIBLE);
        permissionbtn.setVisibility(View.GONE);
        permlist.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdownNow();
    }

    private void saveLoginSession(String userId, String loginType) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PREF_USERID, userId);
        editor.putString("LoginType", loginType);
        editor.putBoolean("ForceLoggedOut", false);
        editor.apply();
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
