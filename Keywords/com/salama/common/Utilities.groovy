package com.salama.common

import java.awt.Desktop
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask

import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent
import javax.sound.sampled.LineListener
import javax.sound.sampled.LineUnavailableException
import javax.sound.sampled.UnsupportedAudioFileException

import com.kms.katalon.core.configuration.RunConfiguration

import internal.GlobalVariable


public class Utilities {
	//TODO refactor these into an enum
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_LIGHT_YELLOW = "\u001B[93m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	public static final String ANSI_BOLD = "\u001B[1m";
	public static final String ANSI_UNBOLD = "\u001B[21m";
	public static final String ANSI_UNDERLINE = "\u001B[4m";
	public static final String ANSI_STOP_UNDERLINE = "\u001B[24m";
	public static final String ANSI_BLINK = "\u001B[5m";

	private static URL getResourceURL(String resourceName) {
		return com.salama.common.Utilities.class.getClassLoader().getResource("Resources/"+resourceName)
	}

	public static void playWavFile(String resourceName) {
		new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							// Load the audio file
							//def file = new File(getResourceURL())
							AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getResourceURL(resourceName));
							Clip clip = AudioSystem.getClip(); // Get a clip resource
							clip.open(audioInputStream); // Open the audio input stream
							clip.start(); // Start playing the audio

							// Add a listener to know when the clip has finished playing
							clip.addLineListener(new LineListener() {
										@Override
										public void update(LineEvent event) {
											if (event.getType() == LineEvent.Type.STOP) {
												clip.close(); // Close the clip when done
											}
										}
									});
						} catch (UnsupportedAudioFileException e) {
							System.err.println("The specified audio file is not supported.");
							e.printStackTrace();
						} catch (IOException e) {
							System.err.println("Error playing the audio file.");
							e.printStackTrace();
						} catch (LineUnavailableException e) {
							System.err.println("Audio line for playing back is unavailable.");
							e.printStackTrace();
						}
					}
				}).start(); // Start the thread to play audio
	}



	public static String readLineWithTimeout(long timeoutMillis, String defaultVal) {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		FutureTask<String> futureTask = new FutureTask<>(new Callable<String>() {
					@Override
					public String call() throws Exception {
						return reader.readLine();
					}
				});

		Thread taskThread = new Thread(futureTask);
		taskThread.start();

		try {
			return futureTask.get(timeoutMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
		} catch (java.util.concurrent.TimeoutException e) {
			taskThread.interrupt(); // Interrupt the thread if timeout occurs
			return defaultVal;
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return defaultVal; // Return default value on error
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

	public static void createDirectoryIfNotExists(String dirPath) {
		Path path = Paths.get(dirPath);

		try {
			if (!Files.exists(path)) {
				Files.createDirectories(path);
				System.out.println("Directory created: " + dirPath);
			} else {
				System.out.println("Directory already exists: " + dirPath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void createFileIfNotExists(String dirPath) {
		Path path = Paths.get(dirPath);

		try {
			if (!Files.exists(path)) {
				Files.createFile(path);
				System.out.println("File created: " + dirPath);
			} else {
				System.out.println("File already exists: " + dirPath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void printLine(String line, String color= ANSI_BLACK) {

		String YELLOW = color;
		String RESET = "\u001B[0m";
		System.out.println("${color}${line}${ANSI_RESET}");
	}

	public static String getExecutionOS() {
		if(RunConfiguration.getExecutionProperties().get("drivers").toString().toLowerCase().contains("ios"))
			return "ios";
		else if(RunConfiguration.getExecutionProperties().get("drivers").toString().toLowerCase().contains("droid"))
			return "android";
		else
			return "browser";
	}

	public static Object getGlobalVarIfExist(String varName) {
		try {
			GlobalVariable.getAt(varName);
		}
		catch (Exception e) {
			return null;
		}
	}

	public static Object getResourceFile(String fileName) {

		return Utilities.class.getClassLoader().getResource(fileName);
	}
}
