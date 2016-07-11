/*
 * Copyright (C) 2016 CypherOS
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

package com.android.settings.dashboard;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.util.Log;
import android.widget.Switch;

import com.android.internal.logging.MetricsLogger;
import com.android.settings.dashboard.DashboardContainerView;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.notification.SettingPref;
import com.android.settings.widget.SwitchBar;

import cyanogenmod.providers.CMSettings;

public class DashOptionSettings extends SettingsPreferenceFragment
    implements Preference.OnPreferenceChangeListener {
		
    private static final String TAG = "DashOptionSettings";
	
	private static final String DASHBOARD_COLUMNS = "dashboard_columns";
	
	private ListPreference mDashboardColumns;
	
	@Override
    protected int getMetricsCategory() {
        return MetricsLogger.DISPLAY;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addPreferencesFromResource(R.xml.dash_option_settings);
		
		ContentResolver resolver = getActivity().getContentResolver();
        Resources res = getResources();
		
		mDashboardColumns = (ListPreference) findPreference(DASHBOARD_COLUMNS);
        mDashboardColumns.setValue(String.valueOf(Settings.System.getInt(
               getContentResolver(), Settings.System.DASHBOARD_COLUMNS, DashboardContainerView.mDashboardValue)));
        mDashboardColumns.setSummary(mDashboardColumns.getEntry());
        mDashboardColumns.setOnPreferenceChangeListener(this);
    }
	
	@Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {

        ContentResolver resolver = getActivity().getContentResolver();
        Resources res = getResources();
        
        if (preference == mDashboardColumns) {
            Settings.System.putInt(getContentResolver(), Settings.System.DASHBOARD_COLUMNS,
                    Integer.valueOf((String) objValue));
            mDashboardColumns.setValue(String.valueOf(objValue));
            mDashboardColumns.setSummary(mDashboardColumns.getEntry());
            return true;
        }
        return false;
    }
}