# Documento de implementación y conceptos

## 1. Jerarquía de Chomsky
| Tipo | Gramática | Lenguaje | Modelo reconocedor | Memoria |
|---|---|---|---|---|
| 3 | Regular | Regular | Autómata Finito (AFD/AFN) | Solo el estado (finita) |
| 2 | Libre de contexto | LLC | Autómata de Pila (AP) | Pila LIFO ilimitada |
| 1 | Sensible al contexto | LSC | Autómata Linealmente Acotado (ALA) | Cinta acotada a la longitud de la entrada |
| 0 | Sin restricciones | Recursivamente enumerable | Máquina de Turing | Cinta infinita |

Cada nivel contiene al anterior: Regular ⊂ LLC ⊂ LSC ⊂ Recursivamente enumerable.

## 2. Arquitectura
Se usa Java Swing con una clase base y cuatro módulos:
- `SimPanel`: componentes comunes (campo de cadena, lista de ejemplos, Paso/Ejecutar/Pausa/Reiniciar, velocidad, bitácora, veredicto) y utilidades de dibujo (flechas, cinta con cabezal). Define el contrato `load(cadena)` y `step()`. `step()` ejecuta una transición y devuelve `false` al terminar. La ejecución automática es un `javax.swing.Timer` que llama a `step()` y repinta, lo que produce la animación.
- `DFAPanel`, `PDAPanel`, `TMPanel`: lógica y gráficos de cada nivel. `Main` solo orquesta con un `CardLayout`.

## 3. Nivel 3: AFD
Se modela como M = (Q, Σ, δ, q0, F) con la tabla `d[estado][símbolo]` y un arreglo `acc[]`. Cada paso lee un símbolo, aplica δ y avanza el índice. Un símbolo fuera de Σ rechaza la cadena.
- Lenguajes predeterminados: número par de 1s y cadenas que terminan en `01`.
- Lenguaje parametrizable: cadenas que contienen una subcadena `s` dada por el usuario. Se construye el autómata de prefijos (estilo KMP): el estado k significa «el mayor prefijo de `s` que es sufijo de lo leído tiene longitud k». δ(k, c) se calcula buscando el mayor l tal que `s[0..l)` es sufijo de `s[0..k)·c`. El estado |s| es final y absorbente.
- Gráfico: estados en círculo, arcos cuadráticos con las etiquetas de símbolos agrupadas, lazos para autotransiciones, doble círculo para finales y estado actual en amarillo. La cinta muestra en gris lo consumido.

## 4. Nivel 2: Autómata de Pila
Se implementa un AP determinista con transiciones ε: δ(q, a, X) → (q', γ), donde γ se apila con su primer carácter como nuevo tope. En cada paso se busca primero una regla que consuma el símbolo actual y, si no existe, una regla ε. La pila inicia con `$` (fondo).
- aⁿbⁿ: se apila una `a` por cada `a`; al ver `b` se pasa a q1 y se desapila una `a` por `b`; al quedar `$` se pasa a `qf`.
- w c wᴿ: se apila w; la `c` marca la mitad (se elimina el no determinismo de adivinarla); luego cada símbolo debe coincidir con el tope.
- Paréntesis balanceados: se apila `(` y se desapila con `)`.
- Aceptación por estado final `qf` con la entrada consumida. Si no hay regla aplicable, se rechaza.
- Gráfico: pila vertical con el tope resaltado y la última regla aplicada. Cada apilar/desapilar se ve al avanzar los pasos.

## 5. Nivel 1: ALA
Un ALA es una Máquina de Turing cuya cinta se restringe a las celdas que contienen la entrada, rodeadas por los delimitadores `⊢` y `⊣`. El simulador (`TMPanel` con `bounded = true`) construye la cinta `⊢ w ⊣` de tamaño fijo y verifica tres cosas: el cabezal no sale de los delimitadores, los delimitadores no se sobrescriben con otro símbolo y no se agregan celdas.
- Lenguaje: aⁿbⁿcⁿ, que no es libre de contexto (lo demuestra el lema de bombeo) pero sí sensible al contexto.
- Algoritmo: en cada pasada se marca una `a` (X), una `b` (Y) y una `c` (Z), y el cabezal retrocede a la X más reciente. Cuando no quedan `a`, se verifica que solo restan Y, Z hasta `⊣`. Cada pasada recorre la cinta, por lo que el uso de espacio es lineal.

## 6. Nivel 0: Máquina de Turing
Es una MT determinista de una cinta: M = (Q, Σ, Γ, δ, q0, _, F). La cinta crece con blancos `_` cuando el cabezal sale por cualquiera de los lados. Un límite de 20 000 pasos evita bloquear la interfaz con máquinas que no terminan (el problema de la parada es indecidible).
- aⁿbⁿ (aceptación): borra una `a` al inicio y una `b` al final, y repite hasta que la cinta queda vacía.
- Sucesor unario y suma unaria: funciones sobre 1ⁿ; la suma reemplaza `+` por `1` y borra el último `1`.
- Incremento binario: va al extremo derecho y propaga el acarreo hacia la izquierda; si el acarreo sobrepasa el bit más significativo, la cinta se extiende.
- Gráfico: cinta con ventana centrada en el cabezal, indicador ▼, estado activo, alfabeto Γ (calculado desde las reglas) y contador de pasos.

## 7. Motor común de reglas (niveles 1 y 0)
Las reglas se leen de un cuadro de texto con el formato `estado leído escrito L|R|S siguiente` y se guardan en un `HashMap` con la clave `"estado símbolo"`. El usuario puede editar o crear sus propias máquinas. La ausencia de una regla implica rechazo, que es el estado de rechazo implícito.

## 8. Limitaciones
- Los modelos son deterministas (el AP de w wᴿ usa un marcador central para evitarlo).
- Las MT son de una cinta.
- No se hicieron pruebas automáticas; la validación se hace con las cadenas de ejemplo incluidas en la interfaz.
