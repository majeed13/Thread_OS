import java.util.Vector;

/* * * * * * * * * * * * * * * * * * * * * *
 * FILENAME: QueueNode.java
 * 
 * This class is written to be used by SyncQueue class to keep track of 
 * child threads for parent threads, put parent threads to sleep and notify
 * parent threads when an child is finished executing all in a synchronized
 * manner. 
 * 
 * By: Mustafa Majeed
 * 
 * Date: 5/09/19
 * 
 * CHANGES:
 * 
 */
public class QueueNode {
	
	private Vector<Integer> children; // used to track tids of children
	
	/* * * * * * QueueNode * * * * *
	 * Default no arg constructor that creates a new Integer Vector
	 */
	public QueueNode() {
		children = new Vector<Integer>();
	}
	
	/* * * * * * synchronized sleep * * * * *
	 * this method will put the calling method to sleep via the wait call in java
	 * and return the tid of any child thread that has finished executing.
	 */
	public synchronized int sleep() {
		try {
			wait();
		} catch (InterruptedException e) {
			SysLib.cout(e.getMessage());
		}
		return children.remove(0);
	}
	
	/* * * * * * synchronized wakeup * * * * *
	 * this method will add a child tid to the children vector and notify
	 * a waiting parent that it has finished executing.
	 */
	public synchronized void wakeup(int child) {
		children.add(child);
		notify();
	}
}
