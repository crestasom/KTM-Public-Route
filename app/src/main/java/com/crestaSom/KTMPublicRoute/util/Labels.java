package com.crestaSom.KTMPublicRoute.util;

import android.content.Context;
import android.content.res.Configuration;
import com.crestaSom.KTMPublicRoute.R;
import com.crestaSom.model.Vertex;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Bilingual string catalog.
 * Loads strings from res/values/strings.xml (EN) or res/values-ne/strings.xml (NE)
 * using a locale-scoped context, so the app's custom language pref is respected
 * independently of the system locale.
 */
public final class Labels {

    public static final int EN = 1;
    public static final int NE = 2;

    private Labels() {}

    /** Returns a context whose resources resolve to the requested language. */
    private static Context localized(Context ctx, int lang) {
        Locale locale = (lang == NE) ? new Locale("ne") : Locale.ENGLISH;
        Configuration config = new Configuration(ctx.getResources().getConfiguration());
        config.setLocale(locale);
        return ctx.createConfigurationContext(config);
    }

    // ── Section headers ──────────────────────────────────────────────────────

    public static String shortestRoute(Context ctx, int lang) {
        return localized(ctx, lang).getString(R.string.shortest_route);
    }

    public static String directRoute(Context ctx, int lang) {
        return localized(ctx, lang).getString(R.string.direct_route);
    }

    public static String alternativeRoute(Context ctx, int lang) {
        return localized(ctx, lang).getString(R.string.alternative_route);
    }

    public static String availableRoutes(Context ctx, int lang) {
        return localized(ctx, lang).getString(R.string.available_routes);
    }

    // ── Travel legs ──────────────────────────────────────────────────────────

    public static String travel(Context ctx, int lang, int n) {
        if (lang == EN) return localized(ctx, lang).getString(R.string.travel, n);
        return localized(ctx, lang).getString(R.string.travel, Util.convertNepali(n));
    }

    /** Full "Travel N: From देखी/to To" line for transit detail view. */
    public static String travelLeg(Context ctx, int lang, int n, Vertex from, Vertex to) {
        String fromName = (lang == EN) ? from.getName() : from.getNameNepali();
        String toName   = (lang == EN) ? to.getName()   : to.getNameNepali();
        return localized(ctx, lang).getString(R.string.travel_leg, travel(ctx, lang, n), fromName, toName);
    }

    public static String routeNum(Context ctx, int lang, int n) {
        if (lang == EN) return localized(ctx, lang).getString(R.string.route_num, n);
        return localized(ctx, lang).getString(R.string.route_num, Util.convertNepali(n));
    }

    // ── Summary ──────────────────────────────────────────────────────────────

    public static String totalDistance(Context ctx, int lang, double dist) {
        String d = new DecimalFormat("#.##").format(dist);
        if (lang == EN) return localized(ctx, lang).getString(R.string.total_distance, d);
        return localized(ctx, lang).getString(R.string.total_distance, Util.convertNumberToNepali(d));
    }

    public static String totalCost(Context ctx, int lang, int cost) {
        if (lang == EN) return localized(ctx, lang).getString(R.string.total_cost, cost);
        return localized(ctx, lang).getString(R.string.total_cost, Util.convertNumberToNepali(cost));
    }

    // ── UI hints / buttons ───────────────────────────────────────────────────

    public static String viewDetail(Context ctx, int lang) {
        return localized(ctx, lang).getString(R.string.view_detail);
    }

    public static String searchSource(Context ctx, int lang) {
        return localized(ctx, lang).getString(R.string.search_source);
    }

    public static String searchDestination(Context ctx, int lang) {
        return localized(ctx, lang).getString(R.string.search_destination);
    }

    public static String listOfRoutes(Context ctx, int lang) {
        return localized(ctx, lang).getString(R.string.list_of_routes);
    }

    public static String searchPlaceHint(Context ctx, int lang) {
        return localized(ctx, lang).getString(R.string.search_place_hint);
    }

    public static String nearbyStops(Context ctx, int lang) {
        return localized(ctx, lang).getString(R.string.nearby_stops);
    }

    public static String noNearbyLocation(Context ctx, int lang) {
        return localized(ctx, lang).getString(R.string.no_nearby_location);
    }

    // ── Progress / toasts ────────────────────────────────────────────────────

    public static String detectingPath(Context ctx, int lang) {
        return localized(ctx, lang).getString(R.string.detecting_path);
    }

    public static String detectingLocation(Context ctx) {
        return ctx.getString(R.string.detecting_location);
    }

    public static String locationNotFound(Context ctx) {
        return ctx.getString(R.string.location_not_found);
    }

    public static String locationCancelled(Context ctx) {
        return ctx.getString(R.string.location_cancelled);
    }

    public static String enableLocation(Context ctx) {
        return ctx.getString(R.string.enable_location);
    }

    // ── Detail view ──────────────────────────────────────────────────────────

    public static String transitStops(Context ctx, int lang) {
        return localized(ctx, lang).getString(R.string.transit_stops);
    }

    public static String vehicleType(Context ctx, int lang, String type) {
        return localized(ctx, lang).getString(R.string.vehicle_type, type);
    }

    // ── Menu toggle labels ───────────────────────────────────────────────────

    public static String menuAnimation(Context ctx, boolean enabled) {
        return ctx.getString(enabled ? R.string.menu_animation_on : R.string.menu_animation_off);
    }

    public static String menuFare(Context ctx, boolean visible) {
        return ctx.getString(visible ? R.string.menu_hide_fare : R.string.menu_show_fare);
    }
}
