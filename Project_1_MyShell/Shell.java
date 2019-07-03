import java.io.*;
import java.util.*;

/*
* FILENAME :        Shell.java
*
* DESCRIPTION :
* 		This java class is written to represent a command shell that will run in the
		the ThreadOS environment. The program will read in commands from a user and
		run the correct system files that correspond with the input command from 
		the terminal.
*
* AUTHOR :    Mustafa Majeed        START DATE :    April 12 2019
*
* CHANGES :
*
*/
public class Shell extends Thread{

	private int cmdLineNum;		//current command line number
	private boolean running;	//bool used to keep shell running
	private String shell;		//String that will display with each iteration
	private StringBuffer cmd;	//Stringbuffer used to intake command from user
	
	/**
	******* NO Arg Constructor ***********
	* No arg constructor to construct a new instance of Shell and correctly
	* initialize the values of the private variables.
	*/
	public Shell() {
		cmd = new StringBuffer("");
		cmdLineNum = 1;
		running = true;
	}
	
	/**
	****** void run *********
	* This function is the main program for this Shell.java. The program will
	* be in a continuous loop until the user input "exit" and then the program will
	* terminate. 
	* Otherwise, the run will accept the user input and run the correct system file
	* to perform that command from the user.
	*
	*/
	public void run() {
		
		while (running) {
			shell = "Shell[" + cmdLineNum++ + "]% ";
			//display current shell line number
			SysLib.cout(shell);
			//wait for user to input command and store in cmd	
			SysLib.cin(cmd);
			
			//termination check
			if(cmd.toString().equals("exit")) {
				SysLib.cout("exit\n");
				running = false;
			}
			
			//perform the input user process
			else {
				//check to see cmd line has some proper format
				if (cmd.charAt(0) == '&' | cmd.charAt(0) == ';' | cmd.charAt(0) == ' ') {
					SysLib.cout("** error : not a valid cmd line structure... nothing executed **\n");
					cmd.delete(0, cmd.length());
				}
				//check to see cmd line has some proper format
				else if (cmd.charAt(cmd.length() - 1) == '&' | cmd.charAt(cmd.length() - 1) == ';') {
					SysLib.cout("** error : not a valid cmd line structure... nothing executed **\n");
					cmd.delete(0, cmd.length());
				}
				//begin to process cmd line
				else {
					//track current position of cmd line
					int cur = 0;
					//use to add new threads
					int tid;
					//iterate the cmd line to look for '&' or ';' 
					for(int i = 0; i < cmd.length(); i++) {
						if (cmd.charAt(i) == '&') { 
							//create a new substring of arguments before '&' delim
							String toExec = cmd.substring(cur, i - 1).toString();
							//create String array to pass to SysLib.exec
							String[] args = SysLib.stringToArgs(toExec);
							//display command to execute
							SysLib.cout(args[0] + "\n");
							//update current to reflect next command to process
							cur = i + 2;
							//create new thread
							tid = SysLib.exec(args);					
							//no waiting for prev process
							continue;
						}
						else if (cmd.charAt(i) == ';') {
							//create a new substring of arguments before ';' delim
							String toExec = cmd.substring(cur, i - 1).toString();
							//create String array to pass to SysLib.exec
							String[] args = SysLib.stringToArgs(toExec);
							//display command to execute
							SysLib.cout(args[0] + "\n");
							//update current to reflect next command to process
							cur = i + 2;
							//create new thread
							tid = SysLib.exec(args);
							//wait for the created process to finish
							int tidCheck = SysLib.join();
							//make sure that SysLib.join returns the thread number we are
							//waiting for
							while (tidCheck != tid) {
								tidCheck = SysLib.join();
							}
						}
					}
				
					//this will execute the last part of the command as if ';'
					String toExec = cmd.substring(cur).toString();
					String[] args = SysLib.stringToArgs(toExec);
					SysLib.cout(args[0] + "\n");
					tid = SysLib.exec(args);
					int tidCheck = SysLib.join();
					//make sure that SysLib.join returns the thread number we are
					//waiting for
					while (tidCheck != tid) {
						tidCheck = SysLib.join();
					}
					//remove the values in cmd
					cmd.delete(0, cmd.length());
				}
			}	
		}
		//terminate shell
		SysLib.exit();
	}
}
