/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
* FILE NAME : Inode.java
*
* This class is written to represent Inodes for each file in the FileSystem.
* Each file gets 1 Inode that will track the size of the file and pointers
* to the disk blocks that its contents reside in.
*
* By: Mustafa Majeed & Cody Rhee
*
* Date: 6/7/2019
*
* CHANGES:
*
*
*/

public class Inode {
   public final static int iNodeSize = 32;       // fix to 32 bytes
   private final static int directSize = 11;      // # direct pointers

   public int length;                             // file size in bytes
   public short count;                            // # file-table entries pointing to this
   public short flag;                             // 0 = unused, 1 = read, 2 = write, 3 = to be deleted
   public short direct[] = new short[directSize]; // direct pointers
   public short indirect;                         // a indirect pointer

   /* * * * * * Inode * * * * * *
    * defalut no arg constructor that will create an Inode with length of 0
    * a count of 0, a flag of 1 and set all pointers to -1. 
    */
   public Inode( ) {                                   
      length = 0;
      count = 0;
      flag = 1;
      for ( int i = 0; i < directSize; i++ )
         direct[i] = -1;
      indirect = -1;
   }

   /* * * * * * Inode( short ) * * * * * *
    * this constructor is used to retrieve an Inode from disk using the passed
    * in iNumber. 
    */
   public Inode( short iNumber ) {                     
      // determine block number of Inode to retrieve
      int bNum = 1 + iNumber / 16;
      // create byte buffer
      byte[] data = new byte[Disk.blockSize];
      SysLib.rawread( bNum, data );
      // determine the offset for this Inode
      int offset = ( iNumber % 16 ) * 32;
      // begin to read the Inode data
      length = SysLib.bytes2int( data, offset );
      offset += 4;
      count = SysLib.bytes2short( data, offset );
      offset += 2;
      flag = SysLib.bytes2short( data, offset );
      offset += 2;

      // must get the direct pointers and indirect pointer for this INODE from disk
      for (int i = 0; i < directSize; i++, offset += 2) {
    	  direct[i] = SysLib.bytes2short( data, offset );
      }
      
      indirect = SysLib.bytes2short( data, offset );
   }

   /* * * * * * toDisk( short ) * * * * * *
    * this method is used to write the callind Inode to DISK at the passed in
    * iNumber position in Inode list. 
    */
   public void toDisk( short iNumber ) { 
	   // create byte buffer for 1 Inode
	   byte[] bytes = new byte[32];
	   int offset = 0;
	   // begin writing Inode data to byte buffer
	   SysLib.int2bytes(length, bytes, offset);
	   offset += 4;
	   SysLib.short2bytes(count, bytes, offset);
	   offset += 2;
	   SysLib.short2bytes(flag, bytes, offset);
	   offset += 2;
	    
	   for (int i = 0; i < 11; i++) {
	     SysLib.short2bytes(direct[i], bytes, offset);
	     offset += 2;
	   }
	    
	   SysLib.short2bytes(indirect, bytes, offset);
	   offset += 2;
	   // determine block number where this Inode resides
	   int bNum = 1 + iNumber / 16;
	   byte[] fromDisk = new byte[Disk.blockSize];
	   // read the data from that block number (to ensure data is lost)
	   SysLib.rawread(bNum, fromDisk);
	   // determine offset for this Inode in the list
	   offset = (iNumber % 16) * 32;
	   // write Inode byte data to fromDisk buffer
	   System.arraycopy(bytes, 0, fromDisk, offset, 32);
	   // re write the fromDisk buffer to the disk block
	   SysLib.rawwrite(bNum, fromDisk);
   }
   
   /* * * * * * findIndirectBlock * * * * * *
    * this method is used to write the calling Inode to DISK at the passed in
    * iNumber position in Inode list. 
    */
   public int findIndirectBlock() {
     return indirect;
   }
   
