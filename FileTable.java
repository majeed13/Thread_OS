import java.util.Vector;

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
         iNum = (fname.equals("/")) ? 0 : dir.namei( fname );
         if ( iNum == -1 ) {
        	 SysLib.cout("**filetable** FILE NOT FOUND\n");
        	 if ( mode.equals("r") ) {
        		 SysLib.cout("File Does Not Exist: Read Not Allowed.\n");
        		 return null;
        	 }
        	 iNum = (short)dir.freeSpot();
        	 SysLib.cout("**iNum = " + iNum + "**\n");
        	 inode = new Inode ( iNum );
        	 inode.flag = 2;
        	 break;
         }
         if ( iNum >= 0 ) {
            inode = new Inode( iNum );
            // read mode attempted access
            if ( mode.equals("r") ) {
               if ( inode.flag == 1 || inode.flag == 0 ) {// read flag
            	  inode.flag = 1;
                  break;
               }
               else if ( inode.flag == 2 ) { // write flag
                  try {
                     wait();
                  } catch (InterruptedException e) { }; // print error 
               }
               else { // to be deleted flag
                  iNum = -1;
                  return null; 
               }
            }
            // write mode attempted access
            else if ( mode.equals("w") ) {
            	if ( inode.flag == 0 ) {
            		inode.flag = 2;
            		break;
            	}
            	if ( inode.flag == 1 || inode.flag == 2 ) {
            		try {
            			wait();
            		} catch (InterruptedException e) { }; // print error 
            	}
            	else { // to be deleted flag
                    iNum = -1;
                    return null; 
                 }
            }
            
            // read and write mode attempted access
            else if ( mode.equals("w+") ) {
            	if ( inode.flag == 0 ) {
            		inode.flag = 2;
            		break;
            	}
            	if ( inode.flag == 1 || inode.flag == 2 ) {
            		try {
            			wait();
            		} catch (InterruptedException e) { }; // print error 
            	}
            	else { // to be deleted flag
                    iNum = -1;
                    return null; 
                 }
            }
            
            // append mode attempted access
            else if ( mode.equals("a") ) {
            	if ( inode.flag == 0 ) {
            		inode.flag = 2;
            		break;
            	}
            	if ( inode.flag == 1 || inode.flag == 2 ) {
            		try {
            			wait();
            		} catch (InterruptedException e) { }; // print error 
            	}
            	else { // to be deleted flag
                    iNum = -1;
                    return null; 
                 }
            }
            else {
               // error mode
            }
         }
      }
      
      if ( mode.equals("r") ) {
     	 for(int i = 0; i < table.size(); i++) {
     		 if (table.get(i).iNumber == iNum) {
     			 table.get(i).count++;
     			 inode.count++;
     			 return table.get(i);
     		 }
     	 }
      }
      
      inode.count++;
      inode.toDisk( iNum );
      FileTableEntry e = new FileTableEntry( inode, iNum, mode );
      table.addElement( e ); // create a table entry and register it
      return e;
      
   }

   public synchronized boolean ffree( FileTableEntry e ) {
      // receive a file table entry reference
      // save the corresponding inode to the disk
      // free this file table entry.
      // return true if this file table entry found in my table
      return false;
   }

   public synchronized boolean fempty( ) {
      return table.isEmpty( );  // return if table is empty 
   }                            // should be called before starting a format
}
