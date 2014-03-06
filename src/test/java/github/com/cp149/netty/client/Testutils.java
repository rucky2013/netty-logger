package github.com.cp149.netty.client;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class Testutils {

	/**
	 * @param filename
	 * @return
	 * @throws IOException
	 *             count how many lines a file
	 */
	public static int countlines(String filename) throws IOException {
		LineNumberReader lnr = new LineNumberReader(new FileReader(new File(filename)));
		lnr.skip(Long.MAX_VALUE);
		lnr.close();
		return (lnr.getLineNumber());
	}

}
