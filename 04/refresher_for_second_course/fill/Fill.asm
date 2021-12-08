// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.

// Put your code here.

// Initialization
    @SCREEN
    D=A 

    @first
    M=D

    @current
    M=D

    @KBD 
    D=A 

    @last
    M=D

// Check if key is pressed 
(CHECKINPUT)
    // Reinitialize current position
    @SCREEN
    D=A 
    @current 
    M=D

    // Read keyboard
    @KBD
    D=M 

    // If any key pressed, fill with black 
    @LOOPFILLBLACK
    D;JNE

    // Otherwise, fill with white

(LOOPFILLWHITE)
    @current
    D=M 

    // Fill position on screen
    A=D
    M=0

    // Increment position on screen 
    @current
    M=M+1
    D=M

    // Check if ended
    @last 
    D=M-D
    @LOOPFILLWHITE
    D;JNE

    @CHECKINPUT
    0;JMP

//  DIVISION BETWEEN LOOPS

(LOOPFILLBLACK)
    @current
    D=M 

    // Fill position on screen
    A=D
    M=-1

    // Increment position on screen 
    @current
    M=M+1
    D=M

    // Check if ended
    @last 
    D=M-D
    @LOOPFILLBLACK 
    D;JNE

    @CHECKINPUT
    0;JMP

// Here just as precaution, program should never reach this part
(END)
    @END 
    0;JMP
