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

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import com.nxp.nhs31xx.demo.tlogger.MainActivity;
import com.nxp.nhs31xx.demo.tlogger.Message.Message;
import com.nxp.nhs31xx.demo.tlogger.R;

import static com.nxp.nhs31xx.demo.tlogger.R.id.intervalConfigTextView;
import static com.nxp.nhs31xx.demo.tlogger.R.id.runningTimeConfigTextView;
import static com.nxp.nhs31xx.demo.tlogger.R.id.startDelayConfigTextView;
import static com.nxp.nhs31xx.demo.tlogger.R.id.validMaximumConfigTextView;
import static com.nxp.nhs31xx.demo.tlogger.R.id.validMinimumConfigTextView;
import static com.nxp.nhs31xx.demo.tlogger.R.id.validationConfigSwitch;

public class ConfigFragment extends Fragment {

    static private int interval; /* seconds */
    static private int startDelay; /* seconds */
    static private int runningTime; /* seconds */
    static private int validMinimum; /* deci-celsius */
    static private int validMaximum; /* deci-celsius */

    static private int intervalNumber;
    static private int startDelayNumber;
    static private int runningTimeNumber;
    static private int validMinimumNumber;
    static private int validMaximumNumber;

    static private int intervalUnit;
    static private int startDelayUnit;
    static private int runningTimeUnit;
    static private boolean validationEnabled;
    static private int validMinimumUnit;
    static private int validMaximumUnit;

    final private static int[] intervals = new int[]{1, 2, 3, 4, 5, 6, 8, 10, 12, 15, 20, 25, 30, 35, 40, 50, 60, 75, 90, 100, 120};
    final private static int[] intervalUnitIds = new int[]{R.string.seconds, R.string.minutes};

    final private static int DELAY_START_INDEFINITELY = 0xFFFFFFFF; /* APP_MSG_DELAY_START_INDEFINITELY */
    /* When changing, also adapt SetStartDelay() accordingly. */
    final private static int[] startDelayIds = new int[]{R.string.startDelay_noDelay, R.string.startDelay_5Seconds,
            R.string.startDelay_10Seconds, R.string.startDelay_30Seconds, R.string.startDelay_1Minute,
            R.string.startDelay_2Minutes, R.string.startDelay_5Minutes, R.string.startDelay_10Minutes,
            R.string.startDelay_30Minutes, R.string.startDelay_1Hour, R.string.startDelay_ExternalTrigger};

    /* When changing, also adapt SetRunningTime() accordingly */
    final private static int[] runningTimeIds = new int[]{R.string.runningTime_indefinitely,
            R.string.runningTime_30minutes, R.string.runningTime_1hour, R.string.runningTime_2hours,
            R.string.runningTime_4hours, R.string.runningTime_8hours, R.string.runningTime_12hours,
            R.string.runningTime_1day, R.string.runningTime_2days, R.string.runningTime_3days,
            R.string.runningTime_5days, R.string.runningTime_1week};
    final private static int[] oldRunningTimeIds = new int[]{R.string.runningTimeOld_indefinitely, R.string.runningTimeOld_30minutes};

    final private static int[] thresholds = new int[]{-40, -30, -20, -18, -15, -10, -8, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 8, 10, 15, 18, 20, 23, 25, 30, 35, 40, 50, 60, 70, 85}; /* Celsius */
    final private static int[] thresholdUnitIds = new int[]{R.string.celsius};

    static private void SetInterval(int number, int unit) {
        ConfigFragment.intervalNumber = number;
        ConfigFragment.intervalUnit = unit;
        ConfigFragment.interval = intervals[number] * (unit == 0 ? 1 /* seconds */ : 60 /* minutes */);
    }

