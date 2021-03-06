/*
 * Copyright 2018 data Artisans GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camilo.spendreport;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * POJO representing a taxi ride.
 *
 * All fields are public for ease of use in our example.
 */
public class TaxiRide {

	// id of the taxi
	public String medallion;
	// license of the driver
	public String licenseId;
	// time when passengers were picked up
	public long pickUpTime;
	// time when passengers were dropped off
	public long dropOffTime;
	// longitude where passengers were picked up
	public double pickUpLon;
	// latitude where passengers were picked up
	public double pickUpLat;
	// longitude where passengers were dropped off
	public double dropOffLon;
	// latitude where passengers were dropped off
	public double dropOffLat;
	// total amount paid by the passengers
	public double total;
	
	public String getLicenseId() {
		return licenseId;
	}
	
	private static transient DateTimeFormatter timeFormatter =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); //Z");

	public static TaxiRide fromString(String line) {

		String[] tokens = line.split(",");
		if (tokens.length != 17) {
			throw new RuntimeException("Invalid record: " + line);
		}

		TaxiRide ride = new TaxiRide();

		try {
			ride.medallion = tokens[0];
			ride.licenseId = tokens[1];
			ride.pickUpTime = LocalDateTime.parse(tokens[2], timeFormatter).toEpochSecond(ZoneOffset.UTC);
			ride.dropOffTime = LocalDateTime.parse(tokens[3], timeFormatter).toEpochSecond(ZoneOffset.UTC);
			ride.pickUpLon = tokens[6].length() > 0 ? Float.parseFloat(tokens[6]) : 0.0f;
			ride.pickUpLat = tokens[7].length() > 0 ? Float.parseFloat(tokens[7]) : 0.0f;
			ride.dropOffLon = tokens[8].length() > 0 ? Float.parseFloat(tokens[8]) : 0.0f;
			ride.dropOffLat = tokens[9].length() > 0 ? Float.parseFloat(tokens[9]) : 0.0f;
			ride.total = tokens[11].length() > 0 ? Float.parseFloat(tokens[11]) : 0.0f;
		} catch (NumberFormatException nfe) {
			throw new RuntimeException("Invalid field: " + line, nfe);
		}

		return ride;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(medallion, licenseId, pickUpTime, dropOffTime);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(medallion);
		sb.append(',');
		sb.append(licenseId);
		sb.append(',');
		sb.append(pickUpTime);
		sb.append(',');
		sb.append(dropOffTime);
		sb.append(',');
		sb.append(pickUpLon);
		sb.append(',');
		sb.append(pickUpLat);
		sb.append(',');
		sb.append(dropOffLon);
		sb.append(',');
		sb.append(dropOffLat);
		sb.append(',');
		sb.append(total);
		sb.append(']');
		return sb.toString();
	}
}