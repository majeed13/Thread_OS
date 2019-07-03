import java.util.*;

public class Scheduler extends Thread
{
    private Vector queue;
    private Vector queue1;
    private Vector queue2;
    private int counter1;
    private int counter2;
    private int timeSlice;
    private static final int DEFAULT_TIME_SLICE = 500;

    // New data added to p161 
    private boolean[] tids; // Indicate which ids have been used
    private static final int DEFAULT_MAX_THREADS = 10000;

    // A new feature added to p161 
    // Allocate an ID array, each element indicating if that id has been used
    private int nextId = 0;
    private void initTid( int maxThreads ) {
	tids = new boolean[maxThreads];
	for ( int i = 0; i < maxThreads; i++ )
	    tids[i] = false;
    }

    // A new feature added to p161 
    // Search an available thread ID and provide a new thread with this ID
    private int getNewTid( ) {
	for ( int i = 0; i < tids.length; i++ ) {
	    int tentative = ( nextId + i ) % tids.length;
	    if ( tids[tentative] == false ) {
		tids[tentative] = true;
		nextId = ( tentative + 1 ) % tids.length;
		return tentative;
	    }
	}
	return -1;
    }

    // A new feature added to p161 
    // Return the thread ID and set the corresponding tids element to be unused
    private boolean returnTid( int tid ) {
	if ( tid >= 0 && tid < tids.length && tids[tid] == true ) {
	    tids[tid] = false;
	    return true;
	}
	return false;
    }

    // A new feature added to p161 
    // Retrieve the current thread's TCB from the queue
    /*
    * * * * * * ADDITION FOR MFQS * * * * *
    * changed getMyTcb to search all non empty queues to look
    * for and return the current TCB - MUSTAFA MAJEED
    */
    public TCB getMyTcb( ) {
	Thread myThread = Thread.currentThread( ); // Get my thread object
	//queue0
	if (queue.size() != 0) {
	synchronized( queue ) {
	    for ( int i = 0; i < queue.size( ); i++ ) {
		TCB tcb = ( TCB )queue.elementAt( i );
		Thread thread = tcb.getThread( );
		if ( thread == myThread ) // if this is my TCB, return it
		    return tcb;
	    }
	}
	}
	//queue1
	if (queue1.size() != 0) {
		synchronized( queue1 ) {
	    for ( int i = 0; i < queue1.size( ); i++ ) {
		TCB tcb = ( TCB )queue1.elementAt( i );
		Thread thread = tcb.getThread( );
		if ( thread == myThread ) // if this is my TCB, return it
		    return tcb;
	    }
		}
	}
	//queue2
	if (queue2.size() != 0) {
		synchronized( queue2 ) {
	    for ( int i = 0; i < queue2.size( ); i++ ) {
		TCB tcb = ( TCB )queue2.elementAt( i );
		Thread thread = tcb.getThread( );
		if ( thread == myThread ) // if this is my TCB, return it
		    return tcb;
	    }
		}
	}
	return null;
    }

    // A new feature added to p161 
    // Return the maximal number of threads to be spawned in the system
    public int getMaxThreads( ) {
	return tids.length;
    }

    public Scheduler( ) {
	timeSlice = DEFAULT_TIME_SLICE;
	// added for MFQS
	counter1 = 0;
	counter2 = 0;
	queue1 = new Vector();
	queue2 = new Vector();
	// end of addition
	queue = new Vector( );

	initTid( DEFAULT_MAX_THREADS );
    }

    public Scheduler( int quantum ) {
	timeSlice = quantum;
	// added for MFQS
	counter1 = 0;
	counter2 = 0;
	queue1 = new Vector();
	queue2 = new Vector();
	// end of addition
	queue = new Vector( );
	initTid( DEFAULT_MAX_THREADS );
    }

    // A new feature added to p161 
    // A constructor to receive the max number of threads to be spawned
    public Scheduler( int quantum, int maxThreads ) {
	timeSlice = quantum;
	// added for MFQS
	counter1 = 0;
	counter2 = 0;
	queue1 = new Vector();
	queue2 = new Vector();
	// end of addition
	queue = new Vector( );
	initTid( maxThreads );
    }

    private void schedulerSleep( ) {
	try {
	    Thread.sleep( timeSlice );
	} catch ( InterruptedException e ) {
	}
    }

    // A modified addThread of p161 example
    public TCB addThread( Thread t ) {
	//t.setPriority( 2 );
	TCB parentTcb = getMyTcb( ); // get my TCB and find my TID
	int pid = ( parentTcb != null ) ? parentTcb.getTid( ) : -1;
	int tid = getNewTid( ); // get a new TID
	if ( tid == -1)
	    return null;
	TCB tcb = new TCB( t, tid, pid ); // create a new TCB
	queue.add( tcb );
	return tcb;
    }

