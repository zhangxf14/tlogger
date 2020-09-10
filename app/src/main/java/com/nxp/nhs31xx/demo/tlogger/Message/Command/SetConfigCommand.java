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

public class SetConfigCommand extends Command {
    public static final Parcelable.Creator<SetConfigCommand> CREATOR = new Parcelable.Creator<SetConfigCommand>() {

        public SetConfigCommand createFromParcel(Parcel in) {
            return new SetConfigCommand(in);
        }

        public SetConfigCommand[] newArray(int size) {
            return new SetConfigCommand[size];
        }
    };
    private static final int LENGTH = 20;

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    public SetConfigCommand(int interval, int startDelay, int runningTime, int validMinimum, int validMaximum) {
        super(Message.Id.SETCONFIG, LENGTH);

        long currentTime = System.currentTimeMillis() / 1000;
        data[2] = (byte) (currentTime & 0x000000FF);
        data[3] = (byte) ((currentTime >> 8) & 0x000000FF);
        data[4] = (byte) ((currentTime >> 16) & 0x000000FF);
        data[5] = (byte) ((currentTime >> 24) & 0x000000FF);

        interval = Math.max(0, Math.min(0xFFFF, interval));
        data[6] = (byte) (interval & 0x00FF);
        data[7] = (byte) ((interval >> 8) & 0x00FF);

        data[8] = (byte) (startDelay & 0x000000FF);
        data[9] = (byte) ((startDelay >> 8) & 0x000000FF);
        data[10] = (byte) ((startDelay >> 16) & 0x000000FF);
        data[11] = (byte) ((startDelay >> 24) & 0x000000FF);

        data[12] = (byte) (runningTime & 0x000000FF);
        data[13] = (byte) ((runningTime >> 8) & 0x000000FF);
        data[14] = (byte) ((runningTime >> 16) & 0x000000FF);
        data[15] = (byte) ((runningTime >> 24) & 0x000000FF);

        if (validMinimum > validMaximum) {
            int t = validMinimum;
            validMinimum = validMaximum;
            validMaximum = t;
        }

        validMinimum = Math.max(Util.MINIMUM_VALID_TEMPERATURE_VALUE, validMinimum);
        data[16] = (byte) (validMinimum & 0x000000FF);
        data[17] = (byte) ((validMinimum >> 8) & 0x000000FF);

        validMaximum = Math.min(Util.MAXIMUM_VALID_TEMPERATURE_VALUE, validMaximum);
        data[18] = (byte) (validMaximum & 0x000000FF);
        data[19] = (byte) ((validMaximum >> 8) & 0x000000FF);
    }

    protected SetConfigCommand(Parcel in) {
        super(in);
    }
}
