# Proyecto Compiladores II - Mini-C Compiler

## Descripcion

Este proyecto implementa la Fase 1 y el inicio de la Fase 2 de un compilador para el lenguaje Mini-C usando ANTLR4 y Java.

La fase actual incluye el front-end del compilador y un analisis semantico basico.

El proyecto incluye:

* Analisis lexico.
* Analisis sintactico.
* Gramatica ANTLR4 en `MiniC.g4`.
* Lectura de archivos `.mc` por consola.
* Impresion de tokens.
* Resumen lexico.
* Resumen sintactico.
* Parse tree generado por ANTLR.
* Recorrido del arbol usando Visitor.
* AST textual generado desde el Visitor.
* Tabla de simbolos inicial.
* Analisis semantico basico.
* Reporte de errores con linea y columna.
* Casos de prueba lexicos, sintacticos y semanticos.

## Tecnologias utilizadas

* Java
* Gradle
* ANTLR4
* IntelliJ IDEA

## Estructura del proyecto

```text
Proyecto_compiladores_II_V2/
├─ build.gradle
├─ settings.gradle
├─ gradlew
├─ gradlew.bat
└─ src/
   └─ main/
      ├─ antlr/
      │  └─ MiniC.g4
      ├─ java/
      │  └─ compiler/
      │     ├─ Main.java
      │     ├─ SyntaxErrorListener.java
      │     ├─ analysis/
      │     │  ├─ LexicalSummary.java
      │     │  └─ SyntaxSummaryVisitor.java
      │     ├─ ast/
      │     │  └─ AstPrinterVisitor.java
      │     └─ semantic/
      │        ├─ SemanticAnalyzer.java
      │        ├─ SymbolEntry.java
      │        ├─ SymbolTable.java
      │        └─ SymbolTableBuilder.java
      └─ resources/
         └─ examples/
            ├─ correct/
            │  ├─ programa1.mc
            │  ├─ programa2.mc
            │  ├─ programa3.mc
            │  └─ programa4.mc
            ├─ errors/
            │  ├─ error_lexico.mc
            │  ├─ error_sintactico1.mc
            │  ├─ error_sintactico2.mc
            │  └─ error_comentario_bloque.mc
            └─ semantic/
               ├─ correct_semantico1.mc
               ├─ correct_semantico2.mc
               ├─ error_variable_no_declarada.mc
               ├─ error_redeclaracion.mc
               ├─ error_funcion_no_declarada.mc
               ├─ error_parametros.mc
               ├─ error_tipo_asignacion.mc
               ├─ error_indice_arreglo.mc
               ├─ error_return_tipo.mc
               ├─ error_void_retorna_valor.mc
               ├─ error_funcion_sin_return.mc
               ├─ error_parametro_tipo.mc
               ├─ error_variable_como_funcion.mc
               ├─ error_funcion_como_variable.mc
               └─ error_demasiados_indices.mc
```

## Gramatica MiniC.g4

El archivo principal de la gramatica se encuentra en:

```text
src/main/antlr/MiniC.g4
```

Este archivo define las reglas lexicas y sintacticas del lenguaje Mini-C.

La gramatica reconoce:

* Declaraciones globales.
* Funciones.
* Parametros.
* Variables.
* Arreglos de una o dos dimensiones.
* Bloques.
* Sentencias `if`, `while`, `for`, `do while`.
* Sentencias `return`.
* Expresiones aritmeticas.
* Expresiones relacionales.
* Expresiones logicas.
* Asignaciones.
* Llamadas a funciones.
* Literales enteros, char, string, true y false.
* Comentarios de linea.
* Comentarios de bloque.

Tambien se incluyen reglas para detectar errores lexicos como:

* Simbolo no reconocido.
* String sin cerrar.
* Char sin cerrar.
* Char invalido.
* Comentario de bloque sin cerrar.

## Compilar el proyecto

Desde la raiz del proyecto, ejecutar:

```powershell
.\gradlew.bat clean generateGrammarSource
```

Luego:

```powershell
.\gradlew.bat clean build
```

Si todo esta correcto, debe aparecer:

```text
BUILD SUCCESSFUL
```

## Ejecutar todas las pruebas de Fase 1

