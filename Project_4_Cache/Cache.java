import java.util.Vector;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
* FILE NAME : Cache.java
*
* This class is written to simulate a cache table and cache blocks in ThreadOS.
* The cache will store accessed DISK blocks in cache memory for quick reaccess
* if needed rather than going all the way out to DISK each time a read or write
* is called by a program.
*
* By: Mustafa Majeed 
*
* Date: 5/24/2019
*
* CHANGES:
*
*
*/
public class Cache {

	// the data for each cacheEntry stored here
	private Vector cachePage; 
	// used for FIFO to track longest resident in cache
	private int time;
	// size of each block in bytes         
	private int blockSize;
	
	/* * * * * PRIVATE ENTRY CLASS * * * * * *
	* This is a private class that will be used to track blockId, reference bit
	* dirty bit and time stamp of each entry in the cache block.
	*/
	private class Entry {
		public int blockNum;
		public boolean ref;
		public boolean dirty;
		public int tStamp;
		
		// No Arg constructor to initialize all values of Entry
		public Entry() {
			blockNum = -1;
			ref = false;
			dirty = false;
			tStamp = 0;
		}
	}
	
	// This array will be used to store cache block info. Each Entry will have
	// the information of the matching index cache block for this cache
	private Entry[] cEntries;
	
	/* * * * * Cache * * * * *
	* This is the only provided constructor for Cache.java. Requires 2 int
	* values. First will be used for the size of each cach block in bytes,
	* second will be used to initiate how many blocks this cache will have.
	*/
	public Cache(int blkSize, int cacheBlocks) {
		time = 0;
		blockSize = blkSize;
		cachePage = new Vector();
		cEntries = new Entry[cacheBlocks];
		for(int i = 0; i < cacheBlocks; i++) {
			byte[] bArray =new byte[blkSize];
			cachePage.addElement(bArray);
			cEntries[i] = new Entry();
		}
	}
	
	/* * * * * * read * * * * * *
	* This method is used to read a requested blockId into the passed in byte[]
	* using the bytes stored in cachePage Vector. DISK reads will only be
	* performed if the requested blockId is not in cachePage Vector. If the
	* cache is full, and the requested blockId is not found, a victim will be
	* selected for eviction using FIFO + Enhanced 2nd Change Algorithm. DISK
	* writes will also only occur if the dirty bit of the evicted victim is set
	* to true.
	*/
	public synchronized boolean read(int blockId, byte[] buf) {
		if (blockId < 0) {
			SysLib.cout("* * Error with blockId, cannot be below 0 in disk!! * *\n");
			return false;
		}
		// array of bytes 
		byte[] copyArray;
		// will be used to find the index in cache of the requested block
		int p;
		p = findInCEntries(blockId);
		
		if (p != -1) {
			// blockId is FOUND!
			copyArray = (byte[]) cachePage.elementAt(p);
			System.arraycopy(copyArray, 0, buf, 0, blockSize);
			cEntries[p].ref = true;
			return true;
		}

		// blockID is NOT FOUND	
		p = findEmptyCEntry();
		
		if (p == -1) {
			// cache is FULL and we must find a VICTIM
			p = getVictim();
			writeToDisk(p);
		}
		
		copyArray = new byte[blockSize];
		SysLib.rawread(blockId, copyArray); // read block from DISK
		System.arraycopy(copyArray, 0, buf, 0, blockSize);
		// make the correct changes to this cache
		cachePage.set(p, (byte[]) copyArray);
		cEntries[p].blockNum = blockId;
		cEntries[p].ref = true;
		cEntries[p].tStamp = time++;
		return true;
	}
	
	/* * * * * * write * * * * * *
	* This method will write to the requested blockId the contents that are
	* passed in byte[]. If requested blockId is already in the cache, the
	* previouse contents of that cachePage will be overwritten with the new
	* passed in content.
	* If the cache is FULL and the requested blockId is not found, then a
	* a victim will be selected for eviction to be replaced by the requested
	* content. If that Victim has a dirty bit of true, it is then written to
	* DISK in the appropriate block. After performing a successful write, this
	* this method will always set the blockId cache Entry dirty bit to true.
	*/
	public synchronized boolean write(int blockId, byte[] buf) {
		if (blockId < 0) {
			SysLib.cout("* * Error with blockId, cannot be below 0 in disk!! * *\n");
			return false;
		}
		// array of bytes
		byte[] copyArray;
		// will be used to find the index in cache of the requested block
		int p;
		p = findInCEntries(blockId);
		if (p != -1) {
			// requested block is FOUND!
			copyArray = new byte[blockSize];
			System.arraycopy(buf, 0, copyArray, 0, blockSize);
			cachePage.set(p, (byte[]) copyArray);
			cEntries[p].ref = true;
			cEntries[p].dirty = true;
			return true;
		}
		
		// requested block is NOT FOUND
		p = findEmptyCEntry();
		if (p == -1) {
			// cache is FULL and we must find a VICTIM
			p = getVictim();
			writeToDisk(p);
		}
		
		copyArray = new byte[blockSize];
		System.arraycopy(buf, 0, copyArray, 0, blockSize);
		cachePage.set(p, (byte[]) copyArray);
		// make the changes to this cache
		cEntries[p].blockNum = blockId;
		cEntries[p].ref = true;
		cEntries[p].dirty = true;
		cEntries[p].tStamp = time++;
		return true;
	}
	
