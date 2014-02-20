package cn.cloudchain.yboxserver.bean;

public enum OperType {
	shutdown(100), battery(101), mobile_data_control(102), wifi_info_set(103), wifi_info(
			104), wifi_devices(105), wifi_blacklist_add(106), wifi_blacklist_clear(
			107), signal_quality(108), traffic(109), storage(110);

	private int value;

	private OperType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}
}
