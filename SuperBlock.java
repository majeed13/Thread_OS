
public class SuperBlock {
    private final int NUM_FILES = 64;
    public int totalBlocks; // the number of disk blocks
    public int totalInodes; // the number of inodes
    public int freeList;    // the block number of the free list's head
   
    public SuperBlock( int diskSize ) {
    	//read the superblock from disk
    	byte[] bytes = new byte[Disk.blockSize];
    	SysLib.rawread(0, bytes);
    	totalBlocks = SysLib.bytes2int(bytes, 0);
    	totalInodes = SysLib.bytes2int(bytes, 4);
    	freeList = SysLib.bytes2int(bytes, 8);

    	if(totalBlocks == diskSize && totalInodes > 0 && freeList >= 2) {
    		//disk contents are valid
    		//return;
    	}
    	else {
    		//need to format the disk
    		totalBlocks = diskSize;
    		format( NUM_FILES );
    	}
    }

    public boolean format( int files ) {
    	if (files <= 64 && files > 0) {
            //totalBlocks = numberOfBlocks;
            totalInodes = files;
            freeList = (files)/16 + 1;
            byte[] buf = new byte[Disk.blockSize];
            SysLib.cout("block size = " + totalBlocks + "\n");
            // write the int values to the buffer
            SysLib.int2bytes(1000, buf, 0);
            SysLib.int2bytes(totalInodes, buf, 4);
            SysLib.int2bytes(freeList, buf, 8);
            
            // write the buffer to DISK
            SysLib.rawwrite(0, buf);
            short freeLink = (short)(freeList + 1);

            // create the links between free blocks
            for( int i = freeList; freeLink < totalBlocks; i++, freeLink++ ) {
            	byte[] link = new byte[Disk.blockSize];
            	SysLib.int2bytes( freeLink, link, 0 );
            	SysLib.rawwrite(i, link);
            	link = null;
            }
            
            buf = null;
            return true;
        }
        else {
            //print error
            return false;
        }
    }

    public void sync( ) {
    	// write back totalBlocks, inodeBlocks and freeList to disk
    }

    public int getFreeBlock( ) {
    	// dequeue the top block from the free list
        return 0;
    }

    public boolean returnBlock ( ) {
    	// enqueue a given block to the end of the free list
        return false;
    }


}
