Feature: Optimización de la Ficha de Play Store (ASO & Copywriting)
  Como desarrollador de la app Compose Calculator
  Quiero optimizar los textos y metadatos de la ficha en Google Play Store
  Para maximizar la visibilidad orgánica, las conversiones de búsqueda y el posicionamiento ASO

  Scenario: Redacción y humanización de la descripción corta y completa
    Given la propuesta de valor de Compose Calculator como calculadora moderna, offline y fluida en Kotlin Multiplatform
    When se redactan el título (<= 30 caracteres), descripción breve (<= 80 caracteres) y descripción detallada (<= 4000 caracteres) aplicando el skill humanizalo
    Then los textos no deben contener frases robóticas ni clichés de IA
    And deben incluir las palabras clave estratégicas (calculadora, offline, compose multiplatform, rápida, material design)
    And deben destacar el respeto absoluto a la privacidad sin recopilación de datos

  Scenario: Configuración de categorización y etiquetas de Play Store
    Given la categoría de aplicación "Herramientas" / "Productividad"
    When se seleccionan las etiquetas oficiales de Google Play relevantes
    Then la aplicación queda categorizada de forma óptima para el algoritmo de recomendaciones de Google Play
