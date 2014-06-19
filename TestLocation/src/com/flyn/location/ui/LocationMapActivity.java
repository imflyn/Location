package com.flyn.location.ui;

import java.util.List;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;

import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.flyn.location.service.CoordinateConvertUtil;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class LocationMapActivity extends AbstractLocationMapActivity
{
    // 百度
    private LocationData                          baiduPoint;
    private LocationOverlay                       baiduLocationOverlay;
    private PopupOverlay                          baiduPop;
    private MyLocationMapView                     baiduMapView;
    private MapController                         baiduMapController;

    // google
    private MapView                               googleMapView;
    private com.google.android.maps.MapController googleMapController;
    private GeoPoint                              googlePoint;
    private MyItemizedOverlay                     googleItemizedOverlay;
    private List<Overlay>                         googleOverlays;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void initMap()
    {
        if (isChina)
        {
            initBaiduMap();
        } else
        {
            initGoogleMap();
        }
    }

    private void initBaiduMap()
    {
        // 地图初始化
        baiduMapView = new MyLocationMapView(this);
        fl_contain.addView(baiduMapView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        baiduMapController = baiduMapView.getController();
        baiduMapController.setZoom(20);
        baiduMapController.enableClick(true);
        baiduMapController.setZoomGesturesEnabled(true);
        baiduMapController.setScrollGesturesEnabled(true);
        baiduMapView.setBuiltInZoomControls(true);
        // 创建 弹出泡泡图层
        createPop();

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
        String key = "";
        try
        {
            ActivityInfo info = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
            key = info.metaData.getString("com.google.android.maps.v2.API_KEY");
        } catch (NameNotFoundException e)
        {
            e.printStackTrace();
        }

        googleMapView = new MapView(this, key);
        fl_contain.addView(googleMapView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        googleMapController = googleMapView.getController();
        googleOverlays = googleMapView.getOverlays();
        googleMapView.setBuiltInZoomControls(true);
        googleMapView.setEnabled(true);
        googleMapView.setClickable(true);
        // 设置地图支持缩放
        googleMapView.setBuiltInZoomControls(true);
        googleMapController.setZoom(20);
        googleItemizedOverlay = new MyItemizedOverlay(getResources().getDrawable(R.drawable.location_mark));
    }

    @Override
    protected void setLocation(GpsInfo gpsInfo)
    {
        super.setLocation(gpsInfo);
        if (isChina)
        {
            setBaiduLocation();
        } else
        {
            setGoogleLocation();
        }
    }

    private void setBaiduLocation()
    {
        try
        {
            baiduPoint = new LocationData();

            com.baidu.platform.comapi.basestruct.GeoPoint geoPoint = CoordinateConvertUtil.gcj02ToBd09(gpsInfo);

            baiduPoint.longitude = geoPoint.getLongitudeE6() / 1e6;
            baiduPoint.latitude = geoPoint.getLatitudeE6() / 1e6;

            baiduLocationOverlay.setData(baiduPoint);

            baiduMapController.animateTo(geoPoint);

            popupText.setText(gpsInfo.getAddress());
            baiduPop.showPopup(ImageUtil.getBitmapFromView(popupText), geoPoint, 18);
            baiduMapView.refresh();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void setGoogleLocation()
    {
        googlePoint = new GeoPoint((int) (Double.valueOf(gpsInfo.getLatitude()) * 1E6),
                (int) (Double.valueOf(gpsInfo.getLongitude()) * 1E6));
        OverlayItem overlayitem = new OverlayItem(googlePoint, null, gpsInfo.getAddress());
        if (googleItemizedOverlay.size() > 0)
        {
            googleItemizedOverlay.removeOverlay(0);
        }
        googleItemizedOverlay.addOverlay(overlayitem);
        googleItemizedOverlay.setFocus(overlayitem);
        googleOverlays.add(googleItemizedOverlay);
        googleMapController.animateTo(googlePoint);
        createPop();
        if (!TextUtils.isEmpty(gpsInfo.getAddress()))
        {
            popupText.setText(gpsInfo.getAddress());
        }
        googleMapView.invalidate();
    }

    @Override
    protected void follow()
    {
        if (isChina)
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
        if (null != googleMapController && null != googlePoint)
        {
            googleMapController.animateTo(googlePoint);
            googleMapView.invalidate();
        }
    }

    @Override
    protected void createPop()
    {
        if (null != viewCache)
            return;
        super.createPop();
        if (isChina)
        {
            createBaiduPop();
        } else
        {
            createGooglePop();
        }
    }

    private void createBaiduPop()
    {
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

    private void createGooglePop()
    {
        MapView.LayoutParams params = new MapView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, googlePoint,
                MapView.LayoutParams.BOTTOM_CENTER);
        if (null != googleMapView)
        {
            googleMapView.removeView(viewCache);
        }
        googleMapView.addView(viewCache, params);
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
                isPop = true;
                popupText.setText(gpsInfo.getAddress());

                baiduPop.showPopup(ImageUtil.getBitmapFromView(popupText), CoordinateConvertUtil.gcj02ToBd09(gpsInfo), 18);
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
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        if (null != baiduMapView)
        {
            baiduMapView.onResume();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        // 退出时销毁定位
        try
        {
            if (null != baiduMapView)
            {
                baiduMapView.destroy();
                baiduMapView = null;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (null != baiduMapView)
        {
            baiduMapView.onSaveInstanceState(outState);
        }
        if (null != googleMapView)
        {
            googleMapView.onSaveInstanceState(outState);
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
        if (null != googleMapView)
        {
            googleMapView.onRestoreInstanceState(savedInstanceState);
        }
    }

}
