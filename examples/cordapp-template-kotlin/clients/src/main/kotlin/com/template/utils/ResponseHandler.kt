package com.template.utils

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.util.*


object ResponseHandler {
    fun generateResponse(status:HttpStatus,data: Any,error:Any): ResponseEntity<Any> {
        val map: MutableMap<String, Any> = HashMap()


        map["data"] = data
        map["error"] = error

        return ResponseEntity(map, status)
    }
}