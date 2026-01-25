#!/bin/bash
# Script to generate API client with retry logic for CI/CD environments
# Handles cases where backend might be deploying simultaneously

OPENAPI_URL="${OPENAPI_URL:-http://localhost:8080/api-docs}"
MAX_RETRIES=12
RETRY_DELAY=10

echo "Fetching OpenAPI spec from: $OPENAPI_URL"

for i in $(seq 1 $MAX_RETRIES); do
  echo "Attempt $i/$MAX_RETRIES..."

  if curl -sf --connect-timeout 10 --max-time 30 "$OPENAPI_URL" -o openapi.json; then
    echo "✅ Successfully fetched OpenAPI spec"

    # Generate the client
    echo "Generating TypeScript client..."
    pnpm openapi-generator-cli generate \
      -i openapi.json \
      -g typescript-axios \
      -o ./lib/api-client \
      --additional-properties=supportsES6=true,typescriptThreePlus=true

    exit 0
  fi

  if [ $i -lt $MAX_RETRIES ]; then
    echo "⏳ Backend not ready, waiting ${RETRY_DELAY}s before retry..."
    sleep $RETRY_DELAY
  fi
done

echo "❌ Failed to fetch OpenAPI spec after $MAX_RETRIES attempts"
exit 1
