# Guía y Playbook para Publicación de Apps en Google Play

Este documento recopila el conocimiento técnico, normativo y operativo aprendido para publicar, mantener y resolver infracciones de políticas en aplicaciones móviles Android / Kotlin Multiplatform en **Google Play Console**.

---

## 1. Requisitos Técnicos y de Compilación (Android / KMP)

### A. Nivel de API de Destino (`targetSdkVersion`)
- **Regla de Google Play:** Google Play exige anualmente que las aplicaciones nuevas y actualizaciones se orienten a la versión reciente de Android (a partir de agosto de 2026, **Android 16 / API 36**).
- **Configuración en el proyecto (`gradle.properties`):**
  ```properties
  android.targetSdk=36
  android.compileSdk=34 # o la versión de compilación soportada por tu AGP
  android.minSdk=24
  ```
- **Advertencias de compatibilidad de AGP:**
  Si utilizas un Android Gradle Plugin anterior y compilas contra un SDK moderno, puedes suprimir la advertencia con:
  ```properties
  android.suppressUnsupportedCompileSdk=36
  ```

### B. Incremento Obligatorio de `versionCode`
- Google Play rechaza cualquier archivo `.aab` cuyo `versionCode` sea igual o menor al de cualquier versión subida previamente.
- Historial de versiones del proyecto en Google Play:
  - `1000004`: Lanzado en Prueba cerrada - Alpha (24 oct 2023)
  - `1000007`: Lanzado en Prueba interna (24 oct 2023)
  - `1000008`: Nueva versión compilada para Android 16 (API 36)
- **Configuración en `build.gradle.kts`:**
  ```kotlin
  defaultConfig {
      versionCode = 1000008 // Mayor que 1000007
      versionName = "1.0.1"
  }
  ```

### C. Generación y Validación del Bundle de Producción (`AAB`)
- Google Play **solo acepta formatos Android App Bundle (`.aab`)**, ya no acepta `.apk` para producción.
- **Comando de compilación:**
  ```bash
  JAVA_HOME=/Users/jesusdmedinac/Library/Java/JavaVirtualMachines/jbr-17.0.14/Contents/Home ./gradlew :androidApp:bundleRelease
  ```
- El artefacto firmado resultante se ubica en:
  `androidApp/build/outputs/bundle/release/androidApp-release.aab`
- **Script de validación automática del AAB:**
  Ejecuta el script antes de subir el paquete a Play Console:
  ```bash
  ./verify-release-aab.sh
  ```
  *Valida automáticamente:*
  1. Archivo físico y tamaño.
  2. Nombre de paquete (`com.jesusdmedinac.compose.calculator`).
  3. `targetSdkVersion = 36` (Android 16).
  4. `versionCode = 1000008` y `versionName = 1.0.1`.
  5. `minSdkVersion = 24`.
  6. Firma JAR y huella SHA-256 (`6B:C7:A1...`) a nombre de Jesús Daniel Medina Cruz.

- **Reglas ProGuard / R8:** Asegurarse de que exista el archivo `proguard-rules.pro` en el módulo para evitar fallos de compilación durante `minifyReleaseWithR8`.

### D. Adaptabilidad Edge-to-Edge (Android 15 / 16)
- A partir de Android 15 (API 35) y Android 16 (API 36), el sistema operativo fuerza el renderizado *edge-to-edge* (las barras de estado y navegación se vuelven transparentes).
- En Compose, siempre se debe consumir el padding del `Scaffold`:
  ```kotlin
  Scaffold(
      modifier = Modifier.fillMaxSize(),
  ) { innerPadding ->
      Column(
          modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
      ) {
          // Contenido de la UI
      }
  }
  ```

---

## 2. Requisitos de Políticas de Datos y Privacidad

### A. Política de Privacidad Pública
- **Causa común de retiro de apps:** Google Play utiliza bots para rastrear la URL de la política de privacidad. Si la URL devuelve un error HTTP (404, 500, error de certificado SSL) o requiere autenticación, la aplicación es retirada de la tienda.
- **Requisitos de la URL:**
  1. Debe ser pública, permanente y con protocolo seguro (`https://`).
  2. Debe identificar claramente el nombre de la app (ej. *Compose Calculator*) y del desarrollador.
  3. Debe especificar qué datos se recopilan (o declarar explícitamente que no se recopila información personal).
  4. Debe indicar que los cálculos u operaciones se procesan localmente en el dispositivo.
  5. Debe incluir información de contacto del desarrollador.

### B. Hosting en Cloudflare Pages con Git Automático
- Para evitar que la política de privacidad dependa de servidores personales o dominios que puedan vencer:
  1. Se crea una carpeta `docs/` con `index.html` y `privacy-policy.html`.
  2. Se conecta el repositorio a **Cloudflare Pages** (**Workers & Pages** > **Create application** > **Pages** > **Connect to Git**).
  3. Parámetros en Cloudflare:
     - Branch: `main`
     - Framework preset: `None`
     - Build output directory: `docs`
  4. Cada `git push origin main` actualiza la web de forma automática, gratuita y con SSL global.

