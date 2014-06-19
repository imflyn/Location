package com.flyn.location.service;

import android.os.Parcel;
import android.os.Parcelable;

public class GpsInfo implements Parcelable
{

    private String latitude; // 纬度
    private String longitude; // 经度
    private String address;  // 地址

    /**
     * 获取纬度
     * 
     * @return
     */
    public final String getLatitude()
    {
        return latitude;
    }

    public final void setLatitude(String latitude)
    {
        this.latitude = latitude;
    }

    /**
     * 获取经度
     * 
     * @return
     */
    public final String getLongitude()
    {
        return longitude;
    }

    public final void setLongitude(String longitude)
    {
        this.longitude = longitude;
    }

    /**
     * 获取地址
     * 
     * @return
     */
    public final String getAddress()
    {
        return address;
    }

    public final void setAddress(String address)
    {
        this.address = address;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    public GpsInfo()
    {

    }

    public GpsInfo(String latitude, String longitude)
    {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public GpsInfo(String latitude, String longitude, String address)
    {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    private GpsInfo(final Parcel in)
    {
        latitude = in.readString();
        longitude = in.readString();
        address = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeString(address);
    }

    public static final Parcelable.Creator<GpsInfo> CREATOR = new Parcelable.Creator<GpsInfo>()
                                                            {

                                                                @Override
                                                                public GpsInfo createFromParcel(Parcel source)
                                                                {
                                                                    return new GpsInfo(source);
                                                                }

                                                                @Override
                                                                public GpsInfo[] newArray(int size)
                                                                {
                                                                    return new GpsInfo[size];
                                                                }

                                                            };
}
