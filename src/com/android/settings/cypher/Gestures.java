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

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Message;
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
import static android.provider.Settings.Secure.DOUBLE_TAP_TO_WAKE;

import java.util.ArrayList;
import java.util.List;

import org.cyanogenmod.internal.logging.CMMetricsLogger;

public class Gestures extends SettingsPreferenceFragment
    implements Preference.OnPreferenceChangeListener, Indexable {

    private static final String TAG = Gestures.class.getSimpleName();
	
	private static final String KEY_CATEGORY_POWER = "power_key";
	private static final String KEY_CAMERA_DOUBLE_TAP_POWER_GESTURE
            = "camera_double_tap_power_gesture";
	private static final String KEY_TAP_TO_WAKE 
	        = "tap_to_wake";
	private static final String KEY_MOTION_GESTURES 
            = "motion_gestures";
			
	private SwitchPreference mCameraDoubleTapPowerGesture;
	private SwitchPreference mTapToWakePreference;
	private PreferenceScreen mGestureMotionPreference;
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.gestures_settings);
        final PreferenceScreen prefScreen = getPreferenceScreen();
		
		PreferenceCategory powerPrefs = (PreferenceCategory)
                findPreference(KEY_CATEGORY_POWER);

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
		
		mTapToWakePreference = (SwitchPreference) findPreference(KEY_TAP_TO_WAKE);
        if (mTapToWakePreference != null && isTapToWakeAvailable(getResources())) {
            mTapToWakePreference.setOnPreferenceChangeListener(this);
        } else {
            if (powerPrefs != null && mTapToWakePreference != null) {
                powerPrefs.removePreference(mTapToWakePreference);
            }
        }
		
		mGestureMotionPreference = (prefScreen) findPreference(KEY_MOTION_GESTURES);
		if (mGestureMotionPreference !=null && isMotionGesturesAvailable(getResources())) {
			mGestureMotionPreference.setOnPreferenceChangeListener(this);
		} else {
		    powerPrefs.removePreference(mGestureMotionPreference);
		}
		
		
    }

    @Override
    protected int getMetricsCategory() {
        // todo add a constant in MetricsLogger.java
        return CMMetricsLogger.MAIN_SETTINGS;
    }
	
	@Override
    public void onResume() {
        super.onResume();
        updateState();
    }
	
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mCameraDoubleTapPowerGesture) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getContentResolver(), CAMERA_DOUBLE_TAP_POWER_GESTURE_DISABLED,
                    value ? 0 : 1 /* Backwards because setting is for disabling */);
        }
		if (preference == mTapToWakePreference) {
            boolean value = (Boolean) newValue;
            Settings.Secure.putInt(getContentResolver(), DOUBLE_TAP_TO_WAKE, value ? 1 : 0);
        }
	    return true;
    }
	
	@Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mTapToWakePreference) {
            if (mTapToWakePreference.isChecked()) {
                warnTapToWake();
            } else {
               isTapToWakeAvailable(getResources());
            }
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return false;
    }

    private static boolean isCameraDoubleTapPowerGestureAvailable(Resources res) {
        return res.getBoolean(
                com.android.internal.R.bool.config_cameraDoubleTapPowerGestureEnabled);
    }	
	
	private static boolean isTapToWakeAvailable(Resources res) {
        return res.getBoolean(com.android.internal.R.bool.config_supportDoubleTapWake);
    }
	
	private static boolean isMotionGesturesAvailable(Resources res) {
		return res.getBoolean(
		        com.android.internal.R.bool.config_isMotionSupported);
	}
	
	private void warnTapToWake() {
		DialogInterface.OnClickListener onConfirmListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isTapToWakeAvailable(getResources());
                updateState();
            }
        };
		
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.tap_to_wake_warning)
                .setMessage(R.string.tap_to_wake_warning_message)
                .setPositiveButton(R.string.ok_string, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Enable tap to wake when user presses ok
                        updateState();
                    }
                })
                .setNegativeButton(android.R.string.cancel, onConfirmListener)
		        .setCancelable(false)
                .create()
                .show();
    }
	
	private void updateState() {
        // Update tap to wake if it is available.
        if (mTapToWakePreference != null) {
            int value = Settings.Secure.getInt(getContentResolver(), DOUBLE_TAP_TO_WAKE, 0);
            mTapToWakePreference.setChecked(value != 0);
        }
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
					if (!isTapToWakeAvailable(context.getResources())) {
                        result.add(KEY_TAP_TO_WAKE);
                    }
                    return result;
                }
            };
}