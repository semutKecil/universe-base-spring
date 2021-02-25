package com.kawanansemut.universebasespring.lib

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
}