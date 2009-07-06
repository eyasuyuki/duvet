package org.javaopen.chaton;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Settings extends PreferenceActivity {
	public static final String OPT_URI = "uri";
	public static final String OPT_URI_DEF = "";
	public static final String OPT_NICKNAME = "nickname";
	public static final String OPT_NICKNAME_DEF = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}
	
	public static String getUri(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getString(OPT_URI, OPT_URI_DEF);
	}
	public static String getNickname(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
			.getString(OPT_NICKNAME, OPT_NICKNAME_DEF);
	}
}
