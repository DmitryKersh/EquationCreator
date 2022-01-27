# Equation Creator
Позволяет генерировать случайные задания по шаблону.

## Поддерживает следующие объекты:
- Переменная
- Диапазон вещественных чисел с шагом (делителем)
- Диапазон целых чисел
- Список элементов

## Диапазон вещественных чисел с шагом (делителем)
**Описание:**

Режим шага: (через `|`)

- Генерирует случайное вещественное числа от `x` до `y` с шагом `s`, то есть к `x` прибавляется случайное число шагов 
  `s`, но так, чтобы полученное число лежало в диапазоне от `x` до `y`
- Синтаксис: `[x..y|s]`
- Пример: `[1..1.5|0.11]` может выдать: 1, 1.11, 1.22, 1.33, 1.44

Режим делителя: (через `|:`)

- Генерирует случайное вещественное число от `x` до `y`, нацело делящееся на `d`. При этом `d` может быть и не целым 
- Синтаксис: `[x..y|:d]`
- Пример: `[1..1.5|:0.11]` может выдать: 1.1, 1.21, 1.32, 1.43

## Диапазон целых чисел
**Описание:**

- Упрощенная версия предыдущего, не требует третьего аргумента и поддерживает только целые числа.
- Генерирует целые числа от `x` до `y`
- Синтаксис: `[x..y]`
- Пример: `[1..5]` может выдать: 1, 2, 3, 4, 5

## Список
**Описание:**

- Возвращает случайный объект из перечисленных в нем. Вероятности возвращения каждого объекта одинаковы и равны 
  `1/кол-во объектов`
- Объектом могут быть не только последовательности символов, но и элементы синтаксиса: диапазоны любого вида, 
  переменные и даже такие же списки
- Синтаксис: `{obj1|obj2|obj3}` Разделитель: `|`
- Пример: `{123|abc|[1..10]}` выдаст либо `123`, либо `abc`, либо случайное число от 1 до 10

## Арифметика

Часто требуется, например, генерировать числа в границах, определенных записанными в переменные другими числами

**Для использования знаков арифметических действий** `+` `-` `*` `/` `^` использовать префикс `$` (доллар)

**Скобки не требуют префикса**

**Действия отделяются от операндов пробелами**

Пример: `2 $+ 3` выдаст `5`, а `2$+3` останется как есть

Пример: `[1..10] $+ [1..20]` сложит 2 случайных числа: от 1 до 10 и от 1 до 20

## Переменная
**Описание:** 

Позволяет запомнить значение любого объекта (сделано вообще для случайных объектов, но можно 
использовать для любых) для дальнейшего использования в шаблоне.

#### Объявление

Имя может состоять **только** из латинских букв, знака `_` и цифр, но начинаться может только с буквы

Синтаксис объявления: `имяпеременной=<значение переменной>`, например: `a=<[1..20]>` или `sign=<{+|-|*|/}>`

  **ВАЖНО:** ОБЪЯВЛЕНИЕ ПЕРЕМЕННОЙ ЯВЛЯЕТСЯ ЕЕ МОМЕНТАЛЬНЫМ ИСПОЛЬЗОВАНИЕМ, ТО ЕСТЬ НА МЕСТО ОБЪЯВЛЕНИЯ ТАКЖЕ 
  ПОДСТАВЛЯЕТСЯ ЕЕ ЗНАЧЕНИЕ

Значением переменной могут быть любые комбинации любых объектов, в том числе и другие переменные: `a=<using another 
variable: <b>>` при условии что они (здесь `b`) объявлены. Если `b` = `12345` то `a` = `using another
variable: 12345`

#### Использование

Синтаксис использования: Как уже было видно в примере, синтаксис: `<имяпеременной>`

Вместо `<имяпеременной>` подставляется значение этой переменной

Если переменная является арифметическим знаком `+` `-` `*` `/`, можно получить обратный знак: `<!имяпеременной>`

Пример: если `sign=<+>` то `5 $<!sign> 1` выдаст `4`

*Для не-знака это никак не повлияет на значение переменной:* если `var=<abc>` то `<!var>` выдаст `abc`

## Экранирование

Чтобы Equation Creator рассматривал спецсимволы как обычные, их надо экранировать

Экранируются с помощью '\' следующие символы:

`{`,`}`,`\[`,`]`,`<`,`>`,`(`,`)`,`\`,`|`

#### Пример:

`{a|b}` -> `a` или `b`

`\{a\|b\}` -> `{a|b}`

## Порядок парсинга

#### Стадия 1 (переменные)

*Так как объявления переменных могут содержать использования других переменных, то эта стадия зациклена до тех пор, 
пока не останется ни объявлений, ни использований*

1. Объявления переменных. Содержание треугольных `< >` скобок обрабатывается отдельным обработчиком как полноправная 
   строка формата, т.е. внутри нее сразу проходят обе стадии. Это позволяет инициализировать все переменные в 
   самую первую очередь
2. Использования переменных.

#### Стадия 2 (случайные объекты и арифметика)

*Так как объекты разных типов могут быть (так же как и переменные) вложены, эта стадия также зациклена*

1. Вещественные диапазоны
2. Целочисленные диапазоны
3. **Арифметические действия**
4. Списки

## Параметры командной строки

- `-o "path"`, `-out "path"` - изменить имя файла вывода (default: output.txt)
- `-help` - вывести краткую справку
- `-outputformat "format"` - использовать собственное форматирование вывода (подробнее см. ниже)

## OutputFormat

При необходимости, можно выводить сгенерированные задания в своем формате: несколько в строке, с указанием номера, и т.д.

Формат указывается как параметр `format` при `-outputformat`. Используются плейсхолдеры `{TASK}` (задание) и `{NUM}` (номер строки)

Пример (5 LaTex-дробей в строку)

`\\frac\{[10..100]\}\{[10..100]\}\\ ;\\ \\frac\{[10..100]\}\{[10..100]\}\\ ;\\ \\frac\{[10..100]\}\{[10..100]\}\\ ;\\ \\frac\{[10..100]\}\{[10..100]\}\\ ;\\ \\frac\{[10..100]\}\{[10..100]\}\\ ;\\ \\frac\{[10..100]\}\{[10..100]\}\\ ;\\\\`

Используя `-outputformat "{TASK};\ {TASK};\ {TASK};\ {TASK};\ {TASK};\\"`:

`\\\\frac\{[10..100]\}\{[10..100]\}`

# Примеры работы: 
### Простейший пример
#### Ввод

    [1..1000] {+|-} [5000..6000] = 

#### Возможный вывод

    234 + 5218
    492 - 5111


### Линейное уравнение с целыми положительными решениями
#### Ввод

    a=<[100..500|:b=<[5..80|:c=<[5..30]>]>]> sign=<{+|-|/|*}> {x|y|z|w|t|h} = (<a> $<sign> <b>) $signtwo=<{+|-|*|/}> <c> <!signtwo> <c>

#### Возможный вывод

    340 - h = 289 - 17
    424 * w = 27136 / 8
    285 + h = 18 * 19
    450 * y = 22475 + 25
