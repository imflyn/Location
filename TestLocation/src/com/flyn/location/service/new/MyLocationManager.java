package com.baital.android.project.readKids.service.location;

import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baital.android.project.readKids.BeemApplication;
import com.baital.android.project.readKids.bll.GpsInfo;
import com.baital.android.project.readKids.constant.FurtherControl;
import com.baital.android.project.readKids.httpUtils.L;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class MyLocationManager implements LocationConfiguration
{
    private static final MapType     DEFAULT_MAPTYPE = MapType.Baidu;

    private MapType                  map_type        = DEFAULT_MAPTYPE;

    private static MyLocationManager locationManager;

    private Listener                 listener;

    private LocationClient           mLocationClient;
    private boolean                  mLocationInit;

    private LocationManager          gpsLocationManager;
    private LocationManager          networkLocationManager;

    private GpsInfo                  baiDuGpsInfo;
    private GpsInfo                  googleGpsInfo;

    public interface Listener
    {
        void onSuccess(GpsInfo gpsInfo);

        void onError(String errorMsg);

    }

    private MyLocationManager()
    {
        init(BeemApplication.getContext());
    }

    public static MyLocationManager getInstance()
    {
        if (null == locationManager)
        {
            synchronized (MyLocationManager.class)
            {
                locationManager = new MyLocationManager();
            }

        }
        return locationManager;
    }

    private void init(Context context)
    {
        if (!FurtherControl.hasFurther(context, FurtherControl.Location_Further_Key))
            return;

        mLocationClient = new LocationClient(context);
        mLocationClient.registerLocationListener(mMyLocationListener);

        gpsLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 基站定位
        networkLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        setLocationOption();
    }

    public void getLocationInfo(Listener listener, MapType map_type)
    {
        if (!FurtherControl.hasFurther(BeemApplication.getContext(), FurtherControl.Location_Further_Key))
            return;

        this.listener = listener;
        this.map_type = map_type;

        if (map_type == MapType.Google)
        {
            if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(BeemApplication.getContext()) == ConnectionResult.SUCCESS)
            {
                L.i("google服务可用");
                if (null != networkLocationManager
                        && networkLocationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER))
                {
                    gpsLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mScanSpan, MININSTANCE, locationListener);
                }

                if (null != networkLocationManager
                        && networkLocationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER))
                {
                    networkLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    networkLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, mScanSpan, MININSTANCE,
                            locationListener);
                }

            } else
            {
                L.i("google服务不可用");

                if (null != listener)
                {
                    listener.onError("google service unavailable");
                    this.listener = null;
                }
            }

        } else
        {
            if (mLocationInit)
            {
                mLocationClient.start();
            }
            // 开始定位
            boolean isStart = mLocationClient.isStarted();
            if (!mLocationSequency && isStart)
            {
                // 单次请求定位
                mLocationClient.requestLocation();
            }
        }
    }
    
    /**
     * 默认使用百度地图，该方法现在主要在通知时获取位置，该方法主要对应常州客户，书童中暂未使用
     */
    public void requestLocationInfo()
    {
        this.getLocationInfo(null, DEFAULT_MAPTYPE);
    }

    public GpsInfo getLastLocationInfo()
    {
        if (map_type == MapType.Baidu)
        {
            return baiDuGpsInfo;
        } else
        {
            return googleGpsInfo;
        }
    }

    private BDLocationListener     mMyLocationListener = new BDLocationListener()
                                                       {

                                                           @Override
                                                           public void onReceiveLocation(BDLocation location)
                                                           {
                                                               stop();

                                                               if (location.getLocType() == 61 || location.getLocType() == 65
                                                                       || location.getLocType() == 66 || location.getLocType() == 161)
                                                               {
                                                                   L.i("百度地图位置:getLatitude==" + location.getLatitude());
                                                                   L.i("百度地图位置:getLongitude==" + location.getLongitude());

                                                                   baiDuGpsInfo = new GpsInfo(String.valueOf(location.getLatitude()),
                                                                           String.valueOf(location.getLongitude()), location.getAddrStr());

                                                                   callListener();
                                                               } else
                                                               {
                                                                   if (null != listener)
                                                                       listener.onError("Error code:" + location.getLocType());
                                                                   baiDuGpsInfo = null;
                                                               }

                                                           }

                                                           @Override
                                                           public void onReceivePoi(BDLocation location)
                                                           {
                                                           }
                                                       };

    private final LocationListener locationListener    = new LocationListener()
                                                       {
                                                           @Override
                                                           public void onStatusChanged(String provider, int status, Bundle extras)
                                                           {
                                                           }

                                                           @Override
                                                           public void onProviderEnabled(String provider)
                                                           {
                                                           }

                                                           @Override
                                                           public void onProviderDisabled(String provider)
                                                           {
                                                           }

                                                           @Override
                                                           public void onLocationChanged(Location location)
                                                           {
                                                               L.i("谷歌地图位置:getLatitude==" + location.getLatitude());
                                                               L.i("谷歌地图位置:getLongitude==" + location.getLongitude());

                                                               String address = getLocationAddress(location.getLatitude(),
                                                                       location.getLongitude());
                                                               // googleGpsInfo
                                                               // =
                                                               // CoordinateConvertUtil.transformFromWGSToGCJ(
                                                               // location.getLatitude(),
                                                               // location.getLongitude());

                                                               googleGpsInfo = new GpsInfo(String.valueOf(location.getLatitude()),
                                                                       String.valueOf(location.getLongitude()));

                                                               googleGpsInfo.setAddress(address);

                                                               stop();

                                                               callListener();

                                                           }
                                                       };

    /**
     * 谷歌根据经纬度获取地址
     * 
     * @param latitude
     * @param longitude
     * @return
     */
    private String getLocationAddress(double latitude, double longitude)
    {
        String add = "";
        Geocoder geoCoder = new Geocoder(BeemApplication.getContext(), Locale.getDefault());
        try
        {
            List<Address> addresses = geoCoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0)
            {
                Address address = addresses.get(0);
                add = address.getAddressLine(0) ;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return add;
    }

    public void stop()
    {
        if (null != mLocationClient)
            mLocationClient.stop();

        if (null != gpsLocationManager && null != locationListener)
        {
            gpsLocationManager.removeUpdates(locationListener);
        }
        if (null != networkLocationManager && null != locationListener)
        {
            networkLocationManager.removeUpdates(locationListener);
        }
    }

    // 设置Option
    private void setLocationOption()
    {
        // 初始化百度
        try
        {
            LocationClientOption option = new LocationClientOption();
            option.setLocationMode(mLocationMode);
            option.setCoorType(mCoordType);
            option.setScanSpan(mScanSpan);
            option.setNeedDeviceDirect(mIsNeedDirection);
            option.setIsNeedAddress(mIsNeedAddress);
            if (null != mLocationClient)
            {
                mLocationClient.setLocOption(option);
            }
            mLocationInit = true;
        } catch (Exception e)
        {
            e.printStackTrace();
            mLocationInit = false;
        }
        // 初始化谷歌设置
        try
        {
            // 查找到服务信息
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            // 高精度
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            criteria.setCostAllowed(true);
            criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
            // 低功耗
            if (null != gpsLocationManager)
            {
                gpsLocationManager.getBestProvider(criteria, true);
            }
            if (null != networkLocationManager)
            {
                networkLocationManager.getBestProvider(criteria, true);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void callListener()
    {
        if (null != listener)
        {
            if (map_type == MapType.Baidu)
            {
                if (baiDuGpsInfo != null)
                {
                    L.i("使用百度坐标");
                    listener.onSuccess(baiDuGpsInfo);
                }
            } else
            {
                if (googleGpsInfo != null)
                {
                    L.i("使用谷歌坐标");
                    listener.onSuccess(googleGpsInfo);
                }
            }
        }
    }

}
