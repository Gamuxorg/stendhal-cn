/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Common phrases used by players to interact with a SpeakerNPC.
 *
 * @author hendrik
 */
public class ConversationPhrases {

	// define "no" trigger to be exactly matched while ignoring case
	// (for available matching option strings see ExpressionMatcher)
	public static final String NO_EXPRESSION = "|EXACT|ICASE|no";

	// do not use a mutable list here
	public static final List<String> EMPTY = Arrays.asList(new String[0]);

	public static final List<String> GREETING_MESSAGES = Arrays.asList("hi",
			"hello", "喂", "嗨", "你好","您好");

	public static final List<String> JOB_MESSAGES = Arrays.asList("job", "work", "工作", "职业");

	public static final List<String> HELP_MESSAGES = Arrays.asList("help",
			"帮助","求助","帮忙","帮我");

	public static final List<String> QUEST_MESSAGES = Arrays.asList("task",
			"quest", "favor", "favour", "任务","调查","需求","好感","喜好");

	public static final List<String> FINISH_MESSAGES = Arrays.asList("done",
			"finish", "complete","完成","好了","已完成");

	public static final List<String> QUEST_FINISH_MESSAGES = combine(QUEST_MESSAGES, FINISH_MESSAGES);

	public static final List<String> ABORT_MESSAGES = Arrays.asList("another", "abort","其他","其它","取消","另外");

	public static final List<String> OFFER_MESSAGES = Arrays.asList("offer", "deal", "trade","生意","交易","价格","报价","支付");

	public static final List<String> YES_MESSAGES = Arrays.asList("yes", "ok", "好的", "可以","好","是","中","行","要");

	public static final List<String> NO_MESSAGES = Arrays.asList(NO_EXPRESSION, "nope",
			"nothing", "none","不","不是","没有","不行","不好");

	public static final List<String> GOODBYE_MESSAGES = Arrays.asList("bye", "goodbye",
			"再见", "走了", "退出","拜拜");

	public static final List<String> BUY_MESSAGES = Arrays.asList("buy", "买","购买","买入","买进","我买", "我要","我要买");
	public static final List<String> SELL_MESSAGES = Arrays.asList("sell", "卖","我卖", "我要卖", "卖出");
	public static final List<String> HEAL_MESSAGES = Arrays.asList("heal", "治疗","医疗", "加血", "奶我","救命");



	/**
	 * Combine a string collection (list) with additional strings.
	 *
	 * @param list first collection of strings
	 * @param args additional strings
	 * @return new list with the contents of the list and all the additional
	 * 	strings
	 */
	public static final List<String> combine(Collection<String> list, String ...args) {
		List<String> ret = new ArrayList<String>(list);

		for(String s : args) {
			ret.add(s);
		}

		return ret;
	}

	/**
	 * Combine a string collection with other collections.
	 *
	 * @param list1 first collection
	 * @param lists additional collections
	 * @return a new list with contents of all the collections
	 */
	@SafeVarargs
	public static final List<String> combine(Collection<String> list1, Collection<String>... lists) {
		List<String> ret = new LinkedList<String>(list1);
		for (Collection<String> list : lists) {
			ret.addAll(list);
		}
		return ret;
	}

}
