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

public class mra2_1552_GetConfigResponse extends mra2_1625_GetConfigResponse {

    public static final Creator<mra2_1552_GetConfigResponse> CREATOR = new Creator<mra2_1552_GetConfigResponse>() {

        public mra2_1552_GetConfigResponse createFromParcel(Parcel in) {
            return new mra2_1552_GetConfigResponse(in);
        }

        public mra2_1552_GetConfigResponse[] newArray(int size) {
            return new mra2_1552_GetConfigResponse[size];
        }
    };

    public mra2_1552_GetConfigResponse() {
        super();
        LENGTH = 21;
    }

    protected mra2_1552_GetConfigResponse(Parcel in) {
        super(in);
    }
}
