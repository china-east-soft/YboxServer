package cn.cloudchain.yboxserver.bean;

public enum OperType {
	shutdown(100), battery(101), mobile_data_control(102), wifi_info_set(103), wifi_info(
			104), wifi_devices(105), wifi_blacklist_add(106), wifi_blacklist_clear(
			107), signal_quality(108), traffic(109), storage(110), ethernet_info(
			111), ethernet_dhcp_set(112), ethernet_static_set(113), wifi_restart(
			114), wifi_auto_disable_info(115), wifi_auto_disable_set(116), mobile_traffic_info(
			117), sleep(118), mobile_net_info(119), auto_sleep_set(120), auto_sleep_info(
			121), update_root_image(122), update_middle(123), download_root_image(
			124), download_middle_apk(125);

	private int value;

	private OperType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static OperType getOperType(int value) {
		OperType result = null;
		switch (value) {
		case 100:
			result = OperType.shutdown;
			break;
		case 101:
			result = OperType.battery;
			break;
		case 102:
			result = OperType.mobile_data_control;
			break;
		case 103:
			result = OperType.wifi_info_set;
			break;
		case 104:
			result = OperType.wifi_info;
			break;
		case 105:
			result = OperType.wifi_devices;
			break;
		case 106:
			result = OperType.wifi_blacklist_add;
			break;
		case 107:
			result = OperType.wifi_blacklist_clear;
			break;
		case 108:
			result = OperType.signal_quality;
			break;
		case 109:
			result = OperType.traffic;
			break;
		case 110:
			result = OperType.storage;
			break;
		case 111:
			result = OperType.ethernet_info;
			break;
		case 112:
			result = OperType.ethernet_dhcp_set;
			break;
		case 113:
			result = OperType.ethernet_static_set;
			break;
		case 114:
			result = OperType.wifi_restart;
			break;
		case 115:
			result = OperType.wifi_auto_disable_info;
			break;
		case 116:
			result = OperType.wifi_auto_disable_set;
			break;
		case 117:
			result = OperType.mobile_traffic_info;
			break;
		case 118:
			result = OperType.sleep;
			break;
		case 119:
			result = OperType.mobile_net_info;
			break;
		case 120:
			result = OperType.auto_sleep_set;
			break;
		case 121:
			result = OperType.auto_sleep_info;
			break;
		case 122:
			result = OperType.update_root_image;
			break;
		case 123:
			result = OperType.update_middle;
			break;
		case 124:
			result = OperType.download_root_image;
			break;
		case 125:
			result = OperType.download_middle_apk;
			break;
		}
		return result;
	}

}
