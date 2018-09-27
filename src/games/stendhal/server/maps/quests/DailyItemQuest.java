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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.MathHelper;
//import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropRecordedItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPDependentOnLevelAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemAction;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingRandomItemCollectionAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasRecordedItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Daily Item Fetch Quest.
 * <p>
 * PARTICIPANTS:
 * <li> Mayor of Ados
 * <li> some items
 * <p>
 * STEPS:
 * <li> talk to Mayor of Ados to get a quest to fetch an item
 * <li> bring the item to the mayor
 * <li> if you cannot bring it in one week he offers you the chance to fetch
 * another instead
 * <p>
 * REWARD:
 * <li> xp
 * <li> 10 Karma
 * <p>
 * REPETITIONS:
 * <li> once a day
 */
public class DailyItemQuest extends AbstractQuest {

	private static final String QUEST_SLOT = "daily_item";

	/** How long until the player can give up and start another quest */
	private static final int expireDelay = MathHelper.MINUTES_IN_ONE_WEEK;

	/** How often the quest may be repeated */
	private static final int delay = MathHelper.MINUTES_IN_ONE_DAY;

	/**
	 * All items which are possible/easy enough to find. If you want to do
	 * it better, go ahead. *
	 */
	private static Map<String,Integer> items;

	private static void buildItemsMap() {
		items = new HashMap<String, Integer>();

		// ammunition
		items.put("金箭",5);
		items.put("加强箭",5);
		items.put("铁箭",7);
		items.put("木箭",10);

		// armor
        items.put("蓝色盔甲",1);
		items.put("链甲",1);
		items.put("布衣",1);
		items.put("加固锁子甲",1);
		items.put("黄金锁子甲",1);
		items.put("铁鳞甲",1);
		items.put("皮甲",1);
		items.put("皮胸甲",1);
		items.put("皮鳞甲",1);
		items.put("护肩铁胸甲",1);
		items.put("护肩皮胸甲",1);
		items.put("板甲",1);
		items.put("鳞甲",1);
		items.put("镶嵌甲",1);

		// 重斧
		items.put("重斧",1);
		items.put("大砍刀",1);
		items.put("战斧",1);
		items.put("战戟",1);
		items.put("手斧",1);
		items.put("旧的大镰刀",1);
		items.put("大镰刀",1);
		items.put("镰刀",1);
		items.put("小斧头",1);
		items.put("双刃斧",1);

		// boots
        items.put("蓝靴子",1);
		items.put("链靴",1);
		items.put("皮靴",1);
		items.put("钉靴",1);

		// cloaks
		items.put("蓝灵斗篷",1);
		items.put("cloak",1);
		items.put("矮人斗篷",1);
		items.put("精灵斗篷",1);
		items.put("绿龙斗篷",1);

		// club
		items.put("木棍",1);
		items.put("加强狼牙棒",1);
		items.put("连枷",1);
		items.put("黄金战锤",1);
		items.put("黄金权杖",1);
		items.put("铁锤",1);
		items.put("权杖",1);
		items.put("破晓之星",1);
		items.put("法杖",1);
		items.put("大战锤",1);

		// container
		items.put("耳瓶",3);
		items.put("瓶子",5);
		items.put("细瓶子",5);

		// drinks
		items.put("抗毒药济",5);
		items.put("啤酒",10);
		items.put("大瓶抗毒药济",5);
		items.put("大治疗剂",5);
		items.put("牛奶",5);
		items.put("小治疗剂",5);
		items.put("毒药",5);
		items.put("治疗剂",5);
		items.put("茶",3);
		items.put("水",5);
		items.put("红酒",10);

		// flower
		items.put("雏菊",5);
		items.put("紫丁香",5);
		items.put("玫瑰",10);

		// food
		items.put("苹果",5);
		items.put("苹果派",2);
		items.put("洋蓟",10);
		items.put("面包",5);
		items.put("西蓝花",5);
		items.put("小圆菇",10);
		items.put("胡萝卜",10);
		items.put("洋花菜",5);
		items.put("char",5);
		items.put("干酪",10);
		items.put("樱桃",10);
		items.put("樱桃派",2);
		items.put("鸡腿",10);
		items.put("巧克力棒",5);
		items.put("clownfish",5);
		items.put("甘蓝",5);
		items.put("西葫芦",5);
		items.put("egg",1);
		items.put("仙女蛋糕", 5);
		items.put("鱼肉派",1);
		items.put("大蒜",5);
		items.put("葡萄",5);
		items.put("烤排",1);
		items.put("火腿",10);
		items.put("蜂蜜",2);
		items.put("韭菜",5);
		items.put("肉",10);
		items.put("洋葱",5);
		items.put("梨子",5);
		items.put("perch",5);
		items.put("馅饼",5);
		items.put("石榴",5);
		items.put("大脚菇",10);
		items.put("red lionfish",5);
		items.put("roach",5);
		items.put("色拉",10);
		items.put("三明治",5);
		items.put("菠菜",5);
		items.put("surgeonfish",5);
		items.put("红伞菇",15);
		items.put("西红柿",5);
		items.put("trout",5);
		items.put("橄榄",5);

		// helmet
        items.put("blue helmet",1);
		items.put("aventail",1);
		items.put("chain helmet",1);
		items.put("leather helmet",1);
		items.put("robins hat",1);
		items.put("studded helmet",1);
		items.put("viking helmet",1);

		// herb
		items.put("海芋",5);
		items.put("鼠尾草",5);
		items.put("曼德拉草",3);
		items.put("百里香",5);

		// legs
        items.put("蓝裤子",1);
		items.put("索链护腿",1);
		items.put("皮裤",1);
		items.put("镶嵌护腿",1);

		// misc
		items.put("dice",1);
		items.put("玻璃球", 2);
		items.put("捕兽夹",5);
		items.put("teddy",1);

		// money
		items.put("money",100);

		// ranged
		items.put("composite bow",1);
		items.put("longbow",1);
		items.put("木弓",1);

		// resource
		items.put("煤炭",10);
		items.put("面粉",5);
		items.put("小麦",20);
		items.put("金条",5);
		items.put("金砂",10);
		items.put("铁锭",5);
		items.put("铁矿石",10);
		items.put("木头",10);

		// shield
        items.put("蓝盾",1);
		items.put("小圆盾",1);
		items.put("王冠之盾",1);
		items.put("加强狮盾",1);
		items.put("狮盾",1);
		items.put("钢盾",1);
		items.put("骷髅盾",1);
		items.put("镶嵌盾",1);
		items.put("独角盾",1);
		items.put("木盾",1);

		// sword
		items.put("突刺剑",1);
		items.put("阔剑",1);
		items.put("双刃剑",1);
		items.put("dagger",1);
		items.put("精灵之剑",1);
		items.put("太刀",1);
		items.put("小刀",1);
		items.put("半月弯刀",1);
		items.put("短剑",1);
		items.put("sword",1);

		// tool
		items.put("pick",1);
		items.put("淘金盘",1);
	}

