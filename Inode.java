public class Inode {
   public final static int iNodeSize = 32;       // fix to 32 bytes
   private final static int directSize = 11;      // # direct pointers

   public int length;                             // file size in bytes
   public short count;                            // # file-table entries pointing to this
   public short flag;                             // 0 = unused, 1 = read, 2 = write, 3 = to be deleted
   public short direct[] = new short[directSize]; // direct pointers
   public short indirect;                         // a indirect pointer

   public Inode( ) {                                     // a default constructor
      length = 0;
      count = 0;
      flag = 1;
      for ( int i = 0; i < directSize; i++ )
         direct[i] = -1;
      indirect = -1;
   }

   public Inode( short iNumber ) {                       // retrieving inode from disk
      // design it by yourself.
      int bNum = 1 + iNumber / 16;
      byte[] data = new byte[Disk.blockSize];
      SysLib.rawread( bNum, data );
      int offset = ( iNumber % 16 ) * 32;

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
      // offset += 2;
   }

   public void toDisk( short iNumber ) {                  // save to disk as the i-th inode
	   byte[] bytes = new byte[32];
	    int offset = 0;
	    
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
	    
	    int bNum = 1 + iNumber / 16;
	    byte[] fromDisk = new byte[Disk.blockSize];
	    SysLib.rawread(bNum, fromDisk);
	    offset = (iNumber % 16) * 32;
	    
	    System.arraycopy(bytes, 0, fromDisk, offset, 32);
	    SysLib.rawwrite(bNum, fromDisk);
   }
   
   public int findIndexBlock() {
     return this.indirect;
   }
   
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
     // read contents at the offest in the indirect block
     short tmp = SysLib.bytes2short( bytes, indirectBNum * 2);
     // check if the content is not -1
     if ( tmp > 0 )
     {
       SysLib.cout("indexBlock, indirectNumber = " + indirectBNum + " contents = " + tmp + "\n");
       return -1;
     }
     // write the block num to register in the indirect block at the correct offset
     SysLib.short2bytes(bNum, bytes, indirectBNum * 2);
     // write the buffer back to the indirect block
     SysLib.rawwrite(indirect, bytes);
     return 0;
   }
   
   byte[] unregisterIndexBlock() {
     if (this.indirect >= 0) {
       byte[] arrayOfByte = new byte[Disk.blockSize];
       SysLib.rawread(this.indirect, arrayOfByte);
       this.indirect = -1;
       return arrayOfByte;
     }
     return null;
   }
}