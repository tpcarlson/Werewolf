package no.arcticdrakefox.wolfbot.management;

import java.util.MissingResourceException;
import java.util.Random;
import java.util.ResourceBundle;

public class Messages {
	private static final String BUNDLE_NAME = "no.arcticdrakefox.wolfbot.roles.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);
	
	private static final Random rng = new Random();

	private Messages() {
	}

	public static String getString(String key) {
		try {
			int count = Integer.parseInt(RESOURCE_BUNDLE.getString(key + ".count"));
			int rnd = rng.nextInt(count);
			return RESOURCE_BUNDLE.getString(key+"."+rnd);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
