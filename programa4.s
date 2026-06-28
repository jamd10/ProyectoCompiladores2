.data
# datos globales y literales de cadena
_str_newline: .asciiz "\n"
_str_true: .asciiz "true"
_str_false: .asciiz "false"
_str_lit_1: .asciiz "a["
_str_lit_2: .asciiz "] = "
_str_lit_3: .asciiz "Gracias por usar Mini-C!\n"
.align 2
m: .space 264
a: .space 204

.text
.globl main

fill:
# prologo abi o32
addiu $sp, $sp, -64
sw $ra, 60($sp)
sw $fp, 56($sp)
move $fp, $sp
sw $a0, 0($fp)
sw $a1, 4($fp)
li $t0, 1
sw $t0, 16($fp)
lw $t0, 0($fp)
sw $t0, 8($fp)
L_for_1:
lw $t0, 8($fp)
li $t1, 1
slt $t2, $t0, $t1
xori $t2, $t2, 1
sw $t2, 20($fp)
lw $t0, 20($fp)
beq $t0, $zero, L_endfor_2
lw $t0, 4($fp)
sw $t0, 12($fp)
L_for_3:
lw $t0, 12($fp)
li $t1, 1
slt $t2, $t0, $t1
xori $t2, $t2, 1
sw $t2, 24($fp)
lw $t0, 24($fp)
beq $t0, $zero, L_endfor_4
lw $t0, 0($fp)
lw $t1, 4($fp)
subu $t2, $t0, $t1
sw $t2, 28($fp)
lw $t0, 16($fp)
lw $t1, 28($fp)
addu $t2, $t0, $t1
sw $t2, 32($fp)
lw $t0, 32($fp)
li $t1, 5
addu $t2, $t0, $t1
sw $t2, 36($fp)
lw $t0, 36($fp)
li $t1, 15
div $t0, $t1
mfhi $t2
sw $t2, 40($fp)
lw $t0, 40($fp)
la $t9, m
lw $t8, 8($fp)
li $t7, 6
mul $t8, $t8, $t7
lw $t7, 12($fp)
addu $t8, $t8, $t7
sll $t8, $t8, 2
addu $t9, $t9, $t8
sw $t0, 0($t9)
lw $t0, 16($fp)
li $t1, 1
addu $t2, $t0, $t1
sw $t2, 44($fp)
lw $t0, 44($fp)
sw $t0, 16($fp)
lw $t0, 12($fp)
li $t1, 1
subu $t2, $t0, $t1
sw $t2, 48($fp)
lw $t0, 48($fp)
sw $t0, 12($fp)
j L_for_3
L_endfor_4:
lw $t0, 8($fp)
li $t1, 1
subu $t2, $t0, $t1
sw $t2, 52($fp)
lw $t0, 52($fp)
sw $t0, 8($fp)
j L_for_1
L_endfor_2:
_end_fill:
# epilogo abi o32
lw $ra, 60($fp)
lw $fp, 56($fp)
addiu $sp, $sp, 64
jr $ra

main:
# prologo abi o32
addiu $sp, $sp, -64
sw $ra, 60($sp)
sw $fp, 56($sp)
move $fp, $sp
li $t0, 10
sw $t0, 8($fp)
li $t0, 5
sw $t0, 12($fp)
li $t0, 1
sw $t0, 16($fp)
li $t0, 50
sw $t0, 20($fp)
li $a0, 10
li $a1, 5
jal fill
li $t0, 1
sw $t0, 16($fp)
li $t0, 1
sw $t0, 0($fp)
L_for_5:
lw $t0, 0($fp)
lw $t1, 8($fp)
slt $t2, $t1, $t0
xori $t2, $t2, 1
sw $t2, 24($fp)
lw $t0, 24($fp)
beq $t0, $zero, L_endfor_6
li $t0, 1
sw $t0, 4($fp)
L_for_7:
lw $t0, 4($fp)
lw $t1, 12($fp)
slt $t2, $t1, $t0
xori $t2, $t2, 1
sw $t2, 28($fp)
lw $t0, 28($fp)
beq $t0, $zero, L_endfor_8
la $t9, m
lw $t8, 0($fp)
li $t7, 6
mul $t8, $t8, $t7
lw $t7, 4($fp)
addu $t8, $t8, $t7
sll $t8, $t8, 2
addu $t9, $t9, $t8
lw $t0, 0($t9)
la $t9, a
lw $t8, 16($fp)
sll $t8, $t8, 2
addu $t9, $t9, $t8
sw $t0, 0($t9)
lw $t0, 16($fp)
li $t1, 1
addu $t2, $t0, $t1
sw $t2, 32($fp)
lw $t0, 32($fp)
sw $t0, 16($fp)
lw $t0, 4($fp)
li $t1, 1
addu $t2, $t0, $t1
sw $t2, 36($fp)
lw $t0, 36($fp)
sw $t0, 4($fp)
j L_for_7
L_endfor_8:
lw $t0, 0($fp)
li $t1, 1
addu $t2, $t0, $t1
sw $t2, 40($fp)
lw $t0, 40($fp)
sw $t0, 0($fp)
j L_for_5
L_endfor_6:
li $t0, 1
sw $t0, 16($fp)
L_while_9:
lw $t0, 20($fp)
li $t1, 1
addu $t2, $t0, $t1
sw $t2, 44($fp)
lw $t0, 16($fp)
lw $t1, 44($fp)
subu $t2, $t0, $t1
sltu $t2, $zero, $t2
sw $t2, 48($fp)
lw $t0, 48($fp)
beq $t0, $zero, L_endwhile_10
la $a0, _str_lit_1
jal print_str
lw $a0, 16($fp)
jal print_int
la $a0, _str_lit_2
jal print_str
la $t9, a
lw $t8, 16($fp)
sll $t8, $t8, 2
addu $t9, $t9, $t8
lw $a0, 0($t9)
jal print_int
jal println
lw $t0, 16($fp)
li $t1, 1
addu $t2, $t0, $t1
sw $t2, 52($fp)
lw $t0, 52($fp)
sw $t0, 16($fp)
j L_while_9
L_endwhile_10:
la $a0, _str_lit_3
jal print_str
li $v0, 0
j _end_main
_end_main:
# epilogo abi o32
li $v0, 10
syscall

# runtime minimo para qtspim/mars
print_int:
li $v0, 1
syscall
jr $ra

print_char:
li $v0, 11
syscall
jr $ra

print_bool:
li $v0, 1
syscall
jr $ra

print_str:
li $v0, 4
syscall
jr $ra

println:
la $a0, _str_newline
li $v0, 4
syscall
jr $ra

read_int:
li $v0, 5
syscall
jr $ra

read_char:
li $v0, 12
syscall
jr $ra

read_str:
li $v0, 8
syscall
jr $ra
