/***************************************************************************
 *                      (C) Copyright 2011 - Stendhal                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.common.grammar;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Manage the rules for building full forms from words
 * by prefixing with word phrases like "piece of".
 */
final class PrefixManager
{
	/**
	 * Entry for registering singular/plural prefixes for a keyword.
	 */
	private static class PrefixEntry {
		public final String keyword;
		public final String prefixPlural;
		public final String prefixSingular;

		public PrefixEntry(final String keyword, final String prefixSingular, final String prefixPlural) {
			this.keyword = keyword;
			this.prefixSingular = prefixSingular;
			this.prefixPlural = prefixPlural;
		}
	}


	public static PrefixManager s_instance = new PrefixManager();

	private Collection<String> pluralPrefixes = new HashSet<String>();

	private Collection<PrefixEntry> prefixEndList = new ArrayList<PrefixEntry>();


	private Map<String, PrefixEntry> prefixMap = new HashMap<String, PrefixEntry>();

	private Collection<String> singularPrefixes = new HashSet<String>();

	/**
	 * Initialise the map of nouns and prefix expressions.
	 */
//	public PrefixManager() {
//		register("piece of ", "pieces of ", "meat");
//		register("piece of ", "pieces of ", "火腿");
//		register("piece of ", "pieces of ", "cheese");
//		register("piece of ", "pieces of ", "wood");
//		register("piece of ", "pieces of ", "paper");
//		register("piece of ", "pieces of ", "iron");
//		register("piece of ", "pieces of ", "chicken");
//		register("piece of ", "pieces of ", "coal");
//		register("piece of ", "pieces of ", "beeswax");
//
//		register("sack of ", "sacks of ", "面粉");
//		register("sack of ", "sacks of ", "糖块");
//
//		register("sheaf of ", "sheaves of ", "小麦");
//		register("loaf of ", "loaves of ", "面包");
//		register("stick of ", "sticks of ", "奶油");
//		register("bulb of ", "bulbs of ", "大蒜");
//		register("jar of ", "jars of ", "蜂蜜");
//		register("glass of ", "glasses of ", "红酒");
//		register("cup of ", "cups of ", "茶");
//		register("sprig of ", "sprigs of ", "海芋");
//		register("root of ", "roots of ", "mandragora");
//		register("bunch of ", "bunches of ", "daisies");
//		register("bunch of ", "bunches of ", "grapes");
//		register("can of ", "cans of ", "oil");
//
//		register("bottle of ", "bottles of ", "啤酒");
//		register("bottle of ", "bottles of ", "水");
//		register("bottle of ", "bottles of ", "沸水");
//		register("bottle of ", "bottles of ", "牛奶");
//		register("bottle of ", "bottles of ", "holy water");
//
//		registerEnd("bottle of ", "bottles of ", "治疗济");
//		registerEnd("bottle of ", "bottles of ", "毒药");
//		registerEnd("bottle of ", "bottles of ", "抗毒药济");
//		registerEnd("nugget of ", "nuggets of ", " ore");
//
//		registerEnd("pair of ", "pairs of ", " legs");
//		registerEnd("pair of ", "pairs of ", " boots");
//
//		registerEnd("spool of ", "spools of ", " thread");
//
//		registerPrefix("suit of ", "suits of "); // "armor"
//	}


	/**
	 * Prefix one of the registered nouns with an expression like "piece of".
	 *
	 * @param str noun to process
	 * @param lowString lowercase version of str
	 * @return noun with prefix
	 */
//	public String fullForm(final String str, final String lowString) {
//		String ret = lowString;
//
//		PrefixEntry found = prefixMap.get(str);
//
//		if (found != null) {
//			ret = found.prefixSingular + ret;
//		} else {
//			for(PrefixEntry entry : prefixEndList) {
//				if (str.endsWith(entry.keyword)) {
//					ret = Grammar.addPrefixIfNotAlreadyThere(ret, entry.prefixSingular, entry.prefixPlural);
//					break;
//				}
//			}
//		}

//		return ret;
//	}

	/**
	 * @return collection of all registered plural prefixes.
	 */
	public Collection<String> getPluralPrefixes() {
		return pluralPrefixes;
	}
	/**
	 * @return collection of all registered singular prefixes.
	 */
	public Collection<String> getSingularPrefixes() {
		return singularPrefixes;
	}

	/**
	 * Define the singular and plural prefix strings for an item name with full match,
	 * for example "piece of paper".
	 * @param prefixSingular
	 * @param prefixPlural
	 * @param noun
	 */
	private void register(final String prefixSingular, final String prefixPlural, final String noun) {
		prefixMap.put(noun, new PrefixEntry(noun, prefixSingular, prefixPlural));

		registerPrefix(prefixSingular, prefixPlural);
	}
	/**
	 * Define the singular and plural prefix strings for an item name to be matched at the end,
	 * for example "bottle of ... 治疗济".
	 * @param prefixSingular
	 * @param prefixPlural
	 * @param endString
	 */
	private void registerEnd(final String prefixSingular, final String prefixPlural, final String endString) {
		prefixEndList.add(new PrefixEntry(endString, prefixSingular, prefixPlural));

		registerPrefix(prefixSingular, prefixPlural);
	}


	/**
	 * Register a pair of singular and plural prefix strings to be removed
	 * when parsing item names, for example "suits of leather armor".
	 * @param prefixSingular
	 * @param prefixPlural
	 */
	private void registerPrefix(final String prefixSingular, final String prefixPlural) {
		singularPrefixes.add(prefixSingular);
		pluralPrefixes.add(prefixPlural);
	}
}
