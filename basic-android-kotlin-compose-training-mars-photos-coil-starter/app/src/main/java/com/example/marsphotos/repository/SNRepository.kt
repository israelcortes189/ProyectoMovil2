/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.marsphotos.repository


import android.content.Context
import android.preference.PreferenceManager
import com.example.marsphotos.network.bodyPerfil
import com.example.marsphotos.network.bodyacceso
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import android.util.Log
import com.example.marsphotos.model.entityes.CalificacionFinalItem
import com.example.marsphotos.model.entityes.CalificacionUnidadItem
import com.example.marsphotos.model.entityes.CardexItem
import com.example.marsphotos.model.entityes.CargaItem
import com.example.marsphotos.model.entityes.ProfileStudent
import com.example.marsphotos.model.entityes.PromedioInfo
import com.example.marsphotos.network.SICENETWService
import com.example.marsphotos.network.bodyCalificacionFinal
import com.example.marsphotos.network.bodyCalificacionesUnidades
import com.example.marsphotos.network.bodyCardex
import com.example.marsphotos.network.bodyCargaAcademica

/**
 * Repository interface para SICENET
 */
interface SNRepository {
    suspend fun acceso(m: String, p: String): String
    suspend fun profile(): ProfileStudent?
    fun hasSession(): Boolean
    fun logout()
}

