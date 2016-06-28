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

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.provider.SearchIndexableResource;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import org.cyanogenmod.internal.logging.CMMetricsLogger;

public class Information extends SettingsPreferenceFragment
        implements Indexable {

    private static final String TAG = Information.class.getSimpleName();
	
	private static final String KEY_DEVICE_MAINTAINER = "device_maintainer";
	
	Preference mWebUrl;
	Preference mGoogleUrl;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.info_settings);
        final PreferenceScreen prefScreen = getPreferenceScreen();
		
		mWebUrl = findPreference("cypher_web");
		mGoogleUrl = findPreference("cypher_plus");
		
		setMaintainerSummary(KEY_DEVICE_MAINTAINER, "ro.cypher.maintainer");

    }

    @Override
    protected int getMetricsCategory() {
        // todo add a constant in MetricsLogger.java
        return CMMetricsLogger.MAIN_SETTINGS;
    }
	
	@Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mWebUrl) {
            launchUrl("get.cypheros.co");
        }
		} else if (preference == mGoogleUrl) {
            launchUrl("https://plus.google.com/communities/111402352496339801246");
		}
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
	
	private void launchUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent donate = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(donate);
    }
	
	private void setMaintainerSummary(String preference, String property) {
        try {
            String maintainers = SystemProperties.get(property,
                    getResources().getString(R.string.device_info_default));
            findPreference(preference).setSummary(maintainers);
            if (maintainers.contains(",")) {
                findPreference(preference).setTitle(
                        getResources().getString(R.string.device_maintainers));
            }
        } catch (RuntimeException e) {
            // No recovery
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
                    sir.xmlResId = R.xml.info_settings;
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