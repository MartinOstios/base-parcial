package services;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.AggregateIterable;
import models.Reserva;
import models.Habitacion;
import models.Cliente;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class ReservaService {
    private final MongoCollection<Document> collection;

    public ReservaService(MongoDatabase database) {
        this.collection = database.getCollection("reservas");
    }

    // Create
    public void create(Reserva reserva) {
        Document clienteDoc = new Document()
                .append("nombre", reserva.getCliente().getNombre())
                .append("correo", reserva.getCliente().getCorreo())
                .append("telefono", reserva.getCliente().getTelefono())
                .append("direccion", reserva.getCliente().getDireccion());

        Document habitacionDoc = new Document()
                .append("tipo", reserva.getHabitacion().getTipo())
                .append("numero", reserva.getHabitacion().getNumero())
                .append("precio_noche", reserva.getHabitacion().getPrecioNoche())
                .append("capacidad", reserva.getHabitacion().getCapacidad())
                .append("descripcion", reserva.getHabitacion().getDescripcion());

        Document doc = new Document("_id", reserva.getId())
                .append("cliente", clienteDoc)
                .append("habitacion", habitacionDoc)
                .append("fecha_entrada", reserva.getFechaEntrada())
                .append("fecha_salida", reserva.getFechaSalida())
                .append("total", reserva.getTotal())
                .append("estado_pago", reserva.getEstadoPago())
                .append("metodo_pago", reserva.getMetodoPago())
                .append("fecha_reserva", reserva.getFechaReserva());
        
        collection.insertOne(doc);
    }

    // Read
    public Reserva findById(String id) {
        Document doc = collection.find(Filters.eq("_id", id)).first();
        if (doc != null) {
            return documentToReserva(doc);
        }
        return null;
    }

    public List<Reserva> findAll() {
        List<Reserva> reservas = new ArrayList<>();
        for (Document doc : collection.find()) {
            reservas.add(documentToReserva(doc));
        }
        return reservas;
    }

    // Update
    public void update(Reserva reserva) {
        Document clienteDoc = new Document()
                .append("nombre", reserva.getCliente().getNombre())
                .append("correo", reserva.getCliente().getCorreo())
                .append("telefono", reserva.getCliente().getTelefono())
                .append("direccion", reserva.getCliente().getDireccion());

        Document habitacionDoc = new Document()
                .append("tipo", reserva.getHabitacion().getTipo())
                .append("numero", reserva.getHabitacion().getNumero())
                .append("precio_noche", reserva.getHabitacion().getPrecioNoche())
                .append("capacidad", reserva.getHabitacion().getCapacidad())
                .append("descripcion", reserva.getHabitacion().getDescripcion());

        Document doc = new Document()
                .append("cliente", clienteDoc)
                .append("habitacion", habitacionDoc)
                .append("fecha_entrada", reserva.getFechaEntrada())
                .append("fecha_salida", reserva.getFechaSalida())
                .append("total", reserva.getTotal())
                .append("estado_pago", reserva.getEstadoPago())
                .append("metodo_pago", reserva.getMetodoPago())
                .append("fecha_reserva", reserva.getFechaReserva());

        collection.updateOne(Filters.eq("_id", reserva.getId()), new Document("$set", doc));
    }

    // Delete
    public void delete(String id) {
        collection.deleteOne(Filters.eq("_id", id));
    }

    // Consultas específicas
    public List<Reserva> findByTipoHabitacion(String tipo) {
        List<Reserva> reservas = new ArrayList<>();
        for (Document doc : collection.find(Filters.eq("habitacion.tipo", tipo))) {
            reservas.add(documentToReserva(doc));
        }
        return reservas;
    }

    public double getSumaTotalReservasPagadas() {
        List<Document> pipeline = new ArrayList<>();
        pipeline.add(new Document("$match", new Document("estado_pago", "PAGADO")));
        pipeline.add(new Document("$group", new Document("_id", null)
                .append("total", new Document("$sum", "$total"))));

        AggregateIterable<Document> result = collection.aggregate(pipeline);
        Document doc = result.first();
        return doc != null ? doc.getDouble("total") : 0.0;
    }

    public List<Reserva> findByPrecioNocheMayorA(double precio) {
        List<Reserva> reservas = new ArrayList<>();
        for (Document doc : collection.find(Filters.gt("habitacion.precio_noche", precio))) {
            reservas.add(documentToReserva(doc));
        }
        return reservas;
    }

    private Reserva documentToReserva(Document doc) {
        Document clienteDoc = (Document) doc.get("cliente");
        Cliente cliente = new Cliente(
            clienteDoc.getString("nombre"),
            clienteDoc.getString("correo"),
            clienteDoc.getString("telefono"),
            clienteDoc.getString("direccion")
        );

        Document habDoc = (Document) doc.get("habitacion");
        Habitacion habitacion = new Habitacion(
            habDoc.getString("tipo"),
            habDoc.getInteger("numero"),
            habDoc.getDouble("precio_noche"),
            habDoc.getInteger("capacidad"),
            habDoc.getString("descripcion")
        );

        return new Reserva(
            doc.getString("_id"),
            cliente,
            habitacion,
            doc.getDate("fecha_entrada"),
            doc.getDate("fecha_salida"),
            doc.getDouble("total"),
            doc.getString("estado_pago"),
            doc.getString("metodo_pago"),
            doc.getDate("fecha_reserva")
        );
    }
} 