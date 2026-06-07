package com.crestaSom.KTMPublicRoute.util;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crestaSom.KTMPublicRoute.R;
import com.crestaSom.model.Vertex;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class Util {
    final static String[] nepaliNum = {"०", "१", "२", "३", "४", "५", "६", "७", "८", "९"};
    /***
     * To add a text view to given linearlayout
     *
     * @param SpannableString displayText
     * @param LinearLayout    parentLayout
     * @param int             textSize
     * @param Boolean         isBold
     * @param int             textColor
     * @return
     */
    public static TextView addTextView(SpannableString displayText, LinearLayout parentLayout, int textSize, Boolean isBold, int textColor, Context con) {
        TextView displayView = new TextView(con);
        displayView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        if (isBold)
            displayView.setTypeface(Typeface.DEFAULT_BOLD);
        displayView.setText(displayText);
        displayView.setTextColor(textColor);
        displayView.setPadding(0, 2, 0, 2);
        parentLayout.addView(displayView);

        return displayView;
    }


    public static SpannableString displayTravelText(Context ctx, List<Vertex> vertexList, double dst, int fareL, boolean isWalk, int lang, boolean showFare) {
        Locale locale = (lang == 2) ? new Locale("ne") : Locale.ENGLISH;
        Configuration config = new Configuration(ctx.getResources().getConfiguration());
        config.setLocale(locale);
        Context lctx = ctx.createConfigurationContext(config);

        String from = (lang == 2) ? vertexList.get(0).getNameNepali() : vertexList.get(0).getName();
        String to = (lang == 2) ? vertexList.get(vertexList.size() - 1).getNameNepali()
                                : vertexList.get(vertexList.size() - 1).getName();
        String distStr = new DecimalFormat("#.##").format(dst);
        if (lang == 2) distStr = convertNumberToNepali(distStr);

        String prefix    = lctx.getString(isWalk ? R.string.walk_prefix    : R.string.ride_prefix);
        String connector = lctx.getString(R.string.ride_connector);
        String suffix    = lctx.getString(isWalk ? R.string.walk_suffix    : R.string.ride_suffix, distStr);

        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        int mark1 = sb.length();
        sb.append(from);
        int mark2 = sb.length();
        sb.append(connector);
        int mark3 = sb.length();
        sb.append(to);
        int mark4 = sb.length();
        sb.append(suffix);
        if (!isWalk && showFare) {
            String fareStr = (lang == 2) ? convertNumberToNepali(fareL) : String.valueOf(fareL);
            sb.append(lctx.getString(R.string.ride_fare, fareStr));
        }
        if (!isWalk && lang == 1) sb.append("\n");

        SpannableString displayTravelL = new SpannableString(sb.toString());
        displayTravelL.setSpan(new StyleSpan(Typeface.BOLD), mark1, mark2, 0);
        displayTravelL.setSpan(new StyleSpan(Typeface.BOLD), mark3, mark4, 0);
        return displayTravelL;
    }

    public static String convertNumberToNepali(double num) {
        String numInNep = "";
        String temp = String.valueOf(num);
        String temp1 = "";
        System.out.println(temp);
        for (int i = 0; i < temp.length(); i++) {
            temp1 = String.valueOf(temp.charAt(i));
            if (temp1.equals(".")) {
                numInNep += temp1;
            } else {
                numInNep += convertNepali(Integer.parseInt(temp1));
            }

        }
        return numInNep;

    }

    public static String convertNumberToNepali(String num) {
        String numInNep = "";
        //String temp=String.valueOf(num);
        String temp1 = "";
        for (int i = 0; i < num.length(); i++) {
            temp1 = String.valueOf(num.charAt(i));
            if (temp1.equals(".")) {
                numInNep += temp1;
            } else {
                numInNep += convertNepali(Integer.parseInt(temp1));
            }

        }
        return numInNep;

    }

    public static String convertNumberToNepali(int num) {
        String numInNep = "";
        String temp = String.valueOf(num);
        String temp1 = "";
        for (int i = 0; i < temp.length(); i++) {
            temp1 = String.valueOf(temp.charAt(i));

            numInNep += convertNepali(Integer.parseInt(temp1));

        }
        return numInNep;
    }

    public static String convertNepali(int n) {
        String s = "";
        //System.out.println("sdfa:"+n);
        s = nepaliNum[n];
        return s;
    }
}
