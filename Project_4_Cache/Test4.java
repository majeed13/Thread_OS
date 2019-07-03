import java.util.Random;
import java.util.Date;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
* FILE NAME : Test4.java
*
* This class is written to test the Cache.java class that was written by
* Mustafa Majeed. It is used to run tests using an enabled cache or disabled
* cache with random, local, mixed or adversary block accesses.
*
* By: Mustafa Majeed 
*
* Date: 5/24/2019
*
* CHANGES:
*
*
*/

public class Test4 extends Thread {
	private String cache; // state of the cache
	private int perTest;  // performance test to do
	private Random rand;  // used to get random numbers
	private long sTime;   // track starting time of test
	private long eTime;   // track ending time of test
	private byte[] bytesToWrite;
	private byte[] bytesRead;
	
	/* * * * * Test4 * * * * *
	* This is the only provided constructor for Test4. It accepts a String
	* array of arguments where index 0 can only be "-enabled" if the user
	* wishes to use Cache.java read and write methods. Otherwise, Cache.java
	* methods will not be used. index 1 will correspond to the appropriate test
	* that the user would like to run.
	*/
	public Test4(String[] args) {
		// this describes the state of cache
		cache = args[0];
		perTest = Integer.parseInt(args[1]);
		rand = new Random();
		bytesToWrite = new byte[512];
		bytesRead = new byte[512];
	}
	
	/* * * * * run * * * * *
	* This is the only public method in this class and it will perform the
	* user requested test in the specified condition of the cache.
	*/
	public void run() {
		SysLib.flush(); // make sure to have clean cache
		sTime = new Date().getTime();
		//default disabled cache
		boolean disabled = true;
		//check if user requested cache enabled
		if (cache.equalsIgnoreCase("-enabled"))
			disabled = false;
		// perform requested test and exit
		if (perTest == 1) {
			//run randomAccess
			rAccess(disabled);
			eTime = new Date().getTime();
			displayResult(1);
			SysLib.exit();
		}
		
		else if (perTest == 2) {
			//run localizedAccess
			localAccess(disabled);
			eTime = new Date().getTime();
			displayResult(2);
			SysLib.exit();		
		}
		
		else if (perTest == 3) {
			//run mixedAcces
			mixedAccess(disabled);
			eTime = new Date().getTime();
			displayResult(3);
			SysLib.exit();
		}
		
		else if (perTest == 4) {
			//run adversaryAccess
			adversary(disabled);
			eTime = new Date().getTime();
			displayResult(4);
			SysLib.exit();
		}
		
		else {
			//error
			SysLib.cout("* * Incorrect Test request, no test performed * *\n");
			SysLib.exit();
		}
	}
	
	/* * * * * rAccess * * * * *
	* This method is used to perform a random access test for reads and writes.
	* A randomly generated reference of block ids ranging from 0 to 999 will be
	* used to perform 400 randomly selected reads or writes. The passed in
	* boolean will determine if Cache.java methods are enabled or disabled for
	* this test.
	*/
	private void rAccess(boolean disabledCache) {
		int[] randBlock = new int[200];
		for (int i = 0; i < 200; i++) {
			randBlock[i] = rand.nextInt(1000);
		}
		for (int i = 0; i < 400; i++) {
			// randomly select a read or write access
			int readOrWrite = Math.abs(rand.nextInt()) % 2;
			if (readOrWrite == 0) {
				if (!disabledCache)
					SysLib.cwrite(randBlock[i % 200], bytesToWrite);
				else
					SysLib.rawwrite(randBlock[i % 200], bytesToWrite);
			}
			else {
				if (!disabledCache)
					SysLib.cread(randBlock[i % 200], bytesRead);
				else
					SysLib.rawread(randBlock[i % 200], bytesRead);
			}
		}
	}
	
