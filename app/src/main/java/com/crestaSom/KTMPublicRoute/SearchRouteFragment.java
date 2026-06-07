package com.crestaSom.KTMPublicRoute;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.crestaSom.KTMPublicRoute.data.DataWrapper;
import com.crestaSom.KTMPublicRoute.util.Labels;
import com.crestaSom.KTMPublicRoute.util.Util;
import com.crestaSom.autocomplete.CustomAutoCompleteView;
import com.crestaSom.database.Database;
import com.crestaSom.implementation.KtmPublicRoute;
import com.crestaSom.model.RouteData;
import com.crestaSom.model.RouteDataWrapper;
import com.crestaSom.model.Vertex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * A simple {@link Fragment} subclass.
 */

public class SearchRouteFragment extends Fragment implements View.OnClickListener {



    int language;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private int textColor;
    double[] distanceList;
    SharedPreferences prefs;
    List<Vertex> path;
    Queue<RouteDataWrapper> altPathSingleTransit;
    List<RouteData> singlePaths;
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String KEY = "flag";
    SharedPreferences sharedPref;
    TextView viewDetailTemplate;
    List<Vertex> path1;
    Double distMin = 0.0;
    LocationManager locationmanager;
    Location mlocation, mlocationNew;
    List<Vertex> singleRouteVertex;
    String provider;
    String gpsOrigin = "";
    Criteria cri;
    Double lat = 0.0, longi = 0.0;
    public CustomAutoCompleteView source, destination;
    private ProgressDialog pDialog;
    int startFlag;
    private String display, displaySingle = "";
    public ArrayAdapter<String> myAdapter;
    int displayFlag = 0;
    boolean sourceSelected = false, destinationSelected = false, pathFound = false;
    InputMethodManager imm;
    ImageView gpsToggle, clearSource, clearDestination, gpsToggleDest, swapText;
    int gpsFlag = 0, gpsFlagDest = 0;
    public List<String> item = new ArrayList<>();
    public List<Integer> itemId = new ArrayList<>();
    TextView singleRoute, ViewDetail, ViewDetailSingle, displayTextView;
    int srcId, destId;
    KtmPublicRoute imp;
    ScrollView sv;
    SpannableString displayTravel = null;
    // cached from background thread before posting to UI thread
    private Vertex cachedSourceVertex, cachedDestVertex;

    int mark1, mark2, mark3, mark4;
    LinearLayout shortestRouteLayout, singleRouteLayout, singleRouteLayoutMain, shortestRouteLayoutMain;

