package com.lehvolk.xodus.web

import retrofit2.Call
import retrofit2.http.*

interface DBsApi {

    @GET("/api/dbs")
    fun all(): Call<List<DBSummary>>

    @POST("/api/dbs")
    fun new(@Body dbSummary: DBSummary): Call<DBSummary>
}

interface DBApi {

    @GET("/api/dbs/{uuid}/types")
    fun allDbTypes(@Path("uuid") uuid: String): Call<List<String>>

    @POST("/api/dbs/{uuid}/types")
    fun addDbType(@Path("uuid") uuid: String, @Body type: EntityType): Call<List<EntityType>>

    @DELETE("/api/dbs/{uuid}")
    fun delete(@Path("uuid") uuid: String): Call<Void>

    @POST("/api/dbs/{uuid}")
    fun startOrStop(@Path("uuid") uuid: String, @Query("op") operation: String): Call<DBSummary>
}