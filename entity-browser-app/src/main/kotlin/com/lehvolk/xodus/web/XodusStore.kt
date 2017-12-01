package com.lehvolk.xodus.web

class XodusStoreRequisites(val location: String, val key: String)

fun DBSummary.asRequisites(): XodusStoreRequisites = XodusStoreRequisites(location!!, key!!)