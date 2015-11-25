package lsparser.xmlparser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Calendar.Builder;
import java.util.List;

import lsparser.tycho.*;

public class TimeSeries {
	public List<Entry> entries;
	
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
	
	public class Entry {
		public int value;
		public ALSIDQueryInput alsIDQueryInput;
		
		public Entry(RowData rowData) {
			//String event = rowData.getEvent();
			alsIDQueryInput = new ALSIDQueryInput();
			Builder builder = new Calendar.Builder();
			
			value = rowData.getNumber().intValue();
			alsIDQueryInput.locationName = rowData.getLoc();
			alsIDQueryInput.details.put("locationType", rowData.getLocType());
			alsIDQueryInput.details.put("state", rowData.getState());
			alsIDQueryInput.details.put("country", rowData.getCountry());
			
			//set the date
			builder.set(Calendar.YEAR, rowData.getYear().intValue());
			builder.set(Calendar.WEEK_OF_YEAR, rowData.getWeek().intValue());
			alsIDQueryInput.date = builder.build().getTime();
			
			return;
		}
		
		public String toString() {
			String summary = "value: " + value;
			summary += (", ALSIDQueryInput: "+ alsIDQueryInput);
			
			return summary;
		}
	}
}
