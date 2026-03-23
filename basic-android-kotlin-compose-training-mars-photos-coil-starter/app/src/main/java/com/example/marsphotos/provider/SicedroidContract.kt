package com.example.marsphotos.provider
import android.content.UriRelativeFilter.PATH
import android.net.Uri

object SicedroidContract {
    const val AUTHORITY = "com.example.marsphotos.provider"
    val BASE_CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY")

    object Carga {
        const val PATH = "carga"
        val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(PATH).build()
        const val TABLE = "carga_academica"
        const val MIME_DIR = "vnd.android.cursor.dir/vnd.com.example.marsphotos"
        const val MIME_ITEM = "vnd.android.cursor.item/vnd.com.example.marsphotos"

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

    object Cardex {
        const val PATH = "cardex"
        val CONTENT_URI: Uri = BASE_CONTENT_URI.buildUpon().appendPath(PATH).build()
        const val TABLE = "cardex"
        const val MIME_DIR = "vnd.android.cursor.dir/vnd.com.example.marsphotos"
        const val MIME_ITEM = "vnd.android.cursor.item/vnd.com.example.marsphotos"

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