	private ChatAction startQuestAction() {
		// common place to get the start quest actions as we can both starts it and abort and start again

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new StartRecordingRandomItemCollectionAction(QUEST_SLOT,0,items,"Ados 城需根支援, 快去找 [item] 物资"
				+ " ,当你找到了, 回来说 #complete 完成任务"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));

		return new MultipleActions(actions);
	}

	private void getQuest() {
		final SpeakerNPC npc = npcs.get("查玛斯市长");
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,expireDelay))),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"你已经有了一个取得 [item] 物资的任务. "
						+ ". 如果你能回来, 回复 #complete 可完成!"));

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new TimePassedCondition(QUEST_SLOT,1,expireDelay)),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"你已有了一个取得 [item] 物资的任务. "
						+ ". 如果你把物资带回来, 对我说 #complete 完成它. 可能你根本没有那种物资剩下, 你也可以拿回 #another 物资, 或者带回我向你要的物资. "));

		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,delay))),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT,1, delay, "我一天只能交给你一次任务, 请核对任务记录. "));




		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new OrCondition(new QuestNotStartedCondition(QUEST_SLOT),
								new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
												 new TimePassedCondition(QUEST_SLOT,1,delay))),
				ConversationStates.ATTENDING,
				null,
				startQuestAction());
	}

	private void completeQuest() {
		final SpeakerNPC npc = npcs.get("查玛斯市长");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"恐怕我还没有交给你任何 #quest 任务. ",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"你已完成我交给你的全部任务. ",
				null);

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new DropRecordedItemAction(QUEST_SLOT,0));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new IncrementQuestAction(QUEST_SLOT, 2, 1));
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "done"));
		actions.add(new IncreaseXPDependentOnLevelAction(8, 90.0));
		actions.add(new IncreaseKarmaAction(10.0));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0)),
				ConversationStates.ATTENDING,
				"干的好! 我代表Ados城的人民对你表示感谢. ",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
								 new NotCondition(new PlayerHasRecordedItemWithHimCondition(QUEST_SLOT,0))),
				ConversationStates.ATTENDING,
				null,
				new SayRequiredItemAction(QUEST_SLOT,0,"你仍没有拿到 [item]"
						+ " . 当你拿到后, 回去只用说 #complete 就能完成任务了. "));

	}

	private void abortQuest() {
		final SpeakerNPC npc = npcs.get("查玛斯市长");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
						 		 new TimePassedCondition(QUEST_SLOT,1,expireDelay)),
				ConversationStates.ATTENDING,
				null,
				// start quest again immediately
				startQuestAction());

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new AndCondition(new QuestActiveCondition(QUEST_SLOT),
						 		 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,expireDelay))),
				ConversationStates.ATTENDING,
				"你已开始做这个任务很长了, 我不让你这么快放弃. ",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.ABORT_MESSAGES,
				new QuestNotActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"恐怕我还没有给你发过一个 #quest 请求.",
				null);

	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("我在Ados城镇大厅中会见了城主 Chalmers. ");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("我不想帮助Ados城. ");
			return res;
		}

		res.add("我想帮助Ados城. ");
		if (player.hasQuest(QUEST_SLOT) && !player.isQuestCompleted(QUEST_SLOT)) {
			String questItem = player.getRequiredItemName(QUEST_SLOT,0);
			int amount = player.getRequiredItemQuantity(QUEST_SLOT,0);
			if (!player.isEquipped(questItem, amount)) {
				res.add("要帮助Ados城, 需要取得一个"
						+  questItem  + " , 但我身上还没有. ");
			} else {
				res.add("我找到了"
						+ questItem + " , 这下可以回去帮助Ados城了. ");
			}
		}
		int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
		if (repetitions > 0) {
			res.add("目前, 我已向Ados城提供了 "
					+ repetitions+ "次帮助. ");
		}
		if (isRepeatable(player)) {
			res.add("我取来城主让我去寻找的最后一件东西, Ados城又有了新需要. ");
		} else if (isCompleted(player)){
			res.add("我取来城主让我去寻找的最后一件东西, 并要求我在24小时内送到. ");
		}
		return res;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"每日物品任务",
				"城主Chalmers需要为Ados城供应各类物资. ",
				true);

		buildItemsMap();

		getQuest();
		completeQuest();
		abortQuest();
	}

	@Override
	public String getName() {
		return "DailyItemQuest";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,delay)).fire(player, null, null);
	}

	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "查玛斯市长";
	}
}
