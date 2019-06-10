/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
* FILE NAME : FileSystem.java
*
* This class is written to be the File System of ThreadOS. It will be used to 
* keep track the SuperBlock, Directory and all created files for this Volume.
*
* By: Mustafa Majeed & Cody Rhee
*
* Date: 6/8/2019
*
* CHANGES:
*
*
*/

public class FileSystem {
	private SuperBlock superblock; // This volume SuperBlock
	private Directory directory; // This volume Directory
	private FileTable fileTable; // This system wide open file table

	/* * * * * FileSystem( int ) * * * * * *
     * constructor that takes in an int to represent the number of disk blocks
     * on the ThreadOS DISK that the FileSystem can use to allocate for 
     * Superblock, Inode blocks and file blocks.
     */
	public FileSystem(int diskBlocks) {
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

	/* * * * * sync * * * * * *
     * this method is used to write the directory and the superblock to DISK
     */
	public void sync() {
		// write superblock
	    superblock.sync();
		// open Directory
		FileTableEntry ftEnt = open("/", "w");
		// create buffer
	    byte[] bytes = directory.directory2bytes();
	    // write to DISK
	    write(ftEnt, bytes);
	    close(ftEnt);
	}
	
	/* * * * * format( int ) * * * * * *
     * this method is used to re format the FileSystem using the passed in int
     * value to represent the max number of files that this Volume will
     * contain.
     */
	public boolean format(int files) {
		// check for limitation on max num files
		// and if FileTable is empty
	    if ( files <= 64 && files > 0  && fileTable.fempty() ) {
	    	superblock.format(files);
	    	// create Inodes for this Volume
	    	createInodes(files);
	    	directory = new Directory(files);
	    	fileTable = new FileTable(directory);
	    	// success
	    	return true;
	    } 
	    else { // error message
	    	SysLib.cerr("threadOS: Error with max number of files chosen for"
	    			+ "FileSystem.format(int files)\n");
	    	return false;
	    }
	  }

	/* * * * * createInodes( int ) * * * * * *
     * this method is used to create and write the Inodes for this volume
     * to the DISK based on the passed in int files that will signify the
     * max number of files for this Volume
     */
    private boolean createInodes(int files) {
    	// max num files error
        if (files > 64 || files < 0) {
            SysLib.cerr("threadOS: Error in "
            		+ "FileSystem.creatInodes(int files)\n");
        	return false;
        }
        // determine number of blocks for Inode storing
        int bNum = files/16;
        // always begin at offset 0 in the DISK block
        short offset = 0;
        // write Inodes to each block
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
        // success
        return true;
    }
    
    /* * * * * open( String, String ) * * * * * *
     * this method is used to open a file and add it to the FileTable
     * if does not already exist. It will return the correct pointer to
     * the FileTableEntry if the file is already in the FileTable.
     */
	public FileTableEntry open(String fileName, String mode) {
		// for debug purposes
		//SysLib.cout("Open in mode[" + mode +"]\n");
		FileTableEntry localFileTableEntry = fileTable.falloc(fileName, mode);
	    if ( (mode == "w") && 
	      (!deallocAllBlocks(localFileTableEntry)) ) {
	      return null;
	    }
	    return localFileTableEntry;
	}

	/* * * * * close( FileTableEntry ) * * * * * *
     * this method is used to close a file in the passed in FileTableEntry.
     * This is accomplished by decrementing the FileTableEntry count first
     * and then checking if the count is 0 to determine if the entry needs to
     * be removed from the FileTable or if there are other threads currently
     * in this file and thus keeping it in the file table.
     */
	public synchronized boolean close(FileTableEntry ftEnt) {
		// decrement the count to signify that the calling thread
		// is done using the file
		ftEnt.count -= 1;
		  // check to see if there are others using the file currently
	      if (ftEnt.count > 0) {
	        return true;
	      }
	    // remove from FileTable if count is 0
	    return fileTable.freeEntry(ftEnt);
	}
    
	/* * * * * read( FileTableEntry, byte[] ) * * * * * *
     * this method is used to read the contents of a file to the passed in 
     * byte[] buf. Will return the number of bytes read from the file.
     */
	public synchronized int read(FileTableEntry ftEnt, byte[] buf) {
		// make sure that the entry mode is in r or w+
		if ( ftEnt.mode.equals("w") || ftEnt.mode.equals("a") ) {
			SysLib.cout("Invalid read attempt... Current mode = \"" + ftEnt.mode + "\"" );
			return -1;
		}
		// current number of bytes read
		int cur = 0;
		// bytes to read to buf remaining
		int remaining = buf.length;
		// loop until no more bytes to read into buf OR end of file
		while ( remaining > 0 && (ftEnt.seekPtr < (ftEnt.fileSize())) ) {
			// current DISK block to read from
			int blockToRead = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
			if ( blockToRead == -1 ) // error 
				break;
			// create internal buffer
			byte[] bytes = new byte[Disk.blockSize];
			// read ENTIRE DISK block to buffer
			SysLib.rawread(blockToRead, bytes);
			// calculate where we are in the current block
			int seekPtrInBlock = ftEnt.seekPtr % Disk.blockSize;
			// how many bytes remaining in the current block
			int blockSizeLeft = Disk.blockSize - seekPtrInBlock;
			// number of bytes we CAN read for THIS block
			int curLengthTR = Math.min( remaining, blockSizeLeft );
			// how many bytes remain in file
			int length1 = ftEnt.fileSize() - ftEnt.seekPtr;
			// TOTAL BYTES WE CAN READ THIS ITERATION
			int totalLengthToRead = Math.min( curLengthTR, length1 );
			// copy from the internal buffer starting at the correct position
			// to the passed in buffer starting at current number of bytes read
			System.arraycopy(bytes, seekPtrInBlock, buf, cur, totalLengthToRead);
			// update cur number of bytes read
			cur += totalLengthToRead;
			// update the file seekPtr
			ftEnt.seekPtr += totalLengthToRead;
			// update remaining bytes to read into passed in buf
			remaining -= totalLengthToRead;
		}
		// return TOTAL number of bytes read for this call
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
