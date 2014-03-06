package github.com.cp149.netty.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MvnCommandexe {

	/**
	 * @param arg
	 * @return execute commands in new jvm
	 */
	public int executeCommands(String... arg) {
		try {
			String path = System.getenv("PATH");
			ProcessBuilder pb = new ProcessBuilder(arg);
			// String libPath = System.getProperty("java.library.path");

			pb.environment().put("PATH", path);
			pb.redirectErrorStream(true);
			Process process = pb.start();

			InputStream fis = process.getInputStream();

			InputStreamReader isr = new InputStreamReader(fis);

			BufferedReader br = new BufferedReader(isr);
			String line = null;

			while ((line = br.readLine()) != null) {
				if (line.contains("time="))
					System.out.println(line);
			}
			return 0;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
