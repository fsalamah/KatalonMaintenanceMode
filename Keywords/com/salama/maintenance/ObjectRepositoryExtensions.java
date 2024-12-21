package com.salama.maintenance;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.lang.NotImplementedException;
import org.openqa.selenium.By;

import com.kms.katalon.core.configuration.RunConfiguration;
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords;
import com.kms.katalon.core.mobile.keyword.internal.MobileDriverFactory;
import com.kms.katalon.core.model.FailureHandling;
import com.kms.katalon.core.testdata.ExcelData;
import com.kms.katalon.core.testdata.reader.ExcelFactory;
import com.kms.katalon.core.testobject.ConditionType;
import com.kms.katalon.core.testobject.MobileTestObject;
import com.kms.katalon.core.testobject.TestObject;
import com.kms.katalon.core.testobject.TestObjectProperty;
import com.kms.katalon.core.util.KeywordUtil;
import com.kms.katalon.core.webui.driver.DriverFactory;
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords;
import com.salama.common.ScannerSingleton;
import com.salama.common.Utilities;
import com.salama.common.XmlUtilities;

import internal.GlobalVariable;

public class ObjectRepositoryExtensions extends com.kms.katalon.core.testobject.ObjectRepository {

	private static final String WEBELEMENT_FILE_EXTENSION = ".rs";

	private static String[] readIgnoredObjects() {

		Utilities.createFileIfNotExists(".mntcignore");
		
		File ignoredPaths = new File(".mntcignore");
	

		String[] ignoredFiles = readFileAsString(".mntcignore").split("\n");
		for (String filePath : ignoredFiles) {
			filePath = filePath.trim();
		}
		return ignoredFiles;
	}

	private static String readFileAsString(String filePath) {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString().trim();
	}

	private static ArrayList<String> extractVars(String input) {
		ArrayList<String> variableNames = new ArrayList<>();
		Matcher matcher = Pattern.compile("\\$\\{(\\w+)\\}").matcher(input);
		while (matcher.find()) {
			variableNames.add(matcher.group(1));
		}
		return variableNames;
	}
	private static String prependObjectRepositoryIfNotPresent(String testObjectRelativeId)
	{
		if(testObjectRelativeId.startsWith("Object Repository") == true)		
			return "/"+testObjectRelativeId;
		
		
		return "/Object Repository/"+testObjectRelativeId;
	}
	public static TestObject findTestObject(String testObjectRelativeId, Map<String, Object> variables) {
		// Perform custom logic here
		String filePath = "";
		boolean fileExists = false;
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (fileExists == false) {
			filePath = RunConfiguration.getProjectDir() +  prependObjectRepositoryIfNotPresent(testObjectRelativeId)
					+ WEBELEMENT_FILE_EXTENSION;
			File file = new File(filePath);
			System.out.println("File exists? " + file.exists());
			logToBS("info", filePath);

			fileExists = file.exists();
			if (fileExists == false) {
				Utilities.playWavFile("error.wav");
				logToBS("error", "Invalid Object Path: " + testObjectRelativeId);
				System.out.println(
						"The file was not found, please provide a correct object path and update the path in your code:\n");

				try {
					testObjectRelativeId = reader.readLine();
					if (testObjectRelativeId.toString().trim().equalsIgnoreCase("exit") == true)
						break;
				} catch (Exception e) {
					System.out.println("Couldn't read user input\n" + e.getStackTrace());
				}
			}
		}
		;
		String locator = XmlUtilities.getNodesByTagName(filePath, "locator").item(0).getTextContent();

		TestObject object = createTestObjectByXpath(testObjectRelativeId, locator); // com.kms.katalon.core.testobject.ObjectRepository.findTestObject(testObjectRelativeId,variables);

		System.out.println(variables);

		boolean exist = false;
		boolean shouldExist = true;
		// StrSubstitutor strSubstitutor = new StrSubstitutor(variablesStringMap);
		int counter = 0;
		while (counter < 100 && exist == false && shouldExist == true) {
			System.out.println("Retry #" + counter);
			object = com.kms.katalon.core.testobject.ObjectRepository.findTestObject(testObjectRelativeId, variables);
			exist = ObjectRepositoryExtensions.checkForObjectOnThePage(object, shouldExist, filePath);
			counter++;
		}

		// TestObject testObject = findTestObject(objectPath)

		// Example: Add a custom property to the test object
		// testObject.addTestObjectProperty("myCustomProperty", "customValue")

		return object;
	}

