package cn.cloudchain.yboxserver.helper;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import cn.cloudchain.yboxserver.MyApplication;

public class PreferenceHelper {
	public static final String AUTO_SLEEP = "auto_sleep";
	public static final int AUTO_SLEEP_OFF = 0;
	public static final int AUTO_SLEEP_FOR_10 = 1;
	public static final int AUTO_SLEEP_FOR_30 = 2;

	private static SharedPreferences prefSetting = PreferenceManager
			.getDefaultSharedPreferences(MyApplication.getAppContext());

	public static boolean putInt(String key, int value) {
		Editor editor = prefSetting.edit();
		editor.putInt(key, value);
		boolean result = editor.commit();
		editor = null;
		return result;
	}

	public static int getInt(String key, int defValue) {
		return prefSetting.getInt(key, defValue);
	}

}
