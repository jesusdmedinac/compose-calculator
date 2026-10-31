# Hilo de Lanzamiento para X / Twitter

## Post 1 (Hook / Gancho visual)
La mayoría de las calculadoras en Android tienen anuncios a pantalla completa o fallan en cuentas básicas como `0.1 + 0.2` por problemas de coma flotante.

Hice **Compose Calculator**: una calculadora open source, 100% offline, sin publicidad y con aritmética decimal exacta.

Abro hilo con lo que aprendí construyéndola con Compose Multiplatform y actualizándola a Android 16 🧵👇

---

## Post 2 (El problema de la precisión matemática)
Casi cualquier app sencilla usa `Double` o `Float`. El problema:
`0.1 + 0.2 = 0.30000000000000004`.

En una calculadora de diario, eso no es aceptable. Implementé aritmética con `BigDecimal` y un parser de expresiones con evaluación de AST en memoria para calcular operaciones balanceadas en tiempo real mientras escribes.

---

## Post 3 (Privacidad real: Cero permisos)
¿Por qué una calculadora te pide acceso a internet, almacenamiento o ubicación?

Compose Calculator tiene 0 permisos en el manifest.
• No guarda analíticas
• No rastrea tu dispositivo
• Toda la memoria vive y muere en la sesión local
• La política de privacidad cabe en un tweet: no tocamos tus datos.

---

## Post 4 (Multiplataforma de verdad)
Toda la interfaz y la lógica están escritas una sola vez en `commonMain`:
• Jetpack Compose Material 3
• Soporte Edge-to-Edge nativo (Android 15 y 16)
• Compila nativo tanto para Android como para iOS vía `ComposeUIViewController` sin tocar XML ni Storyboards.

---

## Post 5 (Call to Action / Enlaces)
El código está completamente abierto en GitHub y el nuevo paquete con soporte para Android 16 ya está en camino a Google Play.

Repositorio: https://github.com/jesusdmedinac/compose-calculator
Página web y política: https://compose-calculator.pages.dev/

Feedback, PRs y estrellas siempre bienvenidos. ¿Qué otra función le agregarías?
