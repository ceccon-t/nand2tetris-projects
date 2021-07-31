import sys
import parser
import SymbolTable

# Helper functions (might be moved from here soon)
def dessymbolize(instructions: list) -> list:
    dessymbolized = []
    for inst in instructions:
        if parser.is_pseudo(inst):
            continue
        if parser.is_A_type(inst) and parser.contains_symbol(inst):
            dessymbolized.append('@' + str(symbols.get(parser.get_symbol_from_A_type(inst))))
        else:
            dessymbolized.append(inst)
    return dessymbolized

def binarize(instructions: list) -> list:
    binaries = []
    for inst in instructions:
        typed_inst = parser.get_typed_inst(inst)
        binaries.append(typed_inst.get_binary())
    return binaries

# ASSEMBLY PROCESS

# Read input file
path_source = sys.argv[1]
path_output = path_source.replace('.asm', '.hack')

print("Assembling program:", path_source)

with open(path_source) as f:
    lines = f.readlines()

# Generate structures
instructions = [parser.sanitize(line) for line in lines if parser.is_instruction(line)]
symbols = SymbolTable.create_from(instructions)

# Convert instructions to binary
symbolless_instructions = dessymbolize(instructions)
binary_instructions = binarize(symbolless_instructions)

# Write output file
print("Creating binary:", path_output)
with open(path_output, 'w') as f:
    for instruction in binary_instructions:
        f.write(instruction + '\n')

print("Assemblage finished.")