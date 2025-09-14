package com.advisor.custom.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class StringUtils {
	public static boolean isNullOrEmpty(Object item) {
		return item == null || String.valueOf(item).isEmpty();
	}
	
	public static boolean isNotNullOrEmpty(Object item) {
		return !isNullOrEmpty(item);
	}
	
	public static String getMybatisId(String ifId, String queryType) {
		return ifId + "." + queryType;
	}
	
	public static String fillPlaceholder(String path, Map<String, String> placeholderMap) {
		String normailizePath = path.replace("\\", "/");
		String[] resultArr = Arrays.asList(normailizePath.split("/")).stream().map(item -> {
			if(placeholderMap.containsKey(item)) {
				return placeholderMap.get(item);
			} else {
				return item;
			}
		}).toArray(String[]::new);
		return String.join("/", resultArr);
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
}