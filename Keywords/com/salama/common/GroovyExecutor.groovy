package com.salama.common

import com.kms.katalon.core.annotation.SetUp
import com.kms.katalon.core.annotation.SetupTestCase
import com.kms.katalon.core.annotation.TearDown
import com.kms.katalon.core.annotation.TearDownTestCase
import java.lang.reflect.Method
import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject
import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.checkpoint.Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testcase.TestCase
import com.kms.katalon.core.testdata.TestData
import com.kms.katalon.core.testng.keyword.TestNGBuiltinKeywords as TestNGKW
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows





public class GroovyExecutor {
	public static void executeGroovyScriptWithConditions(String scriptPath, String hook) {
		// Create a class loader for Groovy
		GroovyClassLoader classLoader = new GroovyClassLoader();

		try {
			// Load the Groovy class from the specified file
			Class<?> groovyClass = classLoader.parseClass(new File(scriptPath));
			GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();

			// Iterate through all methods of the loaded Groovy class
			for (Method method : groovyClass.getDeclaredMethods()) {
				// Check if the method has an annotation (e.g., @SetUp, @TearDown, etc.)
				if (method.isAnnotationPresent(SetUp.class) ||
						method.isAnnotationPresent(TearDown.class) ||
						method.isAnnotationPresent(SetupTestCase.class) ||
						method.isAnnotationPresent(TearDownTestCase.class)) {

					// Get the 'skipped' value from the annotation
					boolean skipped = true;
					if (method.isAnnotationPresent(SetUp.class) && hook == 'setup') {
						skipped = method.getAnnotation(SetUp.class).skipped();
					} else if (method.isAnnotationPresent(TearDown.class) && hook == 'teardown') {
						skipped = method.getAnnotation(TearDown.class).skipped();
					} else if (method.isAnnotationPresent(SetupTestCase.class) && hook == 'setuptestcase') {
						skipped = method.getAnnotation(SetupTestCase.class).skipped();
					} else if (method.isAnnotationPresent(TearDownTestCase.class) && hook == 'teardowntestcase') {
						skipped = method.getAnnotation(TearDownTestCase.class).skipped();
					}

					// If the method is not skipped, invoke it
					if (!skipped) {
						System.out.println("Executing method: " + method.getName());
						method.invoke(groovyObject);
					} else {
						System.out.println("Skipping method: " + method.getName());
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("An error occurred while executing the Groovy script: " + e.getMessage());
		} finally {
			// Cleanup the class loader
			classLoader.close();
		}
	}
}
