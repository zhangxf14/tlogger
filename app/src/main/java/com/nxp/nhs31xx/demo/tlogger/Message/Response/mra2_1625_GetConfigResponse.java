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

import com.nxp.nhs31xx.demo.tlogger.Helper.Util;

public class mra2_1625_GetConfigResponse extends GetConfigResponse {
    public static final Parcelable.Creator<mra2_1625_GetConfigResponse> CREATOR = new Parcelable.Creator<mra2_1625_GetConfigResponse>() {

        public mra2_1625_GetConfigResponse createFromParcel(Parcel in) {
            return new mra2_1625_GetConfigResponse(in);
        }

        public mra2_1625_GetConfigResponse[] newArray(int size) {
            return new mra2_1625_GetConfigResponse[size];
        }
    };


    public mra2_1625_GetConfigResponse() {
        super();
        LENGTH = 25;
    }

    protected mra2_1625_GetConfigResponse(Parcel in) {
        super(in);
    }

    @Override
    protected void SetData(byte[] data) throws ArrayIndexOutOfBoundsException {
        super.SetData(data);

        int maxCount = Util.bytesToInt(new byte[]{data[12], data[13]});
        boolean valid = (data[20] != 0);

        configTime = Util.bytesToInt(new byte[]{data[6], data[7], data[8], data[9]});
        interval = Util.bytesToInt(new byte[]{data[10], data[11]});
		startDelay = 0;
		if (interval == 0) {
		    runningTime = 0;
		} else {
		    runningTime = maxCount / interval;
		}

        int t;
        t = Util.bytesToInt(new byte[]{data[14], data[15]});
        if (t >= 0x00008000) {
            t -= 0x10000;
        }
        validMinimum = t;
        t = Util.bytesToInt(new byte[]{data[16], data[17]});
        if (t >= 0x00008000) {
            t -= 0x10000;
        }
        validMaximum = t;
        attainedMinimum = 0;
		attainedMaximum = 0;

        count = Util.bytesToInt(new byte[]{data[18], data[19]});
		startTime = configTime + interval;

        /* Deducing status from the fields retrieved above, using this logic:
         *   interval 0 ? Not started yet
         *   else configured and starting
         *     count > 0 ? logging
         *     current time > config time ? Running
         *     else stopped
         *        'maxCount' == 0 ? battery has died
         *        'maxCount' == 0xFFFF ? storage full
         *        'maxCount' == 'count' ? demo mode, time expired
         *       else battery died.
         *   ! temperature too high or too low can not be discriminated !
         */
        status |= MSG_EVENT_PRISTINE;
        if (interval > 0) {
            status |= MSG_EVENT_CONFIGURED;
            status |= MSG_EVENT_STARTING;
            if (count > 0) {
                status |= MSG_EVENT_LOGGING;
                if (!valid) {
                    status |= MSG_EVENT_TEMPERATURE_TOO_HIGH; /* Either too high, either too low. Just guess. */
                }
            }
            if (currentTime < configTime) {
                status |= MSG_EVENT_LOGGING;
                status |= MSG_EVENT_STOPPED;
                if (maxCount == 0xFFFF) {
                    status |= MSG_EVENT_FULL;
                } else if (maxCount == count) {
                    status |= MSG_EVENT_EXPIRED;
                } else {
                    status |= MSG_EVENT_BOD;
                }
            }
        }

        if (LENGTH < 25) { /* This constructor is also called by mra2_1552_GetConfigResponse */
            islandTime = currentTime;
        } else {
            islandTime = Util.bytesToInt(new byte[]{data[21], data[22], data[23], data[24]});
        }
    }
}
