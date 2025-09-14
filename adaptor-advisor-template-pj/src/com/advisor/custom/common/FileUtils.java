package com.advisor.custom.common;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileUtils {
	private static List<Character> CHARS = Arrays.asList('*', '?', '{', '[');
	public static String BASE_DIR = "BASE_DIR";
	public static String GLOB_PATTERN = "GLOB_PATTERN";
	public static String FILE_BASE_NAME = "FILE_BASE_NAME";
	public static String FILE_EXTENSION = "FILE_EXTENSION";
	
	public static Map<String, String> fileNameInfo(String path) {
		Map<String, String> map = new HashMap<String, String>();
		String[] parts = path.replace("\\", "/").split("/");
		String name = parts[parts.length - 1];
		int index = name.lastIndexOf('.');
		if(index > 0) {
			map.put(FILE_BASE_NAME, name.substring(0, index));
			map.put(FILE_EXTENSION, name.substring(index + 1));
		} else {
			map.put(FILE_BASE_NAME, name);
			map.put(FILE_EXTENSION, null);
		}
		return map;
	}
	
	public static Map<String, String> fileNameInfo(Path path){
		return fileNameInfo(path.toAbsolutePath().toString());
	}
	
	public static Map<String, String> getBaseAndGlobInfo(String path) {
		Map<String, String> map = new HashMap<String, String>();
		String normalizedPath = path.replace("\\", "/");
		String[] parts = normalizedPath.split("/");
		
		String baseDir = null;
		String globPattern = null;
		int index = -1;
		for(int i = 0; i < parts.length; i++) {
			String part = parts[i];
			for(Character c : CHARS) {
				if(part.indexOf(c) > 0) {
					index = i;
					break;
				}
			}
			if(index > 0) {
				break;
			}
		}
		
		if(index > 0) {
			baseDir = String.join("/", Arrays.copyOfRange(parts, 0, index));
			globPattern = String.join("/", Arrays.copyOfRange(parts, index, parts.length - 1));
		} else {
			baseDir = normalizedPath;
			globPattern = "*";
		}
		map.put(BASE_DIR, baseDir);
		map.put(GLOB_PATTERN, globPattern);
		return map;
	}
}
