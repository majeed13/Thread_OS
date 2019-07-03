import java.util.Date;

/* * * * * * * * * * * * * * * * * * * * * *
 * FILENAME: Test3.java
 * 
 * This class is written to test ThreadOS kernel class that is written to
 * use java synchronized methods wait() and notify() on threads that
 * spawn computation and disk IO children threads.
 * 
 * By: Mustafa Majeed
 * 
 * Date: 5/09/19
 * 
 * Changes:
 * 
 */
public class Test3 extends Thread{
	private int numPair; // number of pairs of threads to launch
	
	/* * * * * * Test3 * * * * * *
	 * Constructor for Test3 class that takes in a String[] array
	 * as parameter and then sets numPair to the 1st index of the 
	 * passed in String[] array
	 */
	public Test3(String[] args) {
		numPair = Integer.parseInt(args[0]);
	}
	
	public void run() {

		long startTime = new Date().getTime(); // record start time of this class call
		
		// create arguments to pass to SysLib.exec
		String[] computation = SysLib.stringToArgs("TestThread3a Computation");
		String[] disk = SysLib.stringToArgs("TestThread3a Disk");

		// launch requestd pairs
		for(int i = 0; i < numPair; i++) {
			SysLib.exec(computation);
			SysLib.exec(disk);
		}

		// wait for all children to finish
		int numPair2 = numPair * 2;
		for(int i = 0; i < numPair2; i++) {
			SysLib.join();
		}
		
		long endTime = new Date().getTime(); // record end time of execution
		SysLib.cout("elapsed time = " + (endTime - startTime) + " msec\n");
		SysLib.exit();
	}
}
