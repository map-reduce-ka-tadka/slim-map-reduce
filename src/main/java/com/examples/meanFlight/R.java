package com.examples.meanFlight;
import com.main.Context;
import com.reduce.Reducer;

public class R extends Reducer {

	@Override
	public void reduce(String key, Iterable<String> values, Context context) {
	
		try {
		int count = 0;
		Double sum = 0d;
		for (String val : values) {
			sum+=Double.parseDouble(val);
			count++;
		}
		Double mean = (Double) sum / count ;
		context.write(key, mean.toString());
		
		} catch (Exception e) {
			
		}
				
	}

}
