#!/bin/bash

# Script de Testing para Reseñas de Proveedores
# Ejecutar desde la terminal: bash test-provider-reviews.sh

BASE_URL="http://localhost:8084/api/reviews"
USER_ID=1
PROVIDER_ID=123

echo "==================================="
echo "TESTING: Reseñas de Proveedores"
echo "==================================="
echo ""

# Test 1: Crear reseña de proveedor
echo "📝 Test 1: Crear reseña de proveedor..."
curl -X POST "${BASE_URL}/provider" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: ${USER_ID}" \
  -d "{
    \"providerId\": ${PROVIDER_ID},
    \"rating\": 5,
    \"comment\": \"Excelente proveedor, muy profesional y puntual\"
  }" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "-----------------------------------"
echo ""

# Test 2: Obtener todas las reseñas del proveedor
echo "📖 Test 2: Obtener reseñas del proveedor (paginadas)..."
curl -X GET "${BASE_URL}/provider/${PROVIDER_ID}?page=0&size=10&sortBy=createdAt" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "-----------------------------------"
echo ""

# Test 3: Obtener rating del proveedor
echo "⭐ Test 3: Obtener rating del proveedor..."
curl -X GET "${BASE_URL}/provider/${PROVIDER_ID}/rating" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "-----------------------------------"
echo ""

# Test 4: Obtener reseñas recientes
echo "🕒 Test 4: Obtener reseñas recientes (últimas 5)..."
curl -X GET "${BASE_URL}/provider/${PROVIDER_ID}/recent?limit=5" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "-----------------------------------"
echo ""

# Test 5: Intentar crear reseña duplicada (debe fallar con 409)
echo "❌ Test 5: Intentar crear reseña duplicada (debe fallar)..."
curl -X POST "${BASE_URL}/provider" \
  -H "Content-Type: application/json" \
  -H "X-User-Id: ${USER_ID}" \
  -d "{
    \"providerId\": ${PROVIDER_ID},
    \"rating\": 4,
    \"comment\": \"Segunda reseña (no debería permitirse)\"
  }" \
  -w "\nHTTP Status: %{http_code}\n" \
  -s | jq '.'

echo ""
echo "==================================="
echo "✅ Tests completados"
echo "==================================="
