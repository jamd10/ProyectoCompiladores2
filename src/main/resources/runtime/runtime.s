.data
_str_newline: .asciiz "\n"
_str_true: .asciiz "true"
_str_false: .asciiz "false"

.text

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