	/* * * * * localAccess * * * * *
	* This method is used to perform a local access test for read and writes.
	* Reads and writes on small selection of blocks many times to get a high 
	* ratio of cache hits when enabled. The passed in
	* boolean will determine if Cache.java methods are enabled or disabled for
	* this test.
	*/
	private void localAccess(boolean disabledCache) {
		// will be used to make sure same 10 blockIds are used
		int modBlockNum = 100;
		// read or write
		int writeOrRead;
		// used to increment blockId
		int blockIds;
		for (int i = 0; i < 400; i++) {
			// READ OR WRITE?
			writeOrRead = Math.abs(rand.nextInt()) % 2;
			// blockIds should be in the following order:
			// 0, 10, 20, 30, 40, 50, 60, 70, 80, 90
			blockIds = (i * 10) % modBlockNum;
			if (writeOrRead == 0) {
				if (!disabledCache)
					SysLib.cwrite(blockIds, bytesToWrite);
				else
					SysLib.rawwrite(blockIds, bytesToWrite);
			}
			else {
				if (!disabledCache)
					SysLib.cread(blockIds, bytesRead);
				else
					SysLib.rawread(blockIds, bytesRead);
			}
		}
	}
	
	/* * * * * mixedAccess * * * * *
	* This method is used to perform a mixed access test for read and writes.
	* 
	* The passed in boolean will determine if Cache.java methods are enabled or
	* disabled for this test.
	*/
	private void mixedAccess(boolean disabledCache) {
		// ref "string" for blockIds
		int[] blockIds = new int[400];
		// will be used to check 90%local vs 10%random
		int mixedNum;
		// will be used to track block 0 - 800 incrementing by 100
		// for local access
		int local = 0;
		// will be used to determine a read or write at random
		int writeOrRead;
		for (int i = 0; i < 400; i++) {
			mixedNum = Math.abs(rand.nextInt() % 10);
			if(mixedNum > 8)
				blockIds[i] = Math.abs(rand.nextInt(1000));
			else 
				// local blockIds should be the following:
				// 0, 100, 200, 300, 400, 500, 600, 700, 800
				blockIds[i] = (local++ % 9) * 100;	
		}
		for (int i = 0; i < 400; i++) {
			writeOrRead = Math.abs(rand.nextInt()) % 2;
			if (writeOrRead == 0) {
				if (!disabledCache)
					SysLib.cwrite(blockIds[i], bytesToWrite);
				else
					SysLib.rawwrite(blockIds[i], bytesToWrite);
			}
			else {
				if (!disabledCache)
					SysLib.cread(blockIds[i], bytesRead);
				else
					SysLib.rawread(blockIds[i], bytesRead);
			}
		}
	}
	
	/* * * * * adversary * * * * *
	* Will run a test to generate disk accesses that do not make good use of 
	* the  cache at all. This is done by ensuring that each access will cause
	* a page fault.
	*/
	private void adversary(boolean disabledCache) {
		int writeOrRead;
		for (int i = 0; i < 400; i++) {
			writeOrRead = Math.abs(rand.nextInt()) % 2;
			if (writeOrRead == 0) {
				if (!disabledCache)
					SysLib.cwrite(i, bytesToWrite);
				else
					SysLib.rawwrite(i, bytesToWrite);
			}
			else {
				if (!disabledCache)
					SysLib.cread(i, bytesRead);
				else
					SysLib.rawread(i, bytesRead);
			}
		}
	}
	
	/* * * * * displayResult * * * * *
	* Will dispaly the name of the test that was performed as well as the
	* the total elapsed time in ms it took to complete. Then the avg time
	* for each access will be displayed as well. The passed in int signifies
	* what type of test was perfromed in order to display the correct message
	*/
	private void displayResult(int test) {
		String pTest;
		if (test == 1)
			pTest = "Random Access";
		else if (test == 2)
			pTest = "Localized Access";
		else if (test == 3)
			pTest = "Mixed Access";
		else
			pTest = "Adversary Access";
		
		SysLib.cout("Test performed: " + pTest + "(cache " + cache + ") = "
				+ (eTime - sTime) + "ms\n");
		SysLib.cout("Average time per access = " + ((double)(eTime - sTime)/(double)400) + "ms\n");
	}
}
