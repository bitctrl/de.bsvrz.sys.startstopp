package de.bsvrz.sys.startstopp.process.sample;

public class Sample {

	public static void main(String[] args) throws InterruptedException {
		
		for( int idx = 0; idx < 10000; idx++) {
			System.err.println("Aufruf: " + idx);
		}
	}

}