class NetworSNRepository(
    private val snApiService: SICENETWService,
    private val context: Context
) : SNRepository {

    //se guarda la matricula en esta variable
    //cuando se elimina la app del adminitrador de tareas esta
    // tambien se elimina
    var currentMatricula: String? = null

    /*
    este metodo revisa si hay cookies guardadas en SharedPreferences.
    Si encuentra alguna y si hay tambien alguna currentMatricula,
    considera que hay sesión activa; si no, devuelve false,
     */
    override fun hasSession(): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val cookies = prefs.getStringSet(AddCookiesInterceptor.PREF_COOKIES, null)
        Log.d("SESSION_CHECK", "Cookies encontradas: $cookies")
        return !cookies.isNullOrEmpty() && currentMatricula != null
    }

    override suspend fun acceso(m: String, p: String): String {
        val soapFinal = bodyacceso.format(m.uppercase(), p)
        val body = soapFinal.toRequestBody("text/xml; charset=utf-8".toMediaType())
        val response = snApiService.acceso(body)

        val xml = response.body()?.string() ?: return "ERROR"
        Log.d("SOAP_XML_ACCESO", xml)

        val resultRegex = "<accesoLoginResult>(.*?)</accesoLoginResult>".toRegex()
        val result = resultRegex.find(xml)?.groupValues?.get(1)


        return if (result != null && result.contains("true", ignoreCase = true)) {
            currentMatricula = m.uppercase()
            Log.d("LOGIN", "Resultado del repo: 'OK'")
            "OK"
        } else {
            currentMatricula = null
            Log.d("LOGIN", "Resultado del repo: 'ERROR'")
            "ERROR"
        }
    }


    override suspend fun profile(): ProfileStudent? {
        if (!hasSession()) return null

        val bodyPerfilFormatted = bodyPerfil.format(currentMatricula ?: "")
        val body = bodyPerfilFormatted.toRequestBody("text/xml; charset=utf-8".toMediaType())
        val response = snApiService.perfil(body)

        val xml = response.body()?.string()
        Log.d("SOAP_PROFILE", xml ?: "sin respuesta")

        if (xml == null) return null

        val resultRegex =
            "<getAlumnoAcademicoWithLineamientoResult>(.*?)</getAlumnoAcademicoWithLineamientoResult>"
                .toRegex(RegexOption.DOT_MATCHES_ALL)
        val result = resultRegex.find(xml)?.groupValues?.get(1)

        Log.d("SOAP_PROFILE_RESULT", result ?: "sin resultado")

        if (result.isNullOrBlank()) {
            Log.e("SOAP_PROFILE", "No se encontró el bloque de resultado")
            return null
        }

        return try {
            val jsonObj = org.json.JSONObject(result)
            ProfileStudent(
                matricula = jsonObj.optString("matricula", currentMatricula ?: ""),
                nombre = jsonObj.optString("nombre", "Alumno"),
                carrera = jsonObj.optString("carrera", ""),
                semActual = jsonObj.optInt("semActual", 0),
                cdtosAcumulados = jsonObj.optInt("cdtosAcumulados", 0)
            )
        } catch (e: Exception) {
            Log.e("SOAP_PROFILE", "Error parseando JSON: ${e.message}, contenido: $result", e)
            null
        }
    }

    //funcion para cerrar secion en el repository
    //se borra la matrícula en memoria y se
    // eliminan las cookies existentes en SharedPreferences
    override fun logout() {
        currentMatricula = null
        val prefs = PreferenceManager.getDefaultSharedPreferences(context).edit()
        prefs.remove(AddCookiesInterceptor.PREF_COOKIES).apply()
        Log.d("LOGOUT", "Sesión cerrada y cookies eliminadas")
    }

    suspend fun cardex(lineamiento: Int): Pair<List<CardexItem>, PromedioInfo>? {
        if (!hasSession()) return null

        val soapFinal = bodyCardex.format(lineamiento)
        val body = soapFinal.toRequestBody("text/xml; charset=utf-8".toMediaType())
        val response = snApiService.cardex(body)

        val xml = response.body()?.string()
        Log.d("SOAP_CARDEX", xml ?: "sin respuesta")

        if (xml == null) return null

        val resultRegex =
            "<getAllKardexConPromedioByAlumnoResult>(.*?)</getAllKardexConPromedioByAlumnoResult>"
                .toRegex(RegexOption.DOT_MATCHES_ALL)
        val result = resultRegex.find(xml)?.groupValues?.get(1)

        Log.d("SOAP_CARDEX_RESULT", result ?: "sin resultado")

        if (result.isNullOrBlank()) return null

        return try {
            val jsonObj = org.json.JSONObject(result)

            // 1) Parsear el array de materias
            val kardexArray = jsonObj.getJSONArray("lstKardex")
            val list = mutableListOf<CardexItem>()
            for (i in 0 until kardexArray.length()) {
                val obj = kardexArray.getJSONObject(i)
                list.add(
                    CardexItem(
                        claveMateria = obj.optString("ClvMat"),
                        claveOficial = obj.optString("ClvOfiMat"),
                        materia = obj.optString("Materia"),
                        creditos = obj.optInt("Cdts"),
                        calificacion = obj.optInt("Calif"),
                        acreditacion = obj.optString("Acred"),
                        semestre = obj.optString("S1"),
                        periodo = obj.optString("P1"),
                        anio = obj.optString("A1")
                    )
                )
            }

            // 2) Parsear el objeto Promedio
            val promedioObj = jsonObj.getJSONObject("Promedio")
            val promedioInfo = PromedioInfo(
                promedioGral = promedioObj.optDouble("PromedioGral"),
                creditosAcumulados = promedioObj.optInt("CdtsAcum"),
                creditosPlan = promedioObj.optInt("CdtsPlan"),
                materiasCursadas = promedioObj.optInt("MatCursadas"),
                materiasAprobadas = promedioObj.optInt("MatAprobadas"),
                avanceCreditos = promedioObj.optDouble("AvanceCdts")
            )

            Pair(list, promedioInfo)
        } catch (e: Exception) {
            Log.e("SOAP_CARDEX", "Error parseando JSON: ${e.message}, contenido: $result", e)
            null
        }
    }

    suspend fun cargaAcademica(): List<CargaItem>? {
        if (!hasSession()) return null

        val body = bodyCargaAcademica.toRequestBody("text/xml; charset=utf-8".toMediaType())
        val response = snApiService.cargaAcademica(body)

        val xml = response.body()?.string()
        Log.d("SOAP_CARGA", xml ?: "sin respuesta")

        if (xml == null) return null

        val resultRegex =
            "<getCargaAcademicaByAlumnoResult>(.*?)</getCargaAcademicaByAlumnoResult>"
                .toRegex(RegexOption.DOT_MATCHES_ALL)
        val result = resultRegex.find(xml)?.groupValues?.get(1)

        Log.d("SOAP_CARGA_RESULT", result ?: "sin resultado")

        if (result.isNullOrBlank()) return null

        return try {
            val jsonArray = org.json.JSONArray(result)
            val list = mutableListOf<CargaItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    CargaItem(
                        clvOficial = obj.optString("clvOficial"),
                        materia = obj.optString("Materia"),
                        grupo = obj.optString("Grupo"),
                        docente = obj.optString("Docente"),
                        creditos = obj.optInt("CreditosMateria"),
                        estadoMateria = obj.optString("EstadoMateria"),
                        observaciones = obj.optString("Observaciones"),
                        semipresencial = obj.optString("Semipresencial"),
                        lunes = obj.optString("Lunes"),
                        martes = obj.optString("Martes"),
                        miercoles = obj.optString("Miercoles"),
                        jueves = obj.optString("Jueves"),
                        viernes = obj.optString("Viernes"),
                        sabado = obj.optString("Sabado")
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e("SOAP_CARGA", "Error parseando JSON: ${e.message}, contenido: $result", e)
            null
        }
    }

    suspend fun calificacionesPorUnidad(): List<CalificacionUnidadItem>? {
        if (!hasSession()) return null

        val body = bodyCalificacionesUnidades.toRequestBody("text/xml; charset=utf-8".toMediaType())
        val response = snApiService.calificacionesUnidades(body)

        val xml = response.body()?.string()
        Log.d("SOAP_CALIF_UNIDADES", xml ?: "sin respuesta")

        if (xml == null) return null

        val resultRegex =
            "<getCalifUnidadesByAlumnoResult>(.*?)</getCalifUnidadesByAlumnoResult>"
                .toRegex(RegexOption.DOT_MATCHES_ALL)
        val result = resultRegex.find(xml)?.groupValues?.get(1)

        Log.d("SOAP_CALIF_UNIDADES_RESULT", result ?: "sin resultado")

        if (result.isNullOrBlank()) return null


        return try {
            val jsonArray = org.json.JSONArray(result)
            val list = mutableListOf<CalificacionUnidadItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    CalificacionUnidadItem(
                        materia = obj.optString("Materia"),
                        grupo = obj.optString("Grupo"),
                        observaciones = obj.optString("Observaciones"),
                        unidadesActivas = obj.optString("UnidadesActivas"),
                        c1 = obj.optString("C1"),
                        c2 = obj.optString("C2"),
                        c3 = obj.optString("C3"),
                        c4 = obj.optString("C4"),
                        c5 = obj.optString("C5"),
                        c6 = obj.optString("C6"),
                        c7 = obj.optString("C7"),
                        c8 = obj.optString("C8"),
                        c9 = obj.optString("C9"),
                        c10 = obj.optString("C10"),
                        c11 = obj.optString("C11"),
                        c12 = obj.optString("C12"),
                        c13 = obj.optString("C13")
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e("SOAP_CALIF_UNIDADES", "Error parseando JSON: ${e.message}, contenido: $result", e)
            null
        }
    }

    suspend fun calificacionFinal(modEducativo: Int): List<CalificacionFinalItem>? {
        if (!hasSession()) return null

        val body = bodyCalificacionFinal.format(modEducativo)
            .toRequestBody("text/xml; charset=utf-8".toMediaType())
        val response = snApiService.calificacionFinal(body)

        val xml = response.body()?.string()
        Log.d("SOAP_CALIF_FINAL", xml ?: "sin respuesta")

        if (xml == null) return null

        val resultRegex =
            "<getAllCalifFinalByAlumnosResult>(.*?)</getAllCalifFinalByAlumnosResult>"
                .toRegex(RegexOption.DOT_MATCHES_ALL)
        val result = resultRegex.find(xml)?.groupValues?.get(1)

        Log.d("SOAP_CALIF_FINAL_RESULT", result ?: "sin resultado")

        if (result.isNullOrBlank()) return null

        return try {
            val jsonArray = org.json.JSONArray(result)
            val list = mutableListOf<CalificacionFinalItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    CalificacionFinalItem(
                        calif = obj.optInt("calif"),
                        acreditacion = obj.optString("acred"),
                        grupo = obj.optString("grupo"),
                        materia = obj.optString("materia"),
                        observaciones = obj.optString("Observaciones")
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e("SOAP_CALIF_FINAL", "Error parseando JSON: ${e.message}, contenido: $result", e)
            null
        }
    }
}



