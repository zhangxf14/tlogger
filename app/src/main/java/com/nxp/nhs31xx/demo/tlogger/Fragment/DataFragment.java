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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.BaseSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;
import com.nxp.nhs31xx.demo.tlogger.Helper.Util;
import com.nxp.nhs31xx.demo.tlogger.MainActivity;
import com.nxp.nhs31xx.demo.tlogger.Message.Response.GetConfigResponse;
import com.nxp.nhs31xx.demo.tlogger.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class DataFragment extends Fragment {

    private static final int LOGGED_COLOR = 0x7bb1db; // blue company color
//    private static final int GREEN_COLOR = 0xc9d200; // green company color
    private static final int MARKER_COLOR = 0xf9b500; // orange company color

    static public LineGraphSeries<DataPoint> upperLimitLineGraphSeries = null;
    static public LineGraphSeries<DataPoint> lowerLimitLineGraphSeries = null;

    public DataFragment() {
        // Required empty public constructor
    }

    static public void addLoggedDataPoints(final MainActivity mainActivity, List<DataPoint> dataPoints) {
        int startTime = 0;
        double correctionFactor = 0;
        int validMaximum = 0;
        int validMinimum = 0;
        final GetConfigResponse r = mainActivity.lastGetConfigResponse;
        if (r != null) {
            correctionFactor = r.getCorrectionFactor();
            startTime = r.getStartTime();
            validMaximum = r.getValidMaximum();
            validMinimum = r.getValidMinimum();
        }
        DataPoint before = getLast(mainActivity.loggedDataLineGraphSeries);
        DataPoint[] _dataPoints = reworkDataPoints(dataPoints, before, startTime, correctionFactor);
        if ((_dataPoints != null) && (_dataPoints.length > 0)){
            if (mainActivity.loggedDataLineGraphSeries == null) {
                mainActivity.loggedDataLineGraphSeries = new LineGraphSeries<>(_dataPoints);
                initGraphSeries(mainActivity, mainActivity.loggedDataLineGraphSeries, LOGGED_COLOR);
            } else {
                for (DataPoint dataPoint : _dataPoints) {
                    try {
                        if ((dataPoint != null) && (Math.abs(dataPoint.getY() - Util.PLACEHOLDER_TEMPERATURE_VALUE) > 0.001)) {
                            mainActivity.loggedDataLineGraphSeries.appendData(dataPoint, true, 100 * 1024);
                        }
                    } catch (IllegalArgumentException e) {
                        // absorb
                    }
                }
            }
            if (validMinimum < validMaximum) {
                if (upperLimitLineGraphSeries == null) {
                    double x = mainActivity.loggedDataLineGraphSeries.getLowestValueX();
                    upperLimitLineGraphSeries = new LineGraphSeries<>(new DataPoint[] {new DataPoint(x, 0.1 * validMaximum)});
                    initMarkerGraphSeries(upperLimitLineGraphSeries, MARKER_COLOR);
                }
                try {
                    DataPoint d = new DataPoint(_dataPoints[_dataPoints.length-1].getX(), 0.1 * validMaximum);
                    upperLimitLineGraphSeries.appendData(d, true, 100*1024);
                } catch (IllegalArgumentException e) {
                    // absorb
                }
                if (lowerLimitLineGraphSeries == null) {
                    double x = mainActivity.loggedDataLineGraphSeries.getLowestValueX();
                    lowerLimitLineGraphSeries = new LineGraphSeries<>(new DataPoint[] {new DataPoint(x, 0.1 * validMinimum)});
                    initMarkerGraphSeries(lowerLimitLineGraphSeries, MARKER_COLOR);
                }
                try {
                    DataPoint d = new DataPoint(_dataPoints[_dataPoints.length-1].getX(), 0.1 * validMinimum);
                    lowerLimitLineGraphSeries.appendData(d, true, 100*1024);
                } catch (IllegalArgumentException e) {
                    // absorb
                }
            }
        }
    }

    static private void initGraphSeries(final MainActivity mainActivity, LineGraphSeries<DataPoint> lineGraphSeries, int color) {
        if (lineGraphSeries != null) {
            lineGraphSeries.setDrawDataPoints(true);
            lineGraphSeries.setDataPointsRadius(3.5f);
            lineGraphSeries.setBackgroundColor(0x77000000 | color);
            lineGraphSeries.setDrawBackground(true);
            lineGraphSeries.setColor(0xFF000000 | color);
            lineGraphSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPointInterface) {
                    DateFormat dateTimeInstance = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                    String s = dateTimeInstance.format((long) dataPointInterface.getX());
                    float c = (float) dataPointInterface.getY();
                    mainActivity.showMessage(String.format(Locale.getDefault(), "%.1f °C\n%s", c, s));
                }
            });
        }
    }

    static private void initMarkerGraphSeries(LineGraphSeries<DataPoint> lineGraphSeries, int color) {
        lineGraphSeries.setThickness(6);
        lineGraphSeries.setDrawDataPoints(false);
        lineGraphSeries.setDrawBackground(false);
        lineGraphSeries.setColor(0xFF000000 | color);
    }

    static public DataPoint[] reworkDataPoints(List<DataPoint> dataPoints, DataPoint before, int start, double correctionFactor) {
        DataPoint[] _dataPoints = null;
        if (dataPoints != null) {
            _dataPoints = new DataPoint[dataPoints.size()];
            for (int n = 0; n < dataPoints.size(); n++) {
                DataPoint dataPoint = dataPoints.get(n);
                double x = start*1000 + (dataPoint.getX() - start*1000) * correctionFactor;
                double y = dataPoint.getY();
                if (Math.abs(y - Util.PLACEHOLDER_TEMPERATURE_VALUE) < 0.001) {
                    /* A measurement was due while the NFC field was present. This heats up the IC a lot, not so
                     * much the attached product it is to monitor. The firmware has not taken a measurement, instead
                     * it has inserted a placeholder value.
                     * For this demo, it is chosen to just re-use a valid measurement value in its vicinity.
                     */
                    if (before != null) {
                        y = before.getY();
                    }
                    int i = n + 1;
                    while (i < dataPoints.size()) {
                        DataPoint d = dataPoints.get(i);
                        if (Math.abs(d.getY() - Util.PLACEHOLDER_TEMPERATURE_VALUE) > 0.001) {
                            y = d.getY();
                            break;
                        }
                        i++;
                    }
                    int j = n - 1;
                    while (j >= 0) {
                        DataPoint d = dataPoints.get(j);
                        if (Math.abs(d.getY() - Util.PLACEHOLDER_TEMPERATURE_VALUE) > 0.001) {
                            y = d.getY();
                            break;
                        }
                        j--;
                    }
                    Log.d("rDP", String.format("Placeholder value replaced with %.1f", y));
                }

                _dataPoints[n] = new DataPoint(x, y);
                Log.d("reposition", String.format("%d -> %d", (long) (dataPoint.getX() / 1000), (long) (x / 1000)));
            }
        }
        return _dataPoints;
    }

    private static DataPoint getLast(BaseSeries<DataPoint> baseSeries) {
        DataPoint dataPoint = null;
        if (baseSeries != null) {
            double x = baseSeries.getHighestValueX();
            final Iterator<DataPoint> dataPointIterator = baseSeries.getValues(x, x);
            while (dataPointIterator.hasNext()) {
                dataPoint = dataPointIterator.next();
            }
        }
        return dataPoint;
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        initGraphView();
    }

    public void update(BaseSeries<DataPoint> baseSeries) {
        if (baseSeries != null) {
            View view = this.getView();
            if (view != null) {
                GraphView graphView = view.findViewById(R.id.dataGraphView);
                if (graphView != null) {
                    if (!graphView.getSeries().contains(baseSeries)) {
                        graphView.addSeries(baseSeries);
                    }
                    if (!graphView.getSeries().contains(DataFragment.upperLimitLineGraphSeries)) {
                        if (DataFragment.upperLimitLineGraphSeries != null) {
                            graphView.addSeries(DataFragment.upperLimitLineGraphSeries);
                        }
                    }
                    if (!graphView.getSeries().contains(DataFragment.lowerLimitLineGraphSeries)) {
                        if (DataFragment.lowerLimitLineGraphSeries != null) {
                            graphView.addSeries(DataFragment.lowerLimitLineGraphSeries);
                        }
                    }
                }
            }
        }
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    public void updateViewport() {
        View view = this.getView();
        if (view != null) {
            GraphView graphView = view.findViewById(R.id.dataGraphView);
            if (graphView != null) {
                final Viewport viewport = graphView.getViewport();
                final LineGraphSeries<DataPoint> logged = ((MainActivity) getActivity()).loggedDataLineGraphSeries;
                if (logged == null) {
                    viewport.setYAxisBoundsManual(false);
                } else {
                    viewport.setMinX(logged.getLowestValueX());
                    viewport.setMaxX(logged.getHighestValueX());
                    double maxY = logged.getHighestValueY();
                    double minY = logged.getLowestValueY();
                    final LineGraphSeries<DataPoint> u = DataFragment.upperLimitLineGraphSeries;
                    final LineGraphSeries<DataPoint> l = DataFragment.lowerLimitLineGraphSeries;
                    if ((u != null) && (l != null)) {
                        maxY = Math.max(maxY, u.getHighestValueY());
                        minY = Math.min(minY, l.getLowestValueY());
                    }
                    viewport.setMaxY(maxY + 2);
                    viewport.setMinY(minY - 2);
                    viewport.setYAxisBoundsManual(true);
                }
                graphView.invalidate();
                view.invalidate();
            }
        }
    }

    public void reset() {
        View view = this.getView();
        if (view != null) {
            GraphView graph = view.findViewById(R.id.dataGraphView);
            graph.removeAllSeries();
        }
    }

    private void initGraphView() {
        View view = this.getView();
        if (view != null) {
            final GraphView graphView = view.findViewById(R.id.dataGraphView);
            if (graphView != null) {
                update(((MainActivity) getActivity()).loggedDataLineGraphSeries);
                updateViewport();

                Viewport viewport = graphView.getViewport();
                viewport.setScalable(true);
                viewport.setScrollable(true);

                GridLabelRenderer gridLabelRenderer = graphView.getGridLabelRenderer();
                gridLabelRenderer.setGridColor(R.color.nxp_blue);
                gridLabelRenderer.setHighlightZeroLines(false);
                gridLabelRenderer.setHorizontalLabelsColor(R.color.nxp_blue);
                gridLabelRenderer.setVerticalLabelsColor(R.color.nxp_blue);
                gridLabelRenderer.setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
                gridLabelRenderer.setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity(), new SimpleDateFormat("HH:mm", Locale.US)));
                gridLabelRenderer.setNumVerticalLabels(10);
                gridLabelRenderer.setNumHorizontalLabels(6);
                gridLabelRenderer.setHorizontalLabelsAngle(30);
                gridLabelRenderer.setHumanRounding(true);
                gridLabelRenderer.reloadStyles();
            }
        }
    }
}
