package cn.cloudchain.yboxserver.helper;

import android.os.SystemProperties;

public class ApHelper {
	
	public static void startHls() {
		SystemProperties.set("ctl.start", "hls_service");
	}
	
	public static void endHls() {
		SystemProperties.set("ctl.stop", "hls_service");
	}

}
