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


package com.nxp.nhs31xx.demo.tlogger;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.nxp.nhs31xx.demo.tlogger.Fragment.AboutFragment;
import com.nxp.nhs31xx.demo.tlogger.Fragment.ConfigFragment;
import com.nxp.nhs31xx.demo.tlogger.Fragment.DataFragment;
import com.nxp.nhs31xx.demo.tlogger.Fragment.StatusFragment;
import com.nxp.nhs31xx.demo.tlogger.Helper.ConfigStorage;
import com.nxp.nhs31xx.demo.tlogger.Helper.DataPointsStorage;
import com.nxp.nhs31xx.demo.tlogger.Helper.MyPagerAdapter;
import com.nxp.nhs31xx.demo.tlogger.Helper.Util;
import com.nxp.nhs31xx.demo.tlogger.Message.Command.Command;
import com.nxp.nhs31xx.demo.tlogger.Message.Command.GetConfigCommand;
import com.nxp.nhs31xx.demo.tlogger.Message.Command.GetMeasurementsCommand;
import com.nxp.nhs31xx.demo.tlogger.Message.Command.GetResponseCommand;
import com.nxp.nhs31xx.demo.tlogger.Message.Command.GetVersionCommand;
import com.nxp.nhs31xx.demo.tlogger.Message.Command.MeasureTemperatureCommand;
import com.nxp.nhs31xx.demo.tlogger.Message.Command.SetConfigCommand;
import com.nxp.nhs31xx.demo.tlogger.Message.Command.mra2_1625_SetConfigCommand;
import com.nxp.nhs31xx.demo.tlogger.Message.Response.GetConfigResponse;
import com.nxp.nhs31xx.demo.tlogger.Message.Response.GetMeasurementsResponse;
import com.nxp.nhs31xx.demo.tlogger.Message.Response.GetVersionResponse;
import com.nxp.nhs31xx.demo.tlogger.Message.Response.MeasureTemperatureResponse;
import com.nxp.nhs31xx.demo.tlogger.Message.Response.Response;
import com.nxp.nhs31xx.demo.tlogger.Message.Response.SetConfigResponse;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;

import static com.nxp.nhs31xx.demo.tlogger.Message.Message.swVersion;

public class MainActivity extends FragmentActivity {
    public static boolean swipeHelpGiven = false;
    public static boolean nfcUnlocked = false;
    public static boolean isConnected = false;
    public String lastUsedTagId = null;
    public GetConfigResponse lastGetConfigResponse = null;
    public GetVersionResponse lastGetVersionResponse = null;
    public MeasureTemperatureResponse lastMeasureTemperatureResponse = null;
    public LineGraphSeries<DataPoint> loggedDataLineGraphSeries = null;
    private boolean goingToIdle = false;

    private GetMeasurementsResponse lastGetMeasurementsResponse = null;
    private Handler handler;
    private NfcAdapter nfcAdapter = null;
    private PendingIntent pendingIntent = null;

