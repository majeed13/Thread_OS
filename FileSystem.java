public class FileSystem {
	private SuperBlock superblock;
	private Directory directory;
	private FileTable fileTable;
    private int numberOfBlocks;

	public FileSystem(int diskBlocks) {
        // number of total blocks on DISK
        numberOfBlocks = diskBlocks;
	    // create superblock, and format disk with 64 inodes in default
	    superblock = new SuperBlock(diskBlocks);

	    // create directory, and register "/" in directory entry 0
	    directory = new Directory(superblock.totalInodes);

	    // file table is created, and store directory in the file table
	    fileTable = new FileTable(directory);

	    // directory reconstruction
	    FileTableEntry dirEnt = open("/", "r");
	    int dirSize = fsize(dirEnt);
	    if (dirSize > 0) {
	    	byte[] dirData = new byte[dirSize];
	    	read(dirEnt, dirData);
	    	directory.bytes2directory(dirData);
	    }
	    close(dirEnt);
	}

	public void sync() {
		// open Directory
		FileTableEntry ftEnt = open("/", "w");
		// create buffer
	    byte[] bytes = directory.directory2bytes();
	    // write to DISK
	    write(ftEnt, bytes);
	    close(ftEnt);
	    // write superblock
	    superblock.sync();
	}
	
	public boolean format(int files) {
	    
		superblock.format(files);
		
	    createInodes(files);
	    
	    directory = new Directory(files);
	    
	    fileTable = new FileTable(directory);
	    
	    return true;
	  }

    private boolean createInodes(int files) {
        if (files > 64 || files < 0)
            return false;

        int bNum = files/16;
        short offset = 0;
        for ( int i = 1; i <= bNum; i++) {
            byte[] buf = new byte[Disk.blockSize];
            for (int j = 0; j < 16; j++) {
                Inode toAdd = new Inode();
                SysLib.int2bytes( toAdd.length, buf, offset );
                offset += 4;
                SysLib.short2bytes( toAdd.count, buf, offset );
                offset += 2;
                SysLib.short2bytes( toAdd.flag, buf, offset );
                offset += 2;
                for(int k = 0; k < 11; k++, offset += 2) {
                    SysLib.short2bytes( toAdd.direct[k], buf, offset );
                }
                SysLib.short2bytes( toAdd.indirect, buf, offset);
                offset += 2;
            }
            SysLib.rawwrite(i, buf);
            offset = 0;
        }
        return true;
    }
    
	public FileTableEntry open(String fileName, String mode) {
		SysLib.cout("Open in mode[" + mode +"]\n");
		FileTableEntry localFileTableEntry = fileTable.falloc(fileName, mode);
	    if ( (mode == "w") && 
	      (!deallocAllBlocks(localFileTableEntry)) ) {
	      return null;
	    }
	    return localFileTableEntry;
	}

    
	public synchronized boolean close(FileTableEntry ftEnt) {
		ftEnt.count -= 1;
	      if (ftEnt.count > 0) {
	        return true;
	      }
	    return fileTable.freeEntry(ftEnt);
	}
    
	public synchronized int read(FileTableEntry ftEnt, byte[] buf) {
		if ( ftEnt.mode.equals("w") || ftEnt.mode.equals("a") ) {
			SysLib.cout("Invalid read attempt... Current mode = \"" + ftEnt.mode + "\"" );
			return -1;
		}
		int cur = 0;
		int remaining = buf.length;
		while ( remaining > 0 && (ftEnt.seekPtr < (ftEnt.fileSize())) ) {
			int blockToRead = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
			if ( blockToRead == -1 )
				break;
			byte[] bytes = new byte[Disk.blockSize];
			
			SysLib.rawread(blockToRead, bytes);
			int seekPtrInBlock = ftEnt.seekPtr % Disk.blockSize;
			int blockSizeLeft = Disk.blockSize - seekPtrInBlock;
			int curLengthTR = Math.min( remaining, blockSizeLeft );
			int length1 = ftEnt.fileSize() - ftEnt.seekPtr;
			int totalLengthToRead = Math.min( curLengthTR, length1 );
			
			System.arraycopy(bytes, seekPtrInBlock, buf, cur, totalLengthToRead);
			
			cur += totalLengthToRead;
			ftEnt.seekPtr += totalLengthToRead;
			remaining -= totalLengthToRead;
		}
		return cur;
	}

	public int fsize(FileTableEntry ftEnt) {
		return ftEnt.fileSize();
	}
    
	public synchronized int write(FileTableEntry ftEnt, byte[] buf) {
		int cur = 0;
		int remaining = buf.length;
		while (remaining > 0) {
			int blockToWrite = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
			// block not assigned in iNode
			if ( blockToWrite == -1 ) {
				// retrieve first free block
				int freeBlock = (short)superblock.getFreeBlock();
				// attempt to register the free bock in the iNode of the ftEnt
				int reg = ftEnt.inode.registerTargetBlock( ftEnt.seekPtr, (short)freeBlock );
				
				if ( reg == -1 ) {
					SysLib.cout("* *Error: invalid register action* *\n");
					return -1;
				}
				else if ( reg == -2 ) {
					short indirectBNum = (short)superblock.getFreeBlock();
					if ( !ftEnt.inode.registerIndexBlock(indirectBNum) ) {
						SysLib.cout("Cannot allocate indirect pointer in iNode # ="  + ftEnt.iNumber + "\n");
						return -1;
					}
					int regCheck = ftEnt.inode.registerTargetBlock(ftEnt.seekPtr, (short)freeBlock);
					if ( regCheck != 0 ) {
						SysLib.cout("Unable to register new block to iNode # = * " + ftEnt.iNumber + " *for WRITE\n");
						return -1;
					}
				}
		        blockToWrite = freeBlock;
			}
		        
		    byte[] bytes = new byte[Disk.blockSize];
		    if ( SysLib.rawread( blockToWrite, bytes ) == -1 ) {
		    	System.exit(2);
		    }
		    int seekPtrInBlock = ftEnt.seekPtr % Disk.blockSize;
		    int blockBytesLeft = Disk.blockSize - seekPtrInBlock;
		    int lengthWritten = Math.min( blockBytesLeft, remaining );
		        
		    System.arraycopy( buf, cur, bytes, seekPtrInBlock, lengthWritten );
		        
		    SysLib.rawwrite( blockToWrite, bytes );
		        
		    ftEnt.seekPtr += lengthWritten;
		    cur += lengthWritten;
		    remaining -= lengthWritten;
		    if (ftEnt.seekPtr > ftEnt.inode.length) {
		       	ftEnt.inode.length = ftEnt.seekPtr;
		    }
		}
		ftEnt.inode.toDisk(ftEnt.iNumber);
	      
	    return cur;
	}

	private boolean deallocAllBlocks(FileTableEntry ftEnt) {
		if (ftEnt.inode.count != 1) {
		      return false;
		    }
		    byte[] arrayOfByte = ftEnt.inode.unregisterIndexBlock();
		    if (arrayOfByte != null) {
		      int i = 0;
		      int j;
		      while ( (j = SysLib.bytes2short(arrayOfByte, i) ) != -1) {
		        superblock.returnBlock(j);
		      }
		    }
		    for (int i = 0; i < 11; i++) {
		      if (ftEnt.inode.direct[i] != -1) {
		        superblock.returnBlock(ftEnt.inode.direct[i]);
		        ftEnt.inode.direct[i] = -1;
		      }
		    }
		    ftEnt.inode.toDisk(ftEnt.iNumber);
		    return true;
	}

    
	public boolean delete(String fileName) {
		FileTableEntry ftEnt = open( fileName, "w" );
		short iNum = ftEnt.iNumber;
		return ( close(ftEnt) && directory.ifree(iNum) ); 
	}

    public static final int SEEK_SET = 0;
    public static final int SEEK_CUR = 1;
    public static final int SEEK_END = 2;

   
	public synchronized int seek(FileTableEntry ftEnt, int offset, int whence) {
		
		if ( whence == SEEK_SET ) {
			// make sure seekPtr is not gonna be negative or past the file size
			if ( (offset >= 0) && (offset <= ftEnt.fileSize())) {
				ftEnt.seekPtr = offset;
			}
			else // error seek
				return -1;
		}
		else if ( whence == SEEK_CUR ) {
			// add offset to current seekPtr position
			int cur = ftEnt.seekPtr + offset;
			// make sure seekPtr is not gonna be negative or past the file size
			if ( (cur >= 0) && ( cur <= ftEnt.fileSize())) {
				ftEnt.seekPtr = cur;
			}
			else // error
				return -1;
		}
		else if ( whence == SEEK_END ) {
			// add offset to end of file
			int cur = ftEnt.fileSize() + offset;
			// make sure seekPtr is not gonna be negative or past the file size
			if ( (cur >= 0) && (cur <= ftEnt.fileSize())) {
				ftEnt.seekPtr = cur;
			}
			else // error
				return -1;
		}
		// return the seekPtr in the file after making correct adjustment
		return ftEnt.seekPtr;
	}

}
