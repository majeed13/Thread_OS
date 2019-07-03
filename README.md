# Thread_OS

ThreadOS is a program that `simulates` how an operating system works on a computer. It is written in Java and uses java threads for multi thread programming. ThreadOS is made as a toy operating system to help students understand how the less privileged user mode applications make system calls to the OS kernel in order to perform the appropriate user requested task.

*******************************************************************************************************************************************
Project Details
•	myShell
Create the basic shell for ThreadOS. The shell was designed to allow a user to load 1 to many programs for execution. Each program can be separated by an `&` or `;` to signify concurrent or sequential execution.

•	Scheduler
Create a synchronized thread scheduling system for ThreadOS that implements a multilevel feedback queue (MFQS).

•	Synchronization
Add thread synchronization to ThreadOS so I/O bound threads stop wasting CPU power via busy waiting.

•	Buffer Cache
Add a buffer cache system to ThreadOS to reduce the time it takes to read and write to frequently used blocks on DISK. Victims from the buffer cache are chosen by the enhanced second chance algorithm. 

•	File System
Worked as a group of 3 to create a File System for ThreadOS. It is used manage all the memory on DISK for the Superblock, the inode blocks and the Directory blocks on DISK. The File System will also allocate DISK blocks for newly created files when necessary.
