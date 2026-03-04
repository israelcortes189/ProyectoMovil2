package com.example.marsphotos.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Header

//Retrofit
// Body para login
val bodyacceso = """
    <?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <accesoLogin xmlns="http://tempuri.org/">
          <strMatricula>%s</strMatricula>
          <strContrasenia>%s</strContrasenia>
          <tipoUsuario>ALUMNO</tipoUsuario>
        </accesoLogin>
      </soap:Body>
    </soap:Envelope>
""".trimIndent()

// Body para perfil
val bodyPerfil = """
    <?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <getAlumnoAcademicoWithLineamiento xmlns="http://tempuri.org/">
          <strMatricula>%s</strMatricula>
          <tipoUsuario>ALUMNO</tipoUsuario>
        </getAlumnoAcademicoWithLineamiento>
      </soap:Body>
    </soap:Envelope>
""".trimIndent()

val bodyCardex = """
    <?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <getAllKardexConPromedioByAlumno xmlns="http://tempuri.org/">
          <aluLineamiento>%s</aluLineamiento>
        </getAllKardexConPromedioByAlumno>
      </soap:Body>
    </soap:Envelope>
""".trimIndent()

val bodyCargaAcademica = """
    <?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <getCargaAcademicaByAlumno xmlns="http://tempuri.org/" />
      </soap:Body>
    </soap:Envelope>
""".trimIndent()

val bodyCalificacionesUnidades = """
    <?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <getCalifUnidadesByAlumno xmlns="http://tempuri.org/" />
      </soap:Body>
    </soap:Envelope>
""".trimIndent()

val bodyCalificacionFinal = """
    <?xml version="1.0" encoding="utf-8"?>
    <soap:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                   xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
      <soap:Body>
        <getAllCalifFinalByAlumnos xmlns="http://tempuri.org/">
          <bytModEducativo>%s</bytModEducativo>
        </getAllCalifFinalByAlumnos>
      </soap:Body>
    </soap:Envelope>
""".trimIndent()

interface SICENETWService {
    // Login
    @Headers(
        "Content-Type: text/xml; charset=utf-8",
        "SOAPAction: \"http://tempuri.org/accesoLogin\""
    )
    @POST("/ws/wsalumnos.asmx")
    suspend fun acceso(@Body soap: RequestBody): retrofit2.Response<ResponseBody>

    // Perfil académico
    @Headers(
        "Content-Type: text/xml; charset=utf-8",
        "SOAPAction: \"http://tempuri.org/getAlumnoAcademicoWithLineamiento\""
    )
    @POST("/ws/wsalumnos.asmx")
    suspend fun perfil(@Body soap: RequestBody): retrofit2.Response<ResponseBody>

    // Cardex académico
    @Headers(
        "Content-Type: text/xml; charset=utf-8",
        "SOAPAction: \"http://tempuri.org/getAllKardexConPromedioByAlumno\""
    )
    @POST("/ws/wsalumnos.asmx")
    suspend fun cardex(@Body soap: RequestBody): Response<ResponseBody>

    // Carga académica
    @Headers(
        "Content-Type: text/xml; charset=utf-8",
        "SOAPAction: \"http://tempuri.org/getCargaAcademicaByAlumno\""
    )
    @POST("/ws/wsalumnos.asmx")
    suspend fun cargaAcademica(@Body soap: RequestBody): Response<ResponseBody>

    // Calificaciones por unidad
    @Headers(
        "Content-Type: text/xml; charset=utf-8",
        "SOAPAction: \"http://tempuri.org/getCalifUnidadesByAlumno\""
    )
    @POST("/ws/wsalumnos.asmx")
    suspend fun calificacionesUnidades(@Body soap: RequestBody): Response<ResponseBody>

    // Calificación final
    @Headers(
        "Content-Type: text/xml; charset=utf-8",
        "SOAPAction: \"http://tempuri.org/getAllCalifFinalByAlumnos\""
    )
    @POST("/ws/wsalumnos.asmx")
    suspend fun calificacionFinal(@Body soap: RequestBody): Response<ResponseBody>
}








