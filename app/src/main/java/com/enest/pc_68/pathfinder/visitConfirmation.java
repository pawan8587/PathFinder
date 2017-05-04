package com.enest.pc_68.pathfinder;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.enest.pc_68.pathfinder.Modules.DirectionFinder;
import com.enest.pc_68.pathfinder.Modules.DirectionFinderListener;
import com.enest.pc_68.pathfinder.Modules.Route;
import com.enest.pc_68.pathfinder.helper.CircleImageView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.enest.pc_68.pathfinder.R.id.map;

public class visitConfirmation extends FragmentActivity implements OnMapReadyCallback, DirectionFinderListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    static String source = null;
    static String destination = null;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private LatLng mCenterLatLong;
    private GoogleMap googleMap;
    private CoordinatorLayout layoutRequestPickupDialog;
    private BottomSheetBehavior mBottomSheetBehavior;
    private BottomSheetDialog bottomSheetDialog;
    private CircleImageView circleImageViewUberGo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visit_confirmation);
        android.support.v7.widget.Toolbar toolbar= (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);


        layoutRequestPickupDialog= (CoordinatorLayout) findViewById(R.id.layoutRequestPickupDialog);

        circleImageViewUberGo= (CircleImageView) findViewById(R.id.circleImageViewUberGo);

        if (source == null && source.length() < 0) {
            Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
            return;
        } else if (destination == null && destination.length() < 0) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        } else {

            try {
                new DirectionFinder((DirectionFinderListener) this, source, destination).execute();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }


        circleImageViewUberGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(),"Okkk",Toast.LENGTH_LONG).show();
                if (dismissDialog()) {
                    return;
                }
                View view = getLayoutInflater().inflate(R.layout.layout_far_description_sheet, null);
                bottomSheetDialog = new BottomSheetDialog(visitConfirmation.this);
                bottomSheetDialog.getWindow().setLayout(CoordinatorLayout.LayoutParams.FILL_PARENT, CoordinatorLayout.LayoutParams.FILL_PARENT);
                bottomSheetDialog.setContentView(view);
                bottomSheetDialog.show();
            }
        });
        //initBottomSheet();
    }


    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Finding direction..!", true);

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();




        for (Route route : routes) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 16));
            //((TextView) findViewById(R.id.tvDuration)).setText(route.duration.text);
            //((TextView) findViewById(R.id.tvDistance)).setText(route.distance.text);
            Log.i("Geo origin Loaction", route.startAddress + route.startLocation);
            Log.i("Geo destination Loaction", route.endAddress + route.endLocation);


            //*************************Custom Source Marker Start***********************

            View sourceMarker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_custom_source_marker, null);
            TextView duration = (TextView) sourceMarker.findViewById(R.id.textViewSourceDuration);
            duration.setText(route.duration.text+" Min");


            TextView sourceLocationName = (TextView) sourceMarker.findViewById(R.id.textViewSourceLocationName);
            sourceLocationName.setText(route.startAddress);

            originMarkers.add(googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this,sourceMarker))).flat(true).anchor(0.5f,0.5f)
                    .position(route.startLocation)));

            //*************************Custom Source Marker End***********************

            //*************************Custom Destination Marker Start***********************

            View destinationMarker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_custom_destination_marker, null);
            TextView destinationLocationName = (TextView) destinationMarker.findViewById(R.id.textViewDestinationLocationName);
            destinationLocationName.setText(route.endAddress);

            destinationMarkers.add(googleMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this,destinationMarker))).flat(true).anchor(0.5f,0.5f)
                    .position(route.endLocation)));

            //*************************Custom Destination Marker Start***********************




            PolylineOptions polylineOptions = new PolylineOptions().geodesic(true).color(Color.RED).width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(googleMap.addPolyline(polylineOptions));

            LatLng sourceLatlang=new LatLng(route.startLocation.latitude,route.startLocation.longitude);
            LatLng destinationLatlang=new LatLng(route.endLocation.latitude,route.endLocation.longitude);

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(sourceLatlang);
            builder.include(destinationLatlang);
            LatLngBounds bounds = builder.build();
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20));

            //googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(route.endLocation, 15));
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);

    }


    // Convert a view to bitmap
    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setLayoutParams(new Toolbar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));
        }
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }


    private void initBottomSheet() {
        View bottomSheet =getLayoutInflater().inflate(R.layout.layout_far_description_sheet, null);
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                else {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // React to dragging events
                if(mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                else {
                    mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });
    }


    private boolean dismissDialog() {
        if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
            bottomSheetDialog.dismiss();
            return true;
        }



        return false;
    }

}