	private static TestObject getCustomObject(String testObjectRelativeId) {
		String locator = XmlUtilities.getNodesByTagName(testObjectRelativeId + WEBELEMENT_FILE_EXTENSION, "locator")
				.item(0).getTextContent();
		return createTestObjectByXpath(testObjectRelativeId, locator);
	}

	private static String[] getSkipObjects () 
	{
		return readIgnoredObjects();
		}

	/**
	 * Intercepts the findTestObject() function and performs custom logic.
	 * 
	 * @param objectPath The path of the test object in the Object Repository.
	 * @return The test object with custom modifications (if any).
	 */

	public static TestObject findTestObject(String objectPath) {

		String filePath = "";
		boolean fileExists = false;
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (fileExists == false) {
			filePath = RunConfiguration.getProjectDir() + prependObjectRepositoryIfNotPresent(objectPath)
					+ WEBELEMENT_FILE_EXTENSION;
			File file = new File(filePath);
			System.out.println("File exists? " + file.exists());
			logToBS("info", (filePath));

			fileExists = file.exists();
			if (fileExists == false) {
				Utilities.playWavFile("error.wav");
				Utilities.printLine("Invalid Object Path: " + objectPath, Utilities.ANSI_YELLOW_BACKGROUND);
				Utilities.printLine("The file was not found, please provide a correct object path and update the path in your code:", Utilities.ANSI_YELLOW_BACKGROUND);
				try {
					objectPath = reader.readLine();
				} catch (Exception e) {
					Utilities.printLine("Couldn't read user input\n" + e.getStackTrace(), Utilities.ANSI_YELLOW_BACKGROUND);
				}
			}
		}

		String fileText = readFileAsString(filePath);

		File file = new File(filePath);

		ArrayList<String> variables = extractVars(fileText);

		Map<String, Object> assignedVariables = new HashMap<String, Object>();



		boolean exist = false;
		boolean shouldExist = true;

		TestObject object = com.kms.katalon.core.testobject.ObjectRepository.findTestObject(objectPath,
				assignedVariables);
		


		System.out.println(assignedVariables);

		// StrSubstitutor strSubstitutor = new StrSubstitutor(variablesStringMap);
		int counter = 0;
		while (counter < 100 && exist == false && shouldExist == true) {
			System.out.println("Retry #" + counter);

			object = com.kms.katalon.core.testobject.ObjectRepository.findTestObject(objectPath, assignedVariables);
			exist = ObjectRepositoryExtensions.checkForObjectOnThePage(object, shouldExist, filePath);

			counter++;
		}

		return object;
	}

	
	private static String takeScreenshot()
	{
		if(isAppiumDriver())
				return MobileBuiltInKeywords.takeScreenshot();
			else
				return WebUiBuiltInKeywords.takeScreenshot();
	}
	
	private static boolean checkIfElementExists(TestObject to)
	{
		try {
		if(isAppiumDriver())
			return MobileBuiltInKeywords.verifyElementExist(to, getMaintenanceModeTimeoutSeconds(), FailureHandling.OPTIONAL);
		else
			return WebUiBuiltInKeywords.verifyElementPresent(to, getMaintenanceModeTimeoutSeconds(), FailureHandling.OPTIONAL);
		}
		catch (Exception e)//TODO add proper exception handling  
		{
			return false;
		}
	}
	
