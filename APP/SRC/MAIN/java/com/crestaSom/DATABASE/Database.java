package com.crestaSom.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.crestaSom.model.Edge;
import com.crestaSom.model.Fare;
import com.crestaSom.model.Route;
import com.crestaSom.model.Vertex;

/**
 * Created by CrestaSom on 11/3/2016.
 */

public class Database {
    private static final String DATABASE_TABLE_VERTEX = "stops";
    private static final String VERTEX_ROWID = "id";
    private static final String VERTEX_NAME = "name";
    private static final String VERTEX_REF_STOP = "referenceStops";
    private static final String VERTEX_LAT = "latCode";
    private static final String VERTEX_LONG = "longCode";
    private static final String VERTEX_ISTRANSIT = "isTransit";
    private static final String VERTEX_NAME_NEPALI = "name_nepali";

    private static final String DATABASE_TABLE_EDGE = "edges";
    private static final String EDGE_ID = "id";
    private static final String EDGE_SOURCE_STOP = "source_stop";
    private static final String EDGE_DEST_STOP = "destination_stop";
    private static final String EDGE_DISTANCE = "distance";
    private static final String EDGE_ONEWAY = "oneway";
    private static final String EDGE_REF_STOP = "referenceStops";
    //private static final String EDGE_NEW = "isNew";

    private static final String DATABASE_TABLE_ROUTE = "route";
    private static final String ROUTE_ID = "id";
    private static final String ROUTE_NAME = "name";
    private static final String ROUTE_NAME_NEPALI = "name_nepali";
    private static final String ROUTE_STOPS = "stops";
    //private static final String ROUTE_DOUBLESIDED = "doubleSided";
    private static final String ROUTE_VEHICLETYPE = "vehicleType";
    private static final String ROUTE_VEHICLETYPE_NEPALI = "vehicleType_nepali";
    //private static final String ROUTE_STATUS = "status";
    //private static final String ROUTE_NEW = "isNew";

    private static final String DATABASE_TABLE_FARE = "fare_rate";
    private static final String FARE_DISTANCE = "distance";
    private static final String FARE_RATE = "fare";

    private static final String DATABASE_NAME = "ktm_public_route";
    private static final int DATABASE_VERSION = 5;

    String[] allColumnsEdge = {EDGE_ID, EDGE_REF_STOP, EDGE_SOURCE_STOP,
            EDGE_DEST_STOP, EDGE_DISTANCE, EDGE_ONEWAY};

    String[] allColumnsVertex = {VERTEX_ROWID, VERTEX_NAME, VERTEX_REF_STOP,
            VERTEX_LAT, VERTEX_LONG, VERTEX_ISTRANSIT,VERTEX_NAME_NEPALI};
    String[] allColumnsRoute = {ROUTE_ID, ROUTE_NAME,ROUTE_NAME_NEPALI, ROUTE_STOPS,
            ROUTE_VEHICLETYPE,ROUTE_VEHICLETYPE_NEPALI};

    String[] allColumnsFare = {FARE_DISTANCE, FARE_RATE};

    private DbHelper dbHelper;
    private final Context dbContext;
    private SQLiteDatabase dbTest;
    Cursor cursor;


    public static class DbHelper extends SQLiteOpenHelper {
        Context con;

