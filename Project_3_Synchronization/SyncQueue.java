/* * * * * * * * * * * * * * * * * * * * * *
 * FILENAME: SyncQueue.java
 * 
 * This class is written to imitate a synchronized queue of parent threads
 * used when they are waiting for a child thread to finish executing.
 * 
 * By: Mustafa Majeed
 * 
 * Date: 5/09/19
 * 
 * CHANGES:
 * 
 */
public class SyncQueue {

	private QueueNode[] queue; // queue to track parent threads
	
	/* * * * * * SyncQueue * * * * *
	 * Constructor for SyncQueue that takes no arguments and sets the size
	 * of the queue to 10 and initiates all QueueNodes to be new QueueNode objects
	 */
	public SyncQueue() {
		queue = new QueueNode[10];
		initQueue(10);
	}
	
	/* * * * * * SyncQueue * * * * *
	 * Constructor that takes an int parameter and sets the size of the queue
	 * to that value and initiates all the QueueNodes to be new QueueNode objects
	 */
	public SyncQueue(int condMax) {
		queue = new QueueNode[condMax];
		initQueue(condMax);
	}
	
	/* * * * * * enqueueAndSleep * * * * * 
	 * This method will enqueue the calling thread into the QueueNode array
	 * and put it to sleep (by wait() in java) until any child notifies the
	 * parent that they are finished executing.
	 * This method will return the tid of any child that has finished executing.
	 */
	//makes current thread wait unil child terminates... returns TID of a child
	//think blockMe(myTID)
	public int enqueueAndSleep(int condition) {
		// check that the passed in condition fits in this queue
		if ((condition >= 0) && (condition < queue.length)) {
			return queue[condition].sleep();
		}
		// error return 
		return -1;
	}
	
	/* * * * * * dequeueAndWakeup * * * * *
	 * This method will wakeup the parent thread of an child that has finished
	 * executing who had a parent call SysLib.join().
	 * This is done by calling QueueNodes wakeup() method which will notify 
	 * the waiting thread in a synchronized manner. (only 1 thread at a time)
	 */
	public void dequeueAndWakeup(int condition) {
		dequeueAndWakeup(condition,0);
	}
	/* * * * * * dequeueAndWakeup * * * * *
	 * This method will wakeup the parent thread of an child that has finished
	 * executing who had a parent call SysLib.join().
	 * This is done by calling QueueNodes wakeup() method which will notify 
	 * the waiting thread in a synchronized manner. (only 1 thread at a time)
	 */
	//think unblockHim(hisTid, myTID)
	public void dequeueAndWakeup(int condition, int tid) {
		if ((condition >= 0) && (condition < queue.length))
			queue[condition].wakeup(tid);
	}
	
	/* * * * * * initQueue * * * * *
	 * This method will initiate all index values of queue to be new QueueNode
	 * objects up to the int size passed in
	 */
	private void initQueue(int max) {
		for (int i = 0; i < max; i++) {
			queue[i] = new QueueNode();
		}
	}
	
}
