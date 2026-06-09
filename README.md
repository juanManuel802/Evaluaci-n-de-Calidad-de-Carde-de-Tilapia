<img width="1536" height="1024" alt="99c65264-b4c5-447a-9f9b-4180013e07cd" src="https://github.com/user-attachments/assets/a36d6af1-edfc-4adb-8c77-bbbe9a703536" />


# SECCT — Sistema de Evaluación de Calidad de Carne de Tilapia

SECCT es una aplicación desarrollada para apoyar el registro y la evaluación de lotes de tilapia bajo los estándares **NTC 1443** y **GTC 157**.. Su propósito es facilitar el seguimiento de la calidad del producto mediante el registro de información relevante y la aplicación de criterios de evaluación establecidos.

---

## ¿Qué hace?

Permite llevar el registro de lotes de tilapia a lo largo de la cadena productiva: desde la estación piscícola hasta la plaza de mercado. Con la app puedes:

- Registrar un lote con sus datos básicos (origen, peso, fecha de captura, etc.)
- Evaluar unidades de muestra dentro de ese lote
- Cerrar la evaluación y obtener una clasificación final del lote

---

## Requisitos

- Java 21
- JavaFX
- Maven 3.8+
- Python 3.13 con entorno virtual en `servicioCNN/.venv` (dependencias en `servicioCNN/requirements.txt`)

---

## Estructura del proyecto

El proyecto está organizado siguiendo principios de arquitectura limpia, separando la lógica de negocio, la interfaz de usuario y el acceso a los datos para facilitar el mantenimiento y la evolución del sistema.
---

models/                    # Modelo CNN entrenado (.h5)
servicioCNN/               # Servicio de inferencia Python/FastAPI
src/
├── main/java/co/unillanos/secct/
│   ├── entities/          # Lote, Evaluacion y sus value objects
│   ├── usecases/          # Casos de uso y puertos
│   ├── adapters/ui/       # Pantallas JavaFX
│   └── infrastructure/    # Repositorio en memoria y cliente HTTP al servicio CNN
└── test/                  # Tests por capa
---
## Documentación
 
En la carpeta `/docs` se encuentra documentación técnica adicional:
 
- **Guía de entrenamiento del modelo** — describe el proceso, los datos utilizados y los parámetros del clasificador CNN.
- **Restricciones de integración** — especifica las condiciones y límites que debe respetar cualquier implementación del modelo real al integrarse con el sistema.

---
## Ejecución

### Linux (recomendado)

1. Crear el entorno virtual e instalar dependencias Python:
   ```bash
   cd servicioCNN && python3 -m venv .venv && .venv/bin/pip install -r requirements.txt
   ```
2. Ejecutar el script de arranque desde la raíz del proyecto:
   ```bash
   chmod +x iniciar_secct.sh && ./iniciar_secct.sh
   ```
   El script levanta el servicio CNN, espera a que esté listo y abre la app Java automáticamente.

3. **Agregar al menú de aplicaciones (opcional):** crear un archivo `~/.local/share/applications/SECCT.desktop` con:
   ```ini
   [Desktop Entry]
   Type=Application
   Name=SECCT
   Exec=/ruta/al/proyecto/iniciar_secct.sh
   Terminal=false
   Categories=Science;
   ```

### Otros sistemas operativos

Instalar Java 21, Maven y Python 3.13. Crear el entorno virtual en `servicioCNN/` e instalar dependencias (`pip install -r requirements.txt`). Lanzar el servicio CNN (`servicioCNN/servicio_inferencia.py`) y luego la app con `mvn javafx:run` desde la raíz.

> **Importante:** el servicio busca el modelo en `models/modelo_calidad_tilapia.h5` por defecto. Si el archivo tiene otro nombre, el sistema no arrancará. Se puede sobreescribir la ruta con la variable de entorno `SECCT_MODELO_H5`.

---

## Estado actual

El proyecto cubre los casos de uso principales (registrar lote, evaluar unidades, cerrar evaluación). El clasificador CNN está integrado mediante un servicio Python/FastAPI que carga el modelo entrenado en `models/modelo_calidad_tilapia.h5`.
Aún no se generan reportes.

---

*Universidad de los Llanos · Ingeniería de Sistemas*
