# Simulador de la Jerarquía de Chomsky

Aplicación de escritorio en **Java Swing** (sin dependencias externas) con cuatro módulos interactivos, uno por nivel de la Jerarquía de Chomsky.

| Nivel | Modelo | Lenguajes de ejemplo | Visualización |
|---|---|---|---|
| 3 | Autómata Finito Determinista | pares de 1s, termina en `01`, contiene subcadena (parametrizable) | Grafo de estados, estado actual resaltado, cinta de entrada |
| 2 | Autómata de Pila | aⁿbⁿ, w c wᴿ, paréntesis balanceados | Pila LIFO animada, estado, última regla aplicada |
| 1 | Autómata Linealmente Acotado | aⁿbⁿcⁿ | Cinta `⊢ w ⊣` de tamaño fijo, cabezal bidireccional |
| 0 | Máquina de Turing | aⁿbⁿ, sucesor unario, suma unaria, incremento binario | Cinta extendible, alfabeto Γ, cabezal, estado activo |

## Requisitos
- JDK 17 o superior (`javac -version`).

## Compilar y ejecutar
```bash
git clone <URL-DEL-REPOSITORIO>
cd chomsky-simulator
mkdir out
javac -encoding UTF-8 -d out src/*.java
java -cp out Main
```
En Windows (PowerShell/CMD) los mismos comandos funcionan; use `mkdir out` y `java -cp out Main`.

## Uso
1. En el menú principal elija un módulo.
2. Escriba una cadena o elija una de la lista **Ejemplos** (cada ejemplo indica si debe ser aceptada o rechazada; `ε` = cadena vacía).
3. **Paso**: avanza una transición. **Ejecutar**: avanza automáticamente (control de **Velocidad**). **Pausa** y **Reiniciar**.
4. El panel inferior muestra la bitácora de transiciones y el veredicto final (verde = aceptada, rojo = rechazada).

## Cadenas de prueba (también incluidas en la lista Ejemplos de la interfaz)

**Nivel 3 – AFD**
- Par de 1s: `1010` ✔, `0000` ✔, `ε` ✔, `111` ✘, `10101` ✘
- Termina en 01: `1101` ✔, `01` ✔, `10` ✘, `0100` ✘
- Contiene subcadena (parámetro por defecto `abb`): `abb` ✔, `aabba`… ✔, `ab` ✘. Cambie el campo de subcadena y pulse *Aplicar parámetro*.

**Nivel 2 – Autómata de Pila**
- aⁿbⁿ: `aaabbb` ✔, `ab` ✔, `ε` ✔, `aabbb` ✘, `abab` ✘
- w c wᴿ: `abcba` ✔, `c` ✔, `abbcbba` ✔, `abcab` ✘
- Paréntesis: `(()())` ✔, `()()` ✔, `(()` ✘, `())(` ✘

**Nivel 1 – ALA (aⁿbⁿcⁿ)**
- `aabbcc` ✔, `abc` ✔, `aaabbbccc` ✔, `ε` ✔, `aabbc` ✘, `abcabc` ✘, `acb` ✘

**Nivel 0 – Máquina de Turing**
- aⁿbⁿ: `aabb` ✔, `aaabbb` ✔, `aab` ✘, `abb` ✘
- Sucesor unario: `111` → `1111`
- Suma unaria: `111+11` → `11111`
- Incremento binario: `1011` → `1100`, `111` → `1000`

## Máquinas propias (niveles 1 y 0)
Las reglas se pueden editar en el cuadro de texto, una por línea:
```
estado  símbolo_leído  símbolo_escrito  L|R|S  nuevo_estado
```
Directivas `start q0` y `accept qa`; `#` inicia un comentario; `_` es el blanco. En el ALA, `<` y `>` representan los delimitadores ⊢ y ⊣. Si no hay transición aplicable, la cadena se rechaza. Pulse **Reiniciar** para recargar las reglas.

## Estructura
```
src/Main.java      menú principal
src/SimPanel.java  controles comunes y utilidades de dibujo
src/DFAPanel.java  Nivel 3
src/PDAPanel.java  Nivel 2
src/TMPanel.java   Niveles 1 y 0 (mismo motor; cambia la cinta)
DOCUMENTO.md       documento de implementación y conceptos
```
