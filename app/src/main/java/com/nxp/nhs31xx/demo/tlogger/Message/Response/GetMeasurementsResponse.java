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

import com.nxp.nhs31xx.demo.tlogger.Helper.Util;

public class GetMeasurementsResponse extends Response {
    public static final Parcelable.Creator<GetMeasurementsResponse> CREATOR = new Parcelable.Creator<GetMeasurementsResponse>() {

        public GetMeasurementsResponse createFromParcel(Parcel in) {
            return new GetMeasurementsResponse(in);
        }

        public GetMeasurementsResponse[] newArray(int size) {
            return new GetMeasurementsResponse[size];
        }
    };

    GetMeasurementsResponse() {
        super();
        LENGTH = -12; // Multiple response lengths possible: 12 + 2*n
    }

    GetMeasurementsResponse(Parcel in) {
        super(in);
    }

    public int getOffset() {
        int offset = Integer.MAX_VALUE;
        if (data.length > 7) {
            offset = Util.bytesToInt(new byte[]{data[6], data[7]});
        }
        return offset;
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    public int getCount() {
        int count = 0;
        if (data.length > 8) {
            count = (int) data[8] & 0x000000FF;
        }
        return count;
    }

    public float[] getTemperatures() {
        float[] temperatures = new float[getCount()];
        for (int i = 0; i < getCount(); i++) {
            int t = Util.bytesToInt(new byte[]{data[-LENGTH + 2 * i], data[-LENGTH + 2 * i + 1]});
            if (t >= 0x00008000) {
                t -= 0x10000;
            }
            temperatures[i] = (float) t / 10;
        }
        return temperatures;
    }
}
