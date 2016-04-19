/*
 * @author Mehta, Deepen  
 * @created Apr 10, 2016 
 */

package com.main;

import org.apache.commons.lang3.RandomStringUtils;

import com.net.SortClient;
import com.net.SortServer;

public class Configuration {

	public static void main(String[] args) throws InterruptedException {

		if(args.length < 5) {
			System.out.println("Incomplete Arguments\n Terminating.");
			return;
		}
		ConfigParams.setParams(args);
		if(args[0].equalsIgnoreCase("server")) {
			// Dynamically creating log files for Server
			System.setProperty("logfile.name","logs/server_"+ RandomStringUtils.randomAlphanumeric(10) + ".log");
			SortServer.start();
		} else if (args[0].equalsIgnoreCase("client")) {
			// Dynamically creating log files for Client
			System.setProperty("logfile.name","logs/client_" + RandomStringUtils.randomAlphanumeric(10) + ".log");
			SortClient.start();
		}

	}

}
