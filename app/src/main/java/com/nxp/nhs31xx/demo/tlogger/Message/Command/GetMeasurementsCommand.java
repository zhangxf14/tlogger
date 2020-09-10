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

public class GetMeasurementsCommand extends Command {
    public static final Parcelable.Creator<GetMeasurementsCommand> CREATOR = new Parcelable.Creator<GetMeasurementsCommand>() {

        public GetMeasurementsCommand createFromParcel(Parcel in) {
            return new GetMeasurementsCommand(in);
        }

        public GetMeasurementsCommand[] newArray(int size) {
            return new GetMeasurementsCommand[size];
        }
    };
    private static final int LENGTH = 4;

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    public GetMeasurementsCommand(int offset) {
        super(Message.Id.GETMEASUREMENTS, LENGTH);
        data[2] = (byte) (offset & 0x00FF);
        data[3] = (byte) ((offset >> 8) & 0x00FF);
    }

    private GetMeasurementsCommand(Parcel in) {
        super(in);
    }
}
