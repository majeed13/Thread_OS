/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
* FILE NAME : FileTableEntry.java
*
* This class is written to represent entries in a FileTable
*
* By: Mustafa Majeed & Cody Rhee
*
* Date: 6/7/2019
*
* CHANGES:
*
*
*/

public class FileTableEntry {
   
   public int seekPtr;                 //    a file seek pointer
   public final Inode inode;           //    a reference to its inode
   public final short iNumber;         //    this inode number
   public int count;                   //    # threads sharing this entry
   public final String mode;           //    "r", "w", "w+", or "a"

   /* * * * * * FileTableEntry * * * * * *
    * constructor that will create a FileTableEntry using the passed in 
    * parameters. 
    */
   public FileTableEntry ( Inode i, short inumber, String m ) {
      seekPtr = 0;             // the seek pointer is set to the file top
      inode = i;
      iNumber = inumber;
      count = 0;               // at least on thread is using this entry
      mode = m;                // once access mode is set, it never changes
      if ( mode.compareTo( "a" ) == 0 ) // if mode is append,
         seekPtr = inode.length;        // seekPtr points to the end of file
   }
   
   /* * * * * * fileSize * * * * * *
    * will return the length of the Inode in this FileTableEntry. This number
    * represents the size of the file
    */
   public int fileSize() {
	   return inode.length;
   }
}
