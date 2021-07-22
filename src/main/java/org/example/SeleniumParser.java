package org.example;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.java.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

@Log
public class SeleniumParser {

    private final String login;
    private final String password;

    private final HashMap<String, AdditionalInformation> authorForApartment = new HashMap<>();

    public SeleniumParser(String login, String password) {
        this.login = login;
        this.password = password;
    }

    //test account
//    private final String login = "xohoke9968@godpeed.com";
//    private final String password = "Qwerty123456";

    /**
     * Main method of class, which contains all methods.
     *
     * @param url where web chrome driver should start from
     * @return List of apartments parsed and filled with required info
     * @throws InterruptedException technical exception
     */
    public List<Apartment> getApartments(String url) throws InterruptedException {
        List<String> links = new ArrayList<>();
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
        ChromeOptions op = new ChromeOptions();
        op.setExperimentalOption("excludeSwitches", "disable-popup-blocking");

        // set up webdriver
        WebDriver driver = getWebDriver(url);
        log.info("driver set up successfully");

        //check if there any pop ups and close it
        checkIfAnyIframes(driver);

        //accept cookies
        acceptCookies(driver);
        Thread.sleep(200);

        // check is user is logged in or not and log in if not
        logIn(driver);

        List<WebElement> cardComponentDivs = driver.findElements(By.cssSelector("article[data-name=CardComponent]"));
        List<Apartment> apartmentsList = iterateDivsAndParseApartments(cardComponentDivs, links);

        // add apartment to favourites and add comment
        openLinksAndAddToFavoriteAndAddComments(links, driver);

        // extract info from map and add to apartment from list (find apartment by id)
        extractInfoFromMap(apartmentsList);

        driver.close();
        return apartmentsList;
    }

    /**
     * Method which opens links and add apartments to favorite and leave some comments
     *
     * @param links  stands for list of links which should be parsed
     * @param driver our chrome web driver
     * @throws InterruptedException technical exception
     */
    private void openLinksAndAddToFavoriteAndAddComments(List<String> links, WebDriver driver) throws InterruptedException {
        for (String link : links) {
            driver.get(link);
            checkIfThereA3DTourOnThePageAndClosePopUp(driver);
            Thread.sleep(1000);
            String id = getId(link);
            String announceMark = driver.findElement(By.cssSelector("div[data-name=OfferValueAddedServices]")).getText();
            String author = extractAuthor(driver);
            addComment(driver, id);
            authorForApartment.put(id, new AdditionalInformation(author, announceMark));
        }
    }

    /**
     * internal method for adding a comment
     *
     * @param driver our chrome web driver
     * @param id     stands for apartment id
     * @throws InterruptedException technical exception
     */
    private void addComment(WebDriver driver, String id) throws InterruptedException {
        driver.findElement(By.cssSelector("div[data-name=CommentsButton]")).click();
        String xpathString = "//div[contains(@class, 'comment')]";
        List<WebElement> elements = driver.findElements(By.xpath(xpathString));
        WebElement commentElement = elements.get(1);
        WebElement textarea = commentElement.findElement(By.tagName("textarea"));
        Thread.sleep(1000);
        String price = driver.findElement(By.cssSelector("span[itemprop=price")).getText();
        textarea.clear();

        String comment = id + " - " + price + " " + LocalDate.now();
        textarea.sendKeys(comment);
        List<WebElement> buttons = driver.findElements(By.tagName("button"));
        for (WebElement button : buttons) {
            if (button.getText().equalsIgnoreCase("Сохранить")) {
                button.click();
                break;
            }
        }
        log.info(String.format("comment added and saved: %s", comment));
    }

    /**
     * internal method which extracts author of announce
     *
     * @param driver our chrome web driver
     * @return author of announce
     */
    private String extractAuthor(WebDriver driver) {
        String author = "";
        List<WebElement> authorElement = driver.findElements(By.cssSelector("div[data-name=AuthorAsideBrand]"));
        for (WebElement element : authorElement) {
            author = element.findElement(By.tagName("h2")).getText();
            break;
        }
        return author;
    }

    /**
     * method which checks whether page has 3d tour and if does -> close pop up with it
     *
     * @param driver our chrome web driver
     */
    private void checkIfThereA3DTourOnThePageAndClosePopUp(WebDriver driver) {
        if (driver.findElements(By.cssSelector("div[data-name=TourModal]")).size() > 0) {
            List<WebElement> elements = driver.findElements(By.cssSelector("div[data-name=TourModal"));
            for (WebElement element : elements) {
                WebElement button = element.findElement(By.tagName("button"));
                button.click();
                break;
            }
        }
    }

    /**
     * method for accepting cookies
     *
     * @param driver our chrome web driver
     */
    private void acceptCookies(WebDriver driver) {
        WebElement cookieBar = driver.findElement(By.cssSelector("div[data-name=CookieAgreementBar]"));
        cookieBar.findElement(By.tagName("button")).click();
    }

