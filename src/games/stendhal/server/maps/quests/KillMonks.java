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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.MathHelper;
import games.stendhal.common.Rand;
//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.KillsForQuestCounter;
import marauroa.common.Pair;

/**
 * QUEST: KillMonks
 *
 * PARTICIPANTS: <ul>
 * <li> Andy on Ados cemetery
 * <li> Darkmonks and normal monks
 * </ul>
 *
 * STEPS:<ul>
 * <li> Andy who is sad about the death of his wife, wants revenge for her death
 * <li> Kill 25 monks and 25 darkmonks for him for reaching his goal
 * </ul>
 *
 *
 * REWARD:<ul>
 * <li> 15000 XP
 * <li> 1-5 soup
 * <li> some karma
 * </ul>
 *
 * REPETITIONS: <ul><li>once in two weeks</ul>
 *
 * @author Vanessa Julius, idea by anoyyou

 */

public class KillMonks extends AbstractQuest {

	private static final String QUEST_SLOT = "kill_monks";
	protected HashMap<String, Pair<Integer, Integer>> creaturestokill = new HashMap<String, Pair<Integer,Integer>>();

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	public KillMonks() {
		super();

		 creaturestokill.put("monk",
				 new Pair<Integer, Integer>(0, 25));

		 creaturestokill.put("darkmonk",
				 new Pair<Integer, Integer>(0, 25));

	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Andy");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"我的爱妻去 Kroip 送新鲜 比萨时, 被一些monk僧侣尾随, 她没机会逃脱, 最后在去Wo'fol的路上时被杀了. 现在我要报仇, 你能帮我吗？",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
						 new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*2)),
				ConversationStates.QUEST_OFFERED,
				"这些僧侣太凶残, 我无法复仇, 你可能帮助我吗？",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*2)), new QuestStateStartsWithCondition(QUEST_SLOT, "killed")),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*2, "这些僧侣得到了教训, 但我还需要你的再次帮助. in"));


		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "start"));
		actions.add(new IncreaseKarmaAction(5));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, creaturestokill));


		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"谢谢你! 请新掉 25 monks 和 25 darkmonks 为了我已故的爱妻. ",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"可怜...也许你试着改变想法, 帮助这个悲伤的男人. ",
				new MultipleActions(
				        new SetQuestAction(QUEST_SLOT, 0, "rejected"),
				        new DecreaseKarmaAction(5)));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Andy");

		ChatAction addRandomNumberOfItemsAction = new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				//add random number of soups
				final StackableItem soup = (StackableItem) SingletonRepository.getEntityManager()
						.getItem("蔬菜汤");
				int amount;
				// between 1 and 5 soup
				amount = Rand.rand(4) + 1;
				soup.setQuantity(amount);
				player.equipOrPutOnGround(soup);
			}
		};

		final List<ChatAction> actions = new LinkedList<ChatAction>();
	    actions.add(addRandomNumberOfItemsAction);
		actions.add(new IncreaseXPAction(15000));
		actions.add(new SetQuestAction(QUEST_SLOT, 0, "killed"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new IncrementQuestAction(QUEST_SLOT,2,1));

		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);
		npc.add(ConversationStates.ATTENDING,
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING,
				"非常感谢！现在我可以睡得安稳一点了, 请拿着这些汤.",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING,
				"请帮我实现复仇的目标!",
				null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Kill Monks",
				"Andy 的妻子被僧侣杀了. 现在他要为亡妻报仇. ",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public List<String> getHistory(final Player player) {
		return getHistory(player, false);
	}

	@Override
	public List<String> getFormattedHistory(final Player player) {
		return getHistory(player, true);
	}

	private List<String> getHistory(final Player player, boolean formatted) {
			final List<String> res = new ArrayList<>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			res.add("我在Ados城见到了Andy, 他想让我为他的亡妻报仇. ");
			final String questStateFull = player.getQuest(QUEST_SLOT);
			final String[] parts = questStateFull.split(";");
			final String questState = parts[0];

			if ("rejected".equals(questState)) {
				res.add("我拒绝了他的请求.");
			}
			if ("start".equals(questState)) {
				res.add("我答应为Andy的亡妻报仇, 杀死 25 个 monks 和 25 darkmonks. ");
				if (formatted) {
					res.addAll(howManyWereKilledFormatted(player, parts[1]));
				} else {
					res.add(howManyWereKilled(player, parts[1]));
				}
			}
			if (isCompleted(player)) {
				if(isRepeatable(player)){
					res.add("现在已过了两周, 我要为Andy的请求做出交待. 也许他需要我的帮助!");
				} else {
					res.add("我已杀死了这些 monks , Andy 最后也能睡得踏实了. !");
				}
			}
			int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
			if (repetitions > 0) {
				res.add("现在我已为Andy复仇 "
						+ repetitions + "time . " );
			}
			return res;
	}

	private String howManyWereKilled(final Player player, final String questState) {
		KillsForQuestCounter killsCounter = new KillsForQuestCounter(questState);
		int killedMonks = 25 - killsCounter.remainingKills(player, "monk");
		int killedDarkMonks = 25 - killsCounter.remainingKills(player, "darkmonk");
		return "我已杀死了 " + killedMonks + "monk" + " 和 " + killedDarkMonks + "darkmonk" + ".";
	}

	private List<String> howManyWereKilledFormatted(final Player player, final String questState) {
		KillsForQuestCounter killsCounter = new KillsForQuestCounter(questState);
		int killedMonks = 25 - killsCounter.remainingKills(player, "monk");
		int killedDarkMonks = 25 - killsCounter.remainingKills(player, "darkmonk");

		List<String> entries = new ArrayList<>();
		entries.add("Monks: <tally>" + killedMonks + "</tally>");
		entries.add("Darkmonks: <tally>" + killedDarkMonks + "</tally>");
		return entries;
	}

	@Override
	public String getName() {
		return "KillMonks";

	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
				 new TimePassedCondition(QUEST_SLOT, 1, MathHelper.MINUTES_IN_ONE_WEEK*2)).fire(player,null, null);
	}

	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"killed").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "Andy";
	}

	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}
}
