package com.project.test;

import io.github.bonigarcia.wdm.WebDriverManager;

import org.junit.jupiter.api.Test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import org.openqa.selenium.chrome.ChromeDriver;

public class LoginTest {

    @Test
    public void loginTest() throws InterruptedException {

        // setup chromedriver
        WebDriverManager.chromedriver().setup();

        // mở chrome
        WebDriver driver = new ChromeDriver();

        // maximize
        driver.manage().window().maximize();

        // mở trang login
        driver.get("https://outshine-deviancy-uncloak.ngrok-free.dev/user-login");

        // chờ 2 giây
        Thread.sleep(2000);

        // nhập email
        WebElement email = driver.findElement(By.name("email"));

        email.sendKeys("admin@gmail.com");

        // nhập password
        WebElement password = driver.findElement(By.name("password"));

        password.sendKeys("123456");

        // chờ nhìn thao tác
        Thread.sleep(1000);

        // click đăng nhập
        driver.findElement(
                By.cssSelector("button[type='submit']")).click();

        // chờ xem kết quả
        Thread.sleep(5000);

        // đóng browser
        driver.quit();
    }
}