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

public class GetUidResponse extends Response {
    public static final Creator<GetUidResponse> CREATOR = new Creator<GetUidResponse>() {

        public GetUidResponse createFromParcel(Parcel in) {
            return new GetUidResponse(in);
        }

        public GetUidResponse[] newArray(int size) {
            return new GetUidResponse[size];
        }
    };

    public GetUidResponse() {
        super();
        LENGTH = 2 + 16;
    }

    private GetUidResponse(Parcel in) {
        super(in);
    }

    public byte[] getUid() {
        return super.data;
    }
}
