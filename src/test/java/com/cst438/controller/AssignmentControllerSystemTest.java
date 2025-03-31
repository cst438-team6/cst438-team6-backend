package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AssignmentControllerSystemTest {

    public static final String CHROME_DRIVER_FILE_LOCATION =
            //"C:/chromedriver_win32/chromedriver.exe";
            "C:/chromedriver-win64/chromedriver.exe";

    //public static final String CHROME_DRIVER_FILE_LOCATION =
    //        "~/chromedriver_macOS/chromedriver";
    public static final String URL = "http://localhost:3000";

    public static final int SLEEP_DURATION = 1000; // 1 second.


    // add selenium dependency to pom.xml

    // these tests assumes that test data does NOT contain any
    // sections for course cst499 in 2024 Spring term.

    WebDriver driver;

    @BeforeEach
    public void setUpDriver() throws Exception {

        // set properties required by Chrome Driver
        System.setProperty(
                "webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
        ChromeOptions ops = new ChromeOptions();
        ops.addArguments("--remote-allow-origins=*");

        // start the driver
        driver = new ChromeDriver(ops);
//        driver.findElement(By.id("section-title")).click();
//        driver.findElement(By.id("section-title")).sendKeys("Section Title");
        driver.get(URL);
        // must have a short wait to allow time for the page to download
        Thread.sleep(SLEEP_DURATION);

    }

    @AfterEach
    public void terminateDriver() {
        if (driver != null) {
            // quit driver
            driver.close();
            driver.quit();
            driver = null;
        }
    }

    @Test
    public void systemTestAddAssignment() throws Exception {
        // add an assignment
        // verify the assignment shows up
        // delete the assignment
        // verify the assignment no longer exists

        // enter 2024, Spring and click sections
        driver.findElement(By.id("year")).sendKeys("2024");
        driver.findElement(By.id("semester")).sendKeys("Spring");

        // click link to navigate to assignments
        WebElement we = driver.findElement(By.linkText("Show Sections"));
        we.click();
        Thread.sleep(SLEEP_DURATION);

        // find and click the link to view assignments for cst438
        Thread.sleep(SLEEP_DURATION);
        WebElement row1 = driver.findElement(By.xpath("//tr[td='cst438']"));
        row1.findElements(By.linkText("View Assignments")).get(0).click();
        Thread.sleep(SLEEP_DURATION);

        //remove any assignment "TestAssignment"
        try {
            while (true) {
                WebElement existingRow = driver.findElement(By.xpath("//tr[td='TestAssignment']"));
                List<WebElement> buttons = existingRow.findElements(By.tagName("button"));
                // delete is the second button
                assertEquals(3, buttons.size());
                buttons.get(1).click();
                Thread.sleep(SLEEP_DURATION);
            }
        } catch (NoSuchElementException e) {
            // do nothing, continue with test
        }

        // add a new assignment "TestAssignment"
        driver.findElement(By.id("addAssignmentBtn")).click();
        // enter data
        //  title: TestAssignment,
        driver.findElement(By.id("eTitle")).sendKeys("TestAssignment");
        //  dueDate: 2025-03-30,
        driver.findElement(By.id("eDueDate")).sendKeys("03302025");
        // click Save
        driver.findElement(By.id("save")).click();
        Thread.sleep(SLEEP_DURATION);

        // verify that new assignment shows up on the list
        // find the row for "TestAssignment"
        WebElement rowTest = driver.findElement(By.xpath("//tr[td='TestAssignment']"));
        List<WebElement> buttons = rowTest.findElements(By.tagName("button"));
        // delete is the second button
        assertEquals(3, buttons.size());
        buttons.get(1).click();
        Thread.sleep(SLEEP_DURATION);

        // verify that TestAssignment no longer exists
        assertThrows(NoSuchElementException.class, () ->
                driver.findElement(By.xpath("//tr[td='TestAssignment']")));
    }
}