    // A new feature added to p161
    // Removing the TCB of a terminating thread
    public boolean deleteThread( ) {
	TCB tcb = getMyTcb( ); 
	if ( tcb!= null )
	    return tcb.setTerminated( );
	else
	    return false;
    }

    public void sleepThread( int milliseconds ) {
	try {
	    sleep( milliseconds );
	} catch ( InterruptedException e ) { }
    }
    
    // A modified run of p161
    /*
    * * * * * * MODIFIED FOR MFQS * * * * * 
    * changed run to start threads in q0, suspend and transfer to q1 if still
    * alive after q0 quantum.
    * Suspend and transfer to q2 if thread still alive after q1 time quantum.
    * Continue to run in a RR fashion in q2 with q2 time quantum for the remainder
    * of the threads life.
    */
    public void run( ) {
	
		while ( true ) {
	   		try {
				// get the next TCB and its thrad
		    	if ( queue.size( ) == 0 ) {
		    		if ( queue1.size() == 0) {
		    			if ( queue2.size() == 0) {
		    				continue;
		    				// all queues are empty
		    			}
		    			else {
		    				TCB currentTCB = (TCB)queue2.firstElement( );
		    				if ( currentTCB.getTerminated( ) == true ) {
		        				queue2.remove( currentTCB );
		        				returnTid( currentTCB.getTid( ) );
		        				continue;
		    				}
		    				
		    				queue2sim();

		    				//debug
		    				
		    				/*if (counter2 != 0) {
		    				TCB cTCB = (TCB)queue2.firstElement( );
		    				boolean t = cTCB.getTerminated();
		    				System.out.println( "* * * TERMINATED of tid=" + cTCB.getTid() + " = " + t + " * * *");
		    				}*/
		    			}
		    		}
		    		else {
		    			TCB currentTCB = (TCB)queue1.firstElement( );
		    			if ( currentTCB.getTerminated( ) == true ) {
		        			queue1.remove( currentTCB );
		        			returnTid( currentTCB.getTid( ) );
		        			continue;
		    			}
		    			queue1sim();

		    			//debug
		    	
		    			/*if (counter1 != 0) {
		    			TCB cTCB = (TCB)queue1.firstElement( );
		    			boolean t = cTCB.getTerminated();
		    			System.out.println( "* * * TERMINATED of tid=" + cTCB.getTid() + " = " + t + " * * *");
		    			}*/
		    		}
		    	}
		    	else {

		    		TCB currentTCB = (TCB)queue.firstElement( );
		    		if ( currentTCB.getTerminated( ) == true ) {
		        		queue.remove( currentTCB );
		        		returnTid( currentTCB.getTid( ) );
		        		continue;
		    		}
		    		queue0sim();
				}
	    	} catch ( NullPointerException e3 ) { };
		}
    }

    // this method is used to execute threads in queue0
    private void queue0sim() {
    	TCB currentTCB = (TCB)queue.firstElement( );
		Thread current = currentTCB.getThread( );
		if ( current != null ) {
			if ( current.isAlive( ) ) {
				// might not need this check since all threads in queue0 will need
				// to be started
		        current.resume();
		    }
		    else {
			   	current.start( ); 
		    }
		}
		
		schedulerSleep( );

		synchronized ( queue ) {
			if ( current != null && current.isAlive( ) ) {
				current.suspend();
			}
		   	queue.remove( currentTCB );
		    queue1.add( currentTCB );
		}
    }

    // this method is used to execute threads in queue1
    private void queue1sim() {
    	TCB currentTCB = (TCB)queue1.firstElement( );
		Thread current = currentTCB.getThread( );
    	if ( current != null ) {
			if ( current.isAlive( ) ) {
		        current.resume();
		    }
		}
		
		// for use to keep track of quantum for queue1
		counter1++;
		schedulerSleep( );
		
		// delete dead threads after execution
		if(!current.isAlive()) {
			deleteThread();
		}

		if (counter1 == 2 | !current.isAlive()) {
			synchronized ( queue1 ) {
				if ( current != null && current.isAlive( ) ) {
					current.suspend();
				}
				queue1.remove( currentTCB );
				queue2.add( currentTCB );
				// reset to use for next in line
				counter1 = 0;
			}
		}	
    }

    // this method is used to execute threads in queue2
    private void queue2sim() {
    	TCB currentTCB = (TCB)queue2.firstElement( );
		Thread current = currentTCB.getThread( );
    	if ( current != null ) {
			if ( current.isAlive( ) ) {
		        current.resume();
		    }
		}
		
		// for use to keep track of quantum for queue2
		counter2++;
		schedulerSleep( );

		// delete dead threads
		if(!current.isAlive()) {
			deleteThread();
		}

		if (counter2 == 4 | !current.isAlive()) {
			synchronized ( queue2 ) {
				if ( current != null && current.isAlive( ) ) {
					current.suspend();
				}
				queue2.remove( currentTCB );
				queue2.add( currentTCB );
				// reset to use for next in line
				counter2 = 0;
			}
		}
    }
}
