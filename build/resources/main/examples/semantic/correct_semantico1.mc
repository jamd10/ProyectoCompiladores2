int suma(int a, int b) {
    int resultado = a + b;
    return resultado;
}

void mostrar(int valor) {
    print_str("Resultado: ");
    print_int(valor);
    println();
}

int main() {
    int x = 10;
    int y = 20;
    int z = suma(x, y);

    mostrar(z);

    return z;
}