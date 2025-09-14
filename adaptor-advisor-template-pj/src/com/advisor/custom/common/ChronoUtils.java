package com.advisor.custom.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ChronoUtils {
	private final static DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
	private final static DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
	private final static DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HHmmssSSS");
	public static String DATE_TIME = "DATE_TIME";
	public static String DATE = "DATE";
	public static String TIME = "TIME";
	
	public static Map<String, String> getChronoInfo() {
		Map<String, String> map = new HashMap<String, String>();
		String dateTime = getNowDateTime();
		map.put(DATE_TIME, dateTime);
		map.put(DATE, dateTime.substring(0, 8));
		map.put(TIME, dateTime.substring(8));
		return map;
	}
	
	public static String getNowDateTime() {
		return LocalDateTime.now().format(DATE_TIME_FMT);
	}
	
	public static String getNowDate() {
		return LocalDateTime.now().format(DATE_FMT);
	}
	
	public static String getNowTime() {
		return LocalDateTime.now().format(TIME_FMT);
	}
	
	public static Map<String, String> getChronoInfoFromTxId(String txId){
		String[] parts = txId.split("_");
		if(parts.length > 1 && parts[1].length() == 17) {
			Map<String, String> map = new HashMap<String, String>();
			String dateTime = parts[1];
			map.put(DATE_TIME, dateTime);
			map.put(DATE, dateTime.substring(0, 8));
			map.put(TIME, dateTime.substring(8));
			return map;
		} else {
			return null;
		}
	}
}
