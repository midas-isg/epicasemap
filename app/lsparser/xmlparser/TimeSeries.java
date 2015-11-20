package lsparser.xmlparser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Calendar.Builder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lsparser.tycho.*;

public class TimeSeries {
	private List<Entry> entries;
	
	public TimeSeries(List<RowData> rows) {
		int count = rows.size();
		entries = new ArrayList<Entry>();
		
		for(int i = 0; i < count; i++) {
			entries.add(new Entry(rows.get(i)));
		}
		
		return;
	}
	
	public void printSeries() {
		int count = entries.size();
		
		for(int i = 0; i < count; i++) {
			System.out.println(entries.get(i));
		}
		
		return;
	}
	
	private class Entry {
		int value;
		String locationName;
		String locationType;
		Calendar date;
		Map<String, String> details;
		
		public Entry(RowData rowData) {
			String event;
			Builder builder = new Calendar.Builder();
			
			value = rowData.getNumber().intValue();
			locationName = rowData.getLoc();
			locationType = rowData.getLocType();
			details = new HashMap<String, String>();
			details.put("state", rowData.getState());
			details.put("country", rowData.getCountry());
			
			//set the date
			builder.set(Calendar.YEAR, rowData.getYear().intValue());
			builder.set(Calendar.WEEK_OF_YEAR, rowData.getWeek().intValue());
			date = builder.build();
			
			event = rowData.getEvent();
			
			return;
		}
		
		public String toString() {
			String summary;
			
			summary = "value: " + value;
			summary += (", location: " + locationName);
			summary += (", locationType: " + locationType);
			summary += (", date: " + date.getTime());
			summary += (", details: " + details);
			
			return summary;
		}
	}
}
