package org.example;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PriceCalc {
    
    /**
     * Processes the last two entries in a Piloto's data map and extracts race points and race value
     * for each entry. The extracted data is stored in instance variables for later use.
     * 
     * @param piloto The Piloto object containing the data
     */
    public static void processLastTwoEntries(Piloto piloto) {
        if (piloto == null) {
            System.out.println("Error: Piloto object is null");
            return;
        }
        
        Map<String, String> data = piloto.getData();
        if (data == null || data.isEmpty()) {
            System.out.println("No data available for pilot: " + piloto.getName());
            return;
        }

        // Get the last two entries from the LinkedHashMap
        List<Map.Entry<String, String>> lastTwoEntries = new ArrayList<>();

        // Convert to array to get last two entries
        Map.Entry<String, String>[] entries = data.entrySet().toArray(new Map.Entry[0]);
        int size = entries.length;

        // Get last two entries or all if less than two
        int startIndex = Math.max(0, size - 2);
        for (int i = startIndex; i < size; i++) {
            lastTwoEntries.add(entries[i]);
        }

        double[] PPM = new double[3];
        int i = 0;

        // Process each of the last two entries
        for (Map.Entry<String, String> entry : lastTwoEntries) {
            String circuitName = entry.getKey();
            String circuitData = entry.getValue();
            
            // Extract race points and race value using regex
            double[] raceData = extractRaceData(circuitData);
            
            if (raceData != null) {
                double racePoints = raceData[0];
                double raceValue = raceData[1];

                double racePPM = racePoints / raceValue;

                PPM[i] = racePPM;
                
                // Instead of returning, we process or store the data
                System.out.println("Circuit: " + circuitName);
                System.out.println("  Race Points: " + racePoints);
                System.out.println("  Race Value: " + raceValue);
                System.out.println("  Race PPM: " + racePPM);
                // You can store this data in a database, use it for calculations, etc.
                // For now we're just printing it
            }
            i++;
        }

        System.out.println("Average PPM: " + (PPM[0] + PPM[1] + PPM[2])/3 );

        double poor = 0.6;
        double good = 0.9;
        double excellent = 1.2;

        double currentValue = piloto.getValue();

        System.out.println("Current Value: " + currentValue);

        int pointStart = -1000;
        int pointsToPoor;
        int pointsToGood;
        int pointsToExcelent;

        double nextRacePPM = pointStart / currentValue;

        PPM[2] = nextRacePPM;
        while( (PPM[0] + PPM[1] + PPM[2])/3 < poor){
            pointStart++;
            nextRacePPM = pointStart / currentValue;
            PPM[2] = nextRacePPM;
        }
        pointsToPoor = pointStart;
        while( (PPM[0] + PPM[1] + PPM[2])/3 < good){
            pointStart++;
            nextRacePPM = pointStart / currentValue;
            PPM[2] = nextRacePPM;
        }
        pointsToGood = pointStart;
        while( (PPM[0] + PPM[1] + PPM[2])/3 < excellent){
            pointStart++;
            nextRacePPM = pointStart / currentValue;
            PPM[2] = nextRacePPM;
        }
        pointsToExcelent = pointStart;

        System.out.println("Points to Poor: " + pointsToPoor);
        System.out.println("Points to Good: " + pointsToGood);
        System.out.println("Points to Excelent: " + pointsToExcelent);

        piloto.setPointsToExcelent(pointsToExcelent);
        piloto.setPointsToGood(pointsToGood);
        piloto.setPointsToPoor(pointsToPoor);

    }

    /**
     * Extracts race points and race value from a circuit data string
     * 
     * @param circuitData String containing circuit data in format "-16 pts | $4,5M | -0,5M"
     * @return double array where [0] = racePoints and [1] = raceValue, or null if parsing fails
     */
    private static double[] extractRaceData(String circuitData) {
        if (circuitData == null) {
            return null;
        }
        
        double[] result = new double[2];
        
        // Pattern to extract the race points, value and trend
        // Format example: "-16 pts | $4,5M | -0,5M"
        Pattern pattern = Pattern.compile("([-+]?\\d+)\\s+pts\\s+\\|\\s+\\$(\\d+[,.]\\d+)M\\s+\\|\\s+([-+]?\\d+[,.]\\d+)M");
        Matcher matcher = pattern.matcher(circuitData);
        
        if (matcher.find()) {
            // Extract race points
            result[0] = Double.parseDouble(matcher.group(1));
            
            // Extract value and trend, converting comma to dot if needed
            double value = Double.parseDouble(matcher.group(2).replace(',', '.'));
            double trend = Double.parseDouble(matcher.group(3).replace(',', '.'));
            
            // Calculate race value according to the formula: value + (trend * -1)
            result[1] = value + (trend * -1);
            
            return result;
        }
        
        // Handle older data format that might only contain points
        Pattern simplePattern = Pattern.compile("([-+]?\\d+)\\s+pts");
        Matcher simpleMatcher = simplePattern.matcher(circuitData);
        
        if (simpleMatcher.find()) {
            result[0] = Double.parseDouble(simpleMatcher.group(1));
            result[1] = 0; // Default value since no value/trend information is available
            return result;
        }
        
        return null;
    }
}