	private static String getPageSource()
	{
		if(isAppiumDriver())
			return MobileDriverFactory.getDriver().getPageSource();
		else
			return DriverFactory.getWebDriver().getPageSource();
		
	}
	private static boolean checkForObjectOnThePage(TestObject to, boolean shoudExist, String filePath) {

		String objectId = to.getObjectId().toString();
		if (Arrays.asList(ObjectRepositoryExtensions.getSkipObjects()).contains(objectId) == true
				|| isMaintenanceMode() == false)

			return true;

		if (checkIfElementExists(to) == true)
			return true;
		else

		{

			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			try {

				String line = "";

				Utilities.playWavFile("notification.wav");
				while (!line.equalsIgnoreCase("r") == true && !line.equalsIgnoreCase("i") == true) {

					Utilities.printLine("Test Object was not found, press:\n ${to.getObjectId()}\n R - retry finding the object press \n I- Ignore and continue \n X- Get the view XML \n S- Take a screenshot \n O- Open test object file \n exit- to exit the test case exeuction with failure", Utilities.ANSI_YELLOW_BACKGROUND);
					
					Utilities.printLine("Enter you selection:", Utilities.ANSI_YELLOW_BACKGROUND);
					

					line = reader.readLine();
					if (line.equalsIgnoreCase("x") == true)
						saveAndOpenPageXML(getPageSource());

					else if (line.equalsIgnoreCase("s") == true) {
						String imagePath = MobileBuiltInKeywords.takeScreenshot();

						openFile(imagePath);
						System.out.println(imagePath);
					} else if (line.equalsIgnoreCase("o") == true) {
						openFile(objectId + ".rs");
					} else if (line.equalsIgnoreCase("exit") == true) {
						KeywordUtil.markErrorAndStop("Exited thru maintainence mode");
					} else if (line.equalsIgnoreCase("pl") == true) {
						printLocatorXMLDetails(filePath);
					} else if (line.startsWith("rl:")) {
						String newLocator = line.substring(3);
						String locatorStrategy = "XPATH";
						updateLocatorFile(filePath, newLocator, locatorStrategy);
					} else if (line.startsWith("ow:")) {
						String newLocator = line.substring(3);
						String locatorStrategy = "XPATH";
						XmlUtilities.overwriteLocatorXML(filePath, line.substring(3).trim());
					}

					// System.out.println(line);
				}

				if (line.trim().toLowerCase().equals("r") == true) {
					System.out.println("Retry checking if the object exists.");
					return false;
				} else if (line == "s") {
					System.out.println("Source \n\n" + MobileDriverFactory.getDriver().getPageSource() + "\n\n");
					return false;
				}

				else {
					System.out.println("Continuing execution, ignoring the object.");
					return true;

				}

			} catch (IOException ioex) {

				ioex.printStackTrace();
				return false;
			}
		}

	}

	/**
	 * Check if element present in timeout
	 * 
	 * @param to      Katalon test object
	 * @param timeout time to wait for element to show up
	 * @return true if element present, otherwise false
	 */

	public static String getTranslation(String key) {
		String lang = (String) GlobalVariable.language;

		int langIndex = (lang.toLowerCase().equals("ar") ? 1 : 2);
		try {
			ExcelData excelData = ExcelFactory.getExcelDataWithDefaultSheet("(String)GlobalVariable.translations_path",
					"Sheet1", true);
			int rowCount = excelData.getRowNumbers();

			for (int i = 0; i < rowCount; i++) {

				System.out.println((String) excelData.getAllData().get(i).get(0));
				if (key.equals((String) excelData.getAllData().get(i).get(0)))

					return (String) excelData.getAllData().get(i).get(langIndex);
			}
		} catch (Exception e) {
			KeywordUtil.markFailed(e.getMessage());
		}
		return null;
	}

	public static String readProperty(String key) {
		Properties properties = new Properties();

		// Path to your properties file
		String fileName = "console.properties"; // Change this to your actual file name

		try (InputStream input = new FileInputStream(fileName)) {
			// Load the properties file
			properties.load(input);

			// Read properties
			return properties.getProperty(key);

		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}

	}

	private static Scanner getScanner() {
		return ScannerSingleton.getInstance();
	}

