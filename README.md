# P5_File_Systems

In this project, we will build a Unix-like file system on the ThreadOS toy operating system. Through the use of the file system, user programs will now be able to access persistent data on disk by way of stream-oriented files rather than the more painful direct access to disk blocks with rawread() and rawrite().

**************************************************************************************************************************************
FileSystem.java Specification
This class is written to be the File System of ThreadOS. It will be used to keep track the SuperBlock, Directory and all created files for this Volume. With this class, it provides user threads with system calls that allows them to format, to open, to close, to format, to write to, to read from, to update the seek pointer, to delete, and to get the size of the files. Below are the list of functions that are provided in this class:

•	void sync(): this method is used to write the directory and the superblock to DISK

•	boolean format( int ): this method is used to re format the FileSystem using the passed in int value to represent the max number of files that this Volume will contain.

•	boolean createInodes( int ): this method is used to create and write the Inodes for this volume to the DISK based on the passed in int files that will signify the max number of files for this Volume.

•	FileTableEntry open( String, String ): this method is used to open a file and add it to the FileTable if does not already exist. It will return the correct pointer to the FileTableEntry if the file is already in the FileTable.

•	boolean close( FileTableEntry ): this method is used to close a file in the passed in FileTableEntry. This is accomplished by decrementing the FileTableEntry count first and then checking if the count is 0 to determine if the entry needs to be removed from the FileTable or if there are other threads currently in this file and thus keeping it in the file table.

•	int read( FileTableEntry, byte[] ): this method is used to read the contents of a file to the passed in byte[] buf. Will return the number of bytes read from the file.

•	int fsize( FileTableEntry ): this method returns the file 
size. 
•	int write( FileTableEntry, byte[] ): this method writes in the content in the byte[] buf passed in the argument to the ftEnt also passed in the argument. Method may overwrite or append on the existing data in the file, depending on where the seek pointer of file is. Method returns the number of bytes that have been written.

•	boolean deallocAllBlocks( FileTableEntry ): this method will zero out all blocks associated with the FileTableEntry that is passed in and reset Inode values to default.

•	boolean delete( String ): this method deletes the file name passed in the argument. All blocks used by the file are freed. In the case that the file is currently open, instead of deleting the file, the method returns false, indicating the file was not deleted. If the file was deleted successfully, the method returns true.

•	int seek( FileTableEntry, int, int ): this method updates the seekpointer of the passed in file with the given set from the beginning of the file, current seek pointer value, or the end of the file.
