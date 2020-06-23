package com.company;

import com.company.exceptions.ProcessingException;
import com.company.exceptions.ValidationException;
import com.company.model.Command;
import com.company.parser.CommandLineParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class LineEditor {

    private final String fileName;
    private final Map<Command, Function<List<String>, String>> commands = initializeCommands();
    private final List<String> content;

    public LineEditor(final String fileName) {
        this.fileName = fileName;
        this.content = readFile(); // load file from disk
    }

    private Map<Command, Function<List<String>, String>> initializeCommands() {
        Map<Command, Function<List<String>, String>> commands = new HashMap<>();

        commands.put(Command.LIST, args -> {
            final int[] index = {0};
            return content.stream()
                    .map(line -> String.format("%d: %s", ++index[0], line)) // construct formatted line
                    .reduce((a, b) -> String.format("%s\n%s", a, b)) // merge all lines to one string
                    .orElse("");
        });

        commands.put(Command.DEL, args -> {
            int index = parseIndex(args);
            if (index < 0 || index >= content.size()) {
                throw new ProcessingException(String.format("can delete only from 1-%d lines", content.size()));
            }
            return content.remove(index);
        });

        commands.put(Command.INS, args -> {
            int index = parseIndex(args);
            String text = args.get(2);
            if (index < content.size()) {
                content.set(index, text); // modify existing line
            } else if (index == content.size()) {
                content.add(text); // insert new line
            } else {
                throw new ProcessingException(String.format(
                        "can insert only into 1-%d lines", content.size() + 1)); // bad line number provided
            }
            return text;
        });

        commands.put(Command.SAVE, args -> {
            // transform content to bytes
            byte[] output = content
                    .stream()
                    .reduce((a, b) -> String.format("%s\n%s", a, b))
                    .orElse("")
                    .getBytes();
            try {
                Files.write(Paths.get(fileName), output); // write file to disk
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "saved";
        });

        commands.put(Command.QUIT, args -> {
            System.out.println("bye");
            System.exit(0); //the best place of application
            return "bye";
        });

        commands.put(Command.HELP, args ->
                Arrays.stream(Command.values())
                        .map(Command::getDescription)
                        .collect(Collectors.joining("\n")));

        return Collections.unmodifiableMap(commands);
    }

    private List<String> readFile() {
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            return stream.collect(Collectors.toList());
        } catch (NoSuchFileException ex) {
            System.out.println(String.format("file not found: %s", fileName));
            System.exit(1);
            return Collections.emptyList();
        } catch (IOException ex) {
            throw new ProcessingException(ex);
        }
    }

    private int parseIndex(final List<String> arguments) {
        return Integer.parseInt(arguments.get(1)) - 1;
    }

    public static void main(final String[] args) {
        if (args.length == 0 || args[0] == null || args[0].trim().isEmpty()) {
            System.out.println("file name cannot be null or empty");
            return;
        }
        new LineEditor(args[0].trim()).run();
    }

    private void run() {
        CommandLineParser parser = new CommandLineParser();
        System.out.println("*** Welcome to line-editor ***\n" +
                "type 'help' to list all available commands\n" +
                "type 'quit' to exit");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                try {
                    String input = reader.readLine();
                    if (input == null || input.isEmpty()) {
                        continue; // skip empty input
                    }
                    List<String> commandWithArgs = parser.parse(input);
                    String result = execute(commandWithArgs);
                    System.out.println(result);
                } catch (ValidationException | ProcessingException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String execute(final List<String> args) {
        return commands.get(Command.fromString(args.get(0))).apply(args);
    }
}
