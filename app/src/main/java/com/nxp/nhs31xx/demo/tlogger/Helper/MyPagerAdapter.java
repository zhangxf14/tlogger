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


package com.nxp.nhs31xx.demo.tlogger.Helper;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.nxp.nhs31xx.demo.tlogger.Fragment.AboutFragment;
import com.nxp.nhs31xx.demo.tlogger.Fragment.ConfigFragment;
import com.nxp.nhs31xx.demo.tlogger.Fragment.DataFragment;
import com.nxp.nhs31xx.demo.tlogger.Fragment.StatusFragment;
import com.nxp.nhs31xx.demo.tlogger.R;

public class MyPagerAdapter extends FragmentPagerAdapter {

    private final String[] tabTitles;

    public MyPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        tabTitles = new String[]{context.getResources().getString(R.string.action_about),
                context.getResources().getString(R.string.action_status),
                context.getResources().getString(R.string.action_data),
                context.getResources().getString(R.string.action_config)};
    }

    @Override
    public Fragment getItem(int index) {
        switch (Position.ToEnum(index)) {
            case ABOUT:
                return new AboutFragment();
            case CONFIG:
                return new ConfigFragment();
            case STATUS:
                return new StatusFragment();
            case DATA:
                return new DataFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    public enum Position {
        ABOUT(0),
        STATUS(1),
        DATA(2),
        CONFIG(3);

        private final int position;

        Position(int position) {
            this.position = position;
        }

        public static Position ToEnum(int position) {
            Position[] ids = Position.values();
            for (Position x : ids) {
                if (x.Compare(position)) {
                    return x;
                }
            }
            return Position.ABOUT;
        }

        public static int ToInt(Position position) {
            return position.position;
        }

        public boolean Compare(int position) {
            return this.position == position;
        }
    }
}
