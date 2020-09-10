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

public class SetConfigResponse extends Response {

    public static final Parcelable.Creator<SetConfigResponse> CREATOR = new Parcelable.Creator<SetConfigResponse>() {

        public SetConfigResponse createFromParcel(Parcel in) {
            return new SetConfigResponse(in);
        }

        public SetConfigResponse[] newArray(int size) {
            return new SetConfigResponse[size];
        }
    };

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    public SetConfigResponse() {
        super();
        LENGTH = 6;
    }

    public SetConfigResponse(byte[] data) {
        super(data);
    }

    private SetConfigResponse(Parcel in) {
        super(in);
    }
}
