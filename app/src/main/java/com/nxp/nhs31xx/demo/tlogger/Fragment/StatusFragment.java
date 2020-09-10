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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.nxp.nhs31xx.demo.tlogger.MainActivity;
import com.nxp.nhs31xx.demo.tlogger.Message.Response.GetConfigResponse;
import com.nxp.nhs31xx.demo.tlogger.Message.Response.MeasureTemperatureResponse;
import com.nxp.nhs31xx.demo.tlogger.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StatusFragment extends Fragment {

    public StatusFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_status, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        initFields();
    }

    public boolean liveMeasurementsIsChecked() {
        boolean checked = false;
        View view = this.getView();
        if (view != null) {
            checked = ((Switch) view.findViewById(R.id.liveMeasurementsStatusSwitch)).isChecked();
        }
        return checked;
    }

    public void update(boolean exporting) {
        View view = this.getView();
        if (view != null) {
            try {
                MainActivity activity = (MainActivity) getActivity();
                Button button = view.findViewById(R.id.exportStatusButton);
                if (activity.lastUsedTagId == null) {
                    view.findViewById(R.id.exportStatusButton).setEnabled(false);
                } else if (exporting) {
                    button.setText(R.string.exportOngoingStatus);
                    view.findViewById(R.id.exportStatusButton).setEnabled(false);
                } else {
                    button.setText(R.string.exportStatus);
                    view.findViewById(R.id.exportStatusButton).setEnabled(true);
                }
            } catch (NullPointerException e) {
                // absorb
            }
        }
    }

    public void update(int retrievedMeasurementsCount, int totalMeasurementsCount) {
        View view = this.getView();
        if (view != null) {
            TextView textView;

            textView = view.findViewById(R.id.countStatusTextView);
            if (retrievedMeasurementsCount >= totalMeasurementsCount) {
                textView.setText(String.format(getResources().getString(R.string.countEndStatus), totalMeasurementsCount));
                view.findViewById(R.id.exportStatusButton).setEnabled(true);
            } else {
                textView.setText(String.format(getResources().getString(R.string.countOngoingStatus), retrievedMeasurementsCount, totalMeasurementsCount));
            }
        }
    }

    public void update(GetConfigResponse getConfigResponse) {
        View view = this.getView();
        if (view != null) {
            TextView textView;
            try {
                MainActivity activity = (MainActivity) getActivity();
                textView = view.findViewById(R.id.tagStatusTextView);
                if (activity.lastUsedTagId == null) {
                    textView.setText("");
                    view.findViewById(R.id.exportStatusButton).setEnabled(false);
                } else {
                    textView.setText(String.format(getResources().getString(R.string.tagStatus), activity.lastUsedTagId));
                    view.findViewById(R.id.exportStatusButton).setEnabled(true);
                }

                Date start = new Date((long)getConfigResponse.getStartTime() * 1000);
                TextView startTextView = view.findViewById(R.id.startStatusTextView);
                TextView countTextView = view.findViewById(R.id.countStatusTextView);
                TextView limitTextView = view.findViewById(R.id.limitStatusTextView);
                TextView rangeTextView = view.findViewById(R.id.rangeStatusTextView);
                TextView alarmTextView = view.findViewById(R.id.alarmStatusTextView);
                if (getConfigResponse.isPristine()) {
                    startTextView.setText(R.string.notStartedStatus);
                    countTextView.setText(R.string.noCountStatus);
                    limitTextView.setText("");
                    rangeTextView.setText("");
                } else {
                    SimpleDateFormat format = new SimpleDateFormat(getString(R.string.simpleDateFormat), Locale.US);
                    startTextView.setText(String.format(getResources().getString(R.string.startedStatus), format.format(start), getConfigResponse.getInterval()));
                    countTextView.setText(String.format(getResources().getString(R.string.countStartStatus), getConfigResponse.getCount()));
                    if (getConfigResponse.memoryIsFull()) {
                        limitTextView.setText(R.string.memoryFullStatus);
                    }
                    else if (getConfigResponse.countIsLimited()) {
                        if (getConfigResponse.countLimitIsReached()) {
                            limitTextView.setText(R.string.limitReachedStatus);
                        } else {
                            limitTextView.setText(R.string.limitNotReachedStatus);
                        }
                    } else {
                        limitTextView.setText(R.string.noLimitStatus);
                    }
					int validMinimum = getConfigResponse.getValidMinimum();
                    int validMaximum = getConfigResponse.getValidMaximum();
                    String unit = getResources().getString(R.string.celsius);
                    if (validMinimum < validMaximum) {
                        rangeTextView.setText(String.format(getResources().getString(R.string.rangeStatus), 0.1 * validMinimum, unit, 0.1 * validMaximum, unit));
                        if (getConfigResponse.isValid()) {
                            alarmTextView.setText(R.string.alarmNotTrippedStatus);
                        } else {
                            alarmTextView.setText(R.string.alarmTrippedStatus);
                        }
                    } else {
                        rangeTextView.setText(R.string.noRangeStatus);
                        alarmTextView.setText("");
                    }
                }

                Button button = view.findViewById(R.id.exportStatusButton);
                button.setVisibility(getConfigResponse.getCount() > 0 ? View.VISIBLE : View.INVISIBLE);
            } catch (NullPointerException e) {
                // absorb
            }
        }
    }

    public void update(MeasureTemperatureResponse measureTemperatureResponse) {
        View view = this.getView();
        if (view != null) {
            TextView textView;
            try {
                textView = view.findViewById(R.id.liveMeasurementsStatusSwitch);
                textView.setText(String.format(getResources().getString(R.string.liveMeasurementStatus), 0.1f * measureTemperatureResponse.getTemperature()));
            } catch (NullPointerException e) {
                // absorb
            }
        }
    }

    public void reset() {
        // void for now
    }

    private void initFields() {
        MainActivity activity = (MainActivity) getActivity();
        update(activity.lastGetConfigResponse);
        update(activity.lastMeasureTemperatureResponse);
    }
}
