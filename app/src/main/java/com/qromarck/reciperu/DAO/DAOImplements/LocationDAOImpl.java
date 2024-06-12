package com.qromarck.reciperu.DAO.DAOImplements;

import static com.qromarck.reciperu.Utilities.InterfacesUtilities.entityToMap;

import android.util.Log;

import com.qromarck.reciperu.DAO.LocationDAO;
import com.qromarck.reciperu.Entity.Location;
import com.qromarck.reciperu.Utilities.DataAccessUtilities;

import java.util.Map;
import java.util.Objects;

public class LocationDAOImpl extends DataAccessUtilities implements LocationDAO {

    private Location location;
    private final static String COLLECTION_NAME = "user_locations";
    public LocationDAOImpl(Location location) {
        this.location = location;
    }
    @Override
    public void setEntity(Location location) {
        this.location = location;
    }

    @Override
    public void listarFromFireStore(DataAccessUtilities.OnDataRetrievedListener<Location> listener) {

    }

    @Override
    public void insertOnFireStore(OnInsertionListener listener) {
        Map<String, Object> entityToMap = entityToMap(location);
        String documentId = Objects.requireNonNull(entityToMap.get("userId")).toString();
        insertOnFireStoreRealtime(COLLECTION_NAME, documentId, entityToMap,
                new DataAccessUtilities.OnInsertionListener() {
                    @Override
                    public void onInsertionSuccess() {
                        listener.onInsertionSuccess();
                    }

                    @Override
                    public void onInsertionError(String errorMessage) {
                        listener.onInsertionError(errorMessage);
                    }
                });
    }

    @Override
    public void updateOnFireStore(OnUpdateListener listener) {
        //DONT IMPLEMENT
    }

    @Override
    public void deleteFromFireStore() {

    }
}