Para ejecutar los casos de prueba automaticos de lexico y sintaxis:

```powershell
.\gradlew.bat run --args="--test"
```

El resultado esperado es:

```text
Pruebas fallidas: 0
BUILD SUCCESSFUL
```

## Ejecutar pruebas semanticas

Para ejecutar las pruebas automaticas del analisis semantico:

```powershell
.\gradlew.bat run --args="--semantic-test"
```

El resultado esperado es:

```text
Pruebas semanticas fallidas: 0
BUILD SUCCESSFUL
```

Actualmente se prueban programas semanticos correctos y programas con errores semanticos esperados.

## Ejecutar un archivo Mini-C

Para analizar un archivo `.mc`:

```powershell
.\gradlew.bat run --args="src/main/resources/examples/correct/programa1.mc"
```

Para mostrar toda la informacion del analisis:

```powershell
.\gradlew.bat run --args="src/main/resources/examples/correct/programa1.mc --all"
```

## Opciones disponibles

El programa puede ejecutarse con diferentes opciones:

```powershell
.\gradlew.bat run --args="ruta/archivo.mc --tokens"
```

Muestra los tokens reconocidos por el analizador lexico.

```powershell
.\gradlew.bat run --args="ruta/archivo.mc --lexical-summary"
```

Muestra el resumen lexico.

```powershell
.\gradlew.bat run --args="ruta/archivo.mc --syntax-summary"
```

Muestra el resumen sintactico.

```powershell
.\gradlew.bat run --args="ruta/archivo.mc --ast"
```

Muestra el AST textual generado mediante Visitor.

```powershell
.\gradlew.bat run --args="ruta/archivo.mc --symbols"
```

Muestra la tabla de simbolos.

```powershell
.\gradlew.bat run --args="ruta/archivo.mc --tree"
```

Muestra el parse tree generado por ANTLR.

```powershell
.\gradlew.bat run --args="ruta/archivo.mc --all"
```

Muestra toda la informacion disponible.

```powershell
.\gradlew.bat run --args="--test"
```

Ejecuta las pruebas automaticas de analisis lexico y sintactico.

```powershell
.\gradlew.bat run --args="--semantic-test"
```

Ejecuta las pruebas automaticas del analisis semantico.

## Analisis lexico

El analizador lexico reconoce los tokens definidos en `MiniC.g4`.

Entre los tokens principales estan:

* Palabras reservadas: `int`, `char`, `bool`, `void`, `string`, `if`, `else`, `while`, `for`, `do`, `return`, `true`, `false`.
* Operadores: `+`, `-`, `*`, `/`, `%`, `=`, `==`, `!=`, `<`, `<=`, `>`, `>=`, `&&`, `||`, `!`, `&`.
* Delimitadores: `;`, `,`, `(`, `)`, `{`, `}`, `[`, `]`.
* Identificadores.
* Constantes enteras.
* Literales `char`.
* Literales `string`.

El analizador reporta errores lexicos con linea y columna.

Ejemplo:

```text
Error lexico linea 3:4 - comentario de bloque sin cerrar
```

## Analisis sintactico

El analizador sintactico se genera a partir de la gramatica `MiniC.g4`.

El parser reconoce la estructura de un programa Mini-C, incluyendo declaraciones, funciones, bloques, sentencias y expresiones.

Cuando existe un error sintactico, se muestra un mensaje con linea y columna.

Ejemplo:

```text
Error linea 2:14 - missing ';' at 'return'
```

## Visitor y AST textual

ANTLR genera infraestructura para recorrer el arbol mediante Listener y Visitor.

En este proyecto se utiliza Visitor mediante la clase:

```text
AstPrinterVisitor.java
```

Esta clase recorre el arbol generado por ANTLR y construye una representacion textual mas clara del programa.

Ejemplo de salida:

```text
Program
  Function: suma
    Return type: int
    Signature: int suma(int a, int b)
    Parameters
      int a
      int b
    Block
      Declaration: int resultado
        Init: a+b
      Return: resultado
```

Esto demuestra que el proyecto no depende unicamente del arbol visual del plugin de ANTLR, sino que recorre el arbol mediante codigo propio.

## Tabla de simbolos

