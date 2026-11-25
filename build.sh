#!/usr/bin/env bash
# Uso: ./build.sh [compilar|run|test]
set -e
cd "$(dirname "$0")"
# Usa javac si existe; si solo hay JRE, usa el modulo jdk.compiler
if command -v javac >/dev/null 2>&1; then JAVAC=(javac); else JAVAC=(java -m jdk.compiler/com.sun.tools.javac.Main); fi
case "${1:-compilar}" in
  compilar) mkdir -p out && "${JAVAC[@]}" -encoding UTF-8 -d out $(find src -name '*.java') ;;
  run)      "$0" compilar && java -cp out ascensor.Main ;;
  test)     "$0" compilar && java -cp out ascensor.PruebasSistema ;;
esac
