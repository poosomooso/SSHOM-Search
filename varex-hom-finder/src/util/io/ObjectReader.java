package util.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class ObjectReader {
	
	private  ObjectReader() {
	}

	public static Object readObject(File file) throws IOException {
		System.out.println("read from: " + file.getName());
		try (ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			try {
				return in.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
}
