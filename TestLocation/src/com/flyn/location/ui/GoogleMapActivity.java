package com.flyn.location.ui;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.flyn.location.R;
import com.flyn.location.service.GpsInfo;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMapActivity extends AbstractLocationMap
{
    private static final String MAP_FRAGMENT_TAG = "map";
    private GoogleMap           mMap;
    private SupportMapFragment  mMapFragment;
    private CameraPosition      position;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initMap()
    {
        // mMapFragment = (SupportMapFragment)
        // getSupportFragmentManager().findFragmentByTag(MAP_FRAGMENT_TAG);
        //
        // if (mMapFragment == null)
        // {
        // // To programmatically add the map, we first create a
        // // SupportMapFragment.
        //
        // GoogleMapOptions options = new GoogleMapOptions();
        // options.zOrderOnTop(false);
        //
        // mMapFragment = SupportMapFragment.newInstance(options);
        //
        // // Then we add it using a FragmentTransaction.
        // FragmentTransaction fragmentTransaction =
        // getSupportFragmentManager().beginTransaction();
        // fragmentTransaction.add(R.id.fl_contain, mMapFragment,
        // MAP_FRAGMENT_TAG);
        // fragmentTransaction.commit();
        // }
    }

    private void setUpMapIfNeeded()
    {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mMap == null)
        {
            // Try to obtain the map from the SupportMapFragment.
            mMap = mMapFragment.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null)
            {
                setUpMap();
            }
        }
    }

    private void setUpMap()
    {
        UiSettings setting = mMap.getUiSettings();
        setting.setZoomControlsEnabled(true);
        setting.setZoomGesturesEnabled(true);
        setting.setScrollGesturesEnabled(true);
        // 指南针启用
        setting.setCompassEnabled(false);
        setting.setRotateGesturesEnabled(true);
        setting.setTiltGesturesEnabled(true);

        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_mark));
        markerOptions.position(new LatLng(Double.valueOf(gpsInfo.getLatitude()), Double.valueOf(gpsInfo.getLongitude())));
        if (!TextUtils.isEmpty(gpsInfo.getAddress()))
        {
            markerOptions.title(gpsInfo.getAddress());
        }
        final Marker marker = mMap.addMarker(markerOptions);
        if (!TextUtils.isEmpty(gpsInfo.getAddress()))
        {
            new Handler().postDelayed(new Runnable()
            {

                @Override
                public void run()
                {
                    marker.showInfoWindow();
                }
            }, 1000);
        }
    }

    @Override
    protected void follow()
    {
        if (null == this.position)
            return;

        try
        {
            changeCamera(CameraUpdateFactory.newCameraPosition(position));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void setLocation(GpsInfo gpsInfo)
    {
        // We can't be guaranteed that the map is available because Google Play
        // services might
        // not be available.
        setUpMapIfNeeded();

        position = new CameraPosition.Builder()
                .target(new LatLng(Double.valueOf(gpsInfo.getLatitude()), Double.valueOf(gpsInfo.getLongitude()))).zoom(15.5f).bearing(0)
                .tilt(25).build();

        try
        {
            changeCamera(CameraUpdateFactory.newCameraPosition(position));
            setFollowVisible();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void changeCamera(CameraUpdate update)
    {
        if (null != mMap)
        {
            mMap.animateCamera(update);
        }
    }

}
