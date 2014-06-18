package com.flyn.location.ui;

import java.util.List;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup.LayoutParams;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class GoogleMapActivity extends AbstractLocationMap
{
    private MapView           mMapView;
    private MapController     mMapController;
    private GeoPoint          locPoint;
    private MyItemizedOverlay mMyItemizedOverlay;
    private List<Overlay>     mapOverlays;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initMap()
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

        mMapView = new MapView(this, key);

        fl_contain.addView(mMapView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mMapController = mMapView.getController();
        mapOverlays = mMapView.getOverlays();
        mMapView.setBuiltInZoomControls(true);
        mMapView.setEnabled(true);
        mMapView.setClickable(true);
        // 设置地图支持缩放
        mMapView.setBuiltInZoomControls(true);
        mMapController.setZoom(20);
        mMyItemizedOverlay = new MyItemizedOverlay(getResources().getDrawable(R.drawable.location_mark));

    }

    protected void follow()
    {
        if (null != mMapController && null != locPoint)
        {
            mMapController.animateTo(locPoint);
            mMapView.invalidate();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void setLocation(GpsInfo gpsInfo)
    {
        locPoint = new GeoPoint((int) (Double.valueOf(gpsInfo.getLatitude()) * 1E6), (int) (Double.valueOf(gpsInfo.getLongitude()) * 1E6));
        OverlayItem overlayitem = new OverlayItem(locPoint, null, gpsInfo.getAddress());
        if (mMyItemizedOverlay.size() > 0)
        {
            mMyItemizedOverlay.removeOverlay(0);
        }
        mMyItemizedOverlay.addOverlay(overlayitem);
        mMyItemizedOverlay.setFocus(overlayitem);
        mapOverlays.add(mMyItemizedOverlay);
        mMapController.animateTo(locPoint);
        createPaopao();
        if (!TextUtils.isEmpty(gpsInfo.getAddress()))
        {
            popupText.setText(gpsInfo.getAddress());
        }
        mMapView.invalidate();
    }

    protected void createPaopao()
    {
        if (null != viewCache)
        {
            return;
        }
        super.createPaopao();

        MapView.LayoutParams params = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT, MapView.LayoutParams.WRAP_CONTENT,
                locPoint, MapView.LayoutParams.BOTTOM_CENTER);
        if (null != mMapView)
        {
            mMapView.removeView(mMapView);
        }
        mMapView.addView(viewCache, params);
    }

}
