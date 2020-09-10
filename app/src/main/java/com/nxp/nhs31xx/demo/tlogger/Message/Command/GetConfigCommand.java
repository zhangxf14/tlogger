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


package com.nxp.nhs31xx.demo.tlogger.Message.Command;

import android.os.Parcel;
import android.os.Parcelable;

import com.nxp.nhs31xx.demo.tlogger.Message.Message;

public class GetConfigCommand extends Command {
    public static final int LENGTH = 2;

    public static final Parcelable.Creator<GetConfigCommand> CREATOR = new Parcelable.Creator<GetConfigCommand>() {

        public GetConfigCommand createFromParcel(Parcel in) {
            return new GetConfigCommand(in);
        }

        public GetConfigCommand[] newArray(int size) {
            return new GetConfigCommand[size];
        }
    };

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    public GetConfigCommand() {
        super(Message.Id.GETCONFIG);
    }

    private GetConfigCommand(Parcel in) {
        super(in);
    }
}

