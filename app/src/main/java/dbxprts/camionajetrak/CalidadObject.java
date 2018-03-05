package dbxprts.camionajetrak;

/**
 * Created by Diego on 08/08/2017.
 */

public class CalidadObject {
    int _id_evento;
    String _placa;
    String _producto;
    String _cliente;
    String _etapa;
    String _hora_inicio;

    public CalidadObject(){

    }

    public CalidadObject(int id_evento, String placa, String producto, String cliente, String etapa, String hora_inicio){
        this._id_evento = id_evento;
        this._placa = placa;
        this._producto = producto;
        this._cliente = cliente;
        this._etapa = etapa;
        this._hora_inicio = hora_inicio;
    }

    public int get_id_evento() {
        return _id_evento;
    }

    public void set_id_evento(int _id_evento) {
        this._id_evento = _id_evento;
    }

    public String get_placa() {
        return _placa;
    }

    public void set_placa(String _placa) {
        this._placa = _placa;
    }

    public String get_producto() {
        return _producto;
    }

    public void set_producto(String _producto) {
        this._producto = _producto;
    }

    public String get_cliente() {
        return _cliente;
    }

    public void set_cliente(String _cliente) {
        this._cliente = _cliente;
    }

    public String get_etapa() {
        return _etapa;
    }

    public void set_etapa(String _etapa) {
        this._etapa = _etapa;
    }

    public String get_hora_inicio() {
        return _hora_inicio;
    }

    public void set_hora_inicio(String _hora_inicio) {
        this._hora_inicio = _hora_inicio;
    }
}
