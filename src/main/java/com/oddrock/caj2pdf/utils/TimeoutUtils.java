package com.oddrock.caj2pdf.utils;

public class TimeoutUtils {
	public static Long getTimeout(String name) {
		if(Prop.get(name)!=null) {
			return Prop.getLong(name);
		}else {
			return Prop.getLong("timeout");
		}
	}
}
