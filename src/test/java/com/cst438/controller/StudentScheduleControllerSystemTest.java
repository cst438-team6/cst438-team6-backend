package com.cst438.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StudentScheduleControllerSystemTest {

    public static final String CHROME_DRIVER_FILE_LOCATION =
            //"C:/chromedriver_win32/chromedriver.exe";
            "C:/chromedriver-win64/chromedriver.exe";

    //public static final String CHROME_DRIVER_FILE_LOCATION =
    //        "~/chromedriver_macOS/chromedriver";
    public static final String URL = "http://localhost:3000";

    public static final int SLEEP_DURATION = 1000; // 1 second.


    // add selenium dependency to pom.xml

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
    public void systemTestEnrollInSection() throws Exception {
        // enroll in a section
        // verify the section shows up
        // drop the section
        // verify the section no longer exists


        //remove any enrollment for "cst338"
        WebElement we = driver.findElement(By.linkText("VIew Class Schedule"));
        we.click();
        Thread.sleep(SLEEP_DURATION);
        driver.findElement(By.id("syear")).sendKeys("2025");
        //  year: 2025,
        driver.findElement(By.id("ssemester")).sendKeys("Spring");
        //  semester: Spring,
        driver.findElement(By.id("search")).click();
        // click search
        Thread.sleep(SLEEP_DURATION);
        try {
            while (true) {
                WebElement existingRow = driver.findElement(By.xpath("//tr[td='cst338']"));
                List<WebElement> buttons = existingRow.findElements(By.tagName("button"));
                // drop is the first button
                assertEquals(1, buttons.size());
                buttons.get(1).click();
                Thread.sleep(SLEEP_DURATION);
                // find the YES to confirm button
                List<WebElement> confirmButtons = driver
                        .findElement(By.className("react-confirm-alert-button-group"))
                        .findElements(By.tagName("button"));
                assertEquals(2, confirmButtons.size());
                confirmButtons.get(0).click();
                Thread.sleep(SLEEP_DURATION);
            }
        } catch (NoSuchElementException e) {
            // do nothing, continue with test
        }

        // click link to navigate to enroll
        we = driver.findElement(By.linkText("Enroll in a class"));
        we.click();
        Thread.sleep(SLEEP_DURATION);

        WebElement row1 = driver.findElement(By.xpath("//tr[td='cst338']"));
        List<WebElement> buttons = row1.findElements(By.tagName("button"));
        // add is the first button
        assertEquals(1, buttons.size());
        buttons.get(0).click();
        Thread.sleep(SLEEP_DURATION);
        // find the YES to confirm button
        List<WebElement> confirmButtons = driver
                .findElement(By.className("react-confirm-alert-button-group"))
                .findElements(By.tagName("button"));
        assertEquals(2, confirmButtons.size());
        confirmButtons.get(0).click();
        Thread.sleep(SLEEP_DURATION);

        // verify that new enrollment shows up in the schedule
        // find the row for cst338
        we = driver.findElement(By.linkText("VIew Class Schedule"));
        we.click();
        Thread.sleep(SLEEP_DURATION);
        driver.findElement(By.id("syear")).sendKeys("2025");
        //  year: 2025,
        driver.findElement(By.id("ssemester")).sendKeys("Spring");
        //  semester: Spring,
        driver.findElement(By.id("search")).click();
        // click search
        Thread.sleep(SLEEP_DURATION);
        WebElement rowTest = driver.findElement(By.xpath("//tr[td='cst338']"));

        // drop is the first button
        buttons = rowTest.findElements(By.tagName("button"));
        // add is the first button
        assertEquals(1, buttons.size());
        buttons.get(0).click();
        Thread.sleep(SLEEP_DURATION);
        // find the YES to confirm button
        confirmButtons = driver
                .findElement(By.className("react-confirm-alert-button-group"))
                .findElements(By.tagName("button"));
        assertEquals(2, confirmButtons.size());
        confirmButtons.get(0).click();
        Thread.sleep(SLEEP_DURATION);

        // verify the schedule no longer contains cst338
        assertThrows(NoSuchElementException.class, () ->
                driver.findElement(By.xpath("//tr[td='cst338']")));
    }
}
