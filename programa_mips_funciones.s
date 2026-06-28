.data
# datos globales y literales de cadena
_str_newline: .asciiz "\n"
_str_true: .asciiz "true"
_str_false: .asciiz "false"
_str_lit_1: .asciiz "resultado = "

.text
.globl main

suma:
# prologo abi o32
addiu $sp, $sp, -24
sw $ra, 20($sp)
sw $fp, 16($sp)
move $fp, $sp
sw $a0, 0($fp)
sw $a1, 4($fp)
lw $t0, 0($fp)
lw $t1, 4($fp)
addu $t2, $t0, $t1
sw $t2, 12($fp)
lw $t0, 12($fp)
sw $t0, 8($fp)
lw $v0, 8($fp)
j _end_suma
_end_suma:
# epilogo abi o32
lw $ra, 20($fp)
lw $fp, 16($fp)
addiu $sp, $sp, 24
jr $ra

main:
# prologo abi o32
addiu $sp, $sp, -24
sw $ra, 20($sp)
sw $fp, 16($sp)
move $fp, $sp
li $t0, 10
sw $t0, 0($fp)
li $t0, 20
sw $t0, 4($fp)
li $a0, 10
li $a1, 20
jal suma
sw $v0, 12($fp)
lw $t0, 12($fp)
sw $t0, 8($fp)
la $a0, _str_lit_1
jal print_str
lw $a0, 8($fp)
jal print_int
jal println
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
