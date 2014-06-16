package com.flyn.location.service;

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
import com.flyn.location.AppContext;

public class MyLocationManager implements LocationConfiguration
{
    private static final int         MININSTANCE = 2;

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
        init(AppContext.getContext());
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

        mLocationClient = new LocationClient(context);
        mLocationClient.registerLocationListener(mMyLocationListener);

        gpsLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (null != gpsLocationManager && gpsLocationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER))
        {
            gpsLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mScanSpan, MININSTANCE, locationListener);
        }
        // 基站定位
        networkLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (null != networkLocationManager && networkLocationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER))
        {
            networkLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            networkLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, mScanSpan, MININSTANCE, locationListener);
        }
        setLocationOption();
    }

    public void getLocationInfo(Listener listener)
    {

        this.listener = listener;

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
        if (null != networkLocationManager && networkLocationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER))
        {
            gpsLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mScanSpan, MININSTANCE, locationListener);
        }

        if (null != networkLocationManager && networkLocationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER))
        {
            networkLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            networkLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, mScanSpan, MININSTANCE, locationListener);
        }
    }

    public void requestLocationInfo()
    {
        this.getLocationInfo(null);
    }

    public GpsInfo getLastLocationInfo()
    {
        // 如果是中文环境优先返回百度地理位置否则优先返回谷歌地理位置
        if (CoordinateConvertUtil.isZh())
        {
            if (baiDuGpsInfo != null)
            {
                return baiDuGpsInfo;
            } else
            {
                return googleGpsInfo;
            }
        } else
        {
            if (googleGpsInfo != null)
            {
                return googleGpsInfo;
            } else
            {
                return baiDuGpsInfo;
            }
        }
    }

    private BDLocationListener     mMyLocationListener = new BDLocationListener()
                                                       {

                                                           @Override
                                                           public void onReceiveLocation(BDLocation location)
                                                           {
                                                               // Receive
                                                               // Location
                                                               // StringBuffer
                                                               // sb = new
                                                               // StringBuffer(256);
                                                               // sb.append("time : ");
                                                               // sb.append(location.getTime());
                                                               // sb.append("\nerror code : ");
                                                               // sb.append(location.getLocType());
                                                               // sb.append("\nlatitude : ");
                                                               // sb.append(location.getLatitude());
                                                               // sb.append("\nlontitude : ");
                                                               // sb.append(location.getLongitude());
                                                               // sb.append("\nradius : ");
                                                               // sb.append(location.getRadius());
                                                               // if
                                                               // (location.getLocType()
                                                               // ==
                                                               // BDLocation.TypeGpsLocation)
                                                               // {
                                                               // sb.append("\nspeed : ");
                                                               // sb.append(location.getSpeed());
                                                               // sb.append("\nsatellite : ");
                                                               // sb.append(location.getSatelliteNumber());
                                                               // sb.append("\ndirection : ");
                                                               // sb.append(location.getDirection());
                                                               // } else if
                                                               // (location.getLocType()
                                                               // ==
                                                               // BDLocation.TypeNetWorkLocation)
                                                               // {
                                                               // sb.append("\naddr : ");
                                                               // sb.append(location.getAddrStr());
                                                               // // 运营商信息
                                                               // sb.append("\noperationers : ");
                                                               // sb.append(location.getOperators());
                                                               // }
                                                               stop();

                                                               if (location.getLocType() == 61 || location.getLocType() == 65 || location.getLocType() == 66 || location.getLocType() == 161)
                                                               {
                                                                   L.i("百度地图位置:getLatitude==" + location.getLatitude());
                                                                   L.i("百度地图位置:getLongitude==" + location.getLongitude());

                                                                   baiDuGpsInfo = new GpsInfo(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), location.getAddrStr());

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

                                                               // GeoPoint
                                                               // getPoint = new
                                                               // GeoPoint((int)
                                                               // (location.getLatitude()
                                                               // * 1E6),
                                                               // (int)
                                                               // (location.getLongitude()
                                                               // * 1E6));

                                                               String address = getLocationAddress(location.getLatitude(), location.getLongitude());
                                                               googleGpsInfo = CoordinateConvertUtil.transformFromWGSToGCJ(location.getLatitude(), location.getLongitude());

                                                               // googleGpsInfo
                                                               // = new
                                                               // GpsInfo(String.valueOf(location.getLatitude()),
                                                               // String.valueOf(location.getLongitude()),
                                                               // getLocationAddress(getPoint));

                                                               googleGpsInfo.setAddress(address);

                                                               stop();

                                                               callListener();

                                                           }
                                                       };

    private String getLocationAddress(double latitude, double longitude)
    {
        String add = "";
        Geocoder geoCoder = new Geocoder(AppContext.getContext(), Locale.getDefault());
        try
        {
            List<Address> addresses = geoCoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0)
            {
                Address address = addresses.get(0);
                if (address.getMaxAddressLineIndex() >= 0)
                {
                    add = address.getAddressLine(0);
                }
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

        if (null != gpsLocationManager)
        {
            gpsLocationManager.removeUpdates(locationListener);
        }
        if (null != networkLocationManager)
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
            mLocationClient.setLocOption(option);
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
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
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

            if (CoordinateConvertUtil.isZh())
            {
                if (baiDuGpsInfo != null)
                {
                    L.i("使用百度坐标");
                    listener.onSuccess(baiDuGpsInfo);
                } else if (null != googleGpsInfo)
                {
                    L.i("使用谷歌坐标");
                    listener.onSuccess(googleGpsInfo);
                }
            } else
            {
                if (googleGpsInfo != null)
                {
                    L.i("使用谷歌坐标");
                    listener.onSuccess(googleGpsInfo);
                } else if (null != baiDuGpsInfo)
                {
                    L.i("使用百度坐标");
                    listener.onSuccess(baiDuGpsInfo);
                }
            }
        }
    }

}
