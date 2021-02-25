package com.kawanansemut.universebasespring.scan

import com.kawanansemut.semutut.utility.FilterData
import com.kawanansemut.semutut.utility.FilterDataBuilder
import com.kawanansemut.universebasespring.scan.entities.EntityWithId
import com.kawanansemut.universebasespring.scan.entities.UniversalRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.rest.webmvc.RepositorySearchesResource
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.RepresentationModelProcessor
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

open class EntityBaseController<T : EntityWithId, ID>(
    val tClass: Class<T>,
    val universalRepository: UniversalRepository<T, ID>,
    val pagedResourcesAssembler: PagedResourcesAssembler<T>,
    val repositoryEntityLinks: RepositoryEntityLinks
) : RepresentationModelProcessor<RepositorySearchesResource> {

    fun currentLink(): String {
        return repositoryEntityLinks.linkToCollectionResource(tClass).href.split("{")[0]
    }

    open fun findAllFunction(filter: String?, pageable: Pageable): Page<T> {


        return if (filter == null) {
            universalRepository.findAll(pageable)
        } else {
            val spec = Specification<T> { r, cq, cb ->
                val fd = FilterData.fromJson(filter)
                FilterDataBuilder(fd, tClass).buildPredicate(r, cq, cb)
            }
            universalRepository.findAll(spec, pageable)
        }
    }

    open fun findAllWithFilter(
        filter: String?,
        pageable: Pageable
    ): ResponseEntity<PagedModel<EntityModel<T>>> {
        val pageModel = findAllFunction(filter, pageable)

        val pagedModel = pagedResourcesAssembler.toModel(pageModel, buildAssembler(tClass, repositoryEntityLinks))
        return ResponseEntity.ok(pagedModel)
    }

    open fun deleteMultiple(ids: Array<ID>): ResponseEntity<Any> {
        val failed = ids.mapNotNull { universalRepository.findByIdOrNull(it) }.mapNotNull {
            try {
                universalRepository.delete(it)
                null
            } catch (e: Exception) {
                FailedResponse(e.message, it)
            }
        }
        return if (failed.isNotEmpty()) {
            ResponseEntity.ok(
                mutableMapOf(
                    Pair("deleted", ids.size - failed.size),
                    Pair("failed", mutableMapOf(Pair("total", failed.size), Pair("list", failed)))
                )
            )
        } else {
            ResponseEntity(HttpStatus.NO_CONTENT)
        }
    }

    protected fun <T : EntityWithId> buildAssembler(
        tClass: Class<T>,
        repositoryEntityLinks: RepositoryEntityLinks
    ): RepresentationModelAssembler<T, EntityModel<T>> {
        return RepresentationModelAssembler { entity ->
            val personLink: Link = repositoryEntityLinks.linkToItemResource(tClass, entity.withId())
            val selfLink = Link.of(personLink.href, "self")
            val links = mutableListOf(selfLink, personLink)
            links.addAll(repositoryEntityLinks.linksToSearchResources(tClass).toList())
            EntityModel.of(entity, links)
        }
    }

    override fun process(model: RepositorySearchesResource): RepositorySearchesResource {
        if (!model.links.any { it.hasRel("customFind") }) {
            model.links.first { it.hasRel("self") }.href
            model.add(
                Link.of(model.links.first { it.hasRel("self") }.href + "/customFind{?filter,page,size,sort}")
                    .withRel("customFind")
            )
        }
        return model
    }

}

class FailedResponse(val message: String?, val obj: Any)