package services;

import models.Persona;
import org.neo4j.driver.*;
import static org.neo4j.driver.Values.parameters;

public class PersonaService implements AutoCloseable {
    private final Driver driver;

    public PersonaService(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    @Override
    public void close() {
        driver.close();
    }

    // Crear una persona
    public void crearPersona(Persona persona) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run("CREATE (p:Persona {id: $id, nombre: $nombre, correo: $correo, edad: $edad, ciudad: $ciudad})",
                    parameters(
                        "id", persona.getId(),
                        "nombre", persona.getNombre(),
                        "correo", persona.getCorreo(),
                        "edad", persona.getEdad(),
                        "ciudad", persona.getCiudad()
                    )
                );
                return null;
            });
        }
    }

    // Crear un comentario entre dos personas
    public void crearComentario(String idPersonaOrigen, String idPersonaDestino, String descripcion) {
        try (Session session = driver.session()) {
            session.executeWrite(tx -> {
                tx.run(
                    "MATCH (p1:Persona {id: $idOrigen}), (p2:Persona {id: $idDestino}) " +
                    "CREATE (p1)-[:COMENTO {descripcion: $descripcion}]->(p2)",
                    parameters(
                        "idOrigen", idPersonaOrigen,
                        "idDestino", idPersonaDestino,
                        "descripcion", descripcion
                    )
                );
                return null;
            });
        }
    }

    // Obtener todos los comentarios hechos por una persona
    public void obtenerComentariosDePersona(String idPersona) {
        try (Session session = driver.session()) {
            session.executeRead(tx -> {
                var result = tx.run(
                    "MATCH (p1:Persona {id: $id})-[c:COMENTO]->(p2:Persona) " +
                    "RETURN p1.nombre as origen, p2.nombre as destino, c.descripcion as comentario",
                    parameters("id", idPersona)
                );
                
                while (result.hasNext()) {
                    var record = result.next();
                    System.out.printf("%s comentó a %s: %s%n",
                        record.get("origen").asString(),
                        record.get("destino").asString(),
                        record.get("comentario").asString()
                    );
                }
                return null;
            });
        }
    }
} 