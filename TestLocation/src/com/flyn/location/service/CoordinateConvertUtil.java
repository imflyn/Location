package com.flyn.location.service;

import java.util.Locale;

import com.baidu.mapapi.utils.CoordinateConvert;
import com.flyn.location.AppContext;

public class CoordinateConvertUtil
{

    private static double a  = 6378245.0;
    private static double ee = 0.00669342162296594323;

    public static GpsInfo transformFromWGSToGCJ(double latitude, double longitude)
    {
        // 如果在国外，则默认不进行转换
        if (outOfChina(latitude, longitude))
        {
            return new GpsInfo(String.valueOf(latitude), String.valueOf(longitude));
        }
        double dLat = transformLat(longitude - 105.0, latitude - 35.0);
        double dLon = transformLon(longitude - 105.0, latitude - 35.0);
        double radLat = latitude / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI);
        return new GpsInfo(String.valueOf(latitude + dLat), String.valueOf(longitude + dLon));
    }

    private static double transformLat(double x, double y)
    {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(x > 0 ? x : -x);
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLon(double x, double y)
    {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(x > 0 ? x : -x);
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }

    public static boolean outOfChina(double lat, double lon)
    {
        if (lon < 72.004 || lon > 137.8347)
            return true;
        if (lat < 0.8293 || lat > 55.8271)
            return true;
        return false;
    }

    /**
     * GCJ坐标转百度坐标
     * 
     * @return
     */
    public static com.baidu.platform.comapi.basestruct.GeoPoint gcj02ToBd09(GpsInfo gps)
    {

        com.baidu.platform.comapi.basestruct.GeoPoint geoPoint = new com.baidu.platform.comapi.basestruct.GeoPoint((int) (Double.valueOf(gps.getLatitude()) * 1e6), (int) (Double.valueOf(gps
                .getLongitude()) * 1e6));
        return CoordinateConvert.fromGcjToBaidu(geoPoint);
    }

    public static boolean isZh()
    {
        Locale locale = AppContext.getContext().getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        if (language.endsWith("zh"))
            return true;
        else
            return false;
    }

}
