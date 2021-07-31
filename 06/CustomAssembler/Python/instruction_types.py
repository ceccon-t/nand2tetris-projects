import translator

class Instruction:
    def __init__(self, raw: str):
        self._raw = raw

    def get_binary(self) -> str:
        return self._raw

class A_Type(Instruction):
    def __init__(self, raw: str):
        super().__init__(raw)

    def get_binary(self) -> str:
        value_in_decimal = int(self._raw[1:])
        value_in_binary = f'{value_in_decimal:015b}'
        return '0' + value_in_binary


class C_Type(Instruction):
    def __init__(self, raw: str):
        super().__init__(raw)
        #_dest = raw.split('=')[0]
        #_jump = raw.split(';')[1]
        #_comp = raw.replace((_dest + '=', ';' + _jump), '')
        self._dest = self.get_dest(raw)
        self._jump = self.get_jump(raw)
        self._comp = self.get_comp(raw)

    def get_original(self) -> str:
        d = f'{self._dest}=' if self._dest != '' else ''
        j = f';{self._jump}' if self._jump != '' else ''
        return f'{d}{self._comp}{j}'

    def get_dest(self, inst: str) -> str:
        separated = inst.split('=')
        if len(separated) > 1:
            return separated[0]
        else:
            return ''

    def get_jump(self, inst: str) -> str:
        separated = inst.split(';')
        if len(separated) > 1:
            return separated[1]
        else:
            return ''

    def get_comp(self, inst: str) -> str:
        prefix = f'{self.get_dest(inst)}='
        suffix = f';{self.get_jump(inst)}'
        return inst.replace(prefix, '').replace(suffix, '')

    def get_binary(self) -> str:
        return f'111{translator.bin_comp(self._comp)}{translator.bin_dest(self._dest)}{translator.bin_jump(self._jump)}'


if __name__ == '__main__':
    a = A_Type('@23')
    print(a.get_binary())
    b = C_Type('D=M')
    print(b.get_original())
    print(b.get_binary())
    c = C_Type('D;JGT')
    print(c.get_original())
    print(c.get_binary())
    d = C_Type('D=D&A;JMP')
    print(d.get_original())
    print(d.get_binary())
    