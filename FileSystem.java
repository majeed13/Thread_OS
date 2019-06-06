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

	}

    /*
    Formats the disk (Disk.java's data contents). The parameter files specifies the maximum number of files to be created 
    (the number of inodes to be allocated) in your file system. The return value is 0 on success, otherwise -1.
    */
	public boolean format(int files) {
        if (files <= 64 && files > 0) {
            superblock.totalBlocks = numberOfBlocks;
            superblock.totalInodes = files;
            superblock.freeList = (files)/16 + 1;
            byte[] buf = new byte[Disk.blockSize];
            // write the int values to the buffer
            SysLib.int2bytes(totalBlocks, buf, 0);
            SysLib.int2bytes(totalInodes, buf, 4);
            SysLib.int2bytes(freeList, buf, 8);
            // write the buffer to DISK
            SysLib.rawwrite(0, buf);
            return true;
        }
        else {
            //print error
            return false;
        }
	}

    /*
    Opens the file specified by the fileName string in the given mode (where "r" = ready only, "w" = write only, "w+" = read/write, "a" = append). 
    The call allocates a new file descriptor, fd to this file. The file is created if it does not exist in the mode "w", "w+" or "a". SysLib.open 
    must return a negative number as an error value if the file does not exist in the mode "r". Note that the file descriptors 0, 1, and 2 are 
    reserved as the standard input, output, and error, and therefore a newly opened file must receive a new descriptor numbered in the range between 3 and 31. 
    If the calling thread's user file descriptor table is full, SysLib.open should return an error value. The seek pointer is initialized to zero in the mode 
    "r", "w", and "w+", whereas initialized at the end of the file in the mode "a".
    */
	FileTableEntry open(String fileName, String mode) {

	}

    /*
    Closes the file corresponding to fd, commits all file transactions on this file, and unregisters fd from the user file descriptor table of the 
    calling thread's TCB. The return value is 0 in success, otherwise -1.
    */
	boolean close(FileTableEntry ftEnt) {

	}

    /*
    Returns the size in bytes of the file indicated by fd.
    */
	int fsize(FileTableEntry ftEnt) {

	}

    /*
    Reads up to buffer.length bytes from the file indicated by fd, starting at the position currently pointed to by the seek pointer. If bytes remaining between 
    the current seek pointer and the end of file are less than buffer.length, SysLib.read reads as many bytes as possible, putting them into the beginning of buffer. 
    It increments the seek pointer by the number of bytes to have been read. The return value is the number of bytes that have been read, or a negative value upon an error.
    */
	int read(FileTableEntry ftEnt, byte[] buffer) {

	}

    /*
    Writes the contents of buffer to the file indicated by fd, starting at the position indicated by the seek pointer. The operation may overwrite existing data in 
    the file and/or append to the end of the file. SysLib.write increments the seek pointer by the number of bytes to have been written. The return value is the number
    of bytes that have been written, or a negative value upon an error.
    */
	int write(FiletableEntry ftEnt, byte[] buffer) {

	}

	private boolean deallocAllBlocks(FieTableEntry ftEnt) {

	}

    /*
    Deletes the file specified by fileName. All blocks used by file are freed. If the file is currently open, it is not deleted and the operation returns a -1. 
    If successfully deleted a 0 is returned.
    */
	boolean delete(String fileName) {

	}

    public static final int SEEK_SET = 0;
    public static final int SEEK_CUR = 1;
    public static final int SEEK_END = 2;

    /*
    Updates the seek pointer corresponding to fd as follows:
    - If whence is SEEK_SET (= 0), the file's seek pointer is set to offset bytes from the beginning of the file
    - If whence is SEEK_CUR (= 1), the file's seek pointer is set to its current value plus the offset. The offset can be positive or negative.
    - If whence is SEEK_END (= 2), the file's seek pointer is set to the size of the file plus the offset. The offset can be positive or negative.
    */
	int seek(FileTableEntry ftEnt, int offset, int whence) {

	}
}