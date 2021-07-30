import sys
from parser import sanitize, is_instruction, is_pseudo
import SymbolTable

# Read input file
path_source = sys.argv[1]
path_output = path_source.replace('.asm', '.hack')

print("Assembling program:", path_source)

with open(path_source) as f:
    lines = f.readlines()

instructions = [sanitize(line) for line in lines if is_instruction(line)]

# assembly -> binary
symbols = SymbolTable.create_from(instructions)
print(symbols._table)
binary_instructions = instructions


# Write output file
print("Creating binary:", path_output)
with open(path_output, 'w') as f:
    for instruction in binary_instructions:
        f.write(instruction + '\n')

#print(path_src.replace('.asm', '.hack'))
#print(path_output)
#print(contents)

print("Assembly finished.")