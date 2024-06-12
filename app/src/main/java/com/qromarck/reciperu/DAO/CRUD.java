package com.qromarck.reciperu.DAO;

import com.qromarck.reciperu.Utilities.DataAccessUtilities;

import java.util.Map;

public interface CRUD<T> {

    void setEntity(T entity);
    void listarFromFireStore(DataAccessUtilities.OnDataRetrievedListener<T> listener);

    void insertOnFireStore(DataAccessUtilities.OnInsertionListener listener);

    void updateOnFireStore(DataAccessUtilities.OnUpdateListener listener);

    void deleteFromFireStore();
}
