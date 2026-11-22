# Nexo Parking · Frontend

Frontend web del parqueadero **Nexo Parking**. Está hecho con **Spring Boot + Thymeleaf** y consume con **OpenFeign** las APIs REST del backend ([Nexo_parking](https://github.com/santiagopalacio29/Nexo_parking)).

Los dos proyectos están separados y se comunican por HTTP:

```
Navegador ──► Frontend (localhost:8091) ──Feign/HTTP──► Backend (localhost:8080/parking-api) ──► MySQL
```

## Cómo ejecutarlo

1. Levantar el **backend** (`Nexo_parking/parking`) con su base de datos MySQL. Queda en `http://localhost:8080/parking-api` (el back usa `server.servlet.context-path=/parking-api`).
2. Levantar este **frontend**:
   ```bash
   ./gradlew bootRun        # Windows: gradlew.bat bootRun
   ```
   También se puede correr `FrontendNexoParkingApplication` desde IntelliJ.
3. Abrir `http://localhost:8091`.

Si el backend corre en otra dirección, se cambia en `src/main/resources/application.yaml` (`nexo.api.base-url`) o al arrancar:
`./gradlew bootRun --args="--nexo.api.base-url=http://192.168.1.10:8080/parking-api"`

## Funcionalidades

| Pantalla | Ruta | APIs del backend que consume |
|---|---|---|
| Panel (indicadores y mapa de espacios) | `/` | `GET /api/espacio`, `GET /api/parqueo`, `GET /api/factura` |
| Entradas y salidas | `/parqueo` | `POST /api/parqueo/entrada`, `PUT /api/parqueo/{id}/salida`, `GET /api/parqueo`, `GET /api/vehiculo`, `GET /api/espacio` |
| Espacios | `/espacios` | `GET /api/espacio`, `POST /api/espacio`, `PUT /api/espacio/{id}` |
| Vehículos | `/vehiculos` | `GET/POST /api/vehiculo`, `GET/PUT/DELETE /api/vehiculo/{placa}` |
| Clientes | `/clientes` | `GET/POST /api/clientes`, `GET/PUT/DELETE /api/clientes/{id}` |
| Tarifas | `/tarifas` | `GET/POST /api/tarifa`, `GET/PUT/DELETE /api/tarifa/{id}` |
| Facturas | `/facturas` | `GET /api/factura`, `GET/DELETE /api/factura/{id}`, `GET /api/parqueo/{id}` |

Flujo principal: **registrar entrada** (vehículo + espacio libre) → el espacio queda ocupado → **registrar salida** con el método de pago → el backend calcula las horas, cobra según la tarifa, genera la factura y libera el espacio → se muestra el comprobante.

## Estructura

```
src/main/java/com/nexoparking/frontend
├── clients/         Interfaces Feign, una por cada API del backend
├── controllers/     Controladores MVC de cada pantalla
├── services/        Lógica de presentación (indicadores, filtros, orden) con su interfaz
├── dtos/            Objetos que viajan entre el front y el back
├── errors/          Traducción de errores HTTP y manejo global de excepciones
└── configuration/   Beans de configuración
src/main/resources
├── templates/       Vistas Thymeleaf (Bootstrap 5) y fragmentos comunes
└── static/css/      Estilos
```

## Manejo de errores

- Si el backend está apagado o responde con error en una consulta, se muestra una página explicando el problema.
- Si falla un formulario, se vuelve al listado con el mensaje traducido (400 datos inválidos, 404 no existe, 406 no aceptado, 500 error del backend).

## Pruebas

```bash
./gradlew test
```

Hay pruebas unitarias con Mockito para `ParqueaderoService` y para el manejo de errores.