	// Method to check if a file exists and allow the user to correct the path
	public static String fileExists(String initialPath) {

		String filePath = initialPath;

		while (true) {
			Scanner scanner = getScanner();
			File file = new File(filePath);
			if (file.exists()) {
				// Print the file path if it exists
				System.out.println("File path: " + file.getPath());
				scanner.close();
				return filePath; // Return the path as provided
			} else {
				System.out.println("The file does not exist at: " + file.getPath());
				System.out.print("Would you like to correct the path? (yes/no): ");

				String response = scanner.nextLine();
				if (response.equalsIgnoreCase("no")) {
					System.out.println("Exiting without a valid path.");
					scanner.close();
					return null; // Or handle as needed
				}

				System.out.print("Please enter a new file path: ");
				filePath = scanner.nextLine();
			}
		}
	}

	/**
	 * Logs messages to both the console and, if necessary, to BrowserStack for
	 * annotation. The log level can be adjusted based on the provided level, and
	 * the message can be sent to BrowserStack if the `bs_labeling` global variable
	 * is set to true.
	 *
	 * @param level   The log level, which could be "info" or other values (e.g.,
	 *                "warn", "error").
	 * @param message The message to be logged.
	 */
	public static void logToBS(String level, String message) {

		// Print the message to the console (standard output)
		System.out.println(message);

		// Check if the global variable `bs_labeling` is true
//		if ((boolean) GlobalVariable.bs_labeling == true) {
//			// Escape the message to make it safe for JSON insertion (handles special
//			// characters)
//			message = StringEscapeUtils.escapeJson(message);
//
//			// Get the WebDriver instance for BrowserStack from MobileDriverFactory
//			JavascriptExecutor jse = (JavascriptExecutor) (MobileDriverFactory.getDriver());
//
//			// Execute a JavaScript command to send the log to BrowserStack as an annotation
//			jse.executeScript("browserstack_executor: {\"action\": \"annotate\", \"arguments\": {\"data\": \"" + message
//					+ "\", \"level\": \"" + level + "\"}}");
//		}
	}

	public static void saveAndOpenPageXML(String textToSave) {
		//setup dir and file name
		Utilities.createDirectoryIfNotExists("Page XMLs");
		
		String filePath = "Page XMLs/" + MobileDriverFactory.getDriver().getSessionId() + ".xml";
		
		// Step 1: Save text to file
		try {
			FileWriter writer = new FileWriter(filePath);
			writer.write(textToSave);
			System.out.println("File saved successfully.");
			writer.close();
		} catch (IOException e) {
			System.err.println("Error writing to file: " + e.getMessage());
			return;
		}

		// Step 2: Open the file in the system editor
		openFile(filePath);
	}

