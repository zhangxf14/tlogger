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

import java.util.Arrays;

public abstract class Command implements Parcelable {
    private static final byte commandTag = 0;
    byte[] data;

    Command(Message.Id id) {
        this(id, 2);
    }

    Command(Message.Id id, int length) {
        data = new byte[length];
        data[0] = (byte) Message.Id.GetInt(id);
        data[1] = commandTag;
    }

    Command(Parcel in) {
        readFromParcel(in);
    }

    public byte[] encode() {
        return data;
    }

    public Message.Id getId() {
        return Message.Id.GetEnum(data[0]);
    }

    public int getTag() {
        return (int) data[1] & 0x000000FF;
    }

    @Override
    public String toString() {
        return "C." + getId().toString() + "." + Integer.toString(data[1]) + " - " + Arrays.toString(data);
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeByteArray(data);
    }

    private void readFromParcel(Parcel in) {
        data = in.createByteArray();
    }

}
