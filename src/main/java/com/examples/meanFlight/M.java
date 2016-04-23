package com.examples.meanFlight;
import com.main.Context;
import com.map.Mapper;

public class M extends Mapper {

	@Override
	public void map(String key, String value, Context context) {
		
		try {
		CSVParser csvParser = new CSVParser();
		String[] record = csvParser.parseLine(value);
		Flight flight = FlightManager.getFlightDetails(record);
		if (flight.getAvgTicketPrice() != null) {
			if(FlightManager.isSane(flight) && !flight.getAvgTicketPrice().equals("")) {
				context.write(flight.getUniqueCarrier(), String.valueOf(flight.getAvgTicketPrice()));
			}
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
