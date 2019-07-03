/* * * * * * * * * * * * * * * * * * * * * *
 * FILENAME: TestThread3a.java
 * 
 * This class is written to be used by Test3.class and launch
 * a thread that does random computation on the CPU or a disk 
 * IO and report when finished.
 * 
 * By: Mustafa Majeed
 * 
 * Date: 5/09/19
 * 
 * CHANGES:
 * 
 */
public class TestThread3a extends Thread {
	
	private final int RANDOMCONST = 237412; // random number used to do computation
	private String type; // string to keep track of passed in type
	
	/* * * * * * TestThread3a * * * * * *
	 * Constructor for TestThread3a class that takes in a String[] array
	 * as parameter and then sets type to the 1st index of the 
	 * passed in String[] array
	 */
	public TestThread3a(String[] args) {
		type = args[0];
	}
	
	public void run() {
		
		int garb = 1; // garbage int used for comp
		
		if (type.equalsIgnoreCase("Computation")) {
			for(int i = 1; i < RANDOMCONST; i++) {
				garb *= i;
			}
		}
		
		else if (type.equalsIgnoreCase("Disk")) {
			byte[] bArray = new byte[512];
			for(int i = 0; i < 1000; i++) {
				SysLib.rawread(i, bArray);
			}
		}
		
		SysLib.cout(type + " finished...\n");
		SysLib.exit();
	}
}
