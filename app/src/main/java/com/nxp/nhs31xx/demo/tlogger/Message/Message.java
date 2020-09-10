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


package com.nxp.nhs31xx.demo.tlogger.Message;

import java.util.HashMap;

public class Message {

    private final static HashMap<Id, String> defaultCommand = new HashMap<Id, String>() {{
        put(Id.GETVERSION, "com.nxp.nhs31xx.demo.tlogger.Message.Command.GetVersionCommand");
        put(Id.GETMEASUREMENTS, "com.nxp.nhs31xx.demo.tlogger.Message.Command.GetMeasurementsCommand");
        put(Id.GETCONFIG, "com.nxp.nhs31xx.demo.tlogger.Message.Command.GetConfigCommand");
        put(Id.SETCONFIG, "com.nxp.nhs31xx.demo.tlogger.Message.Command.SetConfigCommand");
        put(Id.MEASURETEMPERATURE, "com.nxp.nhs31xx.demo.tlogger.Message.Command.MeasureTemperatureCommand");
        put(Id.START, "com.nxp.nhs31xx.demo.tlogger.Message.Command.StartCommand");
    }};

    private final static HashMap<Id, String> defaultResponse = new HashMap<Id, String>() {{
        put(Id.GETVERSION, "com.nxp.nhs31xx.demo.tlogger.Message.Response.GetVersionResponse");
        put(Id.GETMEASUREMENTS, "com.nxp.nhs31xx.demo.tlogger.Message.Response.GetMeasurementsResponse");
        put(Id.GETCONFIG, "com.nxp.nhs31xx.demo.tlogger.Message.Response.GetConfigResponse");
        put(Id.SETCONFIG, "com.nxp.nhs31xx.demo.tlogger.Message.Response.SetConfigResponse");
        put(Id.MEASURETEMPERATURE, "com.nxp.nhs31xx.demo.tlogger.Message.Response.MeasureTemperatureResponse");
    }};

    // ------------------------------------------------------------------------

    private final static HashMap<Id, String> _mra2_1552_command = new HashMap<Id, String>() {{
        put(Id.GETVERSION, "com.nxp.nhs31xx.demo.tlogger.Message.Command.GetVersionCommand");
        put(Id.GETMEASUREMENTS, "com.nxp.nhs31xx.demo.tlogger.Message.Command.GetMeasurementsCommand");
        put(Id.GETCONFIG, "com.nxp.nhs31xx.demo.tlogger.Message.Command.GetConfigCommand");
        put(Id.SETCONFIG, "com.nxp.nhs31xx.demo.tlogger.Message.Command.mra2_1625_SetConfigCommand");
        put(Id.MEASURETEMPERATURE, "com.nxp.nhs31xx.demo.tlogger.Message.Command.MeasureTemperatureCommand");
    }};
    private final static HashMap<SwVersion, HashMap<Id, String>> _mra2_command = new HashMap<SwVersion, HashMap<Id, String>>() {{
        put(SwVersion._1619_8_4_0_, null);
        put(SwVersion._1638_0_5_0_, _mra2_1552_command);
        put(SwVersion._1707_10_5_0_, _mra2_1552_command);
        put(SwVersion._1748_13_6_0_, defaultCommand);
    }};
    public final static HashMap<HwVersion, HashMap<SwVersion, HashMap<Id, String>>> command = new HashMap<HwVersion, HashMap<SwVersion, HashMap<Id, String>>>() {{
        put(HwVersion.MRA1, null);
        put(HwVersion.MRA2, _mra2_command);
    }};

    // ------------------------------------------------------------------------

    private final static HashMap<Id, String> _unknown_response = new HashMap<Id, String>() {{
        put(Id.GETVERSION, "com.nxp.nhs31xx.demo.tlogger.Message.Response.GetVersionResponse");
    }};
    private final static HashMap<Id, String> _mra2_1552_response = new HashMap<Id, String>() {{
        put(Id.GETVERSION, "com.nxp.nhs31xx.demo.tlogger.Message.Response.GetVersionResponse");
        put(Id.GETMEASUREMENTS, "com.nxp.nhs31xx.demo.tlogger.Message.Response.GetMeasurementsResponse");
        put(Id.GETCONFIG, "com.nxp.nhs31xx.demo.tlogger.Message.Response.mra2_1552_GetConfigResponse");
        put(Id.SETCONFIG, "com.nxp.nhs31xx.demo.tlogger.Message.Response.SetConfigResponse");
        put(Id.MEASURETEMPERATURE, "com.nxp.nhs31xx.demo.tlogger.Message.Response.MeasureTemperatureResponse");
    }};
    private final static HashMap<Id, String> _mra2_1625_response = new HashMap<Id, String>() {{
        put(Id.GETVERSION, "com.nxp.nhs31xx.demo.tlogger.Message.Response.GetVersionResponse");
        put(Id.GETMEASUREMENTS, "com.nxp.nhs31xx.demo.tlogger.Message.Response.GetMeasurementsResponse");
        put(Id.GETCONFIG, "com.nxp.nhs31xx.demo.tlogger.Message.Response.mra2_1625_GetConfigResponse");
        put(Id.SETCONFIG, "com.nxp.nhs31xx.demo.tlogger.Message.Response.SetConfigResponse");
        put(Id.MEASURETEMPERATURE, "com.nxp.nhs31xx.demo.tlogger.Message.Response.MeasureTemperatureResponse");
    }};
    private final static HashMap<SwVersion, HashMap<Id, String>> _mra2_response = new HashMap<SwVersion, HashMap<Id, String>>() {{
        put(SwVersion.UNKNOWN, _unknown_response);
        put(SwVersion._1619_8_4_0_, _unknown_response);
        put(SwVersion._1638_0_5_0_, _mra2_1552_response);
        put(SwVersion._1707_10_5_0_, _mra2_1625_response);
        put(SwVersion._1748_13_6_0_, defaultResponse);
    }};
    public final static HashMap<HwVersion, HashMap<SwVersion, HashMap<Id, String>>> response = new HashMap<HwVersion, HashMap<SwVersion, HashMap<Id, String>>>() {{
        put(HwVersion.MRA1, null);
        put(HwVersion.MRA2, _mra2_response);
    }};

