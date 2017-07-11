package de.bsvrz.sys.startstopp.process;

public class TestInkarnation {

	public static void main(String[] args) {
		for(int i = 0; i<10; i++) {
			System.out.println("Test: " + i);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
