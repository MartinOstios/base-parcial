package main;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import models.*;
import services.*;
import java.util.Date;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // PARTE 1 y 2: MongoDB - Esquema Normalizado
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase("parcial_db");

        // Inicializar servicios MongoDB
        ProductoService productoService = new ProductoService(database);
        PedidoService pedidoService = new PedidoService(database);
        DetallePedidoService detallePedidoService = new DetallePedidoService(database);
        ReservaService reservaService = new ReservaService(database);

        // PRIMERA PARTE: Esquema Normalizado
        System.out.println("ESQUEMA NORMALIZADO - PRODUCTOS Y PEDIDOS");
        System.out.println("----------------------------------------");

        // Ejemplo de creación de un producto
        Producto producto = new Producto(
            "producto001",
            "Camiseta de algodón",
            "Camiseta 100% de algodón",
            15.99,
            200
        );
        productoService.create(producto);

        // Ejemplo de creación de un pedido
        Pedido pedido = new Pedido(
            "pedido001",
            "cliente001",
            new Date(),
            "pendiente",
            150.0
        );
        pedidoService.create(pedido);

        // Ejemplo de creación de un detalle de pedido
        DetallePedido detallePedido = new DetallePedido(
            "detalle001",
            pedido.getId(),
            producto.getId(),
            2,
            15.99
        );
        detallePedidoService.create(detallePedido);

        // Consultas esquema normalizado
        System.out.println("\nConsultas esquema normalizado:");
        
        // 1. Obtener los productos con un precio mayor a 20 dólares
        System.out.println("\nProductos con precio mayor a 20 dólares:");
        List<Producto> productosCostosos = productoService.findByPrecioMayorA(20.0);
        for (Producto p : productosCostosos) {
            System.out.println(p.getNombre() + " - $" + p.getPrecio());
        }

        // 2. Obtener los pedidos que tengan un total mayor a 100 dólares
        System.out.println("\nPedidos con total mayor a 100 dólares:");
        List<Pedido> pedidosGrandes = pedidoService.findByTotalMayorA(100.0);
        for (Pedido p : pedidosGrandes) {
            System.out.println("Pedido ID: " + p.getId() + " - Total: $" + p.getTotal());
        }

        // 3. Obtener los pedidos en donde exista un detalle de pedido con el producto010
        System.out.println("\nPedidos con producto010:");
        List<DetallePedido> detallesProducto010 = detallePedidoService.findByProductoId("producto010");
        for (DetallePedido dp : detallesProducto010) {
            Pedido p = pedidoService.findById(dp.getPedidoId());
            if (p != null) {
                System.out.println("Pedido ID: " + p.getId() + " - Cliente: " + p.getCliente());
            }
        }

        // PARTE 3 y 4: MongoDB - Esquema Desnormalizado
        System.out.println("\n\nESQUEMA DESNORMALIZADO - RESERVAS DE HOTEL");
        System.out.println("------------------------------------------");

        // Crear cliente de ejemplo
        Cliente cliente = new Cliente(
            "Ana Gómez",
            "ana.gomez@example.com",
            "+54111223344",
            "Calle Ficticia"
        );

        // Crear habitación de ejemplo
        Habitacion habitacion = new Habitacion(
            "Suite",
            101,
            200.00,
            2,
            "Suite de lujo con vista al mar"
        );

        // Crear reserva de ejemplo
        Reserva reserva = new Reserva(
            "reserva001",
            cliente,
            habitacion,
            new Date(),
            new Date(),
            400.00,
            "PAGADO",
            "TARJETA",
            new Date()
        );

        // Crear la reserva en la base de datos
        System.out.println("Creando reserva de ejemplo...");
        reservaService.create(reserva);

        // Consultas esquema desnormalizado
        System.out.println("\nConsultas esquema desnormalizado:");

        // 1. Obtener las habitaciones reservadas de tipo "Sencilla"
        System.out.println("\nReservas de habitaciones tipo Sencilla:");
        List<Reserva> reservasSencillas = reservaService.findByTipoHabitacion("Sencilla");
        for (Reserva r : reservasSencillas) {
            System.out.println("Reserva ID: " + r.getId() + 
                             " - Cliente: " + r.getCliente().getNombre() + 
                             " - Habitación: " + r.getHabitacion().getNumero());
        }

        // 2. Obtener la sumatoria total de las reservas pagadas
        System.out.println("\nTotal de reservas pagadas:");
        double totalReservasPagadas = reservaService.getSumaTotalReservasPagadas();
        System.out.println("Total: $" + totalReservasPagadas);

        // 3. Obtener las reservas de las habitaciones con un precio_noche mayor a 100 dolares
        System.out.println("\nReservas de habitaciones con precio por noche mayor a $100:");
        List<Reserva> reservasCaras = reservaService.findByPrecioNocheMayorA(100.0);
        for (Reserva r : reservasCaras) {
            System.out.println("Reserva ID: " + r.getId() + 
                             " - Cliente: " + r.getCliente().getNombre() +
                             " - Habitación: " + r.getHabitacion().getNumero() + 
                             " - Precio/Noche: $" + r.getHabitacion().getPrecioNoche());
        }

        // PARTE 5: Neo4j - Base de datos de grafos
        System.out.println("\n\nBASE DE DATOS DE GRAFOS - RED SOCIAL");
        System.out.println("-------------------------------------");

        // Inicializar servicio de Neo4j
        try (PersonaService personaService = new PersonaService(
                "neo4j://localhost:7687", "neo4j", "martin123")) {

            // Crear dos personas
            Persona persona1 = new Persona(
                "1",
                "Juan Pérez",
                "juan@email.com",
                25,
                "Bogotá"
            );

            Persona persona2 = new Persona(
                "2",
                "María García",
                "maria@email.com",
                30,
                "Medellín"
            );

            // Crear las personas en Neo4j
            System.out.println("\nCreando personas en la base de datos...");
            personaService.crearPersona(persona1);
            personaService.crearPersona(persona2);

            // Crear un comentario entre las personas
            System.out.println("Creando comentario entre personas...");
            personaService.crearComentario(
                persona1.getId(),
                persona2.getId(),
                "¡Excelente publicación! Me gustó mucho tu análisis."
            );

            // Obtener los comentarios realizados por Juan
            System.out.println("\nComentarios realizados por " + persona1.getNombre() + ":");
            personaService.obtenerComentariosDePersona(persona1.getId());

        } catch (Exception e) {
            System.err.println("Error al conectar con Neo4j: " + e.getMessage());
        }

        // Cerrar conexión MongoDB
        mongoClient.close();
    }
} 