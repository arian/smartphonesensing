package nl.tudelft.followbot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.util.Log;

public class LogFile {

	private static String tag = "LOG_FILE";

	public static void appendLog(File logFile, String text) {

		if (!logFile.exists()) {
			Log.d(tag, "Creating file " + logFile.getAbsolutePath());
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				Log.e(tag, e.getMessage());
			}
		} else {
			Log.d(tag, "Existing file " + logFile.getAbsolutePath());
		}
		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));
			buf.append(text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			Log.e(tag, e.getMessage());
		}
	}
}
