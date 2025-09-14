package com.advisor.custom.common;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class StringUtils {
	public static boolean isNullOrEmpty(Object item) {
		return item == null || String.valueOf(item).isEmpty();
	}
	
	public static boolean isNotNullAndEmpty(Object item) {
		return !isNullOrEmpty(item);
	}
	
	public static String getMybatisId(String ifId, String queryType) {
		return ifId + "." + queryType;
	}
	
	public static String getPkId(Map<String, Object> map, String[] privateKeyArr) {
		StringBuffer sb = new StringBuffer();
		for(String key : privateKeyArr) {
			if(map.containsKey(key) && isNotNullAndEmpty(map.get(key))) {
				sb.append(String.valueOf(map.get(key)));
			}
		}
		return sb.toString();
	}
	
	public static String fillPlaceholder(String path, Map<String, String> placeholderMap) {
		String normailizePath = path.replace("\\", "/");
		String[] parts = normailizePath.split("/");
		StringBuffer sb = new StringBuffer();
		for(String part : parts) {
			if(placeholderMap.containsKey(part)) {
				String value = placeholderMap.get(part);
				if(isNotNullAndEmpty(part)) {
					sb.append(value).append(File.separator);
				}
			} else {
				sb.append(part).append(File.separator);
			}
		}
		String result = sb.toString();
		return result.endsWith("/") ? result.substring(0, result.length()) : result;
	}
	
	public static String fillPalceholder(String path, String dateTime, String date, String time, String ifId, String txId) {
		Map<String, String> placeholderMap = new HashMap<String, String>();
		placeholderMap.put("yyyyMMddHHmmssSSS", dateTime);
		placeholderMap.put("yyyyMMdd", date);
		placeholderMap.put("HHmmssSSS", time);
		placeholderMap.put("ifId", ifId);
		placeholderMap.put("txId", txId);
		return fillPlaceholder(path, placeholderMap);
	}
	
	public static String fillPalceholder(String path, String dateTime, String date, String time, String ifId, String txId, String pkId) {
		Map<String, String> placeholderMap = new HashMap<String, String>();
		placeholderMap.put("yyyyMMddHHmmssSSS", dateTime);
		placeholderMap.put("yyyyMMdd", date);
		placeholderMap.put("HHmmssSSS", time);
		placeholderMap.put("ifId", ifId);
		placeholderMap.put("txId", txId);
		placeholderMap.put("pkId", pkId);
		return fillPlaceholder(path, placeholderMap);
	}
}