package com.cst438.controller;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssignmentGradeSystemTest {
    private WebDriver driver; // WebDriver instance to control the browser
    private WebDriverWait wait; // WebDriverWait instance for waiting conditions

    @BeforeEach
    public void setup() {
        // Set the path for the ChromeDriver executable
        System.setProperty("webdriver.chrome.driver", "/Users/m/Downloads/chromedriver-mac-x64/chromedriver");

        // Initialize the ChromeDriver and WebDriverWait
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Navigate to the application URL
        driver.get("http://localhost:3000");
    }

    @Test
    public void testInstructorGradesAssignment() {
        // Enter the year and semester, then navigate to sections
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("toSections")).click();

        // Wait until the sections header is visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("secNoHeader")));

        // Locate all the sections displayed in the table
        List<WebElement> sections = driver.findElements(By.xpath("//tbody/tr"));
        for (WebElement section : sections) {
            // Get the section ID text
            String secIdText = section.findElement(By.id("secNo")).getText();
            if ("8".equals(secIdText)) { // Check for the specific section with ID 8
                // Click on the view assignments button for this section
                WebElement viewAssignmentsButton = section.findElement(By.id("viewAssignmentsButton"));
                clickElement(viewAssignmentsButton); // Use the custom click method
                break; // Exit the loop once the correct section is found
            }
        }

        // Wait until the assignment ID is visible, indicating we've navigated successfully
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("assignmentID")));

        // Locate all the buttons for entering grades
        List<WebElement> enterGradesButtons = driver.findElements(By.id("enterGradesButton"));

        // Iterate through each enter grades button
        for (WebElement enterGradesButton : enterGradesButtons) {
            // Click the button to enter grades
            clickElement(enterGradesButton); // Use the custom click method
            // Wait until the grading dialog is visible
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("MuiDialog-root")));

            // Retrieve all input fields in the dialog for grades entry
            List<WebElement> gradeInputs = driver.findElements(By.xpath("//input"));
            assertEquals(1, gradeInputs.size()); // Assert that there is exactly one grade input field

            for (WebElement gradeInput : gradeInputs) {
                // Clear any existing value in the input field
                gradeInput.sendKeys(Keys.chord(Keys.COMMAND, "a", Keys.DELETE));
                // Enter the new grade value of 90
                gradeInput.sendKeys("91");
            }

            // Click the save button to save the grade
            WebElement saveButton = driver.findElement(By.id("save"));
            clickElement(saveButton); // Using the custom click method

            // Wait for the alert indicating successful update to be present
            wait.until(ExpectedConditions.alertIsPresent());
            String alertText = driver.switchTo().alert().getText(); // Get alert text
            driver.switchTo().alert().accept(); // Close the alert
            // Assert that the alert text confirming update success is displayed
            assertEquals("Grades updated successfully!", alertText);
            // Wait until the enter grades button is again visible for the next iteration
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("enterGradesButton")));
        }

        // Call the verifyGrade method to check the grades after they are saved
        verifyGrade();
    }

    private void verifyGrade() {
        // Navigate back to the main page to verify the grades
        driver.get("http://localhost:3000");

        // Enter the year and semester again to re-navigate to sections
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("toSections")).click();

        // Wait until the sections header is visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("secNoHeader")));

        // Locate the section with ID 8 to view assignments again
        List<WebElement> sections = driver.findElements(By.xpath("//tbody/tr"));
        for (WebElement section : sections) {
            String secIdText = section.findElement(By.id("secNo")).getText();
            if ("8".equals(secIdText)) {
                // Click the view assignments button to check grades
                WebElement viewAssignmentsButton = section.findElement(By.id("viewAssignmentsButton"));
                clickElement(viewAssignmentsButton); // Use the custom click method
                break; // Exit the loop once the correct section is found
            }
        }

        // Wait until the assignment ID is visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("assignmentID")));

        // Locate all the buttons for entering grades
        List<WebElement> enterGradesButtons = driver.findElements(By.id("enterGradesButton"));

        // Iterate through each enter grades button
        for (WebElement enterGradesButton : enterGradesButtons) {
            // Click the button to enter grades
            clickElement(enterGradesButton); // Use the custom click method
            // Wait until the grading dialog is visible
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("MuiDialog-root")));

            // Retrieve all input fields in the dialog for grades entry
            List<WebElement> gradeInputs = driver.findElements(By.xpath("//input"));

            for (WebElement gradeInput : gradeInputs) {
                // Get the current value of the grade input
                String currentGrade = gradeInput.getAttribute("value");
                // Assert that the grade is now set to 90
                assertEquals("91", currentGrade);
            }
        }
    }

    private void clickElement(WebElement element) {
        // Scroll the element into view and click it
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        try {
            element.click(); // Try to click the element
        } catch (Exception e) {
            // If the click fails, retry using JavaScript
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    @AfterEach
    public void teardown() {
        // Quit the WebDriver session and close the browser
        if (driver != null) {
            driver.quit();
        }
    }
}