    private CommunicationThread communicationThread = null;
    private Timer statusCommandsTimer = null;
    private Timer dataCommandsTimer = null;
    private Timer liveCommandsTimer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), this);
        ViewPager viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(myPagerAdapter);

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                if (message.obj == null) {
                    disconnect();
                    showMessage(R.string.connectionLost_message, Snackbar.LENGTH_LONG);
                } else if (message.obj instanceof Integer) {
                    disconnect();
                    showMessage(getResources().getString((Integer) message.obj), Snackbar.LENGTH_INDEFINITE);
                } else if (message.obj instanceof com.nxp.nhs31xx.demo.tlogger.Message.Message.Id) {
                    Message communicationMessage;
                    communicationMessage = Message.obtain();
                    communicationMessage.obj = createCommand((com.nxp.nhs31xx.demo.tlogger.Message.Message.Id) (message.obj));
                    (communicationThread.getHandler()).sendMessage(communicationMessage);
                } else if (message.obj instanceof Boolean) {
                    Message communicationMessage;
                    communicationMessage = Message.obtain();
                    communicationMessage.obj = message.obj;
                    (communicationThread.getHandler()).sendMessage(communicationMessage);
                } else if (message.obj instanceof Response) {
                    handleResponse((Response) message.obj);
                } else {
                    showMessage(message.obj.toString());
                }
            }
        };
        communicationThread = new CommunicationThread(handler);
        communicationThread.setPriority(Thread.NORM_PRIORITY - 1);
        communicationThread.start();

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        restoreInstanceState(savedInstanceState);
        restorePreferences();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        nfcAdapter.disableForegroundDispatch(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);

        /* Take care to only show one toast message. If more than one is given, a Null pointer
         * exception _may_ be triggered at startup inside an internal getVisibility() call, executed
         * during a background UI thread - impossible to try - catch.
         */
        if (!MainActivity.swipeHelpGiven) {
            showMessage(R.string.swipeHelp, Snackbar.LENGTH_INDEFINITE);
            MainActivity.swipeHelpGiven = true;
        } else {
            //showMessage("This is a TEST version only.", Snackbar.LENGTH_LONG);
        }
    }

    @Override
    protected void onStop() {
        storePreferences();
        super.onStop();
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);

        if (!MainActivity.nfcUnlocked) {
            try {
                getAboutFragment().popupAcceptWebView();
            } catch (IllegalArgumentException e) {
                // absorb
            }
            showMessage(R.string.tagConnectionRefused_message, Snackbar.LENGTH_INDEFINITE);
            return;
        }
        lastGetConfigResponse = null;
        lastGetVersionResponse = null;
        lastMeasureTemperatureResponse = null;
        lastGetMeasurementsResponse = null;

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            String tagId = Util.bytesToHexString(tag.getId(), ':');
            Message communicationMessage;
            communicationMessage = Message.obtain();
            communicationMessage.obj = tag;
            (communicationThread.getHandler()).sendMessage(communicationMessage);

            if ((lastUsedTagId == null) || (!lastUsedTagId.equals(tagId))) {
                try {
                    getDataFragment().reset();
                } catch (IllegalArgumentException e) {
                    // absorb
                }

                lastUsedTagId = tagId;
                loggedDataLineGraphSeries = null;
                lastGetMeasurementsResponse = null;
                DataFragment.upperLimitLineGraphSeries = null;
                DataFragment.lowerLimitLineGraphSeries = null;
                try {
                    getDataFragment().reset();
                } catch (IllegalArgumentException e) {
                    // absorb
                }
            }

            Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] duration = {15, 30, 60, 90};
            vib.vibrate(duration, -1);
            showMessage(String.format(getString(R.string.tagFound_message), lastUsedTagId));
            isConnected = true;
            startTimers(true, false, false);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void disconnect() {
        stopTimers(true, true, true);
        try {
            getConfigFragment().enable(false);
        } catch (IllegalArgumentException e) {
            // absorb
        }
        try {
            getAboutFragment().closePopup("upgrade");
        } catch (IllegalArgumentException e) {
            // absorb
        }
        isConnected = false;
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    public void showMessage(String message) {
        showMessage(message, Snackbar.LENGTH_SHORT);
    }

    private void showMessage(int resourceId) {
        showMessage(resourceId, Snackbar.LENGTH_SHORT);
    }

    private void showMessage(int resourceId, int displayLength) {
        showMessage(getResources().getString(resourceId), displayLength);
    }

    private void showMessage(String message, int displayLength) {
        View view = getWindow().getDecorView().getRootView();
        final Snackbar snackbar = Snackbar.make(view, message + "\n", displayLength);
        TextView textView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setBackgroundResource(R.color.nxp_deepBlue);
        textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
        textView.setLines(4);
        if (displayLength == Snackbar.LENGTH_INDEFINITE) {
            snackbar.setAction("x", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                }
            });
        }
        snackbar.show();
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    private void startTimers(boolean status, boolean data, boolean live) {
        if (status && (statusCommandsTimer == null)) {
            statusCommandsTimer = new Timer();
            statusCommandsTimer.schedule(new TimedCommand(handler, TimedCommand.Purpose.STATUS), 1111, 1111);
            Log.d("startTimers", "status");
        }
        if (data && (dataCommandsTimer == null)) {
            dataCommandsTimer = new Timer();
            dataCommandsTimer.schedule(new TimedCommand(handler, TimedCommand.Purpose.STORED), 1, 4444);
            Log.d("startTimers", "data");
        }
        if (live && (liveCommandsTimer == null)) {
            liveCommandsTimer = new Timer();
            liveCommandsTimer.schedule(new TimedCommand(handler, TimedCommand.Purpose.LIVE), 1, 1111);
            Log.d("startTimers", "live");
        }
    }

    private boolean stopTimers(boolean status, boolean data, boolean live) {
        boolean timersStopped = false;
        if (status && (statusCommandsTimer != null)) {
            statusCommandsTimer.cancel();
            statusCommandsTimer.purge();
            statusCommandsTimer = null;
            Log.d("stopTimers", "status");
            timersStopped = true;
        }
        if (data && (dataCommandsTimer != null)) {
            dataCommandsTimer.cancel();
            dataCommandsTimer.purge();
            dataCommandsTimer = null;
            Log.d("stopTimers", "data");
            timersStopped = true;
        }
        if (live && (liveCommandsTimer != null)) {
            liveCommandsTimer.cancel();
            liveCommandsTimer.purge();
            liveCommandsTimer = null;
            Log.d("stopTimers", "live");
            timersStopped = true;
        }
        return timersStopped;
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    public Fragment getCurrentFragment() throws IllegalArgumentException {
        Fragment currentFragment = null;
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if ((fragment.isVisible()) && (fragment.getView() != null) && (fragment.getView().getContext() != null) && (fragment.getUserVisibleHint())) {
                    currentFragment = fragment;
                }
            }
        }
        return currentFragment;
    }

    private AboutFragment getAboutFragment() throws IllegalArgumentException {
        AboutFragment aboutFragment = null;
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if ((fragment != null) && (fragment instanceof AboutFragment)) {
                    aboutFragment = (AboutFragment) fragment;
                }
            }
            if (aboutFragment == null) {
                FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
                aboutFragment = new AboutFragment();
                if (fragments.size() == 0) {
                    transaction.add(0, aboutFragment);
                } else {
                    transaction.replace(0, aboutFragment);
                }
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }
        return aboutFragment;
    }

    private StatusFragment getStatusFragment() throws IllegalArgumentException {
        StatusFragment statusFragment = null;
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if ((fragment != null) && (fragment instanceof StatusFragment)) {
                    statusFragment = (StatusFragment) fragment;
                }
            }
            if (statusFragment == null) {
                FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
                statusFragment = new StatusFragment();
                if (fragments.size() == 0) {
                    transaction.add(0, statusFragment);
                } else {
                    transaction.replace(0, statusFragment);
                }
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }
        return statusFragment;
    }

    private ConfigFragment getConfigFragment() throws IllegalArgumentException {
        ConfigFragment configFragment = null;
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if ((fragment != null) && (fragment instanceof ConfigFragment)) {
                    configFragment = (ConfigFragment) fragment;
                }
            }
            if (configFragment == null) {
                FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
                configFragment = new ConfigFragment();
                if (fragments.size() == 0) {
                    transaction.add(0, configFragment);
                } else {
                    transaction.replace(0, configFragment);
                }
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }
        return configFragment;
    }

    private DataFragment getDataFragment() throws IllegalArgumentException {
        DataFragment dataFragment = null;
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if ((fragment != null) && (fragment instanceof DataFragment)) {
                    dataFragment = (DataFragment) fragment;
                }
            }
            if (dataFragment == null) {
                FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
                dataFragment = new DataFragment();
                if (fragments.size() == 0) {
                    transaction.add(0, dataFragment);
                } else {
                    transaction.replace(0, dataFragment);
                }
                transaction.addToBackStack(null);
                transaction.commit();
            }
        }
        return dataFragment;
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    public void onIntervalConfigTextViewClicked(View view) {
        ConfigFragment configFragment = getConfigFragment();
        if (configFragment != null) {
            configFragment.NumberPickerPopup(view);
        }
    }

    public void onStartDelayConfigTextViewClicked(View view) {
        ConfigFragment configFragment = getConfigFragment();
        if (configFragment != null) {
            configFragment.NumberPickerPopup(view);
        }
    }

    public void onRunningTimeConfigTextViewClicked(View view) {
        ConfigFragment configFragment = getConfigFragment();
        if (configFragment != null) {
            configFragment.NumberPickerPopup(view);
        }
    }

    public void onValidationConfigSwitchClicked(View view) {
        ConfigFragment configFragment = getConfigFragment();
        configFragment.update((Switch) view);
    }

    public void onValidMinimumConfigTextViewClicked(View view) {
        ConfigFragment configFragment = getConfigFragment();
        if (configFragment != null) {
            configFragment.NumberPickerPopup(view);
        }
    }

    public void onValidMaximumConfigTextViewClicked(View view) {
        ConfigFragment configFragment = getConfigFragment();
        if (configFragment != null) {
            configFragment.NumberPickerPopup(view);
        }
    }

    public void onApplyConfigButtonClicked(View view) {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        final View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            final IBinder windowToken = currentFocus.getWindowToken();
            if (windowToken != null) {
                inputManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }

        try {
            int interval = ConfigFragment.getInterval();
            int startDelay = ConfigFragment.getStartDelay();
            int validMinimum = ConfigFragment.getValidMinimum();
            int validMaximum = ConfigFragment.getValidMaximum();
            int runningTime = ConfigFragment.getRunningTime();

            if (interval < 10) {
                String s = String.format(getResources().getString(R.string.configNotSent_message), 10, getResources().getString(R.string.seconds));
                showMessage(s, Snackbar.LENGTH_LONG);
                return;
            }
            goingToIdle = false;

            Message communicationMessage;
            communicationMessage = Message.obtain();
            switch (swVersion) {
                case _1619_8_4_0_:
                case _1638_0_5_0_:
                case _1707_10_5_0_:
                case UNKNOWN:
                    communicationMessage.obj = new mra2_1625_SetConfigCommand(interval, startDelay, runningTime, validMinimum, validMaximum);
                    break;
                case _1748_13_6_0_:
                default:
                    communicationMessage.obj = new SetConfigCommand(interval, startDelay, runningTime, validMinimum, validMaximum);
                    break;
            }
            (communicationThread.getHandler()).sendMessage(communicationMessage);
            showMessage(R.string.configSent_message);
            communicationMessage = Message.obtain();
            communicationMessage.obj = new GetConfigCommand();
            (communicationThread.getHandler()).sendMessage(communicationMessage);
        } catch (IllegalArgumentException e) {
            // absorb
        }
    }

    public void onResetConfigButtonClicked(View view) {
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        final View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            final IBinder windowToken = currentFocus.getWindowToken();
            if (windowToken != null) {
                inputManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }

        goingToIdle = true;

        Message communicationMessage;
        communicationMessage = Message.obtain();
        switch (swVersion) {
            case _1619_8_4_0_:
            case _1638_0_5_0_:
            case _1707_10_5_0_:
            case UNKNOWN:
                communicationMessage.obj = new mra2_1625_SetConfigCommand(0, 0, 0, 0, 0);
                break;
            case _1748_13_6_0_:
            default:
                communicationMessage.obj = new SetConfigCommand(0, 0, 0, 0, 0);
                break;
        }
        (communicationThread.getHandler()).sendMessage(communicationMessage);
        showMessage(R.string.configSent_message);
        communicationMessage = Message.obtain();
        communicationMessage.obj = new GetConfigCommand();
        (communicationThread.getHandler()).sendMessage(communicationMessage);

        try {
            getStatusFragment().reset();
        } catch (IllegalArgumentException e) {
            // absorb
        }
        try {
            getDataFragment().reset();
        } catch (IllegalArgumentException e) {
            // absorb
        }
        lastGetMeasurementsResponse = null;
        loggedDataLineGraphSeries = null;
        DataFragment.upperLimitLineGraphSeries = null;
        DataFragment.lowerLimitLineGraphSeries = null;
        deleteData(lastUsedTagId);
        deleteConfig(lastUsedTagId);
    }

    public void onLogoImageViewClicked(View view) {
        try {
            getAboutFragment().popupAcceptWebView();
        } catch (IllegalArgumentException e) {
            // absorb
        }
    }

    public void onAccept1PopupCheckBoxClicked(View view) {
        try {
            getAboutFragment().updateButtonStates();
        } catch (IllegalArgumentException e) {
            // absorb
        }
    }

    public void onAccept2PopupCheckBoxClicked(View view) {
        try {
            getAboutFragment().updateButtonStates();
        } catch (IllegalArgumentException e) {
            // absorb
        }
    }

    public void onAgreePopupButtonClicked(View view) {
        nfcUnlocked = true;
        try {
            getAboutFragment().closePopup();
        } catch (IllegalArgumentException e) {
            // absorb
        }
    }

    public void onDisagreePopupButtonClicked(View view) {
        nfcUnlocked = false;
        try {
            getAboutFragment().closePopup();
        } catch (IllegalArgumentException e) {
            // absorb
        }
    }

    public void onLiveMeasurementsStatusSwitchClicked(View view) {
        try {
            boolean liveMeasurementsIsChecked = getStatusFragment().liveMeasurementsIsChecked();
            if (liveMeasurementsIsChecked) {
                if (isConnected) {
                    startTimers(false, false, true);
                }
            } else {
                stopTimers(false, false, true);
            }
        } catch (IllegalArgumentException e) {
            // absorb
        }
    }

    public void onExportStatusButtonClicked(View view) {
        File baseDir = new File(view.getContext().getExternalFilesDir(null).getAbsolutePath());
        if (!baseDir.exists()) {
            baseDir.mkdir();
        }

        if (lastGetConfigResponse == null) {
            showMessage(R.string.exportNotStarted);
        } else {
            final double correctionFactor = lastGetConfigResponse.getCorrectionFactor();
            new ExportDataAsyncTask().execute(baseDir, lastUsedTagId, correctionFactor, this);
            showMessage(R.string.exportStarted);
            try {
                getStatusFragment().update(true);
            } catch (IllegalArgumentException e) {
                // absorb
            }
        }
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    private int determineGetMeasurementsCommandOffset(String tagId) {
        if ((lastGetConfigResponse == null) || (lastGetConfigResponse.getCount() <= 0)) {
            // First get (correct) status information.
            return -1;
        }

        int offset;
        if (lastGetMeasurementsResponse == null) {
            offset = 0;
            Date storedStart = restoreConfigDate(tagId);
            Date start = new Date((long) lastGetConfigResponse.getStartTime() * 1000);
            if ((storedStart != null) && (storedStart.compareTo(start) == 0)) {
                DataPointsStorage db = new DataPointsStorage(this);
                offset = db.getDataCount(tagId);
            }
        } else {
            offset = lastGetMeasurementsResponse.getOffset() + lastGetMeasurementsResponse.getCount();
        }
        Log.d("offset", Integer.toString(offset));
        return offset;
    }

    private Command createCommand(com.nxp.nhs31xx.demo.tlogger.Message.Message.Id commandId) {
        Command command = null;
        if (isConnected) {
            switch (commandId) {
                case GETRESPONSE:
                    command = new GetResponseCommand();
                    break;
                case MEASURETEMPERATURE:
                    command = new MeasureTemperatureCommand();
                    break;
                case GETVERSION:
                    command = new GetVersionCommand();
                    break;
                case GETMEASUREMENTS:
                    int offset = determineGetMeasurementsCommandOffset(lastUsedTagId);
                    if (offset >= 0) {
                        command = new GetMeasurementsCommand(offset);
                    }
                    break;
                case GETCONFIG:
                    command = new GetConfigCommand();
                    break;
                case SETCONFIG: // Is only created on user request
                default:
                    command = null;
                    break;
            }
        }
        return command;
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    private void handleResponse(Response response) {
        if (!(handleGetVersionResponse(response))) {
            if (isConnected) {
                if (!(handleMeasureTemperatureResponse(response))) {
                    if (!(handleGetMeasurementsResponse(response))) {
                        if (!(handleGetConfigResponse(response))) {
                            if (!(handleSetConfigResponse(response))) {
                                if (!(handleGetMeasurementsResponse(response))) {
                                    Log.d("response", "Unknown response " + response.toString());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean handleGetVersionResponse(Response response) {
        if (response instanceof GetVersionResponse) {
            GetVersionResponse r = (GetVersionResponse) response;
            try {
                getAboutFragment().update(r);
            } catch (IllegalArgumentException e) {
                // absorb
            }
            try {
                getConfigFragment().update(swVersion);
            } catch (IllegalArgumentException e) {
                // absorb
            }
            final com.nxp.nhs31xx.demo.tlogger.Message.Message.HwVersion hwVersion = com.nxp.nhs31xx.demo.tlogger.Message.Message.hwVersion;
            switch (hwVersion) {
                case MRA1:
                    disconnect();
                    try {
                        getAboutFragment().popupUpgradeWebView();
                    } catch (IllegalArgumentException e) {
                        // absorb
                    }
                    break;

                case MRA2:
                    final int apiVersion = r.getApiMajor();
                    final HashMap<com.nxp.nhs31xx.demo.tlogger.Message.Message.HwVersion, HashMap<com.nxp.nhs31xx.demo.tlogger.Message.Message.SwVersion, Integer>> v = com.nxp.nhs31xx.demo.tlogger.Message.Message.minimalMsgApiMajorVersion;
                    if (v.containsKey(hwVersion)
                            && v.get(hwVersion).containsKey(swVersion)
                            && v.get(hwVersion).get(swVersion) <= apiVersion) {
                        lastGetVersionResponse = r;
                    } else {
                        disconnect();
                        showMessage(R.string.tagIncompatible_message, Snackbar.LENGTH_LONG);
                    }
                    break;

                case UNKNOWN:
                default:
                    disconnect();
                    showMessage(R.string.connectionLost_message, Snackbar.LENGTH_LONG);
                    break;
            }
        }
        return (response instanceof GetVersionResponse);
    }

    private boolean handleMeasureTemperatureResponse(Response response) {
        if (response instanceof MeasureTemperatureResponse) {
            MeasureTemperatureResponse r = (MeasureTemperatureResponse) response;
            if (r.isValid()) {
                lastMeasureTemperatureResponse = r;
                try {
                    getStatusFragment().update(r);
                } catch (IllegalArgumentException e) {
                    // absorb
                }
            }
        }
        return (response instanceof MeasureTemperatureResponse);
    }

    private boolean handleGetMeasurementsResponse(Response response) {
        if (response instanceof GetMeasurementsResponse) {
            GetMeasurementsResponse r = (GetMeasurementsResponse) response;
            final int offset = r.getOffset();
            final int count = r.getCount();
            int expectedOffset = determineGetMeasurementsCommandOffset(lastUsedTagId);
            boolean accept;
            if (expectedOffset < 0) {
                accept = false; // Do nothing; wait for other info first.
                Log.d("hGMR", String.format(Locale.getDefault(), "%d < 0", expectedOffset));
            } else if (expectedOffset < offset) {
                accept = false; // Reject response. First get older data.
                Log.d("hGMR", String.format(Locale.getDefault(), "%d < %d", expectedOffset, offset));
            } else if (expectedOffset >= offset + count) {
                accept = false; // Reject response. Get newer data.
                Log.d("hGMR", String.format(Locale.getDefault(), "%d >= %d + %d", expectedOffset, offset, count));
            } else {
                accept = true;
            }
            if (accept) {
                lastGetMeasurementsResponse = r;

                int start = lastGetConfigResponse.getStartTime();
                long interval = Math.max(1, lastGetConfigResponse.getInterval());
                int dataOffset = lastGetMeasurementsResponse.getOffset();
                float[] values = lastGetMeasurementsResponse.getTemperatures();
                int totalReceived = dataOffset + values.length;

                // create array '_dataPoints' from retrieved stored values
                List<DataPoint> _dataPoints = new ArrayList<>();
                for (int n = expectedOffset - dataOffset; n < values.length; n++) {
                    Date date = new Date((start + (dataOffset + n) * interval) * 1000);
                    DataPoint dataPoint = new DataPoint(date, values[n]);
                    _dataPoints.add(dataPoint);
                }
                DataFragment.addLoggedDataPoints(this, _dataPoints);
                storeData(lastUsedTagId, _dataPoints);

                // Determine what to do next
                if (totalReceived < lastGetConfigResponse.getCount()) {
                    Message communicationMessage;
                    communicationMessage = Message.obtain();
                    communicationMessage.obj = createCommand(com.nxp.nhs31xx.demo.tlogger.Message.Message.Id.GETMEASUREMENTS);
                    (communicationThread.getHandler()).sendMessage(communicationMessage);
                } else {
                    if (lastGetConfigResponse.getCount() > 0) {
                        Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        long[] duration = {15, 30, 60, 90};
                        vib.vibrate(duration, -1);
                    }
                    if (stopTimers(false, true, false)) {
                        if (lastGetConfigResponse.getCount() > 0) {
                            int n = lastGetMeasurementsResponse.getOffset() + count;
                            String s = getResources().getQuantityString(R.plurals.allSamplesRetrieved_message, n);
                            showMessage(String.format(s, n), Snackbar.LENGTH_LONG);
                        }
                    }
                }

                try {
                    getStatusFragment().update(totalReceived, lastGetConfigResponse.getCount());
                } catch (IllegalArgumentException e) {
                    // absorb
                }

                /* Present a notification every this x samples so users know reading out the data is still ongoing. */
                final int PROGRESS_BLOCK = 3333; /* Just a number. Any reasonable number will do. */
                if ((dataOffset / PROGRESS_BLOCK) != (totalReceived / PROGRESS_BLOCK)) {
                    if (totalReceived + (PROGRESS_BLOCK / 3) < lastGetConfigResponse.getCount()) {
                        showMessage(String.format(getString(R.string.someSamplesRetrieved_message), totalReceived, lastGetConfigResponse.getCount()));
                    }
                }
            }

            try {
                getDataFragment().update(loggedDataLineGraphSeries);
                getDataFragment().updateViewport();
            } catch (IllegalArgumentException e) {
                // absorb
            }
        }
        return (response instanceof GetMeasurementsResponse);
    }

    private boolean handleGetConfigResponse(Response response) {
        if (response instanceof GetConfigResponse) {
            GetConfigResponse r = (GetConfigResponse) response;
            if (lastGetConfigResponse == null) {
                if (r.getCount() == 0) {
                    showMessage(R.string.emptyTag_message, Snackbar.LENGTH_LONG);
                } else {
                    showMessage(String.format(getString(R.string.nhsTag_message), r.getCount()));
                }
                if (r.countLimitIsReached()) {
                    showMessage(getString(R.string.measurementsStopped_message));
                }
            }

            lastGetConfigResponse = r;
            Date storedStart = restoreConfigDate(lastUsedTagId);
            Date start = new Date((long) lastGetConfigResponse.getStartTime() * 1000);
            if ((storedStart != null) && (storedStart.compareTo(start) == 0)) {
                if (loggedDataLineGraphSeries == null) {
                    final List<DataPoint> dataPoints_ = restoreData(lastUsedTagId);
                    DataFragment.addLoggedDataPoints(this, dataPoints_);
                    try {
                        getDataFragment().update(loggedDataLineGraphSeries);
                        getDataFragment().updateViewport();
                    } catch (IllegalArgumentException e) {
                        // absorb
                    }
                }
            } else {
                lastGetMeasurementsResponse = null;
                loggedDataLineGraphSeries = null;
                DataFragment.upperLimitLineGraphSeries = null;
                DataFragment.lowerLimitLineGraphSeries = null;
                deleteData(lastUsedTagId);
                deleteConfig(lastUsedTagId);

                storeConfig(lastUsedTagId, start, lastGetConfigResponse.getCount());
            }

            try {
                getStatusFragment().update(lastGetConfigResponse);
            } catch (IllegalArgumentException e) {
                // absorb
            }
            try {
                getConfigFragment().enable(true);
            } catch (IllegalArgumentException e) {
                // absorb
            }
            if (lastGetConfigResponse.getCount() == 0) {
                stopTimers(false, true, false);
            } else {
                startTimers(false, true, false);
                Message communicationMessage;
                communicationMessage = Message.obtain();
                communicationMessage.obj = createCommand(com.nxp.nhs31xx.demo.tlogger.Message.Message.Id.GETMEASUREMENTS);
                (communicationThread.getHandler()).sendMessage(communicationMessage);
            }
            onLiveMeasurementsStatusSwitchClicked(null);
        }
        return (response instanceof GetConfigResponse);
    }

    private boolean handleSetConfigResponse(Response response) {
        if (response instanceof SetConfigResponse) {
            SetConfigResponse setConfigResponse = (SetConfigResponse) response;
            Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            long[] duration = {15, 30, 60, 90};
            vib.vibrate(duration, -1);

            boolean operatingUpToNow = (lastGetConfigResponse != null) && (lastGetConfigResponse.getInterval() > 0);
            if (setConfigResponse.getErrorCode() != 0) {
                showMessage(R.string.reconfigRejected_message, Snackbar.LENGTH_LONG);
            } else if (goingToIdle) {
                showMessage(R.string.pristineConfigConfirmed_message, Snackbar.LENGTH_LONG);
            } else if (operatingUpToNow) {
                showMessage(R.string.reconfigConfirmed_message, Snackbar.LENGTH_LONG);
            } else {
                int startDelay = ConfigFragment.getStartDelay();
                if (startDelay == 0) {
                    showMessage(R.string.firstConfigConfirmed_0_message, Snackbar.LENGTH_LONG);
                } else if (startDelay == -1) {
                    showMessage(R.string.firstConfigConfirmed_wait_message, Snackbar.LENGTH_LONG);
                } else {
                    String startDelayUnit = getResources().getString(R.string.seconds);
                    String s = String.format(getResources().getString(R.string.firstConfigConfirmed_message), startDelay, startDelayUnit);
                    showMessage(s, Snackbar.LENGTH_LONG);
                }
            }

            if (setConfigResponse.getErrorCode() == 0) {
                lastGetMeasurementsResponse = null;
                loggedDataLineGraphSeries = null;
                DataFragment.upperLimitLineGraphSeries = null;
                DataFragment.lowerLimitLineGraphSeries = null;
                deleteData(lastUsedTagId);
                deleteConfig(lastUsedTagId);
            }
        }
        return (response instanceof SetConfigResponse);
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveInstanceState(outState);
    }

    private void saveInstanceState(Bundle outState) {
        outState.putParcelable("lastGetConfigResponse", lastGetConfigResponse);
        outState.putParcelable("lastGetVersionResponse", lastGetVersionResponse);
        outState.putParcelable("lastMeasureTemperatureResponse", lastMeasureTemperatureResponse);
        outState.putParcelable("lastGetMeasurementsResponse", lastGetMeasurementsResponse);

        Iterator<DataPoint> values;
        if (loggedDataLineGraphSeries != null) {
            values = loggedDataLineGraphSeries.getValues(loggedDataLineGraphSeries.getLowestValueX(), loggedDataLineGraphSeries.getHighestValueX());
            storeDataPointValues("loggedDataLineGraphSeries", values, outState);
        }
        outState.putString("lastUsedTagId", lastUsedTagId);

        outState.putBoolean("statusCommandsTimer", statusCommandsTimer != null);
        outState.putBoolean("dataCommandsTimer", dataCommandsTimer != null);
        outState.putBoolean("liveCommandsTimer", liveCommandsTimer != null);
    }

    private void restoreInstanceState(Bundle inState) {
        if (inState != null) {
            lastGetConfigResponse = inState.getParcelable("lastGetConfigResponse");
            lastGetVersionResponse = inState.getParcelable("lastGetVersionResponse");
            lastMeasureTemperatureResponse = inState.getParcelable("lastMeasureTemperatureResponse");
            lastGetMeasurementsResponse = inState.getParcelable("lastGetMeasurementsResponse");

            DataFragment.upperLimitLineGraphSeries = null;
            DataFragment.lowerLimitLineGraphSeries = null;
            DataFragment.addLoggedDataPoints(this, restoreDataPointValues("loggedDataLineGraphSeries", inState));
            lastUsedTagId = inState.getString("lastUsedTagId");

            startTimers(inState.getBoolean("statusCommandsTimer"), inState.getBoolean("dataCommandsTimer"), inState.getBoolean("liveCommandsTimer"));
        }
    }

    private void storeDataPointValues(String name, Iterator<DataPoint> values, Bundle outState) {
        if (outState != null) {
            List<Double> x = new ArrayList<>();
            List<Double> y = new ArrayList<>();
            double[] x_;
            double[] y_;
            while (values.hasNext()) {
                DataPoint dataPoint = values.next();
                x.add(dataPoint.getX());
                y.add(dataPoint.getY());
            }
            x_ = new double[x.size()];
            for (int i = 0; i < x.size(); i++) {
                x_[i] = x.get(i);
            }
            outState.putDoubleArray(name + "X", x_);
            y_ = new double[y.size()];
            for (int i = 0; i < y.size(); i++) {
                y_[i] = y.get(i);
            }
            outState.putDoubleArray(name + "Y", y_);
        }
    }

    private List<DataPoint> restoreDataPointValues(String name, Bundle inState) {
        List<DataPoint> dataPoints = null;
        if (inState.containsKey(name + "X") && inState.containsKey(name + "Y")) {
            double[] x = inState.getDoubleArray(name + "X");
            double[] y = inState.getDoubleArray(name + "Y");
            if ((x != null) && (y != null) && (x.length > 0) && (y.length > 0)) {
                dataPoints = new ArrayList<>();
                for (int n = 0; (n < x.length) && (n < y.length); n++) {
                    dataPoints.add(new DataPoint(x[n], y[n]));
                }
            }
        }
        return dataPoints;
    }

    private void storePreferences() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("R.string.hwVersion", this.getString(R.string.version));
        editor.putBoolean("nfcUnlocked", nfcUnlocked);
        editor.putBoolean("swipeHelpGiven", swipeHelpGiven);
        ConfigFragment.storePreferences(editor);
        editor.apply();
    }

    private void restorePreferences() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        nfcUnlocked = preferences.getBoolean("nfcUnlocked", false);
        swipeHelpGiven = preferences.getBoolean("swipeHelpGiven", false);
        ConfigFragment.restorePreferences(preferences);
        if (!preferences.getString("R.string.hwVersion", "").equals(this.getString(R.string.version))) {
            nfcUnlocked = false;
            storePreferences();
        }
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    private void storeData(String tagId, List<DataPoint> dataPoints) {
        if (dataPoints != null) {
            new StoreDataAsyncTask().execute(tagId, dataPoints, this);
        }
    }

    private List<DataPoint> restoreData(String tagId) {
        DataPointsStorage db = new DataPointsStorage(this);
        return db.getDataPoints(tagId);
    }

    private void deleteData(String tagId) {
        DataPointsStorage db = new DataPointsStorage(this);
        db.deleteDataPoints(tagId);
    }

    private void storeConfig(String tagId, Date date, int count) {
        ConfigStorage db = new ConfigStorage(this);
        db.addConfig(tagId, date, count);
    }

    private Date restoreConfigDate(String tagId) {
        ConfigStorage db = new ConfigStorage(this);
        return db.getDate(tagId);
    }

    private int restoreConfigCount(String tagId) {
        ConfigStorage db = new ConfigStorage(this);
        return db.getCount(tagId);
    }

    private void deleteConfig(String tagId) {
        ConfigStorage db = new ConfigStorage(this);
        db.deleteConfig(tagId);
    }

    private class StoreDataAsyncTask extends AsyncTask<Object, Void, Void> {
        protected void onPreExecute() {
            Log.d("StoreDataAsyncTask", "lock");
        }

        protected Void doInBackground(Object... objects) {
            try {
                if (objects.length >= 3) {
                    String tagId = (String) objects[0];
                    List<DataPoint> dataPoints = (List<DataPoint>) objects[1];
                    Context context = (Context) objects[2];
                    DataPointsStorage db = new DataPointsStorage(context);
                    db.addDataPoints(tagId, dataPoints);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onCancelled(Void result) {
            Log.d("StoreDataAsyncTask", "unlock - data store cancelled");
        }

        protected void onPostExecute(Void result) {
            Log.d("StoreDataAsyncTask", "unlock - data stored");
        }
    }

    private class ExportDataAsyncTask extends AsyncTask<Object, Void, Void> {
        protected void onPreExecute() {
            Log.d("ExportDataAsyncTask", "lock");
        }

        protected Void doInBackground(Object... objects) {
            try {
                if (objects.length >= 3) {
                    File baseDir = (File) objects[0];
                    String tagId = (String) objects[1];
                    double correctionFactor = (double) objects[2];
                    Context context = (Context) objects[3];
                    DataPointsStorage db = new DataPointsStorage(context);
                    List<DataPoint> dataPoints = db.getDataPoints(tagId);
                    export(baseDir, dataPoints, correctionFactor);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onCancelled(Void result) {
            Log.d("ExportDataAsyncTask", "unlock - data export cancelled");
            showMessage(String.format(getString(R.string.exportFailed), "cancelled"));
            try {
                getStatusFragment().update(false);
            } catch (IllegalArgumentException e) {
                // absorb
            }
        }

        protected void onPostExecute(Void result) {
            Log.d("ExportDataAsyncTask", "unlock - data exported");
            try {
                getStatusFragment().update(false);
            } catch (IllegalArgumentException e) {
                // absorb
            }
        }

        private void export(File baseDir, List<DataPoint> dataPoints, double correctionFactor) {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);
            final String filename = baseDir.getAbsolutePath() + File.separator + format.format(new Date(System.currentTimeMillis())) + ".csv";
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
//            format.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

            try {
                CSVWriter writer = new CSVWriter(new FileWriter(filename), '\t');
                String[] headers;
                String epochHeader;
                if (correctionFactor == 1) {
                    epochHeader = "drift: n.a.";
                } else {
                    epochHeader = String.format(Locale.US, "drift: %.1f%%", (correctionFactor - 1) * 100);
                }
                headers = new String[]{"tag " + lastUsedTagId, "epoch", epochHeader, "UTC after correction", "C"};
                writer.writeNext(headers);

                Double start = 0.;
                final int size = dataPoints.size();
                if (size > 0) {
                    start = dataPoints.get(0).getX() / 1000;
                }
                for (int n = 0; n < size; n++) {
                    long x = (long) (dataPoints.get(n).getX() / 1000);
                    long xCorrected = (long) (start + (x - start) * correctionFactor);
                    String t = format.format(new Date(xCorrected * 1000));

                    double y = dataPoints.get(n).getY();
                    String v;
                    if (Math.abs(y - Util.PLACEHOLDER_TEMPERATURE_VALUE) < 0.001) {
                        v = ""; /* NFC field present - no measurement taken */
                    } else {
                        v = String.format(Locale.US, "%.1f", y);
                    }

                    String[] values = {(Integer.valueOf(n + 1)).toString(), (Long.valueOf(x)).toString(), (Long.valueOf(xCorrected)).toString(), t, v};
                    writer.writeNext(values);
                }
                writer.close();

                String[] scanArray = new String[]{baseDir.getAbsolutePath(), filename};
                MediaScannerConnection.scanFile(getApplicationContext(), scanArray, null, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        final String[] parts = path.split(File.separator);
                        String s = "";
                        if (parts.length > 4) {
                            for (int i = 4; i < parts.length - 1; i++) {
                                s += parts[i] + File.separator;
                            }
                            s += parts[parts.length - 1];
                        } else {
                            s = path;
                        }
                        showMessage(String.format(Locale.US, getString(R.string.exportFinished), size, s), Snackbar.LENGTH_INDEFINITE);
                    }
                });
            } catch (IOException e) {
                showMessage(String.format(getString(R.string.exportFailed), e.getMessage()));
            }
        }
    }
}
