package dev.sotoestevez.spring5petclinic

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class Spring5PetClinicApplication

fun main(args: Array<String>) {
	runApplication<Spring5PetClinicApplication>(*args)
}
