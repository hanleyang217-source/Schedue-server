package org.example.schedueserver.utils;

import org.example.schedueserver.pojo.CourseInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;

public class CourseCrawler {

    private static final String LOGIN_URL = "http://jwxt.wit.edu.cn.jsxsd";
    private static final String COURSE_TABLE_URL = "http://jwxt.wit.edu.cn/jsxsd/xskb_list.do";

    private WebDriver driver;
    private WebDriverWait wait;

    public CourseCrawler() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--headless"); // 无头模式，不显示浏览器窗口

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    /**
     * 爬取指定学生的课表
     * @param studentId 学号
     * @param password 密码
     * @return 课程列表
     */
    public List<CourseInfo> crawlCourseTable(String studentId, String password) {
        List<CourseInfo> courses = new ArrayList<>();

        try {
            System.out.println("=== 开始爬取课表 ===");

            login(studentId, password);

            if (isLoginSuccess()) {
                System.out.println("登录成功！");
                courses = getCourseTable();
                System.out.println("共爬取到 " + courses.size() + " 门课程");
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

        return courses;
    }

    private void login(String studentId, String password) throws IOException, InterruptedException {
        driver.get(LOGIN_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userAccount")));

        driver.findElement(By.id("userAccount")).sendKeys(studentId);
        driver.findElement(By.id("userPassword")).sendKeys(password);

        String captchaText = getCaptchaText();
        driver.findElement(By.id("RANDOMCODE")).sendKeys(captchaText);

        driver.findElement(By.id("loginButton")).click();

        Thread.sleep(2000);
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

    private String getCaptchaText() throws IOException {
        WebElement captchaImg = driver.findElement(By.id("imgCode"));
        File screenshot = captchaImg.getScreenshotAs(OutputType.FILE);
        File saveFile = new File("captcha.png");

        Files.copy(screenshot.toPath(), saveFile.toPath(),
                (CopyOption) Files.createDirectories(saveFile.getParentFile().toPath()),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        System.out.println("验证码已保存为: " + saveFile.getAbsolutePath());
        System.out.print("请输入验证码: ");
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine().trim();
    }

    private List<CourseInfo> getCourseTable() {
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

    private List<CourseInfo> parseCourseTable(String html) {
        List<CourseInfo> courseList = new ArrayList<>();
        Document doc = Jsoup.parse(html);

        Element kbtable = doc.getElementById("kbtable");
        if (kbtable == null) {
            System.err.println("未找到课表表格");
            return courseList;
        }

        Elements rows = kbtable.select("tbody tr");

        for (Element row : rows) {
            Elements cells = row.select("td");

            for (int i = 0; i < cells.size(); i++) {
                Element cell = cells.get(i);
                Element courseDiv = cell.selectFirst("div.kbcontent");

                if (courseDiv != null && !courseDiv.text().isEmpty()) {
                    CourseInfo course = extractCourseInfo(courseDiv, i);
                    if (course != null) {
                        courseList.add(course);
                    }
                }
            }
        }

        return courseList;
    }

    private CourseInfo extractCourseInfo(Element courseDiv, int columnIndex) {
        CourseInfo course = new CourseInfo();

        String text = courseDiv.text();
        if (text.isEmpty()) {
            return null;
        }

        course.setRawInfo(text);

        String[] lines = text.split("\\s+");
        if (lines.length > 0) {
            course.setCourseName(lines[0]);
        }
        if (lines.length > 1) {
            course.setTeacher(lines[1]);
        }
        if (lines.length > 2) {
            course.setLocation(lines[2]);
        }

        course.setWeekDay(getWeekDay(columnIndex));
        course.setSection(getSectionFromCell(courseDiv.parent()));

        return course;
    }

    private String getWeekDay(int columnIndex) {
        String[] weekDays = {"", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        if (columnIndex >= 0 && columnIndex < weekDays.length) {
            return weekDays[columnIndex];
        }
        return "未知";
    }

    private String getSectionFromCell(Element cell) {
        Element sectionElement = cell.selectFirst("td.mc");
        if (sectionElement != null) {
            return sectionElement.text();
        }
        return "";
    }

    public static void printCourses(List<CourseInfo> courses) {
        System.out.println("\n========== 课程列表 ==========");
        System.out.println("共找到 " + courses.size() + " 门课程\n");

        for (int i = 0; i < courses.size(); i++) {
            CourseInfo course = courses.get(i);
            System.out.println("课程 " + (i + 1) + ":");
            System.out.println("  课程名: " + course.getCourseName());
            System.out.println("  教师: " + course.getTeacher());
            System.out.println("  地点: " + course.getLocation());
            System.out.println("  时间: " + course.getWeekDay() + " " + course.getSection());
            System.out.println();
        }
    }

    public static void main(String[] args) {
        CourseCrawler crawler = new CourseCrawler();

        String studentId = "2407010812";
        String password = "Jinitaimei123@";

        List<CourseInfo> courses = crawler.crawlCourseTable(studentId, password);
        printCourses(courses);
    }
}