El proyecto incluye una tabla de simbolos inicial construida mediante Visitor.

La tabla muestra:

* ID
* Tipo
* Clase
* Ambito
* Nivel
* Linea
* Firma

Ejemplo:

```text
ID              TIPO         CLASE        AMBITO                    NIVEL    LINEA      FIRMA
suma            int          FUNCTION     global                    0        1:4        int suma(int a, int b)
a               int          PARAMETER    global/suma               1        1:13       int a
b               int          PARAMETER    global/suma               1        1:20       int b
resultado       int          VARIABLE     global/suma/block1        2        2:8        int resultado
```

La tabla permite visualizar la estructura de ambitos del programa y prepara la base para el analisis semantico.

## Funciones runtime

Se agregan funciones de runtime a la tabla de simbolos para representar funciones de entrada y salida usadas por Mini-C:

```text
print_int
print_char
print_bool
print_str
println
read_int
read_char
read_str
```

Estas funciones se registran en el ambito global.

## Analisis semantico

El proyecto incluye una implementacion inicial del analisis semantico mediante la clase:

```text
SemanticAnalyzer.java
```

Esta clase recorre el arbol usando Visitor y valida reglas semanticas del lenguaje Mini-C.

Actualmente el analizador semantico valida:

* Variables no declaradas.
* Redeclaracion de identificadores en el mismo ambito.
* Funciones no declaradas.
* Cantidad incorrecta de parametros en llamadas a funciones.
* Tipos incompatibles en asignaciones.
* Indices de arreglos que no son `int`.
* Return incompatible con el tipo de la funcion.
* Funciones `void` que retornan un valor.
* Funciones no `void` sin sentencia `return`.
* Parametros con tipo incorrecto.
* Variables usadas como si fueran funciones.
* Funciones usadas como si fueran variables.
* Uso de demasiados indices en arreglos.

Ejemplo de error semantico:

```text
Error semantico linea 2:4 - identificador no declarado: x
```

Otro ejemplo:

```text
Error semantico linea 2:4 - return incompatible. Se esperaba int y se obtuvo string
```

## Pruebas semanticas

El proyecto incluye pruebas semanticas correctas y pruebas con errores esperados.

### Programas semanticamente correctos

```text
src/main/resources/examples/semantic/correct_semantico1.mc
src/main/resources/examples/semantic/correct_semantico2.mc
```

Estos programas comprueban que el analizador semantico acepte programas validos sin reportar errores.

### Programas con errores semanticos

```text
src/main/resources/examples/semantic/error_variable_no_declarada.mc
src/main/resources/examples/semantic/error_redeclaracion.mc
src/main/resources/examples/semantic/error_funcion_no_declarada.mc
src/main/resources/examples/semantic/error_parametros.mc
src/main/resources/examples/semantic/error_tipo_asignacion.mc
src/main/resources/examples/semantic/error_indice_arreglo.mc
src/main/resources/examples/semantic/error_return_tipo.mc
src/main/resources/examples/semantic/error_void_retorna_valor.mc
src/main/resources/examples/semantic/error_funcion_sin_return.mc
src/main/resources/examples/semantic/error_parametro_tipo.mc
src/main/resources/examples/semantic/error_variable_como_funcion.mc
src/main/resources/examples/semantic/error_funcion_como_variable.mc
src/main/resources/examples/semantic/error_demasiados_indices.mc
```

Estos programas comprueban que el analizador semantico detecte errores de uso del lenguaje que no necesariamente son errores lexicos o sintacticos.

## Casos de prueba

El proyecto incluye programas correctos y programas con errores.

### Programas correctos

```text
src/main/resources/examples/correct/programa1.mc
src/main/resources/examples/correct/programa2.mc
src/main/resources/examples/correct/programa3.mc
src/main/resources/examples/correct/programa4.mc
```

Estos programas prueban:

* Funciones.
* Parametros.
* Variables.
* Arreglos.
* Matrices.
* For.
* While.
* If / else.
* Llamadas a funciones.
* Operadores aritmeticos.
* Operadores relacionales.
* Strings.

### Programas con errores lexicos y sintacticos

```text
src/main/resources/examples/errors/error_lexico.mc
src/main/resources/examples/errors/error_sintactico1.mc
src/main/resources/examples/errors/error_sintactico2.mc
src/main/resources/examples/errors/error_comentario_bloque.mc
```

