package org.example;

import java.util.List;
import java.util.Scanner;

import static org.example.NextRacePrinter.printPilotosOrderedByPointsToExcelent;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("Choose an option:");
        System.out.println("1. Run web scraper to get all new data");
        System.out.println("2. Load saved data and update with new circuits only");
        System.out.println("3. Load saved data and process race data with PriceCalc");
        System.out.print("Enter your choice (1, 2, or 3): ");
        
        String choice = scanner.nextLine().trim();
        
        List<Piloto> pilotos = null;
        
        if (choice.equals("2")) {
            // Load saved data
            pilotos = DataManager.loadPilots();
            
            if (pilotos == null || pilotos.isEmpty()) {
                System.out.println("No saved data found. Proceeding with full web scraping...");
                pilotos = runFullScraper();
            } else {
                System.out.println("Loaded " + pilotos.size() + " pilots. Checking for updates.....");
                pilotos = updatePilotCircuits(pilotos);
            }
        } else if (choice.equals("3")) {
            // Load saved data and process with PriceCalc
            pilotos = DataManager.loadPilots();
            
            if (pilotos == null || pilotos.isEmpty()) {
                System.out.println("No saved data found. Please run option 1 or 2 first to gather data.");
            } else {
                System.out.println("Loaded " + pilotos.size() + " pilots. Processing race data...");
                System.out.println("\n=== RACE DATA ANALYSIS ===");
            
                // Process each pilot with PriceCalc
                for (Piloto piloto : pilotos) {
                    System.out.println("\nAnalyzing pilot: " + piloto.getName());
                    PriceCalc.processLastTwoEntries(piloto);
                }
            
                System.out.println("\n=== ANALYSIS COMPLETE ===");

                printPilotosOrderedByPointsToExcelent(pilotos);
            }
        } else {
            // Default to running the full scraper
            pilotos = runFullScraper();
        }
        
        // Display the results if we have pilots and didn't choose option 3
        if (pilotos != null && !pilotos.isEmpty() && !choice.equals("3")) {
            displayResults(pilotos);
        
            // Ask if user wants to save the data
            System.out.print("Do you want to save this data? (y/n): ");
            String saveChoice = scanner.nextLine().trim().toLowerCase();
        
            if (saveChoice.equals("y")) {
                boolean saved = DataManager.savePilots(pilotos);
                if (saved) {
                    System.out.println("Data saved successfully.");
                } else {
                    System.out.println("Failed to save data.");
                }
            }
        }
        
        scanner.close();
    }
    
    private static List<Piloto> runFullScraper() {
        // Create an instance of the StatScrapper
        StatScrapper scraper = new StatScrapper();
        List<Piloto> pilotos = null;
        
        try {
            // Run the scraper and get the list of Piloto objects
            pilotos = scraper.scrapeDriverStats();
        } catch (Exception e) {
            System.err.println("Error in scraping process: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Make sure to close the WebDriver
            scraper.close();
        }
        
        return pilotos;
    }
    
    private static List<Piloto> updatePilotCircuits(List<Piloto> existingPilotos) {
        // Create an instance of the StatScrapper
        StatScrapper scraper = new StatScrapper();
        List<Piloto> updatedPilotos = null;
        
        try {
            // Update the existing pilots with new circuit data
            updatedPilotos = scraper.updatePilotCircuits(existingPilotos);
        } catch (Exception e) {
            System.err.println("Error updating pilots: " + e.getMessage());
            e.printStackTrace();
            // Return the original list if there was an error
            updatedPilotos = existingPilotos;
        } finally {
            // Make sure to close the WebDriver
            scraper.close();
        }
        
        return updatedPilotos;
    }
    
    private static void displayResults(List<Piloto> pilotos) {
        System.out.println("\n=== DRIVER INFORMATION ===\n");
        if (pilotos != null && !pilotos.isEmpty()) {
            System.out.println("Found " + pilotos.size() + " drivers:");
            for (Piloto piloto : pilotos) {
                System.out.println(piloto);
                System.out.println("----------------------------");
            }
        } else {
            System.out.println("No driver information was extracted.");
        }
    }
}