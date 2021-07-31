A custom implementation of the Hack assembler in Python, which does not follow the canon architecture.

The main orchestrator of the process is HackAssembler.py . It uses the other modules and classes to generate a binary .hack file from a valid .asm program.

To run it, just run it with the python interpreter passing the path to the .asm program.

Example, assuming "Add.asm" is a valid .asm program on the same directory as the assembler: `$ python3 HackAssembler.py ./Add.asm`

The assembler will generate a file with the same name as the original, but with the `.hack` extension instead of `.asm`. If a file of the same name and extension already exists, it will be overwritten.

Assumptions:

- The provided .asm file contains a valid, error-free Hack program.
