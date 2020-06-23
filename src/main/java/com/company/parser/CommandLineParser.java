package com.company.parser;

import com.company.model.Command;
import com.company.exceptions.ValidationException;

import java.util.*;

public final class CommandLineParser {

    public List<String> parse(final String input) {
        String trimmed = input.replaceAll("^\\s+", ""); // trim leading spaces
        Deque<String> split = new LinkedList<>(Arrays.asList(trimmed.split("\\s")));
        Command command = Command.fromString(split.pollFirst());

        if (command.getArgsCount() == 0) {
            return Collections.singletonList(command.getName());
        } else if (command.getArgsCount() == 1) {
            String firstArg = getFirstArg(command, split);
            return Collections.unmodifiableList(Arrays.asList(command.getName(), firstArg));
        } else if (command.getArgsCount() == 2) {
            String firstArg = getFirstArg(command, split);
            String secondArg = getSecondArg(command, split);
            return Collections.unmodifiableList(Arrays.asList(command.getName(), firstArg, secondArg));
        } else {
            throw new ValidationException("command supports maximum 2 args");
        }
    }

    private String getFirstArg(final Command command, final Deque<String> split) {
        while (true) {
            String first = split.pollFirst();
            if (first == null) {
                throwInvalidArgsException(command); // no arg provided
            }
            if (first.isEmpty()) {
                continue; // skip leading spaces
            }
            return validateFirstArg(command, first);
        }
    }

    private String getSecondArg(final Command command, final Deque<String> split) {
        String first = split.pollFirst();
        if (first == null) {
            throwInvalidArgsException(command); // no arg provided
        }
        if (first.isEmpty()) {
            return String.join(" ", split); // trim leading spaces
        }
        split.addFirst(first);
        return String.join(" ", split);
    }

    private String validateFirstArg(final Command command, final String firstArg) {
        if (firstArg.matches("^\\D+$")) {
            throw new ValidationException(String.format(
                    "first argument for command '%s' must be a number, got '%s'", command, firstArg));
        }
        if (Integer.parseInt(firstArg) < 1) {
            throw new ValidationException(String.format(
                    "first argument for command '%s' must be positive number, got '%s'", command, firstArg));
        }
        return firstArg;
    }

    private void throwInvalidArgsException(final Command command) {
        throw new ValidationException(String.format(
                "'%s' command must have %d argument(s)", command.getName(), command.getArgsCount()));
    }
}
