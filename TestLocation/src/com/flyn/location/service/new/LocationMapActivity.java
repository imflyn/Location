package com.baital.android.project.readKids.service.location;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.baital.android.project.R;
import com.baital.android.project.readKids.bll.GpsInfo;
import com.baital.android.project.readKids.service.PreferencesManager;
import com.baital.android.project.readKids.service.location.LocationConfiguration.MapType;
import com.baital.android.project.readKids.utils.ImageUtil;
import com.baital.android.project.readKids.view.MyLocationMapView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class LocationMapActivity extends AbstractLocationMap
{

    // 百度
    private LocationData      baiduPoint;
    private LocationOverlay   baiduLocationOverlay;
    private PopupOverlay      baiduPop;
    private MyLocationMapView baiduMapView;
    private MapController     baiduMapController;
    private BMapManager       bMapManager;

    // google
    private GoogleMap         googleMap;
    private MapView           googleMapView;
    private CameraPosition    googlePoint;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initMap()
    {
        createBaiduMap();
        createGoogleMap();

        if (MapType.Baidu == mapType)
        {
            initBaiduMap();
        } else
        {
            initGoogleMap();
        }
    }

    private void createBaiduMap()
    {
        if (null == bMapManager)
        {
            bMapManager = new BMapManager(getApplicationContext());
            bMapManager.init(null);

            baiduMapView = new MyLocationMapView(this);
        }
    }

    private void createGoogleMap()
    {
        if (null == googleMapView)
        {
            MapsInitializer.initialize(this);

            googleMapView = new MapView(this);
            googleMapView.onCreate(null);
        }

    }

    private void initBaiduMap()
    {
        fl_contain.addView(baiduMapView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        baiduMapController = baiduMapView.getController();
        baiduMapController.setZoom(20);
        baiduMapController.enableClick(true);
        baiduMapController.setZoomGesturesEnabled(true);
        baiduMapController.setScrollGesturesEnabled(true);
        baiduMapView.regMapViewListener(bMapManager, new MKMapViewListener()
        {

            @Override
            public void onMapMoveFinish()
            {

            }

            @Override
            public void onMapLoadFinish()
            {

            }

            @Override
            public void onMapAnimationFinish()
            {

            }

            @Override
            public void onGetCurrentMap(Bitmap arg0)
            {

            }

            @Override
            public void onClickMapPoi(MapPoi arg0)
            {

            }
        });

        baiduMapView.setBuiltInZoomControls(true);
        // 创建 弹出泡泡图层
        createBaiduPop();

        // 定位图层初始化
        baiduLocationOverlay = new LocationOverlay(baiduMapView);

        // 添加定位图层
        baiduMapView.getOverlays().add(baiduLocationOverlay);
        baiduLocationOverlay.enableCompass();
        // 修改定位数据后刷新图层生效
        // mMapView.refresh();
    }

    private void initGoogleMap()
    {
        fl_contain.addView(googleMapView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    protected void setLocation(GpsInfo gpsInfo)
    {
        if (MapType.Baidu == mapType)
        {
            setBaiduLocation();
        } else
        {
            setGoogleLocation();
        }
    }

    private void setBaiduLocation()
    {
        // 设置定位数据
        try
        {
            baiduPoint = new LocationData();

            GeoPoint geoPoint = CoordinateConvertUtil.gcj02ToBd09(gpsInfo);

            baiduPoint.longitude = geoPoint.getLongitudeE6() / 1e6;
            baiduPoint.latitude = geoPoint.getLatitudeE6() / 1e6;

            baiduLocationOverlay.setData(baiduPoint);

            baiduMapController.animateTo(geoPoint);
            if (!TextUtils.isEmpty(gpsInfo.getAddress()))
            {
                baiduLocationOverlay.isPop = true;
                popupText.setText(gpsInfo.getAddress());
                baiduPop.showPopup(ImageUtil.getBitmapFromView(popupText), geoPoint, 18);
            }
            baiduMapView.refresh();
            setFollowVisible();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void setGoogleLocation()
    {
        // We can't be guaranteed that the map is available because Google Play
        // services might
        // not be available.
        setUpMapIfNeeded();

        googlePoint = new CameraPosition.Builder()
                .target(new LatLng(Double.valueOf(gpsInfo.getLatitude()), Double.valueOf(gpsInfo.getLongitude()))).zoom(16f).bearing(0)
                .tilt(25).build();

        try
        {
            changeCamera(CameraUpdateFactory.newCameraPosition(googlePoint));
            setFollowVisible();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void setUpMapIfNeeded()
    {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (googleMap == null)
        {
            // Try to obtain the map from the SupportMapFragment.
            // googleMap = googleMapFragment.getMap();
            googleMap = googleMapView.getMap();

            // Check if we were successful in obtaining the map.

            if (googleMap != null)
            {
                setUpMap();
            }
        }

        showLocation();
    }

    private void setUpMap()
    {
        UiSettings setting = googleMap.getUiSettings();
        setting.setZoomControlsEnabled(true);
        setting.setZoomGesturesEnabled(true);
        setting.setScrollGesturesEnabled(true);
        // 指南针启用
        setting.setCompassEnabled(false);
        setting.setRotateGesturesEnabled(true);
        setting.setTiltGesturesEnabled(true);

    }

    private void showLocation()
    {
        if (null == googleMap)
        {
            return;
        }
        googleMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.location_mark));
        markerOptions.position(new LatLng(Double.valueOf(gpsInfo.getLatitude()), Double.valueOf(gpsInfo.getLongitude())));
        if (!TextUtils.isEmpty(gpsInfo.getAddress()))
        {
            markerOptions.title(gpsInfo.getAddress());
        }
        final Marker marker = googleMap.addMarker(markerOptions);
        if (!TextUtils.isEmpty(gpsInfo.getAddress()))
        {
            googleMap.setOnMapLoadedCallback(new OnMapLoadedCallback()
            {
                @Override
                public void onMapLoaded()
                {
                    marker.showInfoWindow();
                }
            });
        }
    }

    @Override
    protected void follow()
    {
        if (MapType.Baidu == mapType)
        {
            baiduFollow();
        } else
        {
            googleFollow();
        }
    }

    private void baiduFollow()
    {
        if (this.baiduPoint != null && null != baiduMapController)
        {
            baiduMapController.animateTo(CoordinateConvertUtil.gcj02ToBd09(gpsInfo));
        }
    }

    private void googleFollow()
    {
        if (null == this.googlePoint)
            return;

        try
        {
            changeCamera(CameraUpdateFactory.newCameraPosition(googlePoint));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void changeCamera(CameraUpdate update)
    {
        if (null != googleMap)
        {
            googleMap.animateCamera(update);
        }
    }

    private void createBaiduPop()
    {
        super.createPaopao();
        // 泡泡点击响应回调
        PopupClickListener popListener = new PopupClickListener()
        {
            @Override
            public void onClickedPopup(int index)
            {
                Log.v("click", "clickapoapo");
            }
        };
        baiduPop = new PopupOverlay(baiduMapView, popListener);
        baiduMapView.pop = baiduPop;
    }

    class LocationOverlay extends MyLocationOverlay
    {
        private boolean isPop = true;

        public LocationOverlay(com.baidu.mapapi.map.MapView mapView)
        {
            super(mapView);
        }

        @Override
        protected boolean dispatchTap()
        {
            // 处理点击事件,弹出泡泡
            if (isPop)
            {
                baiduPop.hidePop();
                isPop = false;
            } else
            {

                if (!TextUtils.isEmpty(gpsInfo.getAddress()))
                {
                    isPop = true;
                    popupText.setText(gpsInfo.getAddress());
                    baiduPop.showPopup(ImageUtil.getBitmapFromView(popupText), CoordinateConvertUtil.gcj02ToBd09(gpsInfo), 18);
                }
            }
            return true;
        }

    }

    @Override
    protected void onPause()
    {
        if (null != baiduMapView)
        {
            baiduMapView.onPause();
        }
        if (null != googleMapView)
        {
            googleMapView.onPause();
        }
        super.onPause();

    }

    @Override
    protected void onResume()
    {
        if (null != baiduMapView)
        {
            baiduMapView.onResume();
        }
        if (null != googleMapView)
        {
            googleMapView.onResume();
        }
        super.onResume();

    }

    @Override
    protected void onDestroy()
    {
        destoryBaiduMap();
        super.onDestroy();
        destoryGoogleMap();
    }

    private void destoryBaiduMap()
    {
        // 退出时销毁定位
        try
        {
            if (null != baiduMapView)
            {
                baiduMapView.destroy();
                baiduMapView = null;
            }
            if (null != bMapManager)
            {
                bMapManager.destroy();
                bMapManager = null;
            }
            fl_contain.removeAllViews();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void destoryGoogleMap()
    {
        fl_contain.removeAllViews();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (null != baiduMapView)
        {
            baiduMapView.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        if (null != baiduMapView)
        {
            baiduMapView.onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    protected void switchMap()
    {
        if (mapType == MapType.Baidu)
        {
            mapType = MapType.Google;
            destoryBaiduMap();
        } else
        {
            mapType = MapType.Baidu;
            destoryGoogleMap();
        }
        
        initMap();
        PreferencesManager.getInstance().setMapType(mapType);
        if (getLocation)
        {
            getLocation();
        } else
        {
            setLocation(gpsInfo);
        }
    }
}
