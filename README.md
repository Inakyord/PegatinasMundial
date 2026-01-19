# Proyecto Final: Pegatinas

Este es el proyecto final de la asignatura **"Desarrollo de Aplicaciones Móviles"** del **Máster Universitario En Ingeniería Informática** de la **ETSI Informáticos (UPM)**.

## Descripción
Este repositorio contiene el código fuente completo de la aplicación.

## Funcionalidades Principales

La aplicación permite gestionar una colección digital de cromos (tipo Mundial/Fútbol) con las siguientes características:

*   **Álbum Digital Completo**: Navegación por equipos y visualización de todos los cromos disponibles. Los cromos se muestran en gris si no se tienen y en color si ya están en la colección.
*   **Gestión de Colección**:
    *   **Entrada de Cromos**: Permite añadir cromos rápidamente introduciendo sus números identificadores.
    *   **Detalle de Cromo**: Visualización ampliada de cada cromo con su información.
*   **Geolocalización Automática**: Utiliza el GPS del dispositivo para detectar el país actual del usuario y mostrarle directamente el equipo correspondiente a su ubicación (Función "País Favorito").
*   **Herramienta de Nivel**: Incluye una utilidad de nivel de burbuja ("Nivel") que utiliza el acelerómetro del dispositivo para indicar si el teléfono está en una superficie plana.

## Tecnologías Utilizadas

*   **Lenguaje**: Java (Android SDK).
*   **Almacenamiento**: Base de datos local (SQLite) y SharedPreferences.
*   **Sensores**: Acceso al GPS (LocationManager) y Acelerómetro (SensorManager).
*   **Arquitectura**: Uso de Fragmentos y Actividades.

## Instrucciones de Instalación y Uso

Para probar la aplicación, sigue estos pasos:

1. Descarga el directorio completo `ProyectoFinalPegatinas` que se encuentra en la raíz de este repositorio.
2. Abre **Android Studio**.
3. Selecciona la opción de abrir un proyecto existente y selecciona la carpeta descargada.
4. Espera a que Gradle sincronice las dependencias y ejecuta la aplicación en un emulador o dispositivo físico.
