package dev.sotoestevez.spring5petclinic.model

import java.time.LocalDateTime

data class Visit(
	val date: LocalDateTime,
	val description: String,
	val pet: Pet
): BaseEntity()