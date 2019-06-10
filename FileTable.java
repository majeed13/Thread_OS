import java.util.Vector;
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
* FILE NAME : FileTable.java
*
* This class is written to represent the system wide open FileTable for this volume
*
* By: Mustafa Majeed & Cody Rhee
*
* Date: 6/8/2019
*
* CHANGES:
*
*
*/

public class FileTable {

   private Vector<FileTableEntry> table; // the actual entity of this file table
   private Directory dir;        // the root directory 

   /* * * * * * FileTable( Directory ) * * * * * *
    * constructor that will create a FileTable using the passed in Directory
    * parameter as a reference to the File System Directory
    */
   public FileTable( Directory directory ) {
      table = new Vector( );     // instantiate a file (structure) table
      dir = directory;           // receive a reference to the Directory
   }                             // from the file system

   /* * * * * * falloc( String, String ) * * * * * *
    * constructor that will create a FileTable using the passed in Directory
    * parameter as a reference to the File System Directory
    */
   public synchronized FileTableEntry falloc( String fname, String mode ) {
      // allocate a new file (structure) table entry for this file name
      // allocate/retrieve and register the corresponding inode using dir
      // increment this inode's count
      // immediately write back this inode to the disk
      // return a reference to this file (structure) table entry
      short iNum = -1;
      Inode inode = null;
      boolean inDirectory = false;

      while ( true ) {
         iNum = (fname.equals("/")) ? 0 : dir.namei( fname );
         if ( iNum == -1 ) {
        	 if ( mode.equals("r") ) {
        		 SysLib.cout("File Does Not Exist: Read Not Allowed.\n");
        		 return null;
        	 }
        	 iNum = (short)dir.freeSpot();
        	 // debug purposes
        	 //SysLib.cout("**iNum = " + iNum + "**\n");
        	 inode = new Inode ( iNum );
        	 inode.flag = 2;
        	 if ( !dir.ialloc(fname, iNum) ) {
        		 SysLib.cout("* *Error while creating File in Directory* *\n");
        		 return null;
        	 }
        	 break; 
         }
         if ( iNum >= 0 ) {
        	inDirectory = true;
            inode = new Inode( iNum );
            // read mode attempted access
            if ( mode.equals("r") ) {
            	// debug purposes
            	//SysLib.cout("Flag == " + inode.flag + "\n");
               if ( inode.flag == 1 || inode.flag == 0 ) { // read flag
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
            	SysLib.cerr("threadOS: Incorrect mode (" + mode + ") "
            			+ "attemped\n");
            	iNum = -1;
            	return null;
            }
         }
      }
        
      // check FileTable for FTE 
      if ( inDirectory ) {
    	  //SysLib.cout("* *Attempting to return FTE in FILETABLE* *\n");
    	  //SysLib.cout("iNum = " + iNum + "\n");
    	  for(int i = 0; i < table.size(); i++) {
    		  if (table.get(i).iNumber == iNum) {
    			  // dubug purposes
    			  SysLib.cout("found in table\n");
    			  table.get(i).count++;
    			  inode.count++;
    			  return table.get(i);
    		  }
    	  }
      }
      
      // FTE Not in FileTable
      inode.count++;
	  inode.toDisk( iNum );
	  FileTableEntry ftEnt = new FileTableEntry( inode, iNum, mode );
	  ftEnt.count++;
	  table.addElement( ftEnt ); // create a table entry and register it
	  return ftEnt;
      
   }

   public synchronized boolean freeEntry( FileTableEntry ftEnt ) {
      // receive a file table entry reference
      // save the corresponding inode to the disk
      // free this file table entry.
      // return true if this file table entry found in my table
	 // SysLib.cout("FileTableEntry count = " + ftEnt.count + " for FileTableEntry = " + ftEnt.iNumber + "\n");
	  if (table.removeElement(ftEnt)) {
		  ftEnt.inode.count--;
		  if ( ftEnt.inode.flag == 1 && ftEnt.count == 0 )
			  ftEnt.inode.flag = 0;
		  if ( ftEnt.inode.flag == 2 && ftEnt.count == 0 )
			  ftEnt.inode.flag = 0;
		  ftEnt.inode.toDisk(ftEnt.iNumber);
		  ftEnt = null;
		  notify( );
		  return true;
	  }
      return false;
   }

   public synchronized boolean fempty( ) {
      return table.isEmpty( );  // return if table is empty 
   }                            // should be called before starting a format
}
