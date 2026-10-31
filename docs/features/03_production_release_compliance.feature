Feature: Cumplimiento de Políticas y Lanzamiento a Producción
  Como administrador de la app en Google Play Console
  Quiero asegurar el cumplimiento de todas las directivas de seguridad y lanzar la app al canal de Producción
  Para restaurar la disponibilidad pública en la tienda sin riesgo de rechazo

  Scenario: Auditoría estática y verificación pre-vuelo del paquete AAB
    Given el artefacto generado androidApp-release.aab
    When se ejecuta la validación automatizada mediante ./verify-release-aab.sh y play-policy-insights
    Then confirma targetSdkVersion = 36 (Android 16)
    And valida versionCode superior al historial (>= 1000008)
    And confirma firma válida con el keystore de release y ausencia de permisos sensibles no declarados

  Scenario: Promoción y lanzamiento en el canal de Producción
    Given el paquete validado y la política de privacidad activa en Cloudflare Pages
    When se crea la versión de Producción en Google Play Console con notas de versión claras
    Then se envía a revisión para publicación pública global
    And el estado en Play Console pasa a "En revisión" o "Publicada"
