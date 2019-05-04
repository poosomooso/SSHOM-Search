package util;

import java.lang.reflect.Field;

/**
 * Initialize configuration options from System config.<br>
 * use java -Doption="value" (e.g., -Dcovereage=true)
 * @author Jens Meinicke
 *
 */
public final class ConfigLoader {

	private ConfigLoader() {
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void initialize(Class<?> klass) {
		System.out.println("### initialize: " + klass);
		try {
			Field[] flags = klass.getDeclaredFields();
			for (Field flag : flags) {
				flag.setAccessible(true);
				final Object value;
				if (flag.getType() == boolean.class) {
					String valueString = System.getProperty(flag.getName(), Boolean.toString(flag.getBoolean(null)));
					value = Boolean.parseBoolean(valueString);
				} else if (flag.getType() == int.class) {
					String valueString = System.getProperty(flag.getName(), Integer.toString(flag.getInt(null)));
					value = Integer.parseInt(valueString);
				} else if (flag.getType().isEnum()) {
					String valueString = System.getProperty(flag.getName(), flag.get(null).toString());
					value = Enum.valueOf((Class<Enum>) flag.getType(), valueString);
				} else {
					System.err.println("Flag not supported: " + flag);
					continue;
				}
				flag.set(null, value);
				System.out.println("### " + flag.getName() + ": " + flag.get(null));
			}
				
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		System.out.println("###################################");
	}
}
