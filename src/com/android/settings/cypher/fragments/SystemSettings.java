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
import com.android.settings.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.List;
import java.util.Map;

import org.cyanogenmod.internal.logging.CMMetricsLogger;

public class SystemSettings extends SettingsPreferenceFragment {

    private static final String TAG = SystemSettings.class.getSimpleName();
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
		
		addPreferencesFromResource(R.xml.system_settings);
        
		PreferenceScreen prefSet = getPreferenceScreen();
	}

    @Override
    protected int getMetricsCategory() {
        // todo add a constant in MetricsLogger.java
        return CMMetricsLogger.MAIN_SETTINGS;
    }
}