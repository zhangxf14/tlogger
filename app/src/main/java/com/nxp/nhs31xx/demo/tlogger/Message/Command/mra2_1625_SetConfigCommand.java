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

import com.nxp.nhs31xx.demo.tlogger.Helper.Util;
import com.nxp.nhs31xx.demo.tlogger.Message.Message;

public class mra2_1625_SetConfigCommand extends Command {
    public static final Parcelable.Creator<mra2_1625_SetConfigCommand> CREATOR = new Parcelable.Creator<mra2_1625_SetConfigCommand>() {

        public mra2_1625_SetConfigCommand createFromParcel(Parcel in) {
            return new mra2_1625_SetConfigCommand(in);
        }

        public mra2_1625_SetConfigCommand[] newArray(int size) {
            return new mra2_1625_SetConfigCommand[size];
        }
    };
    private static final int LENGTH = 13;

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    public mra2_1625_SetConfigCommand(int interval, int startDelay, int runningTime, int validMinimum, int validMaximum) {
        super(Message.Id.SETCONFIG, LENGTH);

        long epoch = System.currentTimeMillis() / 1000;
        data[2] = (byte) (epoch & 0x000000FF);
        data[3] = (byte) ((epoch >> 8) & 0x000000FF);
        data[4] = (byte) ((epoch >> 16) & 0x000000FF);
        data[5] = (byte) ((epoch >> 24) & 0x000000FF);

        interval = Math.max(0, Math.min(0xFFFF, interval));
        data[6] = (byte) (interval & 0x00FF);
        data[7] = (byte) ((interval >> 8) & 0x00FF);

        if (validMinimum > validMaximum) {
            int t = validMinimum;
            validMinimum = validMaximum;
            validMaximum = t;
        }

        validMinimum = Math.max(Util.MINIMUM_VALID_TEMPERATURE_VALUE, validMinimum);
        data[8] = (byte) (validMinimum & 0x000000FF);
        data[9] = (byte) ((validMinimum >> 8) & 0x000000FF);

        validMaximum = Math.min(Util.MAXIMUM_VALID_TEMPERATURE_VALUE, validMaximum);
        data[10] = (byte) (validMaximum & 0x000000FF);
        data[11] = (byte) ((validMaximum >> 8) & 0x000000FF);

        data[12] = (byte) ((runningTime > 0) ? 1 : 0);
    }

    protected mra2_1625_SetConfigCommand(Parcel in) {
        super(in);
    }
}
