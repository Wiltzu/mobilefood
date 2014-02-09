package fi.nottingham.mobilefood;

import java.util.List;

import com.google.common.collect.Lists;

public class MobilefoodModules {
	
	private static List<Object> modules = null;
	
	public static List<Object> getModules() {
		//TODO: get modules by activity?
		if(modules == null) {
			modules = Lists.newArrayList();
			modules.add(new MobilefoodModule());
		}
		return modules;
	}
	
	private MobilefoodModules(){
	}
}
