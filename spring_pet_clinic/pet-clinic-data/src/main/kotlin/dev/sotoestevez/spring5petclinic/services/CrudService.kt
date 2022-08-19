package dev.sotoestevez.spring5petclinic.services

interface CrudService<T, ID> {

	fun findAll(): Set<T>

	fun findById(id: ID): T?

	fun save(entity: T): T

	fun delete(entity: T): T?

	fun deleteById(id: ID): T?

	fun count(): Int

}