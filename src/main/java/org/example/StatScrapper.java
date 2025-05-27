package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;

public class StatScrapper {
    private WebDriver driver;
    private WebDriverWait wait;

    /**
     * Constructor - initializes the WebDriver with specified options
     */
    public StatScrapper() {
        // Setup Edge WebDriver
        WebDriverManager.edgedriver().setup();

        // Configure Edge options
        EdgeOptions options = new EdgeOptions();

        // Create a dedicated profile directory for Selenium
        String userHome = System.getProperty("user.home");
        String profilePath = userHome + "\\selenium-edge-profile";
        options.addArguments("user-data-dir=" + profilePath);

        System.out.println("Using Edge profile at: " + profilePath);

        // Initialize the WebDriver
        this.driver = new EdgeDriver(options);
        this.driver.manage().window().maximize(); // Maximize window to ensure elements are visible
        
        // Create a wait object for waiting for elements
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    /**
     * Main method to scrape driver statistics
     * @return List of Piloto objects with their stats
     */
    public List<Piloto> scrapeDriverStats() {
        List<Piloto> pilotos = new ArrayList<>();
        
        try {
            // Navigate to the website
            driver.get("https://fantasy.formula1.com/en/statistics/details?tab=driver&filter=fAvg");

            // Wait for the page to fully load
            waitForPageToLoad();

            // Find the parent container
            WebElement container = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("#root > div > div.si-master__wrap > section.si-stats__wrap > div:nth-child(2) > div > div > div > div.si-stats__tracker-grid-rhs > div > div")));

            // Find all LI elements inside the UL
            WebElement ul = container.findElement(By.tagName("ul"));
            List<WebElement> liElements = ul.findElements(By.tagName("li"));

            // Debug code to check things are returned properly
            System.out.println("Found " + liElements.size() + " LI elements");
            for (WebElement liElement : liElements) {
                System.out.println("Found element: " + liElement.getText());
            }

            // Process each driver in the list
            for (int i = 1; i < liElements.size(); i++) {
                Piloto piloto = processDriverElement(liElements, i);
                if (piloto != null) {
                    pilotos.add(piloto);
                }
            }

        } catch (Exception e) {
            System.err.println("An error occurred during web scraping:");
            e.printStackTrace();
        }
        
