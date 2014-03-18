package cn.cloudchain.yboxserver.helper;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.JsonWriter;
import cn.cloudchain.yboxcommon.bean.Constants;
import cn.cloudchain.yboxcommon.bean.ErrorBean;

public class FileManager {
	final static String TAG = FileManager.class.getSimpleName();
	private static FileManager instance;

	public static FileManager getInstance() {
		if (instance == null)
			instance = new FileManager();
		return instance;
	}

	/**
	 * 获取文件夹具体信息
	 * 
	 * @param directoryPath
	 *            如果该值为null或者空，则默认为SD卡路径
	 * @return 文件列表详情
	 */
	public String getFilesInDirectory(String directoryPath) {
		if (!isSDcardAvailable()) {
			return Helper.getInstance().getErrorJson(ErrorBean.SD_NOT_READY);
		}

		File directory = null;
		if (TextUtils.isEmpty(directoryPath)) {
			directory = Environment.getExternalStorageDirectory();
		} else {
			directory = new File(directoryPath);
		}

		if (!directory.exists()) {
			return Helper.getInstance().getErrorJson(ErrorBean.FILE_NOT_EXIST);
		}

		if (!directory.isDirectory()) {
			return Helper.getInstance().getErrorJson(
					ErrorBean.FILE_NOT_DIRECTORY);
		}

		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject();
			jWriter.name(Constants.RESULT).value(true)
					.name(Constants.File.FILES);
			File[] files = directory.listFiles();
			jWriter.beginArray();
			for (File file : files) {
				jWriter.beginObject();
				jWriter.name(Constants.File.NAME).value(file.getName());
				jWriter.name(Constants.File.PATH_ABSOLUTE).value(
						file.getAbsolutePath());
				boolean isDirectory = file.isDirectory();
				jWriter.name(Constants.File.IS_DIRECTORY).value(isDirectory);
				if (isDirectory) {
					jWriter.name(Constants.File.CHILDREN_NUM).value(
							file.list().length);
				} else {
					jWriter.name(Constants.File.SIZE).value(file.length());
				}
				jWriter.name(Constants.File.LAST_MODIFY_TIME).value(
						file.lastModified());
				jWriter.endObject();
			}
			jWriter.endArray();
			jWriter.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sw.toString();
	}

	/**
	 * 获取SD存储卡的存储信息
	 * 
	 * @return {"result":true, "total":717272771, "remain":72771727}
	 */
	public String getStorageInfo() {
		long totalSize = getSDcardTotalMemory();
		if (totalSize < 0) {
			return Helper.getInstance().getErrorJson(ErrorBean.SD_NOT_READY);
		}
		long availableSize = getSDcardAvailableMemory();

		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(Constants.RESULT).value(true);
			jWriter.name(Constants.File.MEM_TOTAL).value(totalSize);
			jWriter.name(Constants.File.MEM_REMAIN).value(availableSize);
			jWriter.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sw.toString();
	}

	/**
	 * 返回SD卡的总存储空间，单位b
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private long getSDcardTotalMemory() {
		long memory = -1;
		if (isSDcardAvailable()) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs statFs = new StatFs(sdcardDir.getPath());
			long blockCount = statFs.getBlockCount();
			long blockSize = statFs.getBlockSize();
			memory = blockCount * blockSize;
		}
		return memory;
	}

	/**
	 * 返回SD卡的剩余存储空间，单位b
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private long getSDcardAvailableMemory() {
		long memory = -1;
		if (isSDcardAvailable()) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs statFs = new StatFs(sdcardDir.getPath());
			long blockSize = statFs.getBlockSize();
			long availableBlock = statFs.getAvailableBlocks();
			memory = blockSize * availableBlock;
		}
		return memory;
	}

	/**
	 * 返回SD卡是否可用
	 * 
	 * @return
	 */
	private boolean isSDcardAvailable() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

}
