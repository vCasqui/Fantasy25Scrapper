package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataManager {
    private static final String SAVE_DIRECTORY = "data";
    private static final String PILOTS_FILE = "pilots.dat";

    // Ensure the save directory exists
    static {
        File directory = new File(SAVE_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * Save a list of pilots to a file
     * @param pilots The list of pilots to save
     * @return true if saved successfully, false otherwise
     */
    public static boolean savePilots(List<Piloto> pilots) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(SAVE_DIRECTORY + File.separator + PILOTS_FILE))) {
            oos.writeObject(pilots);
            System.out.println("Successfully saved " + pilots.size() + " pilots to file");
            return true;
        } catch (IOException e) {
            System.err.println("Error saving pilots: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Load the list of pilots from a file
     * @return The list of pilots, or an empty list if the file doesn't exist or an error occurs
     */
    @SuppressWarnings("unchecked")
    public static List<Piloto> loadPilots() {
        File file = new File(SAVE_DIRECTORY + File.separator + PILOTS_FILE);
        if (!file.exists()) {
            System.out.println("No saved pilots file found");
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file))) {
            List<Piloto> pilots = (List<Piloto>) ois.readObject();
            System.out.println("Successfully loaded " + pilots.size() + " pilots from file");
            return pilots;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading pilots: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Save an individual pilot to a file
     * @param pilot The pilot to save
     * @return true if saved successfully, false otherwise
     */
    public static boolean savePilot(Piloto pilot) {
        String fileName = pilot.getName().replaceAll("[^a-zA-Z0-9]", "_") + ".dat";
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(SAVE_DIRECTORY + File.separator + fileName))) {
            oos.writeObject(pilot);
            System.out.println("Successfully saved pilot " + pilot.getName() + " to file");
            return true;
        } catch (IOException e) {
            System.err.println("Error saving pilot " + pilot.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Load a pilot from a file
     * @param pilotName The name of the pilot to load
     * @return The pilot, or null if the file doesn't exist or an error occurs
     */
    public static Piloto loadPilot(String pilotName) {
        String fileName = pilotName.replaceAll("[^a-zA-Z0-9]", "_") + ".dat";
        File file = new File(SAVE_DIRECTORY + File.separator + fileName);
        if (!file.exists()) {
            System.out.println("No saved file found for pilot: " + pilotName);
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(file))) {
            Piloto pilot = (Piloto) ois.readObject();
            System.out.println("Successfully loaded pilot " + pilot.getName() + " from file");
            return pilot;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading pilot " + pilotName + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}