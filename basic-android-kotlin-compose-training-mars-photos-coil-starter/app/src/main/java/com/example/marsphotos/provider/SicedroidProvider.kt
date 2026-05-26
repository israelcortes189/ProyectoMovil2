package com.example.marsphotos.provider

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.example.marsphotos.data.datbase.AppDatabase


// Provider que expone acceso de solo lectura/escritura a las tablas 'carga' y 'cardex'
class SicedroidProvider : ContentProvider() {

    // Instancia de la base de datos (Room). Se inicializa en onCreate().
    private lateinit var db: AppDatabase

    // UriMatcher para distinguir URIs: 1 -> carga, 2 -> cardex
    private val matcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(SicedroidContract.AUTHORITY, "carga", 1)
        addURI(SicedroidContract.AUTHORITY, "cardex", 2)
    }

    /**
     * Se llama cuando el ContentProvider se crea.
     * Aquí inicializamos la base de datos (Room).
     * Devuelve true si la inicialización fue correcta.
     */
    override fun onCreate(): Boolean {
        db = AppDatabase.getDatabase(context!!)
        return true
    }

    /**
     * query: devuelve un Cursor con los resultados de la consulta.
     * - uri: la URI solicitada por el cliente
     * - p1..p4: parámetros estándar de query (projection, selection, selectionArgs, sortOrder)
     *
     * NOTA: aquí usamos métodos DAO que devuelven Cursor (getCargaCursor / getCardexCursor).
     * Es importante llamar a setNotificationUri para que los clientes puedan observar cambios.
     */
    override fun query(
        uri: Uri,
        p1: Array<String>?,
        p2: String?,
        p3: Array<String>?,
        p4: String?
    ): Cursor? {
        val cursor = when (matcher.match(uri)) {
            1 -> db.cargaDao().getCargaCursor(null)   // Cursor para la tabla 'carga'
            2 -> db.cardexDao().getCardexCursor(null) // Cursor para la tabla 'cardex'
            else -> null
        }
        // Registrar la URI en el cursor para notificaciones de cambios
        cursor?.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    /**
     * insert: inserta un registro a partir de ContentValues.
     * - v: ContentValues con los campos a insertar
     * Devuelve la URI del nuevo elemento (ContentUris.withAppendedId) o null si falla.
     *
     * Observaciones:
     * - Se asume que los DAOs tienen un método insertFromContentValues(ContentValues): Long
     *   que devuelve el id insertado o -1 si falla.
     * - Tras insertar notificamos a ContentResolver para que observers (Loaders, CursorAdapters)
     *   reciban la actualización.
     */
    override fun insert(uri: Uri, v: ContentValues?): Uri? {
        // 1. Obtener el ID tras insertar en la DB (v!! asume que envías datos válidos)
        val id = when (matcher.match(uri)) {
            1 -> db.cargaDao().insertFromContentValues(v!!)
            2 -> db.cardexDao().insertFromContentValues(v!!)
            else -> -1L
        }

        // 2. Si la inserción falló, devolver null
        if (id == -1L) return null

        // 3. Notificar que los datos cambiaron para que los clientes se actualicen
        context?.contentResolver?.notifyChange(uri, null)

        // 4. Devolver la URI del nuevo elemento (ej: content://.../carga/123)
        return ContentUris.withAppendedId(uri, id)
    }

    /**
     * getType: devuelve el MIME type para la URI.
     * Puede devolver null si no lo necesitas, pero es buena práctica devolver
     * algo como "vnd.android.cursor.dir/vnd.com.example.carga" para colecciones
     * y "vnd.android.cursor.item/vnd.com.example.carga" para items individuales.
     */
    override fun getType(uri: Uri): String? = null

    /**
     * update y delete: no implementados en este provider de prueba.
     * Devuelven 0 filas afectadas. Implementa si necesitas soporte para actualizar/borrar.
     */
    override fun update(u: Uri, v: ContentValues?, s: String?, sa: Array<String>?) = 0
    override fun delete(u: Uri, s: String?, sa: Array<String>?) = 0
}