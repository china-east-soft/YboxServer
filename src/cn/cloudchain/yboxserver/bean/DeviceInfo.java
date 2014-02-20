package cn.cloudchain.yboxserver.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class DeviceInfo implements Parcelable {
	public String mac;
	public String ip;
	public String name;
	public boolean blocked;

	public static final Parcelable.Creator<DeviceInfo> CREATOR = new Parcelable.Creator<DeviceInfo>() {

		@Override
		public DeviceInfo createFromParcel(Parcel source) {
			DeviceInfo info = new DeviceInfo();
			info.mac = source.readString();
			info.ip = source.readString();
			info.name = source.readString();
			info.blocked = source.readInt() > 0 ? true : false;
			return info;
		}

		@Override
		public DeviceInfo[] newArray(int size) {
			return new DeviceInfo[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mac);
		dest.writeString(ip);
		dest.writeString(name);
		dest.writeInt(blocked ? 1 : 0);
	}

}
