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

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.SearchIndexableResource;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.Utils;

import android.preference.SwitchPreference;

import static android.provider.Settings.Secure.CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED;

import java.util.ArrayList;
import java.util.List;

import org.cyanogenmod.internal.logging.CMMetricsLogger;

public class Gestures extends SettingsPreferenceFragment
    implements Preference.OnPreferenceChangeListener, Indexable {

    private static final String TAG = Gestures.class.getSimpleName();
	
	private static final String KEY_CAMERA_DOUBLE_TAP_POWER_GESTURE
            = "camera_double_tap_power_gesture";
			
	private SwitchPreference mCameraDoubleTapPowerGesture;
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.gestures_settings);
        final PreferenceScreen prefScreen = getPreferenceScreen();

	    // Double press power to launch camera.
        mCameraDoubleTapPowerGesture
                    = (SwitchPreference) findPreference(KEY_CAMERA_DOUBLE_TAP_POWER_GESTURE);
					
	    if (mCameraDoubleTapPowerGesture != null &&
                isCameraDoubleTapPowerGestureAvailable(getResources())) {
            // Update double tap power to launch camera if available.
            mCameraDoubleTapPowerGesture.setOnPreferenceChangeListener(this);
            int cameraDoubleTapPowerDisabled = Settings.Secure.getInt(
                    getContentResolver(), CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED, 0);
            mCameraDoubleTapPowerGesture.setChecked(cameraDoubleTapPowerDisabled == 0);
	    }
    }

    @Override
    protected int getMetricsCategory() {
        // todo add a constant in MetricsLogger.java
        return CMMetricsLogger.MAIN_SETTINGS;
    }
	
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mCameraDoubleTapPowerGesture) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getContentResolver(), CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED,
                    value ? 0 : 1 /* Backwards because setting is for disabling */);
            return true;
	}
        else {
            return false;
        }
    }

    private static boolean isCameraDoubleTapPowerGestureAvailable(Resources res) {
        return res.getBoolean(
                com.android.internal.R.bool.config_cameraDoubleTapPowerGestureEnabled);
    }	
	
    public static final Indexable.SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                                                                            boolean enabled) {
                    ArrayList<SearchIndexableResource> result =
                            new ArrayList<SearchIndexableResource>();

                    SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.gestures_settings;
                    result.add(sir);

                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    ArrayList<String> result = new ArrayList<String>();
                    return result;
                }
            };
}