    // ------------------------------------------------------------------------

    private final static HashMap<SwVersion, Integer> _mra2_minimalMsgApiMajorVersion = new HashMap<SwVersion, Integer>() {{
        put(SwVersion._1619_8_4_0_, 4);
        put(SwVersion._1638_0_5_0_, 5);
        put(SwVersion._1707_10_5_0_, 5);
        put(SwVersion._1748_13_6_0_, 6);
    }};
    public final static HashMap<HwVersion, HashMap<SwVersion, Integer>> minimalMsgApiMajorVersion = new HashMap<HwVersion, HashMap<SwVersion, Integer>>() {{
        put(HwVersion.MRA2, _mra2_minimalMsgApiMajorVersion);
    }}; /* Only MRA2 is supported now. */

    public static HwVersion hwVersion = HwVersion.UNKNOWN;
    public static SwVersion swVersion = SwVersion.UNKNOWN;

    public enum HwVersion {
        UNKNOWN(0),
        MRA1(4),
        MRA2(5);

        private final int version;

        HwVersion(int version) {
            this.version = version;
        }

        public static HwVersion GetEnum(int version) {
            for (HwVersion x : HwVersion.values()) {
                if (x.Compare(version)) {
                    return x;
                }
            }
            return HwVersion.MRA1;
        }

        public static int GetVersion(HwVersion hwVersion) {
            return hwVersion.version;
        }

        public boolean Compare(int version) {
            return this.version == version;
        }
    }

    public enum SwVersion {
        UNKNOWN(0),
        _1619_8_4_0_(1), // SDK 7.1.1 - SW 1619.8, api 4.0 - from early April 2015 onwards
        _1638_0_5_0_(2), // SDK 8.1 - SW 1638.9, api 5.0 - from end of 2015 onwards
        _1707_10_5_0_(3), // SDK 9.1 - SW 1707.10, api 5.0 - from late June 2016 onwards
        _1748_13_6_0_(4); // SDK 11 - SW 1748.13, api 6.0 - from late August 2017 onwards

        private static final int[][] versionNumbers = {
                /* minimal or exact values for swMajor, swMinor, apiMajor, apiMinor */
                /* UNKNOWN */ {0, 0, 0, 0},
                /* _1619_8_4_0_ */ {1619, 8, 4, 0},
                /* _1638_0_5_0_ */ {1638, 0, 5, 0},
                /* _1707_10_5_0_ */ {1707, 0, 5, 0},
                /* _1748_13_6_0_ */ {1734, 0, 6, 0}, /* Accept earlier builds too: work started around week 34 */
        };
        private final int version;

        SwVersion(int version) {
            this.version = version;
        }

        public static SwVersion GetEnum(int swMajor, int swMinor, int apiMajor, int apiMinor) {
            // The version reported is compatible with the highest enum value less than or equal to that number.
            SwVersion hit = SwVersion.UNKNOWN;
            for (SwVersion x : SwVersion.values()) {
                int[] v = versionNumbers[GetVersion(x)];
                if ((v[0] <= swMajor) && (v[1] <= swMinor) && (v[2] == apiMajor) && (v[3] <= apiMinor)) {
                    hit = x;
                }
            }
            return hit;
        }

        public static int GetVersion(SwVersion swVersion) {
            return swVersion.version;
        }
    }

    public enum Id {
        NONE(0x00),
        GETRESPONSE(0x01),
        GETVERSION(0x02),
        GETUID(0x09),
        GETNFCUID(0x0a),
        GETMEASUREMENTS(0x46),
        GETCONFIG(0x48),
        SETCONFIG(0x49),
        MEASURETEMPERATURE(0x50),
        START(0x51);

        private final int id;

        Id(int id) {
            this.id = id;
        }

        public static Id GetEnum(int id) {
            Id[] ids = Id.values();
            for (Id x : ids) {
                if (x.Compare(id)) {
                    return x;
                }
            }
            return Id.NONE;
        }

        public static int GetInt(Id id) {
            return id.id;
        }

        public boolean Compare(int id) {
            return this.id == id;
        }
    }
}
