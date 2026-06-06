int datos[5];

int doble(int x) {
    return x * 2;
}

int main() {
    int i = 0;

    for (i = 0; i < 5; i = i + 1) {
        datos[i] = doble(i);
    }

    do {
        i = i - 1;
    } while (i > 0);

    return datos[4];
}