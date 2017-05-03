package com.enest.pc_68.pathfinder.Modules;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * Created by PC-68 on 5/3/2017.
 */


public class GeocoderHelper {
    Context context;
    Location mLocation;
    List<Address> addresses = null;
    String cityName = null;

    public GeocoderHelper(Context context, Location mLocation) throws IOException {
        this.mLocation = mLocation;
        this.context = context;
    }

    public String GeocoderHelper() {
        String address = "";
        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(context);
            try {
                addresses = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses.size() > 0) {
                cityName = addresses.get(0).getLocality();
            }
        } else {

        }

        return address;
    }

    public void getAddress() {
        Geocoder geocoder = new Geocoder(context);
        try {
            addresses = geocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses.size() > 0) {
            cityName = addresses.get(0).getLocality();
        }
    }


    //RequestQueue requestQueue=new RequestQueue()

    public String fetchAddressByVolley() {
        String googleMapUrl = "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + mLocation.getLatitude() + "," + mLocation.getLongitude() + "&sensor=false";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, googleMapUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


                JSONArray results = null;
                try {
                    results = (JSONArray) response.get("results");

                    for (int i = 0; i < results.length(); i++) {
                        // loop among all addresses within this result
                        JSONObject result = results.getJSONObject(i);
                        if (result.has("address_components")) {
                            JSONArray addressComponents = result.getJSONArray("address_components");
                            // loop among all address component to find a 'locality' or 'sublocality'
                            for (int j = 0; j < addressComponents.length(); j++) {
                                JSONObject addressComponent = addressComponents.getJSONObject(j);
                                if (result.has("types")) {
                                    JSONArray types = addressComponent.getJSONArray("types");

                                    // search for locality and sublocality
                                    String cityName = null;

                                    for (int k = 0; k < types.length(); k++) {
                                        if ("locality".equals(types.getString(k)) && cityName == null) {
                                            if (addressComponent.has("long_name")) {
                                                cityName = addressComponent.getString("long_name");
                                            } else if (addressComponent.has("short_name")) {
                                                cityName = addressComponent.getString("short_name");
                                            }
                                        }
                                        if ("sublocality".equals(types.getString(k))) {
                                            if (addressComponent.has("long_name")) {
                                                cityName = addressComponent.getString("long_name");
                                            } else if (addressComponent.has("short_name")) {
                                                cityName = addressComponent.getString("short_name");
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        if (cityName != null) {
            return cityName;
        }
        return cityName;
    }


}