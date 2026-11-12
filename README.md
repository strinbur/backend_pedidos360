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
| product | 8081 | CRUD de productos (MongoDB), seguridad JWT (Entra ID). Consume checkout (RabbitMQ), publica `stock.low` y consume `orders.checkout` (Kafka). |
| cart | 8082 | Cálculo y checkout del carrito. Publica `orders.checkout` y consume `stock.low` (Kafka). |
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

## Flujo con Kafka

- **cart → product (`orders.checkout`)**: al aceptarse un checkout, cart publica la orden (key = `cartId`). product la consume y guarda una venta por producto en la colección `sales_history`.
- **product → cart (`stock.low`)**: tras descontar stock (por RabbitMQ), si un producto queda bajo el umbral (5), product publica el aviso (key = `productId`). cart lo consume y lo usa como último stock conocido si product no responde.

Los tópicos se crean desde `kafkaadmin` (la auto-creación está desactivada).

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
