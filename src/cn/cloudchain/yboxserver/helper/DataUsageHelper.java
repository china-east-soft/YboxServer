package cn.cloudchain.yboxserver.helper;

import static android.net.NetworkPolicyManager.computeLastCycleBoundary;
import static android.net.NetworkPolicyManager.computeNextCycleBoundary;
import static android.net.NetworkStatsHistory.FIELD_RX_BYTES;
import static android.net.NetworkStatsHistory.FIELD_TX_BYTES;
import static android.net.NetworkTemplate.buildTemplateWifiWildcard;
import static android.net.NetworkTemplate.buildTemplateMobileAll;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import com.mediatek.common.featureoption.FeatureOption;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.provider.Telephony;
import android.provider.Telephony.SIMInfo;
import android.net.INetworkStatsService;
import android.net.INetworkStatsSession;
import android.net.NetworkStatsHistory;
import android.net.NetworkTemplate;
import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import com.android.settings.net.ChartData;
import com.android.settings.net.NetworkPolicyEditor;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;

public class DataUsageHelper {
	private final String TAG = DataUsageHelper.class.getSimpleName();

	private Context context;
	private INetworkStatsService mStatsService;
	// private INetworkStatsSession mStatsSession;
	private NetworkPolicyEditor mPolicyEditor;
	private NetworkPolicyManager mPolicyManager;

	private static final String TEST_SUBSCRIBER_PROP = "test.subscriberid";
	private static final int POLICY_NULL_FLAG = -2;
	private static final int CYCLE_RANGE_OVER_WEEK = 4;

	// 流量相关
	public static final int KEY_RX_TODAY = 10;
	public static final int KEY_TX_TODAY = 11;
	public static final int KEY_RX_TOTAL = 20;
	public static final int KEY_TX_TOTAL = 21;
	public static final int KEY_RX_MONTH = 30;
	public static final int KEY_TX_MONTH = 31;
	public static final int KEY_LIMIT_MONTH = 40;
	public static final int KEY_SIM_SLOT = 50;

	public DataUsageHelper(Context context) {
		refreshPolicy();
		mStatsService = INetworkStatsService.Stub.asInterface(ServiceManager
				.getService(Context.NETWORK_STATS_SERVICE));
	}

	// mTemplate = buildTemplateEthernet()
	// getLoaderManager().restartLoader(LOADER_CHART_DATA,
	// ChartDataLoader.buildArgs(mTemplate, mCurrentApp), mChartDataCallbacks);

	/**
	 * 获取wlan相关信息
	 * 
	 * @return
	 */
	public SparseArray<Long> getWifiData() {
		ChartData data = new ChartData();
		try {
			INetworkStatsSession mStatsSession = mStatsService.openSession();
			data.network = mStatsSession.getHistoryForNetwork(
					buildTemplateWifiWildcard(), FIELD_RX_BYTES
							| FIELD_TX_BYTES);
		} catch (RemoteException e) {
			Log.d(TAG, "Remote Exception happens");
		}
		long historyStart = data.network.getStart();
		long historyEnd = data.network.getEnd();
		final long now = System.currentTimeMillis();
		Log.d(TAG, "historyStart = " + historyStart + " historyEnd = "
				+ historyEnd + " now = " + now);
		long cycleEnd = historyEnd;
		long cycleEndBak = historyEnd;
		long cycleStart = historyStart;
		while (cycleEnd > historyStart) {
			cycleStart = cycleEnd
					- (DateUtils.WEEK_IN_MILLIS * CYCLE_RANGE_OVER_WEEK);
			if (cycleStart <= now && now <= cycleEnd) {
				Log.d(TAG, "cycleStart <= now && now <= cycleEnd");
				break;
			}
			cycleEndBak = cycleEnd;
			cycleEnd = cycleStart;
		}

		SparseArray<Long> map = new SparseArray<Long>(6);

		// 获取总流量信息
		NetworkStatsHistory.Entry entry = data.network.getValues(cycleStart,
				cycleEndBak, now, null);
		map.put(KEY_RX_TOTAL, entry != null ? entry.rxBytes : 0L);
		map.put(KEY_TX_TOTAL, entry != null ? entry.txBytes : 0L);

		// 获取当日流量信息
		entry = data.network.getValues(getUtcDateMillis(), now, now, null);
		map.put(KEY_RX_TODAY, entry != null ? entry.rxBytes : 0L);
		map.put(KEY_TX_TODAY, entry != null ? entry.txBytes : 0L);

		// 获取当月流量信息
		entry = data.network.getValues(getUtcMonthMillis(), now, now, null);
		map.put(KEY_RX_MONTH, entry != null ? entry.rxBytes : 0L);
		map.put(KEY_TX_MONTH, entry != null ? entry.txBytes : 0L);
	}

