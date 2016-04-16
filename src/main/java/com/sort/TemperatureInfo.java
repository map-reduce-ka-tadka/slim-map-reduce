/*
 * @author Mehta, Deepen  
 * @created Apr 8, 2016 
 */

package com.sort;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class TemperatureInfo {

	long wban;
	String date;
	String time;
	Double temperature;

	public long getWban() {
		return wban;
	}

	public void setWban(long wban) {
		this.wban = wban;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public Double getTemperature() {
		return temperature;
	}

	public void setTemperature(Double temperature) {
		this.temperature = temperature;
	}

	public TemperatureInfo() {

	}


	public static TemperatureInfo createInstance(String[] record) {

		try {
			TemperatureInfo ti = new TemperatureInfo();
			ti.setWban(Long.parseLong(record[0]));
			ti.setDate(record[1]);
			ti.setTime(record[2]);
			ti.setTemperature(Double.parseDouble(record[8]));
			return ti;
		} catch (NumberFormatException|ArrayIndexOutOfBoundsException n) {
			return null;
		}

	}

	public static void upsertData(HashMap<Double, ArrayList<TemperatureInfo>> dataMap, TemperatureInfo record) {
		if (dataMap.containsKey(record.getTemperature())) {
			ArrayList<TemperatureInfo> temp = dataMap.get(record.getTemperature());
			temp.add(record);
		} else {
			ArrayList<TemperatureInfo> newList = new ArrayList<TemperatureInfo>();
			newList.add(record);
			dataMap.put(record.getTemperature(), newList);
		}
	}

	@Override
	public String toString() {
		return wban + "," + date + "," + time + "," + temperature;
	}

}

class TemperatureComparator implements Comparator<TemperatureInfo> {
	public int compare(TemperatureInfo object1, TemperatureInfo object2) {
		return object1.getTemperature().compareTo(object2.getTemperature());
	}
}

