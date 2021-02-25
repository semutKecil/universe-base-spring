package com.kawanansemut.universebasespring.lib

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.io.Serializable

object Helper {
    fun objectToMap(obj: Serializable): Map<String, Any> {
        return obj.javaClass.declaredFields
            .filter { f ->
                !f.annotations.any { a -> a.toString().contains("JsonIgnore") || a.toString().contains("Transient") }
            }.map {
                it.isAccessible = true
                Pair(it.name, it.get(obj))
            }.toMap()
    }


    fun currentUrl(): String {
        return try {
            (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request.requestURL.toString()
        } catch (e: Exception) {
            "/"
        }
    }
}