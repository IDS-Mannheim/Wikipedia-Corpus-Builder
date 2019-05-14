package de.mannheim.ids.config;

import java.util.regex.Pattern;

public class PostingPatterns {

	public static final String USER_PAGE_EN = "User|user";
	public static final String USER_TALK_EN = "User talk|User_talk|User "
			+ "Talk|User_Talk|user talk |user_talk";
	public static final String SPECIAL_CONTRIBUTIONS_EN = "Special:"
			+ "Contributions|special:contributions";
	public static final String SIGNATURE_EN = "wikipedia:signatures";
	public static final String UNSIGNED_EN = "Unsigned|unsigned";

	private Pattern signaturePattern, signaturePattern2, userTalkPattern,
			specialContribution, unsignedPattern, unsignedPattern2;

	public PostingPatterns() {}

	public PostingPatterns(String languageCode, String userPage,
			String userTalk, String contributions, String unsigned) {
		createSignaturePattern(languageCode, userPage);
		createUserTalkPattern(languageCode, userTalk);
		createSpecialContribution(languageCode, contributions);
		createUnsignedPattern(languageCode, unsigned);
	}

	private String extendKeyword(String languageCode, String keyword,
			String en_keyword) {
		if (!languageCode.equals("en")) {
			StringBuilder sb = new StringBuilder(keyword);
			sb.append("|");
			sb.append(keyword.toLowerCase());
			sb.append("|");
			if (keyword.contains(" ")) {
				String underscore = keyword.replace(" ", "_");
				sb.append(underscore);
				sb.append("|");
				sb.append(underscore.toLowerCase());
				sb.append("|");
			}
			sb.append(en_keyword);
			keyword = sb.toString();
		}
		else {
			keyword = en_keyword;
		}
		return keyword;
	}

	public void createSignaturePattern(String languageCode,
			String userPage) {
		userPage = extendKeyword(languageCode, userPage, USER_PAGE_EN);
		signaturePattern = Pattern.compile("(.*-{0,2})\\s*\\[\\[:?(("
				+ userPage + "):[^\\]]+)\\]\\](.*)");
		signaturePattern2 = Pattern.compile("(.*-{0,2})\\s*\\[\\[:?(("
				+ userPage + "):[^/]+\\|[^\\]]+)\\]\\](.*)");
	}

	public void createUserTalkPattern(String languageCode, String userTalk) {
		userTalk = extendKeyword(languageCode, userTalk, USER_TALK_EN);
		userTalkPattern = Pattern.compile("(.*)\\[\\[((" + userTalk
				+ "):[^\\]]+)\\]\\](.*)");
	}

	public void createSpecialContribution(String languageCode,
			String contributions) {
		contributions = extendKeyword(languageCode, contributions,
				SPECIAL_CONTRIBUTIONS_EN);
		specialContribution = Pattern.compile("(.*)\\[\\[((" + contributions
				+ ")/[^\\|]+)\\|([^\\]]+)\\]\\](.*)");
	}

	public void createUnsignedPattern(String languageCode, String unsigned) {
		unsigned = extendKeyword(languageCode, unsigned, UNSIGNED_EN);
		unsignedPattern = Pattern.compile("(.*)\\{\\{(" + unsigned
				+ ")\\}\\}(.*)");
		unsignedPattern2 = Pattern.compile("(.*)\\{\\{(" + unsigned
				+ ")\\|([^\\|\\}]+)\\|?(.*)\\}\\}(.*)");
	}

	public Pattern getSignaturePattern() {
		return signaturePattern;
	}

	public void setSignaturePattern(Pattern signaturePattern) {
		this.signaturePattern = signaturePattern;
	}

	public Pattern getSignaturePattern2() {
		return signaturePattern2;
	}

	public void setSignaturePattern2(Pattern signaturePattern2) {
		this.signaturePattern2 = signaturePattern2;
	}

	public Pattern getUserTalkPattern() {
		return userTalkPattern;
	}

	public void setUserTalkPattern(Pattern userTalkPattern) {
		this.userTalkPattern = userTalkPattern;
	}

	public Pattern getSpecialContribution() {
		return specialContribution;
	}

	public void setSpecialContribution(Pattern specialContribution) {
		this.specialContribution = specialContribution;
	}

	public Pattern getUnsignedPattern() {
		return unsignedPattern;
	}

	public void setUnsignedPattern(Pattern unsignedPattern) {
		this.unsignedPattern = unsignedPattern;
	}

	public Pattern getUnsignedPattern2() {
		return unsignedPattern2;
	}

	public void setUnsignedPattern2(Pattern unsignedPattern2) {
		this.unsignedPattern2 = unsignedPattern2;
	}
}
