int matriz[2][2];
int vector[4];

void llenar() {
    int i = 0;
    int j = 0;
    int cont = 1;

    for (i = 0; i < 2; i = i + 1) {
        for (j = 0; j < 2; j = j + 1) {
            matriz[i][j] = cont;
            cont = cont + 1;
        }
    }
}

int main() {
    int i = 0;
    int j = 0;
    int pos = 0;

    llenar();

    for (i = 0; i < 2; i = i + 1) {
        for (j = 0; j < 2; j = j + 1) {
            vector[pos] = matriz[i][j];
            pos = pos + 1;
        }
    }

    print_str("Vector cargado");
    println();

    return vector[0];
}