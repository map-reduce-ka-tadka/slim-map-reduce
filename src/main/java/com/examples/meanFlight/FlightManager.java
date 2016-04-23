package com.examples.meanFlight;
import java.io.IOException;

/**
 * FlightManager has several utility methods for Flight object which helps in getting the flight details from 
 * a CSV data record and also has methods which performs sanity check.
 * @author Deepen, Akshay
 *
 */
public class FlightManager {
	/**
	 * Method to get values for flight information
	 * @param dataRecord - Array of Strings of parsed csv record from csv.gz file
	 * @return Flight object containing all information from data record
	 * @throws IOException
	 */
	public static Flight getFlightDetails(String[] dataRecord) throws IOException {
		Flight flight = new Flight();
		// Maps all the required fields from the given data record
		flight.setUniqueCarrier(dataRecord[8]);
		try {
			flight.setAvgTicketPrice(Double.parseDouble(dataRecord[109]));
		}
		catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			// Skip that record
		}
		flight.setCrsArrTime(dataRecord[40]);
		flight.setCrsDepTime(dataRecord[29]);
		flight.setCrsElapsedTime(dataRecord[50]);
		flight.setOriginAirportId(dataRecord[11]);
		flight.setOriginAirportSeqId(dataRecord[12]);
		flight.setOriginCityMarketId(dataRecord[13]);
		flight.setOriginStateFips(dataRecord[17]);
		flight.setOriginWac(dataRecord[19]);
		flight.setDestAirportId(dataRecord[20]);
		flight.setDestAiportSeqId(dataRecord[21]);
		flight.setDestCityMarketId(dataRecord[22]);
		flight.setDestStateFips(dataRecord[26]);
		flight.setDestWac(dataRecord[28]);
		flight.setOrigin(dataRecord[14]);
		flight.setOriginCityName(dataRecord[15]);
		flight.setOriginState(dataRecord[16]);
		flight.setOriinStateName(dataRecord[18]);
		flight.setDest(dataRecord[23]);
		flight.setDestCityName(dataRecord[24]);
		flight.setDestState(dataRecord[25]);
		flight.setDestStateName(dataRecord[27]);
		flight.setCancelled(dataRecord[47]);
		flight.setArrTime(dataRecord[41]);
		flight.setDepTime(dataRecord[30]);
		flight.setActualElapsedTime(dataRecord[51]);
		flight.setArrDelay(dataRecord[42]);
		flight.setArrDelayMinutes(dataRecord[43]);
		flight.setArrDelay15(dataRecord[44]);
		flight.setMonth(dataRecord[2]);
		flight.setYear(dataRecord[0]);
		flight.setQuater(dataRecord[1]);
		flight.setFlightDate(dataRecord[5]);
		flight.setDayOfMonth(dataRecord[3]);
		flight.setDayOfWeek(dataRecord[4]);
		flight.setAirlineID(dataRecord[7]);
		flight.setFlightNumber(dataRecord[10]);
		flight.setDistanceGroup(dataRecord[55]);
		return flight;
	}

	/**
	 *
	 * @param flight Flight object
	 * @return true if CRS arrival and departure time are valid
	 */
	public static boolean checkCrsArrDepTime(Flight flight) {
		return (Integer.parseInt(flight.getCrsArrTime()) > 0
				&& Integer.parseInt(flight.getCrsDepTime()) > 0);
	}

	/**
	 *
	 * @param flight Flight object
	 * @return true if CRS Time zone is valid
	 */
	public static boolean checkCRSTimeZone(Flight flight) {
		Integer cactualElapsedTime = Integer.parseInt(flight.getCrsElapsedTime());
		Integer ctimezone = (int) (Flight.getDifferenceMinutes(flight.getCrsArrTime(), flight.getCrsDepTime())
				- cactualElapsedTime );
		Integer t = ctimezone % 60;
		return (t ==0);
	}

	/**
	 *
	 * @param flight Flight object
	 * @return true if Origin and Destination airport ID, CityMarketID, StateFips and WAC are positive.
	 */
	public static boolean checkOriginAndDestId(Flight flight) {
		return (Integer.parseInt(flight.getOriginAirportId()) > 0
				&& Integer.parseInt(flight.getOriginAirportSeqId()) > 0
				&& Integer.parseInt(flight.getOriginCityMarketId()) > 0
				&& Integer.parseInt(flight.getOriginStateFips()) > 0
				&& Integer.parseInt(flight.getOriginWac()) > 0);
	}

	/**
	 *
	 * @param flight Flight object
	 * @return true if the Origin and Destination city names are not empty
	 */
	public static boolean checkOriginAndDestSt(Flight flight) {
		return (!flight.getOriginState().equals("")
				&& !flight.getOrigin().equals("")
				&& !flight.getDest().equals("")
				&& !flight.getOriginCityName().equals(""));
	}

	/**
	 *
	 * @param flight Flight object
	 * @return true the flight is cancelled
	 */
	public static boolean checkCancelled(Flight flight) {
		return (Integer.parseInt(flight.getCancelled()) == 0) ;
	}

	/**
	 *
	 * @param flight Flight object
	 * @return true if the time zone entered is valid
	 */
	public static boolean checkTimeZone(Flight flight) {
		Integer cactualElapsedTime = Integer.parseInt(flight.getCrsElapsedTime());
		Integer ctimezone = (int) (Flight.getDifferenceMinutes(flight.getCrsArrTime(), flight.getCrsDepTime())
				- cactualElapsedTime );
		Integer actualElapsedTime = Integer.parseInt(flight.getActualElapsedTime());
		Integer tempVal = (int) (Flight.getDifferenceMinutes(flight.getArrTime(), flight.getDepTime())
				- actualElapsedTime - ctimezone);
		return (tempVal == 0);
	}

	/**
	 *
	 * @param flight Flight object
	 * @return true if the arrival delay timings are valid
	 */
	public static boolean checkArrDelay(Flight flight) {
		if (Double.parseDouble(flight.getArrDelay()) > 0.0
				&& Double.valueOf(flight.getArrDelay()).equals(Double.valueOf(flight.getArrDelayMinutes())) ) {
			if (Double.parseDouble(flight.getArrDelayMinutes()) >= 15.0
					&& Double.valueOf(flight.getArrDelay15()) == 1) {
				return true;
			}
		}
		else if (Double.parseDouble(flight.getArrDelay()) < 0.0
				&& Double.valueOf(flight.getArrDelayMinutes()).equals(0.0)) {
			return true;
		}
		return true;
	}


	/**
	 * For checking whether given flight is sane or not ?
	 *	CRSArrTime and CRSDepTime should not be zero
	 *	timeZone = CRSArrTime - CRSDepTime - CRSElapsedTime;
	 *	timeZone % 60 should be 0
	 *	AirportID,  AirportSeqID, CityMarketID, StateFips, Wac should be larger than 0
	 *	Origin, Destination,  CityName, State, StateName should not be empty
	 *	For flights that not Cancelled:
	 *	ArrTime -  DepTime - ActualElapsedTime - timeZone should be zero
	 *	if ArrDelay > 0 then ArrDelay should equal to ArrDelayMinutes
	 *	if ArrDelay < 0 then ArrDelayMinutes should be zero
	 *	if ArrDelayMinutes >= 15 then ArrDel15 should be true
	 * @param flight - Flight object to check for sanity
	 * @return
	 */
	public static boolean isSane(Flight flight) {
		try {
			return checkCrsArrDepTime(flight) && checkCRSTimeZone(flight)
					&& checkOriginAndDestId(flight) && checkOriginAndDestSt(flight)
					&& (checkCancelled(flight) ? checkTimeZone(flight) && checkArrDelay(flight) : true);
		}
		catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean isSane(Flight flight, boolean isTest) {		
		try {
			if(isTest) {
				return checkCrsArrDepTime(flight) && checkCRSTimeZone(flight)
						&& checkOriginAndDestId(flight) && checkOriginAndDestSt(flight);		
			} else {
				return isSane(flight);
			}				
		}
		catch (NumberFormatException e) {
			return false;
		}
		
	}
}
