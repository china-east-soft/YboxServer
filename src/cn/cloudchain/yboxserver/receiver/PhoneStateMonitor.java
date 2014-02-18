package cn.cloudchain.yboxserver.receiver;

import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import cn.cloudchain.yboxserver.MyApplication;

public class PhoneStateMonitor extends PhoneStateListener {
	final String TAG = PhoneStateMonitor.class.getSimpleName();
	private TelephonyManager telManager;

	public PhoneStateMonitor(TelephonyManager telManager) {
		this.telManager = telManager;
	}

	@Override
	public void onSignalStrengthsChanged(SignalStrength signalStrength) {
		super.onSignalStrengthsChanged(signalStrength);
		int strength = -1;
		int phoneType = telManager.getPhoneType();
		Log.i(TAG, "phone type = " + telManager.getPhoneType());
		if (phoneType == TelephonyManager.PHONE_TYPE_GSM) {
			strength = signalStrength.getGsmSignalStrength() * 2 - 113;
		} else if (phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
			int netType = telManager.getNetworkType();
			Log.i(TAG, "net type = " + telManager.getNetworkType());

			switch (netType) {
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
				strength = signalStrength.getEvdoDbm();
				break;
			default:
				strength = signalStrength.getCdmaDbm();
				break;
			}
		}
		Log.i(TAG, "信号强度 = " + strength + "Dbm");
		MyApplication.getInstance().signalStrength = strength;
	}

	@Override
	public void onServiceStateChanged(ServiceState serviceState) {
		super.onServiceStateChanged(serviceState);
	}
}