        public DbHelper(Context context) {

            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            con = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            //Log.d("Database", "Creating Database");
            //Log.d("Database", "Creating Table Vertex");
            db.execSQL("CREATE TABLE " + DATABASE_TABLE_VERTEX + "("
                    + VERTEX_ROWID + "  INTEGER PRIMARY KEY, " + VERTEX_NAME
                    + " VARCHAR(255) NOT NULL, " + VERTEX_REF_STOP
                    + " VARCHAR(7) NOT NULL, " + VERTEX_LAT
                    + " DOUBLE NOT NULL, "
                    + VERTEX_LONG + " DOUBLE NOT NULL, " + VERTEX_ISTRANSIT
                    + " TINYINT NOT NULL DEFAULT '0', " + VERTEX_NAME_NEPALI
                    + " VARCHAR(255) ); ");
            Log.d("Database", "Creating Table Edge");
            db.execSQL("CREATE TABLE " + DATABASE_TABLE_EDGE + "(" + EDGE_ID
                    + "  INTEGER PRIMARY KEY, " + EDGE_REF_STOP
                    + " INTEGER NOT NULL, " + EDGE_SOURCE_STOP
                    + " INTEGER NOT NULL, " + EDGE_DEST_STOP
                    + " INTEGER NOT NULL, " + EDGE_DISTANCE
                    + " DOUBLE NOT NULL, "
                    + EDGE_ONEWAY + " TINYINT NOT NULL); ");
            Log.d("Database", "Creating Table Route");
            db.execSQL("CREATE TABLE " + DATABASE_TABLE_ROUTE + "(" + ROUTE_ID
                    + "  INTEGER PRIMARY KEY, " + ROUTE_NAME
                    + " VARCHAR(255) NOT NULL, "+ ROUTE_NAME_NEPALI
                    + " VARCHAR(255) NOT NULL, "  + ROUTE_STOPS
                    + " VARCHAR(255) NOT NULL, " + ROUTE_VEHICLETYPE
                    + " VARCHAR(25) NOT NULL, " + ROUTE_VEHICLETYPE_NEPALI
                    + " VARCHAR(25) NOT NULL); ");
            db.execSQL("CREATE TABLE " + DATABASE_TABLE_FARE + "(" + FARE_DISTANCE
                    + "  DOUBLE NOT NULL , " + FARE_RATE
                    + " INTEGER NOT NULL); ");
            Log.d("Database", "Database Created");

            String sqlEdge, sqlVertex, sqlRoute, sqlFare;
            SQLString sqls = new SQLString();
            sqlVertex = sqls.sqlVertex;
            sqlEdge = sqls.sqlEdge;
            sqlRoute = sqls.sqlRoute;
            sqlFare = sqls.sqlFare;

            try {
                db.execSQL(sqlVertex);
                Log.d("Database", "Vertex Table Populated");
            } catch (SQLiteException ex) {
                Log.d("Sqlite Error", ex.getMessage());
                //Log.d("Database", "Vertex Table Populated");
            }

            try {
                db.execSQL(sqlEdge);
                Log.d("Database", "Edge Table Populated");
            } catch (SQLiteException ex) {
                Log.d("Sqlite Error", ex.getMessage());

            }

            try {
                db.execSQL(sqlRoute);
                Log.d("Database", "Route Table Populated");
            } catch (SQLiteException ex) {
                Log.d("Sqlite Error", ex.getMessage());
            }

            try {
                db.execSQL(sqlFare);
                Log.d("Database", "Fare Table Populated");

                //Toast.makeText(con,"Fare Table Populated",Toast.LENGTH_LONG).show();
            } catch (SQLiteException ex) {
                Log.d("Sqlite Error", ex.getMessage());
                Toast.makeText(con, ex.getMessage(), Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d("Database", "Database Upgraded");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_VERTEX);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_EDGE);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_ROUTE);
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_FARE);
            onCreate(db);
        }

    }

    public Database(Context c) {
        //Log.d("Context", c.toString());
        dbContext = c;

    }

    public Database open() {

        dbHelper = new DbHelper(dbContext);
        dbTest = dbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbHelper.close();
    }

    public List<Vertex> getAllVertex() {

        open();
        List<Vertex> comments = new ArrayList<Vertex>();
        cursor = dbTest.query(DATABASE_TABLE_VERTEX, allColumnsVertex, null,
                null, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Vertex comment = cursorToVertex(cursor);
            comments.add(comment);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        close();
        return comments;
    }

    //
    //
    private Vertex cursorToVertex(Cursor cursor) {

        //Log.d("cursor",cursor.getColumnCount()+"");
        List<String> items = null;
        int id1, id2, length;
        String refPoints;
        Vertex v = new Vertex();
        Vertex temp = null;
        v.setId(cursor.getInt(0));
        v.setName(cursor.getString(1));
        refPoints = cursor.getString(2);
        items = Arrays.asList(refPoints.split("\\s*,\\s*"));
        length = items.size();
        id1 = Integer.parseInt(items.get(0));
        v.setReferenceStop(id1);
        if (length == 2) {
            id2 = Integer.parseInt(items.get(1));
            v.setReferenceStop1(id2);
        } else {
            temp = new Vertex();
            temp.setId(-1);
            v.setReferenceStop1(-1);
        }
        v.setLatCode(cursor.getDouble(3));
        v.setLongCode(cursor.getDouble(4));
        v.setTransit(cursor.getInt(5) > 0);
        v.setNameNepali(cursor.getString(6));
        //Log.d("nepali name",v.getNameNepali());
        return v;
    }

    public List<Edge> getAllEdges() {
        open();
        List<Edge> comments = new ArrayList<Edge>();

        cursor = dbTest.query(DATABASE_TABLE_EDGE, allColumnsEdge, null, null,
                null, null, null);

        cursor.moveToFirst();
        cursor.getCount();
        // //Log.d("Edge Count", "" + count);
        while (!cursor.isAfterLast()) {
            // count++;
            Edge comment = cursorToEdge(cursor);
            comments.add(comment);
            cursor.moveToNext();
        }
        // //Log.d("Edge Check", "After Edge");

        // make sure to close the cursor
        cursor.close();
        close();
        // return comments;
        return comments;
    }

    //
    //
    private Edge cursorToEdge(Cursor cursor) {
        List<String> items = null;
        int id1, id2, length;
        String refPoints;
        Edge e = new Edge();
        e.setId(cursor.getInt(0));
        e.setSource(getVertex(cursor.getInt(2)));
        e.setDestination(getVertex(cursor.getInt(3)));
        e.setWeight(cursor.getDouble(4));
        e.setOneway(cursor.getInt(5) > 0);
        refPoints = cursor.getString(1);
        items = Arrays.asList(refPoints.split("\\s*,\\s*"));
        length = items.size();
        id1 = Integer.parseInt(items.get(0));

        e.setReferenceStop(id1);
        if (length == 2) {
            id2 = Integer.parseInt(items.get(1));
            e.setReferenceStop1(id2);
        } else {
            Vertex temp = new Vertex();
            temp.setId(-1);
            e.setReferenceStop1(-1);
        }
        return e;
    }

    public Vertex getVertex(int id) {
        Vertex v = null;
        open();
        Cursor vertexRecord = dbTest.query(DATABASE_TABLE_VERTEX,
                allColumnsVertex, "id=" + id, null, null, null, null);
        // //Log.d("Test Data", vertexRecord.toString());
        vertexRecord.moveToFirst();
        if (vertexRecord.getCount() == 0) {
            return null;
        }
        v = cursorToVertex(vertexRecord);
        close();
        vertexRecord.close();
        return v;
    }

    public Edge getEdge(int id) {
        // //Log.d("Checkpoint", "Checkpoint");
        Edge e = null;
        open();
        Cursor edgeRecord = dbTest.query(DATABASE_TABLE_EDGE, allColumnsEdge,
                "id=" + id, null, null, null, null);
        edgeRecord.moveToFirst();
        // //Log.d("EdgeRecord", "" + edgeRecord);
        e = cursorToEdge(edgeRecord);

        close();
        edgeRecord.close();
        return e;
    }

    public List<Route> getAllRoute() {

        List<Route> comments = new ArrayList<Route>();
        open();
        cursor = dbTest.query(DATABASE_TABLE_ROUTE, allColumnsRoute,
                null, null, null, null, ROUTE_NAME);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Route comment = cursorToRoute(cursor);
            comments.add(comment);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        close();
        return comments;
    }

    //
    //
    private Route cursorToRoute(Cursor cursor) {
        List<String> items = null;
        String ids;
        Route r = new Route();
        List<Integer> vertexList = new ArrayList<Integer>();

        items = new ArrayList<String>();
        r = new Route();
        r.setId(cursor.getInt(0));
        r.setName(cursor.getString(1));
        r.setNameNepali(cursor.getString(2));
        ids = cursor.getString(3);

        items = Arrays.asList(ids.split("\\s*,\\s*"));
        for (String id : items) {
            vertexList.add(Integer.parseInt(id));
        }
        r.setAllVertexes(vertexList);
        r.setVehicleType(cursor.getString(4));
        r.setVehicleTypeNepali(cursor.getString(5));
        return r;
    }

    public List<Vertex> getVertexes(String string) {

        List<Vertex> vertexes = new LinkedList<Vertex>();
        Vertex v;
        open();
        cursor = dbTest.query(DATABASE_TABLE_VERTEX, allColumnsVertex,
                VERTEX_REF_STOP + " like '" + string + "'", null, null, null,
                null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            v = cursorToVertex(cursor);
            cursor.moveToNext();
            vertexes.add(v);
        }

        cursor.close();
        close();
        return vertexes;

    }

    public List<Edge> getEdges(String string) {

        List<Edge> edges = new LinkedList<Edge>();
        Edge e;
        open();
        cursor = dbTest.query(DATABASE_TABLE_EDGE, allColumnsEdge,
                EDGE_REF_STOP + " like '" + string + "'", null, null, null,
                null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            e = cursorToEdge(cursor);
            cursor.moveToNext();
            edges.add(e);
        }

        cursor.close();
        close();
        return edges;

    }

    public List<Vertex> getVertexUsingQuery(String searchTerm) {
        // TODO Auto-generated method stub
        //Log.d("Context", dbContext.toString());
        List<Vertex> result = new ArrayList<Vertex>();
        Vertex v = null;
        open();
        String sql = "SELECT * FROM " + DATABASE_TABLE_VERTEX + " WHERE "
                + VERTEX_NAME + " LIKE '%" + searchTerm + "%' or "+VERTEX_NAME_NEPALI+" LIKE '%"+searchTerm+"%' and isTransit=0 LIMIT 0,5";
        Cursor cursor = dbTest.rawQuery(sql, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            v = cursorToVertex(cursor);
            result.add(v);
            cursor.moveToNext();

        }
        close();
        return result;
    }

    public void addNewRecords(JSONArray vertexNew, JSONArray edgeNew,
                              JSONArray routeNew, JSONArray fareNew) {
        // TODO Auto-generated method stub


        open();
        dbTest.beginTransaction();
        ContentValues values;
        try {
            dbTest.delete(DATABASE_TABLE_VERTEX, null, null);
            dbTest.delete(DATABASE_TABLE_EDGE, null, null);
            dbTest.delete(DATABASE_TABLE_ROUTE, null, null);
            dbTest.delete(DATABASE_TABLE_FARE, null, null);
            //Log.d("Database", "Database Cleared");
            JSONObject obj;
            int sizeV = vertexNew.length();
            int sizeE = edgeNew.length();
            int sizeR = routeNew.length();
            int sizeF = fareNew.length();
            for (int i = 0; i < sizeV; i++) {
                obj = new JSONObject();
                values = new ContentValues();
                obj = vertexNew.getJSONObject(i);
                values.put(VERTEX_ROWID, obj.getInt("id"));
                values.put(VERTEX_NAME, obj.getString("name"));
                values.put(VERTEX_LAT, obj.getDouble("latCode"));
                values.put(VERTEX_LONG, obj.getDouble("longCode"));
                values.put(VERTEX_REF_STOP, obj.getString("referenceStops"));
                values.put(VERTEX_NAME_NEPALI, obj.getString("name_nepali"));

                dbTest.insert(DATABASE_TABLE_VERTEX, null, values);

            }

            for (int i = 0; i < sizeE; i++) {
                obj = new JSONObject();
                values = new ContentValues();
                obj = edgeNew.getJSONObject(i);
                values.put(EDGE_ID, obj.getInt("id"));
                values.put(EDGE_SOURCE_STOP, obj.getInt("source_stop"));
                values.put(EDGE_DEST_STOP, obj.getInt("destination_stop"));
                values.put(EDGE_DISTANCE, obj.getDouble("distance"));
                values.put(EDGE_ONEWAY, obj.getInt("oneway"));
                values.put(EDGE_REF_STOP, obj.getString("referenceStops"));

                dbTest.insert(DATABASE_TABLE_EDGE, null, values);

            }

            for (int i = 0; i < sizeR; i++) {
                obj = new JSONObject();
                values = new ContentValues();
                obj = routeNew.getJSONObject(i);
                values.put(ROUTE_ID, obj.getInt("id"));
                values.put(ROUTE_NAME, obj.getString("name"));
                values.put(ROUTE_NAME_NEPALI, obj.getString("name_nepali"));
                values.put(ROUTE_STOPS, obj.getString("stops"));
                values.put(ROUTE_VEHICLETYPE, obj.getString("vehicleType"));
                values.put(ROUTE_VEHICLETYPE_NEPALI, obj.getString("vehicleType_nepali"));
                dbTest.insert(DATABASE_TABLE_ROUTE, null, values);

            }

            for (int i = 0; i < sizeF; i++) {
                obj = new JSONObject();
                values = new ContentValues();
                obj = fareNew.getJSONObject(i);
                values.put(FARE_DISTANCE, obj.getDouble("distance"));
                values.put(FARE_RATE, obj.getInt("fare"));

                dbTest.insert(DATABASE_TABLE_FARE, null, values);

            }

            dbTest.setTransactionSuccessful();

        } catch (JSONException ex) {
            Log.e("JSON Exception", ex.getMessage());
        } finally {
            dbTest.endTransaction();
            close();
        }

    }

    public Vertex getVertexDetail(String name) {
        Vertex v = new Vertex();
        open();
        String sql = "SELECT * FROM " + DATABASE_TABLE_VERTEX + " WHERE "
                + VERTEX_NAME + " LIKE '" + name + "' or "+VERTEX_NAME_NEPALI+" LIKE '"+name+"' LIMIT 0,5";
        Cursor cursor = dbTest.rawQuery(sql, null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {

            v = cursorToVertex(cursor);
            //Log.d("Checkpoint", v.toString());
        } else {
            v.setId(-1);
        }
        cursor.close();
        close();
        return v;

    }


    public List<Fare> getFareList() {
        List<Fare> fareList = new ArrayList<Fare>();
        Fare fare = new Fare();
        open();

        cursor = dbTest.query(DATABASE_TABLE_FARE, allColumnsFare,
                null, null, null, null,
                null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {

            fare = cursorToFare(cursor);
            cursor.moveToNext();
            fareList.add(fare);
        }

        cursor.close();
        close();
        return fareList;
    }

    private Fare cursorToFare(Cursor cursor) {
        Fare v = new Fare();
        v.setDistance(cursor.getDouble(0));
        v.setFare(cursor.getInt(1));
        //cursor.close();
        return v;
    }


}
