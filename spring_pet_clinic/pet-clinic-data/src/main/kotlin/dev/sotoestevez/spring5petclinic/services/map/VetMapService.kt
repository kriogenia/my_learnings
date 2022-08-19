package dev.sotoestevez.spring5petclinic.services.map

import dev.sotoestevez.spring5petclinic.model.Specialty
import dev.sotoestevez.spring5petclinic.model.Vet
import dev.sotoestevez.spring5petclinic.services.VetService
import org.springframework.stereotype.Service

@Service
class VetMapService : AbstractMapService<Vet>(), VetService {

	private val specialties: MutableMap<Long, Specialty> = HashMap()

	override fun save(entity: Vet): Vet {
		entity.specialties.forEach { it.id = getSpecialty(it).id }
		return super.save(entity)
	}

	private fun getSpecialty(entity: Specialty): Specialty {
		return if (specialties.values.any { it.name == entity.name })
			specialties.values.first { it.name == entity.name }
		else saveOnMap(entity, specialties)
	}

}