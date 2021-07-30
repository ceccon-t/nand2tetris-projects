def remove_whitespace(instruction: str):
    return instruction.strip()


def remove_comments(instruction: str):
    return instruction.split('//')[0]


def sanitize(instruction: str):
    return remove_whitespace(remove_comments(instruction))


def is_instruction(line: str):
    return sanitize(line) != ''


def is_pseudo(inst: str) -> bool:
    return inst.startswith('(')


def get_symbol_from_pseudo(inst: str) -> str:
    return inst.replace('(', '').replace(')', '')


def is_A_type(inst: str) -> bool:
    return inst.startswith('@')


def contains_symbol(inst: str) -> bool:
    value = inst[1:]
    return not value.isnumeric()


def get_symbol_from_A_type(inst: str) -> str:
    return inst[1:]


if __name__ == '__main__':
    print('Test passed.' if remove_whitespace('  Hello    ') == 'Hello' else 'Test 1 failed.')
    print('Test passed.' if remove_comments('// This is a comment') == '' else 'Test 2 failed.')
    print('Test passed.' if remove_comments('@42 // Loads THE answer') == '@42 ' else 'Test 3 failed.')
    print('Test passed.' if sanitize('// This program does nothing') == '' else 'Test 4 failed.')
    print('Test passed.' if sanitize('@42 // Loads THE answer') == '@42' else 'Test 5 failed.')
    print('Test passed.' if sanitize('0;JMP // Goes to the answer') == '0;JMP' else 'Test 6 failed.')
    print('Test passed.' if is_instruction('// This program does nothing') is False else 'Test 7 failed.')
    print('Test passed.' if is_instruction('@42 // Loads THE answer') is True else 'Test 8 failed.')
    print('Test passed.' if is_instruction('0;JMP // Goes to the answer') is True else 'Test 9 failed.')
    