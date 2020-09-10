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


package com.nxp.nhs31xx.demo.tlogger.Message.Response;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.nxp.nhs31xx.demo.tlogger.Helper.Util;

public class MeasureTemperatureResponse extends Response {

    public static final Parcelable.Creator<MeasureTemperatureResponse> CREATOR = new Parcelable.Creator<MeasureTemperatureResponse>() {

        public MeasureTemperatureResponse createFromParcel(Parcel in) {
            return new MeasureTemperatureResponse(in);
        }

        public MeasureTemperatureResponse[] newArray(int size) {
            return new MeasureTemperatureResponse[size];
        }
    };

    public MeasureTemperatureResponse() {
        super();
        LENGTH = -6; // Multiple response lengths possible: 6 or 8
    }

    private MeasureTemperatureResponse(Parcel in) {
        super(in);
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    public int getTemperature() {
        int t = Util.MINIMUM_VALID_TEMPERATURE_VALUE; // Default value, to be used for an Immediate response which is to be ignored.
        if (data.length == 8) {
            t = Util.bytesToInt(new byte[]{data[-LENGTH], data[-LENGTH + 1]});
            if (t >= 0x00008000) {
                t -= 0x10000;
            }
            Log.d("T", Integer.toString(t));
        }
        return t;
    }

    public boolean isValid() {
        int t = getTemperature();
        return ((t > Util.MINIMUM_VALID_TEMPERATURE_VALUE) && (t < Util.MAXIMUM_VALID_TEMPERATURE_VALUE));
    }
}
