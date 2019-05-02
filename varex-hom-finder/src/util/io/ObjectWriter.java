package util.io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ObjectWriter {
	
	private ObjectWriter() {
	}

	public static void writeObject(Serializable object, File file) throws IOException {
		System.out.println("write to: " + file.getName());
		if (!file.exists()) {
			file.createNewFile();
		}
		try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
			out.writeObject(object);
		}
	}
	
}
