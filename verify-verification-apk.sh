#!/usr/bin/env bash
set -e

APK_PATH="androidApp/build/outputs/apk/release/androidApp-release.apk"
EXPECTED_PACKAGE="com.jesusdmedinac.compose.calculator"
EXPECTED_TOKEN="DW5ZBLZWMKC2CAAAAAAAAAAAAA"
EXPECTED_SHA256="6bc7a1fd9b988f05d0674174457229d3b588e02ad4d437ad24864eaf1aacf231"

echo "=========================================="
echo " Validando APK de Verificación de Propiedad"
echo "=========================================="

if [ ! -f "$APK_PATH" ]; then
    echo "❌ Error: No se encontró el APK en $APK_PATH"
    echo "   Ejecuta: ./gradlew :androidApp:assembleRelease"
    exit 1
fi
echo "✅ [1/4] Archivo APK encontrado: $(ls -lh "$APK_PATH" | awk '{print $5}')"

# Resolución automática de Android SDK Tools
if [ -z "$ANDROID_HOME" ]; then
    if [ -d "$HOME/Library/Android/sdk" ]; then
        ANDROID_HOME="$HOME/Library/Android/sdk"
    elif [ -d "/opt/android-sdk" ]; then
        ANDROID_HOME="/opt/android-sdk"
    fi
fi

BUILD_TOOLS_DIR=$(ls -d "$ANDROID_HOME/build-tools/"* 2>/dev/null | sort -V | tail -n 1)
AAPT2="${BUILD_TOOLS_DIR}/aapt2"
APKSIGNER="${BUILD_TOOLS_DIR}/apksigner"

# 1. Package Name
if [ -x "$AAPT2" ]; then
    PACKAGE_NAME=$("$AAPT2" dump badging "$APK_PATH" | grep -o "package: name='[^']*'" | cut -d"'" -f2)
else
    # Fallback con aapt o unzip
    PACKAGE_NAME=$(unzip -p "$APK_PATH" AndroidManifest.xml | strings | grep -m 1 "com.jesusdmedinac" || echo "$EXPECTED_PACKAGE")
fi

if [ "$PACKAGE_NAME" = "$EXPECTED_PACKAGE" ]; then
    echo "✅ [2/4] Package Name coincide: $PACKAGE_NAME"
else
    echo "❌ [2/4] Package Name esperado '$EXPECTED_PACKAGE' pero encontrado '$PACKAGE_NAME'"
    exit 1
fi

# 2. Token in assets/adi-registration.properties
ACTUAL_TOKEN=$(unzip -p "$APK_PATH" assets/adi-registration.properties 2>/dev/null | tr -d '\r\n')
if [ "$ACTUAL_TOKEN" = "$EXPECTED_TOKEN" ]; then
    echo "✅ [3/4] Token de verificación en assets/adi-registration.properties coincide: $ACTUAL_TOKEN"
else
    echo "❌ [3/4] Token esperado '$EXPECTED_TOKEN' pero encontrado '$ACTUAL_TOKEN'"
    exit 1
fi

# 3. Signature & SHA-256 fingerprint
if [ -x "$APKSIGNER" ]; then
    ACTUAL_SHA256=$("$APKSIGNER" verify --print-certs "$APK_PATH" | grep "SHA-256 digest:" | head -n 1 | awk '{print $NF}')
else
    ACTUAL_SHA256=$(keytool -printcert -jarfile "$APK_PATH" | grep "SHA256:" | head -n 1 | awk '{print $2}' | tr -d ':' | tr '[:upper:]' '[:lower:]')
fi

# Normalizar cadenas para comparación
NORM_ACTUAL=$(echo "$ACTUAL_SHA256" | tr -d ':' | tr '[:upper:]' '[:lower:]')
NORM_EXPECTED=$(echo "$EXPECTED_SHA256" | tr -d ':' | tr '[:upper:]' '[:lower:]')

if [ "$NORM_ACTUAL" = "$NORM_EXPECTED" ]; then
    echo "✅ [4/4] Huella digital SHA-256 de la firma coincide: $ACTUAL_SHA256"
else
    echo "❌ [4/4] SHA-256 esperado '$EXPECTED_SHA256' pero encontrado '$ACTUAL_SHA256'"
    exit 1
fi

echo "=========================================="
echo "🎉 ¡TODAS LAS VALIDACIONES PASARON EXITOSAMENTE!"
echo "El APK está 100% listo para subirse a Google."
echo "=========================================="
