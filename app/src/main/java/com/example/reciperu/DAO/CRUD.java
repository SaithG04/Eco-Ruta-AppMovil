package com.example.reciperu.DAO;

public interface CRUD <T> {
    T listar();
    boolean insertar();
    boolean actualizar();
    boolean eliminar();
}
