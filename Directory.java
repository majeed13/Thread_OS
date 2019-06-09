public class Directory {
   private static int maxChars = 30; // max characters of each file name

   // Directory entries
   private int fsize[];        // each element stores a different file size.
   private char fnames[][];    // each element stores a different file name.

   public Directory( int maxInumber ) { // directory constructor
      fsize = new int[maxInumber];     // maxInumber = max files
      for ( int i = 0; i < maxInumber; i++ ) 
         fsize[i] = 0;                 // all file size initialized to 0
      fnames = new char[maxInumber][maxChars];
      String root = "/";                // entry(inode) 0 is "/"
      fsize[0] = root.length( );        // fsize[0] is the size of "/".
      root.getChars( 0, fsize[0], fnames[0], 0 ); // fnames[0] includes "/"
   }

   public void bytes2directory( byte data[] ) {
      // assumes data[] received directory information from disk
      // initializes the Directory instance with this data[]
      int offset = 0;
      for ( int i = 0; i < fsize.length; i++, offset += 4) {
         fsize[i] = SysLib.bytes2int(data, offset);
      }

      for ( int i = 0; i < fnames.length; i++, offset += maxChars * 2) {
         String name = new String(data, offset, maxChars * 2);
         name.getChars(0, fsize[i], fnames[i], 0);
      }
   }

   public byte[] directory2bytes( ) {
      // converts and return Directory information into a plain byte array
      // this byte array will be written back to disk
      // note: only meaningfull directory information should be converted
      // into bytes.
	   return null;
   }

   public boolean ialloc( String filename, int dirIndex ) {
      // filename is the one of a file to be created.
      // allocates a new inode number for this filename
	   if(fsize[dirIndex] != 0)
		   return false;
	   fsize[dirIndex] = Math.min( filename.length(), maxChars );
	   filename.getChars(0, fsize[dirIndex], fnames[dirIndex], 0);
	   return true;
   }

   public boolean ifree( short iNumber ) {
      // deallocates this inumber (inode number)
      // the corresponding file will be deleted.
	   if ( fsize[iNumber] > 0 ) {
		   fsize[iNumber] = 0;
		   return true;
	   }
	   return false;
   }

   public short namei( String filename ) {
      // returns the inumber corresponding to this filename
	   for (int i = 0; i < fsize.length; i++) {
		   //SysLib.cout(fsize[i] + " FILESIZE for [" + i + "]\n");
		   if ( fsize[i] == filename.length() )	{
			   //SysLib.cout( filename.length() + "*****same Size*****" + fsize[i] + "\n");
	        String dirFName = new String(fnames[i], 0, fsize[i]);
	        if (filename.equals(dirFName) ) {
	        	//SysLib.cout( "Might have to trim!\n");
	          return (short) i;
	        }
	      }
		  
	    }
	    return -1;
   }
   
   public int freeSpot() {
	   for (int i = 1; i < fsize.length; i++) {
		   if (fsize[i] == 0)
			   return i;
	   }
	   return -1;
   }
}