"""
SECCT — Simulador Mock del Servicio de Inferencia CNN (Puerto 8000).

Este script simula el servicio de visión por computador para que el Backend (HU-04)
pueda evaluar unidades sin requerir TensorFlow ni dependencias pesadas.

Contrato simulado:
  POST http://localhost:8000/evaluar
  Request:  { "imagen_base64": "<bytes en base64>" }
  Response: { "partes": [ 
              { "parte": "OJO", "categoria_ntc": 1-4, "confianza": 0.0-1.0 },
              { "parte": "PIEL", "categoria_ntc": 1-4, "confianza": 0.0-1.0 }
            ] }
"""

import json
import random
from http.server import HTTPServer, BaseHTTPRequestHandler

class MockCNNHandler(BaseHTTPRequestHandler):
    def log_message(self, format, *args):
        # Silenciar logs ruidosos por defecto de BaseHTTPRequestHandler
        pass

    def do_OPTIONS(self):
        self.send_response(200)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "*")
        self.end_headers()

    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        info = {
            "servicio": "SECCT Simulador Mock CNN",
            "puerto": 8000,
            "estado": "ACTIVO",
            "endpoint": "POST /evaluar"
        }
        self.wfile.write(json.dumps(info, ensure_ascii=False).encode("utf-8"))

    def do_POST(self):
        if self.path in ("/evaluar", "/evaluar/"):
            try:
                content_length = int(self.headers.get("Content-Length", 0))
                _ = self.rfile.read(content_length)

                # Generar respuestas coherentes según NTC 1443:
                # Categorías posibles: 1 (Extra), 2 (Primera), 3 (Segunda), 4 (No apta)
                # En un lote fresco predominan categorías 1 y 2 con alta confianza.
                categoria_ojo = random.choice([1, 1, 1, 2])
                categoria_piel = random.choice([1, 1, 2, 2])
                confianza_ojo = round(random.uniform(0.88, 0.97), 4)
                confianza_piel = round(random.uniform(0.86, 0.95), 4)

                respuesta = {
                    "partes": [
                        {
                            "parte": "OJO",
                            "categoria_ntc": categoria_ojo,
                            "confianza": confianza_ojo
                        },
                        {
                            "parte": "PIEL",
                            "categoria_ntc": categoria_piel,
                            "confianza": confianza_piel
                        }
                    ]
                }

                self.send_response(200)
                self.send_header("Content-Type", "application/json; charset=utf-8")
                self.send_header("Access-Control-Allow-Origin", "*")
                self.end_headers()
                self.wfile.write(json.dumps(respuesta).encode("utf-8"))

                print(f"[MOCK CNN] [OK] Unidad evaluada con exito -> OJO: Cat {categoria_ojo} ({confianza_ojo*100:.1f}%), PIEL: Cat {categoria_piel} ({confianza_piel*100:.1f}%)")

            except Exception as e:
                self.send_response(500)
                self.send_header("Content-Type", "application/json")
                self.end_headers()
                self.wfile.write(json.dumps({"error": str(e)}).encode("utf-8"))
        else:
            self.send_response(404)
            self.end_headers()

def main():
    puerto = 8000
    servidor = HTTPServer(("0.0.0.0", puerto), MockCNNHandler)
    print("==================================================================")
    print("   SECCT · SERVIDOR SIMULADOR CNN (MOCK IA)")
    print("==================================================================")
    print(f" * Escuchando en: http://localhost:{puerto}/evaluar")
    print(" * Cero dependencias: usa la biblioteca estándar de Python.")
    print(" * Listo para recibir evaluaciones de HU-04 desde Spring Boot.")
    print(" * Presiona Ctrl+C para detener.")
    print("==================================================================")
    try:
        servidor.serve_forever()
    except KeyboardInterrupt:
        print("\n[MOCK CNN] Servidor detenido por el usuario.")

if __name__ == "__main__":
    main()