    private static final int ANIM_SLIDE_MS = 1000;
    private static final int ANIM_FADE_MS  = 1500;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());


    public SearchRouteFragment() {
        // Required empty public constructor
    }


    public void setUIElements(View v) {

        source = (CustomAutoCompleteView) v.findViewById(R.id.editSource);
        destination = (CustomAutoCompleteView) v.findViewById(R.id.editDestination);
        swapText = (ImageView) v.findViewById(R.id.swapText);
        gpsToggle = (ImageView) v.findViewById(R.id.gpslocation);
        myAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_dropdown_item_1line, item);
        textColor = ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark);
        path = new LinkedList<Vertex>();
        path1 = new ArrayList<>();
        sharedPref = getActivity().getApplicationContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        startFlag = sharedPref.getInt(KEY, -1);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        language = Integer.parseInt(prefs.getString("language", "1"));
        sv = (ScrollView) v.findViewById(R.id.scrollView1);

        clearSource = (ImageView) v.findViewById(R.id.clearSource);
        clearSource.setOnClickListener(this);
        clearDestination = (ImageView) v.findViewById(R.id.clearDestination);
        clearDestination.setOnClickListener(this);

        shortestRouteLayout = (LinearLayout) v.findViewById(R.id.shortestRoute);
        singleRouteLayout = (LinearLayout) v.findViewById(R.id.singleRoute);
        ViewDetail = (TextView) v.findViewById(R.id.viewDetailRoute);
        ViewDetail.setOnClickListener(this);
        ViewDetailSingle = (TextView) v.findViewById(R.id.viewSingleRoute);
        singleRouteLayoutMain = (LinearLayout) v.findViewById(R.id.singleRouteDisplay);
        shortestRouteLayoutMain = (LinearLayout) v.findViewById(R.id.shortestRouteLayout);
        ViewDetailSingle.setOnClickListener(this);
        altPathSingleTransit = new PriorityQueue<>();
        // set our adapter
        myAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_dropdown_item_1line, item);
        // autocompletetextview is in activity_main.xml
        source = (CustomAutoCompleteView) v.findViewById(R.id.editSource);
        Log.d("check", "" + (source == null));
        source.setDropDownBackgroundResource(R.color.dropDownBackground);
    }

    private void setUpSourceAndDestination() {
        source.addTextChangedListener(new CustomAutoCompleteTextChangedListener(getActivity().getApplicationContext(), source.getId()));
        source.setAdapter(myAdapter);
        source.setOnKeyListener((v, keyCode, event) -> {
            srcId = -1;
            return false;
        });

        source.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                sourceSelected = true;
                destination.requestFocus();
            }
        });
        destination.addTextChangedListener(new CustomAutoCompleteTextChangedListener(getActivity().getApplicationContext(), destination.getId()));
        destination.setDropDownBackgroundResource(R.color.dropDownBackground);
        destination.setAdapter(myAdapter);
        destination.setOnKeyListener((v, keyCode, event) -> {
            destId = -1;
            return false;
        });
        destination.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                destinationSelected = true;
            }
        });
    }

    private void setUpSwapText() {

        swapText.setOnClickListener(
                new View.OnClickListener() {

                    @SuppressLint("InlinedApi")
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        String srcT, destT;
                        srcT = source.getText().toString();
                        destT = destination.getText().toString();
                        destination.setText(srcT);
                        source.setText(destT);
                        if (!source.isEnabled()) {
                            source.setEnabled(true);
                            gpsToggle.setImageResource(R.drawable.gps_new);
                        }
                        source.dismissDropDown();
                        destination.dismissDropDown();
                        //if(pathFound){
                        // findPath();
                        //}

                    }
                }
        );
    }


    private void setUpGpsToggle() {
        gpsToggle.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("InlinedApi")
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub


                boolean enabled = locationmanager
                        .isProviderEnabled(LocationManager.GPS_PROVIDER);

                if (!enabled) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    Toast.makeText(getActivity().getApplicationContext(), Labels.enableLocation(getActivity()), Toast.LENGTH_SHORT).show();
                    startActivity(intent);

                } else if (gpsFlag == 0) {
                    gpsOrigin = "source";
                    gpsFlag = 1;
                    startFetchCoordinates();
                } else {
                    gpsFlag = 0;
                    source.setText("");
                    source.setEnabled(true);
//                        source.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
//                        source.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                    source.requestFocus();
                    imm.showSoftInput(source, InputMethodManager.SHOW_IMPLICIT);
                    gpsToggle.setImageResource(R.drawable.gps);
                }


            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_search_route, container, false);
        // Inflate the layout for this fragment
        setUIElements(v);

        singlePaths = new ArrayList<>();
        locationmanager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        imp = new KtmPublicRoute(getActivity().getApplicationContext());
        imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        setUpSourceAndDestination();
        setUpGpsToggle();
        setUpSwapText();
        setDisplayViewText();
        return v;
    }


    private void findShortestPath(Vertex source, Vertex dest) {
        path.clear();
        path = imp.findShortestPath(source, dest);
        path1.clear();
        path1.addAll(path);
    }


    @Override
    public void onClick(View v) {
        language = Integer.parseInt(prefs.getString("language", "1"));
        if (v.getId() == R.id.viewDetailRoute) {
            Intent i = new Intent(getActivity().getApplicationContext(), DetailActivity.class);
            i.putExtra("data", new DataWrapper(path1));
            i.putExtra("flag", false);
            i.putExtra("distanceList", distanceList);

            startActivity(i);

        } else if (v.getTag() instanceof RouteData) {
            RouteData rd = (RouteData) v.getTag();
            Intent i = new Intent(getActivity().getApplicationContext(), DetailActivity.class);
            i.putExtra("data", new DataWrapper(rd.getvList()));
            i.putExtra("flag", false);
            i.putExtra("distanceList", distanceList);
            startActivity(i);
        } else if (v.getTag() instanceof RouteDataWrapper) {
            RouteDataWrapper rdw = (RouteDataWrapper) v.getTag();
            Intent i = new Intent(getActivity().getApplicationContext(), DetailActivity.class);
            i.putExtra("data", rdw);
            i.putExtra("flag", false);
            i.putExtra("flagAlt", true);
            i.putExtra("distanceList", distanceList);
            startActivity(i);
        } else if (v.getId() == R.id.clearSource) {
            source.setText("");
            source.requestFocus();
            source.setError(null);
            imm.showSoftInput(source, InputMethodManager.SHOW_IMPLICIT);
        } else if (v.getId() == R.id.clearDestination) {
            destination.setText("");
            destination.requestFocus();
            destination.setError(null);
            imm.showSoftInput(destination, InputMethodManager.SHOW_IMPLICIT);
        }

    }

    public List<Vertex> getItemsFromDb(String searchTerm) {
        Database db = new Database(getActivity());
        List<Vertex> vertexes = db.getVertexUsingQuery(searchTerm);
        itemId.clear();
        for (Vertex v : vertexes) {
            itemId.add(v.getId());
        }
        return vertexes;
    }

    private void runCalculatePath() {
        pDialog = new ProgressDialog(getActivity());
        pDialog.setIcon(R.drawable.find);
        pDialog.setMessage(Labels.detectingPath(getActivity(), language));
        pDialog.setCancelable(false);
        pDialog.setIndeterminate(false);
        shortestRouteLayout.removeAllViews();
        singleRouteLayout.removeAllViews();
        pDialog.show();

        executor.execute(() -> {
            Database db = new Database(getActivity());
            cachedSourceVertex = db.getVertex(srcId);
            cachedDestVertex = db.getVertex(destId);
            findShortestPath(cachedSourceVertex, cachedDestVertex);
            singlePaths = imp.getSingleRoutes(cachedSourceVertex, cachedDestVertex);
            if (singlePaths.isEmpty()) {
                altPathSingleTransit = imp.getAlternativeRouteOneTransit(cachedSourceVertex, cachedDestVertex);
            }
            mainHandler.post(() -> {
                displayFlag = 1;
                setDisplayText();
            });
        });
    }

    private void setDisplayText() {
        Log.d("path", path.toString());
        sv.smoothScrollTo(0, 0);
        shortestRouteLayout.removeAllViews();
        singleRouteLayout.removeAllViews();
        pDialog.dismiss();
        boolean animEnabled = prefs.getBoolean("animEnabled", true);
        boolean showFare    = prefs.getBoolean("showFare", false);
        sv.setVisibility(View.VISIBLE);
        Vertex sourceP = cachedSourceVertex;
        Vertex destP   = cachedDestVertex;

        double totalDist = displayShortestRouteSection(animEnabled, showFare);

        singleRouteVertex = new ArrayList<>();
        if (!singlePaths.isEmpty()) {
            displayDirectRoutesSection(animEnabled, showFare, sourceP, destP, totalDist);
        } else if (!altPathSingleTransit.isEmpty()) {
            displayAlternativeRoutesSection(animEnabled, showFare, sourceP, destP, totalDist);
        }
    }

    /** Renders the shortest-route section. Returns totalDist for downstream filtering. */
    private double displayShortestRouteSection(boolean animEnabled, boolean showFare) {
        int i = 1, temp = 0;
        int totalCost = 0;
        double totalDist = 0;
        distanceList = new double[10];
        final float scale = getActivity().getResources().getDisplayMetrics().density;

        TextView header = new TextView(getActivity());
        header.setText(Labels.shortestRoute(getActivity(), language) + ":");
        header.setPadding(2, 10, 2, 0);
        header.setGravity(Gravity.CENTER_HORIZONTAL);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        header.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        shortestRouteLayout.addView(header);

        List<Vertex> pathTemp = new ArrayList<>(path);
        while (!pathTemp.isEmpty()) {
            if (pathTemp.size() == 1) break;
            Map<List<Integer>, List<Vertex>> pathRoute = imp.findRoutePath(pathTemp);
            Iterator<Map.Entry<List<Integer>, List<Vertex>>> it = pathRoute.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<List<Integer>, List<Vertex>> pair = it.next();
                List<Vertex> vertexList = pair.getValue();
                Util.addTextView(new SpannableString(Labels.travel(getActivity(), language, i)),
                        shortestRouteLayout, 24, false, textColor, getActivity());
                double d = imp.getRouteDistance(vertexList);
                distanceList[temp++] = d;
                if (distMin < d) {
                    displayTravel = Util.displayTravelText(getActivity(), vertexList, d, imp.getRouteCost(d), false, language, showFare);
                    totalCost += imp.getRouteCost(d);
                    totalDist += d;
                } else {
                    displayTravel = Util.displayTravelText(getActivity(), vertexList, d, 0, true, language, showFare);
                    totalDist += d;
                }
                i++;
                Util.addTextView(displayTravel, shortestRouteLayout, 16, false, textColor, getActivity());
            }
        }
        String summary = Labels.totalDistance(getActivity(), language, totalDist);
        if (showFare) summary += "\n" + Labels.totalCost(getActivity(), language, totalCost);
        Util.addTextView(new SpannableString(summary), shortestRouteLayout, 16, true, textColor, getActivity());
        ViewDetail.setText(Labels.viewDetail(getActivity(), language));
        shortestRouteLayoutMain.setVisibility(View.VISIBLE);
        ViewDetail.setVisibility(View.VISIBLE);
        if (animEnabled) {
            Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_down);
            anim.setDuration(ANIM_SLIDE_MS);
            shortestRouteLayoutMain.startAnimation(anim);
            anim = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein);
            anim.setDuration(ANIM_FADE_MS);
            anim.setStartOffset(0);
            ViewDetail.startAnimation(anim);
        }
        return totalDist;
    }

    /** Renders direct single-transit routes if available. */
    private void displayDirectRoutesSection(boolean animEnabled, boolean showFare,
                                            Vertex sourceP, Vertex destP, double totalDist) {
        int i = 1, loopVar = 0;
        int size = singlePaths.size();
        final float scale = getActivity().getResources().getDisplayMetrics().density;

        TextView header = new TextView(getActivity());
        header.setText(Labels.directRoute(getActivity(), language) + ":");
        header.setGravity(Gravity.CENTER_HORIZONTAL);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        header.setPadding(2, 2, 2, 0);
        header.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        singleRouteLayout.addView(header);

        for (RouteData dw : singlePaths) {
            singleRouteVertex = dw.getvList();
            double d1 = imp.getRouteDistance(singleRouteVertex);
            List<Vertex> tempList = new ArrayList<>();
            tempList.add(sourceP);
            tempList.add(destP);
            if (!singleRouteVertex.equals(path1) && d1 < totalDist + 8.0) {
                Util.addTextView(new SpannableString(Labels.routeNum(getActivity(), language, i) + ":"),
                        singleRouteLayout, 24, false, textColor, getActivity());
                displayTravel = Util.displayTravelText(getActivity(), tempList, d1, imp.getRouteCost(d1), false, language, showFare);
                singleRouteLayout.setVisibility(View.VISIBLE);
                singleRouteLayoutMain.setVisibility(View.VISIBLE);
                Util.addTextView(displayTravel, singleRouteLayout, 16, false, textColor, getActivity());
                Util.addTextView(new SpannableString(Labels.availableRoutes(getActivity(), language) + ":"),
                        singleRouteLayout, 20, true, textColor, getActivity());
                String routeName = (language == 1) ? dw.getrName() : dw.getrNameNepali();
                Util.addTextView(new SpannableString(routeName), singleRouteLayout, 16, false, textColor, getActivity());
                viewDetailTemplate = (TextView) View.inflate(getActivity(), R.layout.view_detail_textview, null);
                viewDetailTemplate.setText(Labels.viewDetail(getActivity(), language));
                viewDetailTemplate.setTag(dw);
                viewDetailTemplate.setOnClickListener(SearchRouteFragment.this);
                singleRouteLayout.addView(viewDetailTemplate);
                if (loopVar < size - 1) {
                    displayTextView = new TextView(getActivity());
                    displayTextView.setText("");
                    displayTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
                    displayTextView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.detailBackground));
                    displayTextView.setTypeface(Typeface.DEFAULT_BOLD);
                    displayTextView.setTextSize((int) (2 * scale + 0.5f));
                    displayTextView.setPadding(2, 0, 2, 0);
                    singleRouteLayout.addView(displayTextView);
                }
                singleRouteLayoutMain.setVisibility(View.VISIBLE);
                if (animEnabled) {
                    Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_down);
                    anim.setDuration(ANIM_SLIDE_MS);
                    anim.setStartOffset(ANIM_SLIDE_MS);
                    singleRouteLayoutMain.startAnimation(anim);
                    anim = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein);
                    anim.setDuration(ANIM_SLIDE_MS);
                    anim.setStartOffset(ANIM_SLIDE_MS);
                    ViewDetailSingle.startAnimation(anim);
                }
                i++;
            }
            loopVar++;
        }
    }

    /** Renders alternative two-transit routes if no direct routes found. */
    private void displayAlternativeRoutesSection(boolean animEnabled, boolean showFare,
                                                 Vertex sourceP, Vertex destP, double totalDist) {
        int i = 1, loopVar = 0;
        int size = altPathSingleTransit.size();
        final float scale = getActivity().getResources().getDisplayMetrics().density;

        TextView header = new TextView(getActivity());
        header.setText(Labels.alternativeRoute(getActivity(), language) + ":");
        header.setGravity(Gravity.CENTER_HORIZONTAL);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        header.setPadding(2, 2, 2, 0);
        header.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
        singleRouteLayout.addView(header);

        for (RouteDataWrapper routeData : altPathSingleTransit) {
            if (routeData.getDistTotal() == totalDist || routeData.getDistTotal() >= totalDist + 6) {
                loopVar++;
                continue;
            }
            List<Vertex> vertices1  = routeData.getRouteData1().get(0).getvList();
            List<Vertex> vertices2  = routeData.getRouteData2().get(0).getvList();
            Vertex transitStop = vertices2.get(0);
            int altCost = 0;
            double altDist = 0.0;

            Util.addTextView(new SpannableString(Labels.routeNum(getActivity(), language, i) + ":"),
                    singleRouteLayout, 24, false, textColor, getActivity());
            Util.addTextView(new SpannableString(Labels.travel(getActivity(), language, 1)),
                    singleRouteLayout, 20, false, textColor, getActivity());

            double d1 = imp.getRouteDistance(vertices1);
            if (distMin < d1) {
                List<Vertex> t = new ArrayList<>(); t.add(sourceP); t.add(transitStop);
                displayTravel = Util.displayTravelText(getActivity(), t, d1, imp.getRouteCost(d1), false, language, showFare);
                altCost += imp.getRouteCost(d1); altDist += d1;
            } else {
                List<Vertex> t = new ArrayList<>(); t.add(sourceP); t.add(destP);
                displayTravel = Util.displayTravelText(getActivity(), t, d1, 0, true, language, showFare);
                altDist += d1;
            }
            Util.addTextView(displayTravel, singleRouteLayout, 16, false, textColor, getActivity());

            double d2 = imp.getRouteDistance(vertices2);
            Util.addTextView(new SpannableString(Labels.travel(getActivity(), language, 2)),
                    singleRouteLayout, 20, false, textColor, getActivity());
            if (distMin < d2) {
                List<Vertex> t = new ArrayList<>(); t.add(transitStop); t.add(destP);
                displayTravel = Util.displayTravelText(getActivity(), t, d2, imp.getRouteCost(d2), false, language, showFare);
                altCost += imp.getRouteCost(d2); altDist += d2;
            } else {
                List<Vertex> t = new ArrayList<>(); t.add(transitStop); t.add(destP);
                displayTravel = Util.displayTravelText(getActivity(), t, d2, 0, true, language, showFare);
                altDist += d2;
            }
            Util.addTextView(displayTravel, singleRouteLayout, 16, false, textColor, getActivity());

            String summary = Labels.totalDistance(getActivity(), language, altDist);
            if (showFare) summary += "\n" + Labels.totalCost(getActivity(), language, altCost);
            Util.addTextView(new SpannableString(summary), singleRouteLayout, 16, true, textColor, getActivity());

            viewDetailTemplate = (TextView) View.inflate(getActivity(), R.layout.view_detail_textview, null);
            viewDetailTemplate.setText(Labels.viewDetail(getActivity(), language));
            viewDetailTemplate.setTag(routeData);
            viewDetailTemplate.setOnClickListener(SearchRouteFragment.this);
            singleRouteLayout.addView(viewDetailTemplate);

            if (loopVar < size - 1) {
                displayTextView = new TextView(getActivity());
                displayTextView.setText("");
                displayTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
                displayTextView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.detailBackground));
                displayTextView.setTypeface(Typeface.DEFAULT_BOLD);
                displayTextView.setTextSize((int) (2 * scale + 0.5f));
                displayTextView.setPadding(2, 0, 2, 0);
                singleRouteLayout.addView(displayTextView);
            }
            singleRouteLayout.setVisibility(View.VISIBLE);
            singleRouteLayoutMain.setVisibility(View.VISIBLE);
            if (animEnabled) {
                Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_down);
                anim.setDuration(ANIM_SLIDE_MS);
                anim.setStartOffset(ANIM_SLIDE_MS);
                singleRouteLayoutMain.startAnimation(anim);
            }
            i++;
            loopVar++;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getView() != null) {
                // your code goes here
                source.requestFocus();
                //  imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                if (displayFlag == 1) {
                    language = Integer.parseInt(prefs.getString("language", "1"));
                    setDisplayText();
                    setDisplayViewText();
                } else {
                    imm.showSoftInput(source, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 200 && requestCode == 100) {
            if (gpsOrigin.equals("source")) {
                String vName = data.getStringExtra("vName");
                gpsToggle.setImageResource(R.drawable.gpsselected);
                source.setText(vName);
//                source.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
//                source.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorPrimaryDark));
                source.setEnabled(false);
                clearSource.setVisibility(View.INVISIBLE);
                destination.requestFocus();
                sourceSelected = true;
//            InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(
//                    Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(destination, InputMethodManager.SHOW_IMPLICIT);
            } else if (gpsOrigin.equals("destination")) {
                String vName = data.getStringExtra("vName");
                gpsToggleDest.setImageResource(R.drawable.gpsselected);
                destination.setText(vName);
                destination.setEnabled(false);
                source.requestFocus();
//            InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(
//                    Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(source, InputMethodManager.SHOW_IMPLICIT);
            }

        } else if (resultCode == 300 && requestCode == 100) {

        }
    }

    @SuppressLint("MissingPermission")
    private void startFetchCoordinates() {
        LocationManager mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        cri = new Criteria();
        cri.setSpeedRequired(false);
        cri.setBearingRequired(false);
        cri.setAltitudeRequired(false);
        provider = mLocationManager.getBestProvider(cri, false);
        mlocationNew = null;
        try {
            mlocationNew = mLocationManager.getLastKnownLocation(provider);
            if (mlocationNew != null) mlocation = mlocationNew;
            mlocationNew = null;
        } catch (Exception ex) { /* ignore */ }

        ProgressDialog progDailog = new ProgressDialog(getActivity());
        progDailog.setMessage(Labels.detectingLocation(getActivity()));
        progDailog.setIndeterminate(false);
        progDailog.setCancelable(true);
        progDailog.show();

        Handler handler = new Handler(Looper.getMainLooper());
        LocationListener[] listenerRef = new LocationListener[1];

        Runnable onComplete = () -> {
            try { mLocationManager.removeUpdates(listenerRef[0]); } catch (Exception ignored) {}
            progDailog.dismiss();
            if (mlocation != null) {
                lat = mlocation.getLatitude();
                longi = mlocation.getLongitude();
                Queue<Vertex> sourceV = imp.getNearestStop(lat, longi);
                List<Vertex> vList = new ArrayList<>();
                int a = 0;
                while (a < 4) {
                    if (sourceV.isEmpty()) break;
                    Vertex v = sourceV.poll();
                    if (v.getDistanceFromSource() < 1.0) { vList.add(v); a++; }
                }
                Intent intent = new Intent(getActivity().getApplicationContext(), NearestStopSelection.class);
                intent.putExtra("data", new DataWrapper(vList));
                startActivityForResult(intent, 100);
            } else {
                Toast.makeText(getActivity(), Labels.locationNotFound(getActivity()), Toast.LENGTH_LONG).show();
            }
        };

        listenerRef[0] = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mlocationNew = location;
                if (!isBetterLocation(mlocationNew, mlocation)) mlocation = mlocationNew;
                lat = location.getLatitude();
                longi = location.getLongitude();
                handler.removeCallbacksAndMessages(null);
                handler.post(onComplete);
            }
            @Override public void onProviderDisabled(String p) { Log.i("OnProviderDisabled", "OnProviderDisabled"); }
            @Override public void onProviderEnabled(String p) { Log.i("onProviderEnabled", "onProviderEnabled"); }
            @Override public void onStatusChanged(String p, int st, Bundle extras) { Log.i("onStatusChanged", "onStatusChanged"); }
        };

        try {
            mLocationManager.requestLocationUpdates(provider, 120000, 500, listenerRef[0]);
        } catch (Exception ex) { /* ignore */ }

        handler.postDelayed(onComplete, 10000);

        progDailog.setOnCancelListener(d -> {
            handler.removeCallbacksAndMessages(null);
            try { mLocationManager.removeUpdates(listenerRef[0]); } catch (Exception ignored) {}
            Toast.makeText(getActivity().getApplicationContext(), Labels.locationCancelled(getActivity()), Toast.LENGTH_SHORT).show();
            gpsFlag = 0;
        });
    }

    public class CustomAutoCompleteTextChangedListener implements TextWatcher {


        public static final String TAG = "CustomAutoCompleteTextChangedListener.java";
        Context context;
        int id;

        public CustomAutoCompleteTextChangedListener(Context context, int id) {
            this.context = context;
            this.id = id;
        }

        @Override
        public void afterTextChanged(Editable s) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence userInput, int start, int before, int count) {
            findPath();
            displayFlag = 0;
            String query = userInput.toString();
            executor.execute(() -> {
                List<Vertex> vertexes = getItemsFromDb(query);
                mainHandler.post(() -> {
                    item.clear();
                    itemId.clear();
                    if (vertexes.isEmpty()) {
                        if (id == source.getId()) source.setError("No suggestion found");
                        else destination.setError("No suggestion found");
                    } else {
                        for (Vertex v : vertexes) {
                            item.add(language == 1 ? v.getName() : v.getNameNepali());
                        }
                    }
                    myAdapter.notifyDataSetChanged();
                    myAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, item);
                    if (id == source.getId()) {
                        sourceSelected = false;
                        source.setAdapter(myAdapter);
                        clearSource.setVisibility(source.getText().toString().isEmpty() ? View.INVISIBLE : View.VISIBLE);
                    } else {
                        destinationSelected = false;
                        destination.setAdapter(myAdapter);
                        clearDestination.setVisibility(destination.getText().toString().isEmpty() ? View.INVISIBLE : View.VISIBLE);
                    }
                });
            });
        }


    }


    @Override
    public void onResume() {
        super.onResume();
        language = Integer.parseInt(prefs.getString("language", "1"));
        setDisplayViewText();
        if (displayFlag == 1) {
            sv.smoothScrollTo(0, 0);
            setDisplayText();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
    }

    private void setDisplayViewText() {
        source.setHint(Labels.searchSource(getActivity(), language));
        destination.setHint(Labels.searchDestination(getActivity(), language));
    }


    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }





    


    public void findPath() {
        String sourceString = source.getText().toString();
        String destString = destination.getText().toString();
        distMin = Double.parseDouble(prefs.getString("walkingDist", "0.5"));
        if (sourceString.isEmpty() || destString.isEmpty()) return;
        executor.execute(() -> {
            Database db = new Database(getActivity());
            Vertex src = db.getVertexDetail(sourceString);
            Vertex dst = db.getVertexDetail(destString);
            if (src.getId() != -1 && dst.getId() != -1 && !src.equals(dst)) {
                mainHandler.post(() -> {
                    pathFound = true;
                    sv.smoothScrollTo(0, 0);
                    sv.setVisibility(View.GONE);
                    ViewDetailSingle.setVisibility(View.GONE);
                    singleRouteLayout.setVisibility(View.GONE);
                    singleRouteLayoutMain.setVisibility(View.GONE);
                    ViewDetail.setVisibility(View.INVISIBLE);
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                    srcId = src.getId();
                    destId = dst.getId();
                    runCalculatePath();
                });
            }
        });
    }

}
