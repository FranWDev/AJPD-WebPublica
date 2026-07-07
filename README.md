# AJPD Web Pública y API Gateway

Este proyecto es el portal público ([proyectodubini.org](https://www.proyectodubini.org/)) y la API orientada al cliente de la **Asociación Juvenil Proyecto Dubini (AJPD)**. Está estructurado como una aplicación **Spring Boot 3.4.1 (Java 17)** que sirve una interfaz web pública ultra-ligera y actúa como gateway/proxy frente al backoffice privado de gestión, gestionando el envío de formularios, el registro de visitantes al museo, la pasarela de emails y la caché de contenidos.

---

## 🎨 Filosofía de Diseño: Web Eco-Friendly y Eficiente

El constraint principal para esta web pública es ofrecer una experiencia premium y moderna minimizando la huella ecológica digital.

* **Frontend 100% Vanilla (HTML, CSS y JS):** Sin frameworks de JavaScript pesados ni librerías de estilos de terceros. Esto reduce radicalmente el tamaño de los assets descargados, agilizando el renderizado del navegador y consumiendo el mínimo ancho de banda posible.
* **Carga de Datos Inteligente:** La integración de un Service Worker (`sw.js`) y estrategias de almacenamiento local garantizan un inicio instantáneo y reducen las peticiones repetitivas al servidor.
* **Compilación Nativa (GraalVM):** El backend está optimizado para compilarse a imagen nativa. Esto permite un consumo de memoria mínimo en producción (< 64MB-128MB RAM) y tiempos de cold start de milisegundos, ideal para despliegues eficientes en la nube y bajo consumo de CPU.

---

## 🛠️ Tecnologías Clave

### Backend (Spring Boot 3.4.1)
* **Framework:** Spring Boot 3.4.1 (Java 17) con Spring Web, Security y WebFlux.
* **Motor de Plantillas:** Thymeleaf (para inyección de componentes dinámicos en el servidor).
* **Gestión de Caché:** **Caffeine Cache** integrado de forma persistente a nivel de memoria para las noticias, con soporte para revalidación HTTP basada en **ETags** (`CacheEtagService`).
* **Resiliencia:** **Resilience4j Circuit Breaker** aplicado al cliente REST (`NewsClient`) para garantizar que fallos en el backoffice de gestión no afecten a la disponibilidad del portal público.
* **Envío de Emails:** Integración con la API de **Resend** para la entrega fiable de notificaciones de contacto e inscripciones.
* **Limitación de Peticiones:** Rate limiting integrado (`RateLimiterService`) para mitigar abusos de bots en los formularios públicos.
* **Seguridad:** Filtro JWT para securizar los endpoints administrativos (por ejemplo, purgado o precarga manual de caché).

### Frontend (Vanilla Stack)
* **Estilos:** CSS Vanilla modularizado por componentes, layouts y páginas, con un sistema de diseño consistente, variables CSS personalizadas y transiciones optimizadas.
* **Lógica:** JavaScript nativo modularizado para animaciones dinámicas, carruseles de fotos (`jardin-slider.js`), e interacciones de formularios.
* **PWA:** Service Worker propio (`sw.js`) para cachear recursos estáticos y assets de estilo.

---

## 📁 Estructura del Proyecto

### Backend (Java)
El código se organiza en `src/main/java/org/dubini/frontend_api/` bajo los siguientes módulos:

* `cache`: Implementación de gestores de caché personalizados (`PersistentCaffeineCacheManager`), inicializadores y controladores de purgado/precarga.
* `client`: Clientes HTTP reactivos (`WebClient`) de conexión con el Backoffice de administración.
* `config`: Configuraciones de CORS, Seguridad, Propiedades (Resend, Supabase, etc.) y Hints de GraalVM para reflexión.
* `controller`: Controladores de vistas Thymeleaf (`WebController`, `MuseoController`) y endpoints REST públicos (`ContactController`, `InscripcionController`, `NewsRestController`).
* `dto`: Objetos de transferencia de datos de formularios, registros y bloques de texto enriquecido (EditorJS).
* `exception`: Manejo centralizado de excepciones y mapeo de errores HTTP.
* `security`: Filtro JWT y proveedor de tokens para peticiones de administración externa.
* `service`: Lógica de negocio (Rate limiting, Envío de emails mediante Resend, Caché con ETags y llamadas a Supabase Storage).

### Recursos y Frontend
Organizado en `src/main/resources/`:

* `templates/`: Plantillas HTML procesadas por Thymeleaf (vistas de secciones, contacto, museo escolar/virtual, noticias, etc.).
* `static/`: Recursos estáticos del navegador:
  * `assets/`: Imágenes fijas, logotipos y multimedia optimizada.
  * `styles/`: Hojas de estilo CSS divididas en `base.css` (tokens y variables), `layout/` (cabeceras y pies) y `components/`/`pages/`.
  * `scripts/`: JS vanilla estructurado y el módulo para peticiones fetch (`api/`).

---

## ⚙️ Configuración y Variables de Entorno

El portal se configura mediante variables de entorno (que pueden definirse en un archivo `.env` local para desarrollo). Estas variables configuran:
* La seguridad y expiración de tokens JWT administrativos.
* La URL de conexión al backoffice privado de gestión.
* Las API Keys y buckets para servicios e integraciones de terceros (servidor de correos y almacenamiento de archivos).
* Las direcciones de correo de destino para la recepción automatizada de formularios públicos.

---

## 🚀 Ejecución y Construcción

### Prerrequisitos
* **Java 17** instalado y configurado en `JAVA_HOME`.
* **Maven 3.8+** instalado.

### Ejecución en Modo Desarrollo
Para iniciar la aplicación localmente en el puerto configurado (por defecto `8084`):
```bash
mvn spring-boot:run
```

### Ejecutar Pruebas
```bash
mvn test
```

### Compilar a JAR tradicional
```bash
mvn clean package
```
Generará el ejecutable tradicional en `target/frontend-api-1.2.0-SNAPSHOT.jar`.

### Compilación Nativa (GraalVM Image)
Para compilar la aplicación a binario nativo optimizado:
```bash
mvn clean package -Pnative
```
El ejecutable binario estará disponible en `target/frontend-api`. Puedes arrancarlo directamente de manera óptima y ultra-rápida:
```bash
./target/frontend-api
```
