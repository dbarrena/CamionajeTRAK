package dbxprts.camionajetrak;

import android.app.Application;

/**
 * Created by Diego on 26/05/2017.
 */

public class GlobalVariables extends Application {
    private String nombre;
    private String apellido;
    private String user;

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getActiveUser(){
        return user;
    }

    public void setNombreCompleto(String nombre, String apellido) {
        this.nombre = nombre;
        this.apellido = apellido;
    }

    public void setActiveUser(String user){
        this.user = user;
    }
}
