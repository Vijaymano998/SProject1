package org.Utility;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class Baseclass {

	ThreadLocal<WebDriver> driverThread = new ThreadLocal<WebDriver>();
	ThreadLocal<ExtentTest> extentTest = new ThreadLocal<ExtentTest>();
	ThreadLocal<String> methodName = new ThreadLocal<String>();
	ThreadLocal<String> browserName = new ThreadLocal<String>();
	public static String currDir = System.getProperty("user.dir");
	public static ExtentReports extent;

	public void propertyReader() {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(currDir + "./src/main/resources/data/object.properties"));
			Set<String> stringPropertyNames = prop.stringPropertyNames();
			for (String name : stringPropertyNames) {
				System.setProperty(name, prop.getProperty(name));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ExtentReports createInstance() {
		extent = new ExtentReports();
		if (System.getProperty("reportType").equals("html")) {
			String reportName = new SimpleDateFormat("yyMMddHHmmssSS").format(new Date());
			ExtentSparkReporter spark = new ExtentSparkReporter("Test_" + reportName);
			try {
				spark.loadJSONConfig(currDir + "./src/main/resources/spark.json");
				extent.attachReporter(spark);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return extent;
	}

	public synchronized ThreadLocal<WebDriver> chromeHeadless() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--incognito");
		options.addArguments("--headless");
		Map<String, Object> map = new HashMap<String, Object>();
		String downloadPath = Paths.get("src", "main", "resources", "download").toAbsolutePath().toString();
		map.put("download.default_directory", downloadPath);
		options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
		options.setExperimentalOption("prefs", map);
		driverThread.set(new ChromeDriver(options));
		return driverThread;
	}

	public synchronized ThreadLocal<WebDriver> chromeDriver() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--incognito");
		options.addArguments("--headless");
		Map<String, Object> map = new HashMap<String, Object>();
		String downloadPath = Paths.get("src", "main", "resources", "download").toAbsolutePath().toString();
		map.put("download.default_directory", downloadPath);
		options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
		options.setExperimentalOption("prefs", map);
		driverThread.set(new ChromeDriver(options));
		return driverThread;
	}

	public synchronized ThreadLocal<WebDriver> edgeDriver() {
		EdgeOptions options = new EdgeOptions();
		options.addArguments("--incognito");
		options.addArguments("--headless");
		Map<String, Object> map = new HashMap<String, Object>();
		String downloadPath = Paths.get("src", "main", "resources", "download").toAbsolutePath().toString();
		map.put("download.default_directory", downloadPath);
		options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
		options.setExperimentalOption("prefs", map);
		driverThread.set(new EdgeDriver(options));
		return driverThread;
	}

	public synchronized ThreadLocal<WebDriver> firefoxDriver() {
		FirefoxOptions options = new FirefoxOptions();
		options.addArguments("--incognito");
		options.addArguments("--headless");
		Map<String, Object> map = new HashMap<String, Object>();
		String downloadPath = Paths.get("src", "main", "resources", "download").toAbsolutePath().toString();
		map.put("download.default_directory", downloadPath);
		FirefoxProfile features = new FirefoxProfile();
		features.setPreference("excludeSwitches", Collections.singletonList("enable-automation"));
		features.setPreference("prefs", map);
		options.setProfile(features);
		driverThread.set(new FirefoxDriver(options));
		return driverThread;
	}

	public void getBrowser(String browser) {
		if (browser.equals("chromeheadless")) {
			chromeHeadless();
		} else if (browser.equals("chrome")) {
			chromeDriver();
		} else if (browser.equals("firefox")) {
			firefoxDriver();
		} else if (browser.equals("edge")) {
			edgeDriver();
		} else {
			getTest().log(Status.WARNING, "Browser parameter input is not present as expected.");
		}
	}

	public synchronized WebDriver getWebDriver() {
		return driverThread.get();
	}

	public synchronized ExtentTest getTest() {
		return extentTest.get();
	}

	public void createtest() {
		Random r = new Random();
		int value = r.nextInt(13 * 3) - 3;
		try {
			Thread.sleep(Long.valueOf(value));
		} catch (Exception e) {
			e.printStackTrace();
		}
		String testName = methodName.get() + " - " + browserName.get();
		ExtentTest createTest = extent.createTest(testName);
		extentTest.set(createTest);
	}

	@BeforeSuite
	public void beforeSuite() {
		propertyReader();
		createInstance();
	}

	@AfterSuite
	public void afterSuite() {
		extent.flush();
	}

	@Parameters({ "browser" })
	@BeforeMethod
	public void beforeMethods(String browser, Method method) {
		methodName.set(method.getName());
		browserName.set(browser);
		getBrowser(browser);
		createtest();
	}

	@AfterMethod
	public void afterMethod(ITestResult result) {
		if (result.getStatus() == ITestResult.FAILURE) {
			getTest().fail(MarkupHelper.createLabel(methodName.get(), ExtentColor.RED));
		} else if (result.getStatus() == ITestResult.SKIP) {
			getTest().skip(MarkupHelper.createLabel(methodName.get(), ExtentColor.YELLOW));
		} else if (result.getStatus() == ITestResult.SUCCESS) {
			getTest().pass(MarkupHelper.createLabel(methodName.get(), ExtentColor.GREEN));
		}
	}

}
