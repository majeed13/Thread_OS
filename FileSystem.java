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
    
	public boolean format(int files) {
        if (files > 64 || files < 0)
            return false;

        return (superblock.format(files) && createInodes(files));
	}

    private boolean createInodes(int files) {
        if (files > 64 || files < 0)
            return false;

        int bNum = files/16;
        short offset = 0;
        for ( int i = 0; i <= bNum; i++) {
            byte[] buf = new byte[Disk.blockSize];
            for (int j = 0; j < 16; j++) {
                Inode toAdd = new Inode();
                SysLib.int2bytes( toAdd.length, buf, offset );
                offset += 4;
                SysLib.short2byte( toAdd.count, buf, offset );
                offset += 2;
                SysLib.short2byte( toAdd.flag, buf, offset );
                offset += 2;
                for(int k = 0; k < 11; k++, offset += 2) {
                    SysLib.short2byte( toAdd.direct[k], buf, offset );
                }
                SysLib.short2byte( toAdd.indirect, buf, offset);
                offset += 2;
            }
            SysLib.rawwrite(i, buf);
            offset = 0;
        }
        return true;
    }
/*
	public FileTableEntry open(String fileName, String mode) {

	}

    
	public boolean close(FileTableEntry ftEnt) {

	}

    
	public int fsize(FileTableEntry ftEnt) {

	}

    
    
	public int read(FileTableEntry ftEnt, byte[] buffer) {

	}

    
	public int write(FiletableEntry ftEnt, byte[] buffer) {

	}

	private boolean deallocAllBlocks(FieTableEntry ftEnt) {

	}

    
	public boolean delete(String fileName) {

	}

    public static final int SEEK_SET = 0;
    public static final int SEEK_CUR = 1;
    public static final int SEEK_END = 2;

   
	public int seek(FileTableEntry ftEnt, int offset, int whence) {

	}
*/
}