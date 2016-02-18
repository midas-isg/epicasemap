package lsparser.xmlparser;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ALSIDQueryInput {
	public String locationName;
	public Date date;
	public long number;
	public Map<String, String> details;
	
	public ALSIDQueryInput() {
		details = new HashMap<String, String>();
		
		return;
	}
	
	public String toString() {
		String summary;
		
		summary = "{location: " + locationName;
		summary += (", date: " + date);
		summary += (", number: " + number);
		summary += (", details: " + details + "}");
		
		return summary;
	}
}
