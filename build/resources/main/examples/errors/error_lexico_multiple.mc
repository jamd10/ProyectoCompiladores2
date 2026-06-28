int main() {
    int x = 10;
    int y = 20;
    int z = 30;

    x = x @ y;
    y = y $ 2;
    x = x ? y;
    z = z # x;
    x = x ~ y;
    y = y ` 3;
    z = z \ x;

    return x;
}