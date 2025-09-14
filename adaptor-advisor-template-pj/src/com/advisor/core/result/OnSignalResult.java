package com.advisor.core.result;

import java.util.Map;

import com.advisor.core.data.InterfaceInfo;

import lombok.Data;

@Data
public class OnSignalResult {
	private InterfaceInfo interfaceInfo;
	private int count;
	private Object pollDataObj;
	private Map<String, String> properties;
	
	public void addProperty(String key, String value) {
		this.properties.put(key, value);
	}
}
