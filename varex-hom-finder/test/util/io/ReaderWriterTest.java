package util.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ReaderWriterTest {

	@Test
	public void teastReadWrite() throws Exception {
		Map<String, Integer> map = new HashMap<>();
		map.put("A", 1);
		map.put("B", 42);
		
		File file = new File(ReaderWriterTest.class.getSimpleName() + ".serial");
		boolean success = file.createNewFile();
		assertTrue(success);
		file.deleteOnExit();
		ObjectWriter.writeObject((Serializable)map, file);
		Object readObject = ObjectReader.readObject(file);
		assertEquals(map, readObject);
	}
}
