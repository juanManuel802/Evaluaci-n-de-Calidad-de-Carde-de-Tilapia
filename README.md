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

---

## Estructura del proyecto

El proyecto está organizado siguiendo principios de arquitectura limpia, separando la lógica de negocio, la interfaz de usuario y el acceso a los datos para facilitar el mantenimiento y la evolución del sistema.
---

src/
├── main/java/co/unillanos/secct/
│   ├── entities/          # Lote, Evaluacion y sus value objects
│   ├── usecases/          # Casos de uso y puertos
│   ├── adapters/ui/       # Pantallas JavaFX
│   └── infrastructure/    # Repositorio en memoria y clasificador simulado
└── test/                  # Tests por capa
---
## Documentación
 
En la carpeta `/docs` se encuentra documentación técnica adicional:
 
- **Guía de entrenamiento del modelo** — describe el proceso, los datos utilizados y los parámetros del clasificador CNN.
- **Restricciones de integración** — especifica las condiciones y límites que debe respetar cualquier implementación del modelo real al integrarse con el sistema.

---
## Ejecución

1. Clonar o descargar el proyecto.
2. Abrirlo en un entorno de desarrollo compatible con Java.
3. Instalar las dependencias mediante Maven.
4. Ejecutar la aplicación.

---

## Estado actual

El proyecto cubre los casos de uso principales (registrar lote, evaluar unidades, cerrar evaluación). El clasificador CNN es un *fake* por ahora — sirve como placeholder hasta integrar el modelo real. El estado `REPORTADO` está definido pero no implementado todavía.

---

*Universidad de los Llanos · Ingeniería de Sistemas*
