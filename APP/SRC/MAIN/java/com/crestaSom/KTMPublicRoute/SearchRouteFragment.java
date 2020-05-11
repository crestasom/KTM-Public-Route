package com.crestaSom.KTMPublicRoute;


import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.crestaSom.KTMPublicRoute.data.DataWrapper;
import com.crestaSom.KTMPublicRoute.util.Util;
import com.crestaSom.autocomplete.CustomAutoCompleteView;
import com.crestaSom.database.Database;
import com.crestaSom.implementation.KtmPublicRoute;
import com.crestaSom.model.Edge;
import com.crestaSom.model.Route;
import com.crestaSom.model.RouteData;
import com.crestaSom.model.RouteDataWrapper;
import com.crestaSom.model.Vertex;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */

@ReportsCrashes(
        mailTo = "ktmpublicroute@gmail.com",
        reportType = HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.POST,
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PACKAGE_NAME,
                ReportField.REPORT_ID,
                ReportField.BUILD,
                ReportField.STACK_TRACE
        },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.app_name
)
public class SearchRouteFragment extends Fragment implements View.OnClickListener {



    int language;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private int textColor;
    double[] distanceList;
    SharedPreferences prefs;
    ProgressDialog dialog;
    List<Vertex> path;
    Queue<RouteDataWrapper> altPathSingleTransit;
    List<RouteDataWrapper> transferRouteData;
    List<RouteData> tranPath;
    List<RouteData> singlePaths;
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String KEY = "flag";
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    TextView viewDetailTemplate;
    Button viewMap;
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
    CheckBox gps;
    int startFlag;
    int count = 0;
    private String display, displaySingle = "";
    // adapter for auto-complete
    public ArrayAdapter<String> myAdapter;
    int displayFlag = 0;
    boolean sourceSelected = false, destinationSelected = false, pathFound = false;
    InputMethodManager imm;
    ImageView gpsToggle, clearSource, clearDestination, gpsToggleDest, swapText;
    int gpsFlag = 0, gpsFlagDest = 0;
    // just to add some initial value
    public List<String> item = new ArrayList<String>();
    public List<Integer> itemId = new ArrayList<Integer>();
    TextView tv, singleRoute, ViewDetail, ViewDetailSingle, displayTextView;
    EditText source1, dest;
    Button submit;
    Button b, next;
    int srcId, destId;
    KtmPublicRoute imp;
    boolean checkNw = false;
    ScrollView sv;
    SpannableString displayTravel = null;

    int mark1, mark2, mark3, mark4;
    LinearLayout shortestRouteLayout, singleRouteLayout, singleRouteLayoutMain, shortestRouteLayoutMain;


    public SearchRouteFragment() {
        // Required empty public constructor
    }


    public void setUIElements(View v) {

        source = (CustomAutoCompleteView) v.findViewById(R.id.editSource);
        destination = (CustomAutoCompleteView) v.findViewById(R.id.editDestination);
        swapText = (ImageView) v.findViewById(R.id.swapText);
        gpsToggle = (ImageView) v.findViewById(R.id.gpslocation);
        myAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_dropdown_item_1line, item);
        textColor = getResources().getColor(R.color.colorPrimaryDark);
        path = new LinkedList<Vertex>();
        path1 = new ArrayList<Vertex>();
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
        source.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                srcId = -1;
                return false;
            }
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
        destination.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                destId = -1;
                return false;
            }
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
                        submit.requestFocus();
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

/* check if enabled and if not send user to the GSP settings
Better solution would be to display a dialog and suggesting to
 go to the settings */
                if (!enabled) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    Toast.makeText(getActivity().getApplicationContext(), "Please Enable Location", Toast.LENGTH_SHORT).show();
                    startActivity(intent);

                } else if (gpsFlag == 0) {
                    gpsOrigin = "source";
                    gpsFlag = 1;
                    final SearchRouteFragment.FetchCordinates getCord = new SearchRouteFragment.FetchCordinates();
                    getCord.execute();
                    new CountDownTimer(10000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            // Do nothing
                            ////Log.d("Time left:", millisUntilFinished + "");
                        }

                        public void onFinish() {

                            if (getCord.getStatus() == AsyncTask.Status.RUNNING) {
                                ////Log.d("Asnc Task canceal", "true");
                                getCord.cancel(true);
                            }
                        }
//
                    }.start();
