package dev.sotoestevez.jms.demo.model;

import java.io.Serializable;
import java.util.UUID;

public record SimpleMessage(UUID id, String message) implements Serializable { }
