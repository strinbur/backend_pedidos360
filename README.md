# Pedidos360 – Backend

Microservicios con Spring Boot 4 (Java 25), RabbitMQ y Kafka (KRaft).

## Diagramas

c4 nivel 1
<img width="8192" height="6587" alt="Pedidos360 Web Commerce Architecture-2026-10-15-200056" src="https://github.com/user-attachments/assets/a4e8d504-562e-4a04-9650-b9b8d0ff4077" />
c4 nivel 2
<img width="4320" height="8192" alt="Pedidos360 Checkout Microservices Architecture-2026-10-15-200323" src="https://github.com/user-attachments/assets/d398d9a6-f20d-41a4-96b3-a500b15dd12d" />

## Servicios y puertos

| Servicio | Puerto | Descripción |
|---|---|---|
| product | 8081 | CRUD de productos (MongoDB), seguridad JWT (Entra ID). Descuenta stock (RabbitMQ), publica `orders.result` y `stock.low`, y procesa flujos con Kafka Streams (totales, velocidad de venta y alertas). |
| cart | 8082 | Cálculo y checkout del carrito. Publica `orders.checkout`; consume `stock.low` y `orders.result`. |
| rabbitadmin | 8083 | Administración de colas, exchanges y bindings de RabbitMQ. |
| kafkaadmin | 8084 | Administración de tópicos y configuraciones de Kafka. |
| Kafka-UI | 8090 | Panel web de brokers y tópicos. |
| RabbitMQ | 5672 / 5673 | Clúster de 2 nodos (paneles en 15672 / 15673). |
| Kafka | 9092 / 9094 / 9095 | Clúster KRaft de 3 brokers (acceso desde el host). |

## Requisitos

- Docker Desktop
- MongoDB local en el puerto 27017 (`product` se conecta vía `host.docker.internal`)

## Archivos Docker Compose

| Archivo | Contenido |
|---|---|
| `docker-compose.yml` | RabbitMQ (2 nodos), product, cart, rabbitadmin, kafkaadmin |
| `docker-compose.kafka.yml` | Kafka con 3 brokers en modo KRaft (sin Zookeeper) |
| `docker-compose.kafka-ui.yml` | Kafka-UI |

Los tres comparten la red externa `app-net`.

## Cómo levantar

```bash
# Una sola vez
docker network create app-net

# En este orden
docker compose -f docker-compose.kafka.yml up -d
docker compose -f docker-compose.kafka-ui.yml up -d
docker compose up -d --build
```

Para apagar sin perder datos: `docker compose down` (en cada archivo, sin `-v`).

Los tópicos de Kafka deben crearse una vez desde `kafkaadmin` (ver la sección de tópicos y la API de kafkaadmin).

## Control concurrente de inventario

1. **Validación previa (cart → product, REST):** el checkout valida producto y stock disponible. Es una comprobación rápida, no definitiva.
2. **Descuento atómico (RabbitMQ):** cart publica el checkout en `shop.direct`; product descuenta con un `$inc` condicionado (`stock >= cantidad`) en MongoDB. Dos compras simultáneas nunca dejan el stock negativo; si un ítem falla, se devuelve el stock de los anteriores.
3. **Resultado explícito (Kafka):** tras descontar, product publica `orders.result` (`CONFIRMED` o `REJECTED`). Cart lo expone en `GET /cart/orders/{cartId}`.
4. **Alertas de quiebre (Kafka):** cada cambio de stock que cruza el umbral (5) publica `stock.low` con nivel `LOW`, `OUT` (0) u `OK` (recuperado). Un stream los deduplica en `stock.alerts` y los empuja en vivo por SSE.

Los estados de una venta en `sales_history` son `PENDIENTE` (llegó `orders.checkout`), `CONFIRMADA` o `RECHAZADA`. Ambos mensajes llegan por brokers distintos y pueden hacerlo en cualquier orden: las dos escrituras son *upserts* sobre el mismo `_id` (`cartId:productId`), por lo que el orden no importa.

## Tópicos de Kafka