   /* * * * * * registerIndexBlock( short ) * * * * * *
    * this method is used to allocate the passed in block number to the
    * indirect pointer 
    */
   public boolean registerIndexBlock(short bNum) {
	 // check to see if all indirect blocks are allocated
     for (int i = 0; i < directSize; i++) {
       if ( direct[i] == -1 ) {
         return false;
       }
     }
     // check to make sure indirect block is NOT allocated already
     if (indirect != -1) {
       return false;
     }
     // set indirect to point to the passed in bNum
     indirect = bNum;
     // create buffer to write short value -1 into
     byte[] bytes = new byte[Disk.blockSize];
     for (int i = 0; i < 256; i++) {
       SysLib.short2bytes((short)-1, bytes, i * 2);
     }
     // write the buffer to the indirect disk block
     SysLib.rawwrite(bNum, bytes);
     return true;
   }
   
   /* * * * * * findTargetBlock( int ) * * * * * *
    * this method is used to find the DISK block number of a file using the
    * passed in int value for the byte of data to find in that file.
    */
   public int findTargetBlock(int pos) {
	 // determine what block of the file we need access to
     int localBNum = pos / Disk.blockSize;
     // block is pointed to by direct pointers?
     if ( localBNum < directSize ) {
       return direct[localBNum];
     }
     // no indirect set for this iNode
     if ( indirect < 0 ) {
       return -1;
     }
     // create buffer to read block numbers pointed to by indirect
     byte[] arrayOfByte = new byte[Disk.blockSize];
     SysLib.rawread( indirect, arrayOfByte );
     // determine indirect block number we need access to
     int indirectBNum = localBNum - directSize;
     // return the short that points to the correct block we need
     return SysLib.bytes2short( arrayOfByte, indirectBNum * 2 );
   }
   
   /* * * * * * registerTargetBlock( int, short ) * * * * * *
    * this method is used to add the passed in bNum to the list of indirect
    * block pointers for this Inode at the specified position in the file
    */
   public int registerTargetBlock(int pos, short bNum) {
	 // determine what block of the file we need access to
     int localBNum = pos / Disk.blockSize;
     // block is pointed to by direct pointers?
     if ( localBNum < directSize ) {
       if ( direct[localBNum] >= 0 ) {
         return -1;  // invalid register action
       }
       // check if the previous file block is empty?
       if ( (localBNum > 0) && (direct[(localBNum - 1)] == -1) ) {
         return -1;
       }
       // block to register already exists in direct pointer array
       direct[localBNum] = bNum;
       return 0;
     }
     // check if the indirect block is used
     if (this.indirect < 0) {
       return -2; // need to add the block to the indirect pointer
     }
     // need to read the indirect pointers to see if there is open space
     byte[] bytes = new byte[Disk.blockSize];
     SysLib.rawread( indirect, bytes );
     int indirectBNum = localBNum - directSize;
     // read contents at the offset in the indirect block
     short tmp = SysLib.bytes2short( bytes, indirectBNum * 2);
     // check if the content is not -1 (already pointing to another block)
     if ( tmp > 0 )
     {
       SysLib.cout("File block [" + localBNum + "] residing in indirect block [" + indirectBNum + "] "
       		+ "contains = " + tmp + "\n");
       return -1;
     }
     // write the block num to register in the indirect block at the correct offset
     int IBlockOffset = indirectBNum * 2;
     SysLib.short2bytes(bNum, bytes, IBlockOffset);
     // write the buffer back to the indirect block
     SysLib.rawwrite(indirect, bytes);
     return 0;
   }
   
   /* * * * * * unregisterIndexBlock * * * * * *
    * this method will return the contents of the DISK block that is being
    * pointed to by the indirect pointer for this Inode
    */
   public byte[] unregisterIndexBlock() {
	 // will only return the contents of the indirect block if it is not 
	 // set to default value of -1 and not pointing to the Superblock
     if (indirect >= 1) {
       byte[] bytes = new byte[Disk.blockSize];
       SysLib.rawread(indirect, bytes);
       // reset the indirect block pointer
       indirect = -1;
       return bytes;
     }
     return null;
   }
}