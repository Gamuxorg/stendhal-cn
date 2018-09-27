/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Dice;
import games.stendhal.server.entity.mapstuff.sign.Sign;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.CroupierNPC;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import marauroa.common.Pair;

public class DiceGambling extends AbstractQuest {

	private static final int STAKE = 100;

	@Override
	public String getSlotName() {
		return "dice_gambling";
	}

	@Override
	public void addToWorld() {

		final CroupierNPC ricardo = (CroupierNPC) SingletonRepository.getNPCList().get("里卡多");

		final Map<Integer, Pair<String, String>> prizes = initPrices();

		ricardo.setPrizes(prizes);

		final StendhalRPZone zone = ricardo.getZone();

		Sign blackboard = new Sign();
		blackboard.setPosition(25, 0);
		blackboard.setEntityClass("blackboard");
		StringBuilder prizelistBuffer = new StringBuilder("PRIZES:\n");
		for (int i = 18; i >= 13; i--) {
			prizelistBuffer.append("\n" + i + ": " + prizes.get(i).first());
		}
		blackboard.setText(prizelistBuffer.toString());
		zone.add(blackboard);

		blackboard = new Sign();
		blackboard.setPosition(26, 0);
		blackboard.setEntityClass("blackboard");
		prizelistBuffer = new StringBuilder("PRIZES:\n");
		for (int i = 12; i >= 7; i--) {
			prizelistBuffer.append("\n" + i + ": " + prizes.get(i).first());
		}
		blackboard.setText(prizelistBuffer.toString());
		zone.add(blackboard);

		ricardo.add(ConversationStates.ATTENDING, "play", null,
				ConversationStates.QUESTION_1,
				"想要开始游戏, 需要压注 " + STAKE
						+ " 金币, 你确定压吗？", null);

		ricardo.add(ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("money", STAKE),
			ConversationStates.ATTENDING,
			"OK, 这是骰子, 准备好了就扔出来, 祝你好运!",
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					player.drop("money", STAKE);
					final Dice dice = (Dice) SingletonRepository.getEntityManager()
							.getItem("dice");
					dice.setCroupierNPC((CroupierNPC) npc.getEntity());
					player.equipOrPutOnGround(dice);
				}
			});

		ricardo.add(ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES,
			new NotCondition(new PlayerHasItemWithHimCondition("money", STAKE)),
			ConversationStates.ATTENDING,
			"喂! 你的钱不够啊!", null);

		ricardo.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"胆小鬼！你这么胆小怎么能成英雄！",
			null);

		fillQuestInfo(
				"骰子游戏",
				"在塞门镇酒店的赌上碰碰运气.",
				true);
	}

	private Map <Integer, Pair<String, String>> initPrices() {
		Map<Integer, Pair<String, String>> map = new HashMap<Integer, Pair<String, String>>();
		map.put(3, new Pair<String, String>("蓝盾",
				"小哥, 你真是狗屎运！我对你很失望！给你, 把这 蓝盾 拿走吧."));
		map.put(7, new Pair<String, String>("啤酒",
				"这是安慰奖, 一瓶啤酒."));
		map.put(8, new Pair<String, String>("红酒",
				"你赢了一杯可口的红酒！"));
		map.put(9, new Pair<String, String>("镶嵌盾",
				"这个简单的盾是给你的奖励"));
		map.put(10, new Pair<String, String>("索链护腿",
				"我希望你能用到这些 索链护腿."));
		map.put(11,	new Pair<String, String>("抗毒药济",
			   "当你在野外中毒时, 这瓶解毒济可帮上大忙"));
		map.put(12, new Pair<String, String>("三明治",
				"你赢了一块美味的三明治！"));
		map.put(13, new Pair<String, String>("芝士狗",
				"把这个好吃的 cheesydog 带走吧！"));
		map.put(14, new Pair<String, String>("回城卷",
		"你赢到了这个好用的回城卷！"));
		map.put(15,	new Pair<String, String>("大治疗剂",
				"你赢了一瓶大生命药济, 但你以前或许从来没用过这个!"));
		map.put(16,	new Pair<String, String>("longbow",
		"拿着这个拉风的长弓, 你会是个优秀的弓箭手！"));
		map.put(17,	new Pair<String, String>("红斗篷",
		"穿上这件时尚的 红斗篷 你一定变得很酷！"));
		map.put(18, new Pair<String, String>("附魔索链头盔",
				"你得到了特等奖！一个 附魔索链头盔!"));

		return map;
	}

	@Override
	public String getName() {
		return "DiceGambling";
	}

	@Override
	public boolean isVisibleOnQuestStatus() {
		return false;
	}

	@Override
	public List<String> getHistory(final Player player) {
		return new ArrayList<String>();
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "里卡多";
	}

}
