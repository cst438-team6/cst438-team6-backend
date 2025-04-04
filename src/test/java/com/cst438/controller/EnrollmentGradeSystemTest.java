package com.cst438.controller;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnrollmentGradeSystemTest {
    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setup() {
        System.setProperty("webdriver.chrome.driver", "/Users/m/Downloads/chromedriver-mac-x64/chromedriver");
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get("http://localhost:3000");
    }

    @Test
    public void testInstructorEntersFinalGrades() {
        // Starting at instructor home page
        // Enter year, semester, and click sections
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("toSections")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("secNoHeader")));

        // Locate the row that contains the secId value of 8
        List<WebElement> sections = driver.findElements(By.xpath("//tbody/tr"));
        for (WebElement section : sections) {
            String secIdText = section.findElement(By.id("secNo")).getText();
            if ("8".equals(secIdText)) {
                WebElement viewAssignmentsButton = section.findElement(By.id("viewEnrollmentsButton"));
                viewAssignmentsButton.click();
                break;
            }
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("studentId")));

        // Table of enrollments for section 8
        List<WebElement> gradeInputs = driver.findElements(By.cssSelector("input[type='text']"));

        for (WebElement gradeInput : gradeInputs) {
            gradeInput.sendKeys(Keys.chord(Keys.COMMAND, "a", Keys.DELETE)); // Clear existing value
            gradeInput.sendKeys("A"); // Enter new grade
        }

        driver.findElement(By.id("saveGrades")).click();

        // Wait for the alert and handle it
        wait.until(ExpectedConditions.alertIsPresent());
        String alertText = driver.switchTo().alert().getText();
        driver.switchTo().alert().accept();

        assertEquals("Grades updated successfully!", alertText);

        // Call method to verify grades
        verifyGrades();
    }

    private void verifyGrades() {
        // Navigate back to verify the grades
        driver.get("http://localhost:3000");
        driver.findElement(By.id("year")).sendKeys("2025");
        driver.findElement(By.id("semester")).sendKeys("Spring");
        driver.findElement(By.id("toSections")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("secNoHeader")));

        // Locate the section with secId value of 8 again
        List<WebElement> sections = driver.findElements(By.xpath("//tbody/tr"));
        for (WebElement section : sections) {
            String secIdText = section.findElement(By.id("secNo")).getText();
            if ("8".equals(secIdText)) {
                WebElement viewEnrollmentsButton = section.findElement(By.id("viewEnrollmentsButton"));
                viewEnrollmentsButton.click();
                break;
            }
        }

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("studentId")));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='text']")));
        List<WebElement> gradeInputs = driver.findElements(By.cssSelector("input[type='text']"));
        for (WebElement gradeInput : gradeInputs) {
            String currentGrade = gradeInput.getAttribute("value");
            assertEquals("A", currentGrade);
        }
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit(); // Quit the driver
        }
    }
}