//
                } else {
                    gpsFlag = 0;
                    source.setText("");
                    source.setEnabled(true);
//                        source.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
//                        source.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
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


    public String findShortestPath(int srcId, int destId) {

        String display = "";
        Database db = new Database(getActivity().getApplicationContext());
        new ArrayList<Route>();

        new ArrayList<Edge>();
        new ArrayList<Vertex>();

        Vertex source = null, dest = null;
        source = db.getVertex(srcId);
        dest = db.getVertex(destId);
        path.clear();

        path = imp.findShortestPath(source, dest);
        path1.clear();
        path1.addAll(path);

        return display;


    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        language = Integer.parseInt(prefs.getString("language", "1"));
        if (v.getId() == R.id.viewDetailRoute) {
            Intent i = new Intent(getActivity().getApplicationContext(), DetailActivity.class);
            i.putExtra("data", new DataWrapper(path1));
            i.putExtra("flag", false);
            i.putExtra("distanceList", distanceList);

            startActivity(i);

        } else if (v.getTag().equals("View Detail")) {
            Intent i = new Intent(getActivity().getApplicationContext(), DetailActivity.class);
            for (int a = 0; a < count; a++) {
                if (v.getId() == 1000 * a) {
                    i.putExtra("data", new DataWrapper(tranPath.get(a).getvList()));
                }
            }
            i.putExtra("flag", false);
            i.putExtra("distanceList", distanceList);
            startActivity(i);
        } else if (v.getTag().equals("View Detail Alternative")) {
            Intent i = new Intent(getActivity().getApplicationContext(), DetailActivity.class);
            for (int a = 0; a < count; a++) {
                if (v.getId() == 1000 * a) {
                    i.putExtra("data", transferRouteData.get(a));
                    ////Log.d("Data Transferred", transferRouteData.get(a).toString());
                }
            }

            i.putExtra("flag", false);
            i.putExtra("flagAlt", true);
            i.putExtra("distanceList", distanceList);
            ////Log.d("data in search route", singleRouteVertex.toString());

            // i.putParcelableArrayListExtra("path", (ArrayList<Vertex>)
            // path);
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
        // add items on the array dynamically
        Database db = new Database(getActivity());
        List<Vertex> vertexes = new ArrayList<Vertex>();
        ////Log.d("Database", db.toString());
        vertexes = db.getVertexUsingQuery(searchTerm);
        itemId.clear();
        for (Vertex v : vertexes) {
            itemId.add(v.getId());
        }
        return vertexes;

    }

    class CalculatePath extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // new MyCustomProgressDialog(getApplicationContext());
            pDialog = new ProgressDialog(getActivity());
            pDialog.setIcon(R.drawable.find);
            if (language == 1)
                pDialog.setMessage("Detecting Path...");
            else
                pDialog.setMessage("रुटहरु खोजी हुँदैछ...");
            pDialog.setCancelable(false);
            pDialog.setIndeterminate(false);
            //findShortestPath(srcId,destId);
            shortestRouteLayout.removeAllViews();
            singleRouteLayout.removeAllViews();
            pDialog.show();

            // dialog = MyCustomProgressDialog.ctor(getApplicationContext());
            // dialog.show();
        }

        @Override
        protected String doInBackground(String... arg0) {
            Database db = new Database(getActivity());
            display = findShortestPath(srcId, destId);
            Vertex sourceP = db.getVertex(srcId);
            Vertex destP = db.getVertex(destId);
            singlePaths = imp.getSingleRoutes(sourceP, destP);
            if (singlePaths.isEmpty()) {
                altPathSingleTransit = imp.getAlternativeRouteOneTransit(sourceP, destP);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            // dialog.dismiss();
            displayFlag = 1;
            setDisplayText();
        }

    }

    private void setDisplayText() {
        Log.d("path", path.toString());
        sv.smoothScrollTo(0, 0);
        shortestRouteLayout.removeAllViews();
        singleRouteLayout.removeAllViews();
        display = "";
        displaySingle = "";
        Database db = new Database(getActivity());
        Vertex sourceP = db.getVertex(srcId);
        Vertex destP = db.getVertex(destId);
        pDialog.dismiss();
        List<Vertex> vertexList = new ArrayList<Vertex>();
        sv.setVisibility(View.VISIBLE);
        Route r;


        int i = 0;
        int totalCost = 0;
        double totalDist = 0;
        List<Integer> routeIds = new ArrayList<Integer>();
        int pixels;
        final float scale = getActivity().getResources().getDisplayMetrics().density;
        Map<List<Integer>, List<Vertex>> pathRoute;
        i = 1;
        int temp = 0;
        if (language == 1)
            display += "Shortest Route:";
        else
            display += "छोटो रुट:";
        singleRoute = new TextView(getActivity());
        singleRoute.setText(display);
        singleRoute.setPadding(2, 10, 2, 0);
        singleRoute.setGravity(Gravity.CENTER_HORIZONTAL);
        singleRoute.setTypeface(Typeface.DEFAULT_BOLD);
        pixels = (int) (15 * scale + 0.5f);
        singleRoute.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        //singleRoute.setTextSize(pixels);
        singleRoute.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        shortestRouteLayout.addView(singleRoute);
//            addViewAnimation(singleRoute);
        List<Vertex> pathTemp = new ArrayList<>();
        pathTemp.addAll(path);
        distanceList = new double[10];
        while (!pathTemp.isEmpty()) {
            if (pathTemp.size() == 1) {
                break;
            }
            pathRoute = imp.findRoutePath(pathTemp);
            ////Log.d("path Test", path.toString());
            Iterator<Map.Entry<List<Integer>, List<Vertex>>> it = pathRoute.entrySet().iterator();


            while (it.hasNext()) {
                Map.Entry<List<Integer>, List<Vertex>> pair = it.next();
                routeIds = pair.getKey();
                vertexList = pair.getValue();
                display = "";
                if (language == 1)
                    display += "Travel " + i;
                else
                    display += "यात्रा " + Util.convertNepali(i);
                Util.addTextView(new SpannableString(display), shortestRouteLayout, 24, false, textColor,getActivity());
                double d = imp.getRouteDistance(vertexList);
                distanceList[temp++] = d;
                SpannableString travelSource = new SpannableString(vertexList.get(0).toString());
                travelSource.setSpan(new StyleSpan(Typeface.BOLD), 0, travelSource.length(), 0);
                SpannableString travelDest = new SpannableString(vertexList.get(vertexList.size() - 1).toString());
                travelDest.setSpan(new StyleSpan(Typeface.BOLD), 0, travelDest.length(), 0);
                if (distMin < d) {
                    int fare = imp.getRouteCost(d);
                    displayTravel = Util.displayTravelText(vertexList, d, fare, false, language);
                    i++;
                    totalCost += fare;
                    totalDist += d;
                } else {
                    displayTravel = Util.displayTravelText(vertexList, d, 0, true, language);
                    totalDist += d;
                    i++;
                }
                display += "\n";
                Util.addTextView(displayTravel, shortestRouteLayout, 16, false, textColor,getActivity());
            }
        }
        display = "";
        if (language == 2) {
            display += "पूरा दुरी: " + Util.convertNumberToNepali(new DecimalFormat("#.##").format(totalDist)) + " कि.मी.";
            display += "\nभाडा रु. " + Util.convertNumberToNepali(totalCost);
            ;
        } else {
            display += "Total Distance: " + new DecimalFormat("#.##").format(totalDist) + " km";
            display += "\nTotal Cost: Rs. " + totalCost;
        }
        Util.addTextView(new SpannableString(display), shortestRouteLayout, 16, true, textColor,getActivity());
        if (language == 2)
            ViewDetail.setText("[थप जानकारी]");
        else if (language == 1)
            ViewDetail.setText("[View Detail]");
//

        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_down);
        animation.setDuration(1000);
        shortestRouteLayoutMain.setVisibility(View.VISIBLE);
        shortestRouteLayoutMain.startAnimation(animation);
        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein);
        animation.setDuration(1500);
        animation.setStartOffset(0);
        ViewDetail.setVisibility(View.VISIBLE);
        ViewDetail.startAnimation(animation);

        singleRouteVertex = new ArrayList<Vertex>();
        displaySingle = "";

        if (!singlePaths.isEmpty()) {
            tranPath = new ArrayList<>();
            int loopVar = 0;
            i = 1;
            if (language == 1)
                displaySingle += "Direct Route:";
            else
                displaySingle += "सिधा रुट:";
            singleRoute = new TextView(getActivity());
            singleRoute.setGravity(Gravity.CENTER_HORIZONTAL);
            singleRoute.setTypeface(Typeface.DEFAULT_BOLD);
            singleRoute.setText(displaySingle);
            pixels = (int) (15 * scale + 0.5f);
            int size = singlePaths.size();
            singleRoute.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            //singleRoute.setTextSize(pixels);
            singleRoute.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            singleRoute.setPadding(2, 2, 2, 0);
            singleRouteLayout.addView(singleRoute);
            for (RouteData dw : singlePaths) {
                singleRouteVertex = dw.getvList();
                double d1 = imp.getRouteDistance(singleRouteVertex);
                int fare1 = imp.getRouteCost(d1);
                List<Vertex> tempList = new ArrayList<>();
                tempList.add(sourceP);
                tempList.add(destP);
                if (!singleRouteVertex.equals(path1) && d1 < totalDist + 8.0) {
                    displaySingle = "";
                    if (language == 1)
                        displaySingle += "Route " + i;
                    else
                        displaySingle += "रुट " + Util.convertNumberToNepali(i) + ":";
                    Util.addTextView(new SpannableString(displaySingle), singleRouteLayout, 24, false, textColor,getActivity());
                    display = "";
                    fare1 = imp.getRouteCost(d1);
                    displayTravel = Util.displayTravelText(tempList, d1, fare1, false, language);
                    singleRouteLayout.setVisibility(View.VISIBLE);
                    singleRouteLayoutMain.setVisibility(View.VISIBLE);
                    Util.addTextView(displayTravel, singleRouteLayout, 16, false, textColor,getActivity());
                    if (language == 1) {
                        displaySingle = "Available Route:";
                    } else {
                        displaySingle = "उपलब्ध रुटहरु:";
                    }
                    Util.addTextView(new SpannableString(displaySingle), singleRouteLayout, 20, true, textColor,getActivity());
                    displaySingle = "";
                    if (language == 1)
                        displaySingle += dw.getrName();
                    else
                        displaySingle += dw.getrNameNepali();
                    Util.addTextView(new SpannableString(displaySingle), singleRouteLayout, 16, false, textColor,getActivity());
                    viewDetailTemplate = (TextView) View.inflate(getActivity(), R.layout.view_detail_textview, null);
                    viewDetailTemplate.setId(1000 * (i - 1));
                    if (language == 2)
                        viewDetailTemplate.setText("[थप जानकारी]");
                    viewDetailTemplate.setTag("View Detail");
                    viewDetailTemplate.setOnClickListener(SearchRouteFragment.this);
                    singleRouteLayout.addView(viewDetailTemplate);

                    if (loopVar < size - 1) {
                        displaySingle = "";
                        displayTextView = new TextView(getActivity());
                        displayTextView.setText(displaySingle);
                        displayTextView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                        displayTextView.setBackgroundColor(getResources().getColor(R.color.detailBackground));
                        displayTextView.setTypeface(Typeface.DEFAULT_BOLD);
                        pixels = (int) (2 * scale + 0.5f);
                        displayTextView.setTextSize(pixels);
                        displayTextView.setPadding(2, 0, 2, 0);
                        singleRouteLayout.addView(displayTextView);
                    }

                    animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_down);
                    animation.setDuration(1000);
                    animation.setStartOffset(1000);
                    singleRouteLayoutMain.setVisibility(View.VISIBLE);
                    singleRouteLayoutMain.startAnimation(animation);

                    animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein);
                    animation.setDuration(1000);
                    animation.setStartOffset(1000);

                    ViewDetailSingle.startAnimation(animation);
                    tranPath.add(dw);

                    i++;
                }
                loopVar++;
            }
            count = i;
        } else {

            if (!altPathSingleTransit.isEmpty()) {
                int size = altPathSingleTransit.size();
                Queue<RouteDataWrapper> tempRouteDataWrapper = new PriorityQueue<>();
                tempRouteDataWrapper.addAll(altPathSingleTransit);
                i = 1;
                count = 1;
                int loopVar = 0;
                if (language == 1)
                    displaySingle += "Alternative Route:";
                else {
                    displaySingle += "बैकल्पिक रुटहरु:";
                }
                singleRoute = new TextView(getActivity());
                singleRoute.setGravity(Gravity.CENTER_HORIZONTAL);
                singleRoute.setTypeface(Typeface.DEFAULT_BOLD);
                singleRoute.setText(displaySingle);
                pixels = (int) (15 * scale + 0.5f);
                singleRoute.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                singleRoute.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
                singleRoute.setPadding(2, 2, 2, 0);
                singleRouteLayout.addView(singleRoute);
                transferRouteData = new ArrayList<>();
                for (RouteDataWrapper routeData : altPathSingleTransit) {
                    if ((routeData.getDistTotal() != totalDist) && (routeData.getDistTotal() < (totalDist + 6))) {

                        transferRouteData.add(routeData);
                        List<Vertex> vertices1 = routeData.getRouteData1().get(0).getvList();
                        List<Vertex> vertices2 = routeData.getRouteData2().get(0).getvList();
                        Vertex transitStop = vertices2.get(0);
                        totalCost = 0;
                        totalDist = 0.0;
                        //display first transit
//                        Log.d("vertices1",vertices1.toString());
//                        Log.d("vertices2",vertices2.toString());
                        displaySingle = "";
                        if (language == 1)
                            displaySingle += "Route " + i + ":";
                        else
                            displaySingle += "रुट " + Util.convertNepali(i);
                        Util.addTextView(new SpannableString(displaySingle), singleRouteLayout, 24, false, textColor,getActivity());

                        display = "";
                        if (language == 1)
                            display += "Travel " + 1;
                        else
                            display += "यात्रा " + Util.convertNepali(1);
                        Util.addTextView(new SpannableString(display), singleRouteLayout, 20, false, textColor,getActivity());
//
                        double d1 = imp.getRouteDistance(vertices1);
                        int fare1 = imp.getRouteCost(d1);

                        if (distMin < d1) {
                            List<Vertex> tempList = new ArrayList<>();
                            tempList.add(sourceP);
                            tempList.add(transitStop);
                            displayTravel = Util.displayTravelText(tempList, d1, fare1, false, language);
                            totalCost += fare1;
                            totalDist += d1;

                        } else {
                            List<Vertex> tempList = new ArrayList<>();
                            tempList.add(sourceP);
                            tempList.add(destP);
                            displayTravel = Util.displayTravelText(tempList, d1, 0, true, language);
                            totalDist += d1;

                        }
                        Util.addTextView(displayTravel, singleRouteLayout, 16, false, textColor,getActivity());
                        Log.d("d1", "" + d1);
                        Log.d("Total Distance here", "" + totalDist);
                        //display second transit
                        double d2 = imp.getRouteDistance(vertices2);
                        int fare2 = imp.getRouteCost(d2);

                        display = "";
                        if (language == 1)
                            display += "Travel " + 2;
                        else
                            display += "यात्रा " + Util.convertNepali(2);
                        Util.addTextView(new SpannableString(display), singleRouteLayout, 20, false, textColor,getActivity());

                        if (distMin < d2) {
                            List<Vertex> tempList = new ArrayList<>();
                            tempList.add(transitStop);
                            tempList.add(destP);
                            displayTravel = Util.displayTravelText(tempList, d2, fare2, false, language);
                            totalCost += fare2;
                            totalDist += d2;
                        } else {
                            List<Vertex> tempList = new ArrayList<>();
                            tempList.add(transitStop);
                            tempList.add(destP);
                            displayTravel = Util.displayTravelText(tempList, d2, 0, true, language);
                            totalDist += d2;

                        }
                        Util.addTextView(displayTravel, singleRouteLayout, 16, false, textColor,getActivity());
                        Log.d("d1", "" + d2);
                        Log.d("Total Distance here", "" + totalDist);
                      /*  double totalDist1 = d1 + d2;
                        int totalCost1 = fare1 + fare2;
*/
                        display = "";
                        if (language == 2) {
                            display += "पूरा दुरी: " + Util.convertNumberToNepali(new DecimalFormat("#.##").format(totalDist)) + " कि.मी.";
                            display += "\nभाडा रु. " + Util.convertNumberToNepali(totalCost);
                            ;
                        } else {
                            display += "Total Distance: " + new DecimalFormat("#.##").format(totalDist) + " km";
                            display += "\nTotal Cost: Rs. " + totalCost;
                        }
                        Util.addTextView(new SpannableString(display), singleRouteLayout, 16, true, textColor,getActivity());
//                            displaySingle = "";
//                            displaySingle += "Total Distance: " + new DecimalFormat("#.##").format(totalDist1) + " km";
//                            displaySingle += "\nTotal Cost: Rs. " + totalCost1;
//                            addTextView(new SpannableString(displaySingle), singleRouteLayout, 16, true, textColor);

                        viewDetailTemplate = (TextView) View.inflate(getActivity(), R.layout.view_detail_textview, null);
                        viewDetailTemplate.setId(1000 * (i - 1));
                        if (language == 2)
                            viewDetailTemplate.setText("[थप जानकारी]");
                        viewDetailTemplate.setTag("View Detail Alternative");
                        viewDetailTemplate.setOnClickListener(SearchRouteFragment.this);
                        singleRouteLayout.addView(viewDetailTemplate);
                        if (loopVar < size - 1) {
                            displaySingle = "";
                            displayTextView = new TextView(getActivity());
                            displayTextView.setText(displaySingle);
                            displayTextView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                            displayTextView.setBackgroundColor(getResources().getColor(R.color.detailBackground));
                            displayTextView.setTypeface(Typeface.DEFAULT_BOLD);
                            pixels = (int) (2 * scale + 0.5f);
                            displayTextView.setTextSize(pixels);
                            displayTextView.setPadding(2, 0, 2, 0);
                            singleRouteLayout.addView(displayTextView);
                        }
                        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_down);
                        animation.setDuration(1000);
                        animation.setStartOffset(1000);
                        singleRouteLayout.setVisibility(View.VISIBLE);
                        singleRouteLayoutMain.setVisibility(View.VISIBLE);
                        singleRouteLayoutMain.startAnimation(animation);
                        // ViewDetailSingle.setVisibility(View.VISIBLE);

                        animation = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein);
                        animation.setDuration(1000);
                        animation.setStartOffset(1000);
                        i++;
                    }
                    loopVar++;
                }
                count = i;
            }
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
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 200 && requestCode == 100) {
            if (gpsOrigin.equals("source")) {
                String vName = data.getStringExtra("vName");
                gpsToggle.setImageResource(R.drawable.gpsselected);
                source.setText(vName);
//                source.setTextColor(getResources().getColor(R.color.colorPrimary));
//                source.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
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

    public class FetchCordinates extends AsyncTask<String, Integer, String> {
        ProgressDialog progDailog = null;
        Boolean running = true;

//        public double lati = 0.0;
//        public double longi = 0.0;

        public LocationManager mLocationManager;
        public SearchRouteFragment.FetchCordinates.VeggsterLocationListener mVeggsterLocationListener;


        @Override
        protected void onPreExecute() {
            mVeggsterLocationListener = new SearchRouteFragment.FetchCordinates.VeggsterLocationListener();
            mlocationNew = null;
            mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            cri = new Criteria();
            cri.setSpeedRequired(false);
            cri.setBearingRequired(false);
            //cri.setPowerRequirement(Criteria.POWER_MEDIUM);
            cri.setAltitudeRequired(false);
//            cri.setAccuracy(Criteria.ACCURACY_COARSE);
            // cri.setHorizontalAccuracy(Criteria.ACCURACY_FINE);
            provider = mLocationManager.getBestProvider(cri, false);

            //cri.setBearingAccuracy(Criteria.ACCURACY_LOW);


            //cri.setHorizontalAccuracy(1000);

//            if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //	Toast.makeText(MainActivity.this,"Network Mode:"+LocationManager.NETWORK_PROVIDER.toString(),Toast.LENGTH_SHORT).show();

            try {
                mLocationManager.requestLocationUpdates(provider, 120000, 500,
                        mVeggsterLocationListener);
                mlocationNew = mLocationManager.getLastKnownLocation(provider);
                if (mlocationNew != null) {
                    mlocation = mlocationNew;
                }
                mlocationNew = null;
            } catch (Exception ex) {
                ////Log.d("Exception", ex.getMessage());
            }

//                mLocationManager.requestSingleUpdate(provider, mVeggsterLocationListener, null);
            //mLocationManager.

            // mlocation = mLocationManager.getLastKnownLocation(provider);
            //SingleShot

//                    lat=mlocation.getLatitude();
//                    longi=mlocation.getLongitude();


//            } else {
//                cri.setHorizontalAccuracy(Criteria.ACCURACY_FINE);
//                provider = mLocationManager.getBestProvider(cri, false);
            //	Toast.makeText(MainActivity.this,"GPS Mode",Toast.LENGTH_SHORT).show();
//                mLocationManager.requestLocationUpdates(provider, 300000, 0,
//                        mVeggsterLocationListener);
//                mlocation=mLocationManager.getLastKnownLocation(provider);
//                provider = mLocationManager.getBestProvider(cri, false);
//                mLocationManager.requestSingleUpdate(provider, mVeggsterLocationListener, null);
            // mlocation = mLocationManager.getLastKnownLocation(provider);

//                lat=mlocation.getLatitude();
//                longi=mlocation.getLongitude();

//            }
//            lat=27.688653;
//            longi=85.481674;

            progDailog = new ProgressDialog(getActivity());
//            progDailog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//            dialog.setContentView(R.layout.dialog_login);
//            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            progDailog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    SearchRouteFragment.FetchCordinates.this.cancel(true);
                }
            });
            progDailog.setMessage("Detecting your current location... \n(It will only take upto 10 seconds...)");
            progDailog.setIndeterminate(false);
            progDailog.setCancelable(true);
            progDailog.show();

        }

        @Override
        protected void onCancelled() {
            ////Log.d("Cancel message", "Cancelled by user!");
            Toast.makeText(getActivity().getApplicationContext(), "Location Detection Cancel", Toast.LENGTH_SHORT).show();
            progDailog.dismiss();
            running = false;
            this.cancel(true);
            //mLocationManager.removeUpdates(mVeggsterLocationListener);
        }

        @Override
        protected void onPostExecute(String result) {
            progDailog.dismiss();
            //  ////Log.d("coordinates", lat + longi + "");
            if (!(mlocation == null)) {
                lat = mlocation.getLatitude();
                longi = mlocation.getLongitude();
                Queue<Vertex> sourceV = imp.getNearestStop(lat, longi);
                Vertex v;
                //source.setText(sourceV.getName());
                List<Vertex> vList = new ArrayList<Vertex>();
                int a = 0;
                //for (int i = 0; i < 4; i++) {
                while (a < 4) {
                    if (sourceV.isEmpty()) {
                        break;
                    }
                    v = sourceV.poll();
                    if (v.getDistanceFromSource() < 1.0) {
                        ////Log.d("Polled Vertex", v.getName());
                        vList.add(v);
                        a++;
                    }
                }
                //mLocationManager.removeUpdates(mVeggsterLocationListener);
                ////Log.d("vertex", vList.toString());
                Intent i = new Intent(getActivity().getApplicationContext(), NearestStopSelection.class);
                i.putExtra("data", new DataWrapper(vList));
                startActivityForResult(i, 100);
                //Toast.makeText(MainActivity.this,
//					"LATITUDE :" + lati + " LONGITUDE :" + longi,
//					Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Location not found", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            int x = 0;
            long init = System.currentTimeMillis();
            long fint = init + 10000;
            while (((mlocation == null || mlocationNew == null) && !this.isCancelled()) && (System.currentTimeMillis() < fint)) {
//            while (lat == 0.0 && !this.isCancelled()) {
                //while(mlocation==null  && !this.isCancelled()){
                //while(!this.isCancelled()){

                //while(System.currentTimeMillis()<fint){
////				////Log.d("x:",""+x++);
////				System.out.println("x:"+x++);
//                if(this.isCancelled()){
//                    break;
//                }
//             //Thread.sleep(15000);
            }
            return null;
        }

        public class VeggsterLocationListener implements LocationListener {

            @Override
            public void onLocationChanged(Location location) {


                //if(location.getAccuracy()>mlocation.getAccuracy()) {
                mlocationNew = location;
                if (!isBetterLocation(mlocationNew, mlocation)) {
                    mlocation = mlocationNew;
                }
                //}
//
//                String info = location.getProvider();
                try {

                    // LocatorService.myLatitude=location.getLatitude();

                    // LocatorService.myLongitude=location.getLongitude();

                    lat = location.getLatitude();
                    longi = location.getLongitude();

                } catch (Exception e) {
                    // progDailog.dismiss();
                    // Toast.makeText(getApplicationContext(),"Unable to get Location"
                    // , Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.i("OnProviderDisabled", "OnProviderDisabled");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.i("onProviderEnabled", "onProviderEnabled");
            }

            @Override
            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
                Log.i("onStatusChanged", "onStatusChanged");

            }

        }

    }

    public class FetchCordinatesBackground extends AsyncTask<String, Integer, String> {
        ProgressDialog progDailog = null;
        Boolean running = true;

//        public double lati = 0.0;
//        public double longi = 0.0;

        public LocationManager mLocationManager;
        public SearchRouteFragment.FetchCordinatesBackground.VeggsterLocationListener mVeggsterLocationListener;


        @Override
        protected void onPreExecute() {
            mVeggsterLocationListener = new VeggsterLocationListener();
            mlocationNew = null;
            mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            cri = new Criteria();
            cri.setSpeedRequired(false);
            cri.setBearingRequired(false);
            //cri.setPowerRequirement(Criteria.POWER_MEDIUM);
            cri.setAltitudeRequired(false);
//            cri.setAccuracy(Criteria.ACCURACY_COARSE);
            // cri.setHorizontalAccuracy(Criteria.ACCURACY_FINE);
            provider = mLocationManager.getBestProvider(cri, false);
            try {
                mLocationManager.requestLocationUpdates(provider, 120000, 500,
                        mVeggsterLocationListener);
                mlocationNew = mLocationManager.getLastKnownLocation(provider);
                if (mlocationNew != null) {
                    mlocation = mlocationNew;
                }
                mlocationNew = null;
            } catch (Exception ex) {
                ////Log.d("Exception", ex.getMessage());
            }
            progDailog.setMessage("Detecting your current location... \n(It will only take upto 10 seconds...)");
            progDailog.setIndeterminate(false);
            progDailog.setCancelable(true);
            progDailog.show();

        }

        @Override
        protected void onCancelled() {
            ////Log.d("Cancel message", "Cancelled by user!");
            Toast.makeText(getActivity().getApplicationContext(), "Location Detection Cancel", Toast.LENGTH_SHORT).show();
            progDailog.dismiss();
            running = false;
            this.cancel(true);
            //mLocationManager.removeUpdates(mVeggsterLocationListener);
        }

        @Override
        protected void onPostExecute(String result) {
            progDailog.dismiss();
            //  ////Log.d("coordinates", lat + longi + "");
            if (!(mlocation == null)) {
                lat = mlocation.getLatitude();
                longi = mlocation.getLongitude();
                Queue<Vertex> sourceV = imp.getNearestStop(lat, longi);
                Vertex v;
                //source.setText(sourceV.getName());
                List<Vertex> vList = new ArrayList<Vertex>();
                int a = 0;
                //for (int i = 0; i < 4; i++) {
                while (a < 4) {
                    if (sourceV.isEmpty()) {
                        break;
                    }
                    v = sourceV.poll();
                    if (v.getDistanceFromSource() < 1.0) {
                        ////Log.d("Polled Vertex", v.getName());
                        vList.add(v);
                        a++;
                    }
                }
                //mLocationManager.removeUpdates(mVeggsterLocationListener);
                ////Log.d("vertex", vList.toString());
                Intent i = new Intent(getActivity().getApplicationContext(), NearestStopSelection.class);
                i.putExtra("data", new DataWrapper(vList));
                startActivityForResult(i, 100);
                //Toast.makeText(MainActivity.this,
//					"LATITUDE :" + lati + " LONGITUDE :" + longi,
//					Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Location not found", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            int x = 0;
            long init = System.currentTimeMillis();
            long fint = init + 10000;
            while (((mlocation == null || mlocationNew == null) && !this.isCancelled()) && (System.currentTimeMillis() < fint)) {
//            while (lat == 0.0 && !this.isCancelled()) {
                //while(mlocation==null  && !this.isCancelled()){
                //while(!this.isCancelled()){

                //while(System.currentTimeMillis()<fint){
////				////Log.d("x:",""+x++);
////				System.out.println("x:"+x++);
//                if(this.isCancelled()){
//                    break;
//                }
//             //Thread.sleep(15000);
            }
            return null;
        }

        public class VeggsterLocationListener implements LocationListener {

            @Override
            public void onLocationChanged(Location location) {


                //if(location.getAccuracy()>mlocation.getAccuracy()) {
                mlocationNew = location;
                if (!isBetterLocation(mlocationNew, mlocation)) {
                    mlocation = mlocationNew;
                }
                //}
//
//                String info = location.getProvider();
                try {

                    // LocatorService.myLatitude=location.getLatitude();

                    // LocatorService.myLongitude=location.getLongitude();

                    lat = location.getLatitude();
                    longi = location.getLongitude();

                } catch (Exception e) {
                    // progDailog.dismiss();
                    // Toast.makeText(getApplicationContext(),"Unable to get Location"
                    // , Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.i("OnProviderDisabled", "OnProviderDisabled");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.i("onProviderEnabled", "onProviderEnabled");
            }

            @Override
            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
                Log.i("onStatusChanged", "onStatusChanged");

            }

        }

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
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence userInput, int start, int before,
                                  int count) {

            // if you want to see in the logcat what the user types
            //Log.e(TAG, "User input: " + userInput);
            findPath();
            displayFlag = 0;
            List<Vertex> vertexes = getItemsFromDb(userInput
                    .toString());
            // query the database based on the user input

            // if(size==0){
            // item
            // }
            item.clear();
            itemId.clear();
            if (vertexes.size() == 0) {
                if (id == source.getId()) {
                    source.setError("No suggestion found");
                } else {
                    destination.setError("No suggestion found");

                }

            } else {
                for (Vertex v : vertexes) {
                    if (language == 1)
                        item.add(v.getName());
                    else {
                        item.add(v.getNameNepali());
                    }
//                    item.add(v.getName()+" ("+v.getNameNepali()+")");
                    ////Log.d("vertexes", item.toString());
                    //		mainActivity.itemId.add(v.getId());

                }
            }

            // update the adapater

            // mainActivity.myAdapter.
            myAdapter.notifyDataSetChanged();
            myAdapter = new ArrayAdapter<String>(context,
                    android.R.layout.simple_dropdown_item_1line, item);
            if (id == source.getId()) {
                sourceSelected = false;
                source.setAdapter(myAdapter);
                if (source.getText().toString().equals("")) {
                    clearSource.setVisibility(View.INVISIBLE);
                } else {
                    clearSource.setVisibility(View.VISIBLE);
                }
            } else {
                destinationSelected = false;
                destination.setAdapter(myAdapter);
                if (destination.getText().toString().equals("")) {
                    clearDestination.setVisibility(View.INVISIBLE);
                } else {
                    clearDestination.setVisibility(View.VISIBLE);
                }
            }

        }


    }


    @Override
    public void onResume() {
//        destination.clearFocus();
//        source.requestFocus();
        super.onResume();
        language = Integer.parseInt(prefs.getString("language", "1"));
        setDisplayViewText();
        if (displayFlag == 1) {

            sv.smoothScrollTo(0, 0);
            setDisplayText();
        }
    }

    private void setDisplayViewText() {

        if (language == 2) {
            source.setHint("स्रोत");
            destination.setHint("गन्त्यव");
            // submit.setText("रुटहरु खोज्नुहोस्");
        } else {
            source.setHint("Source");
            destination.setHint("Destination");
//            submit.setText("SEARCH ROUTE");
        }
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
        Database db = new Database(getActivity());
        String sourceString = source.getText().toString();
        String destString = destination.getText().toString();
        Vertex src = new Vertex();
        src = db.getVertexDetail(sourceString);
        Vertex dst = new Vertex();
        dst = db.getVertexDetail(destString);
        distMin = Double.parseDouble(prefs.getString("walkingDist", "0.5"));

        if (!sourceString.equals("") && !destString.equals("")&& src.getId() != -1 && dst.getId() != -1 && !src.equals(dst)) {
            pathFound = true;
            sv.smoothScrollTo(0, 0);
            sv.setVisibility(View.GONE);
            ViewDetailSingle.setVisibility(View.GONE);
            singleRouteLayout.setVisibility(View.GONE);
            singleRouteLayoutMain.setVisibility(View.GONE);
            ViewDetail.setVisibility(View.INVISIBLE);
            InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            srcId = src.getId();
            destId = dst.getId();
            new SearchRouteFragment.CalculatePath().execute();
        }
    }

}
