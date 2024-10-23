package guru.springframework.json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    private static final Map<String, Set<Integer>> invertedIndex = new HashMap<>();
    private static final List<String> people = new ArrayList<>();

    public static void main(String[] args) {
        String dataFile = null;
        for (int i = 0; i < args.length - 1; i++) {
            if ("--data".equals(args[i])) {
                dataFile = args[i + 1];
                break;
            }
        }

        if (dataFile != null) {
            readDataFromFile(dataFile);
        } else {
            readDataFromInput();
        }

        buildInvertedIndex();
        startMenu();
    }

    private static void readDataFromFile(String fileName) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(fileName));
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    people.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error: Unable to read the file. " + e.getMessage());
            System.exit(1);
        }
    }

    private static void readDataFromInput() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the number of people:");
        int n;
        try {
            n = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number entered.");
            return;
        }
        System.out.println("Enter all people:");
        for (int i = 0; i < n; i++) {
            String person = scanner.nextLine().trim();
            if (!person.isEmpty()) {
                people.add(person);
            }
        }
    }

    private static void buildInvertedIndex() {
        for (int i = 0; i < people.size(); i++) {
            String line = people.get(i).toLowerCase().trim();
            String[] words = line.split("\\s+");
            for (String word : words) {
                invertedIndex.computeIfAbsent(word, k -> new HashSet<>()).add(i);
            }
        }
    }

    private static void startMenu() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            printMenu();
            String optionInput = scanner.nextLine().trim();
            int option;
            try {
                option = Integer.parseInt(optionInput);
            } catch (NumberFormatException e) {
                System.out.println("Incorrect option! Try again.");
                continue;
            }

            switch (option) {
                case 1:
                    findPerson(scanner);
                    break;
                case 2:
                    printAllPeople();
                    break;
                case 0:
                    System.out.println("Bye!");
                    return;
                default:
                    System.out.println("Incorrect option! Try again.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("=== Menu ===");
        System.out.println("1. Find a person");
        System.out.println("2. Print all people");
        System.out.println("0. Exit");
    }

    private static void findPerson(Scanner scanner) {
        System.out.println("Select a matching strategy: ALL, ANY, NONE");
        String strategy = scanner.nextLine().trim().toUpperCase();
        if (!strategy.equals("ALL") && !strategy.equals("ANY") && !strategy.equals("NONE")) {
            System.out.println("Unknown strategy.");
            return;
        }

        System.out.println("Enter a name or email to search all suitable people.");
        String query = scanner.nextLine().trim();
        if (query.isEmpty()) {
            System.out.println("No matching people found.");
            return;
        }
        String[] queryWords = query.split("\\s+");

        List<Integer> matchingIndexes = searchPeople(queryWords, strategy);

        if (matchingIndexes.isEmpty()) {
            System.out.println("No matching people found.");
        } else {
            System.out.println(matchingIndexes.size() + " persons found:");
            for (int index : matchingIndexes) {
                System.out.println(people.get(index));
            }
        }
    }

    private static List<Integer> searchPeople(String[] queryWords, String strategy) {
        Set<Integer> resultSet = new HashSet<>();

        switch (strategy) {
            case "ALL":
                boolean firstWord = true;
                for (String word : queryWords) {
                    Set<Integer> indexes = invertedIndex.get(word.toLowerCase());
                    if (indexes == null) {
                        resultSet.clear();
                        break;
                    }
                    if (firstWord) {
                        resultSet.addAll(indexes);
                        firstWord = false;
                    } else {
                        resultSet.retainAll(indexes);
                    }
                }
                break;

            case "ANY":
                for (String word : queryWords) {
                    Set<Integer> indexes = invertedIndex.get(word.toLowerCase());
                    if (indexes != null) {
                        resultSet.addAll(indexes);
                    }
                }
                break;

            case "NONE":
                resultSet.addAll(IntStream.range(0, people.size()).boxed().collect(Collectors.toSet()));
                for (String word : queryWords) {
                    Set<Integer> indexes = invertedIndex.get(word.toLowerCase());
                    if (indexes != null) {
                        resultSet.removeAll(indexes);
                    }
                }
                break;

            default:
                System.out.println("Invalid strategy.");
        }

        List<Integer> sortedResult = new ArrayList<>(resultSet);
        Collections.sort(sortedResult);
        return sortedResult;
    }

    private static void printAllPeople() {
        System.out.println("=== List of people ===");
        for (String person : people) {
            System.out.println(person);
        }
    }
}
