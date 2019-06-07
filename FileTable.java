public class FileTable {

   private Vector<FileTableEntry> table;         // the actual entity of this file table
   private Directory dir;        // the root directory 

   public FileTable( Directory directory ) { // constructor
      table = new Vector( );     // instantiate a file (structure) table
      dir = directory;           // receive a reference to the Directory
   }                             // from the file system

   // major public methods
   public synchronized FileTableEntry falloc( String fname, String mode ) {
      // allocate a new file (structure) table entry for this file name
      // allocate/retrieve and register the corresponding inode using dir
      // increment this inode's count
      // immediately write back this inode to the disk
      // return a reference to this file (structure) table entry
      short iNum = -1;
      Inode inode = null;

      while ( true ) {
         iNum = (fname.equals("/")) ? 0 : dir.namei(fname);
         if ( iNum >= 0 ) {
            inode = new Inode( iNum );
            if ( mode.equals("r") ) {
               if ( inode.flag == 0 ) // read flag
                  break;
               else if ( inode.flag == 1) { // write flag
                  try {
                     wait();
                  } catch (InterruptedException e) { }; // print error 
               }
               else { // to be deleted flag
                  iNum = -1;
                  return null; 
               }
            }
            else if ( mode.equals("w") ) {
               ...
            }
            else if ( mode.equals("w+") ) {
               ...
            }
            else if ( mode.equals("a") ) {

            }
            else {
               // error mode
            }
         }

         inode.count++;
         inode.toDisk( iNum );
         FileTableEntry e = new FileTableEntry( inode, iNum, mode );
         table.addElement( e ); // create a table entry and register it
         return e;
      }
   }

   public synchronized boolean ffree( FileTableEntry e ) {
      // receive a file table entry reference
      // save the corresponding inode to the disk
      // free this file table entry.
      // return true if this file table entry found in my table
   }

   public synchronized boolean fempty( ) {
      return table.isEmpty( );  // return if table is empty 
   }                            // should be called before starting a format
}