int main() {
    int i = 0;
    int suma = 0;

    while (i < 5) {
        suma = suma + i;
        i = i + 1;
    }

    if (suma >= 10) {
        print_int(suma);
    } else {
        print_int(0);
    }

    return suma;
}