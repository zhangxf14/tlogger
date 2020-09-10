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

public class MeasureTemperatureCommand extends Command {
    public static final Parcelable.Creator<MeasureTemperatureCommand> CREATOR = new Parcelable.Creator<MeasureTemperatureCommand>() {

        public MeasureTemperatureCommand createFromParcel(Parcel in) {
            return new MeasureTemperatureCommand(in);
        }

        public MeasureTemperatureCommand[] newArray(int size) {
            return new MeasureTemperatureCommand[size];
        }
    };
    private static final int LENGTH = 3;

    public MeasureTemperatureCommand() {
        this(Resolution._12BITS);
    }

    private MeasureTemperatureCommand(Resolution resolution) {
        super(Message.Id.MEASURETEMPERATURE, LENGTH);
        data[2] = (byte) Resolution.GetInt(resolution);
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    private MeasureTemperatureCommand(Parcel in) {
        super(in);
    }

    public enum Resolution {
        _7BITS(2),
        _8BITS(3),
        _9BITS(4),
        _10BITS(5),
        _11BITS(6),
        _12BITS(7);

        private final int resolution;

        Resolution(int resolution) {
            this.resolution = resolution;
        }

        public static Resolution GetEnum(int resolution) {
            Resolution[] resolutions = Resolution.values();
            for (Resolution x : resolutions) {
                if (x.Compare(resolution)) {
                    return x;
                }
            }
            return Resolution._12BITS;
        }

        public static int GetInt(Resolution resolution) {
            return resolution.resolution;
        }

        public boolean Compare(int resolution) {
            return this.resolution == resolution;
        }
    }
}