    static private void SetInterval(int interval) {
        int intervalNumber = 0;
        int intervalUnit = 0;
        int smallestDiff = 1000000; /* ridiculously high */
        for (int i = 0; i < intervals.length; i++) {
            for (int j = 0; j < intervalUnitIds.length; j++) {
                SetInterval(i, j);
                int currentDiff = Math.abs(interval - ConfigFragment.interval);
                if (currentDiff < smallestDiff) {
                    intervalNumber = i;
                    intervalUnit = j;
                    smallestDiff = currentDiff;
                }
            }
        }
        SetInterval(intervalNumber, intervalUnit);
    }

    static private void SetStartDelay(int number, int unit) {
        ConfigFragment.startDelayNumber = number;
        ConfigFragment.startDelayUnit = unit;
        switch (unit) { /* first measurement after: */
            case 0:
                ConfigFragment.startDelay = 0; /* no delay */
                break;
            case 1:
            default:
                ConfigFragment.startDelay = 5; /* 5 seconds */
                break;
            case 2:
                ConfigFragment.startDelay = 10; /* 10 seconds */
                break;
            case 3:
                ConfigFragment.startDelay = 30; /* 30 seconds */
                break;
            case 4:
                ConfigFragment.startDelay = 60; /* 1 minute */
                break;
            case 5:
                ConfigFragment.startDelay = 120; /* 2 minutes */
                break;
            case 6:
                ConfigFragment.startDelay = 5 * 60; /* 5 minutes */
                break;
            case 7:
                ConfigFragment.startDelay = 10 * 60; /* 10 minutes */
                break;
            case 8:
                ConfigFragment.startDelay = 30 * 60; /* 30 minutes */
                break;
            case 9:
                ConfigFragment.startDelay = 60 * 60; /* 60 minutes */
                break;
            case 10:
                ConfigFragment.startDelay = ConfigFragment.DELAY_START_INDEFINITELY; /* only start after receiving an external trigger */
                break;
        }
    }

