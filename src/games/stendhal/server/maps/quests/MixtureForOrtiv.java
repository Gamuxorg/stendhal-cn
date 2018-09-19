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
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.CollectRequestedItemsAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayRequiredItemsFromCollectionAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;

/**
 * QUEST: Mixture for Ortiv
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Ortiv Milquetoast, the retired teacher who lives in the Kirdneh River house</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Ortiv asks you for some ingredients for a mixture which will help him to keep the assassins and bandits in the cellar</li>
 * <li>Find the ingredients</li>
 * <li>Take the ingredients back to Ortiv</li>
 * <li>Ortiv gives you a reward</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>karma +35</li>
 * <li>5000 XP</li>
 * <li>a bounded 刺客匕首</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 *
 * @author Vanessa Julius
 */
public class MixtureForOrtiv extends AbstractQuest {

	public static final String QUEST_SLOT = "mixture_for_ortiv";

	/**
	 * required items for the quest.
	 */
	protected static final String NEEDED_ITEMS = "瓶子=1;海芋=2;red lionfish=10;kokuda=1;红伞菇=12;licorice=2;苹果=10;红酒=30;大蒜=2;pestle and mortar=1";

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("我见到了 Ortiv Milquetoast, 一位退休的老教师, 他住在 Kirdneh River 的一所房子里, 我需要他的帮忙.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("我现在不想帮助 Ortiv . 他应该出去自己拿配料.");
		} else if (!"done".equals(questState)) {
			final ItemCollection missingItems = new ItemCollection();
			missingItems.addFromQuestStateString(questState);
			res.add("我要带给 " + missingItems.toStringList() + ".");
		} else {
			res.add("我帮了 Ortiv. 现在他又能安静的睡觉了. 他给了我一些 xp 和一个刺客匕首作为报答.");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Ortiv Milquetoast");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new LevelGreaterThanCondition(2),
					new QuestNotStartedCondition(QUEST_SLOT),
					new NotCondition(new QuestInStateCondition(QUEST_SLOT,"rejected"))),
			ConversationStates.QUESTION_1,
			"Ohh 我发现一个陌生人藏在我的房子里, 也许你能帮我做点事?", null);

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT,"rejected")),
			ConversationStates.QUEST_OFFERED,
			"嗨, 你想再帮我一次? 你要做什么?", null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"我目前一直忙于把一群吵闹的家伙们赶到楼下, 也许你可以在稍后的时间帮我弄一些配料 #ingredients ,我以后要用.",
			null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.NO_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"我目前一直忙于把一群吵闹的家伙们赶到楼下, 也许你可以在稍后的时间帮我弄一些配料 #ingredients ,我以后要用.",
			null);

		npc.add(
			ConversationStates.ATTENDING,
			"ingredients",
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"以前我是练金教师. 现在我努力把它们混合在一起, 为此我需要一些原料, 希望你能帮我, 好吗？",
			null);

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			null,
			new MultipleActions(new SetQuestAction(QUEST_SLOT, NEEDED_ITEMS),
							    new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "Oh 那太好了, 陌生人！, 你救了我的命！请给我带点 [items].")));

		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"我以为你能帮我, 但我错了, 就像我当老师时, 和学生一样明显的错误. ",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));

		npc.addReply("苹果", "苹果是刺客最爱的食物. 我在Semon城东,Orril和Halwor河的旁边见过一些苹果树" );

			npc.addReply("瓶子", "我听说一个年轻的女人在Semo卖这东西.");

			npc.addReply("红伞菇", "红伞菇s 毒性很强, 我听说曾有猎人在森林吃了一点, 就病了好几天.");

			npc.addReply("海芋", "像是一些旧友跟我说过, 塞门镇的北面, 离小树林很近的地方, 生找着一种名叫 海芋 的药草");

			npc.addReply("red lionfish","Red lionfish 很难找到, 它们体外有红白相门的条纹, 红是紫红色 " +
					"或棕色. 我曾听说在Faiumoni的一个地方, 你可以钓到它们, 但要小心, 每条 lionfish  都有毒!");

			npc.addReply("kokuda","Kokuda 相当难找. 如果你在别的岛能抓住一条我会很开心...");

			npc.addReply("licorice", "在魔法城市有一个好看的小酒吧, 里面年轻的女孩销售这种好吃的甜品.");

			npc.addReply("红酒", "Mmmmm..没有比把喜人的杯子和红酒混合在一起更好的东西了, 咳咳 *cough* 当然为我的混合物需要它... 我打赌, 你可能在旅店或酒吧的某地买到酒...");

			npc.addReply("大蒜", "我知道, 刺客和强盗们不是吸血鬼, 但我会尽量好好使用它对抗他们. 城里的花园里有个漂亮的园丁, 她销售自己种的大蒜.");

			npc.addReply(Arrays.asList("pestle","mortar","pestle and mortar"), "可能一些面包师或厨师用得着这些.");
	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Ortiv Milquetoast");

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestActiveCondition(QUEST_SLOT)),
				ConversationStates.QUESTION_2,
				"你好! 很高兴见到你, 你带给我一些混合物的调料了吗? #ingredients ",
				null);

		/* player asks what exactly is missing (says ingredients) */
		npc.add(ConversationStates.QUESTION_2, "ingredients", null,
				ConversationStates.QUESTION_2, null,
				new SayRequiredItemsFromCollectionAction(QUEST_SLOT, "我需要 [items]. 你带的有吗?"));

		/* player says he has a required item with him (says yes) */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.QUESTION_2, "可怕, 你带的是什么东西?",
				null);

		ChatAction completeAction = new  MultipleActions(
				new SetQuestAction(QUEST_SLOT, "done"),
				new SayTextAction("非常感谢！, 我现在能开始混合这些调料, 有希望用制成品让我安全的呆在房子里, 再没有刺客和盗贼从楼下跑上来. 这一把刺客匕首给你, 这是我我不得不从以前班里的一个学生手中收走它, 现在你可以和他们对决并取胜."),
				new IncreaseXPAction(5000),
				new IncreaseKarmaAction(25),
				new EquipItemAction("刺客匕首", 1 ,true)
				);
		/* add triggers for the item names */
		final ItemCollection items = new ItemCollection();
		items.addFromQuestStateString(NEEDED_ITEMS);
		for (final Map.Entry<String, Integer> item : items.entrySet()) {
			npc.add(ConversationStates.QUESTION_2, item.getKey(), null,
					ConversationStates.QUESTION_2, null,
					new CollectRequestedItemsAction(
							item.getKey(), QUEST_SLOT,
							"太棒了！你带了些东西给我吗?", "你已经给过我这些东西了.",
							completeAction, ConversationStates.ATTENDING));
		}

		/* player says he didn't bring any items (says no) */
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Ok, 现在我要保持一点耐心. 不管我能帮助你做点什么, 就告诉我 #help .",
				null);

		/* player says he didn't bring any items to different question */
		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.NO_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Ok, 现在我要保持一点耐心. 不管我能帮助你做点什么, 就告诉我 #help .", null);

		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestCompletedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"非常感谢！我能平安的睡觉了, 你救了我！", null);
	}


	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Mixture for Ortiv",
				"Ortiv 要制作混合物还需要一些调料, 制成后会帮他让刺客和盗贼困在楼下",
				true);
		prepareRequestingStep();
		prepareBringingStep();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "MixtureForOrtiv";
	}

	public String getTitle() {

		return "Mixture for Ortiv";
	}

	@Override
	public String getNPCName() {
		return "Ortiv Milquetoast";
	}

	@Override
	public String getRegion() {
		return Region.KIRDNEH;
	}
}
