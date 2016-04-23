/*
 * @author Mehta, Deepen  
 * @created Apr 8, 2016 
 */

package com.sort;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Custom class for Sorting the data
 * @author Deepen
 *
 */
public class SortObject {

	public String key;
	private String value;


	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @param key
	 * @param value
	 */
	public SortObject(String key, String value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * Method to insert the records in a HashMap
	 * @param dataMap
	 * @param record
	 */
	public static void upsertData(HashMap<String, ArrayList<SortObject>> dataMap, SortObject record) {
		if (dataMap.containsKey(record.getKey())) {
			ArrayList<SortObject> temp = dataMap.get(record.getKey());
			temp.add(record);
		} else {
			ArrayList<SortObject> newList = new ArrayList<SortObject>();
			newList.add(record);
			dataMap.put(record.getKey(), newList);
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SortObject [key=" + key + ", value=" + value + "]";
	}
}

/**
 * Custom class for compare method 
 * @author Deepen
 *
 */
class SortComparator implements Comparator<SortObject> {
	public int compare(SortObject object1, SortObject object2) {
		return object1.getKey().compareToIgnoreCase(object2.getKey());
	}
}