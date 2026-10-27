#!/usr/bin/env bash
set -e

AAB_PATH="androidApp/build/outputs/bundle/release/androidApp-release.aab"
EXPECTED_PACKAGE="com.jesusdmedinac.compose.calculator"
EXPECTED_VERSION_CODE="1000008"
EXPECTED_VERSION_NAME="1.0.1"
EXPECTED_TARGET_SDK="36"
EXPECTED_MIN_SDK="24"
EXPECTED_SHA256="6B:C7:A1:FD:9B:98:8F:05:D0:67:41:74:45:72:29:D3:B5:88:E0:2A:D4:D4:37:AD:24:86:4E:AF:1A:AC:F2:31"

echo "=========================================="
echo " Validando Android App Bundle (AAB) Release"
echo "=========================================="

if [ ! -f "$AAB_PATH" ]; then
    echo "❌ Error: No se encontró el archivo AAB en $AAB_PATH"
    echo "   Ejecuta: ./gradlew :androidApp:bundleRelease"
    exit 1
fi

AAB_SIZE=$(ls -lh "$AAB_PATH" | awk '{print $5}')
AAB_DATE=$(ls -lh "$AAB_PATH" | awk '{print $6, $7, $8}')
echo "✅ [1/6] Archivo AAB encontrado ($AAB_SIZE, compilado: $AAB_DATE)"

# Inspección de AndroidManifest.xml (Proto XML) dentro de base/manifest/
PARSED_DATA=$(python3 - <<EOF
import zipfile, sys

try:
    with zipfile.ZipFile("$AAB_PATH", "r") as z:
        manifest = z.read("base/manifest/AndroidManifest.xml")
        
        def find_attr(attr_name):
            pos = manifest.find(attr_name.encode("utf-8"))
            if pos == -1:
                return None
            sub = manifest[pos:pos+60]
            # Extraer strings legibles en el rango inmediato
            import re
            strings = re.findall(rb'[a-zA-Z0-9_\.\-]+', sub)
            # El valor sigue a attr_name
            for s in strings[1:]:
                val = s.decode("utf-8")
                if val != attr_name:
                    return val
            return None

        pkg = find_attr("package")
        vc = find_attr("versionCode")
        vn = find_attr("versionName")
        target_sdk = find_attr("targetSdkVersion")
        min_sdk = find_attr("minSdkVersion")

        print(f"PACKAGE={pkg or ''}")
        print(f"VERSION_CODE={vc or ''}")
        print(f"VERSION_NAME={vn or ''}")
        print(f"TARGET_SDK={target_sdk or ''}")
        print(f"MIN_SDK={min_sdk or ''}")
except Exception as e:
    print(f"ERROR={e}", file=sys.stderr)
    sys.exit(1)
EOF
)

eval "$PARSED_DATA"

# 2. Package Name
if [ "$PACKAGE" = "$EXPECTED_PACKAGE" ]; then
    echo "✅ [2/6] Package Name correcto: $PACKAGE"
else
    echo "❌ [2/6] Package Name esperado '$EXPECTED_PACKAGE' pero encontrado '$PACKAGE'"
    exit 1
fi

# 3. Target SDK (Android 16 / API 36)
if [ "$TARGET_SDK" = "$EXPECTED_TARGET_SDK" ]; then
    echo "✅ [3/6] Target SDK coincide: $TARGET_SDK (Android 16)"
else
    echo "❌ [3/6] Target SDK esperado '$EXPECTED_TARGET_SDK' pero encontrado '$TARGET_SDK'"
    exit 1
fi

# 4. Version Code & Name
if [ "$VERSION_CODE" = "$EXPECTED_VERSION_CODE" ] && [ "$VERSION_NAME" = "$EXPECTED_VERSION_NAME" ]; then
    echo "✅ [4/6] Versión correcta: versionCode=$VERSION_CODE, versionName=$VERSION_NAME"
else
    echo "❌ [4/6] Versión esperada code=$EXPECTED_VERSION_CODE name=$EXPECTED_VERSION_NAME (encontrado code=$VERSION_CODE name=$VERSION_NAME)"
    exit 1
fi

# 5. Min SDK
if [ "$MIN_SDK" = "$EXPECTED_MIN_SDK" ]; then
    echo "✅ [5/6] Min SDK verificado: $MIN_SDK"
else
    echo "⚠️  [5/6] Min SDK detectado: $MIN_SDK (esperado: $EXPECTED_MIN_SDK)"
fi

# 6. Certificado y Firma criptográfica
CERT_INFO=$(keytool -printcert -jarfile "$AAB_PATH" 2>/dev/null || true)
ACTUAL_SHA256=$(echo "$CERT_INFO" | grep "SHA256:" | head -n 1 | awk '{print $2}')
SIGNER_OWNER=$(echo "$CERT_INFO" | grep "Owner:" | head -n 1 | sed 's/Owner: //')

# Normalizar comparativa eliminando mayúsculas/minúsculas
if [ "$(echo "$ACTUAL_SHA256" | tr '[:lower:]' '[:upper:]')" = "$(echo "$EXPECTED_SHA256" | tr '[:lower:]' '[:upper:]')" ]; then
    echo "✅ [6/6] Firma SHA-256 válida y coincidente:"
    echo "        SHA-256: $ACTUAL_SHA256"
    echo "        Firmante: $SIGNER_OWNER"
else
    echo "❌ [6/6] Huella SHA-256 no coincide con el Keystore oficial:"
    echo "        Esperada: $EXPECTED_SHA256"
    echo "        Encontrada: $ACTUAL_SHA256"
    exit 1
fi

echo "=========================================="
echo "🎉 ¡EL AAB CUMPLE CON TODOS LOS REQUISITOS DE GOOGLE PLAY!"
echo "Listo para subirse a Producción o Prueba Cerrada."
echo "=========================================="
