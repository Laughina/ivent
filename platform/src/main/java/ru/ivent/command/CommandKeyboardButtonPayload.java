package ru.ivent.command;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * @author Laughina
 */
@Value
@Builder
@Jacksonized
public class CommandKeyboardButtonPayload {
    @JsonProperty("n")
    @JsonAlias("name")
    String name;
    @JsonProperty("a")
    @JsonAlias("args")
    Object[] args;
}
