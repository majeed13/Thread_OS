/*
* FILENAME :        processes.cpp
*
* DESCRIPTION :
* 		This program is written to execute the following linux shell command line
*		
*		"ps -A | grep [args] | wc -l"
*
*		That is, list all processes, find the matching [args] pattern and display
*		a line count to the console.
*		This file will implement this sequence using fork() and execlp() and pipes
*		to communicate between parents and children.
*
* AUTHOR :    Mustafa Majeed        START DATE :    April 10 2019
*
* CHANGES :
*
*/
#include <assert.h>
#include <memory.h>
#include <stdio.h>
#include <sys/wait.h>
#include <unistd.h>
#include <string>
#include <iostream>
using namespace std;

/**
******* greatGrandChild *********
* This function will be the great grand child of the parent processes main.
* The fd[] parameter will be used for pipe communication to the grand child
* and pass the information output by "ps -A" system call from a linux shell. 
*/
void greatGrandChild(int fd[]) {
	//close read end of pipe
	close(fd[0]);
	//redirect STDOUT to write end of pipe
	dup2(fd[1], STDOUT_FILENO);
	//close write end of pipe
	close(fd[1]);
	//run "ps -A"
	execlp("/bin/ps", "ps", "-A", nullptr);
}

/**
******* grandChild *********
* This function will be the grand child of the parent processes main. The passed
* in fd[] pipe will be used to communicate information to the child of processes main
* and fd2[] pipe will be created and used to communicate with the great grand child.
* The information written by great grand child process "ps -A" into fd2[] pipe will
* be used to run the "grep" linux command and look for the pattern arg that is passed
* into this function.
* The results will be written into fd[] pipe to communicate with child or processes
* main. 
*/
void grandChild(int fd[], char* arg) {
	//create a new pipe
	int fd2[2];
	int status = pipe(fd2);
	//fork to GGC
	int pid = fork();
	//if fork fails
	if (pid < 0) {
		cout << "** error : fork() FAILED in grandChild **\n";
	}
	//to be used by GGC
	if (pid == 0) {
		greatGrandChild(fd2);
	}
	//read the info passed and execute correct process
	else {
		//close write end of fd2
		close(fd2[1]);
		//close read end of fd
		close(fd[0]);
		//redirect STDIN to read info passed from GGC
		dup2(fd2[0], STDIN_FILENO);
		//redirect STDOUT to write to pipe to communicate with child of processes
		//main... (parent of this process)
		dup2(fd[1], STDOUT_FILENO);
		//close pipe ends
		close(fd2[0]);
		close(fd[1]);
		//execute grep looking for the passed in patterd
		execlp("/bin/grep", "grep", arg, nullptr);
		int donepid = wait(&status);
		assert(donepid == pid);
	}
}

/**
******* child *********
* This function will be the child of processes main. It will read the information
* passed into fd1[] pipe by grand child of processes main and count the number of
* lines and then display the resulting number to the console screen or STDOUT
*/
void child(char* arg) {
	//create pipe
	int fd1[2];
	int status = pipe(fd1);
	//fork into next process
	int pid = fork();
	//if fork fails
	if (pid < 0) {
		cout << "** error : fork() FAILED in child **\n";
	}
	//to be used by child
	if (pid == 0) {
		grandChild(fd1,arg);
	}
	//read the information passed by grand child and count lines
	else {
		//close write end of fd1 pipe
		close(fd1[1]);
		//redirect STDIN to read end of pipe
		dup2(fd1[0], STDIN_FILENO);
		//close pipe ends
		close(fd1[0]);
		//execute the process wc -l
		execlp("/usr/bin/wc", "wc", "-l", nullptr);
		int donepid = wait(&status);
		assert(donepid == pid);
	}
}

/**
******* main *********
* This is the function that will run when executing this program. This program will
* replicate the :
* "ps -A | grep args[] | wc -l"
* command from a linux shell. The passed in char* argument will be used as the 
* pattern that the grep command will be looking for.
*/
int main (int arg, char* args[]) {
	//isolate the correct part of the args
	char* argument = args[1];
	//fork into child
	int pid = fork();
	//if fork fails
	if (pid < 0) {
		cout << "** error : fork() FAILED in main (parent) **\n";
	}
	//to be used by child
	if (pid == 0) {
		child(argument);
	}
	//wait for all processes to finish
	else {
		wait(NULL);
	}
	return 0;
}


