// Program: Signum.asm
// Computes: if R0 > 0
//              R1 = 1
//           else
//              R1 = 0

  @R0
  D=M       // D = RAM[0]

  @8
  D;JGT     // IF R0>0 goto 8

  @R1 
  M=0       // RAM[1] = 0
  @10
  0;JMP     // finished

  @R1 
  M=1       // R1  = 1, finished 

  @10
  0;JMP     // infinite loop, end of program
