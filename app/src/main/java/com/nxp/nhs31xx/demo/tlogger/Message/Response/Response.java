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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

public abstract class Response implements Parcelable {
    int LENGTH;
    byte[] data;

    Response() {
    }

    Response(byte[] data) {
        this();
        SetData(data);
    }

    Response(Parcel in) {
        readFromParcel(in);
    }

    public static Response decode(byte[] payload) {
        Message.hwVersion = Message.HwVersion.MRA2;
        Response response = null;
        String responseString = null;
        if (payload == null) {
            Log.d("No data", "payload == null");
        } else if (payload.length < 2) {
            Log.d("Not enough data", Arrays.toString(payload));
        } else {
            boolean ok;
            switch (Message.swVersion) {
                case UNKNOWN:
                case _1619_8_4_0_:
                case _1638_0_5_0_:
                    ok = true;
                    break;
                case _1707_10_5_0_:
                case _1748_13_6_0_:
                    ok = payload[1] == 1; /* Directionality byte value for responses */
                    break;
                default:
                    ok = false;
                    Log.e("Code error: swVersion.", Arrays.toString(payload));
                    break;
            }
            if (ok) {
                HashMap<Message.Id, String> idStringHashMap = Message.response.get(Message.hwVersion).get(Message.swVersion);
                if (idStringHashMap != null) {
                    responseString = idStringHashMap.get(Message.Id.GetEnum(payload[0]));
                }
                if (responseString != null) {
                    try {
                        Class<?> c = Class.forName(responseString);
                        response = (Response) c.newInstance();
                        response.SetData(payload);
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ArrayIndexOutOfBoundsException e) {
                        Log.d("Error parsing message", Arrays.toString(payload));
                        response = null;
                    }
                } else {
                    Log.d("Unknown message", Arrays.toString(payload));
                }
            } else {
                Log.d("msg tag value != 1", Arrays.toString(payload));
            }
        }
        return response;
    }

    protected void SetData(byte[] data) throws ArrayIndexOutOfBoundsException {
        if ((LENGTH > 0)) {
            if ((data.length != LENGTH)) {
                throw new ArrayIndexOutOfBoundsException(String.format(Locale.getDefault(),"Response decoding error for Id %s, received length %d differs from expected length %d", Message.Id.GetEnum(data[0]).toString(), data.length, LENGTH));
            }
        } else {
            if ((data.length < -LENGTH)) {
                throw new ArrayIndexOutOfBoundsException(String.format(Locale.getDefault(),"Response decoding error for Id %s, received length %d is less than expected lengths %d and up", Message.Id.GetEnum(data[0]).toString(), data.length, LENGTH));
            }
        }
        this.data = data;
    }

    @Override
    public String toString() {
        if (this.data == null) {
            return "R." + getId().toString() + "." + Integer.toString(getTag());
        } else {
            return "R." + getId().toString() + "." + Integer.toString(getTag()) + " - " + Arrays.toString(data);
        }
    }

    public Message.Id getId() {
        Message.Id id = Message.Id.NONE;
        if ((data != null) && (data.length > 0)) {
            id = Message.Id.GetEnum(data[0]);
        }
        return id;
    }

    public int getTag() {
        int tag = 0;
        if ((data != null) && (data.length > 1)) {
            tag = (int) data[1] & 0x000000FF;
        }
        return tag;
    }

    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //
    // // // // // // // // // // // // // // // // // // // // // // // // // // // // // //

    public int getErrorCode() {
        return Util.bytesToInt(new byte[]{data[2], data[3], data[4], data[5]});
    }

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
