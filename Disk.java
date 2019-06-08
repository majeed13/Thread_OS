import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Disk
  extends Thread
{
  public static final int blockSize = 512;
  private final int trackSize = 10;
  private final int transferTime = 20;
  private final int delayPerTrack = 1;
  private int diskSize;
  private byte[] data;
  private int command;
  private final int IDLE = 0;
  private final int READ = 1;
  private final int WRITE = 2;
  private final int SYNC = 3;
  private boolean readyBuffer;
  private byte[] buffer;
  private int currentBlockId;
  private int targetBlockId;
  
  public Disk(int paramInt)
  {
    this.diskSize = (paramInt > 0 ? paramInt : 1);
    this.data = new byte[this.diskSize * 512];
    this.command = 0;
    this.readyBuffer = false;
    this.buffer = null;
    this.currentBlockId = 0;
    this.targetBlockId = 0;
    try
    {
      FileInputStream localFileInputStream = new FileInputStream("DISK");
      int i = localFileInputStream.available() < this.data.length ? localFileInputStream.available() : this.data.length;
      
      localFileInputStream.read(this.data, 0, i);
      localFileInputStream.close();
    }
    catch (FileNotFoundException localFileNotFoundException)
    {
      SysLib.cerr("threadOS: DISK created\n");
    }
    catch (IOException localIOException)
    {
      SysLib.cerr(localIOException.toString() + "\n");
    }
  }
  
  public synchronized boolean read(int paramInt, byte[] paramArrayOfByte)
  {
    if ((paramInt < 0) || (paramInt > this.diskSize))
    {
      SysLib.cerr("threadOS: a wrong blockId for read\n");
      return false;
    }
    if ((this.command == 0) && (!this.readyBuffer))
    {
      this.buffer = paramArrayOfByte;
      this.targetBlockId = paramInt;
      this.command = 1;
      notify();
      return true;
    }
    return false;
  }
  
  public synchronized boolean write(int paramInt, byte[] paramArrayOfByte)
  {
    if ((paramInt < 0) || (paramInt > this.diskSize))
    {
      SysLib.cerr("threadOS: a wrong blockId for write\n");
      return false;
    }
    if ((this.command == 0) && (!this.readyBuffer))
    {
      this.buffer = paramArrayOfByte;
      this.targetBlockId = paramInt;
      this.command = 2;
      notify();
      return true;
    }
    return false;
  }
  
  public synchronized boolean sync()
  {
    if ((this.command == 0) && (!this.readyBuffer))
    {
      this.command = 3;
      notify();
      
      return true;
    }
    return false;
  }
  
  public synchronized boolean testAndResetReady()
  {
    if ((this.command == 0) && (this.readyBuffer == true))
    {
      this.readyBuffer = false;
      return true;
    }
    return false;
  }
  
  public synchronized boolean testReady()
  {
    if ((this.command == 0) && (this.readyBuffer == true)) {
      return true;
    }
    return false;
  }
  
  private synchronized void waitCommand()
  {
    while (this.command == 0)
    {
      try
      {
        wait();
      }
      catch (InterruptedException localInterruptedException)
      {
        SysLib.cerr(localInterruptedException.toString() + "\n");
      }
      this.readyBuffer = false;
    }
  }
  
  private void seek()
  {
    int i = 20 + 1 * Math.abs(this.targetBlockId / 10 - this.currentBlockId / 10);
    /*try
    {
      Thread.sleep(i);
    }
    catch (InterruptedException localInterruptedException)
    {
      SysLib.cerr(localInterruptedException.toString() + "\n");
    }*/
    this.currentBlockId = this.targetBlockId;
  }
  
  private synchronized void finishCommand()
  {
    this.command = 0;
    this.readyBuffer = true;
    SysLib.disk();
  }
  
  public void run()
  {
    for (;;)
    {
      waitCommand();
      seek();
      switch (this.command)
      {
      case 1: 
        System.arraycopy(this.data, this.targetBlockId * 512, this.buffer, 0, 512);
        
        break;
      case 2: 
        System.arraycopy(this.buffer, 0, this.data, this.targetBlockId * 512, 512);
        
        break;
      case 3: 
        try
        {
          FileOutputStream localFileOutputStream = new FileOutputStream("DISK");
          localFileOutputStream.write(this.data);
          localFileOutputStream.close();
        }
        catch (FileNotFoundException localFileNotFoundException)
        {
          SysLib.cerr(localFileNotFoundException.toString());
        }
        catch (IOException localIOException)
        {
          SysLib.cerr(localIOException.toString());
        }
      }
      finishCommand();
    }
  }
}

