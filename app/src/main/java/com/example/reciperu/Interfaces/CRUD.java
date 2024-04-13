package com.example.reciperu.Interfaces;

public interface CRUD <T> {
    T listar();
    boolean insertar();
    boolean actualizar();
    boolean eliminar();
}
