package com.oddrock.caj2pdf.utils;

import com.oddrock.common.prop.PropertiesReader;

public class Prop {
	private static final PropertiesReader PR = new PropertiesReader();
	static{
		load();
	}
	
	private static void load(){
		PR.addFilePath("caj2pdf.properties");
		PR.loadProperties();
		for(String filePath : PR.getValue("properties.filepath").split(",")) {
			PR.addFilePath(filePath);
			PR.loadProperties();
		}
	}
	
	public static String get(String key){
		return PR.getValue(key);
	}
	
	public static int getInt(String key){
		return PR.getIntValue(key);
	}
	
	public static long getLong(String key){
		return PR.getLongValue(key);
	}
	
	public static boolean getBool(String key){
		return PR.getBooleanValue(key);
	}
	
	public static String get(String key, String defaultValue) {
		return PR.getValue(key, defaultValue);
	}
}