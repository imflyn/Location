package com.flyn.location.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.LocationData;
import com.baidu.mapapi.map.MKMapViewListener;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationOverlay;
import com.baidu.mapapi.map.PopupClickListener;
import com.baidu.mapapi.map.PopupOverlay;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.flyn.location.R;
import com.flyn.location.service.CoordinateConvertUtil;
import com.flyn.location.service.GpsInfo;
import com.flyn.location.service.ImageUtil;

/**
 * 用来展示地理位置地图
 * 
 */
public class BaiduMapActivity extends AbstractLocationMap
{

    private LocationData      locData;
    // 定位图层
    private LocationOverlay   myLocationOverlay;
    // 弹出泡泡图层
    private PopupOverlay      pop;              // 弹出泡泡图层，浏览节点时使用

    // 地图相关，使用继承MapView的MyLocationMapView目的是重写touch事件实现泡泡处理
    // 如果不处理touch事件，则无需继承，直接使用MapView即可
    private MyLocationMapView mMapView;         // 地图View
    private MapController     mMapController;
    private BMapManager       bMapManager;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {

        /**
         * 使用地图sdk前需先初始化BMapManager. BMapManager是全局的，可为多个MapView共用，它需要地图模块创建前创建，
         * 并在地图地图模块销毁后销毁，只要还有地图模块在使用，BMapManager就不应该销毁
         */
        bMapManager = new BMapManager(getApplicationContext());
        bMapManager.init(null);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locationmap);
    }

    protected void initMap()
    {
        // 地图初始化
        mMapView = new MyLocationMapView(this);
        fl_contain.addView(mMapView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mMapController = mMapView.getController();
        mMapController.setZoom(20);
        mMapController.enableClick(true);
        mMapController.setZoomGesturesEnabled(true);
        mMapController.setScrollGesturesEnabled(true);
        mMapView.regMapViewListener(bMapManager, new MKMapViewListener()
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

        mMapView.setBuiltInZoomControls(true);
        // 创建 弹出泡泡图层
        createPaopao();

        // 定位图层初始化
        myLocationOverlay = new LocationOverlay(mMapView);

        // 添加定位图层
        mMapView.getOverlays().add(myLocationOverlay);
        myLocationOverlay.enableCompass();
        // 修改定位数据后刷新图层生效
        // mMapView.refresh();

    }

    @Override
    protected void setLocation(GpsInfo gpsInfo)
    {
        // 设置定位数据
        try
        {
            locData = new LocationData();

            GeoPoint geoPoint = CoordinateConvertUtil.gcj02ToBd09(gpsInfo);

            locData.longitude = geoPoint.getLongitudeE6() / 1e6;
            locData.latitude = geoPoint.getLatitudeE6() / 1e6;

            myLocationOverlay.setData(locData);

            mMapController.animateTo(geoPoint);

            popupText.setText(gpsInfo.getAddress());
            pop.showPopup(ImageUtil.getBitmapFromView(viewCache), geoPoint, 18);
            mMapView.refresh();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 创建弹出泡泡图层
     */
    protected void createPaopao()
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
        pop = new PopupOverlay(mMapView, popListener);
        mMapView.pop = pop;

    }

    // 继承MyLocationOverlay重写dispatchTap实现点击处理
    public class LocationOverlay extends MyLocationOverlay
    {
        private boolean isPop = true;

        public LocationOverlay(MapView mapView)
        {
            super(mapView);
        }

        @Override
        protected boolean dispatchTap()
        {
            // 处理点击事件,弹出泡泡
            if (isPop)
            {
                pop.hidePop();
                isPop = false;
            } else
            {
                isPop = true;
                popupText.setText(gpsInfo.getAddress());

                pop.showPopup(ImageUtil.getBitmapFromView(viewCache), CoordinateConvertUtil.gcj02ToBd09(gpsInfo), 18);
            }
            return true;
        }

    }

    @Override
    protected void onPause()
    {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        // 退出时销毁定位
        try
        {
            if (null != mMapView)
            {
                mMapView.destroy();
                mMapView = null;
            }
            if (null != bMapManager)
            {
                bMapManager.destroy();
                mMapView = null;
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
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        mMapView.onRestoreInstanceState(savedInstanceState);
    }

    public void follow()
    {
        if (this.locData != null && null != mMapController)
        {
            mMapController.animateTo(CoordinateConvertUtil.gcj02ToBd09(gpsInfo));
        }

    }

}
