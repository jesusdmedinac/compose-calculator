# Publicación y Carrusel Educativo para LinkedIn

## Texto del Post (Feed)

Hace tiempo me propuse construir una calculadora simple para Android e iOS usando Compose Multiplatform. Lo que parecía un proyecto de fin de semana terminó dejándome lecciones clave sobre precisión numérica, diseño edge-to-edge y las nuevas normativas de Google Play.

Aquí van 4 decisiones de arquitectura que tomé en el camino:

1. **Aritmética exacta sobre tipos primitivos:** Dejar de usar `Double` y migrar a `BigDecimal`. Para una app de finanzas o cálculo diario, un error de redondeo de coma flotante destruye la confianza del usuario.
2. **UI 100% compartida en Compose:** Escribir la pantalla y los layouts una sola vez en `commonMain`. Tanto Android como iOS ejecutan exactamente los mismos componentes de Material 3.
3. **Adaptación a Android 16 (API 36):** Manejar los insets obligatorios de edge-to-edge directamente en el Scaffold para que la barra de navegación transparente no tape botones ni resultados.
4. **Hosting inmutable de políticas:** Alojé la política de privacidad estática directamente en Cloudflare Pages vinculada al repositorio en GitHub, evitando que caídas de servidores personales causen el retiro de la app en Play Store.

El proyecto es totalmente open source. Les comparto el repositorio y la estructura del proyecto en los comentarios 👇

¿Qué opinan de la madurez actual de Compose Multiplatform para utilidades de producción? Los leo.

---

## Contenido de las Láminas del Carrusel (PDF / Slides)

### Slide 1: Portada
**Título:** De 0.1 + 0.2 = 0.3000000004 a una app multiplataforma en producción.
**Subtítulo:** Lo que aprendí creando Compose Calculator con Kotlin Multiplatform y Android 16.

### Slide 2: El problema de la precisión flotante
• Los procesadores representan números en binario IEEE 754.
• Operaciones cotidianas de dinero o comercio fallan con tipos `Float`/`Double`.
• **Solución:** Motor matemático basado en `BigDecimal` y un parser de expresiones (AST) que evalúa paréntesis y operadores paso a paso en memoria.

### Slide 3: Una sola UI para Android e iOS
• 0 líneas de XML. 0 storyboards.
• Jetpack Compose Material 3 viviendo en `commonMain`.
• En Android: montado en `MainActivity`.
• En iOS: montado en `ComposeUIViewController` dentro de una vista SwiftUI transparente.

### Slide 4: Android 16 y Edge-to-Edge Obligatorio
• En Android 15 y 16, las barras del sistema se vuelven transparentes por defecto.
• Si no consumes los `innerPadding` del `Scaffold`, los botones inferiores quedan tapados por la barra de gestos.
• Un cambio simple en Compose previene bugs visuales en dispositivos modernos.

### Slide 5: El laberinto de políticas en Google Play
• Google retira apps si la URL de privacidad devuelve un error HTTP temporal.
• **Estrategia aplicada:** Servir el HTML estático en Cloudflare Pages (`https://compose-calculator.pages.dev/`) conectado vía Git para tener uptime permanente y SSL global.

### Slide 6: Cierre / Recursos
• Proyecto Open Source en GitHub: `jesusdmedinac/compose-calculator`
• 100% Kotlin Multiplatform & Compose Multiplatform
• Conexión: Jesús Daniel Medina Cruz
