package com.salama.common

import static com.kms.katalon.core.checkpoint.CheckpointFactory.findCheckpoint
import static com.kms.katalon.core.testcase.TestCaseFactory.findTestCase
import static com.kms.katalon.core.testdata.TestDataFactory.findTestData
import static com.kms.katalon.core.testobject.ObjectRepository.findTestObject
import static com.kms.katalon.core.testobject.ObjectRepository.findWindowsObject

import java.nio.charset.StandardCharsets

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.checkpoint.Checkpoint
import com.kms.katalon.core.cucumber.keyword.CucumberBuiltinKeywords as CucumberKW
import com.kms.katalon.core.mobile.keyword.MobileBuiltInKeywords as Mobile
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testcase.TestCase
import com.kms.katalon.core.testdata.TestData
import com.kms.katalon.core.testobject.TestObject
import com.kms.katalon.core.webservice.keyword.WSBuiltInKeywords as WS
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import com.kms.katalon.core.windows.keyword.WindowsBuiltinKeywords as Windows
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import internal.GlobalVariable

public class XmlUtilities {

	public static NodeList getNodesByTagName(String filePath,String Tag) {
		File xmlFile = new File(filePath);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(xmlFile);
		doc.getDocumentElement().normalize();
		return doc.getElementsByTagName(Tag);
	}

	public static void updateNodeText(String filePath, String nodeName, String newText) {
		try {
			// Load and parse the XML file
			File xmlFile = new File(filePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			// Get all elements with the specified node name
			NodeList nodeList = doc.getElementsByTagName(nodeName);

			// Loop through all the nodes and update the text content of the first matching node
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);

				// Ensure the node is an element node
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;

					// Update the text content of the node
					element.setTextContent(newText);
					System.out.println("Updated " + nodeName + " text to: " + newText);

					// After updating, break the loop if only the first occurrence should be updated
					break;
				}
			}

			// Write the changes back to the file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "1");
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(filePath));
			transformer.transform(source, result);

			System.out.println("File updated successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void overwriteLocatorXML(String locatorPath, String locatorVal) {
		String locatorName =  getNodesByTagName(locatorPath, "name").toString().trim();

		String xml = getTestObjectXML(locatorName, locatorVal,"XPATH");

		writeToFile(locatorPath,xml)
	}

	public static void writeToFile(String path, String content) {
		// Using try-with-resources to ensure the writer is closed properly
		try  {
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8)
			writer.write(content); // Write the content to the file
			System.out.println("File written successfully.");
		} catch (IOException e) {
			System.err.println("An error occurred while writing to the file: " + e.getMessage());
		}
	}
	public static String getTestObjectXML(String name, String locator, String locatorStrategy) {
		return	'<?xml version="1.0" encoding="UTF-8"?>'
		+"<MobileElementEntity>"
		+  "<description></description>"
		+ "<name>${name}</name>"
		+"<tag></tag>"
		+"<elementGuidId>53c2e099-a899-4b16-ae17-9e2502314517</elementGuidId>"
		+"<selectorMethod>BASIC</selectorMethod>"
		+"<smartLocatorEnabled>false</smartLocatorEnabled>"
		+"<useRalativeImagePath>false</useRalativeImagePath>"
		+"<locator>${locator}</locator>"
		+"<locatorStrategy>${locatorStrategy}</locatorStrategy>"
		+"</MobileElementEntity>";
	}
}


