package org.example.schedueserver.Crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;

public class WITSeleniumCrawler {

    private static final String STUDENTID = "2407010812";
    private static final String PASSWORD = "Jinitaimei123@";

    private static final String LOGIN_URL = "http://jwxt.wit.edu.cn.jsxsd";
    private static final String COURSE_TABLE_URL = "http://jwxt.wit.edu.cn/jsxsd/xskb_list.do";

    private WebDriver driver;
    private WebDriverWait wait;

    public WITSeleniumCrawler() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void crawl() throws IOException, InterruptedException {
        try {
            System.out.println("=== 开始爬取课表 ===");

            login();

            if (isLoginSuccess()) {
                System.out.println("登录成功！");
                List<Map<String, String>> courses = getCourseTable();
                printCourses(courses);
            } else {
                System.err.println("登录失败，请检查账号密码或验证码");
            }

        } catch (Exception e) {
            System.err.println("爬取过程中出错: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private void login() throws IOException {
        driver.get(LOGIN_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userAccount")));

        driver.findElement(By.id("userAccount")).sendKeys(STUDENTID);
        driver.findElement(By.id("userPassword")).sendKeys(PASSWORD);

        String captchaText = getCaptchaByScreenshot();
        driver.findElement(By.id("RANDOMCODE")).sendKeys(captchaText);

        driver.findElement(By.id("loginButton")).click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isLoginSuccess() {
        try {
            wait.until(ExpectedConditions.urlContains("xsMain.jsp"));
            return true;
        } catch (TimeoutException e) {
            System.err.println("登录超时，当前URL: " + driver.getCurrentUrl());
            return false;
        }
    }

    private List<Map<String, String>> getCourseTable() {
        driver.get(COURSE_TABLE_URL);

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("kbtable")));
        } catch (TimeoutException e) {
            System.err.println("课表加载超时");
            return Collections.emptyList();
        }

        String pageSource = driver.getPageSource();
        return parseCourseTable(pageSource);
    }

    private String getCaptchaByScreenshot() throws IOException {
        WebElement captchaImg = driver.findElement(By.id("imgCode"));
        File screenshot = captchaImg.getScreenshotAs(OutputType.FILE);
        File saveFile = new File("captcha_manual.png");

        java.nio.file.Files.copy(screenshot.toPath(), saveFile.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        System.out.println("验证码已保存为: " + saveFile.getAbsolutePath());
        System.out.print("请输入验证码图片中的字符: ");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine().trim();
    }

    private List<Map<String, String>> parseCourseTable(String html) {
        List<Map<String, String>> courseList = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        Element kbtable = doc.getElementById("kbtable");
        if (kbtable == null) {
            System.err.println("未找到课表表格");
            return courseList;
        }

        Elements rows = kbtable.select("tbody tr");

        for (Element row : rows) {
            Elements cells = row.select("td");

            for (Element cell : cells) {
                Element courseDiv = cell.selectFirst("div.kbcontent");
                if (courseDiv != null) {
                    Map<String, String> courseInfo = extractCourseInfo(courseDiv);
                    if (!courseInfo.isEmpty()) {
                        courseList.add(courseInfo);
                    }
                }
            }
        }

        return courseList;
    }

    private Map<String, String> extractCourseInfo(Element courseDiv) {
        Map<String, String> course = new HashMap<>();

        String text = courseDiv.text();
        if (text.isEmpty()) {
            return course;
        }

        String[] lines = text.split("\\s+");
        if (lines.length > 0) {
            course.put("courseName", lines[0]);
        }
        if (lines.length > 1) {
            course.put("teacher", lines[1]);
        }
        if (lines.length > 2) {
            course.put("location", lines[2]);
        }

        course.put("rawInfo", text);

        return course;
    }

    private void printCourses(List<Map<String, String>> courses) {
        System.out.println("\n========== 课程列表 ==========");
        System.out.println("共找到 " + courses.size() + " 门课程\n");

        for (int i = 0; i < courses.size(); i++) {
            Map<String, String> course = courses.get(i);
            System.out.println("课程 " + (i + 1) + ":");
            System.out.println("  课程名: " + course.getOrDefault("courseName", "未知"));
            System.out.println("  教师: " + course.getOrDefault("teacher", "未知"));
            System.out.println("  地点: " + course.getOrDefault("location", "未知"));
            System.out.println("  详细信息: " + course.getOrDefault("rawInfo", ""));
            System.out.println();
        }
    }

    public static void main(String[] args) throws Exception {
        new WITSeleniumCrawler().crawl();
    }
}
