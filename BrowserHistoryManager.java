
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BrowserHistoryManager {

    private static final String FILE_NAME = "history.txt";

    // Data Structures
    private static LinkedList<PageVisit> history = new LinkedList<>();
    private static Stack<String> backStack = new Stack<>();
    private static Stack<String> forwardStack = new Stack<>();
    private static HashMap<String, Integer> visitCount = new HashMap<>();

    private static String currentPage = null;

    private static Scanner sc = new Scanner(System.in);

    private static DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    static class PageVisit {
        String url;
        LocalDateTime dateTime;

        PageVisit(String url, LocalDateTime dateTime) {
            this.url = url;
            this.dateTime = dateTime;
        }
    }

    public static boolean isValidURL(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        url = url.trim();

        return url.contains(".")
                && url.indexOf('.') > 0
                && url.lastIndexOf('.') < url.length() - 1
                && !url.matches(".*\\s+.*");
    }

    // Visit a new website
    public static void visitPage() {

        System.out.print("Enter URL: ");
        String url = sc.nextLine().trim();

        if (!isValidURL(url)) {
            System.out.println(
                "Invalid URL. Enter a URL like example.com.");
            return;
        }

        if (currentPage != null) {
            backStack.push(currentPage);
        }

        // New visit clears forward history
        forwardStack.clear();

        currentPage = url;

        history.add(new PageVisit(url, LocalDateTime.now()));

        visitCount.put(url,
                visitCount.getOrDefault(url, 0) + 1);

        System.out.println("Visited: " + url);
        System.out.println("Current page: " + currentPage);

        saveHistory();
    }

    // Back navigation
    public static void goBack() {

        if (currentPage == null || backStack.empty()) {
            System.out.println("No previous page.");
            if (currentPage != null) {
                System.out.println("Current page: " + currentPage);
            }
            return;
        }

        forwardStack.push(currentPage);
        currentPage = backStack.pop();

        System.out.println("Back to: " + currentPage);
        saveHistory();
    }

    // Forward navigation
    public static void goForward() {

        if (currentPage == null || forwardStack.empty()) {
            System.out.println("No forward page.");
            if (currentPage != null) {
                System.out.println("Current page: " + currentPage);
            }
            return;
        }

        backStack.push(currentPage);
        currentPage = forwardStack.pop();

        System.out.println("Forward to: " + currentPage);
        saveHistory();
    }

    // Display current page
    public static void showCurrentPage() {

        if (currentPage == null) {
            System.out.println("No page currently open.");
        } else {
            System.out.println("Current page: " + currentPage);
        }
    }

    // Display complete browsing history
    public static void showHistory() {

        if (history.isEmpty()) {
            System.out.println("Browsing history is empty.");
            return;
        }

        System.out.println("\n========== BROWSER HISTORY ==========");

        int i = 1;

        for (PageVisit page : history) {
            System.out.println(
                i++ + ". " + page.url +
                " | " + page.dateTime.format(formatter)
            );
        }

        System.out.println("======================================");
    }

    // Display website visit counts
    public static void showVisitCount() {

        if (visitCount.isEmpty()) {
            System.out.println("No visit records found.");
            return;
        }

        System.out.println("\n====== WEBSITE VISIT COUNTS ======");

        for (Map.Entry<String, Integer> entry :
                visitCount.entrySet()) {

            System.out.println(
                entry.getKey() + " : " + entry.getValue() + " visits"
            );
        }

        System.out.println("==================================");
    }

    // Search website in browsing history
    public static void searchHistory() {

        System.out.print("Enter website to search: ");
        String search = sc.nextLine().trim();

        boolean found = false;

        for (PageVisit page : history) {

            if (page.url.toLowerCase().contains(
                    search.toLowerCase())) {

                System.out.println(
                    page.url + " | " +
                    page.dateTime.format(formatter)
                );

                found = true;
            }
        }

        if (!found) {
            System.out.println("Website not found in history.");
        }
    }

    // Filter history using date range
    public static void filterByDate() {

        try {
            System.out.print("Enter start date (yyyy-MM-dd): ");
            LocalDate start = LocalDate.parse(sc.nextLine().trim());

            System.out.print("Enter end date (yyyy-MM-dd): ");
            LocalDate end = LocalDate.parse(sc.nextLine().trim());

            if (end.isBefore(start)) {
                System.out.println("Invalid date range.");
                return;
            }

            boolean found = false;

            System.out.println("\n====== FILTERED HISTORY ======");

            for (PageVisit page : history) {

                LocalDate date = page.dateTime.toLocalDate();

                if (!date.isBefore(start) && !date.isAfter(end)) {

                    System.out.println(
                        page.url + " | " +
                        page.dateTime.format(formatter)
                    );

                    found = true;
                }
            }

            if (!found) {
                System.out.println("No records found in this date range.");
            }

        } catch (Exception e) {
            System.out.println("Invalid date format. Use yyyy-MM-dd.");
        }
    }

    // Delete all browsing history
    public static void deleteHistory() {

        System.out.print(
            "Are you sure you want to delete all history? (yes/no): ");

        String choice = sc.nextLine().trim();

        if (choice.equalsIgnoreCase("yes")) {

            history.clear();
            backStack.clear();
            forwardStack.clear();
            visitCount.clear();
            currentPage = null;

            saveHistory();

            System.out.println("All browsing history deleted.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    // Save history and navigation state to file
    public static void saveHistory() {

        try (BufferedWriter writer =
                new BufferedWriter(new FileWriter(FILE_NAME))) {

            // Save visited pages
            for (PageVisit page : history) {

                String encodedURL = Base64.getEncoder()
                        .encodeToString(page.url.getBytes("UTF-8"));

                writer.write("H|" + page.dateTime.toString()
                        + "|" + encodedURL);

                writer.newLine();
            }

            // Save current page
            String encodedCurrent = currentPage == null ? "-"
                    : Base64.getEncoder().encodeToString(
                        currentPage.getBytes("UTF-8"));

            writer.write("C|" + encodedCurrent);
            writer.newLine();

            // Save back stack (bottom to top)
            for (String url : backStack) {

                String encoded = Base64.getEncoder()
                        .encodeToString(url.getBytes("UTF-8"));

                writer.write("B|" + encoded);
                writer.newLine();
            }

            // Save forward stack (bottom to top)
            for (String url : forwardStack) {

                String encoded = Base64.getEncoder()
                        .encodeToString(url.getBytes("UTF-8"));

                writer.write("F|" + encoded);
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error saving history: " + e.getMessage());
        }
    }

    // Load browsing history when application starts
    public static void loadHistory() {

        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return;
        }

        history.clear();
        backStack.clear();
        forwardStack.clear();
        visitCount.clear();
        currentPage = null;

        try (BufferedReader reader =
                new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split("\\|", 3);

                if (parts[0].equals("H") && parts.length == 3) {

                    String url = new String(
                        Base64.getDecoder().decode(parts[2]),
                        "UTF-8"
                    );

                    LocalDateTime time =
                            LocalDateTime.parse(parts[1]);

                    history.add(new PageVisit(url, time));

                    visitCount.put(url,
                        visitCount.getOrDefault(url, 0) + 1);

                } else if (parts[0].equals("C")
                        && parts.length == 2) {

                    if (!parts[1].equals("-")) {
                        currentPage = new String(
                            Base64.getDecoder().decode(parts[1]),
                            "UTF-8"
                        );
                    }

                } else if (parts[0].equals("B")
                        && parts.length == 2) {

                    backStack.push(new String(
                        Base64.getDecoder().decode(parts[1]),
                        "UTF-8"
                    ));

                } else if (parts[0].equals("F")
                        && parts.length == 2) {

                    forwardStack.push(new String(
                        Base64.getDecoder().decode(parts[1]),
                        "UTF-8"
                    ));
                }
            }

            System.out.println("Previous history loaded successfully.");

        } catch (Exception e) {
            System.out.println("Error loading history: " + e.getMessage());
        }
    }

    // Main menu
    public static void main(String[] args) {

        loadHistory();

        int choice = -1;

        do {
            System.out.println("\n");
            System.out.println("====================================");
            System.out.println("       BROWSER HISTORY MANAGER");
            System.out.println("====================================");
            System.out.println("1. Visit New Website");
            System.out.println("2. Back");
            System.out.println("3. Forward");
            System.out.println("4. Show Current Page");
            System.out.println("5. Show Browsing History");
            System.out.println("6. Show Website Visit Count");
            System.out.println("7. Search Website History");
            System.out.println("8. Filter History by Date");
            System.out.println("9. Delete All History");
            System.out.println("10. Save History");
            System.out.println("0. Exit");
            System.out.println("====================================");

            System.out.print("Enter your choice: ");

            try {
                choice = Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid menu number.");
                continue;
            }

            switch (choice) {

                case 1:
                    visitPage();
                    break;

                case 2:
                    goBack();
                    break;

                case 3:
                    goForward();
                    break;

                case 4:
                    showCurrentPage();
                    break;

                case 5:
                    showHistory();
                    break;

                case 6:
                    showVisitCount();
                    break;

                case 7:
                    searchHistory();
                    break;

                case 8:
                    filterByDate();
                    break;

                case 9:
                    deleteHistory();
                    break;

                case 10:
                    saveHistory();
                    System.out.println("History saved successfully.");
                    break;

                case 0:
                    saveHistory();
                    System.out.println("History saved successfully.");
                    System.out.println("Thank you for using Browser History Manager!");
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }

        } while (choice != 0);

        sc.close();
    }
}
