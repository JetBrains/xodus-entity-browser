package com.lehvolk.xodus.web


import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

object XodusStore {

    val LOCATION_KEY = "xodus.store.location";
    val STORE_ACCESS_KEY = "xodus.store.key";
    val FILE_KEY = "xodus.store.file.config";

    fun from(pathToFile: String?): XodusStoreRequisites? {
        if (pathToFile == null) {
            return null
        }
        try {
            return from(FileInputStream(pathToFile))
        } catch (e: IOException) {
            //ignore parsing or loading file exceptions
        }
        return null
    }

    fun from(inputStream: InputStream): XodusStoreRequisites? {
        try {
            val properties = Properties()
            properties.load(inputStream)
            val location = properties.getProperty(LOCATION_KEY);
            val key = properties.getProperty(STORE_ACCESS_KEY);
            if (location != null && key != null) {
                return XodusStoreRequisites(location, key)
            }
        } catch (e: IOException) {
            //ignore parsing exceptions
        } finally {
            try {
                inputStream.close()
            } catch(e: Exception) {
                // ignore
            }
        }
        return null
    }

    fun fromSystem(): XodusStoreRequisites? {
        val location = System.getProperty(LOCATION_KEY);
        val key = System.getProperty(STORE_ACCESS_KEY);
        if (location != null && key != null) {
            return XodusStoreRequisites(location, key)
        }
        return null
    }

    fun requisites(): XodusStoreRequisites {
        val file = System.getProperty(FILE_KEY);
        val defaultConfig = XodusStore.javaClass.getResourceAsStream("/xodus-store.properties");
        val result = (fromSystem() ?: from(file)) ?: from(defaultConfig)
        return result!!
    }
}


class XodusStoreRequisites(val location: String, val key: String)