	/* * * * * * sync * * * * * *
	* This method will attempt to write all cache blocks to DISK if they
	* contatin a valid blockId. Then it will reset the time for this cache
	* to 0 and call SysLib.sync().
	*/
	public synchronized void sync() {
		for(int i = 0; i < cEntries.length; i++) {
			if (cEntries[i].blockNum != -1)
				writeToDisk(i);
		}
		time = 0;
		SysLib.sync();
	}
	
	/* * * * * * flush * * * * * *
	* This method will attempt to write all cache blocks to DISK if they
	* contatin a valid blockId and reset all value of cEntries array to default
	* values, like clearing the cache. Then it will reset the time for this
	* cache to 0 and call SysLib.sync().
	*/
	public synchronized void flush() {
		for(int i = 0; i < cEntries.length; i++) {
			if (cEntries[i].blockNum != -1)
				writeToDisk(i);
			cEntries[i].blockNum = -1;
			cEntries[i].ref = false;
			cEntries[i].dirty = false;
			cEntries[i].tStamp = 0;
		}
		time = 0;
		SysLib.sync();
	}
	
	/* * * * * * writeToDisk * * * * * *
	* This method will write the contents of the cache block for the passed
	* in index to DISK ONLY IF THE DIRTY BIT IS SET TO TRUE.
	* If dirty bit is false, then nothing is written to DISK.
	*/
	private void writeToDisk(int index) {
		// check the index is within the size of cachePage
		if (index < 0 || index >= cEntries.length) {
			System.out.println("* index < 0 || index >= cEntries.length *");
			SysLib.cout("* * Invalid Cache Entry requested, no write back performed * *\n");
		}
		// check to make sure no empty cache block is written to disk 
		if (cEntries[index].blockNum == -1) {
			System.out.println("* blockNum == -1 *");
			SysLib.cout("* * Invalid Cache Entry requested, no write back performed * *\n");
			
		}
		// check if this block is DIRTY
		if (cEntries[index].blockNum != -1 && cEntries[index].dirty) {
			byte[] copy = (byte[]) cachePage.elementAt(index);
			SysLib.rawwrite(cEntries[index].blockNum, copy);
			cEntries[index].dirty = false;
		}
	}
	
	/* * * * * * getVictim * * * * * *
	* This method will return the index of the correct victim using Enhanced 
	* 2nd Chance Algorithm. This if accomplished using FIFO + Enhanced 2nd
	* Chance.
	*/
	private int getVictim() {
		// always start at index 0 to check for longest resident
		int victim = 0;
		// this will set victim = longest resident in cache
		for(int i = 1; i < cEntries.length; i++) {
			if(cEntries[i].tStamp < cEntries[victim].tStamp)
				victim = i;
		}
		
		// forever loop to find ref bit = false and dirty bit = false if
		// at all possible
		while (true) {
			if(!cEntries[victim].ref) {
				// potential victim found, check dirty bit
				victim = dirtyCheck(victim);
				return victim;
			}
			// this cache entry has been TOUCHED
			cEntries[victim].ref = false;
			// move to next cache entry
			victim = (victim + 1) % cEntries.length;
		}
	}
	
	/* * * * * * dirtyCheck * * * * * *
	* This method is called by getVictim() to return the first instance of ref
	* bit = false and dirty bit = false. If no such instance is found, then
	* the index of the original victim is returned to be evicted.
	*/
	private int dirtyCheck(int victim) {
		// will be used to traverse entries
		int curVictim = victim;
		for(int i = 0; i < cEntries.length; i++) {
			if(!cEntries[curVictim].ref && !cEntries[curVictim].dirty)
				return curVictim;
			curVictim = (victim + 1) % cEntries.length;
		}
		return victim;
	}
	
	/* * * * * * findEmptyCEntry * * * * * *
	* This method will return the first index found in cEntries where 
	* blockNum = -1. A -1 is returned if no such entry is found.
	*/
	private int findEmptyCEntry() {
		for(int i = 0; i < cEntries.length; i++) {
			if (cEntries[i].blockNum == -1)
				return i;
		}
		return -1;
	}
	
	/* * * * * * findInCEntries * * * * * *
	* This method will return the index found in cEntries where 
	* blockNum = the passed in argument. A -1 is returned if no such entry is
	* found or the requested blockNum is less than 0.
	*/
	private int findInCEntries(int blockNum) {
		if (blockNum < 0 ) {
			return -1;
		}
		for(int i = 0; i < cEntries.length; i++) {
			if (cEntries[i].blockNum == blockNum)
				return i;
		}
		return -1;
	}
	//END OF CLASS
}
