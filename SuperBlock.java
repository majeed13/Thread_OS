
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
            //byte[] buf = new byte[Disk.blockSize];
            SysLib.cout("*SUPERBLOCK* block size = " + totalBlocks + " totalInodes =" + totalInodes
            		+ "freeList = " + freeList + "\n");
            
            // write the int values to the buffer
            /*SysLib.int2bytes(totalBlocks, buf, 0);
            SysLib.int2bytes(totalInodes, buf, 4);
            SysLib.int2bytes(freeList, buf, 8);*/
            
            
            // write the buffer to DISK
            /*SysLib.rawwrite(0, buf);*/
            
            sync();
            
            int freeLink = freeList + 1;

            // create the links between free blocks
            for( int i = freeList; freeLink < totalBlocks; i++, freeLink++ ) {
            	byte[] link = new byte[Disk.blockSize];
            	SysLib.int2bytes( freeLink, link, 0 );
            	SysLib.rawwrite(i, link);
            	link = null;
            }
           
            return true;
        }
        else {
            //print error
            return false;
        }
    }

    public void sync( ) {
    	// write back totalBlocks, inodeBlocks and freeList to disk
    	byte[] bytes = new byte[Disk.blockSize];
    	SysLib.int2bytes(totalBlocks, bytes, 0);
    	SysLib.int2bytes(totalInodes, bytes, 4);
    	SysLib.int2bytes(freeList, bytes, 8);
    	SysLib.rawwrite(0, bytes);
    	SysLib.cout("Superblock created at BLOCK 0\n");
    }

    public int getFreeBlock( ) {
    	// dequeue the top block from the free list
    	int freeBlock = freeList;
        if (freeBlock != -1) {
          byte[] bytes = new byte[Disk.blockSize];
          
          SysLib.rawread(freeBlock, bytes);
          freeList = SysLib.bytes2int(bytes, 0);
          
          SysLib.int2bytes(0, bytes, 0);
          SysLib.rawwrite(freeBlock, bytes);
        }
        return freeBlock;
    }

    public boolean returnBlock ( int bNum ) {
    	// enqueue a given block to the end of the free list
    	if ( bNum >= 1 ) {
          byte[] bytes = new byte[Disk.blockSize];
          for (int i = 0; i < Disk.blockSize; i++) {
            bytes[i] = 0;
          }
          SysLib.int2bytes(freeList, bytes, 0);
          SysLib.rawwrite(bNum, bytes);
          freeList = bNum;
          return true;
        }
        return false;
    }  
    
}

