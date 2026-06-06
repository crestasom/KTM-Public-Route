package com.crestaSom.KTMPublicRoute;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crestaSom.KTMPublicRoute.data.DataWrapper;
import com.crestaSom.implementation.KtmPublicRoute;
import com.crestaSom.model.RouteDataWrapper;
import com.crestaSom.model.Vertex;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements View.OnClickListener {
    private MapView mMapView;
    private IMapController mMapController;
    List<Integer> cList;
    RouteDataWrapper routeDataWrapper;
    TextView routeInfo;
    Button tracker;
    ImageView currentPosition, zoomIn, zoomOut;
    String provider;
    Toast t;
    Criteria cri;
    ItemizedIconOverlay<OverlayItem> currentLocationOverlay;
    ArrayList<OverlayItem> items;
    Boolean flag, flagAlt;
    List<Vertex> path, localPath;
    SharedPreferences prefs;
    int language;

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        currentPosition = (ImageView) view.findViewById(R.id.current_location);
        currentPosition = (ImageView) view.findViewById(R.id.current_location);
        zoomIn = (ImageView) view.findViewById(R.id.zoomin);
        zoomOut = (ImageView) view.findViewById(R.id.zoomout);
        routeInfo = (TextView) view.findViewById(R.id.routeInfo);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        language = Integer.parseInt(prefs.getString("language", "1"));
        zoomIn.setOnClickListener(this);
        zoomOut.setOnClickListener(this);
        cList = new ArrayList<Integer>();
        currentPosition.setOnClickListener(this);
        currentPosition.setVisibility(View.INVISIBLE);
        cList.add(Color.BLUE);
        cList.add(Color.GREEN);
        cList.add(Color.MAGENTA);
        cList.add(Color.CYAN);
        cList.add(Color.DKGRAY);
        cList.add(Color.YELLOW);

        // osmdroid 6.x: must init Configuration before using MapView
        Configuration.getInstance().load(
                getActivity().getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()));
        Configuration.getInstance().setUserAgentValue(
                getActivity().getPackageName());
        // Use app-specific storage so no WRITE_EXTERNAL_STORAGE needed on API 29+
        java.io.File osmDir = new java.io.File(
                getActivity().getExternalFilesDir(null), "osmdroid");
        Configuration.getInstance().setOsmdroidBasePath(osmDir);
        Configuration.getInstance().setOsmdroidTileCache(
                new java.io.File(osmDir, "tiles"));

        mMapView = (MapView) view.findViewById(R.id.mapview);
        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        mMapView.setBuiltInZoomControls(true);
        mMapController = mMapView.getController();
        mMapController.setZoom(14.0);
        mMapView.setBuiltInZoomControls(true);
        mMapView.setMultiTouchControls(true);
        if (!isNetworkAvailable())
            mMapView.setMaxZoomLevel(15.0);
        mMapView.setMinZoomLevel(13.0);
        BoundingBox boundingBox = new BoundingBox(27.79778, 85.583496, 27.589241, 85.216141);
        //new BoundingBox(north, east, south, west)
        mMapView.setScrollableAreaLimitDouble(boundingBox);
        mMapView.setBuiltInZoomControls(false);


        //receiving data from activity
        Bundle bundle = getArguments();

        flagAlt = bundle.getBoolean("flagAlt", false);
        if (flagAlt) {
            routeDataWrapper = (RouteDataWrapper) bundle.getSerializable("data");
            Vertex start = routeDataWrapper.getRouteData1().get(0).getvList().get(0);
            mMapController.setCenter(new GeoPoint(start.getLatCode(), start.getLongCode()));
            createMarker(routeDataWrapper.getRouteData1().get(0).getvList(), 0);
            createMarker(routeDataWrapper.getRouteData2().get(0).getvList(), 1);
        } else {
            DataWrapper dw = (DataWrapper) bundle.getSerializable("vList");
            path = dw.getvList();
            flag = bundle.getBoolean("flag");
            localPath = new ArrayList<>();
            localPath.addAll(path);
            //Log.d("Vertex List", path.toString());
            List<Vertex> vertexList = null;
            int i = 0;
            //Log.d("i", "i:" + i);
            System.out.println(flag + " is value of flag");
            double latCode = 0, longCode = 0;
            for (Vertex v : path) {
                latCode += v.getLatCode();
                longCode += v.getLongCode();
            }
            if (flag) {
                currentPosition.setVisibility(View.INVISIBLE);
            }

            mMapController.setCenter(new GeoPoint(path.get(0).getLatCode(), path.get(0).getLongCode()));
            new ArrayList<Integer>();
            Map<List<Integer>, List<Vertex>> pathRoute;
            KtmPublicRoute imp = new KtmPublicRoute(getActivity());
            while (!localPath.isEmpty()) {
                vertexList = new ArrayList<Vertex>();
                if (localPath.size() == 1) {
                    break;
                }
                pathRoute = imp.findRoutePath(localPath);
                //Log.d("path", "" + pathRoute);

                Iterator<Map.Entry<List<Integer>, List<Vertex>>> it = pathRoute.entrySet().iterator();
                while (it.hasNext()) {

                    Map.Entry<List<Integer>, List<Vertex>> pair = it.next();

                    vertexList = pair.getValue();
                    createMarker(vertexList, i);
                    //Log.d("Vertex", "" + vertexList);


                }
                i++;
            }
        }

        return view;
    }


    void createMarker(List<Vertex> path, int color) {

//		//Log.d("", "Vertex Path in Map:" + path.toString());
//		//Log.d("Line Color", "pos:" + color);

        items = new ArrayList<OverlayItem>();
        Polyline myPath = new Polyline();
        myPath.getOutlinePaint().setColor(cList.get(color));
        myPath.getOutlinePaint().setStrokeWidth(15);
        List<GeoPoint> geoPoints = new ArrayList<>();

        for (Vertex v : path) {
            GeoPoint p1 = new GeoPoint(v.getLatCode(), v.getLongCode());
            if (!v.isTransit()) {
                if (language == 1)
                    items.add(new OverlayItem(v.getName(), "", p1));
                else
                    items.add(new OverlayItem(v.getNameNepali(), "", p1));
            }
            geoPoints.add(p1);
        }

        myPath.setPoints(geoPoints);
        mMapView.getOverlays().add(myPath);

        currentLocationOverlay = new ItemizedIconOverlay<>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        routeInfo.setText(item.getTitle());
                        if (routeInfo.getVisibility() == View.INVISIBLE) {
                            routeInfo.setVisibility(View.VISIBLE);
                        }
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return true;
                    }
                }, getActivity());

        mMapView.getOverlays().add(currentLocationOverlay);
        mMapView.invalidate();
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.current_location) {
            String off = Settings.Secure.getString(getActivity().getApplicationContext().getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            if (off.isEmpty()) {
                Toast.makeText(getActivity().getApplicationContext(), "Please Enable Location", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                return;
            }
            startFetchCoordinates();


        } else if (view.getId() == R.id.zoomin) {
            mMapController.zoomIn();
        } else if (view.getId() == R.id.zoomout) {
            mMapController.zoomOut();
        }
    }

    @SuppressLint("MissingPermission")
    private void startFetchCoordinates() {
        LocationManager mgr = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        cri = new Criteria();
        provider = mgr.getBestProvider(cri, false);

        ProgressDialog progDailog = new ProgressDialog(getActivity());
        progDailog.setMessage("Detecting your current location....");
        progDailog.setIndeterminate(false);
        progDailog.setCancelable(true);
        progDailog.show();

        Handler handler = new Handler(Looper.getMainLooper());
        final double[] coords = {0.0, 0.0};
        LocationListener[] listenerRef = new LocationListener[1];

        Runnable onComplete = () -> {
            try { mgr.removeUpdates(listenerRef[0]); } catch (Exception ignored) {}
            progDailog.dismiss();
            if (coords[0] != 0.0) {
                GeoPoint p1 = new GeoPoint(coords[0], coords[1]);
                mMapController.setCenter(p1);
                ArrayList<OverlayItem> itemst = new ArrayList<>();
                itemst.add(new OverlayItem("Current Position", "", p1));
                currentLocationOverlay = new ItemizedIconOverlay<>(itemst,
                        new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                            @Override
                            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                                Toast.makeText(getActivity(), item.getTitle(), Toast.LENGTH_SHORT).show();
                                return true;
                            }
                            @Override
                            public boolean onItemLongPress(int index, OverlayItem item) { return true; }
                        }, getActivity());
                mMapView.getOverlays().add(currentLocationOverlay);
                mMapView.invalidate();
            } else {
                Toast.makeText(getActivity(), "Location Not Found", Toast.LENGTH_SHORT).show();
            }
        };

        listenerRef[0] = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                coords[0] = location.getLatitude();
                coords[1] = location.getLongitude();
                handler.removeCallbacksAndMessages(null);
                handler.post(onComplete);
            }
            @Override public void onProviderDisabled(String p) { Log.i("OnProviderDisabled", "OnProviderDisabled"); }
            @Override public void onProviderEnabled(String p) { Log.i("onProviderEnabled", "onProviderEnabled"); }
            @Override public void onStatusChanged(String p, int s, Bundle e) { Log.i("onStatusChanged", "onStatusChanged"); }
        };

        if (mgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mgr.requestLocationUpdates(provider, 30000, 0, listenerRef[0]);
        } else {
            mgr.requestLocationUpdates(provider, 0, 0, listenerRef[0]);
        }
        handler.postDelayed(onComplete, 10000);

        progDailog.setOnCancelListener(d -> {
            handler.removeCallbacksAndMessages(null);
            try { mgr.removeUpdates(listenerRef[0]); } catch (Exception ignored) {}
            Toast.makeText(getActivity(), "Location Not Found", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
