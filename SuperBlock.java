
public class Superblock {
    private final int NUM_FILES = 64;
    public int totalBlocks; // the number of disk blocks
    public int totalInodes; // the number of inodes
    public int freeList;    // the block number of the free list's head
   
    public SuperBlock( int diskSize ) {
    	//read the superblock from disk
    	byte[] bytes = new byte(Disk.blockSize);
    	SysLib.rawread(0, bytes);
    	totalBlocks = SysLib.bytes2int(bytes, 0);
    	totalInodes = SysLib.bytes2int(bytes, 4);
    	freeList = SysLib.bytes2int(bytes, 8);

    	if(totalBlocks == diskSize && totalInodes > 0 && freeList >= 2) {
    		//disk contents are valid
    		return;
    	}
    	else {
    		//need to format the disk
    		totalBlocks = diskSize
    		SysLib.format(NUM_FILES);
    	}
    }
}