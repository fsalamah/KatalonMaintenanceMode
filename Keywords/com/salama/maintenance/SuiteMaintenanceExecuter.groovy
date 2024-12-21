package com.salama.maintenance

import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject


import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.xml.bind.helpers.DefaultValidationEventHandler
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.checkpoint.Checkpoint
import com.kms.katalon.core.context.TestCaseContext
import com.kms.katalon.core.context.TestSuiteContext
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.mobile.keyword.internal.MobileDriverFactory
import com.kms.katalon.core.model.FailureHandling;
import com.kms.katalon.core.testcase.TestCase
import com.kms.katalon.core.testdata.TestData
import com.kms.katalon.core.testng.keyword.TestNGBuiltinKeywords as TestNGKW
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows
import com.kms.katalon.entity.link.TestSuiteTestCaseLink
import com.salama.common.GroovyExecutor
import com.salama.common.ScannerSingleton
import com.salama.common.Utilities

import java.io.File;
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.stream.Collectors;

import org.openqa.selenium.remote.RemoteWebDriver
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import internal.GlobalVariable
import io.appium.java_client.MobileDriver




public class TestSuiteMaintenanceExecuter {
	public static  List<TestSuiteTestCaseLink> readTestSuite(String filePath) {
		List<TestSuiteTestCaseLink> testSuite = new ArrayList<>();
		try {


			File xmlFile = new File(filePath + '.ts');
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			NodeList nodeList = doc.getElementsByTagName("testCaseLink");

			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;

					TestSuiteTestCaseLink testCase = new TestSuiteTestCaseLink();
					testCase.setGuid(element.getElementsByTagName("guid").item(0).getTextContent());
					//testCase.isReuseDriver(Boolean.parseBoolean(element.getElementsByTagName("isReuseDriver").item(0).getTextContent()));
					testCase.setIsRun(Boolean.parseBoolean(element.getElementsByTagName("isRun").item(0).getTextContent()) as boolean);
					testCase.setTestCaseId(element.getElementsByTagName("testCaseId").item(0).getTextContent());
					testCase.setUsingDataBindingAtTestSuiteLevel(
							Boolean.parseBoolean(element.getElementsByTagName("usingDataBindingAtTestSuiteLevel").item(0).getTextContent()));

					testSuite.add(testCase);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return testSuite;
	}

	private static LinkedHashMap<TestSuiteTestCaseLink, ExecutionRecord> getSuiteExecutionList(String suitePath) {
		List<TestSuiteTestCaseLink> testCaseLinks =  readTestSuite(suitePath);
		LinkedHashMap<TestSuiteTestCaseLink, ExecutionRecord> result = new LinkedHashMap<>();
		for(int i = 0; i < testCaseLinks.size();i++) {
			ExecutionRecord record = 	new ExecutionRecord();
			record.testCaseId= testCaseLinks[i].testCaseId;
			result.put(testCaseLinks[i],record)
		}

		return result;
	}


	public static void printTestCases(LinkedHashMap<TestSuiteTestCaseLink,ExecutionRecord> testCases, int executingIndex=0) {

		Utilities.printLine("N.\tStatus\tRuns\tTest Case",Utilities.ANSI_BOLD);
		for(int i = 0 ; i < testCases.size(); i++) {
			// ANSI escape code for yellow text

			String result = (testCases.entrySet()[i].value.lastResult!=null?testCases.entrySet()[i].value.lastResult:"N/E");
			String reruns= (testCases.entrySet()[i].value.numberOfReruns!=null?testCases.entrySet()[i].value.numberOfReruns:"0");
			String testName =testCases.entrySet()[i].key.getTestCaseId();

			String color;
			if(result=="true")
				color=Utilities.ANSI_GREEN;
			else if(result=="false" && reruns!="0")
				color = Utilities.ANSI_RED;
			else
				color=Utilities.ANSI_CYAN;
			String exectionFlag = (i==executingIndex?">>":"");
			Utilities.printLine("${exectionFlag}${i+1}\t${(reruns=='0'?'NA':(result=='true'?'PASS':'FAIL'))}\t${reruns}\t${testName}",color);
		}
	}


	private static String readExecutionMode() {

		String choice="";

		choice = (choice == null? "": choice.trim().toUpperCase())
		String[] options = ["1", "2", "3"];
		while(options.toList().indexOf(choice.trim().toUpperCase()) <=-1) {
			Utilities.printLine("Please choose an execution mode");
			Utilities.printLine("1: Pause on failing test case and prompt for next action");
			Utilities.printLine("2: Pause before each test case and prompt for next action");
			Utilities.printLine("3: Hybrid - Start with Mode 1 and switch to Mode 2 on first Failure");
			choice = readInput("Please select a value:\n", "1")
		}
		return choice;
	}
	private static String readExecutionCommand() {
		String choice="";

		choice = (choice == null? "": choice.trim().toUpperCase())
		String[] options = ["C", "R", "G", "I", "L", "CP"];

		while(options.toList().indexOf(choice.trim().toUpperCase()) <=-1) {
			Utilities.printLine("Please choose an execution mode");
			Utilities.printLine("C: Continue exection");
			Utilities.printLine("R: Retry the current test case");
			Utilities.printLine("G: Go to test case number");
			Utilities.printLine("I: Execute text case by id");
			Utilities.printLine("L: List test cases");
			choice = readInput("Please select a value:","C")
			customCommandExecuter(choice);
		}
		return choice;
	}
	private static Scanner getScanner() {
		return ScannerSingleton.getInstance();
	}

	private static int readInteger() {
		//Scanner scanner = new Scanner(System.in);
		int number = 0;
		boolean valid = false;

		while (!valid) {
			System.out.print("Please enter an integer: ");
			if (scanner.hasNextInt()) {
				number = scanner.nextInt();
				valid = true;
			} else {
				System.out.println("That's not a valid integer. Please try again.");
				scanner.next(); // Clear the invalid input
			}
		}

		return number;
	}

	public static String readInput(String prompt, String defaultVal) {

		String result = "c";

		System.out.println(prompt);
		return getScanner().nextLine();
	}
	private static String readTestCasePath() {
		String tcPath = readInput("Please enter the test case ID:", "");

		try {
			findTestCase(tcPath);
			return tcPath;
		}
		catch (Exception e) {
			System.out.println("Incorrect test case ID, please try again.");
			return readTestCasePath();
		}
	}



	/**
	 * Executes a test suite based on the provided test suite ID.
	 * This method manages the execution of test cases, handles user commands,
	 * and provides feedback during the execution process.
	 *
	 * Credits: Faisal salama
	 *
	 * @param testSuiteId The ID of the test suite to be executed.
	 * @param pauseAtTC   The test case index at which to pause execution.
	 * 					  Used with maintenance mode to force a pause at a specific test case 
	 *                    Pass -1 to disable pausing. 
	 */
	@Keyword
	public static void execute(String testSuiteId, int pauseAtTC=-1) {
		int counter = 0;
		Utilities.printLine(
			"Starting suite execution in maintenance mode. You could retry running test cases or jump to a test case.",
			Utilities.ANSI_YELLOW_BACKGROUND);
		Utilities.printLine(
			"NOTE: Katalon Suite Report is not generated in maintenance execution mode",
			Utilities.ANSI_YELLOW_BACKGROUND);
		Utilities.printLine(
			"NOTE: Add maintainence mode ignored paths to your .mntcignore file in the root directory of your project",
			Utilities.ANSI_YELLOW_BACKGROUND);
		// Retrieve the list of test cases for the specified test suite ID.
		LinkedHashMap<TestSuiteTestCaseLink, ExecutionRecord> testCases =
				TestSuiteMaintenanceExecuter.getSuiteExecutionList(testSuiteId);

		// Print the list of test cases.
		printTestCases(testCases);
		Utilities.playWavFile("notification2.wav")
		// Read the execution mode from the user.
		String executionMode = readExecutionMode();
		Utilities.printLine("");
		Utilities.printLine("Selected Execution Mode: " + executionMode);

		// Define the script path for setup and teardown operations.
		String scriptPath = testSuiteId + ".groovy";
		GroovyExecutor.executeGroovyScriptWithConditions(scriptPath, "setup");

		String currentExecutionCommand = "C"; // Default command to continue execution.
		String customTestCaseId = ""; // Placeholder for custom test case ID.
		boolean lastResult = true; // Keep track of the last test case result.
		int currentExecutingTest = 0; // Index of the currently executing test case.

		// Start the main execution loop.
		while (true) {

			if(currentExecutionCommand.equalsIgnoreCase("CP")) {
				//ScriptManager.reloader();
				currentExecutionCommand = readExecutionCommand();
				continue;
			}
			// Adjust execution mode if the last test case failed.
			if (executionMode.equals("3") && lastResult==false) {
				executionMode = "1"; // Switch to default mode.
			}

			// Log the current iteration count.
			System.out.println("Iteration #" + counter);

			// Determine whether to continue execution based on conditions.
			//			boolean isContinue = !(pauseAtTC > -1 && pauseAtTC > currentExecutingTest ) &&
			//					!(executionMode.equals("1") && lastResult == true) &&
			//					!executionMode.equals("2") || executionMode.equals("3");

			//dont continue if the end is reached
			boolean isContinue=(!(pauseAtTC > -1 && pauseAtTC > currentExecutingTest )&&
					//or if we are failing and the execution mode is 1
					(executionMode.equals("1") && lastResult == true) &&
					//or if the execution mode is 2
					!executionMode.equals("2")&&

					currentExecutingTest<testCases.size()
					)  ;

			// Play a notification sound and display the next test case.
			Utilities.playWavFile("notification${lastResult==true?"2":"3"}.wav");

			if(currentExecutingTest>=testCases.size()) {
				//currentExecutingTest = testCases.size()-1;
			}
			else {
				Utilities.printLine("(Next Test) " + currentExecutingTest + ": " +
						testCases.entrySet()[currentExecutingTest].getKey().getTestCaseId());
			}
			System.out.println("Continue execution: " + isContinue);
			printTestCases(testCases, currentExecutingTest-1);

			// Read the user's command if not continuing.
			if (!isContinue || currentExecutionCommand.equalsIgnoreCase("G")) {
				currentExecutionCommand = readExecutionCommand();
			}

			// Process user commands.
			if (currentExecutionCommand.equalsIgnoreCase("C") && currentExecutingTest<= testCases.size()) {
				if(currentExecutingTest >= 0 && currentExecutingTest<testCases.size())
					currentExecutingTest++;
				else
					Utilities.printLine("End of the test suite reached!", Utilities.ANSI_YELLOW_BACKGROUND)
			} else if (currentExecutionCommand.equalsIgnoreCase("R")) {

				if(lastResult==true && executionMode=="1" && currentExecutingTest< testCases.size()) {
					currentExecutionCommand = "C";
					currentExecutingTest++;
				}

				else if (lastResult==true && executionMode=="2") {
					currentExecutionCommand = readExecutionCommand();
				}
			} else if (currentExecutionCommand.equalsIgnoreCase("G")) {
				currentExecutingTest = readInteger();
				// Validate the test case index.
				if (currentExecutingTest < 0 || currentExecutingTest >= testCases.size()) {
					Utilities.printLine("Invalid Test Case Index. Please try again.");
					currentExecutingTest = 0; // Reset to a safe state.
				}
			} else if (currentExecutionCommand.equalsIgnoreCase("P")) {
				customTestCaseId = readTestCasePath();
			} else if (currentExecutionCommand.equalsIgnoreCase("L")) {
				Utilities.printLine("Test Cases:");
				printTestCases(testCases, currentExecutingTest-1);
				continue; // Skip to the next iteration.
			} else if (currentExecutionCommand.equalsIgnoreCase("session")) {
				Utilities.printLine(MobileDriverFactory.getDriver().getSessionDetail(Utilities.ANSI_BLINK));
				continue; // Skip to the next iteration.
			}

			// Execute the selected test case or the custom test case.
			if (currentExecutionCommand.equalsIgnoreCase("P")) {
				lastResult = executeTestCase(customTestCaseId);
			} else if (currentExecutingTest >= 0 &&
					testCases.entrySet()[currentExecutingTest - 1].getKey().isRun) {
				Utilities.printLine("Executing TC: " + currentExecutingTest, Utilities.ANSI_PURPLE);
				lastResult = executeTestCase(testCases.entrySet()[currentExecutingTest - 1].getKey().getTestCaseId());
				testCases.entrySet()[currentExecutingTest - 1].getValue().lastResult = lastResult;
				testCases.entrySet()[currentExecutingTest - 1].getValue().numberOfReruns++;
			}

			counter++;
			// Increment the iteration counter.
		}

		// Execute teardown operations after all tests are completed.
		GroovyExecutor.executeGroovyScriptWithConditions(scriptPath, "teardown");
	}
	private static void customCommandExecuter(String command) {
		if(command.startsWith("S-number")) {
			Utilities.openFile(command.split()[1]);
		}
	}

	private static boolean executeTestCase(String testCaseId) {
		Utilities.printLine("-----------------------Executing Test Case Number: ${testCaseId}");
		try{

			WebUI.callTestCase(findTestCase(testCaseId), [:], com.kms.katalon.core.model.FailureHandling.STOP_ON_FAILURE);
		}
		catch(Exception e) {
			Utilities.printLine("-----------------------TEST CASE FAILED ${testCaseId}");
			return false;
		}
		return true;
	}
}

public class ExecutionRecord{
	public ExecutionRecord() {}
	public String testCaseId;
	public boolean lastResult;
	public int numberOfReruns;
}

