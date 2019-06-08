public class Inode {
   public final static int iNodeSize = 32;       // fix to 32 bytes
   private final static int directSize = 11;      // # direct pointers

   public int length;                             // file size in bytes
   public short count;                            // # file-table entries pointing to this
   public short flag;                             // 0 = unused, 1 = read, 2 = write, 3 = to be deleted
   public short direct[] = new short[directSize]; // direct pointers
   public short indirect;                         // a indirect pointer

   Inode( ) {                                     // a default constructor
      length = 0;
      count = 0;
      flag = 1;
      for ( int i = 0; i < directSize; i++ )
         direct[i] = -1;
      indirect = -1;
   }

   Inode( short iNumber ) {                       // retrieving inode from disk
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
   
   boolean registerIndexBlock(short paramShort) {
     for (int i = 0; i < 11; i++) {
       if (this.direct[i] == -1) {
         return false;
       }
     }
     if (this.indirect != -1) {
       return false;
     }
     this.indirect = paramShort;
     byte[] arrayOfByte = new byte[Disk.blockSize];
     for (int i = 0; i < 256; i++) {
       SysLib.short2bytes((short)-1, arrayOfByte, i * 2);
     }
     SysLib.rawwrite(paramShort, arrayOfByte);
     
     return true;
   }
   
   int findTargetBlock(int paramInt) {
     int i = paramInt / 512;
     if (i < 11) {
       return this.direct[i];
     }
     if (this.indirect < 0) {
       return -1;
     }
     byte[] arrayOfByte = new byte[Disk.blockSize];
     SysLib.rawread(this.indirect, arrayOfByte);
     int j = i - 11;
     return SysLib.bytes2short(arrayOfByte, j * 2);
   }
   
   int registerTargetBlock(int paramInt, short paramShort) {
     int i = paramInt / 512;
     if (i < 11) {
       if (this.direct[i] >= 0) {
         return -1;
       }
       if ((i > 0) && (this.direct[(i - 1)] == -1)) {
         return -2;
       }
       this.direct[i] = paramShort;
       return 0;
     }
     if (this.indirect < 0) {
       return -3;
     }
     byte[] arrayOfByte = new byte[Disk.blockSize];
     SysLib.rawread(this.indirect, arrayOfByte);
     int j = i - 11;
     if (SysLib.bytes2short(arrayOfByte, j * 2) > 0)
     {
       SysLib.cerr("indexBlock, indirectNumber = " + j + " contents = " + SysLib.bytes2short(arrayOfByte, j * 2) + "\n");
       
       return -1;
     }
     SysLib.short2bytes(paramShort, arrayOfByte, j * 2);
     
     SysLib.rawwrite(this.indirect, arrayOfByte);
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