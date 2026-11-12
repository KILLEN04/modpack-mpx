# Clases del Gremio (NeoForge 1.21.1)

Mod de clases del modpack: la recepcionista Lyra te asigna una clase permanente
(Guerrero, Mago, Arquero, Paladín o Nigromante) y tu rango sube de F a S.

## Requisitos
- Java 21 (Gradle lo descarga solo si no lo tienes)
- GeckoLib 4.9.3 para NeoForge 1.21.1 en la carpeta `mods` de la instancia

## Compilar
Abre una terminal en esta carpeta y ejecuta:

    gradlew.bat build

El mod queda en `build/libs/clases_gremio-0.1.0.jar`. Cópialo a la carpeta `mods`
de la instancia "pruebas" de Prism junto con GeckoLib.

Para probar directo desde el proyecto: `gradlew.bat runClient`

## En el juego
- Huevo: pestaña de huevos de generación, "Huevo de Recepcionista del Gremio"
- Clic derecho a la recepcionista: diálogo, cartas de clase y confirmación
- Comandos:
  - `/gremio info [jugador]`
  - `/gremio clase set <jugador> <clase>` (admin)
  - `/gremio clase reset <jugador>` (admin)
  - `/gremio rango set <jugador> <F..S>` (admin)
  - `/gremio recepcionista` (admin: la crea donde estás, mirándote)

## Modelo
`modelos/recepcionista_gremio.bbmodel` es el proyecto de Blockbench.
Si lo cambias, regenera los archivos de GeckoLib con:

    python modelos/bb2gecko.py modelos/recepcionista_gremio.bbmodel src/main/resources/assets/clases_gremio/geo/entity/recepcionista.geo.json src/main/resources/assets/clases_gremio/animations/entity/recepcionista.animation.json recepcionista

(o exporta desde Blockbench con File > Export > GeckoLib Model a esas mismas rutas).
