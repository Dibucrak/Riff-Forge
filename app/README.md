# Riff Forge

**Riff Forge** es una aplicación Android nativa diseñada como una herramienta integral para guitarristas. Permite gestionar repertorios, afinar instrumentos en tiempo real, practicar con metrónomo y estudiar teoría musical mediante un diccionario de acordes interactivo; todo respaldado en la nube con soporte offline-first.

## Características Principales

* **️ Afinador Profesional:** Análisis de audio en tiempo real mediante algoritmos de autocorrelación matemática puros y retroalimentación visual LED.
* **Diccionario de Acordes:** Motor de renderizado nativo en Canvas (Jetpack Compose) que dibuja dinámicamente diapasones y posiciones de los dedos, eliminando la necesidad de imágenes estáticas pesadas.
* **️ Metrónomo:** Generador de tonos precisos utilizando corrutinas para mantener el tempo exacto (BPM) y control de métrica de compás.
* **Gestión de Repertorios:** Editor completo para guardar canciones, letras y tablaturas, visualizador con *AutoScroll* inteligente y organización mediante *Setlists* (relaciones Many-to-Many).
* **️ Sincronización en la Nube:** Soporte *Offline-First*. La aplicación funciona completamente sin internet utilizando una base de datos local, con la capacidad de respaldar y restaurar el catálogo en Firebase Firestore a voluntad del usuario.
* **Círculo de Quintas:** Herramienta interactiva para el estudio de la armonía, escalas relativas y alteraciones.

## ️ Arquitectura y Tecnologías

El proyecto fue desarrollado bajo los principios de **Clean Architecture** (Dominio, Datos, Presentación) acoplado al patrón de diseño **MVVM**, garantizando un código altamente escalable, testeable y desacoplado.

* **Lenguaje:** Kotlin
* **Interfaz de Usuario:** Jetpack Compose (Material Design 3)
* **Inyección de Dependencias:** Dagger Hilt
* **Programación Asíncrona:** Coroutines & Flow
* **Base de Datos Local:** Room (SQLite)
* **Backend y Autenticación:** Firebase Authentication & Firestore
* **Navegación:** Navigation Compose (Con transiciones personalizadas globales)

##  Configuración e Instalación

Para ejecutar este proyecto localmente:

1. Clona el repositorio:
   ```bash
   git clone [https://github.com/tu-usuario/RiffForge.git](https://github.com/tu-usuario/RiffForge.git)