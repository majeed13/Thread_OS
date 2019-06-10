/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
* FILE NAME : Directory.java
*
* The purpose of this class is to maintain different files 
*
* By: Mustafa Majeed & Cody Rhee
*
* Date: 6/7/2019
*
* CHANGES:
*
*
*/

public class Directory {
   private static int maxChars = 30; // max characters of each file name

   // Directory entries
   private int fsize[];        // each element stores a different file size.
   private char fnames[][];    // each element stores a different file name.

   /* * * * * * Directory( int ) * * * * * *
    * this is a constructor that takes in an integer that defines the maximum number
    * of files the Directory has. The file size array size is defined by this parameter
    * and each index of the file size array is initialized to 0. The 2-D array, fnames' size is 
    * defined as max number of files X max characters of each file name.
    */
   public Directory( int maxInumber ) { // directory constructor
      fsize = new int[maxInumber];     // maxInumber = max files
      for ( int i = 0; i < maxInumber; i++ ) 
         fsize[i] = 0;                 // all file size initialized to 0
      fnames = new char[maxInumber][maxChars];
      String root = "/";                // entry(inode) 0 is "/"
      fsize[0] = root.length( );        // fsize[0] is the size of "/".
      root.getChars( 0, fsize[0], fnames[0], 0 ); // fnames[0] includes "/"
   }

   /* * * * * * bytes2directory( byte[] ) * * * * * *
    * this method assumes data[] received directory information from disk
    * initializes the Directory instance with this data[]
    */
   public void bytes2directory( byte data[] ) {
      int offset = 0;

      // loop converts values in the byte data array to integer and adds it into file size array
      for ( int i = 0; i < fsize.length; i++, offset += 4) {
         fsize[i] = SysLib.bytes2int(data, offset); 
      }

      // constructs a new String each iteration and sets each row in fnames 2-D array
      // to the value of name from 0 to reviously read in fsize
      for ( int i = 0; i < fnames.length; i++, offset += maxChars * 2) {
         String name = new String(data, offset, maxChars * 2);
         name.getChars(0, fsize[i], fnames[i], 0);
      }
   }

   /* * * * * * directory2bytes * * * * * *
    * this method converts and return Directory information into a plain byte array
    * this byte array will be written back to disk. Only meaningfull directory 
    * information should be converted into bytes.
    */
   public byte[] directory2bytes( ) {
      // create internal buffer
     int numOfBytes = ( fsize.length * 4 ) + ( fnames.length * maxChars * 2 );
     byte[] bytes = new byte[numOfBytes];
     int offset = 0;
     // write all int values from fsize[] to buffer
     for ( int i = 0; i < fsize.length; i++, offset += 4) {
       SysLib.int2bytes(fsize[i], bytes, offset);
     }
     // write all char values from fnames to buffer
     for ( int i = 0; i < fnames.length; i++, offset += maxChars * 2) {
       String name = new String( fnames[i], 0, fsize[i] );
       byte[] nameBytes = name.getBytes();
       System.arraycopy( nameBytes, 0, bytes, offset, nameBytes.length );
     }
     return bytes;
   }

   /* * * * * * ialloc( String, int ) * * * * * *
    * this method allocates a new inode number for the String filename that is
    * passed in as a parameter
    */
   public boolean ialloc( String filename, int dirIndex ) {
	   if(fsize[dirIndex] != 0)  // file size of the inode number passed in is not available
		   return false;

      // sets the value of the file size to be the smaller value between the max character number 
      // or the length of the String file name passed in argument
	   fsize[dirIndex] = Math.min( filename.length(), maxChars ); 

      // // copies characters from file name in the argument to the 2-D array of file names
	   filename.getChars(0, fsize[dirIndex], fnames[dirIndex], 0); 
	   return true;
   }

   /* * * * * * ifree( short ) * * * * * *
    * this method deallocates the inode number passed in the parameter (inode number)
    * and the corresponding file will be deleted.
    */
   public boolean ifree( short iNumber ) {
      // deallocates this inumber (inode number)
      // the corresponding file will be deleted.
	   if ( fsize[iNumber] > 0 ) {
		   fsize[iNumber] = 0;
		   return true;
	   }
	   return false;
   }

   /* * * * * * namei( String ) * * * * * *
    * this method returns the inumber corresponding to the String filename passed in the
    * parameter
    */
   public short namei( String filename ) {
	   for (int i = 0; i < fsize.length; i++) {
		   if ( fsize[i] == filename.length() )	{  // size of filename passed in the argument found in fsize array
	        String dirFName = new String(fnames[i], 0, fsize[i]); // String variable that is read in from the fnames 2-D array with file size length
	        if (filename.equals(dirFName) ) { // filename found in the file name 2-D array
	          return (short) i;
	        }
	      }
		  
	    }
	    return -1;
   }
   
   /* * * * * * freeSpot * * * * * *
    * this method finds the first index in the file size array that is free
    */
   public int freeSpot() {
	   for (int i = 1; i < fsize.length; i++) {
		   if (fsize[i] == 0) // index with file size 0 found
			   return i;
	   }
	   return -1;
   }
}