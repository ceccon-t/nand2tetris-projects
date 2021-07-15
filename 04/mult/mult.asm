// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

// Put your code here.

// Algorithm:
// Let 'n' be the number on R0 and 'base' be the number on R1
// Initialize 'sum' to 0
// And do n times sum = sum + base 

// total_iters = R0 
    @R0
    D=M 
    @total_iters 
    M=D 
// base = R1 
    @R1 
    D=M 
    @base 
    M=D 
// sum = 0
    @sum 
    M=0
// i = 0
    @i
    M=0
// initialize register for result (R2) with 0
    @R2 
    M=0

(LOOP)
    @i 
    D=M 
    
    // while i != total_iters:
    @total_iters 
    D=M-D
    @RESULT
    D;JEQ

    // sum = sum + base 
    @base
    D=M 
    @sum 
    M=M+D 

    // i += 1
    @i 
    M=M+1

    // go to next iteration 
    @LOOP 
    0;JMP

// R2 = sum
(RESULT)
    @sum
    D=M 
    @R2 
    M=D

// infinite loop to finalize program
(END)
    @END
    0;JMP

