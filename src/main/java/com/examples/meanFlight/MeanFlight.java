package com.examples.meanFlight;
import java.io.IOException;

import com.main.Context;
/**
 * Example Implementation of slim Map Reduce
 * Generate Mean Average Ticket Price by Carrier
 * @author Deepen Mehta
 *
 */
public class MeanFlight {
	public static void main(String[] args) throws NumberFormatException, IOException {
		Context context = new Context();
		context.setMapperClass(M.class);
		context.setReducerClass(R.class);
		context.setInputPath(args[0]);
		context.setOutputPath(args[1]);
		context.jobWaitCompletionTrue();
	}
}
