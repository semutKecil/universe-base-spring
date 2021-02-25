package com.kawanansemut.universebasespring.scan.clients

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.RequestMapping

@FeignClient(value = "idGenerator", url = "\${client.id-generator}")
interface IdGeneratorClient {
    @RequestMapping
    fun generateId(): String
}