    /**
     * General method which parses page and collect information about apartments
     *
     * @param divs  list of divs should be iterated
     * @param links list of links should be filled with announce links
     * @throws InterruptedException technical exception
     */
    private List<Apartment> iterateDivsAndParseApartments(List<WebElement> divs, List<String> links) throws InterruptedException {
        List<Apartment> apartmentsList = new ArrayList<>();

        for (WebElement div : divs) {
            String textPrice = div.findElement(By.cssSelector("span[data-mark=MainPrice]")).getText();
            WebElement areaElement = div.findElement(By.cssSelector("div[data-name=LinkArea]"));
            List<WebElement> addresses = areaElement.findElements(By.cssSelector("a[data-name=GeoLabel]"));
            StringBuilder address = new StringBuilder();
            for (WebElement singleAddress : addresses) {
                address.append(singleAddress.getText()).append(" ");
            }
            String titleComponentSpan = areaElement.findElement(By.cssSelector("div[data-name=TitleComponent]")).getText();
            titleComponentSpan = getNormalString(titleComponentSpan);
            int amountOfRooms = getAmountOfRooms(titleComponentSpan);
            String flour = getFlour(titleComponentSpan);
            double sqr = getSqr(titleComponentSpan);
            long price = getPrice(textPrice);
            String link = div.findElement(By.cssSelector("a")).getAttribute("href");
            String id = getId(link);
            links.add(link);
            WebElement button = div.findElement(By.cssSelector("button[data-mark=PhoneButton]"));
            button.click();
            Thread.sleep(10);
            String phoneNumber = div.findElement(By.cssSelector("span[data-mark=PhoneValue]")).getText();
            Apartment apartment = new Apartment(
                    id, amountOfRooms, sqr, flour, address.toString(), price, link, phoneNumber
            );
            apartmentsList.add(apartment);
            log.info(String.format("apartment added: %s ", apartment));
        }
        return apartmentsList;
    }

    /**
     * method parses page and checks whether any pop ups which interrupts normal working of parser
     *
     * @param driver our chrome web driver
     */
    private void checkIfAnyIframes(WebDriver driver) {
        try {
            if (driver.findElements(By.tagName("iframe")).size() > 0) {
                for (WebElement iframe : driver.findElements(By.tagName("iframe"))) {
                    iframe.findElement(By.tagName("button")).click();
                    break;
                }
            }
        } catch (Exception ignored) {

        }
    }

    /**
     * method extracts authors and special marks from map and add this information to apartment
     *
     * @param apartments is the list of apartments filled before this method
     */
    private void extractInfoFromMap(List<Apartment> apartments) {

        authorForApartment.forEach((id, additionalInformation) -> {
            for (Apartment apartment : apartments) {
                if (apartment.getId().equals(id)) {
                    apartment.setAuthor(additionalInformation.getAuthor());
                    apartment.setMark(additionalInformation.getMark());
                }
            }
        });

    }

    /**
     * set up webdriver
     *
     * @param url link where driver should start from
     * @return Webdriver instance
     */
    private WebDriver getWebDriver(String url) {
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().window().maximize();
        driver.get(url);
        return driver;
    }

    /**
     * login method
     *
     * @param driver chrome web driver
     * @throws InterruptedException technical exception
     */
    private void logIn(WebDriver driver) throws InterruptedException {
        if (isLogInPossible(driver)) {
            driver.findElement(By.cssSelector("a[id=login-btn]")).click();
            Thread.sleep(500);
            if (driver.findElements(By.cssSelector("button[data-mark=SwitchToEmailAuth]")).size() > 0) {
                driver.findElement(By.cssSelector("button[data-mark=SwitchToEmailAuth]")).click();
                Thread.sleep(500);
            }
            driver.findElement(By.cssSelector("input[name=username]")).sendKeys(login);
            Thread.sleep(500);
            driver.findElement(By.cssSelector("button[data-mark=ContinueAuthBtn]")).click();
            Thread.sleep(500);
            driver.findElement(By.cssSelector("input[name=password]")).sendKeys(password);
            Thread.sleep(500);
            driver.findElement(By.cssSelector("button[data-mark=ContinueAuthBtn]")).click();
            Thread.sleep(500);
            log.info("logged in successfully");
        }
    }

    /**
     * extract id from link
     *
     * @param link link of announce
     * @return id
     */
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

    /**
     * checks if user already logged in
     *
     * @param driver webdriver
     * @return true or false
     */
    private boolean isLogInPossible(WebDriver driver) {
        return driver.findElements(By.cssSelector("a[id=login-btn]")).size() > 0;
    }

    /**
     * normalize the announce title and make it the same for all of the announcements
     *
     * @param sourceString is the string we should normilize
     * @return String
     */
    private String getNormalString(String sourceString) {
        Pattern pattern = Pattern.compile("(\\d-комн.\\s)(кв\\.)?(апарт\\.)?");
        Matcher matcher = pattern.matcher(sourceString);
        String result = "";
        while (matcher.find()) {
            result = sourceString.substring(matcher.start());
        }
        return result;
    }

    /**
     * extracts amount of rooms from title
     *
     * @param sourceString source string
     * @return int
     */
    private int getAmountOfRooms(String sourceString) {
        char c = sourceString.charAt(0);
        return Character.getNumericValue(c);
    }

    /**
     * extracts apartment square
     *
     * @param sourceString source string
     * @return double
     */
    private double getSqr(String sourceString) {
        String[] split = sourceString.split("\\s");
        String sqr = split[2].replace(",", ".");
        return Double.parseDouble(sqr);
    }

    /**
     * extracts current and total flour of apartment (type 1/10)
     *
     * @param sourceString source string
     * @return String
     */
    private String getFlour(String sourceString) {
        String str1 = "м²";
        String substring = sourceString.substring(sourceString.indexOf(str1) + str1.length(), sourceString.indexOf("этаж"));
        substring = substring.replaceAll(",", "").trim();
        return substring;
    }

    /**
     * extracts price from source string
     *
     * @param sourceString source string
     * @return long
     */
    private long getPrice(String sourceString) {
        sourceString = sourceString.replaceAll("\\s", "");
        sourceString = sourceString.replace("₽", "");
        return Long.parseLong(sourceString);
    }
}
