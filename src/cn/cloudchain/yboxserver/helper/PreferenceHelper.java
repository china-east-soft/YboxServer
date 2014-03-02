package cn.cloudchain.yboxserver.helper;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import cn.cloudchain.yboxserver.MyApplication;

public class PreferenceHelper {
	public static final String AUTO_SLEEP = "auto_sleep";

	public static final String ROOT_IMAGE_UPDATE = "root_image_update";
	public static final String ROOT_IMAGE_UPDATE_PATH = "root_image_update_path";
	/**
	 * 如果{@link ROOT_IMAGE_UPDATE}为该值，则需执行root image升级操作
	 */
	public static final int ROOT_IMAGE_UPDATE_RESTART = 1;

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

	public static boolean remove(String key) {
		Editor editor = prefSetting.edit();
		editor.remove(key);
		boolean result = editor.commit();
		editor = null;
		return result;
	}

	public static boolean putString(String key, String value) {
		Editor editor = prefSetting.edit();
		editor.putString(key, value);
		boolean result = editor.commit();
		editor = null;
		return result;
	}

	public static String getString(String key, String defValue) {
		return prefSetting.getString(key, defValue);
	}

}
