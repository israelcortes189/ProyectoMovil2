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
package com.example.marsphotos.data


import android.content.Context
import android.preference.PreferenceManager
import android.util.Base64
import com.example.marsphotos.network.bodyPerfil
import com.example.marsphotos.network.bodyacceso
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import android.util.Log
import com.example.marsphotos.model.ProfileStudent
import com.example.marsphotos.model.Usuario
import com.example.marsphotos.network.SICENETWService

/**
 * Repository interface para SICENET
 */
interface SNRepository {
    suspend fun acceso(m: String, p: String): String
    suspend fun accesoObjeto(m: String, p: String): Usuario
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
    private var currentMatricula: String? = null

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

    override suspend fun accesoObjeto(m: String, p: String): Usuario {
        return Usuario(matricula = m)
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
                semestre = jsonObj.optString("semActual", ""),
                creditos = jsonObj.optString("cdtosAcumulados", "")
            )
        } catch (e: Exception) {
            Log.e("SOAP_PROFILE", "Error parseando JSON: ${e.message}")
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
}



