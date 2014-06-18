package com.flyn.location.service;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MyItemizedOverlay extends ItemizedOverlay
{
    private List<OverlayItem> overlays = new ArrayList<OverlayItem>();

    public MyItemizedOverlay(Drawable defaultMarker)
    {
        super(boundCenterBottom(defaultMarker));
    }

    @Override
    protected OverlayItem createItem(int i)
    {
        return overlays.get(i);
    }

    @Override
    public int size()
    {
        return overlays.size();
    }

    public void addOverlay(OverlayItem item)
    {
        overlays.add(item);
        populate();
    }

    public void removeOverlay(int location)
    {
        overlays.remove(location);
    }

    @Override
    public boolean onTap(GeoPoint p, MapView mapView)
    {
        return super.onTap(p, mapView);
    }

    @Override
    protected boolean onTap(int index)
    {
        return super.onTap(index);
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow)
    {
        super.draw(canvas, mapView, shadow);
    }

}