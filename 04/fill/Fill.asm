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

// ====================================
// Init 
    // init fill 
    @fill
    M=0

    // init limit_screen
    @KBD // use the fact that KBD comes right after the end of screen
    D=A 
    @limit_screen 
    M=D-1

// ====================================
// Identify if a key was pressed 
(LOOP)

    // set pos_screen to start of screen
    @SCREEN 
    D=A 
    @pos_screen
    M=D

    @KBD 
    D=M 

    // if keyboard is 0, go to not pressing
    @NOTPRESSING 
    D;JEQ
    // else, go to pressing
    @PRESSING
    0;JMP

// ====================================
// Handle key press 
// Paint screen 
(PRESSING)
    @fill
    M=-1        // fill screen memory map with '1111111111111111' words
    @FILLNEXT 
    0;JMP
(NOTPRESSING)
    @fill
    M=0         // fill screen memory map with '0000000000000000' words
    @FILLNEXT 
    0;JMP
(FILLNEXT)
    // fill current pos
    @fill 
    D=M
    @pos_screen
    A=M         // because 'pos_screen' is a pointer
    M=D         // do the actual filing

    // check if end of screen 
    @pos_screen
    D=M
    @limit_screen 
    A=M
    D=A-D
    //   if yes, go back to main loop 
    @LOOP 
    D;JEQ
    //   if not, increase current pos and fill next
    @pos_screen 
    M=M+1
    @FILLNEXT
    0;JMP 
