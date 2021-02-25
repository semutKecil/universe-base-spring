package com.kawanansemut.universebasespring.lib

import com.kawanansemut.universebasespring.scan.AppContext
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
import org.keycloak.admin.client.Keycloak
import org.keycloak.admin.client.KeycloakBuilder
import org.springframework.core.env.Environment
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

    val keycloak: Keycloak by lazy() {
        val env = AppContext.getBean(Environment::class.java)
        KeycloakBuilder.builder()
            .serverUrl(env.getProperty("keycloak.addr", "keycloak:8080"))
            .realm("master")
            .username(env.getProperty("keycloak.master.username", "admin"))
            .password(env.getProperty("keycloak.master.password", "admin"))
            .clientId("admin-cli")
            .resteasyClient(
                ResteasyClientBuilder()
                    .connectionPoolSize(10).build()
            )
            .build()
    }

}