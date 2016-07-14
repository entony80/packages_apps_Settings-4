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

import android.app.Activity;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference;
import android.preference.PreferenceGroup;

import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.aicp.AicpUtils;

public class StatusBarIcons extends SettingsPreferenceFragment {
	
	private static final String TAG = StatusBarIcons.class.getSimpleName();
	
	private static final String SHOW_FOURG = "show_fourg";
	
	private SwitchPreference mShowFourG;
	
	private boolean mCheckPreferences;
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
		createCustomView();
	}
		
	private PreferenceScreen createCustomView() {

		mCheckPreferences = false;
        addPreferencesFromResource(R.xml.status_bar_icons);
		
		PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
        Context context = getActivity();
		
		// Show 4G
        mShowFourG = (SwitchPreference) findPreference(SHOW_FOURG);
        if (AicpUtils.isWifiOnly(getActivity())) {
            prefSet.removePreference(mShowFourG);
        } else {
           mShowFourG.setChecked((Settings.System.getInt(resolver,
           Settings.System.SHOW_FOURG, 0) == 1));
        }
		
		mCheckPreferences = true;
        return prefSet;
    }
	
	@Override
    protected int getMetricsCategory() {
        // todo add a constant in MetricsLogger.java
        return MetricsLogger.APPLICATION;
	}
	
	@Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mShowFourG) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SHOW_FOURG, checked ? 1:0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}