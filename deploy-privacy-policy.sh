#!/usr/bin/env bash
set -e

echo "Deploying privacy policy to Cloudflare Pages..."
npx wrangler pages deploy docs --project-name compose-calculator --branch main --commit-dirty=true
echo "Deployed to: https://compose-calculator.pages.dev"
