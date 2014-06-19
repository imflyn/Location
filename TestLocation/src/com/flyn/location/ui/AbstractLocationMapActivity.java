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

import com.baidu.mapapi.BMapManager;
import com.flyn.location.service.CoordinateConvertUtil;
import com.flyn.location.service.MyLocationManager;
import com.google.android.maps.MapActivity;

public abstract class AbstractLocationMapActivity extends MapActivity
{
    private static final String GPSINFO      = "GPSINFO";
    private static final String GET_LOCATION = "GET_LOCATION";

    protected GpsInfo           gpsInfo;
    protected boolean           isInited     = false;
    protected boolean           getLocation  = false;
    protected boolean           isChina      = false;

    protected Button            btn_right;
    protected TextView          popupText;                    // 泡泡view
    protected View              viewCache;
    protected FrameLayout       fl_contain;

    private BMapManager         bMapManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        if (null != savedInstanceState)
        {
            gpsInfo = savedInstanceState.getParcelable(GPSINFO);
            getLocation = savedInstanceState.getBoolean(GET_LOCATION);
        } else
        {
            gpsInfo = getIntent().getParcelableExtra(GPSINFO);
            getLocation = getIntent().getBooleanExtra(GET_LOCATION, false);
        }
        setContentView(R.layout.activity_locationmap);
        initView();
        if (gpsInfo != null)
        {// 已有地理位置时点击进入
            setLocation(gpsInfo);
        } else if (getLocation)
        {// 没有地理位置需要获取地理
            MyLocationManager.getInstance().getLocationInfo(new Listener()
            {
                @Override
                public void onSuccess(GpsInfo gpsInfo)
                {

                    AbstractLocationMapActivity.this.gpsInfo = gpsInfo;
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
                MyLocationManager.getInstance().stop();
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
    }

    /**
     * 创建图层
     */
    protected void createPop()
    {
        viewCache = getLayoutInflater().inflate(R.layout.custom_text_view, null);
        popupText = (TextView) viewCache.findViewById(R.id.textcache);
    }

    protected void setLocation(GpsInfo gpsInfo)
    {
        // 如果坐标不在国内需要初始化百度地图
        if (!CoordinateConvertUtil.outOfChina(Double.valueOf(gpsInfo.getLatitude()), Double.valueOf(gpsInfo.getLongitude())))
        {
            bMapManager = new BMapManager(getApplicationContext());
            bMapManager.init(null);
            isChina = true;
        }
        if (!isInited)
        {
            initMap();
            findViewById(R.id.btn_follow).bringToFront();
            synchronized (AbstractLocationMap.class)
            {
                isInited = true;
            }
        }
    }

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

    @Override
    protected boolean isRouteDisplayed()
    {
        return false;
    }

    public void goBack(View view)
    {
        finish();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        try
        {
            if (null != bMapManager)
            {
                bMapManager.destroy();
                bMapManager = null;
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 创建跳转界面Intent
     * 
     * @param context
     * @param gpsInfo
     *            地理位置信息
     * @param getLocation
     *            是否需要获取地理位置(如果gpsInfo为null时传入true时获取地理位置)
     * @return
     */
    public static Intent createIntent(Context context, GpsInfo gpsInfo, boolean getLocation)
    {
        Intent intent = new Intent();
        intent.setClass(context, LocationMapActivity.class);
        if (null != gpsInfo)
        {
            intent.putExtra(GPSINFO, gpsInfo);
        }
        intent.putExtra(GET_LOCATION, getLocation);
        return intent;
    }

}
