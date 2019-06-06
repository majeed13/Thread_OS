public class Inode {
   public final static int iNodeSize = 32;       // fix to 32 bytes
   private final static int directSize = 11;      // # direct pointers

   public int length;                             // file size in bytes
   public short count;                            // # file-table entries pointing to this
   public short flag;                             // 0 = read, 1 = write, 2 = to be deleted
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
   }

   int toDisk( short iNumber ) {                  // save to disk as the i-th inode
      // design it by yourself.
   }
}