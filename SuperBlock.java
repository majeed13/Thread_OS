/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
* FILE NAME : SuperBlock.java
*
* This class is written to represent the SuperBlock for a File System on disk
* block 0.
*
* By: Mustafa Majeed & Cody Rhee
*
* Date: 6/07/2019
*
* CHANGES:
*
*
*/

public class SuperBlock {
    private final int NUM_FILES = 64; // default MAX NUM FILES
    public int totalBlocks; // the number of disk blocks
    public int totalInodes; // the number of inodes
    public int freeList;    // the block number of the free list's head
   
    /* * * * * SuperBlock * * * * * *
     * constructor that takes in an int to represent the number of disk blocks
     * that this superblock needs to keep track of.
     */
    public SuperBlock( int diskSize ) {
    	//read the superblock from disk
    	byte[] bytes = new byte[Disk.blockSize];
    	SysLib.rawread(0, bytes);
    	totalBlocks = SysLib.bytes2int(bytes, 0);
    	totalInodes = SysLib.bytes2int(bytes, 4);
    	freeList = SysLib.bytes2int(bytes, 8);

    	if(totalBlocks == diskSize && totalInodes > 0 && freeList >= 2) {
    		//disk contents are valid
    		SysLib.cout("threadOS: Superblock read from DISK\n");
    		//return;
    	}
    	else {
    		//need to format the disk
    		totalBlocks = diskSize;
    		format( NUM_FILES );
    	}
    }
    
    /* * * * * format( int ) * * * * * *
     * this method will re format the Superblock to keep track of the passed
     * in max number of files. It will reset the totalInodes and freeList 
     * data members to match the values needed for the passed in number of
     * files.
     */
    public boolean format( int files ) {
    	if (files <= 64 && files > 0) {
    		// set the Superblock fields
            totalInodes = files;
            // free list always starts 1 block after iNode blocks
            freeList = (files)/16 + 1;
            // write the superblock to DISK
            sync();
            // create next link in free list
            int freeLink = freeList + 1;
            // create the links between free blocks
            for( int i = freeList; freeLink < totalBlocks; i++, freeLink++ ) {
            	byte[] link = new byte[Disk.blockSize];
            	SysLib.int2bytes( freeLink, link, 0 );
            	SysLib.rawwrite(i, link);
            	link = null;
            }
            // success
            return true;
        }
        else {
            //print error
        	SysLib.cerr("threadOS: Error formating Volume\n");
            return false;
        }
    }

    /* * * * * sync * * * * * *
     * this method will write the contents of this Superblock to the DISK
     */
    public void sync( ) {
    	// write back totalBlocks, inodeBlocks and freeList to disk
    	byte[] bytes = new byte[Disk.blockSize];
    	SysLib.int2bytes(totalBlocks, bytes, 0);
    	SysLib.int2bytes(totalInodes, bytes, 4);
    	SysLib.int2bytes(freeList, bytes, 8);
    	SysLib.rawwrite(0, bytes);
    	SysLib.cout("Superblock created at BLOCK 0\n");
    }

    /* * * * * getFreeBlock * * * * * *
     * will return the int value of a free block that is not being used by
     * any file on the DISK.
     */
    public int getFreeBlock( ) {
    	// dequeue the top block from the free list
    	int freeBlock = freeList;
        if (freeBlock >= 1) {
          byte[] bytes = new byte[Disk.blockSize];
          // read the contents of head of freelist
          SysLib.rawread(freeBlock, bytes);
          // set new freelist value to the link in current head of freelist
          freeList = SysLib.bytes2int(bytes, 0);
          // overwrite the buffer to all 0.
          SysLib.int2bytes(0, bytes, 0);
          // write buffer back to freeblock (zero the block contents)
          SysLib.rawwrite(freeBlock, bytes);
        }
        return freeBlock;
    }

    /* * * * * returnBlock * * * * * *
     * this method is used to return a block to the head of the freeList
     */
    public boolean returnBlock ( int bNum ) {
    	// enqueue a given block to the end of the free list
    	if ( bNum >= 1 ) {
          byte[] bytes = new byte[Disk.blockSize];
          // fill buffer with 0's
          for (int i = 0; i < Disk.blockSize; i++) {
            bytes[i] = 0;
          }
          SysLib.int2bytes(freeList, bytes, 0);
          // overwrite block data with all 0s
          SysLib.rawwrite(bNum, bytes);
          // make the passed in block the new head of freeList
          freeList = bNum;
          return true;
        }
    	SysLib.cerr("threaOS: Invalid block number to return\n");
        return false;
    }  
    
}

