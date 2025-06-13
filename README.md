# 📦 Sistema de Gestión de Inventario y Órdenes de Compra

Esta aplicación permite gestionar productos, proveedores, ventas y órdenes de compra utilizando dos políticas de inventario: **lote fijo** e **intervalo fijo**. Incluye lógica de cálculo automático de campos clave (como punto de pedido, lote óptimo, inventario máximo, etc.) y una automatización para generar órdenes de compra basadas en la demanda y las ventas.

---

## 🔄 Flujo General de la Aplicación

1. **Creación de proveedores**
2. **Creación de productos** con sus datos base y elección del modelo de inventario (`LOTE_FIJO` o `INTERVALO_FIJO`)
3. **Asociación producto ↔ proveedor**: se pueden asociar múltiples proveedores a un producto, incluyendo costos, lead time, etc.
4. **Seteo de proveedor por defecto**: el sistema solo calcula los campos utilizando el proveedor predeterminado.
5. **Creación de ventas y órdenes de compra**: las ventas generan automáticamente eventos que pueden disparar la creación de órdenes de compra si el stock cae por debajo del punto de pedido.
6. **Finalización de órdenes de compra**: al finalizar una orden, se actualiza el stock del producto.

---

## ⚙️ Lógica de Cálculos Automáticos

La clase `CalculationService` se encarga de calcular:

- **Tamaño de lote óptimo**
- **Punto de pedido**
- **Inventario máximo**
- **Costo total**

> ⚠️ Estos cálculos solo se realizan cuando existe un **proveedor predeterminado** asociado al producto, ya que los costos, lead time y precio unitario son necesarios.

### 🧠 ¿Cuándo se hacen los cálculos?

- 🔄 **Al crear un `ProductProvider` y setearlo como default**
- ✏️ **Al actualizar un producto**
- ❌ **No se calculan al crear un producto**, ya que en ese momento no existen aún los datos de proveedor.

---

## 📡 Arquitectura de Eventos

Se utiliza un sistema basado en eventos para mantener sincronizado el stock y generar órdenes de compra automáticamente.

- **Eventos de venta (`SaleEvent`)**: reducen el stock y pueden generar automáticamente una orden de compra si el stock baja del punto de pedido.
- **Eventos de orden de compra (`PurchaseOrderEvent`)**: al finalizar una orden, se incrementa el stock del producto.
- 🔁 Hay un método `@Scheduled` en `PurchaseOrderService` que revisa diariamente si algún producto con política de `INTERVALO_FIJO` requiere una orden de compra.

---

## 📘 Endpoints disponibles

### 📦 Purchase Orders (`purchase-orders`)
- `GET /purchase-orders`
- `GET /purchase-orders/{id}`
- `POST /purchase-orders`
- `PUT /purchase-orders/{id}`
- `DELETE /purchase-orders/{id}`
- `POST /purchase-orders/{id}/send` → Envía una orden
- `POST /purchase-orders/{id}/finalize` → Finaliza una orden (actualiza stock)

### 🧾 Providers (`providers`)
- `GET /providers`
- `GET /providers/{id}`
- `POST /providers`
- `PUT /providers/{id}`
- `DELETE /providers/{id}`
- `GET /providers/product/{productId}` → Proveedores de un producto
- `GET /providers/active` → Proveedores activos

### 📦 Products (`products`)
- `GET /products`
- `GET /products/{id}`
- `POST /products`
- `PUT /products/{id}` → Realiza cálculos si hay proveedor predeterminado
- `DELETE /products/{id}`
- `GET /products/provider/{providerId}` → Productos de un proveedor
- `GET /products/belowSecurityStock` → Productos bajo stock de seguridad
- `GET /products/belowReorderPoint` → Productos bajo punto de pedido

### 🔗 Product-Providers (`product-providers`)
- `GET /api/product-providers`
- `GET /api/product-providers/{id}`
- `POST /api/product-providers` 
- `PUT /api/product-providers/{id}`  → Realiza cálculos si es el proveedor predeterminado
- `DELETE /api/product-providers/{id}`
- `PUT /api/product-providers/set-default/{id}` → Setea proveedor predeterminado → *desencadena cálculos automáticos*

### 🛒 Sales (`sales`)
- `GET /sales`
- `GET /sales/{id}`
- `POST /sales` → Publica evento para disminuir stock y generar orden si corresponde
- `DELETE /sales/{id}`
- `GET /sales/paged` → Paginado

---

## 🛠️ Tecnologías utilizadas

- **Spring Boot**
- **Spring Data JPA**
- **Eventos con `@EventListener`**
- **Tareas programadas con `@Scheduled`**
- **Java 17+**
- **PostgreSQL**
- **Docker (opcional)**

---
