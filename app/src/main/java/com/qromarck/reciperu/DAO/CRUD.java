package com.qromarck.reciperu.DAO;

import com.qromarck.reciperu.Utilities.DataAccessUtilities;

import java.util.Map;

public interface CRUD<T> {

    void setEntity(T entity);
    void listarFromFireStore(DataAccessUtilities.OnDataRetrievedListener<T> listener);

    void insertarOnFireStore(Map<String, Object> userData);

    boolean actualizar();

    boolean eliminar();
}
