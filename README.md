# PixelBase - Backend API 🚀

Este es el núcleo del proyecto E-commerce **PixelBase**, desarrollado con un enfoque en arquitectura limpia,
escalabilidad y buenas prácticas de desarrollo utilizando **Spring Boot 3** y **Java 21**.

---

## 🛠️ Requisitos Previos

* **Java 21**: Asegúrate de tener instalado el JDK 21 (LTS).
* **PostgreSQL**: Debes tener una instancia de base de datos corriendo localmente.

---

## ⚙️ Configuración Inicial

Para que el proyecto funcione en tu entorno local, sigue estos pasos:

1. **Clonar el repositorio.**
2. **Preparar las propiedades**:
    - Ve a la ruta `src/main/resources/`.
    - Verás un archivo llamado `application-dev.properties.template`.
    - **Copia** ese archivo y cámbiale el nombre a `application-dev.properties`.
   > **Nota:** El archivo `application-dev.properties` está ignorado en Git para proteger tus credenciales personales.
   **Nunca** lo subas al repositorio.
3. **Configurar tu base de datos**:
    - Abre el nuevo archivo `application-dev.properties` y completa tus credenciales locales.

---

## 🌍 Manejo de Perfiles

Este proyecto utiliza **Spring Profiles** para separar la configuración de desarrollo de la de producción.

* **application.properties**: Contiene la configuración base y las variables de entorno requeridas. **No pongas
  contraseñas aquí**.
* **application-dev.properties**: Es tu configuración local (Base de datos local, llaves de prueba). **Este archivo está
  ignorado por Git** por seguridad.
* **application-prod.properties**: Contiene la configuración para el entorno de despliegue final.

### Cómo activar un perfil

Para que Spring sepa qué archivo usar, debes pasarle el parámetro `spring.profiles.active`. En este proyecto, usamos el
perfil `dev` para trabajar localmente.

Si usas la terminal con el comando de abajo (`-Dspring-boot.run.profiles=dev`), Spring activará automáticamente todas
las configuraciones del archivo `application-dev.properties`.

---

## 🚀 Cómo Ejecutar el Proyecto

No necesitas tener Maven instalado globalmente. El proyecto incluye un "Maven Wrapper" que descargará todo lo necesario
automáticamente.

Abre una terminal en la raíz del proyecto y ejecuta el siguiente comando:

**En Windows:**

```bash
./mvnw.cmd spring-boot:run '-Dspring-boot.run.profiles=dev'
```

**En Linux / Mac:**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## 📖 Documentación de la API

Una vez que el proyecto esté corriendo, puedes acceder a la documentación de la API en:

`/swagger-ui.html`

---

## 📁 Estructura del Proyecto

- `config/`: Configuraciones globales de Spring (Swagger, Seguridad, etc.).
- `common/`: Clases compartidas, excepciones globales y auditoría.
- `modules/`: Lógica de negocio dividida por dominios (Users, Catalog, Orders).

---

## 🛡️ Manejo de Errores

La API responde con un formato estándar de error para facilitar la integración con el frontend:

```json
{
  "timestamp": "2026-04-18T20:00:00",
  "status": 400,
  "message": "Mensaje descriptivo del error",
  "errors": [
    {
      "field": "email",
      "message": "Formato de correo inválido"
    }
  ]
}
```