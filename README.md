from pathlib import Path

readme = r"""# Proyecto Compiladores II - Mini-C Compiler

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
## Codigo intermedio TAC

El proyecto tambien incluye una etapa inicial de generacion de codigo intermedio TAC, tambien conocido como codigo de tres direcciones.

Esta etapa se implementa mediante las clases:

```text
src/main/java/compiler/ir/TacInstruction.java
src/main/java/compiler/ir/TacGenerator.java
```

## Optimizacion de codigo intermedio TAC

El proyecto incluye una etapa inicial de optimizacion sobre el codigo intermedio TAC. Esta etapa se implementa mediante la clase:

```text
src/main/java/compiler/ir/TacOptimizer.java
```

El optimizador recibe la lista de instrucciones TAC generadas por `TacGenerator` y produce una version optimizada del codigo intermedio.

Actualmente se implementan dos optimizaciones principales:

```text
constant folding
constant propagation
```

### Constant folding

El constant folding permite evaluar operaciones constantes durante la compilacion, sin esperar a la ejecucion del programa.

Por ejemplo, el TAC original:

```text
t1 = 3 * 4
```

se optimiza como:

```text
t1 = 12
```

Tambien se optimizan comparaciones constantes:

```text
t5 = 8 > 3
```

se convierte en:

```text
t5 = true
```

### Constant propagation

El constant propagation permite reutilizar valores constantes ya calculados y propagarlos en instrucciones posteriores.

Por ejemplo, el TAC original:

```text
t1 = 12
t2 = 2 + t1
x = t2
```

se optimiza como:

```text
t1 = 12
t2 = 14
x = 14
```

Esto permite reducir operaciones innecesarias y simplificar el codigo intermedio antes de una futura generacion de codigo ensamblador.

## Ejecutar TAC optimizado

Para generar el TAC normal y el TAC optimizado de un archivo Mini-C, se usa:

```powershell
.\gradlew.bat run --args="src/main/resources/examples/correct/programa_optimizacion.mc --tac-opt"
```

Ejemplo de TAC normal:

```text
func main:
decl int x
t1 = 3 * 4
t2 = 2 + t1
x = t2
decl int y
t3 = 10 - 5
t4 = t3 * 2
y = t4
decl bool z
t5 = 8 > 3
z = t5
t6 = x + y
return t6
endfunc main
```

Ejemplo de TAC optimizado:

```text
func main:
decl int x
t1 = 12
t2 = 14
x = 14
decl int y
t3 = 5
t4 = 10
y = 10
decl bool z
t5 = true
z = true
t6 = 24
return 24
endfunc main
```

## Pruebas de TAC optimizado

El proyecto incluye un comando automatico para probar la generacion de TAC optimizado:

```powershell
.\gradlew.bat run --args="--tac-opt-test"
```

Este comando ejecuta el archivo de prueba de optimizacion, valida que pase el analisis lexico, sintactico y semantico, y luego genera tanto el TAC normal como el TAC optimizado.

El resultado esperado es:

```text
Pruebas TAC optimizado correctas: 1
Pruebas TAC optimizado fallidas: 0
BUILD SUCCESSFUL
```

Con esto, el proyecto demuestra que no solo genera codigo intermedio, sino que tambien aplica optimizaciones basicas antes de una futura etapa de generacion de codigo MIPS32.

Generacion de codigo intermedio TAC.
Optimizacion de TAC.
Constant folding.
Constant propagation.
Pruebas automaticas para TAC optimizado.