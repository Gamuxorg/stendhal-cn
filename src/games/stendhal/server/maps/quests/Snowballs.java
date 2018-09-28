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
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.Rand;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Snowballs
 * <p>
 * PARTICIPANTS:
 * <li> 耶提先生, a creature in a dungeon needs help
 * <p>
 * STEPS:
 * <li> 耶提先生 asks for some snow, and wants you to get 25 snowballs.
 * <li> You collect 25 snowballs from 冰傀儡s.
 * <li> You give the snowballs to 耶提先生.
 * <li> 耶提先生 gives you 20 cod or perch.
 * <p>
 * REWARD: <li> 20 cod or perch <li> 50 XP <li> 22 karma in total (20 + 2)
 * <p>
 * REPETITIONS: <li> Unlimited, but 2 hours of waiting is
 * required between repetitions
 */

public class Snowballs extends AbstractQuest {

	private static final int REQUIRED_SNOWBALLS = 25;

	private static final int REQUIRED_MINUTES = 120;

	private static final String QUEST_SLOT = "雪球";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public boolean isCompleted(final Player player) {
		return player.hasQuest(QUEST_SLOT)
				&& !player.getQuest(QUEST_SLOT).equals("start")
				&& !player.getQuest(QUEST_SLOT).equals("rejected");
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestNotInStateCondition(QUEST_SLOT, "start"), new QuestStartedCondition(QUEST_SLOT), new TimePassedCondition(QUEST_SLOT,REQUIRED_MINUTES)).fire(player, null, null);
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("我进入冰冷的洞窟并见到了 耶提先生.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("我现在不想帮助 耶提先生, 他粗暴地赶走了我...");
			return res;
		}
		res.add("耶提先生 要我为他收集一些雪球, 我保证完成任务.");
		if (player.isEquipped("雪球", REQUIRED_SNOWBALLS) || isCompleted(player)) {
			res.add("通过杀死冰傀儡我得到了一些雪球.");
		}
		if (isCompleted(player)) {
			res.add("我把雪球给了 耶提先生 后, 他很开心.");
		}
		if(isRepeatable(player)){
			res.add("耶提先生 又该需要雪球了!");
		}
		return res;
	}

	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("耶提先生");

		// says hi without having started quest before
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING,
				"你好陌生人! 你看到我的雪雕了吗? 我需要像你这样的朋友帮我完成我的 #需求.",
				null);

		// says hi - got the snow yeti asked for
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new PlayerHasItemWithHimCondition("雪球", REQUIRED_SNOWBALLS)),
				ConversationStates.QUEST_ITEM_BROUGHT,
				"你好陌生人! 我看到你带了些好东西. 这些雪球是给我的吗?",
				null);

		// says hi - didn't get the snow yeti asked for
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, "start"),
						new NotCondition(new PlayerHasItemWithHimCondition("雪球", REQUIRED_SNOWBALLS))),
				ConversationStates.ATTENDING,
				"又回来了? 不要忘了你保证的雪球!",
				null);

		// says hi - quest was done before and is now repeatable
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStartedCondition(QUEST_SLOT),
						new QuestNotInStateCondition(QUEST_SLOT, "start"),
						new TimePassedCondition(QUEST_SLOT, REQUIRED_MINUTES)),
				ConversationStates.ATTENDING,
				"再次欢迎! 你已见识过我最新的雪雕品了吧? 我现在又有 #需求 了...",
				null);

		// says hi - quest was done before and is not yet repeatable
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStartedCondition(QUEST_SLOT),
						new QuestNotInStateCondition(QUEST_SLOT, "start"),
						new NotCondition(new TimePassedCondition(QUEST_SLOT, REQUIRED_MINUTES))),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, REQUIRED_MINUTES, "我已为新雪雕备好足够多的雪球了. 谢谢你的帮助! "
						+ "我要开始雕刻个新的雪雕了" ));

		// asks about quest - has never started it
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"我喜欢制作雪雕, 但这个山洞中的雪不够了. 你可能帮我弄些雪球吗? 25个就够了.",
				null);

		// asks about quest but already on it
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.ATTENDING,
				"你已答应给我带25个雪球! 记住了...",
				null);

		// asks about quest - has done it but it's repeatable now
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStartedCondition(QUEST_SLOT), new QuestNotInStateCondition(QUEST_SLOT, "start"), new TimePassedCondition(QUEST_SLOT, REQUIRED_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"我喜欢制作雪雕, 但这个山洞中的雪不够了. 你可能帮我弄些雪球吗? 25个就够了.",
				null);

		// asks about quest - has done it and it's too soon to do again
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStartedCondition(QUEST_SLOT), new QuestNotInStateCondition(QUEST_SLOT, "start"), new NotCondition(new TimePassedCondition(QUEST_SLOT, REQUIRED_MINUTES))),
				ConversationStates.ATTENDING,
				"我已为新雪雕备好足够多的雪球了. 但还是谢谢你的帮助!.",
				null);

		// player is willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"很好. 你可以在这个洞窟中的冰傀儡身上找到雪球, 但要小心附近的大家伙! 收集够25个雪球再回给吧.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 2.0));

		// player is not willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"那你还来干什么? 滚出去!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private void prepareBringingStep() {

		final SpeakerNPC npc = npcs.get("耶提先生");

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("雪球", REQUIRED_SNOWBALLS));
		reward.add(new IncreaseXPAction(50));
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT));
		// player gets either cod or perch, which we don't have a standard action for
		// and the npc says the name of the reward, too
		reward.add(new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						String rewardClass;
						if (Rand.throwCoin() == 1) {
							rewardClass = "cod";
						} else {
							rewardClass = "perch";
						}
						npc.say("谢谢! 给你, 拿着这些 " + rewardClass + "! 我不喜欢吃.");
						final StackableItem reward = (StackableItem) SingletonRepository.getEntityManager().getItem(rewardClass);
						reward.setQuantity(20);
						player.equipOrPutOnGround(reward);
						player.addKarma(20.0);
						player.notifyWorldAboutChanges();
					}
				});

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("雪球", REQUIRED_SNOWBALLS),
			ConversationStates.ATTENDING,
			null,
			new MultipleActions(reward));

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new NotCondition(new PlayerHasItemWithHimCondition("雪球", REQUIRED_SNOWBALLS)),
			ConversationStates.ATTENDING,
			"嗨! 你在哪弄到的雪球?",
			null);

		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Oh 希望你能马上给我! 我想尽快完成雪雕!",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"耶提先生 的雪球",
				"Faiumoni冰窟里的居民需要你帮他找些雪球.",
				false);
		prepareRequestingStep();
		prepareBringingStep();
	}

	@Override
	public String getName() {
		return "雪球";
	}

	// the djinns, 冰傀儡s and 冰元素s on the way to yeti caves are quite dangerous
	@Override
	public int getMinLevel() {
		return 60;
	}

	@Override
	public String getNPCName() {
		return "耶提先生";
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_YETI_CAVE;
	}

}
