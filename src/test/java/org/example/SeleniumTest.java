package org.example;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

public class SeleniumTest {

    @Test
    public void testSelenium() throws InterruptedException {
        String url = "https://spb.cian.ru/sale/flat/254658549/";
        WebDriver driver = getWebDriver(url);

        if (driver.findElement(By.cssSelector("div[data-name=TourModal]")).isDisplayed()) {
            List<WebElement> elements = driver.findElements(By.cssSelector("div[data-name=TourModal"));
            for (WebElement element : elements) {
                WebElement button = element.findElement(By.tagName("button"));
                button.click();
                break;
            }
        }
        Thread.sleep(1000);
        acceptCookies(driver);
        driver.findElement(By.cssSelector("div[data-name=CommentsButton]")).click();
        String announceMark = driver.findElement(By.cssSelector("div[data-name=OfferValueAddedServices]")).getText();
        String id = getId(url);
        String xpathString = "//div[contains(@class, 'comment')]";
        List<WebElement> elements = driver.findElements(By.xpath(xpathString));
        WebElement commentElement = elements.get(1);
        System.out.println(commentElement.getAttribute("innerHTML"));
        WebElement textarea = commentElement.findElement(By.tagName("textarea"));
        Thread.sleep(1000);
        String price = driver.findElement(By.cssSelector("span[itemprop=price")).getText();
        textarea.sendKeys(id + " - " + price);
        List<WebElement> buttons = driver.findElements(By.tagName("button"));
        for (WebElement button : buttons) {
            if (button.getText().equalsIgnoreCase("Сохранить")) {
                button.click();
                break;
            }
        }
    }

    @Test
    public void testSeleniumWithout3Dtour() throws InterruptedException {
        String url = "https://spb.cian.ru/sale/flat/253301080/";
        WebDriver driver = getWebDriver(url);

        try {
            if (driver.findElement(By.cssSelector("div[data-name=TourModal]")).isDisplayed()) {
                List<WebElement> elements = driver.findElements(By.cssSelector("div[data-name=TourModal"));
                for (WebElement element : elements) {
                    WebElement button = element.findElement(By.tagName("button"));
                    System.out.println(button.getAttribute("innerHTML"));
                    button.click();
                    break;
                }
            }
        } catch (Exception ex) {

        }

    }

    @Test
    public void testAgent() {
        String url1 = "https://spb.cian.ru/sale/flat/257561677/";
        String url2 = "https://spb.cian.ru/sale/flat/259452892/";
        List<String> urls = List.of(url1, url2);
        for (String url : urls) {
            WebDriver driver = getWebDriver(url);
            List<WebElement> authorElement = driver.findElements(By.cssSelector("div[data-name=AuthorAsideBrand]"));
            String author = "";
            for (WebElement element : authorElement) {
                author = element.findElement(By.tagName("h2")).getText();
            }
            System.out.println(author);
        }
    }

    @Test
    public void testSubstring() {

        String link = "https://spb.cian.ru/sale/flat/253301080/";
        String regex = "/\\d+/";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(link);
        String id = "";
        while (matcher.find()) {
            id = link.substring(matcher.start() + 1, matcher.end() - 1);
        }

        assertEquals("253301080", id);

    }

    private WebDriver getWebDriver(String url) {
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().window().maximize();
        driver.get(url);
        return driver;
    }

    private String getId(String link) {
        String regex = "/\\d+/";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(link);
        String id = "";
        while (matcher.find()) {
            id = link.substring(matcher.start() + 1, matcher.end() - 1);
        }
        return id;
    }

    private void acceptCookies(WebDriver driver) {
        WebElement cookieBar = driver.findElement(By.cssSelector("div[data-name=CookieAgreementBar]"));
        cookieBar.findElement(By.tagName("button")).click();
    }
}
