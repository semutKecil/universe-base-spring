package com.kawanansemut.universebasespring.scan.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import com.kawanansemut.universebasespring.scan.AppContext
import com.kawanansemut.universebasespring.lib.Helper
import com.kawanansemut.universebasespring.scan.clients.IdGeneratorClient
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.PagingAndSortingRepository
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

interface EntityWithId {
    fun withId(): Any
}


@MappedSuperclass
abstract class BasicEntity : Serializable, EntityWithId {
    @Id
    var id: Long = 0

    override fun withId(): Any {
        return id
    }

    @JsonIgnore
    @Transient
    private var _beforeUpdate: Map<String, Any>? = null

    @PostLoad
    fun afterLoad() {
        _beforeUpdate = Helper.objectToMap(this)
    }

    @PrePersist
    fun beforeAdd() {
        this.id = AppContext.getBean(IdGeneratorClient::class.java).generateId().toLong()
    }
}

@MappedSuperclass
abstract class BasicEntityWithDate : BasicEntity() {
    val created = LocalDateTime.now()
    var updated: LocalDateTime? = null

    @PreUpdate
    fun beforeDateUpdate() {
        this.updated = LocalDateTime.now()
    }
}

@NoRepositoryBean
interface BasicUniversalRepository<T> : UniversalRepository<T, Long>, JpaSpecificationExecutor<T>

@NoRepositoryBean
interface UniversalRepository<T, ID> : PagingAndSortingRepository<T, ID>, JpaSpecificationExecutor<T>