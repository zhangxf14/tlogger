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
import com.nxp.nhs31xx.demo.tlogger.Message.Message;
import com.nxp.nhs31xx.demo.tlogger.R;

import static com.nxp.nhs31xx.demo.tlogger.Message.Message.swVersion;

public class GetVersionResponse extends Response {
    public static final Parcelable.Creator<GetVersionResponse> CREATOR = new Parcelable.Creator<GetVersionResponse>() {

        public GetVersionResponse createFromParcel(Parcel in) {
            return new GetVersionResponse(in);
        }

        public GetVersionResponse[] newArray(int size) {
            return new GetVersionResponse[size];
        }
    };

    public GetVersionResponse() {
        super();
        LENGTH = 16;
    }

    private GetVersionResponse(Parcel in) {
        super(in);
    }

    @Override
    protected void SetData(byte[] data) throws ArrayIndexOutOfBoundsException {
        super.SetData(data);
        int swMajor = getSwMajor();
        int swMinor = getSwMinor();
        int apiMajor = getApiMajor();
        int apiMinor = getApiMinor();
        swVersion = com.nxp.nhs31xx.demo.tlogger.Message.Message.SwVersion.GetEnum(swMajor, swMinor, apiMajor, apiMinor);
        Log.i("swVersion", swVersion.toString());
    }

    /** Indicates the time of build: YYww */
    public int getSwMajor() {
        int v = 0;
        if ((data != null) && (data.length > 5)) {
            v = Util.bytesToInt(new byte[]{data[4], data[5]});
        }
        return v;
    }

    /** Indicates the functionslity and assembly of fields */
    public int getSwMinor() {
        int v = 0;
        if ((data != null) && (data.length > 7)) {
            v = Util.bytesToInt(new byte[]{data[6], data[7]});
        }
        return v;
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    public int getApiMajor() {
        int v = 0;
        if ((data != null) && (data.length > 9)) {
            v = Util.bytesToInt(new byte[]{data[8], data[9]});
        }
        return v;
    }

    public int getApiMinor() {
        int v = 0;
        if ((data != null) && (data.length > 11)) {
            v = Util.bytesToInt(new byte[]{data[10], data[11]});
        }
        return v;
    }
}
