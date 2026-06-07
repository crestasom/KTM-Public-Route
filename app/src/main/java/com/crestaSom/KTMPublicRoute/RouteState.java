package com.crestaSom.KTMPublicRoute;

import com.crestaSom.model.RouteData;
import com.crestaSom.model.RouteDataWrapper;
import com.crestaSom.model.Vertex;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Holds all route-computation state for SearchRouteFragment.
 * Extracted to reduce the fragment's ~50 scattered fields.
 */
class RouteState {
    Vertex sourceVertex, destVertex;
    List<Vertex> path = new LinkedList<>();
    List<Vertex> path1 = new ArrayList<>();
    List<RouteData> singlePaths = new ArrayList<>();
    Queue<RouteDataWrapper> altPathSingleTransit = new PriorityQueue<>();
    double[] distanceList = new double[10];
    int srcId = -1, destId = -1;
    int displayFlag = 0;
    boolean pathFound = false;
}