        return pilotos;
    }

    /**
     * Process a single driver element
     * @return a Piloto object with the driver's stats
     */
    private Piloto processDriverElement(List<WebElement> liElements, int index) {
        try {
            // Get the element
            WebElement element = liElements.get(index);

            // Scroll the element into view
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);

            // Wait briefly for the scroll to complete
            Thread.sleep(75);
            String teamName = "";
            try {
                WebElement teamElement = element.findElement(By.cssSelector("div.si-stats__list-item.teamname"));
                if (teamElement != null) {
                    teamName = teamElement.getText().trim();
                    System.out.println("Found team name: " + teamName);
                }
            } catch (Exception e) {
                System.out.println("Could not extract team name: " + e.getMessage());
                // Continue execution even if team name is not found
            }


            // Click on the element
            System.out.println("Clicking on the " + index + " element...");
            element.click();

            // Wait for the popup div to appear
            System.out.println("Waiting for popup to appear...");
            WebElement popup = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("#root > div > div.si-master__wrap > div > div > div.si-popup__body > div.si-driCon__playerInfo > div.si-player__name")));

            System.out.println("Popup appeared successfully!");

            // Get player name
            WebElement playerNameElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("#root > div > div.si-master__wrap > div > div > div.si-popup__body > div.si-driCon__playerInfo > div.si-player__name")));
            String playerName = playerNameElement.getText();
            System.out.println("Player Name: " + playerName);
            // Append team name to player name if it was found
            if (!teamName.isEmpty()) {
                playerName = playerName + " " + teamName;
                System.out.println("Player Name with Team: " + playerName);
            }

            // Get driver value from the popup - try all three possible selectors
            WebElement playerValueElement;
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofMillis(30));
            String selectorClass = ""; // Track which selector was successful

            try {
                // Try with .si-up first with a very short wait
                playerValueElement = shortWait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("#root > div > div.si-master__wrap > div > div > div.si-popup__body > div.si-driCon__playerInfo > div.si-player__trends.si-up")));
                selectorClass = "si-up";
            } catch (TimeoutException e) {
                try {
                    // If .si-up not found, try with .si-down with a short wait
                    playerValueElement = shortWait.until(ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("#root > div > div.si-master__wrap > div > div > div.si-popup__body > div.si-driCon__playerInfo > div.si-player__trends.si-down")));
                    selectorClass = "si-down";
                } catch (TimeoutException e2) {
                    // If both failed, try with .false using the normal wait
                    playerValueElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                            By.cssSelector("#root > div > div.si-master__wrap > div > div > div.si-popup__body > div.si-driCon__playerInfo > div.si-player__trends.false")));
                    selectorClass = "false";
                }
            }

            // Get the text from the value element and extract both values
            String fullText = playerValueElement.getText();
            String[] lines = fullText.split("\\n");

            // Get the current value (first line)
            String playerValue = lines[0].trim();
            System.out.println("Player Value: " + playerValue);
            double currentValue = cleanDriverValue(playerValue);

            // Get the trend value (second line) if available
            double trendValue = 0.0;
            if (lines.length > 1 && !lines[1].trim().isEmpty()) {
                String trendText = lines[1].trim();
                System.out.println("Trend Text: " + trendText);
                trendValue = cleanDriverValue(trendText);

                // Apply sign based on selector class
                if (selectorClass.equals("si-down")) {
                    trendValue = -trendValue; // Make it negative for downward trend
                } else if (selectorClass.equals("false")) {
                    trendValue = 0.0; // Set to zero for flat trend
                }
                // si-up remains positive
            }

            System.out.println("Current Value: " + currentValue);
            System.out.println("Trend Value: " + trendValue);


            // Create a Piloto object with the extracted name and value1

            Piloto piloto = new Piloto(playerName);

            // Extract accordion data and populate the Piloto object
            Map<String, Integer> driverData = extractAccordionData();

            int lastIndex = driverData.size() - 1;

            // Add all data to the Piloto object
            for (Map.Entry<String, Integer> entry : driverData.entrySet()) {
                if(lastIndex == 0) {
                    piloto.addCircuitData(entry.getKey(), entry.getValue(), currentValue, trendValue);
                }else if(lastIndex == 1) {
                    piloto.addCircuitData(entry.getKey(), entry.getValue(), currentValue, trendValue);
                }else{
                    piloto.addData(entry.getKey(), entry.getValue());
                }
                lastIndex--;
            }

            // Close the popup
            closePopup();

            return piloto;

        } catch (Exception e) {
            System.out.println("Error processing element " + index + ": " + e.getMessage());
            e.printStackTrace();
            // Return null in case of error
            return null;
        }
    }

    /**
     * Extract data from accordion elements
     * @return Map with league/team names as keys and points as values
     */
    private Map<String, Integer> extractAccordionData() {
        Map<String, Integer> driverData = new LinkedHashMap<>(); // Changed to LinkedHashMap to maintain order
        
        try {
            // Wait for the container to be present
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(".si-performance__list")));

            System.out.println("Container found. Now searching for accordion elements...");

            // Try to find accordion elements by ID (accordion0, accordion1, etc.)
            int accordionIndex = 0;
            boolean foundAccordion = true;

            while (foundAccordion) {
                String accordionId = "accordion" + accordionIndex;
                try {
                    // Try to find this accordion element
                    WebElement accordion = driver.findElement(By.id(accordionId));
                    System.out.println("Found #" + accordionId);

                    // Extract league/team name and points
                    String[] leagueAndPoints = extractAccordionDetails(accordion);

                    if (leagueAndPoints != null && leagueAndPoints.length == 2) {
                        String league = leagueAndPoints[0];
                        String pointsString = leagueAndPoints[1];
                        
                        try {
                            // Parse the points string to an integer
                            Integer points = Integer.parseInt(pointsString);
                            driverData.put(league, points);
                            System.out.println("  Added to data: " + league + " = " + points + " points");
                        } catch (NumberFormatException e) {
                            System.out.println("Could not parse points value: " + pointsString);
                            // Still add to map but with a value of 0
                            driverData.put(league, 0);
                        }
                    }

                    // Increment to look for next accordion
                    accordionIndex++;

                } catch (NoSuchElementException e) {
                    // We've reached the end of the accordions
                    foundAccordion = false;
                    System.out.println("No more accordion elements found. Total found: " + accordionIndex);
                }
            }

            // Report the collected data
            System.out.println("Total data extracted: " + driverData.size() + " leagues/teams");

        } catch (Exception e) {
            System.out.println("An error occurred during accordion data extraction: " + e.getMessage());
            e.printStackTrace();
        }
        
        return driverData;
    }

    /**
     * Extract details from a single accordion element
     * @return String array with [league/team, points]
     */
    private String[] extractAccordionDetails(WebElement accordion) {
        try {
            String league = "Unknown";
            String pointsStr = "0";
            
            // Look for any h3 element in this accordion - it's likely the league/team name
            List<WebElement> h3Elements = accordion.findElements(By.tagName("h3"));
            if (!h3Elements.isEmpty()) {
                league = h3Elements.get(0).getText();
                System.out.println("  League/Team: " + league);
            } else {
                System.out.println("  League/Team: Unknown (no h3 elements found)");
            }

            // Look for the points element
            List<WebElement> pointsElements = accordion.findElements(By.className("si-totalPts__counts"));
            if (!pointsElements.isEmpty()) {
                // Get the full text of the points element
                String fullText = pointsElements.get(0).getText().trim();
                System.out.println("  Points (raw): " + fullText);
                
                // Remove " pts" from the end of the string
                if (fullText.endsWith(" pts")) {
                    pointsStr = fullText.substring(0, fullText.length() - 4).trim();
                } else {
                    pointsStr = fullText.trim();
                }
                
                System.out.println("  Points (cleaned): " + pointsStr);
            } else {
                System.out.println("  Points: Unknown (no si-totalPts__counts elements found)");
            }
        
        return new String[] { league, pointsStr };
        
    } catch (Exception e) {
        System.out.println("Error extracting accordion details: " + e.getMessage());
        return null;
    }
}

    /**
     * Close the popup window
     */
    private void closePopup() {
        try {
            // Find and click the close button
            WebElement closeButton = driver.findElement(By.cssSelector("#root > div > div.si-master__wrap > div > div > div.si-popup__close"));

            // Wait briefly for the button to be clickable
            wait.until(ExpectedConditions.elementToBeClickable(closeButton));

            System.out.println("Clicking close button to dismiss the popup...");
            closeButton.click();

            // Wait briefly for the popup to disappear
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                    By.cssSelector(".si-popup__body")));

            System.out.println("Popup closed successfully.");

        } catch (Exception e) {
            System.out.println("Error closing popup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Wait for the page to fully load
     */
    private void waitForPageToLoad() {
        try {
            // Wait for the page to be in a ready state
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").equals("complete"));
            
            // Additional wait to ensure everything is loaded
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread was interrupted while waiting for page to load");
        }
    }
    
    /**
     * Close the WebDriver
     */
    public void close() {
        if (driver != null) {
            driver.quit();
        }
    }

    /**
     * Extract the driver value from the popup
     * @return the driver value as a double
     */
    private double cleanDriverValue(String valueText) {
        double value = 0.0;
        
        try {
            // Remove $ and M
            if (valueText.startsWith("$") && valueText.endsWith("M")) {
                valueText = valueText.substring(1, valueText.length() - 1);
                try {
                    value = Double.parseDouble(valueText);
                    System.out.println("Parsed driver value: $" + value + "M");
                } catch (NumberFormatException e) {
                    System.out.println("Could not parse driver value: " + valueText);
                }
            }
        } catch (Exception e) {
            System.out.println("Error extracting driver value: " + e.getMessage());
        }
        
        return value;
    }

    /**
     * Updates existing pilots with new circuit data if available
     * @param existingPilotos List of existing pilots to update
     * @return The updated list of pilots
     */
    public List<Piloto> updatePilotCircuits(List<Piloto> existingPilotos) {
        if (existingPilotos == null || existingPilotos.isEmpty()) {
            System.out.println("No existing pilots to update");
            return new ArrayList<>();
        }

        System.out.println("Updating " + existingPilotos.size() + " pilots with new circuit data...");

        try {
            // Navigate to the website
            driver.get("https://fantasy.formula1.com/en/statistics/details?tab=driver&filter=fAvg");

            // Wait for the page to fully load
            waitForPageToLoad();

            // Find the parent container
            WebElement container = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("#root > div > div.si-master__wrap > section.si-stats__wrap > div:nth-child(2) > div > div > div > div.si-stats__tracker-grid-rhs > div > div")));

            // Find all LI elements inside the UL
            WebElement ul = container.findElement(By.tagName("ul"));
            List<WebElement> liElements = ul.findElements(By.tagName("li"));

            System.out.println("Found " + liElements.size() + " drivers on the website");

            // Create a map of existing pilots by name for quick lookup
            Map<String, Piloto> pilotMap = new HashMap<>();
            for (Piloto piloto : existingPilotos) {
                pilotMap.put(piloto.getName(), piloto);
            }

            // Process each driver in the list
            for (int i = 1; i < liElements.size(); i++) {
                WebElement element = liElements.get(i);

                // Click the driver element
                if(i%4 == 0) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
                    Thread.sleep(250);
                }
                element.click();
                // Wait for the popup to appear
                wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("#root > div > div.si-master__wrap > div > div > div.si-popup__body > div.si-driCon__playerInfo > div.si-player__name")));

                // Get the driver name
                WebElement playerNameElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("#root > div > div.si-master__wrap > div > div > div.si-popup__body > div.si-driCon__playerInfo > div.si-player__name")));
                String playerName = playerNameElement.getText();

                // Get the team name if available
                String teamName = "";
                try {
                    // Try to find team name in the popup
                    WebElement teamElement = driver.findElement(By.cssSelector(".si-player__team"));
                    if (teamElement != null) {
                        teamName = teamElement.getText().trim();
                    }
                } catch (Exception e) {
                    // Team name not found in popup, try the list item
                    try {
                        WebElement teamElement = element.findElement(By.cssSelector("div.si-stats__list-item.teamname"));
                        if (teamElement != null) {
                            teamName = teamElement.getText().trim();
                        }
                    } catch (Exception ex) {
                        // Continue without team name
                    }
                }

                // Combine player name and team
                String fullName = playerName;
                if (!teamName.isEmpty()) {
                    fullName = playerName + " " + teamName;
                }

                System.out.println("Processing driver: " + fullName);

                // Check if this driver exists in our list
                Piloto existingPilot = pilotMap.get(fullName);
                if (existingPilot != null) {
                    System.out.println("Found existing pilot: " + fullName);

                    // Get current value and trend
                    double currentValue = 0;
                    double trendValue = 0;

                    // Extract value and trend
                    WebElement playerValueElement;
                    WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofMillis(11));
                    String selectorClass = "";

                    try {
                        // Try with .si-up first
                        playerValueElement = shortWait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector("#root > div > div.si-master__wrap > div > div > div.si-popup__body > div.si-driCon__playerInfo > div.si-player__trends.si-up")));
                        selectorClass = "si-up";
                    } catch (TimeoutException e) {
                        try {
                            // If .si-up not found, try with .si-down
                            playerValueElement = shortWait.until(ExpectedConditions.visibilityOfElementLocated(
                                    By.cssSelector("#root > div > div.si-master__wrap > div > div > div.si-popup__body > div.si-driCon__playerInfo > div.si-player__trends.si-down")));
                            selectorClass = "si-down";
                        } catch (TimeoutException e2) {
                            // If both failed, try with .false
                            playerValueElement = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                    By.cssSelector("#root > div > div.si-master__wrap > div > div > div.si-popup__body > div.si-driCon__playerInfo > div.si-player__trends.false")));
                            selectorClass = "false";
                        }
                    }

                    // Get value and trend from text
                    String fullText = playerValueElement.getText();
                    String[] lines = fullText.split("\\n");

                    // Get current value
                    if (lines.length > 0) {
                        String playerValue = lines[0].trim();
                        currentValue = cleanDriverValue(playerValue);
                    }

                    existingPilot.setValue( currentValue);

                    // Get trend value
                    if (lines.length > 1 && !lines[1].trim().isEmpty()) {
                        String trendText = lines[1].trim();
                        trendValue = cleanDriverValue(trendText);

                        // Apply sign based on selector class
                        if (selectorClass.equals("si-down")) {
                            trendValue = -trendValue;
                        } else if (selectorClass.equals("false")) {
                            trendValue = 0.0;
                        }
                    }

                    // Extract accordion data
                    Map<String, Integer> circuitData = extractAccordionData();

                    // Check for new circuits and add them
                    boolean foundNewCircuit = false;
                    
                    // Get the existing data map from the pilot
                    Map<String, String> existingData = existingPilot.getData();

                    int circuitDataSize = circuitData.size() - 1;

                    for (Map.Entry<String, Integer> entry : circuitData.entrySet()) {
                        String circuitName = entry.getKey();
                        // Check if the circuit name exists as a key in the data map
                        if (!existingData.containsKey(circuitName)) {
                            if(!(circuitDataSize == 0)) {
                                System.out.println("Adding new circuit for " + fullName + ": " + circuitName);
                                existingPilot.addCircuitData(circuitName, entry.getValue(), currentValue, trendValue);
                                foundNewCircuit = true;
                            }
                        } else {
                            System.out.println("Circuit " + circuitName + " already exists for " + fullName);
                        }
                        circuitDataSize--;
                    }

                    if (!foundNewCircuit) {
                        System.out.println("No new circuits found for " + fullName);
                    }
                } else {
                    System.out.println("Driver not in existing list: " + fullName);
                }

                // Close the popup
                closePopup();
            }

            System.out.println("Finished checking for updates");

        } catch (Exception e) {
            System.err.println("Error updating pilots: " + e.getMessage());
            e.printStackTrace();
        }

        return existingPilotos;
    }


}