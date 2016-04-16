/*
 * @author Mehta, Deepen  
 * @created Apr 10, 2016 
 */

package com.main;

import com.net.SortClient;
import com.net.SortServer;

public class Configuration {

	public static void main(String[] args) {

		if(args.length < 5) {
			System.out.println("Incomplete Arguments\n Terminating.");
			return;
		}
		ConfigParams.setParams(args);
		if(args[0].equalsIgnoreCase("server")) {
			SortServer.start();
		} else if (args[0].equalsIgnoreCase("client")) {
			SortClient.start();
		}

	}

}
