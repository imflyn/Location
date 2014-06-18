package com.flyn.location.service;

import com.baidu.location.LocationClientOption.LocationMode;

public interface LocationConfiguration
{
    LocationMode mLocationMode     = LocationMode.Hight_Accuracy; // 定位模式(高精度)
    boolean      mLocationSequency = false;                      // 连续定位
    int          mScanSpan         = 5 * 1000;                   // 连续定位时间间隔
    boolean      mIsNeedAddress    = true;                       // 经纬度是否转化为地址
    String       mCoordType        = "gcj02";                    // (有三种)gcj02,bd09ll(百度建议使用),bd09
    boolean      mIsNeedDirection  = false;                      // 是否需要定位时的箭头方向
    boolean      mGeofenceInit     = false;                      // 使用围栏
    int          MININSTANCE       = 2;
}
