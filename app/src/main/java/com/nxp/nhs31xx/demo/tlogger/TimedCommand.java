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


package com.nxp.nhs31xx.demo.tlogger;

import android.os.Handler;
import android.os.Message;

import java.util.TimerTask;

import static com.nxp.nhs31xx.demo.tlogger.Message.Message.Id;

class TimedCommand extends TimerTask {
    private final Handler guiHandler;
    private final Purpose purpose;

    TimedCommand(Handler handler, Purpose purpose) {
        super();
        guiHandler = handler;
        this.purpose = purpose;
    }

    @Override
    public void run() {
        Message response;
        switch (purpose) {
            case STATUS:
            default:
                response = Message.obtain();
                response.obj = Boolean.TRUE;
                guiHandler.sendMessage(response);
                break;

            case STORED:
                response = Message.obtain();
                response.obj = Id.GETMEASUREMENTS;
                guiHandler.sendMessage(response);
                break;

            case LIVE:
                response = Message.obtain();
                response.obj = Id.MEASURETEMPERATURE;
                guiHandler.sendMessage(response);
                response = Message.obtain();
                response.obj = Id.GETRESPONSE;
                guiHandler.sendMessage(response);
                break;
        }
    }

    enum Purpose {
        STATUS(0),
        STORED(1),
        LIVE(2);

        private final int purpose;

        Purpose(int purpose) {
            this.purpose = purpose;
        }

        public static Purpose GetEnum(int id) {
            Purpose[] ids = Purpose.values();
            for (Purpose x : ids) {
                if (x.Compare(id)) {
                    return x;
                }
            }
            return Purpose.STATUS;
        }

        public static int GetInt(Purpose purpose) {
            return purpose.purpose;
        }

        public boolean Compare(int purpose) {
            return this.purpose == purpose;
        }
    }
}
