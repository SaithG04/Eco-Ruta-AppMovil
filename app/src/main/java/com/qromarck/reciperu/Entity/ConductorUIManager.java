package com.qromarck.reciperu.Entity;

import com.qromarck.reciperu.Interfaces.ConductorUI;

public class ConductorUIManager {
    private static ConductorUIManager instance;
    private ConductorUI conductorUI;

    private ConductorUIManager() {
        // Constructor privado para evitar instanciación externa
    }

    public static synchronized ConductorUIManager getInstance() {
        if (instance == null) {
            instance = new ConductorUIManager();
        }
        return instance;
    }

    public void setConductorUI(ConductorUI conductorUI) {
        this.conductorUI = conductorUI;
    }

    public ConductorUI getConductorUI() {
        return conductorUI;
    }
}
