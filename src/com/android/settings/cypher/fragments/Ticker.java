/*
 * Copyright (C) 2015 DarkKat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cypher.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsLogger;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class Ticker extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String PREF_SHOW_TICKER =
            "notification_show_ticker";
    private static final String PREF_TICKER_TEXT_COLOR =
            "notification_ticker_text_color";

    private static final int WHITE           = 0xffffffff;
    private static final int BLACK           = 0xff000000;
    private static final int HOLO_BLUE_LIGHT = 0xff33b5e5;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET  = 0;

    private SwitchPreference mShowTicker;
    private ColorPickerPreference mTickerTextColor;

    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState)

        addPreferencesFromResource(R.xml.ticker_notifications);
        mResolver = getActivity().getContentResolver();

        boolean showTicker = Settings.System.getInt(mResolver,
               Settings.System.STATUS_BAR_NOTIFICATION_SHOW_TICKER, 0) == 1;

        int intColor;
        String hexColor;

        mShowTicker = (SwitchPreference) findPreference(PREF_SHOW_TICKER);
        mShowTicker.setChecked(showTicker);
        mShowTicker.setOnPreferenceChangeListener(this);

        if (showTicker) {
            mTickerTextColor =
                    (ColorPickerPreference) findPreference(PREF_TICKER_TEXT_COLOR);
            intColor = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_NOTIFICATION_TICKER_TEXT_COLOR, WHITE);
            mTickerTextColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mTickerTextColor.setSummary(hexColor);
            mTickerTextColor.setDefaultColors(WHITE, HOLO_BLUE_LIGHT);
            mTickerTextColor.setOnPreferenceChangeListener(this);
        } else {
            removePreference(PREF_TICKER_TEXT_COLOR);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(com.android.internal.R.drawable.ic_menu_refresh)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                showDialog(DIALOG_RESET_CONFIRM);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String hex;
        int intHex;
        if (preference == mShowTicker) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_NOTIFICATION_SHOW_TICKER,
                    value ? 1 : 0);
            refreshSettings();
            return true;
        } else if (preference == mTickerTextColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_NOTIFICATION_TICKER_TEXT_COLOR,
                    intHex);
            preference.setSummary(hex);
            return true;
        }
        return false;
    }


    @Override
    public Dialog onCreateDialog(int dialogId) {
        Dialog dialog = null;
        switch (dialogId) {
            case DIALOG_RESET_CONFIRM:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setTitle(R.string.ticker_reset_title);
                alertDialog.setMessage(R.string.ticker_reset_message);
                alertDialog.setPositiveButton(R.string.ticker_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        resetTicker();
                    }
				});
				alertDialog.setNegativeButton(R.string.ticker_no, null);
                dialog = alertDialog.create();
                break;
		}
		return dialog;
	}
	
	private void resetTicker() {
        Settings.Secure.putInt(mContext.getContentResolver(),
                "status_bar_notification_show_ticker", 0);
        Settings.Secure.putInt(mContext.getContentResolver(),
                "status_bar_notification_ticker_text_color", WHITE);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.APPLICATION;
    }
}