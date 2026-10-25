#!/usr/bin/env python3
"""Рахує CBO для кожного типу та Ca/Ce/I для пакетів.

Спрощення: зв'язок = у коді типу (без коментарів, рядків та import) зустрічається
ім'я іншого типу проєкту. Типи java.* не враховуються (за методичкою).
У файлі може бути кілька типів (інтерфейс + його реалізації, вкладені enum/record):
кожен рахується окремо, а зв'язок з власним вкладеним чи зовнішнім типом не вважається.
SelfCheck (тести) не рахуємо.

Запуск: python3 docs/metrics.py src   або   python3 docs/metrics.py legacy
"""
import re, sys, pathlib
from collections import defaultdict

root = pathlib.Path(sys.argv[1])
TYPE_RE = re.compile(r'\b(?:class|interface|enum|record)\s+(\w+)')


def clean(src):
    src = re.sub(r'/\*.*?\*/', '', src, flags=re.S)
    src = re.sub(r'//[^\n]*', '', src)
    src = re.sub(r'"(\\.|[^"\\])*"', '""', src)
    src = re.sub(r'^\s*(import|package)\s[^;]*;', '', src, flags=re.M)
    return src


def blocks(code):
    """[(ім'я, початок, кінець)] для кожного оголошеного типу, включно з вкладеними."""
    result = []
    for m in TYPE_RE.finditer(code):
        brace = code.find('{', m.end())
        depth, i = 0, brace
        while i < len(code):
            depth += (code[i] == '{') - (code[i] == '}')
            if depth == 0:
                break
            i += 1
        result.append((m.group(1), m.start(), i + 1))
    return result


types = {}      # ім'я -> (пакет, власний код, множина предків і нащадків)
outer = {}      # ім'я вкладеного типу -> імена типів, у які він вкладений
for f in root.rglob('*.java'):
    if f.stem == 'SelfCheck':
        continue
    text = f.read_text(encoding='utf-8')
    pkg = re.search(r'package\s+([\w.]+);', text).group(1)
    code = clean(text)
    bl = blocks(code)
    for name, s, e in bl:
        children = [(n, cs, ce) for n, cs, ce in bl if (cs, ce) != (s, e) and s <= cs and ce <= e]
        own = code[s:e]
        for _, cs, ce in sorted(children, key=lambda c: -c[1]):
            own = own[:cs - s] + own[ce - s:]
        related = {n for n, cs, ce in children}
        parents = {n for n, ps, pe in bl if (ps, pe) != (s, e) and ps <= s and e <= pe}
        outer[name] = parents
        related |= parents
        types[name] = (pkg, own, related)

cbo = {}
for name, (pkg, code, related) in types.items():
    cbo[name] = {o for o in types
                 if o != name and o not in related and re.search(r'\b' + o + r'\b', code)}

# Room.Category разом з Room це один зв'язок, а не два
for name in cbo:
    cbo[name] = {d for d in cbo[name] if not (outer[d] & cbo[name])}

print(f"{'Тип':28}{'Пакет':24}{'CBO':>4}  залежності")
for name in sorted(types, key=lambda n: (types[n][0], n)):
    print(f"{name:28}{types[name][0]:24}{len(cbo[name]):>4}  {', '.join(sorted(cbo[name]))}")

pk = defaultdict(lambda: [set(), set()])  # пакет -> [Ce, Ca]
for name, deps in cbo.items():
    for d in deps:
        if types[d][0] != types[name][0]:
            pk[types[name][0]][0].add(d)   # Ce: зовнішні типи, від яких залежимо
            pk[types[d][0]][1].add(name)   # Ca: зовнішні типи, які залежать від нас
print()
print(f"{'Пакет':24}{'Ca':>4}{'Ce':>4}{'I':>7}")
for p in sorted(pk):
    ce, ca = len(pk[p][0]), len(pk[p][1])
    print(f"{p:24}{ca:>4}{ce:>4}{(ce / (ca + ce) if ca + ce else 0):>7.2f}")