Estos programas prueban:

* Simbolos no reconocidos.
* Errores sintacticos por falta de simbolos.
* Estructuras mal formadas.
* Comentarios de bloque sin cerrar.

## Ejemplo de ejecucion

Comando:

```powershell
.\gradlew.bat run --args="src/main/resources/examples/correct/programa4.mc --all"
```

Salida esperada:

```text
================ TOKENS ================
...

================ RESUMEN LEXICO ================
Errores lexicos: 0

================ RESUMEN SINTACTICO ================
Funciones: 2
Arreglos: 2
While: 1
For: 4
Llamadas: 7

================ AST / RECORRIDO CON VISITOR ================
Program
  Declaration: int[10][5] m
  Declaration: int[50] a
  Function: fill
  Function: main

--------------------------------- TABLA DE SIMBOLOS ---------------------------------
...
```

## Ejemplo de prueba semantica

Comando:

```powershell
.\gradlew.bat run --args="--semantic-test"
```

Salida esperada:

```text
================ EJECUCION DE PRUEBAS SEMANTICAS ================

================ PROGRAMAS SEMANTICAMENTE CORRECTOS ================
src/main/resources/examples/semantic/correct_semantico1.mc                 OK
src/main/resources/examples/semantic/correct_semantico2.mc                 OK

================ PROGRAMAS CON ERRORES SEMANTICOS ================
src/main/resources/examples/semantic/error_variable_no_declarada.mc        OK
...
Pruebas semanticas fallidas: 0
BUILD SUCCESSFUL
```

## Recuperacion de errores

El proyecto implementa recuperacion y manejo controlado de errores para evitar que el compilador falle de forma abrupta ante entradas incorrectas.

En la parte sintactica, se utiliza el mecanismo de recuperacion de errores que proporciona ANTLR4 junto con un listener personalizado. Esto permite que, cuando existe un error en la estructura del programa, el parser reporte la linea, columna y descripcion del problema. Por ejemplo, si falta un punto y coma, el analizador puede mostrar un mensaje como:

```text
Error linea 3:4 - missing ';' at 'return'
```

Tambien puede reportar errores cuando encuentra simbolos validos en una posicion incorrecta, por ejemplo una llave `{` donde se esperaba cerrar un parentesis `)`.

En la parte lexica, la gramatica incluye reglas especificas para detectar errores comunes de forma controlada. Entre ellos se encuentran:

```text
Simbolo no reconocido
String sin cerrar
Char sin cerrar
Literal char invalido
Comentario de bloque sin cerrar
```

Por ejemplo, si el codigo contiene un simbolo que no pertenece al lenguaje:

```c
int main() {
    int x = 10 @ 2;
    return x;
}
```

El lexer lo detecta y reporta:

```text
Error lexico linea 2:15 - simbolo no reconocido: @
```

De igual forma, si existe un comentario de bloque sin cerrar:

```c
int main() {
    int x = 10;
    /* comentario sin cerrar
    return x;
}
```

El analizador reporta:

```text
Error lexico linea 3:4 - comentario de bloque sin cerrar
```

Con esto, el compilador cumple con una recuperacion de errores razonable, ya que identifica errores lexicos y sintacticos, muestra mensajes claros con linea y columna, y evita que el programa termine sin una explicacion util para el usuario.

## Estado actual del proyecto

El proyecto cuenta actualmente con:

* Gramatica ANTLR4 funcional.
* Analisis lexico.
* Analisis sintactico.
* Manejo de errores con linea y columna.
* Visitor para recorrer el arbol.
* AST textual.
* Tabla de simbolos inicial.
* Analisis semantico basico.
* Pruebas automaticas de Fase 1.
* Pruebas automaticas semanticas.

## Siguiente fase

La siguiente etapa del proyecto corresponde a la generacion de codigo intermedio y posteriormente a la generacion de codigo MIPS32.

Las mejoras futuras incluyen:

* Generar codigo intermedio TAC.
* Aplicar optimizaciones basicas sobre TAC.
* Generar codigo MIPS32.
* Probar programas generados en un simulador MIPS.
