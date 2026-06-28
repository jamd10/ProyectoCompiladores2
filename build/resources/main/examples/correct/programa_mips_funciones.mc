int suma(int a, int b) {
    int r;
    r = a + b;
    return r;
}

int main() {
    int x = 10;
    int y = 20;
    int z;

    z = suma(x, y);

    print_str("resultado = ");
    print_int(z);
    println();

    return 0;
}
