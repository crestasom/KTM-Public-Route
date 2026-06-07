package com.crestaSom.KTMPublicRoute;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.crestaSom.KTMPublicRoute.data.DataWrapper;
import com.crestaSom.KTMPublicRoute.util.Labels;
import com.crestaSom.autocomplete.CustomAutoCompleteView;
import com.crestaSom.database.Database;
import com.crestaSom.implementation.KtmPublicRoute;
import com.crestaSom.model.Route;
import com.crestaSom.model.Vertex;

import java.util.ArrayList;
import java.util.List;


public class ViewRouteFragment extends Fragment {
    ListView routeList;
    List<String> routeName;
    List<Integer> routeId;
    List<Route> routes, routesTemp;
    Database db;
    KtmPublicRoute imp;
    ImageView clearSearchPlace;
    ArrayAdapter<String> ar;
    TextView dp;
    public CustomAutoCompleteView searchPlace;
    public ArrayAdapter<String> myAdapter;
    public List<String> item = new ArrayList<>();
    public List<Integer> itemId = new ArrayList<>();
    SharedPreferences prefs;
    int language, intLang = -1;


    public ViewRouteFragment() {
        // Required empty public constructor
    }




    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getView() != null) {
            InputMethodManager imm = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
            refreshLanguageIfChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshLanguageIfChanged();
    }

    private void refreshLanguageIfChanged() {
        language = Integer.parseInt(prefs.getString("language", "1"));
        if (intLang == -1) {
            intLang = language;
        } else if (intLang != language) {
            routeName.clear();
            routeId.clear();
            for (Route r : routes) {
                routeName.add(language == 1 ? r.getName() : r.getNameNepali());
                routeId.add(r.getId());
            }
            intLang = language;
            setDisplayTextViewText();
            ar = new ArrayAdapter<>(getActivity(), R.layout.mytextview, routeName);
            routeList.setAdapter(ar);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_view_route, container, false);
        routeList = (ListView) root.findViewById(R.id.list);
        searchPlace = (CustomAutoCompleteView) root.findViewById(R.id.searchPlace);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        dp = (TextView) root.findViewById(R.id.textView1);
        intLang = language = Integer.parseInt(prefs.getString("language", "1"));
        clearSearchPlace = (ImageView) root.findViewById(R.id.clearSearchPlace);
        clearSearchPlace.setOnClickListener(v -> {
            searchPlace.setText("");
            routeName.clear();
            routeId.clear();
            for (Route r : routes) {
                routeName.add(language == 1 ? r.getName() : r.getNameNepali());
                routeId.add(r.getId());
            }
            routesTemp.clear();
            routesTemp.addAll(routes);
            ar = new ArrayAdapter<>(getActivity(), R.layout.mytextview, routeName);
            routeList.setAdapter(ar);
            clearSearchPlace.setVisibility(View.INVISIBLE);
            searchPlace.setError(null);
        });
        myAdapter = new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_dropdown_item_1line, item);
        searchPlace.setDropDownBackgroundResource(R.color.dropDownBackground);
        searchPlace.addTextChangedListener(new CustomAutoCompleteTextChangedListener(getActivity().getApplicationContext(), searchPlace.getId()));
        searchPlace.setAdapter(myAdapter);
        searchPlace.setOnKeyListener((v, keyCode, event) -> false);
        searchPlace.setOnItemClickListener((parent, itemView, position, id) ->
                updateRouteList(searchPlace.getText().toString()));


        db=new Database(getActivity());
        routes=db.getAllRoute();

        imp=new KtmPublicRoute(getActivity());
        routeId = new ArrayList<>();
        routeName = new ArrayList<>();

        for(Route r:routes){
            if(language==1)
            routeName.add(r.getName());
            else
                routeName.add(r.getNameNepali());
            routeId.add(r.getId());
        }
        routesTemp=new ArrayList<>();
        routesTemp.addAll(routes);
        ar = new ArrayAdapter<>(getActivity(), R.layout.mytextview, routeName);
        routeList.setAdapter(ar);

        routeList.setOnItemClickListener((parent, itemView, position, id) -> {
            Route selected = routesTemp.get(position);
            List<Integer> vList = selected.getAllVertexes();
            List<Vertex> vLists = new ArrayList<>();
            for (int idr : vList) {
                vLists.add(db.getVertex(idr));
            }
            Intent i = new Intent(getActivity(), DetailActivity.class);
            i.putExtra("data", new DataWrapper(vLists));
            i.putExtra("flag", true);
            i.putExtra("routeName", selected.getName());
            i.putExtra("vehicleType", language == 1
                    ? selected.getVehicleType()
                    : selected.getVehicleTypeNepali());
            startActivity(i);
        });
        setDisplayTextViewText();
        return root;
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

            List<Vertex> vertexes = getItemsFromDb(userInput
                    .toString());
            // query the database based on the user input
            if (searchPlace.getText().toString().equals("")) {
            clearSearchPlace.setVisibility(View.INVISIBLE);
            }else{
                clearSearchPlace.setVisibility(View.VISIBLE);
            }
            item.clear();
            itemId.clear();
            if (vertexes.size() == 0) {
            searchPlace.setError("No suggestion found");

            } else {
                for (Vertex v : vertexes) {
                    if(language==1)
                        item.add(v.getName());
                    else{
                        item.add(v.getNameNepali());
                    }

                }
            }

            myAdapter.notifyDataSetChanged();
            myAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, item);
            searchPlace.setAdapter(myAdapter);

        }


    }


    public List<Vertex> getItemsFromDb(String searchTerm) {
        List<Vertex> vertexes = new Database(getActivity()).getVertexUsingQuery(searchTerm);
        itemId.clear();
        for (Vertex v : vertexes) {
            itemId.add(v.getId());
        }
        return vertexes;
    }

    public void updateRouteList(String searchPlace){
            List<Route> routeListPlace=imp.findRoutePlace(searchPlace);
        routeName.clear();
        routeId.clear();
        for(Route r:routeListPlace){
            if(language==1)
            routeName.add(r.getName());
            else
                routeName.add(r.getNameNepali());
            routeId.add(r.getId());
        }
        routesTemp.clear();
        routesTemp.addAll(routeListPlace);
        ar = new ArrayAdapter<>(getActivity(), R.layout.mytextview, routeName);
        routeList.setAdapter(ar);

    }


    public void setDisplayTextViewText() {
        searchPlace.setHint(Labels.searchPlaceHint(getActivity(), language));
        dp.setText(Labels.listOfRoutes(getActivity(), language));
    }

}
