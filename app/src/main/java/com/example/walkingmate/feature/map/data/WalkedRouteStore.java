package com.example.walkingmate.feature.map.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.naver.maps.geometry.LatLng;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class WalkedRouteStore {
    private static final String PREF_NAME = "WalkedRoutes";
    private static final String ALL_ROUTES_KEY = "allRoutes";

    private final SharedPreferences sharedPreferences;
    private final Gson gson = new Gson();
    private final Type routeListType = new TypeToken<ArrayList<ArrayList<LatLng>>>() {}.getType();

    public WalkedRouteStore(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveRoute(ArrayList<LatLng> route) {
        ArrayList<ArrayList<LatLng>> allRoutes = loadRoutes();
        if (allRoutes == null) {
            allRoutes = new ArrayList<>();
        }

        allRoutes.add(new ArrayList<>(route));
        sharedPreferences.edit()
                .putString(ALL_ROUTES_KEY, gson.toJson(allRoutes))
                .apply();
    }

    public ArrayList<ArrayList<LatLng>> loadRoutes() {
        String json = sharedPreferences.getString(ALL_ROUTES_KEY, null);
        return gson.fromJson(json, routeListType);
    }
}
