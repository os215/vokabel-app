#!/bin/zsh
# Script to resize icon-1024.png to all required icon sizes and create favicon.ico
# Uses macOS native 'sips' command for image processing

set -e

# Base paths
STATIC_DIR="../../../src/main/resources/static"
SOURCE_IMAGE="${STATIC_DIR}/icon-1024.png"

# Check if source image exists
if [ ! -f "$SOURCE_IMAGE" ]; then
    echo "Error: Source image not found: $SOURCE_IMAGE"
    exit 1
fi

echo "Source image: $SOURCE_IMAGE"
echo "--------------------------------------------------"

# Resize PNG icons using sips
echo "Creating icon-120.png at 120x120..."
sips -z 120 120 "$SOURCE_IMAGE" --out "${STATIC_DIR}/icon-120.png" > /dev/null
echo "✓ Created ${STATIC_DIR}/icon-120.png"

echo "Creating icon-180.png at 180x180..."
sips -z 180 180 "$SOURCE_IMAGE" --out "${STATIC_DIR}/icon-180.png" > /dev/null
echo "✓ Created ${STATIC_DIR}/icon-180.png"

echo "Creating icon-192.png at 192x192..."
sips -z 192 192 "$SOURCE_IMAGE" --out "${STATIC_DIR}/icon-192.png" > /dev/null
echo "✓ Created ${STATIC_DIR}/icon-192.png"

echo "Creating icon-512.png at 512x512..."
sips -z 512 512 "$SOURCE_IMAGE" --out "${STATIC_DIR}/icon-512.png" > /dev/null
echo "✓ Created ${STATIC_DIR}/icon-512.png"

echo "--------------------------------------------------"

# Create favicon.ico
# First create temporary 32x32 PNG, then convert to ICO
echo "Creating favicon.ico at 32x32..."
TEMP_FAVICON="${STATIC_DIR}/temp-favicon-32.png"
sips -z 32 32 "$SOURCE_IMAGE" --out "$TEMP_FAVICON" > /dev/null

# Convert PNG to ICO format using sips
sips -s format icns "$TEMP_FAVICON" --out "${STATIC_DIR}/favicon.icns" > /dev/null 2>&1 || true

# Try to create ICO from PNG (sips can do this on some macOS versions)
if sips -s format ico "$TEMP_FAVICON" --out "${STATIC_DIR}/favicon.ico" > /dev/null 2>&1; then
    echo "✓ Created ${STATIC_DIR}/favicon.ico"
else
    # Fallback: just copy the 32x32 PNG as favicon (browsers support PNG favicons)
    cp "$TEMP_FAVICON" "${STATIC_DIR}/favicon.ico"
    echo "✓ Created ${STATIC_DIR}/favicon.ico (as PNG - browsers support this)"
fi

# Clean up temporary files
rm -f "$TEMP_FAVICON"
rm -f "${STATIC_DIR}/favicon.icns"

echo "--------------------------------------------------"
echo "✓ All icons created successfully!"

