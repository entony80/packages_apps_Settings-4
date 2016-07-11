/*
 * Copyright (C) 2016 CypherOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cypher;

import android.content.Intent;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.provider.Settings;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.util.aicp.AicpUtils;
import com.android.settings.Utils;

import com.android.settings.cypher.SeekBarPreference;

import java.util.ArrayList;
import java.util.List;
import java.util.List;
import java.util.Map;

import cyanogenmod.providers.CMSettings;
import org.cyanogenmod.internal.logging.CMMetricsLogger;

public class QuickSettings extends SettingsPreferenceFragment
    implements Preference.OnPreferenceChangeListener {

    private static final String TAG = QuickSettings.class.getSimpleName();
	
	private static final String PREF_QS_TRANSPARENT_HEADER = "qs_transparent_header";
	private static final String PREF_QS_TRANSPARENT_SHADE = "qs_transparent_shade";
	
	private ListPreference mNumColumns;
	private ListPreference mNumRows;
	
	private SeekBarPreference mQSHeaderAlpha;
	private SeekBarPreference mQSShadeAlpha;
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
		createCustomView();
	}
	
	private PreferenceScreen createCustomView() {

        addPreferencesFromResource(R.xml.qs_settings);
        
		PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
		
		// QS shade alpha
        mQSShadeAlpha =
                (SeekBarPreference) prefSet.findPreference(PREF_QS_TRANSPARENT_SHADE);
        int qSShadeAlpha = Settings.System.getInt(resolver,
                Settings.System.QS_TRANSPARENT_SHADE, 255);
        mQSShadeAlpha.setValue(qSShadeAlpha / 1);
        mQSShadeAlpha.setOnPreferenceChangeListener(this);
		
		// QS header alpha
        mQSHeaderAlpha =
                (SeekBarPreference) prefSet.findPreference(PREF_QS_TRANSPARENT_HEADER);
        int qSHeaderAlpha = Settings.System.getInt(resolver,
                Settings.System.QS_TRANSPARENT_HEADER, 255);
        mQSHeaderAlpha.setValue(qSHeaderAlpha / 1);
        mQSHeaderAlpha.setOnPreferenceChangeListener(this);
		
		// Number of QS Columns 3,4,5
        mNumColumns = (ListPreference) findPreference("sysui_qs_num_columns");
        int numColumns = Settings.System.getIntForUser(resolver,
                Settings.System.QS_NUM_TILE_COLUMNS, getDefaultNumColumns(),
                UserHandle.USER_CURRENT);
        mNumColumns.setValue(String.valueOf(numColumns));
        updateNumColumnsSummary(numColumns);
        mNumColumns.setOnPreferenceChangeListener(this);
		
		// Number of QS Rows 3,4
        mNumRows = (ListPreference) findPreference("sysui_qs_num_rows");
        int numRows = Settings.System.getIntForUser(resolver,
                Settings.System.QS_NUM_TILE_ROWS, getDefaultNumRows(),
                UserHandle.USER_CURRENT);
        mNumRows.setValue(String.valueOf(numRows));
        updateNumRowsSummary(numRows);
        mNumRows.setOnPreferenceChangeListener(this);

        return prefSet;
    }

    @Override
    protected int getMetricsCategory() {
        // todo add a constant in MetricsLogger.java
        return CMMetricsLogger.MAIN_SETTINGS;
    }
	
	@Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        ContentResolver resolver = getActivity().getContentResolver();
		Resources res = getResources();
        if (preference == mQSShadeAlpha) {
            int alpha = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.QS_TRANSPARENT_SHADE, alpha * 1);
            return true;
		} else if (preference == mQSHeaderAlpha) {
            int alpha = (Integer) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.QS_TRANSPARENT_HEADER, alpha * 1);
            return true;
		} else if (preference == mNumColumns) {
            int numColumns = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(resolver, Settings.System.QS_NUM_TILE_COLUMNS,
                    numColumns, UserHandle.USER_CURRENT);
            updateNumColumnsSummary(numColumns);
            return true;
		} else if (preference == mNumRows) {
            int numRows = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(resolver, Settings.System.QS_NUM_TILE_ROWS,
                    numRows, UserHandle.USER_CURRENT);
            updateNumRowsSummary(numRows);
            return true;
        }  
        return false;
    }
	
	private void updateNumRowsSummary(int numRows) {
        String prefix = (String) mNumRows.getEntries()[mNumRows.findIndexOfValue(String
                .valueOf(numRows))];
        mNumRows.setSummary(getResources().getString(R.string.qs_num_rows_showing, prefix));
    }
	
	private void updateNumColumnsSummary(int numColumns) {
        String prefix = (String) mNumColumns.getEntries()[mNumColumns.findIndexOfValue(String
                .valueOf(numColumns))];
        mNumColumns.setSummary(getResources().getString(R.string.qs_num_columns_showing, prefix));
    }

	private int getDefaultNumRows() {
        try {
            Resources res = getActivity().getPackageManager()
                    .getResourcesForApplication("com.android.systemui");
            int val = res.getInteger(res.getIdentifier("quick_settings_num_rows", "integer",
                    "com.android.systemui")); // better not be larger than 4, that's as high as the
                                              // list goes atm
            return Math.max(1, val);
        } catch (Exception e) {
            return 3;
        }
    }
	
    private int getDefaultNumColums() {
        try {
            Resources res = getActivity().getPackageManager()
                    .getResourcesForApplication("com.android.systemui");
            int val = res.getInteger(res.getIdentifier("quick_settings_num_columns", "integer",
                    "com.android.systemui")); // better not be larger than 5, that's as high as the
                                              // list goes atm
            return Math.max(1, val);
        } catch (Exception e) {
            return 3;
        }
    }
}