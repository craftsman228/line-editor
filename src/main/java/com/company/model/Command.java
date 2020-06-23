package com.company.model;

import com.company.exceptions.ValidationException;

import java.util.Arrays;

public enum Command {

    LIST("list", 0, "list - list each line in n: xxx format"),
    DEL("del", 1, "del n - delete line at n"),
    INS("ins", 2, "ins n <string> - insert a line at n (row number)"),
    SAVE("save", 0, "save - saves to disk"),
    QUIT("quit", 0, "quit - quits the editor and returns to the command line"),
    HELP("help", 0, "help - prints all available commands");

    private final String name;
    private final int argsCount;
    private final String description;

    Command(final String name, final int argsCount, final String description) {
        this.name = name;
        this.argsCount = argsCount;
        this.description = description;
    }

    public final String getName() {
        return name;
    }

    public final int getArgsCount() {
        return argsCount;
    }

    public final String getDescription() {
        return description;
    }

    public static Command fromString(final String command) {
        return Arrays.stream(values())
                .filter(value -> value.name.equals(command))
                .findFirst()
                .orElseThrow(() -> new ValidationException(String.format(
                        "command must be one of %s", Arrays.toString(values()))));
    }

    @Override
    public final String toString() {
        return name;
    }
}
