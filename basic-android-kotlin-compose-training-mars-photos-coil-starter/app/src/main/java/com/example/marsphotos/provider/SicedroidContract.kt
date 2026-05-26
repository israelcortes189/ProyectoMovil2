package com.example.marsphotos.provider
import android.content.UriRelativeFilter.PATH
import android.net.Uri

object SicedroidContract {
    // Autoridad única que identifica al ContentProvider en el sistema Android.
    // Debe coincidir con la autoridad declarada en el AndroidManifest del provider.
    const val AUTHORITY = "com.example.marsphotos.provider"

    // URI base que se usa para construir URIs completas hacia las distintas rutas (paths).
    val BASE_CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY")

    // Contrato para la tabla / ruta "carga" (carga académica)
    object Carga {
        // Path relativo que se añade a la BASE_CONTENT_URI para formar la URI completa.
        const val PATH = "carga"

        // URI completa para acceder a la colección "carga" del ContentProvider.
        val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(PATH).build()

        // Nombre de la tabla en la base de datos (si se usa SQLite internamente).
        const val TABLE = "carga_academica"

        // MIME type para una colección (varias filas) devuelta por el provider.
        const val MIME_DIR = "vnd.android.cursor.dir/vnd.com.example.marsphotos"

        // MIME type para un solo ítem (una fila) devuelto por el provider.
        const val MIME_ITEM = "vnd.android.cursor.item/vnd.com.example.marsphotos"

        // Constantes con los nombres de las columnas de la tabla "carga_academica"
        const val COL_MATRICULA = "matricula"
        const val COL_CLAVE_OFICIAL = "claveOficial"
        const val COL_MATERIA = "materia"
        const val COL_GRUPO = "grupo"
        const val COL_DOCENTE = "docente"
        const val COL_CREDITOS = "creditos"
        const val COL_ESTADO = "estadoMateria"
        const val COL_OBSERVACIONES = "observaciones"
        const val COL_SEMIPRESENCIAL = "semipresencial"
        const val COL_LUNES = "lunes"
        const val COL_MARTES = "martes"
        const val COL_MIERCOLES = "miercoles"
        const val COL_JUEVES = "jueves"
        const val COL_VIERNES = "viernes"
        const val COL_SABADO = "sabado"
    }

    // Contrato para la tabla / ruta "cardex" (historial académico)
    object Cardex {
        // Path relativo para cardex
        const val PATH = "cardex"

        // URI completa para acceder a la colección "cardex".
        val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(PATH).build()

        // Nombre de la tabla en la base de datos para cardex.
        const val TABLE = "cardex"

        // MIME types para colecciones y items (igual formato que en Carga).
        const val MIME_DIR = "vnd.android.cursor.dir/vnd.com.example.marsphotos"
        const val MIME_ITEM = "vnd.android.cursor.item/vnd.com.example.marsphotos"

        // Nombres de columnas de la tabla "cardex".
        // Útiles para consultas, inserciones y lectura del Cursor.
        const val COL_MATRICULA = "matricula"
        const val COL_CLAVE_MATERIA = "claveMateria"
        const val COL_CLAVE_OFICIAL = "claveOficial"
        const val COL_MATERIA = "materia"
        const val COL_CREDITOS = "creditos"
        const val COL_CALIFICACION = "calificacion"
        const val COL_ACREDITACION = "acreditacion"
        const val COL_SEMESTRE = "semestre"
        const val COL_PERIODO = "periodo"
        const val COL_ANIO = "anio"
    }
}

