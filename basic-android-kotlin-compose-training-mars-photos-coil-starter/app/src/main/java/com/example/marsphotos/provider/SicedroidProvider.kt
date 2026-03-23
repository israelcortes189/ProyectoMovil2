package com.example.marsphotos.provider

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.example.marsphotos.data.datbase.AppDatabase

class SicedroidProvider : ContentProvider() {

    companion object {
        private const val TAG = "SicedroidProvider"
        private const val CODE_CARGA = 100
        private const val CODE_CARGA_ID = 101
        private const val CODE_CARDEX = 200
        private const val CODE_CARDEX_ID = 201
    }

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(SicedroidContract.AUTHORITY, SicedroidContract.Carga.PATH, CODE_CARGA)
        addURI(SicedroidContract.AUTHORITY, "${SicedroidContract.Carga.PATH}/*", CODE_CARGA_ID)
        addURI(SicedroidContract.AUTHORITY, SicedroidContract.Cardex.PATH, CODE_CARDEX)
        addURI(SicedroidContract.AUTHORITY, "${SicedroidContract.Cardex.PATH}/*", CODE_CARDEX_ID)
    }

    private lateinit var db: AppDatabase

    override fun onCreate(): Boolean {
        context?.let {
            db = AppDatabase.getDatabase(it)
            Log.i(TAG, "Provider created")
            return true
        }
        Log.e(TAG, "Context null in onCreate")
        return false
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val cursor = when (uriMatcher.match(uri)) {
            CODE_CARGA -> db.cargaDao().getCargaCursor(null)
            CODE_CARDEX -> db.cardexDao().getCardexCursor(null)
            CODE_CARGA_ID, CODE_CARDEX_ID -> throw IllegalArgumentException("Query by id not implemented")
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        cursor.setNotificationUri(context?.contentResolver, uri)
        return cursor
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            CODE_CARGA -> SicedroidContract.Carga.MIME_DIR
            CODE_CARGA_ID -> SicedroidContract.Carga.MIME_ITEM
            CODE_CARDEX -> SicedroidContract.Cardex.MIME_DIR
            CODE_CARDEX_ID -> SicedroidContract.Cardex.MIME_ITEM
            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        values ?: throw IllegalArgumentException("ContentValues required")
        val resultUri = when (uriMatcher.match(uri)) {
            CODE_CARGA -> {
                val rowId = db.cargaDao().insertFromContentValues(values)
                ContentUris.withAppendedId(SicedroidContract.Carga.CONTENT_URI, rowId)
            }
            CODE_CARDEX -> {
                val rowId = db.cardexDao().insertFromContentValues(values)
                ContentUris.withAppendedId(SicedroidContract.Cardex.CONTENT_URI, rowId)
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context?.contentResolver?.notifyChange(uri, null)
        return resultUri
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Update not implemented")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        throw UnsupportedOperationException("Delete not implemented")
    }
}
