package vazkii.botania.fabric.data.xplat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;

import vazkii.botania.api.BotaniaAPI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class UpsideDownLanguageProvider extends FabricLanguageProvider {
	private static final Int2ObjectMap<String> TRANSLITERATION_MAP = new Int2ObjectLinkedOpenHashMap<>();
	private static final Pattern TOKEN_PATTERN = Pattern.compile("(?!§[0-9a-fk-or]|\\$\\(|%[%sd]).[^§$%]*|[§%].|\\$\\(.*?\\)");
	private static final Pattern FORMAT_CODE_PATTERN = Pattern.compile("§[0-9a-fk-or]");
	private static final Pattern PATCHOULI_PATTERN = Pattern.compile("\\$\\(.*?\\)");
	private static final Pattern STRING_FORMAT_PATTERN = Pattern.compile("%[%sd]");
	private static final Pattern PATCHOULI_LINK_START_PATTERN = Pattern.compile("\\$\\(l:.*?\\)");
	private static final String PATCHOULI_LINK_END_CODE = "$(/l)";
	private static final Set<String> FORMAT_CODES;
	private static final Set<String> PATCHOULI_COLOR_CODES;
	private static final Set<String> PATCHOULI_FORMAT_CODES;

	static {
		//noinspection SpellCheckingInspection
		String originalChars = " abcdefghijklmnopqrstuvwxyzöüABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789()[]<>|%$#&?^!-+*~.,;':/❤\"";
		// TODO: test transliteration characters in game
		//noinspection SpellCheckingInspection
		String translitChars = " ɐqɔpǝɟᵷɥᴉɾʞꞁɯuodbɹsʇnʌʍxʎzonⱯᗺƆᗡƎℲ⅁HIꓩꞰꞀWNOԀꝹᴚS⟘∩ɅMX⅄Z0⥝↊Ɛ߈ϛ9\uD835\uDE1386)(][><|%$#⅋¿˯¡-+*~˙'⸵,:/❤„";

		int orgIndex = 0;
		Pattern combinedCharacterPattern = Pattern.compile("\\X");
		Matcher transMatcher = combinedCharacterPattern.matcher(translitChars);
		while (orgIndex < originalChars.length() && transMatcher.find()) {
			int original = originalChars.codePointAt(orgIndex);
			String transliterated = transMatcher.group();
			BotaniaAPI.LOGGER.info("Mapping for upside-down strings: '{}' -> '{}'", Character.toString(original), transliterated);
			TRANSLITERATION_MAP.put(original, transliterated);
			orgIndex += original > Character.MAX_VALUE ? 2 : 1;
		}

		FORMAT_CODES = Collections.unmodifiableSet(new LinkedHashSet<>(
				"r0123456789abcdefklmno".chars().mapToObj(c -> "§" + Character.toString(c)).toList()));
		PATCHOULI_COLOR_CODES = Collections.unmodifiableSet(new LinkedHashSet<>(
				Stream.concat(Stream.of("0", "item", "thing"), "123456789abcdef".chars()
						.mapToObj(Character::toString)).map("$(%s)"::formatted).toList()));
		PATCHOULI_FORMAT_CODES = Collections.unmodifiableSet(new LinkedHashSet<>(
				Stream.concat(Stream.of(""), "klmno".chars()
						.mapToObj(Character::toString)).map("$(%s)"::formatted).toList()));
	}

	public UpsideDownLanguageProvider(FabricDataOutput dataOutput) {
		super(dataOutput, "en_ud");
	}

	@Override
	public void generateTranslations(TranslationBuilder translationBuilder) {
		Map<String, String> elementsToConvert = loadDefaultLanguage();
		translationBuilder.add("!note", "This file was generated automatically from en_us.json");
		for (Map.Entry<String, String> entry : elementsToConvert.entrySet()) {
			String key = entry.getKey();
			if (key.startsWith("_")) {
				continue;
			}
			String value = entry.getValue();
			translationBuilder.add(key, translate(value));
		}
	}

	private static String translate(String str) {
		List<String> tokens = tokenize(str);
		Collections.reverse(tokens);
		rotateColorCodes(tokens, FORMAT_CODES);
		rotateColorCodes(tokens, PATCHOULI_COLOR_CODES);
		rotateColorCodes(tokens, PATCHOULI_FORMAT_CODES);
		fixPatchouliLinks(tokens);
		StringBuilder sb = new StringBuilder(str.length());
		for (String token : tokens) {
			if (isFormat(token)) {
				sb.append(token);
				continue;
			}
			for (int i = token.length() - 1; i >= 0; i--) {
				if (Character.isLowSurrogate(token.charAt(i))) {
					// hit the second part of a Unicode code point consisting of multiple characters
					continue;
				}
				int c = token.codePointAt(i);
				if (TRANSLITERATION_MAP.containsKey(c)) {
					sb.append(TRANSLITERATION_MAP.get(c));
				} else {
					BotaniaAPI.LOGGER.info("Missing transliteration mapping for '{}' ({}) in '{}'", Character.toString(c), c, token);
					sb.append(Character.toChars(c));
				}
			}
		}
		return sb.toString();
	}

	private static void fixPatchouliLinks(List<String> tokens) {
		int swapPos = -1;
		for (int pos = 0; pos < tokens.size(); pos++) {
			String token = tokens.get(pos);
			if (token.equals(PATCHOULI_LINK_END_CODE)) {
				swapPos = pos;
			} else if (swapPos != -1 && PATCHOULI_LINK_START_PATTERN.matcher(token).matches()) {
				tokens.set(swapPos, token);
				tokens.set(pos, PATCHOULI_LINK_END_CODE);
				swapPos = -1;
			}
		}
	}

	private static void rotateColorCodes(List<String> tokens, Set<String> codes) {
		IntList codePositions = new IntArrayList();
		for (int i = 0; i < tokens.size(); i++) {
			if (codes.contains(tokens.get(i))) {
				codePositions.add(i + 1);
			}
		}
		if (codePositions.isEmpty()) {
			return;
		}
		// TODO: needs to model actual formatting results to properly handle non-color formatting
		String reset = codes.stream().findFirst().orElseThrow();
		tokens.add(0, reset);
		int lastPos = 0;
		for (int pos : codePositions) {
			tokens.set(lastPos, tokens.get(pos));
			lastPos = pos;
		}
		tokens.set(lastPos, reset);
		if (tokens.get(0).equals(reset)) {
			tokens.remove(0);
		}
	}

	private static List<String> tokenize(String str) {
		Matcher matcher = TOKEN_PATTERN.matcher(str);
		List<String> tokens = new ArrayList<>();
		while (matcher.find()) {
			tokens.add(matcher.group());
		}
		return tokens;
	}

	private static boolean isFormat(String token) {
		return switch (token.charAt(0)) {
			case '§' -> FORMAT_CODE_PATTERN.matcher(token).matches();
			case '$' -> PATCHOULI_PATTERN.matcher(token).matches();
			case '%' -> STRING_FORMAT_PATTERN.matcher(token).matches();
			default -> false;
		};
	}

	private static Map<String, String> loadDefaultLanguage() {
		String path = "assets/botania/lang/en_us.json";
		JsonElement jsonElement = loadJsonResource(path);
		if (jsonElement == null) {
			throw new IllegalStateException(String.format("Could not find default lang file: %s", path));
		}
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		Map<String, String> translationEntries = new LinkedHashMap<>();
		for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue().getAsString();
			translationEntries.put(key, value);
		}
		return translationEntries;
	}

	private static JsonElement loadJsonResource(String filepath) {
		InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream(filepath);
		if (systemResourceAsStream == null) {
			return null;
		}
		try (JsonReader reader = new JsonReader(new BufferedReader(new InputStreamReader(systemResourceAsStream)))) {
			reader.setLenient(true);
			return Streams.parse(reader);
		} catch (IOException e) {
			BotaniaAPI.LOGGER.error("Error reading JSON stream", e);
			return null;
		}
	}

}
