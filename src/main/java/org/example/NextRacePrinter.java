package org.example;

import java.util.Comparator;
import java.util.List;

public class NextRacePrinter {
    
    /**
     * Prints a list of Pilotos ordered by pointsToExcelent in ascending order
     * Includes tier designation: 'A' for drivers with value >= 19, 'B' for others
     * 
     * @param pilotos List of Piloto objects to be printed
     */
    public static void printPilotosOrderedByPointsToExcelent(List<Piloto> pilotos) {
        if (pilotos == null || pilotos.isEmpty()) {
            System.out.println("No pilots to display.");
            return;
        }
        
        // Sort the list by pointsToExcellent in ascending order
        pilotos.sort(Comparator.comparingInt(Piloto::getPointsToExcelent));
        
        // Define column widths
        int tierWidth = 5;
        int driverNameWidth = 30;
        int pointsWidth = 15;
        
        // Create the format string
        String formatString = "%-" + tierWidth + "s %-" + driverNameWidth + "s %" + 
                              pointsWidth + "d %" + pointsWidth + "d %" + pointsWidth + "d";
        
        // Print section header
        System.out.println("\n=== DRIVERS RANKED BY POINTS NEEDED FOR EXCELLENT PERFORMANCE ===\n");
        
        // Print the table header with proper alignment
        System.out.println(String.format("%-" + tierWidth + "s %-" + driverNameWidth + "s %" + 
                           pointsWidth + "s %" + pointsWidth + "s %" + pointsWidth + "s", 
                           "Tier", "Driver Name", "Points To Poor", "Points To Good", "Points To Excellent"));
        
        // Print separator line
        StringBuilder separator = new StringBuilder();
        separator.append("-".repeat(tierWidth)).append(" ");
        separator.append("-".repeat(driverNameWidth)).append(" ");
        separator.append("-".repeat(pointsWidth)).append(" ");
        separator.append("-".repeat(pointsWidth)).append(" ");
        separator.append("-".repeat(pointsWidth));
        System.out.println(separator.toString());
        
        // Print each pilot's data with consistent spacing and tier designation
        for (Piloto piloto : pilotos) {
            // Determine tier based on driver value
            String tier = (piloto.getValue() >= 19.0) ? "A" : "B";
            
            System.out.println(String.format(formatString, 
                               tier,
                               piloto.getName(),
                               piloto.getPointsToPoor(), 
                               piloto.getPointsToGood(), 
                               piloto.getPointsToExcelent()));
        }
        
        // Print footer
        System.out.println("\n=== END OF RANKING ===\n");
    }
}