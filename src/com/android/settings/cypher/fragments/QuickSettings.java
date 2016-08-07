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

package com.android.settings.cypher.fragments;

import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.content.ContentResolver;
import android.preference.ListPreference;
import android.provider.Settings;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;

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
import org.cyanogenmod.internal.util.CmLockPatternUtils;
import org.cyanogenmod.internal.logging.CMMetricsLogger;

public class QuickSettings extends SettingsPreferenceFragment
    implements Preference.OnPreferenceChangeListener {

    private static final String TAG = QuickSettings.class.getSimpleName();

	private static final String PREF_BLOCK_ON_SECURE_KEYGUARD = "block_on_secure_keyguard";
    private static final String PREF_CUSTOM_HEADER_DEFAULT = "status_bar_custom_header";
	private static final String CUSTOM_HEADER_IMAGE_SHADOW = "status_bar_custom_header_shadow";
	
	private ListPreference mNumRows;
	
	private SwitchPreference mBlockOnSecureKeyguard;
    private ListPreference mCustomHeaderDefault;
	private SeekBarPreference mHeaderShadow;

    private static final int MY_USER_ID = UserHandle.myUserId();
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
		createCustomView();
	}
	
	private PreferenceScreen createCustomView() {

        addPreferencesFromResource(R.xml.qs_settings);
        
		PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
		final CmLockPatternUtils lockPatternUtils = new CmLockPatternUtils(getActivity());
		Context context = getActivity();

        Resources res = getResources();
        PackageManager pm = getPackageManager();
        Resources systemUiResources;
        try {
            systemUiResources = pm.getResourcesForApplication("com.android.systemui");
        } catch (Exception e) {
            Log.e(TAG, "can't access systemui resources",e);
            return null;
        }
		
		// Number of QS Rows 3,4
        mNumRows = (ListPreference) findPreference("sysui_qs_num_rows");
        int numRows = Settings.System.getIntForUser(resolver,
                Settings.System.QS_NUM_TILE_ROWS, getDefaultNumRows(),
                UserHandle.USER_CURRENT);
        mNumRows.setValue(String.valueOf(numRows));
        updateNumRowsSummary(numRows);
        mNumRows.setOnPreferenceChangeListener(this);
		
        // Status bar custom header default
        mCustomHeaderDefault = (ListPreference) findPreference(PREF_CUSTOM_HEADER_DEFAULT);
        mCustomHeaderDefault.setOnPreferenceChangeListener(this);
		int customHeaderDefault = Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUS_BAR_CUSTOM_HEADER, 0);
        mCustomHeaderDefault.setValue(String.valueOf(customHeaderDefault));
        mCustomHeaderDefault.setSummary(mCustomHeaderDefault.getEntry());
		
		// Custom shadow on header images
        mHeaderShadow = (SeekBarPreference) findPreference(CUSTOM_HEADER_IMAGE_SHADOW);
        final int headerShadow = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, 0);
        mHeaderShadow.setValue((int)((headerShadow / 255) * 100));
        mHeaderShadow.setOnPreferenceChangeListener(this);
		
		// Block QS on secure LockScreen
        mBlockOnSecureKeyguard = (SwitchPreference) findPreference(PREF_BLOCK_ON_SECURE_KEYGUARD);
        if (lockPatternUtils.isSecure(MY_USER_ID)) {
            mBlockOnSecureKeyguard.setChecked(Settings.Secure.getIntForUser(resolver,
                    Settings.Secure.STATUS_BAR_LOCKED_ON_SECURE_KEYGUARD, 1, UserHandle.USER_CURRENT) == 1);
            mBlockOnSecureKeyguard.setOnPreferenceChangeListener(this);
        } else if (mBlockOnSecureKeyguard != null) {
            prefSet.removePreference(mBlockOnSecureKeyguard);
        }

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
        if (preference == mNumRows) {
            int numRows = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(resolver, Settings.System.QS_NUM_TILE_ROWS,
                    numRows, UserHandle.USER_CURRENT);
            updateNumRowsSummary(numRows);
            return true;
		} else if (preference == mBlockOnSecureKeyguard) {
            Settings.Secure.putInt(resolver,
                    Settings.Secure.STATUS_BAR_LOCKED_ON_SECURE_KEYGUARD,
                    (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mCustomHeaderDefault) {
            int customHeaderDefault = Integer.valueOf((String) newValue);
            int index = mCustomHeaderDefault.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(), 
                Settings.System.STATUS_BAR_CUSTOM_HEADER, customHeaderDefault);
            mCustomHeaderDefault.setSummary(mCustomHeaderDefault.getEntries()[index]);
            createCustomView();
            return true;
		} else if (preference == mHeaderShadow) {
            Integer headerShadow = (Integer) newValue;
            int realHeaderValue = (int) (((double) headerShadow / 100) * 255);
            Settings.System.putInt(resolver,
                Settings.System.STATUS_BAR_CUSTOM_HEADER_SHADOW, realHeaderValue);
           return true;
        }  
        return false;
    }
	
	private void updateNumRowsSummary(int numRows) {
        String prefix = (String) mNumRows.getEntries()[mNumRows.findIndexOfValue(String
                .valueOf(numRows))];
        mNumRows.setSummary(getResources().getString(R.string.qs_num_rows_showing, prefix));
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
}