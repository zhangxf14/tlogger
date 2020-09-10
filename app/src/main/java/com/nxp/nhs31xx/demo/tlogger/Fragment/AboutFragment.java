/*
 * Copyright 2020 NXP.
 * This software is owned or controlled by NXP and may only be used
 * strictly in accordance with the applicable license terms.  By expressly
 * accepting such terms or by downloading, installing, activating and/or
 * otherwise using the software, you are agreeing that you have read, and
 * that you agree to comply with and are bound by, such license terms.  If
 * you do not agree to be bound by the applicable license terms, then you
 * may not retain, install, activate or otherwise use the software.
 */


package com.nxp.nhs31xx.demo.tlogger.Fragment;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nxp.nhs31xx.demo.tlogger.MainActivity;
import com.nxp.nhs31xx.demo.tlogger.Message.Response.GetVersionResponse;
import com.nxp.nhs31xx.demo.tlogger.R;

import java.util.Locale;

import static com.nxp.nhs31xx.demo.tlogger.Helper.Util.readTextFromResource;

public class AboutFragment extends Fragment {

    private PopupWindow popupWindow = null;
    private String sw = "-";
    private String api = "-";

    public AboutFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_about, container, false);
        ImageView image = view.findViewById(R.id.logoImageView);
        image.setImageResource(R.mipmap.aaa_016728);
        View title1TextView = view.findViewById(R.id.title1TextView);
        title1TextView.bringToFront();

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                if (id == R.id.licenseTextView) {
                    popupLicenseWebView();
                } else if (id == R.id.contactTextView) {
                    popupContactWebView();
                } else if (id == R.id.logoImageView) {
                    popupAcceptWebView();
                }
            }
        };
        TextView copyrightTextView = view.findViewById(R.id.licenseTextView);
        copyrightTextView.setOnClickListener(onClickListener);
        TextView contactTextView = view.findViewById(R.id.contactTextView);
        contactTextView.setOnClickListener(onClickListener);
        view.setOnClickListener(onClickListener);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        update(((MainActivity) getActivity()).lastGetVersionResponse);
    }

    public void popupAcceptWebView() {
        closePopup();
        Fragment fragment = ((MainActivity) getActivity()).getCurrentFragment();
        View view = fragment.getView();
        if (view != null) {
            Context context = view.getContext();
            if (context != null) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.webview_accept, null);
                popupView.setTag("accept");
                WebView webview = popupView.findViewById(R.id.acceptWebView);
                webview.loadData(readTextFromResource(fragment, R.raw.accept), "text/html", "utf-8");
                webview.setWebViewClient(new MyWebViewClient(getActivity().getBaseContext()));
                CheckBox checkBox1 = popupView.findViewById(R.id.accept1PopupCheckBox);
                checkBox1.setChecked(MainActivity.nfcUnlocked);
                CheckBox checkBox2 = popupView.findViewById(R.id.accept2PopupCheckBox);
                checkBox2.setChecked(MainActivity.nfcUnlocked);
                Button button = popupView.findViewById(R.id.agreePopupButton);
                button.setEnabled(MainActivity.nfcUnlocked);

                DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
                popupWindow = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        (displaymetrics.heightPixels * 3) / 4,
                        true);
                popupWindow.setBackgroundDrawable(new ColorDrawable()); // Now the PopupWindow is dismissed when touched outside
                popupWindow.showAtLocation(view, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, getPopupLocationY());
            }
        }
    }

    private void popupLicenseWebView() {
        closePopup();
        Fragment fragment = ((MainActivity) getActivity()).getCurrentFragment();
        View view = fragment.getView();
        if (view != null) {
            Context context = view.getContext();
            if (context != null) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.webview_license, null);
                popupView.setTag("license");
                WebView webview = popupView.findViewById(R.id.licenseWebView);
                webview.loadData(readTextFromResource(fragment, R.raw.license), "text/html", "utf-8");
                DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
                popupWindow = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        (displaymetrics.heightPixels * 2) / 3,
                        true);
                popupWindow.setBackgroundDrawable(new ColorDrawable()); // Now the PopupWindow is dismissed when touched outside
                popupWindow.showAtLocation(view, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, getPopupLocationY());
            }
        }
    }

    private void popupContactWebView() {
        closePopup();
        Fragment fragment = ((MainActivity) getActivity()).getCurrentFragment();
        View view = fragment.getView();
        if (view != null) {
            Context context = view.getContext();
            if (context != null) {
                LayoutInflater layoutInflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.webview_contact, null);
                popupView.setTag("contact");
                WebView webview = popupView.findViewById(R.id.contactWebView);
                webview.loadData(readTextFromResource(fragment, R.raw.contact), "text/html", "utf-8");
                DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
                popupWindow = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        (displaymetrics.heightPixels / 2),
                        true);
                popupWindow.setBackgroundDrawable(new ColorDrawable()); // Now the PopupWindow is dismissed when touched outside
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
            }
        }
    }

    public void popupUpgradeWebView() {
        closePopup();
        Fragment fragment = ((MainActivity) getActivity()).getCurrentFragment();
        View view = fragment.getView();
        if (view != null) {
            Context context = view.getContext();
            if (context != null) {
                LayoutInflater layoutInflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popupView = layoutInflater.inflate(R.layout.webview_upgrade, null);
                popupView.setTag("upgrade");
                WebView webview = popupView.findViewById(R.id.upgradeWebView);
                webview.loadData(readTextFromResource(fragment, R.raw.upgrade), "text/html", "utf-8");
                webview.setWebViewClient(new MyWebViewClient(getActivity().getBaseContext()));
                DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
                popupWindow = new PopupWindow(
                        popupView,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        (displaymetrics.heightPixels * 2) / 3,
                        true);
                popupWindow.setBackgroundDrawable(new ColorDrawable()); // Now the PopupWindow is dismissed when touched outside
                popupWindow.showAtLocation(view, Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, getPopupLocationY());
            }
        }
    }

    public void updateButtonStates() {
        if (popupWindow != null) {
            View view = popupWindow.getContentView();
            CheckBox checkBox1 = view.findViewById(R.id.accept1PopupCheckBox);
            CheckBox checkBox2 = view.findViewById(R.id.accept2PopupCheckBox);
            Button button = view.findViewById(R.id.agreePopupButton);
            if (button != null) {
                if ((checkBox1 != null) && (checkBox1.isChecked())
                        && (checkBox2 != null) && (checkBox2.isChecked())) {
                    button.setEnabled(true);
                } else {
                    button.setEnabled(false);
                }
            }
        }
    }

    public void closePopup() {
        if (popupWindow != null) {
            try {
                popupWindow.dismiss();
            } catch (java.lang.IllegalArgumentException e) {
                // can happen when orientation is changed at ~ the same time.
                // absorb
            }
            popupWindow = null;
        }
    }

    public void closePopup(String popup) {
        if (popupWindow != null) {
            final String tag = (String) popupWindow.getContentView().getTag();
            if (tag.equals(popup)) {
                closePopup();
            }
        }
    }

    public void update(GetVersionResponse getVersionResponse) {
        if (getVersionResponse != null) {
            int swMajor = getVersionResponse.getSwMajor();
            int swMinor = getVersionResponse.getSwMinor();
            sw = String.format(Locale.US, "%d.%d", swMajor, swMinor);
            int apiMajor = getVersionResponse.getApiMajor();
            int apiMinor = getVersionResponse.getApiMinor();
            api = String.format(Locale.US, "%d.%d", apiMajor, apiMinor);
        }
        update();
    }

    private void update() {
        TextView textView;
        View view = this.getView();
        if (view != null) {
            textView = view.findViewById(R.id.versionTextView);
            if (textView != null) {
                textView.setText(String.format(getString(R.string.fullVersionString), this.getString(R.string.version), sw, api));
            }
        }
    }

    /* Calculate the starting position from the top of the screen where the pop-up window is to be positioned. */
    private int getPopupLocationY() {
        DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
        int y = displaymetrics.heightPixels / 12;
        Fragment fragment = ((MainActivity) getActivity()).getCurrentFragment();
        View view = fragment.getView();
        if (view != null) {
            View title2TextView = view.findViewById(R.id.title2TextView);
            if (title2TextView != null) {
                int[] title2TextViewLocation = new int[2];
                title2TextView.getLocationOnScreen(title2TextViewLocation);
                y = title2TextViewLocation[1];
            }
        }
        return y;
    }

    private class MyWebViewClient extends WebViewClient {
        MyWebViewClient(@SuppressWarnings("UnusedParameters") Context context) {
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.equals("tlogger://license")) {
                popupLicenseWebView();
                return true;
            } else if (url.equals("tlogger://contact")) {
                popupContactWebView();
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    }
}