### C. Declaración Obligatoria de Apps de Salud
- Google Play exige a todas las aplicaciones declarar si contienen funciones médicas, de salud o acondicionamiento físico.
- Para aplicaciones que no son de salud (como una calculadora), se debe ingresar a la declaración y marcar explícitamente:
  *"No, mi app no es una app de salud ni ofrece funciones relacionadas con la salud"*.

### D. Verificación de Desarrolladores de Android y Registro de Claves
- Nueva normativa para distribuir en dispositivos Android certificados a nivel global:
- Se debe registrar el **nombre del paquete** (`com.jesusdmedinac.compose.calculator`) y la **huella digital SHA-256** de la clave de firma (`.jks`) en el portal de Verificación de desarrolladores de Android.
- Huella digital de firma del proyecto (obtenida con `keytool`):
  - **SHA-256:** `6B:C7:A1:FD:9B:98:8F:05:D0:67:41:74:45:72:29:D3:B5:88:E0:2A:D4:D4:37:AD:24:86:4E:AF:1A:AC:F2:31`

#### Verificación de la Clave Privada mediante APK firmado (`adi-registration.properties`):
1. Google genera un token/identificador único de verificación (ej. `DW5ZBLZWMKC2CAAAAAAAAAAAAA`).
2. Se crea el archivo `adi-registration.properties` dentro de `androidApp/src/androidMain/assets/` con dicho token.
3. Se asegura la ruta de assets en `androidApp/build.gradle.kts`:
   ```kotlin
   sourceSets["main"].assets.srcDirs("src/androidMain/assets")
   ```
4. Se compila el APK de release firmado:
   ```bash
   ./gradlew :androidApp:assembleRelease
   ```
5. El APK generado se ubica en:
   `androidApp/build/outputs/apk/release/androidApp-release.apk`
6. **Validación previa del APK:**
   Se puede ejecutar el script de verificación para validar nombre de paquete, token y firma SHA-256 antes de subirlo:
   ```bash
   ./verify-verification-apk.sh
   ```
7. Se sube este APK a la consola de Verificación para que Google valide la propiedad criptográfica de la clave privada.

---

## 3. Mapa de Navegación de Google Play Console

La barra lateral izquierda de Google Play Console está organizada de la siguiente manera:

```text
Google Play Console
├── Página principal
└── [Nombre de la App] (ComposeCalculator)
    ├── Panel (Dashboard)
    ├── Estadísticas (Metrics)
    ├── Descripción general de la publicación (Publishing overview)
    │
    ├── Probar y lanzar (Release management)
    │   ├── Producción          <-- Aquí se sube el nuevo AAB para publicar
    │   ├── Pruebas abiertas
    │   ├── Pruebas cerradas
    │   ├── Pruebas internas
    │   └── Configuración avanzada
    │
    ├── Supervisa y mejora
    │   ├── Android vitals (Crashes, ANRs)
    │   └── Calidad de la app
    │
    ├── Aumenta la cantidad de usuarios
    │   └── Ficha de Play Store principal (Título, descripción, capturas, iconos)
    │
    ├── Monetiza con Play (Compras integradas y suscripciones)
    │
    └── Políticas y programas (Scrollear hacia abajo en el menú)
        ├── Estado con respecto a las políticas (Policy status) <-- Alertas de infracciones
        └── Contenido de la app (App content)                  <-- Configuración crítica
            ├── Política de privacidad (Privacy policy)        <-- Poner la URL pública aquí
            ├── Seguridad de los datos (Data safety)           <-- Declarar si recopilas datos
            ├── Anuncios
            ├── Acceso a apps
            └── Público objetivo y contenido
```

---

## 4. Procedimiento para Resolver la Infracción y Restaurar la App

### Paso 1: Configurar la URL de la Política de Privacidad
1. Desplaza el menú lateral hacia abajo hasta la sección **Políticas y programas** (o *Policy and programs*).
2. Haz clic en **Contenido de la app** (*App content*).
3. En la sección **Política de privacidad** (*Privacy Policy*), haz clic en **Administrar** o **Iniciar**.
4. Pega la URL pública activa:
   `https://compose-calculator.pages.dev/`
5. Haz clic en **Guardar**.

### Paso 2: Declarar la Seguridad de los Datos (*Data Safety*)
1. En **Contenido de la app** (*App content*), busca **Seguridad de los datos** (*Data safety*).
2. Responde el cuestionario confirmando:
   - ¿Tu app recopila o comparte datos de usuarios? **No**.
   - Los datos se procesan únicamente de forma efímera y local en el dispositivo.
3. Guarda los cambios.

### Paso 3: Subir la Nueva Versión AAB
1. En el menú lateral izquierdo, ve a la sección **Probar y lanzar** > **Producción**.
2. Haz clic en **Crear nueva versión** (arriba a la derecha).
3. Sube el archivo App Bundle generado:
   `androidApp/build/outputs/bundle/release/androidApp-release.aab`
4. Verifica que reconozca:
   - Target SDK: 36
   - Código de versión: 1000008
5. Ingresa las notas de la versión (ej. *"Actualización de compatibilidad con Android 16 y optimizaciones generales"*).
6. Haz clic en **Siguiente** / **Revisar versión**.
7. Haz clic en **Iniciar el lanzamiento a Producción** (o **Enviar para revisión**).
