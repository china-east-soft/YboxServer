package cn.cloudchain.yboxserver.bean;

import android.util.SparseArray;

public class ErrorBean {
	public static final int REQUEST_TIMEOUT = 100;
	public static final int REQUEST_FORMAT_WRONG = 103;
	public static final int OPER_NOT_EXIST = 101;
	public static final int RESULT_FAIL = 102;
	public static final int REQUEST_PARAMS_INVALID = 104;
	public static final int SIM_NOT_READY = 201;
	public static final int SD_NOT_READY = 400;

	private static ErrorBean instance;
	private SparseArray<String> errorMap;

	private ErrorBean() {
		errorMap = new SparseArray<String>(6);
		errorMap.put(REQUEST_TIMEOUT, "request timeout");
		errorMap.put(REQUEST_FORMAT_WRONG, "request format invalid");
		errorMap.put(OPER_NOT_EXIST, "operation not exist");
		errorMap.put(RESULT_FAIL, "normal result fail");
		errorMap.put(REQUEST_PARAMS_INVALID, "request params wrong");
		errorMap.put(SIM_NOT_READY, "sim is not ready");
		errorMap.put(SD_NOT_READY, "sdcard not ready");
	}

	public static ErrorBean getInstance() {
		if (instance == null) {
			instance = new ErrorBean();
		}
		return instance;
	}

	public String getErrorMsg(int errorCode) {
		return errorMap.get(errorCode, "");
	}
}
