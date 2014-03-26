package cn.cloudchain.yboxserver.helper;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import cn.cloudchain.yboxserver.MyApplication;

public class PreferenceHelper {
	public static final String AUTO_SLEEP = "auto_sleep";

	public static final String IMAGE_VERSION = "image_version";
	public static final String IMAGE_UPDATE = "image_update";
	public static final String IMAGE_UPDATE_PATH = "image_update_path";
	public static final String MIDDLE_VERSION = "middle_version";
	public static final String MIDDLE_UPDATE = "_middle_update";
	public static final String MIDDLE_UPDATE_PATH = "middle_update_path";
	/**
	 * 如果{@link ROOT_IMAGE_UPDATE}为该值，则需执行root image升级操作
	 */
	public static final int IMAGE_UPDATE_RESTART = 1;
	public static final int MIDDLE_UPDATE_RESTART = 1;

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