    static private void SetStartDelay(int startDelay) {
        int number = 0;
        int unit = 0;
        int smallestDiff = 1000000; /* ridiculously high */
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < ConfigFragment.startDelayIds.length; j++) {
                SetStartDelay(i, j);
                int currentDiff = Math.abs(startDelay - ConfigFragment.startDelay);
                if (currentDiff < smallestDiff) {
                    number = i;
                    unit = j;
                    smallestDiff = currentDiff;
                }
            }
        }
        SetStartDelay(number, unit);
    }

    static private void SetRunningTime(int number, int unit) {
        ConfigFragment.runningTimeNumber = number;
        ConfigFragment.runningTimeUnit = unit;

        switch (unit) { /* unit is the index in the ordered array runningTimeIds */
            case 0:
            default:
                ConfigFragment.runningTime = 0; /* run indefinitely */
                break;
            case 1:
                ConfigFragment.runningTime = 30 * 60; /* run for 30 minutes */
                break;
            case 2:
                ConfigFragment.runningTime = 60 * 60; /* run for 60 minutes */
                break;
            case 3:
                ConfigFragment.runningTime = 2 * 60 * 60; /* run for 2 hours */
                break;
            case 4:
                ConfigFragment.runningTime = 4 * 60 * 60; /* run for 4 hours */
                break;
            case 5:
                ConfigFragment.runningTime = 8 * 60 * 60; /* run for 8 hours */
                break;
            case 6:
                ConfigFragment.runningTime = 12 * 60 * 60; /* run for 12 hours */
                break;
            case 7:
                ConfigFragment.runningTime = 24 * 60 * 60; /* run for 1 day */
                break;
            case 8:
                ConfigFragment.runningTime = 2 * 24 * 60 * 60; /* run for 2 days */
                break;
            case 9:
                ConfigFragment.runningTime = 3 * 24 * 60 * 60; /* run for 3 days */
                break;
            case 10:
                ConfigFragment.runningTime = 5 * 24 * 60 * 60; /* run for 5 days */
                break;
            case 11:
                ConfigFragment.runningTime = 7 * 24 * 60 * 60; /* run for 1 week */
                break;
        }
    }

    static private void SetRunningTime(int runningTime) {
        int number = 0;
        int unit = 0;
        int smallestDiff = 1000000; /* ridiculously high */
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < ConfigFragment.runningTimeIds.length; j++) {
                SetRunningTime(i, j);
                int currentDiff = Math.abs(runningTime - ConfigFragment.runningTime);
                if (currentDiff < smallestDiff) {
                    number = i;
                    unit = j;
                    smallestDiff = currentDiff;
                }
            }
        }
        SetRunningTime(number, unit);
    }

    static private void SetValidMinimum(int number, int unit) {
        ConfigFragment.validMinimumNumber = number;
        ConfigFragment.validMinimumUnit = unit;
        ConfigFragment.validMinimum = ConfigFragment.thresholds[number] * 10;
    }

    static private void SetValidMinimum(int validMinimum) {
        int number = 0;
        int unit = 0;
        int smallestDiff = 1000000; /* ridiculously high */
        for (int i = 0; i < ConfigFragment.thresholds.length; i++) {
            for (int j = 0; j < thresholdUnitIds.length; j++) {
                SetValidMinimum(i, j);
                int currentDiff = Math.abs(validMinimum - ConfigFragment.validMinimum);
                if (currentDiff < smallestDiff) {
                    number = i;
                    unit = j;
                    smallestDiff = currentDiff;
                }
            }
        }
        SetValidMinimum(number, unit);
    }

    static private void SetValidMaximum(int number, int unit) {
        ConfigFragment.validMaximumNumber = number;
        ConfigFragment.validMaximumUnit = unit;
        ConfigFragment.validMaximum = ConfigFragment.thresholds[number] * 10;
    }

    static private void SetValidMaximum(int validMaximum) {
        int number = 0;
        int unit = 0;
        int smallestDiff = 1000000; /* ridiculously high */
        for (int i = 0; i < ConfigFragment.thresholds.length; i++) {
            for (int j = 0; j < thresholdUnitIds.length; j++) {
                SetValidMaximum(i, j);
                int currentDiff = Math.abs(validMaximum - ConfigFragment.validMaximum);
                if (currentDiff < smallestDiff) {
                    number = i;
                    unit = j;
                    smallestDiff = currentDiff;
                }
            }
        }
        SetValidMaximum(number, unit);
    }

    static private void CheckAndSwapMinimumMaximum() {
        if (ConfigFragment.validMaximum < ConfigFragment.validMinimum) {
            int swap = ConfigFragment.validMaximum;
            SetValidMaximum(ConfigFragment.validMinimum);
            SetValidMinimum(swap);
        }
    }

    static public void storePreferences(SharedPreferences.Editor editor) {
        editor.putInt("ConfigFragment.interval", ConfigFragment.interval);
        editor.putInt("ConfigFragment.startDelay", ConfigFragment.startDelay);
        editor.putInt("ConfigFragment.runningTime", ConfigFragment.runningTime);
        editor.putInt("ConfigFragment.validMinimum", ConfigFragment.validMinimum);
        editor.putInt("ConfigFragment.validMaximum", ConfigFragment.validMaximum);
        editor.putBoolean("ConfigFragment.validationEnabled", ConfigFragment.validationEnabled);
    }

    static public void restorePreferences(SharedPreferences preferences) {
        SetInterval(preferences.getInt("ConfigFragment.interval", 10));
        SetStartDelay(preferences.getInt("ConfigFragment.startDelay", 5));
        SetRunningTime(preferences.getInt("ConfigFragment.runningTime", 30 * 60));
        SetValidMinimum(preferences.getInt("ConfigFragment.validMinimum", 0));
        SetValidMaximum(preferences.getInt("ConfigFragment.validMaximum", 400));
        ConfigFragment.validationEnabled = preferences.getBoolean("ConfigFragment.validationEnabled", true);
    }

    public void NumberPickerPopup(final View view) {
        final TextView intervalTextView = view.findViewById(intervalConfigTextView);
        final TextView startDelayTextView = view.findViewById(startDelayConfigTextView);
        final TextView runningTimeTextView = view.findViewById(runningTimeConfigTextView);
        final TextView validMinimumTextView = view.findViewById(validMinimumConfigTextView);
        final TextView validMaximumTextView = view.findViewById(validMaximumConfigTextView);

        int[] values = null;
        int[] unitIds = null;
        int selectedValue = 0;
        int selectedUnit = 0;
        if (view == intervalTextView) {
            values = ConfigFragment.intervals;
            unitIds = ConfigFragment.intervalUnitIds;
            selectedValue = ConfigFragment.intervalNumber;
            selectedUnit = ConfigFragment.intervalUnit;
        } else if (view == startDelayTextView) {
            unitIds = ConfigFragment.startDelayIds;
            selectedUnit = ConfigFragment.startDelayUnit;
        } else if (view == runningTimeTextView) {
            switch (com.nxp.nhs31xx.demo.tlogger.Message.Message.swVersion) {
                case _1619_8_4_0_:
                case _1638_0_5_0_:
                case _1707_10_5_0_:
                    unitIds = ConfigFragment.oldRunningTimeIds;
                    break;
                case _1748_13_6_0_:
                case UNKNOWN:
                default:
                    unitIds = ConfigFragment.runningTimeIds;
                    break;
            }
            selectedUnit = ConfigFragment.runningTimeUnit;
        } else if (view == validMinimumTextView) {
            values = ConfigFragment.thresholds;
            unitIds = ConfigFragment.thresholdUnitIds;
            selectedValue = ConfigFragment.validMinimumNumber;
            selectedUnit = ConfigFragment.validMinimumUnit;
        } else if (view == validMaximumTextView) {
            values = ConfigFragment.thresholds;
            unitIds = ConfigFragment.thresholdUnitIds;
            selectedValue = ConfigFragment.validMaximumNumber;
            selectedUnit = ConfigFragment.validMaximumUnit;
        }

        LinearLayout linearLayout = new LinearLayout(view.getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

        final NumberPicker numberPicker;
        final NumberPicker unitPicker;
        if (values == null) {
            numberPicker = null;
        } else {
            String[] displayedValues = new String[values.length];
            for (int i = 0; i < displayedValues.length; i++) {
                /* Adding a space avoids a bug in NumberPicker where the display of the initial selected value is wrong
                 * if previously a negative number was selected.
                 */
                displayedValues[i] = Integer.toString(values[i]) + " ";
            }

            numberPicker = new NumberPicker(view.getContext());
            numberPicker.setDisplayedValues(displayedValues);
            numberPicker.setMinValue(0);
            numberPicker.setMaxValue(values.length - 1);
            numberPicker.setValue(selectedValue);
            numberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            numberPicker.setWrapSelectorWheel(false);
            numberPicker.setId(View.NO_ID);
            linearLayout.addView(numberPicker);
        }
        if (unitIds == null) {
            unitPicker = null;
        } else {
            String[] unitStrings = new String[unitIds.length];
            for (int i = 0; i < unitStrings.length; i++) {
                unitStrings[i] = getString(unitIds[i]) + " ";
            }
            unitPicker = new NumberPicker(view.getContext());
            unitPicker.setDisplayedValues(unitStrings);
            unitPicker.setMinValue(0);
            unitPicker.setMaxValue(unitIds.length - 1);
            unitPicker.setValue(selectedUnit);
            unitPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            unitPicker.setWrapSelectorWheel(false);
            unitPicker.setId(View.NO_ID);
            linearLayout.addView(unitPicker);
        }

        final android.support.v7.app.AlertDialog.Builder alert = new android.support.v7.app.AlertDialog.Builder(view.getContext());
        alert.setCancelable(false);
        alert.setView(linearLayout);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                int number = (numberPicker == null) ? 0 : numberPicker.getValue();
                int unit = (unitPicker == null) ? 0 : unitPicker.getValue();
                update(view, number, unit);
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }

    /* -------------------------------------------------------------------------------- */

    public ConfigFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_config, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        enable(MainActivity.isConnected);
        View view = this.getView();
        if (view != null) {
            final TextView intervalTextView = view.findViewById(intervalConfigTextView);
            final TextView startDelayTextView = view.findViewById(startDelayConfigTextView);
            final TextView runningTimeTextView = view.findViewById(runningTimeConfigTextView);
            final Switch validationSwitch = view.findViewById(validationConfigSwitch);
            final TextView validMinimumTextView = view.findViewById(validMinimumConfigTextView);
            final TextView validMaximumTextView = view.findViewById(validMaximumConfigTextView);
            update(intervalTextView, ConfigFragment.intervalNumber, ConfigFragment.intervalUnit);
            update(startDelayTextView, ConfigFragment.startDelayNumber, ConfigFragment.startDelayUnit);
            update(runningTimeTextView, ConfigFragment.runningTimeNumber, ConfigFragment.runningTimeUnit);
            update(validationSwitch, ConfigFragment.validationEnabled);
            update(validMinimumTextView, ConfigFragment.validMinimumNumber, ConfigFragment.validMinimumUnit);
            update(validMaximumTextView, ConfigFragment.validMaximumNumber, ConfigFragment.validMaximumUnit);
            update(com.nxp.nhs31xx.demo.tlogger.Message.Message.swVersion);
        }
    }

    public void enable(boolean enable) {
        View view = this.getView();
        if (view != null) {
            Button button;
            button = view.findViewById(R.id.resetConfigButton);
            if (button != null) {
                button.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
            }
            button = view.findViewById(R.id.applyConfigButton);
            if (button != null) {
                button.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
            }
        }
    }

    public void update(Message.SwVersion swVersion) {
        View view = this.getView();
        if (view != null) {
            final TextView startDelayTextView = view.findViewById(startDelayConfigTextView);
            switch (Message.swVersion) {
                case _1619_8_4_0_:
                case _1638_0_5_0_:
                case _1707_10_5_0_:
                case UNKNOWN:
                    startDelayTextView.setVisibility(View.GONE);
                    break;
                case _1748_13_6_0_:
                default:
                    startDelayTextView.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    public void update(View changedView, int number, int unit) {
        View view = this.getView();
        if ((view != null) && (changedView != null)) {
            final TextView intervalTextView = view.findViewById(intervalConfigTextView);
            final TextView startDelayTextView = view.findViewById(startDelayConfigTextView);
            final TextView runningTimeTextView = view.findViewById(runningTimeConfigTextView);
            final TextView validMinimumTextView = view.findViewById(validMinimumConfigTextView);
            final TextView validMaximumTextView = view.findViewById(validMaximumConfigTextView);

            if (changedView == intervalTextView) {
                SetInterval(number, unit);
                String s = getString(ConfigFragment.intervalUnitIds[unit]);
                intervalTextView.setText(String.format(getString(R.string.interval_configRule), intervals[number], s));
                switch (Message.swVersion) {
                    case _1619_8_4_0_:
                    case _1638_0_5_0_:
                    case _1707_10_5_0_:
                    case UNKNOWN:
                        /* The firmware does not expect a separate parameter to set a delay when to measure a first
                         * temperature value. Instead, it is re-using the interval for this. The start delay is here
                         * equal to the measurement interval.
                         */
                        ConfigFragment.startDelay = ConfigFragment.interval;
                        break;
                    case _1748_13_6_0_:
                    default:
                        /* ConfigFragment.startDelay is set separately. */
                        break;
                }
            } else if (changedView == startDelayTextView) {
                SetStartDelay(number, unit);
                if (ConfigFragment.startDelay == 0) {
                    startDelayTextView.setText(R.string.noStartDelay_configRule);
                } else if (ConfigFragment.startDelay == ConfigFragment.DELAY_START_INDEFINITELY) {
                    startDelayTextView.setText(R.string.indefiniteStartDelay_configRule);
                } else {
                    String s = getString(ConfigFragment.startDelayIds[unit]);
                    startDelayTextView.setText(String.format(getString(R.string.startDelay_configRule), s));
                }
            } else if (changedView == runningTimeTextView) {
                SetRunningTime(number, unit);
                if (ConfigFragment.runningTime == 0) {
                    runningTimeTextView.setText(R.string.noLimitRunningTime_configRule);
                } else {
                    String s = getString(ConfigFragment.runningTimeIds[unit]);
                    runningTimeTextView.setText(String.format(getString(R.string.limitRunningTime_configRule), s));
                }
            } else if ((changedView == validMinimumTextView) || (changedView == validMaximumTextView)) {
                if (changedView == validMinimumTextView) {
                    SetValidMinimum(number, unit);
                    CheckAndSwapMinimumMaximum();
                } else /* (changedView == validMaximumTextView) */ {
                    SetValidMaximum(number, unit);
                    CheckAndSwapMinimumMaximum();
                }
                String s;
                int n;
                s = getString(ConfigFragment.thresholdUnitIds[ConfigFragment.validMinimumUnit]);
                n = ConfigFragment.thresholds[ConfigFragment.validMinimumNumber];
                validMinimumTextView.setText(String.format(getString(R.string.lowThreshold_configRule), n, s));
                s = getString(ConfigFragment.thresholdUnitIds[ConfigFragment.validMaximumUnit]);
                n = ConfigFragment.thresholds[ConfigFragment.validMaximumNumber];
                validMaximumTextView.setText(String.format(getString(R.string.highThreshold_configRule), n, s));
            }
        }
    }

    public void update(Switch changedSwitch, boolean enabled) {
        if (changedSwitch != null) {
            changedSwitch.setChecked(enabled);
            update(changedSwitch);
        }
    }

    public void update(Switch changedSwitch) {
        View view = this.getView();
        if ((view != null) && (changedSwitch != null)) {
            final Switch validationSwitch = view.findViewById(validationConfigSwitch);
            final TextView validMinimumTextView = view.findViewById(validMinimumConfigTextView);
            final TextView validMaximumTextView = view.findViewById(validMaximumConfigTextView);
            if (changedSwitch == validationSwitch) {
                ConfigFragment.validationEnabled = changedSwitch.isChecked();
                if (ConfigFragment.validationEnabled) {
                    validationSwitch.setText(R.string.validationEnabled_label);
                    validMinimumTextView.setVisibility(View.VISIBLE);
                    validMaximumTextView.setVisibility(View.VISIBLE);
                } else {
                    validationSwitch.setText(R.string.validationDisabled_label);
                    validMinimumTextView.setVisibility(View.GONE);
                    validMaximumTextView.setVisibility(View.GONE);
                }
            }
        }
    }

    public static int getInterval() {
        return ConfigFragment.interval;
    }

    public static int getStartDelay() {
        int startDelay;
        switch (Message.swVersion) {
            case _1619_8_4_0_:
            case _1638_0_5_0_:
            case _1707_10_5_0_:
            case UNKNOWN:
                /* The firmware does not expect a separate parameter to set a delay when to measure a first
                 * temperature value. Instead, it is re-using the interval for this. The start delay is here
                 * equal to the measurement interval.
                 */
                startDelay = ConfigFragment.interval;
                break;
            case _1748_13_6_0_:
            default:
                startDelay = ConfigFragment.startDelay;
                break;
        }
        return startDelay;
    }

    public static int getRunningTime() {
        return ConfigFragment.runningTime;
    }

    public static int getValidMinimum() {
        return ConfigFragment.validationEnabled ? ConfigFragment.validMinimum : 0;
    }

    public static int getValidMaximum() {
        return ConfigFragment.validationEnabled ? ConfigFragment.validMaximum : 0;
    }
}
