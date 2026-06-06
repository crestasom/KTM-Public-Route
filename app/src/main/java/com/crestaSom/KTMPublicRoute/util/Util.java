package com.crestaSom.KTMPublicRoute.util;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crestaSom.model.Vertex;

import java.text.DecimalFormat;
import java.util.List;

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


    public static SpannableString displayTravelText(List<Vertex> vertexList, double dst, int fareL, boolean isWalk, int lang) {
        String dsply = "";
        int  mark1=0,mark2=0,mark3=0,mark4=0;
        SpannableString displayTravelL = null;
        if (!isWalk) {
            dsply = "";
            if (lang == 1) {
                dsply += "Take a ride from ";
                mark1 = dsply.length();
                dsply += vertexList.get(0);
                mark2 = dsply.length();
                dsply += " to ";
                mark3 = dsply.length();
                dsply += vertexList.get(vertexList.size() - 1);
                mark4 = dsply.length();
                dsply += " with distance " + new DecimalFormat("#.##").format(dst) + " km";
                dsply += " and cost Rs." + fareL + ".";
                dsply += "\n";
            } else if (lang == 2) {
                mark1 = dsply.length();
                dsply += vertexList.get(0).getNameNepali();
                mark2 = dsply.length();
                dsply += " देखी ";
                mark3 = dsply.length();
                dsply += vertexList.get(vertexList.size() - 1).getNameNepali();
                mark4 = dsply.length();
                dsply += " सम्म यात्रा गर्नुहोस।";
                dsply += "\nदुरी: " + Util.convertNumberToNepali(new DecimalFormat("#.##").format(dst)) + " कि.मी.";
                dsply += "\nभाडा रु. " + Util.convertNumberToNepali(fareL);
            }

        } else {
            if (lang == 1) {
                dsply += "Walk from ";
                mark1 = dsply.length();
                dsply += vertexList.get(0);
                mark2 = dsply.length();
                dsply += " to ";
                mark3 = dsply.length();
                dsply += vertexList.get(vertexList.size() - 1);
                mark4 = dsply.length();
                dsply += " with distance " + new DecimalFormat("#.##").format(dst) + " km";
            } else if (lang == 2) {
                mark1 = dsply.length();
                dsply += vertexList.get(0).getNameNepali();
                mark2 = dsply.length();
                dsply += " देखी ";
                mark3 = dsply.length();
                dsply += vertexList.get(vertexList.size() - 1).getNameNepali();
                mark4 = dsply.length();
                dsply += " सम्म हिंड्नुस।";
                dsply += "\nदुरी: " + Util.convertNumberToNepali(new DecimalFormat("#.##").format(dst)) + " कि.मी.";
            }
        }
        displayTravelL = new SpannableString(dsply);
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
