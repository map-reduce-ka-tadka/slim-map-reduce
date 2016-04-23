package com.examples.meanFlight;
/**
 *
 * @author Deepen, Akshay
 * Flight is class which have all required information for a Flight from dataset
 */

public class Flight {
	private String uniqueCarrier ;
	private String airlineID;
	private Double avgTicketPrice;
	private String originAirportId;
	private String originAirportSeqId;
	private String originCityMarketId;
	private String originStateFips;
	private String originWac;
	private String originState;
	private String oriinStateName;
	private String originCityName;
	private String destAirportId;
	private String destAiportSeqId;
	private String destCityMarketId;
	private String destStateFips;
	private String destWac;
	private String destState ;
	private String destStateName;
	private String destCityName ;
	private String crsDepTime ;
	private String crsArrTime;
	private String crsElapsedTime;
	private String arrTime;
	private String arrDelay;
	private String arrDelay15;
	private String arrDelayMinutes;
	private String depTime;
	private String actualElapsedTime;
	private String origin, dest ;
	private String cancelled;
	private String month,year,flightDate;
	private Integer intArrivalTime, intDeptTime;
	private Integer intCrsArrivalTime, intCrsDeptTime;
	private Integer day;
	private String quater;
	private String dayOfMonth;
	private String dayOfWeek;
	private String flightNumber;
	private String distanceGroup;


	public Flight() {}

	
	public Flight(String uniqueCarrier, Double avgTicketPrice, String year) {
		this.uniqueCarrier = uniqueCarrier;
		this.avgTicketPrice = avgTicketPrice;
		this.year = year;
	}
	
	
	
	public String getFlightNumber() {
		return flightNumber;
	}


	public void setFlightNumber(String flightNumber) {
		this.flightNumber = flightNumber;
	}


	public String getAirlineID() {
		return airlineID;
	}

	public void setAirlineID(String airlineID) {
		this.airlineID = airlineID;
	}

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public String getDayOfMonth() {
		return dayOfMonth;
	}

	public void setDayOfMonth(String dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}

	public String getQuater() {
		return quater;
	}

