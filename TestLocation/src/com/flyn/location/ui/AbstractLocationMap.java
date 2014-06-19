package com.flyn.location.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.MapActivity;

public abstract class AbstractLocationMap extends MapActivity
{
    private static final String GPSINFO      = "GPSINFO";
    private static final String GET_LOCATION = "GET_LOCATION";

    protected GpsInfo           gpsInfo;
    protected boolean           getLocation  = false;

    protected Button            btn_right;
    protected TextView          popupText;                    // 泡泡view
    protected View              viewCache;
    protected FrameLayout       fl_contain;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去除标题栏
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        setContentView(R.layout.activity_locationmap);
        if (null != savedInstanceState)
        {
            gpsInfo = savedInstanceState.getParcelable(GPSINFO);
            getLocation = savedInstanceState.getBoolean(GET_LOCATION);
        } else
        {
            gpsInfo = getIntent().getParcelableExtra(GPSINFO);
            getLocation = getIntent().getBooleanExtra(GET_LOCATION, false);
        }

        initView();
        if (gpsInfo != null)
        {// 已有地理位置时点击进入
            initMap();
        } else if (getLocation)
        {// 没有地理位置需要获取地理
            initMap();
            MyLocationManager.getInstance().getLocationInfo(new Listener()
            {
                @Override
                public void onSuccess(GpsInfo gpsInfo)
                {

                    AbstractLocationMap.this.gpsInfo = gpsInfo;
                    setLocation(gpsInfo);
                    btn_right.setClickable(true);
                }

                @Override
                public void onError(String errorMsg)
                {
                    L.i("errorMsg:" + errorMsg);
                    Toast.makeText(getApplicationContext(), getString(R.string.loadmap_failure), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void initView()
    {
        findViewById(R.id.head_left_btn).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        findViewById(R.id.btn_follow).setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                follow();
            }
        });

        if (getLocation)
        {
            btn_right = (Button) findViewById(R.id.head_right_btn);
            btn_right.setText(R.string.notice_send_str);
            btn_right.setVisibility(View.VISIBLE);
            btn_right.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View v)
                {

                    Intent intent = new Intent();
                    intent.putExtra(GPSINFO, gpsInfo);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
            btn_right.setClickable(false);
        } else
            findViewById(R.id.head_right_btn).setVisibility(View.INVISIBLE);

        ((TextView) findViewById(R.id.head_center_tv)).setText(R.string.location);

        this.fl_contain = (FrameLayout) findViewById(R.id.fl_contain);

    }

    protected abstract void initMap();

    protected abstract void follow();

    @Override
    protected void onResume()
    {
        super.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        if (!getLocation)
        {
            setLocation(gpsInfo);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        MyLocationManager.getInstance().stop();
    }

    /**
     * 创建图层
     */
    protected void createPaopao()
    {
        viewCache = getLayoutInflater().inflate(R.layout.custom_text_view, null);
        popupText = (TextView) viewCache.findViewById(R.id.textcache);
    }

    protected abstract void setLocation(GpsInfo gpsInfo);

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putParcelable(GPSINFO, gpsInfo);
        outState.putBoolean(GET_LOCATION, getLocation);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        savedInstanceState.putParcelable(GPSINFO, gpsInfo);
        savedInstanceState.putBoolean(GET_LOCATION, getLocation);
    }

    /**
     * 创建跳转界面Intent
     * 
     * @param context
     * @param gpsInfo
     *            地理位置信息
     * @param getLocation
     *            是否需要获取地理位置(如果gpsInfo为null时传入true时获取地理位置)
     * @return 根据配置文件返回是加载谷歌地图还是百度地图
     */
    public static Intent createIntent(Context context, GpsInfo gpsInfo, boolean getLocation)
    {
        Intent intent = new Intent();
        if (FurtherControl.getValue(context, FurtherControl.Map_Type).equalsIgnoreCase("google"))
        {
            intent.setClass(context, GoogleMapActivity2.class);
        } else
        {
            intent.setClass(context, BaiduMapActivity.class);
        }
        if (null != gpsInfo)
        {
            intent.putExtra(GPSINFO, gpsInfo);
        }
        intent.putExtra(GET_LOCATION, getLocation);
        return intent;
    }

    public void goBack(View view)
    {
        finish();
    }

    protected void setFollowVisible()
    {
        findViewById(R.id.btn_follow).setVisibility(View.VISIBLE);
        findViewById(R.id.btn_follow).bringToFront();
    }

    @Override
    protected boolean isRouteDisplayed()
    {
        return true;
    }
}
