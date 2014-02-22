package cn.cloudchain.yboxserver.bean;

public enum OperType {
	shutdown(100), battery(101), mobile_data_control(102), wifi_info_set(103), wifi_info(
			104), wifi_devices(105), wifi_blacklist_add(106), wifi_blacklist_clear(
			107), signal_quality(108), traffic(109), storage(110), ethernet_info(
			111), ethernet_dhcp_set(112), ethernet_static_set(113), wifi_restart(
			114), wifi_auto_disable_info(115), wifi_auto_disable_set(116);

	private int value;

	private OperType(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}
}
