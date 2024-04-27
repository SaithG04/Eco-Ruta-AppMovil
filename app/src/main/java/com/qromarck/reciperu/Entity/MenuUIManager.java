package com.qromarck.reciperu.Entity;

import com.qromarck.reciperu.Interfaces.MenuUI;

public class MenuUIManager {
    private static MenuUIManager instance;
    private MenuUI menuUI;

    private MenuUIManager() {
        // Constructor privado para evitar instanciación externa
    }

    public static synchronized MenuUIManager getInstance() {
        if (instance == null) {
            instance = new MenuUIManager();
        }
        return instance;
    }

    public void setMenuUI(MenuUI menuUI) {
        this.menuUI = menuUI;
    }

    public MenuUI getMenuUI() {
        return menuUI;
    }
}