	public static void drawSquareOnImage(String imagePath, int x, int y, int width, int height) {
		try {
			// Load the image
			BufferedImage image = ImageIO.read(new File(imagePath));

			// Create a Graphics2D object
			Graphics2D g2d = image.createGraphics();

			// Set the color to yellow
			g2d.setColor(Color.YELLOW);

			// Draw the square
			g2d.drawRect(x, y, width, height);

			// Dispose the graphics context
			g2d.dispose();

			// Save the modified image back to the same path
			ImageIO.write(image, "png", new File(imagePath));

			System.out.println("Image updated and saved to " + imagePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void openFile(String filePath) {
		try {
			File file = new File(filePath);
			if (file.exists()) {
				Desktop.getDesktop().open(file);
				System.out.println("File opened in the default editor.");
			} else {
				System.err.println("File does not exist.");
			}
		} catch (IOException e) {
			System.err.println("Error opening file: " + e.getMessage());
		}
	}

	/**
	 * Converts a Katalon Mobile Test Object to a Selenium By locator.
	 *
	 * @param to The MobileTestObject to be converted.
	 * @return A Selenium By locator corresponding to the mobile locator strategy.
	 * @throws IllegalArgumentException If the mobile locator strategy is
	 *                                  unsupported.
	 */
	public static By convertMobileToSelenium(MobileTestObject to) throws IllegalArgumentException {
		// Retrieve the mobile locator string from the MobileTestObject
		String mobileLocator = to.getMobileLocator();

		// Retrieve the locator strategy used for the mobile locator
		String mobileLocatorStrategy = to.getMobileLocatorStrategy().toString();

		// Determine the appropriate Selenium By locator based on the mobile locator
		// strategy
		switch (mobileLocatorStrategy) {
		case "XPATH":
			// Return By locator for XPATH strategy
			return By.xpath(mobileLocator);
		case "ID":
			// Return By locator for ID strategy
			return By.id(mobileLocator);
		case "NAME":
			// Return By locator for NAME strategy
			return By.name(mobileLocator);
		case "CSS":
			// Return By locator for CSS strategy
			return By.cssSelector(mobileLocator);
		case "CLASS_NAME":
			// Return By locator for CLASS_NAME strategy
			return By.className(mobileLocator);
		// Additional strategies can be added here as needed
		default:
			// Throw an exception if the locator strategy is not supported
			throw new IllegalArgumentException("Unsupported locator strategy: ${mobileLocatorStrategy}");
		}
	}

	// Function to create a TestObject at runtime
	public static TestObject createTestObjectByXpath(String objectName, String xpath) {
		TestObject testObject = new TestObject(objectName);

		// Define the properties of the TestObject
		TestObjectProperty property = new TestObjectProperty("xpath", ConditionType.EQUALS, xpath);

		// Add the property to the TestObject
		testObject.addProperty(property.getName(), ConditionType.EQUALS, property.getValue());

		return testObject;
	}

	public static TestObject createTestObjectByXpath(String xpath) {
		return createTestObjectByXpath(null, xpath);
	}

	public static TestObject getOsSpecificTestObject(String IosXpath, String androidXpath) {
		if(isAppiumDriver())
		{
			if (Utilities.getExecutionOS().toLowerCase().contains("ios"))
				return createTestObjectByXpath(IosXpath);
			else
				return createTestObjectByXpath(androidXpath);
		}
		else throw new NotImplementedException("This method is only implemented for mobile locators using XPATH");
			
	}

	private static void printLocatorXMLDetails(String locatorPath) {
		Utilities.printLine("----------Locator Details----------", Utilities.ANSI_CYAN);
		Utilities.printLine(
				"Locator strategy: "
						+ XmlUtilities.getNodesByTagName(locatorPath, "locatorStrategy").item(0).getTextContent(),
				Utilities.ANSI_CYAN);
		Utilities.printLine("Locator value: ", Utilities.ANSI_CYAN);
		Utilities.printLine(XmlUtilities.getNodesByTagName(locatorPath, "locator").item(0).getTextContent());
	}

	private static void updateLocatorFile(String locatorPath, String locatorVal, String locatorStrategy) {
		XmlUtilities.updateNodeText(locatorPath, "locator", locatorVal);
		XmlUtilities.updateNodeText(locatorPath, "locatorStrategy", "XPATH");
		Utilities.printLine("----------Locator Updated----------", Utilities.ANSI_BLUE);
		// Utilities.printLine("----------NEW LO:----------", Utilities.ANSI_BLUE);
	}
	
	private static boolean isMaintenanceMode()
	{
		Boolean result = (Boolean) Utilities.getGlobalVarIfExist("maintenance_mode");
		if(result == null)
			return false;
		else 
			return result;
		
	}
	
	private static int getMaintenanceModeTimeoutSeconds()
	{
		int result =0;
		
		if( Utilities.getGlobalVarIfExist("maintenance_timeout_ms")!= null)
		{
			result = (int)((int)Utilities.getGlobalVarIfExist("maintenance_timeout_ms")/1000);
			//make sure the timeout is a positive int
			
		}
		if(result < 1 )				
			result = 5;
		
		return result;		 				
	}
	
	private static boolean isAppiumDriver()
	{
		return RunConfiguration.getExecutionProperties().get("drivers").toString().toLowerCase().contains("appium");
	}
	
	public static Object getDriver()
	{
		if(isAppiumDriver())
		{
			return MobileDriverFactory.getDriver();
		}
		else
		return DriverFactory.getWebDriver();	
	}
	
}