	/**
	 * 获取手机流量相关信息
	 * 
	 * @return
	 */
	public List<SparseArray<Long>> getMobileData() {
		List<SIMInfo> mSimList = SIMInfo.getInsertedSIMList(context);
		if (mSimList == null)
			return null;
		INetworkStatsSession mStatsSession = null;
		try {
			mStatsSession = mStatsService.openSession();
		} catch (RemoteException e) {
			Log.d(TAG, "Remote Exception happens");
		}
		if (mStatsSession == null)
			return null;

		List<SparseArray<Long>> list = new ArrayList<SparseArray<Long>>(
				mSimList.size());

		ChartData data = new ChartData();
		NetworkTemplate template;
		NetworkPolicy policy;

		for (SIMInfo siminfo : mSimList) {
			if (FeatureOption.MTK_GEMINI_SUPPORT) {
				template = buildTemplateMobileAll(getSubscriberId(context,
						siminfo.mSlot));
			} else {
				template = buildTemplateMobileAll(getActiveSubscriberId(context));
			}

			long mLimitBytes = POLICY_NULL_FLAG;
			try {
				data.network = mStatsSession.getHistoryForNetwork(template,
						FIELD_RX_BYTES | FIELD_TX_BYTES);
				mLimitBytes = mPolicyEditor.getPolicyLimitBytes(template);
			} catch (Exception e) {
				continue;
			}

			long historyStart = data.network.getStart();
			long historyEnd = data.network.getEnd();
			final long now = System.currentTimeMillis();
			policy = mPolicyEditor.getPolicy(template);
			long cycleEnd = historyEnd;
			long cycleEndBak = historyEnd;
			long cycleStart = historyStart;
			if (policy != null) {
				cycleEnd = computeNextCycleBoundary(historyEnd, policy);
				while (cycleEnd > historyStart) {
					cycleStart = computeLastCycleBoundary(cycleEnd, policy);
					if (cycleStart <= now && now <= cycleEnd) {
						Log.d(TAG, "cycleStart <= now && now <= cycleEnd");
						break;
					}
					cycleEndBak = cycleEnd;
					cycleEnd = cycleStart;
				}
			} else {
				while (cycleEnd > historyStart) {
					cycleStart = cycleEnd
							- (DateUtils.WEEK_IN_MILLIS * CYCLE_RANGE_OVER_WEEK);
					if (cycleStart <= now && now <= cycleEnd) {
						break;
					}
					cycleEndBak = cycleEnd;
					cycleEnd = cycleStart;
				}
			}
			Log.d(TAG, "cycleEndBak=" + cycleEndBak + "cycleStart="
					+ cycleStart);

			SparseArray<Long> map = new SparseArray<Long>(7);

			// 获取总流量信息
			NetworkStatsHistory.Entry entry = data.network.getValues(
					cycleStart, cycleEndBak, now, null);
			map.put(KEY_RX_TOTAL, entry != null ? entry.rxBytes : 0L);
			map.put(KEY_TX_TOTAL, entry != null ? entry.txBytes : 0L);

			// 获取当日流量信息
			entry = data.network.getValues(getUtcDateMillis(), now, now, null);
			map.put(KEY_RX_TODAY, entry != null ? entry.rxBytes : 0L);
			map.put(KEY_TX_TODAY, entry != null ? entry.txBytes : 0L);

			// 获取当月流量信息
			entry = data.network.getValues(getUtcMonthMillis(), now, now, null);
			map.put(KEY_RX_MONTH, entry != null ? entry.rxBytes : 0L);
			map.put(KEY_TX_MONTH, entry != null ? entry.txBytes : 0L);

			map.put(KEY_LIMIT_MONTH, mLimitBytes);
			map.put(KEY_SIM_SLOT, (long) siminfo.mSlot);

			list.add(map);
		}

		return list;

	}

	/**
	 * 刷新PolicyEditor，重新调用read()方法
	 */
	public void refreshPolicy() {
		if (mPolicyManager == null) {
			mPolicyManager = NetworkPolicyManager.from(context);
		}
		if (mPolicyEditor == null) {
			mPolicyEditor = new NetworkPolicyEditor(mPolicyManager);
		}
		if (mPolicyEditor != null) {
			mPolicyEditor.read();
		}
	}

	private String getActiveSubscriberId(Context context) {
		final TelephonyManager tele = TelephonyManager.from(context);
		final String actualSubscriberId = tele.getSubscriberId();
		return SystemProperties.get(TEST_SUBSCRIBER_PROP, actualSubscriberId);
	}

	private static String getSubscriberId(Context context, int simId) {
		final TelephonyManager telephony = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return telephony.getSubscriberIdGemini(simId);
	}

	/**
	 * 获取当天开始的时间2014-12-32 00:00:00.000
	 * 
	 * @return
	 */
	private long getUtcDateMillis() {
		Calendar calendar = Calendar.getInstance(Locale.getDefault());
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTimeInMillis();
	}

	/**
	 * 获取当月开始的时间 2014-12-01 00:00:00.000
	 * 
	 * @return
	 */
	private long getUtcMonthMillis() {
		Calendar calendar = Calendar.getInstance(Locale.getDefault());
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTimeInMillis();
	}

}
