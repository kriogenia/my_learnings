package dev.sotoestevez.spring5petclinic.services

import dev.sotoestevez.spring5petclinic.model.Owner

interface OwnerService : CrudService<Owner, Long> {

	fun findByLastName(lastName: String): List<Owner>

}