package dbxprts.camionajetrak;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Diego on 07/08/2017.
 */

public class LocalDatabaseConnection extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "OnProgress";

    // Calidad table name
    private static final String TABLE_CALIDAD = "calidad_on_progress";

    // Calidad Table Columns names
    private static final String CALIDAD_KEY_ID_EVENTO = "id_evento";
    private static final String CALIDAD_KEY_PLACA = "placa";
    private static final String CALIDAD_KEY_PRODUCTO = "producto";
    private static final String CALIDAD_KEY_CLIENTE = "cliente";
    private static final String CALIDAD_KEY_ETAPA = "etapa";
    private static final String CALIDAD_KEY_HORA_INICIO = "hora_inicio";


    public LocalDatabaseConnection(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void insertIntoDatabase(int id_evento, String placa, String producto, String cliente, String etapa, String hora_inicio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(CALIDAD_KEY_ID_EVENTO, id_evento);
        values.put(CALIDAD_KEY_PLACA, placa);
        values.put(CALIDAD_KEY_PRODUCTO, producto);
        values.put(CALIDAD_KEY_CLIENTE, cliente);
        values.put(CALIDAD_KEY_ETAPA, etapa);
        values.put(CALIDAD_KEY_HORA_INICIO, hora_inicio);

        // Inserting Row
        db.insert(TABLE_CALIDAD, null, values);
        db.close(); // Closing database connection
    }

    public CalidadObject getCalidadObject(int id_evento) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CALIDAD, new String[] { CALIDAD_KEY_ID_EVENTO,
                        CALIDAD_KEY_PLACA, CALIDAD_KEY_PRODUCTO, CALIDAD_KEY_CLIENTE, CALIDAD_KEY_ETAPA, CALIDAD_KEY_HORA_INICIO}, CALIDAD_KEY_ID_EVENTO + "=?",
                new String[] { String.valueOf(id_evento) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        CalidadObject calidad = new CalidadObject(Integer.parseInt(cursor.getString(0)),cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5));
        // return contact
        return calidad;
    }

    public int getCalidadCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CALIDAD;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        //cursor.close();

        // return count
        return cursor.getCount();
    }

    public void updateEtapaCalidad(int id_evento, String etapa) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CALIDAD_KEY_ETAPA, etapa);

        // updating row
        db.update(TABLE_CALIDAD, values, CALIDAD_KEY_ID_EVENTO + " = ?",
                new String[] { String.valueOf(id_evento) });
    }

    public void deleteCalidad(int id_evento) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CALIDAD, CALIDAD_KEY_ID_EVENTO + " = ?",
                new String[] { String.valueOf(id_evento) });
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // SQL statement for creating a new table
        String sql = "CREATE TABLE " + TABLE_CALIDAD + "("
                + CALIDAD_KEY_ID_EVENTO + " INTEGER PRIMARY KEY,"
                + CALIDAD_KEY_PLACA + " TEXT,"
                + CALIDAD_KEY_PRODUCTO + " TEXT,"
                + CALIDAD_KEY_CLIENTE + " TEXT,"
                + CALIDAD_KEY_ETAPA + " TEXT,"
                + CALIDAD_KEY_HORA_INICIO + " TEXT" + ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALIDAD);

        // Create tables again
        onCreate(db);
    }
}
