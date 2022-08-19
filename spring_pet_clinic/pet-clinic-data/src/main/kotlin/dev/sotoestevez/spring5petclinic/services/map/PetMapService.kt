package dev.sotoestevez.spring5petclinic.services.map

import dev.sotoestevez.spring5petclinic.model.Pet
import dev.sotoestevez.spring5petclinic.model.PetType
import dev.sotoestevez.spring5petclinic.services.PetService
import org.springframework.stereotype.Service

@Service
class PetMapService : AbstractMapService<Pet>(), PetService {

	private val types: MutableMap<Long, PetType> = HashMap()

	override fun save(entity: Pet): Pet {
		entity.petType = getPetType(entity.petType)
		return super.save(entity)
	}

	override fun findPetTypes(): List<PetType> = types.values.toList()

	private fun getPetType(entity: PetType): PetType {
		return if (types.values.any { it.name == entity.name })
			types.values.first { it.name == entity.name }
		else saveOnMap(entity, types)
	}

}