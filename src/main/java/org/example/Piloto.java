package org.example;

import java.io.Serial;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Piloto implements Serializable{
    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private double value;
    private int pointsToPoor;
    private int pointsToGood;
    private int pointsToExcelent;
    private Map<String, String> data;

    // Constructor
    public Piloto(String name) {
        this.name = name;
        this.data = new LinkedHashMap<>();
    }

    // Constructor with value (keeping for compatibility)
    public Piloto(String name, double value) {
        this.name = name;
        this.data = new LinkedHashMap<>();
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getData() {
        return data;
    }

    // Method to add circuit data with points, value and trend
    public void addCircuitData(String circuitName, int points, double value, double trend) {
        String formattedData = String.format("%d pts | $%.1fM | %s%.1fM", 
                                            points, 
                                            value,
                                            (trend >= 0 ? "+" : ""), 
                                            trend);
        data.put(circuitName, formattedData);
    }

    // For compatibility with existing code - redirects to new method
    public void addData(String circuitName, Integer points) {
        String formattedData = String.format("%d pts",
                points);
        data.put(circuitName, formattedData);
    }

    // For compatibility with existing code - does nothing now
    public void setValue(double value) {
        this.value = value;
    }
    public double getValue() {
        return value;
    }
    // For compatibility with existing code - does nothing now
    public void setTrend(double trend) {
        // Trend is now stored per circuit
    }

    // Getters and setters for the pointsToPoor field
    public int getPointsToPoor() {
        return pointsToPoor;
    }

    public void setPointsToPoor(int pointsToPoor) {
        this.pointsToPoor = pointsToPoor;
    }

    // Getters and setters for the pointsToGood field
    public int getPointsToGood() {
        return pointsToGood;
    }

    public void setPointsToGood(int pointsToGood) {
        this.pointsToGood = pointsToGood;
    }

    // Getters and setters for the pointsoExcelent field
    public int getPointsToExcelent() {
        return pointsToExcelent;
    }

    public void setPointsToExcelent(int pointsoExcelent) {
        this.pointsToExcelent = pointsoExcelent;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Piloto: ").append(name).append("\n");

        sb.append("Circuit Stats (last 3):\n");
        
        // Get last 3 circuits using streams
        Map<String, String> lastThreeCircuits = data.entrySet().stream()
            .skip(Math.max(0, data.size() - 3))
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
        
        // Display the last 3 circuits
        for (Map.Entry<String, String> entry : lastThreeCircuits.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        return sb.toString();
    }
}