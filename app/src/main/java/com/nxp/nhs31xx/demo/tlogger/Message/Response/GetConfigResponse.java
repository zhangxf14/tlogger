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

public class GetConfigResponse extends Response {
    public static final Parcelable.Creator<GetConfigResponse> CREATOR = new Parcelable.Creator<GetConfigResponse>() {

        public GetConfigResponse createFromParcel(Parcel in) {
            return new GetConfigResponse(in);
        }

        public GetConfigResponse[] newArray(int size) {
            return new GetConfigResponse[size];
        }
    };

    protected static final int MSG_EVENT_PRISTINE = 1; /** State change: the IC no longer has a configuration and contains no data. */
    protected static final int MSG_EVENT_CONFIGURED = 1 << 1; /** State change: the IC is configured, but requires a #APP_MSG_ID_START command to start. */
    protected static final int MSG_EVENT_STARTING = 1 << 2; /** State change: the IC is configured and will make a first measurement after a delay. */
    protected static final int MSG_EVENT_LOGGING = 1 << 3; /** State change: the IC is configured and is logging. At least one sample is available. */
    protected static final int MSG_EVENT_STOPPED = 1 << 4; /** State change: the IC is configured and has been logging. Now it has stopped logging. */
    protected static final int MSG_EVENT_TEMPERATURE_TOO_HIGH = 1 << 5; /**< Failure: at least one temperature was strictly higher than the valid maximum value. */
    protected static final int MSG_EVENT_TEMPERATURE_TOO_LOW  = 1 << 6; /**< Failure: At least one temperature was strictly lower than the valid minimum value. */
    protected static final int MSG_EVENT_BOD = 1 << 7; /**< Failure: A brown-out is about to occur or has occurred. Battery is (nearly) depleted. */
    protected static final int MSG_EVENT_FULL = 1 << 8; /**< Failure: Logging has stopped because no more free space is available to store samples. */
    protected static final int MSG_EVENT_EXPIRED = 1 << 9; /**< Achievement: Logging has stopped because the time spent logging has exceeded the no-zero value set in APP_MSG_CMD_SETCONFIG_T.runningTime. */

    protected int currentTime; // 'current' as in: when the response class was created
    protected int configTime;
    protected int interval;
    protected int startDelay;
    protected int runningTime;
    protected int validMinimum;
    protected int validMaximum;
    protected int attainedMinimum;
    protected int attainedMaximum;
    protected int count;
    protected int status;
    protected int startTime;
    protected int islandTime;

    public GetConfigResponse() {
        super();
        LENGTH = 42;
    }

    protected GetConfigResponse(Parcel in) {
        super(in);
    }

    @Override
    protected void SetData(byte[] data) throws ArrayIndexOutOfBoundsException {
        super.SetData(data);
        currentTime = (int) (System.currentTimeMillis() / 1000);

        if (this instanceof mra2_1625_GetConfigResponse) {
            return;
        }

        /* -------------------------------------------------------------------------------- */
        configTime = Util.bytesToInt(new byte[]{data[6], data[7], data[8], data[9]});
        interval = Util.bytesToInt(new byte[]{data[10], data[11]});
        startDelay = Util.bytesToInt(new byte[]{data[12], data[13], data[14], data[15]});
        runningTime = Util.bytesToInt(new byte[]{data[16], data[17], data[18], data[19]});
        int t = Util.bytesToInt(new byte[]{data[20], data[21]});
        if (t >= 0x00008000) {
            t -= 0x10000;
        }
        validMinimum = t;
        t = Util.bytesToInt(new byte[]{data[22], data[23]});
        if (t >= 0x00008000) {
            t -= 0x10000;
        }
        validMaximum = t;
        /* -------------------------------------------------------------------------------- */
        t = Util.bytesToInt(new byte[]{data[24], data[25]});
        if (t >= 0x00008000) {
            t -= 0x10000;
        }
        attainedMinimum = t;
        t = Util.bytesToInt(new byte[]{data[26], data[27]});
        if (t >= 0x00008000) {
            t -= 0x10000;
        }
        attainedMaximum = t;
        count = Util.bytesToInt(new byte[]{data[28], data[29]});
        status = Util.bytesToInt(new byte[]{data[30], data[31], data[32], data[33]});
        startTime = Util.bytesToInt(new byte[]{data[34], data[35], data[36], data[37]});
        islandTime = Util.bytesToInt(new byte[]{data[38], data[39], data[40], data[41]});
    }

    public int getConfigTime() {
        return configTime;
    }

    public int getInterval() {
        return interval;
    }

    public int getStartDelay() {
        return startDelay;
    }

    public int getRunningTime() {
        return runningTime;
    }

    public int getValidMinimum() {
        return validMinimum;
    }

    public int getValidMaximum() {
        return validMaximum;
    }

    /* -------------------------------------------------------------------------------- */

    public int getAttainedMinimum() {
        return attainedMinimum;
    }

    public int getAttainedMaximum() {
        return attainedMaximum;
    }

    public int getCount() {
        return count;
    }

    // status is not returned in a getter field.

    public int getStartTime() {
        return startTime;
    }

    // islandTime is not returned in a getter field.

    /* -------------------------------------------------------------------------------- */

    public boolean memoryIsFull() {
        return (status & MSG_EVENT_FULL) != 0;
    }

    public boolean countIsLimited() {
        return runningTime > 0;
    }

    public boolean countLimitIsReached() {
        return (status & MSG_EVENT_EXPIRED) != 0;
    }

    public boolean isPristine() {
        return ((status & MSG_EVENT_PRISTINE) != 0) && ((status & (MSG_EVENT_CONFIGURED | MSG_EVENT_STARTING | MSG_EVENT_LOGGING | MSG_EVENT_STOPPED)) == 0);
    }

    public boolean isConfigured() {
        return ((status & MSG_EVENT_CONFIGURED) != 0) && ((status & (MSG_EVENT_STARTING | MSG_EVENT_LOGGING | MSG_EVENT_STOPPED)) == 0);
    }

    public boolean isStarting() {
        return ((status & MSG_EVENT_STARTING) != 0) && ((status & (MSG_EVENT_LOGGING | MSG_EVENT_STOPPED)) == 0);
    }

    public boolean isLogging() {
        return ((status & MSG_EVENT_LOGGING) != 0) && ((status & (MSG_EVENT_STOPPED)) == 0);
    }

    public boolean isStopped() {
        return (status & MSG_EVENT_STOPPED) != 0;
    }

    public boolean isValid() {
        return (status & (MSG_EVENT_STOPPED | MSG_EVENT_TEMPERATURE_TOO_HIGH | MSG_EVENT_TEMPERATURE_TOO_LOW | MSG_EVENT_BOD | MSG_EVENT_FULL | MSG_EVENT_EXPIRED)) == 0;
    }

    /* -------------------------------------------------------------------------------- */

    /**
     * Returns the factor with which to multiply the uncorrected timestamps as received from the NHS31xx:
     * With:
     * - t: uncorrected timestamp
     * - t': corrected timestamp
     * - s: start time
     * The correction can be retrieved using this formula:
     *   t = s + (t' - s) * getCorrectionFactor()
     * @return a strict positive number
     */
    public double getCorrectionFactor() {
        double correctionFactor = (double)(currentTime - configTime) / (islandTime - configTime);
        if (Math.abs(1 - correctionFactor) > 0.10) {
            /* Something went wrong. This deviation is simply not possible.
             * Likely a reset occurred, or memory got full and measurements stopped.
             */
            correctionFactor = 1;
        }
        return correctionFactor;
    }
}
