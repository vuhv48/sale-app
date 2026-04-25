package com.klb.app.application.service.impl.mail;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Thay the chuoi dang {{key}} trong mau mail.
 */
public final class MailTemplateRenderer {

	private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([^}]+)}}");

	private MailTemplateRenderer() {
	}

	public static String render(String template, Map<String, String> variables) {
		if (template == null) {
			return "";
		}
		StringBuilder out = new StringBuilder();
		Matcher m = PLACEHOLDER.matcher(template);
		while (m.find()) {
			String key = m.group(1).trim();
			String value = variables.getOrDefault(key, "");
			m.appendReplacement(out, Matcher.quoteReplacement(value));
		}
		m.appendTail(out);
		return out.toString();
	}
}