	public void setQuater(String quater) {
		this.quater = quater;
	}

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}

	public Integer getIntCrsArrivalTime() {
		return intCrsArrivalTime;
	}

	public void setIntCrsArrivalTime(Integer intCrsArrivalTime) {
		this.intCrsArrivalTime = intCrsArrivalTime;
	}

	public Integer getIntCrsDeptTime() {
		return intCrsDeptTime;
	}

	public void setIntCrsDeptTime(Integer intCrsDeptTime) {
		this.intCrsDeptTime = intCrsDeptTime;
	}

	public Integer getIntArrivalTime() {
		return intArrivalTime;
	}

	public void setIntArrivalTime(Integer intArrivalTime) {
		this.intArrivalTime = intArrivalTime;
	}

	public Integer getIntDeptTime() {
		return intDeptTime;
	}

	public void setIntDeptTime(Integer intDeptTime) {
		this.intDeptTime = intDeptTime;
	}

	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getCancelled() {
		return cancelled;
	}
	public void setCancelled(String cancelled) {
		this.cancelled = cancelled;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public String getDest() {
		return dest;
	}
	public void setDest(String dest) {
		this.dest = dest;
	}
	public String getUniqueCarrier() {
		return uniqueCarrier;
	}
	public void setUniqueCarrier(String uniqueCarrier) {
		this.uniqueCarrier = uniqueCarrier;
	}
	public Double getAvgTicketPrice() {
		return avgTicketPrice;
	}
	public void setAvgTicketPrice(Double avgTicketPrice) {
		this.avgTicketPrice = avgTicketPrice;
	}
	public String getOriginAirportId() {
		return originAirportId;
	}
	public void setOriginAirportId(String originAirportId) {
		this.originAirportId = originAirportId;
	}
	public String getOriginAirportSeqId() {
		return originAirportSeqId;
	}
	public void setOriginAirportSeqId(String originAirportSeqId) {
		this.originAirportSeqId = originAirportSeqId;
	}
	public String getOriginCityMarketId() {
		return originCityMarketId;
	}
	public void setOriginCityMarketId(String originCityMarketId) {
		this.originCityMarketId = originCityMarketId;
	}
	public String getOriginStateFips() {
		return originStateFips;
	}
	public void setOriginStateFips(String originStateFips) {
		this.originStateFips = originStateFips;
	}
	public String getOriginWac() {
		return originWac;
	}
	public void setOriginWac(String originWac) {
		this.originWac = originWac;
	}
	public String getOriginState() {
		return originState;
	}
	public void setOriginState(String originState) {
		this.originState = originState;
	}
	public String getOriinStateName() {
		return oriinStateName;
	}
	public void setOriinStateName(String oriinStateName) {
		this.oriinStateName = oriinStateName;
	}
	public String getOriginCityName() {
		return originCityName;
	}
	public void setOriginCityName(String originCityName) {
		this.originCityName = originCityName;
	}
	public String getDestAirportId() {
		return destAirportId;
	}
	public void setDestAirportId(String destAirportId) {
		this.destAirportId = destAirportId;
	}
	public String getDestAiportSeqId() {
		return destAiportSeqId;
	}
	public void setDestAiportSeqId(String destAiportSeqId) {
		this.destAiportSeqId = destAiportSeqId;
	}
	public String getDestCityMarketId() {
		return destCityMarketId;
	}
	public void setDestCityMarketId(String destCityMarketId) {
		this.destCityMarketId = destCityMarketId;
	}
	public String getDestStateFips() {
		return destStateFips;
	}
	public void setDestStateFips(String destStateFips) {
		this.destStateFips = destStateFips;
	}
	public String getDestWac() {
		return destWac;
	}
	public void setDestWac(String destWac) {
		this.destWac = destWac;
	}
	public String getDestState() {
		return destState;
	}
	public void setDestState(String destState) {
		this.destState = destState;
	}
	public String getDestStateName() {
		return destStateName;
	}
	public void setDestStateName(String destStateName) {
		this.destStateName = destStateName;
	}
	public String getDestCityName() {
		return destCityName;
	}
	public void setDestCityName(String destCityName) {
		this.destCityName = destCityName;
	}
	public String getCrsDepTime() {
		return crsDepTime;
	}
	public void setCrsDepTime(String crsDepTime) {
		this.crsDepTime = crsDepTime;
	}
	public String getCrsArrTime() {
		return crsArrTime;
	}
	public void setCrsArrTime(String crsArrTime) {
		this.crsArrTime = crsArrTime;
	}
	public String getCrsElapsedTime() {
		return crsElapsedTime;
	}
	public void setCrsElapsedTime(String crsElapsedTime) {
		this.crsElapsedTime = crsElapsedTime;
	}
	public String getArrTime() {
		return arrTime;
	}
	public void setArrTime(String arrTime) {
		this.arrTime = arrTime;
	}
	public String getArrDelay() {
		return arrDelay;
	}
	public void setArrDelay(String arrDelay) {
		this.arrDelay = arrDelay;
	}
	public String getArrDelay15() {
		return arrDelay15;
	}
	public void setArrDelay15(String arrDelay15) {
		this.arrDelay15 = arrDelay15;
	}
	public String getArrDelayMinutes() {
		return arrDelayMinutes;
	}
	public void setArrDelayMinutes(String arrDelayMinutes) {
		this.arrDelayMinutes = arrDelayMinutes;
	}
	public String getDepTime() {
		return depTime;
	}
	public void setDepTime(String depTime) {
		this.depTime = depTime;
	}
	public String getActualElapsedTime() {
		return actualElapsedTime;
	}

	public void setActualElapsedTime(String actualElapsedTime) {
		this.actualElapsedTime = actualElapsedTime;
	}

	public String getFlightDate() {
		return flightDate;
	}
	public void setFlightDate(String flightDate) {
		this.flightDate = flightDate;
	}

	public String getDistanceGroup() {
		return distanceGroup;
	}
	public void setDistanceGroup(String distanceGroup) {
		this.distanceGroup = distanceGroup;
	}

	@Override
	public String toString() {
		return "Flight [uniqueCarrier=" + uniqueCarrier + ", avgTicketPrice=" + avgTicketPrice + ", originAirportId="
				+ originAirportId + ", originAirportSeqId=" + originAirportSeqId + ", originCityMarketId="
				+ originCityMarketId + ", originStateFips=" + originStateFips + ", originWac=" + originWac
				+ ", originState=" + originState + ", oriinStateName=" + oriinStateName + ", originCityName="
				+ originCityName + ", destAirportId=" + destAirportId + ", destAiportSeqId=" + destAiportSeqId
				+ ", destCityMarketId=" + destCityMarketId + ", destStateFips=" + destStateFips + ", destWac=" + destWac
				+ ", destState=" + destState + ", destStateName=" + destStateName + ", destCityName=" + destCityName
				+ ", crsDepTime=" + crsDepTime + ", crsArrTime=" + crsArrTime + ", crsElapsedTime=" + crsElapsedTime
				+ ", arrTime=" + arrTime + ", arrDelay=" + arrDelay + ", arrDelay15=" + arrDelay15
				+ ", arrDelayMinutes=" + arrDelayMinutes + ", depTime=" + depTime + ", actualElapsedTime="
				+ actualElapsedTime + ", origin=" + origin + ", dest=" + dest + ", cancelled=" + cancelled + ", distanceGroup=" + distanceGroup + "]";
	}

	/**
	 * Method to get time difference between arrival time and departure time in  minutes
	 * @param arrival
	 * @param departure
	 * @return
	 */
	public static int getDifferenceMinutes(String arrival, String departure) {
		Integer aT = Integer.parseInt(arrival);
		Integer dT = Integer.parseInt(departure);
		Integer aTH = aT / 100; // Arrival Hour
		Integer dTH = dT / 100; // Departure Hour
		Integer aTM = aT % 100; // Arrival Minute
		Integer dTM = dT % 100; // Departure Minute
		Integer H = 0; // Hour Difference
		if (aTH > dTH) {
			H = aTH - dTH;
		} else if (aTH == dTH) {
			if (aTM > dTM) {
				H = aTH - dTH;
			}
			else {
				H = (24 - dTH) + aTH;
			}
		}
		else if (aTH < dTH) {
			H = (24 - dTH) + aTH;
		}
		Integer min = (int) (((H * 60) + (aTM - dTM)));
		return min;
	}
}
