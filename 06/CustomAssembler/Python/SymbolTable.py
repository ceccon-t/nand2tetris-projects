import parser

class SymbolTable:
    def __init__(self):
        self._table = { 'SP':       0,
                        'LCL':      1,
                        'ARG':      2,
                        'THIS':     3,
                        'THAT':     4,
                        'R0':       0,
                        'R1':       1,
                        'R2':       2,
                        'R3':       3,
                        'R4':       4,
                        'R5':       5,
                        'R6':       6,
                        'R7':       7,
                        'R8':       8,
                        'R9':       9,
                        'R10':      10,
                        'R11':      11,
                        'R12':      12,
                        'R13':      13,
                        'R14':      14,
                        'R15':      15,
                        'SCREEN':   16384,
                        'KBD':      24576
                      }

    def contains(self, symb: str) -> bool:
        return symb in self._table

    def add(self, symb: str, val: int):
        self._table[symb] = val

    def get(self, symb: str) -> int:
        return self._table[symb]


def create_from(instructions: list) -> 'SymbolTable':
    table = SymbolTable()
    next_instruction = 0
    next_memaddr = 16

    # First pass: capture all labels for jumps
    for inst in instructions:
        if parser.is_pseudo(inst):
            symb = parser.get_symbol_from_pseudo(inst)
            if not table.contains(symb):
                table.add(symb, next_instruction)
        else:
            next_instruction += 1

    # Second pass: allocate memory addresses for variables
    for inst in instructions:
        if parser.is_A_type(inst) and parser.contains_symbol(inst):
            symb = parser.get_symbol_from_A_type(inst)
            if not table.contains(symb):
                table.add(symb, next_memaddr)
                next_memaddr += 1

    return table


if __name__ == '__main__':
    st = SymbolTable()
    print(st._table)
    st2 = create_from([])
    print(st2._table)