# Katalon Maintenance Mode

## What is Katalon Maintenance Mode?

Katalon Maintenance Mode enhances your testing experience by allowing you to:

1. **Retry finding objects** and correct their locator during test execution without having to restart the test. This feature is available through `ObjectRepositoryExtensions`.
2. **Re-execute specific test cases** during execution or jump between test cases using the MaintenanceTestSuiteExecutor.

---
## Installation 
1. Download the latest **([KatalonMaintenanceMode](https://github.com/fsalamah/KatalonMaintenanceMode/releases/download/release/KatalonMaintenanceMode-1.0.0.jar))** from releases and place it your project "Drivers" directory
2. Replace all text instances of:
      ```
      com.kms.katalon.core.testobject.ObjectRepository.findTestObject
      ```
      with
      ```
      com.salama.maintenance.ObjectRepositoryExtensions.findTestObject
      ```
      

3. Add the following variables to your execution profile:
   ```java
   Variable Name            Type      Value
   maintenance_mode         Boolean    true
   maintenance_timeout_ms   number     5000
   ```
## How to Use Maintenance Mode for Updating Objects During Test Execution

Follow these steps to enable Maintenance Mode:
1. Open the Katalon Studio console to see the options whenever an object is not found.

## How to Use the Test Suite Maintenance Executor
To utilize the Test Suite Maintenance Executor:

1. Create an empty test case.
2. Call the execute method from TestSuiteMaintenanceExecuter, passing the test suite ID as a parameter, for example:
   ```java
   TestSuiteMaintenanceExecuter.execute("Test Suites/New Test Suite");
   ```
3. Open the console in Katalon studio and choose the execution mode