| Tópico | Productor → Consumidor | Key | Contenido |
|---|---|---|---|
| `orders.checkout` | cart → product | `cartId` | Pedido recibido; product lo guarda como `PENDIENTE`. |
| `orders.result` | product → cart, Streams | `cartId` | Resultado del descuento: `CONFIRMED` / `REJECTED` y motivo. |
| `stock.low` | product → cart, Streams | `productId` | Cambio de nivel de stock: `LOW` / `OUT` / `OK`. |
| `sales.totals` | Streams → (consulta) | `productId` | Unidades vendidas acumuladas por producto. |
| `sales.velocity` | Streams → (consulta) | `productId` | Unidades vendidas por producto en ventanas de 5 minutos. |
| `stock.alerts` | Streams → SSE | `productId` | Alertas deduplicadas: solo cuando cambia el nivel. |
| `orders.checkout.dlt` | product → (revisión manual) | `cartId` | Mensajes de `orders.checkout` que fallaron tras los reintentos. |

Los tópicos se crean desde `kafkaadmin` (la auto-creación está desactivada), con 3 particiones y `replicationFactor` 3. Kafka Streams crea además sus tópicos internos (`product-sales-streams-v2-*`).

## Kafka Streams (en product)

`application-id`: `product-sales-streams-v2`, con `processing.guarantee: exactly_once_v2` (un reinicio no cuenta una venta dos veces).

- **Totales:** lee `orders.result`, descarta los `REJECTED`, separa los ítems y acumula unidades por producto → `sales.totals`.
- **Velocidad:** mismas ventas, agrupadas en ventanas de 5 minutos → `sales.velocity`.
- **Alertas:** lee `stock.low`, guarda el último nivel por producto y emite a `stock.alerts` solo cuando el nivel cambia. Su store tiene el caché desactivado para no perder transiciones.

## API de estadísticas (product, requiere rol ADMIN)

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/products/stats/top-sellers?limit=5` | Productos más vendidos, de mayor a menor. |
| GET | `/products/stats/stock-alerts` | Productos en `LOW` u `OUT` en este momento. |
| GET | `/products/stats/alerts/stream` | SSE: cada alerta de `stock.alerts` en tiempo real. |

## API de cart

| Método | Ruta | Acción |
|---|---|---|
| POST | `/cart/calculate` | Calcula el carrito. |
| POST | `/cart/checkout` | Responde 202 con `cartId` y estado `PENDIENTE`. |
| GET | `/cart/orders/{cartId}` | `PENDIENTE`, `CONFIRMADA` o `RECHAZADA`, con motivo. |

## API de kafkaadmin

| Método | Ruta | Acción |
|---|---|---|
| POST | `/topics` | Crear tópico |
| GET | `/topics` | Listar tópicos |
| GET | `/topics/{name}` | Describir tópico (particiones, líder, configs) |
| PUT | `/topics/{name}/configs` | Modificar configuraciones |
| DELETE | `/topics/{name}/configs/{key}` | Eliminar una configuración (vuelve al valor por defecto) |
| PUT | `/topics/{name}/partitions` | Aumentar particiones |
| DELETE | `/topics/{name}` | Eliminar tópico |

Los tópicos internos (prefijo `__`) están protegidos.

Ejemplo:

```json
POST /topics
{ "name": "stock.low", "partitions": 3, "replicationFactor": 3, "configs": { "retention.ms": "604800000" } }
```

## Cómo probar el flujo

```powershell
# 1. Checkout -> cartId
$body = @{ items = @( @{ productId = "<ID>"; quantity = 1 } ) } | ConvertTo-Json -Depth 5
$r = Invoke-RestMethod -Method Post -Uri http://localhost:8082/cart/checkout -ContentType "application/json" -Body $body

# 2. Resultado del pedido
Invoke-RestMethod http://localhost:8082/cart/orders/$($r.cartId)

# 3. Tópicos (ejemplo)
docker exec kafka1 /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server kafka1:19092 --topic stock.alerts --from-beginning --timeout-ms 8000
```

Los endpoints de estadísticas requieren un token de admin (`Authorization: Bearer <token>`):

```powershell
curl.exe -s -H "Authorization: Bearer $token" http://localhost:8081/products/stats/top-sellers
curl.exe -s -H "Authorization: Bearer $token" http://localhost:8081/products/stats/stock-alerts
curl.exe -N -H "Authorization: Bearer $token" http://localhost:8081/products/stats/alerts/stream
```