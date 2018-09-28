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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

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
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KilledInSumForQuestCondition;
import games.stendhal.server.entity.npc.condition.KillsQuestSlotNeedUpdateCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import marauroa.common.Pair;


/**
 * QUEST: KillEnemyArmy
 *
 * PARTICIPANTS: <ul>
 * <li> Despot Halb Errvl
 * <li> some creatures
 * </ul>
 *
 * STEPS:<ul>
 * <li> Despot asking you to kill some of enemy forces.
 * <li> Kill them and go back to Despot for your reward.
 * </ul>
 *
 *
 * REWARD:<ul>
 * <li> 100k of XP, or 300 karma.
 * <li> random moneys - from 10k to 60k, step 10k.
 * <li> 5 karma for killing 100% creatures
 * <li> 5 karma for killing every 50% next creatures
 * </ul>
 *
 * REPETITIONS: <ul><li> once a week.</ul>
 */

 public class KillEnemyArmy extends AbstractQuest {

	private static final String QUEST_NPC = "Despot Halb Errvl";
	private static final String QUEST_SLOT = "杀死敌军";
	private static final int delay = MathHelper.MINUTES_IN_ONE_WEEK;

	protected HashMap<String, Pair<Integer, String>> enemyForces = new HashMap<String, Pair<Integer,String>>();
	protected HashMap<String, List<String>> enemys = new HashMap<String, List<String>>();




	public KillEnemyArmy() {
		super();
		// fill monster types map
		enemyForces.put("blordrough",
				new Pair<Integer, String>(50,"Blordrough warriors 现在驻扎在 Ados 地下通道. 他们在战斗中十分强悍, 这是 Blordrough 能占领 Deniran 的领土的原因. "));
		enemyForces.put("madaram",
				new Pair<Integer, String>(100,"他们的攻事建在了Fado 的地下, 他们真是丑恶."));
		enemyForces.put("黑暗精灵",
				new Pair<Integer, String>(100,"Drows, or dark elves as they are commonly called, can be found under Nalwor. They use poison in battles, gathering it from different poisonous creatures."));
		enemyForces.put("chaos",
				new Pair<Integer, String>(150,"They are strong and crazy. Only my elite archers hold them from expanding more."));
		enemyForces.put("山地矮人",
				new Pair<Integer, String>(150,"They are my historical neighbors, living in 塞门矿山."));
		enemyForces.put("山岭兽人",
				new Pair<Integer, String>(150,"Stupid creatures, but very strong. Can be found in an abandoned underground keep somewhere near Ados."));
		enemyForces.put("imperial",
				new Pair<Integer, String>(200,"They come from their castle in the underground 西大城, ruled by their 帝国大梦."));
		enemyForces.put("蛮人",
				new Pair<Integer, String>(200,"Different 蛮人 tribes live on the surface in the North West area of Ados Mountains. Not dangerous but noisy."));
		enemyForces.put("oni",
				new Pair<Integer, String>(200,"Very strange race, living in their castle in Fado forest. There are rumors that they have agreed an alliance with the Magic city wizards."));

		/*
		 * those are not interesting
		enemyForces.put("矮人",
				new Pair<Integer, String>(275,""));
		enemyForces.put("elf",
				new Pair<Integer, String>(300,""));
		enemyForces.put("skeleton",
				new Pair<Integer, String>(500,""));
		enemyForces.put("侏儒",
				new Pair<Integer, String>(1000,""));
		*/

		/*
		 *  fill creatures map
		 */

		enemys.put("blordrough",
				Arrays.asList("布拉德鲁军队",
							  "布拉德鲁下士",
							  "布拉德鲁风暴骑兵"));
		enemys.put("黑暗精灵",
				Arrays.asList("黑暗小精灵",
							  "黑暗精灵弓箭手",
							  "黑暗精灵",
							  "黑暗精灵精英弓箭手",
							  "黑暗精灵队长",
							  "黑暗精灵骑士",
							  "黑暗精灵将军",
							  "黑暗精灵巫师",
							  "黑暗精灵总督",
							  "黑暗精灵僧侣",
							  "黑暗精灵海军上将",
							  "黑暗精灵大师",
							  "黑暗精灵妈妈"));
		enemys.put("chaos",
				Arrays.asList("混沌士兵",
							  "混沌勇士",
							  "混沌司令",
							  "混沌巫师",
							  "混沌龙骑士",
							  "混沌领主",
							  "混沌绿龙骑士",
							  "混沌君主",
							  "混沌红龙骑士"));
		enemys.put("山地矮人",
				Arrays.asList("山地矮人",
							  "山地矮人壮士",
							  "山地矮人守卫",
							  "山地矮人英雄",
							  "山地矮人首领",
							  "钳工都尔",
							  "巨型都尔",
							  "矮人傀儡"));
		enemys.put("山岭兽人",
				Arrays.asList("山岭兽人",
							  "山岭兽族勇士",
							  "山岭兽族猎手",
							  "山岭兽族首领"));
		enemys.put("imperial",
				Arrays.asList("帝国守卫",
							  "帝国精英士兵",
							  "帝国弩手",
							  "帝国牧师",
							  "帝国精英守卫",
							  "帝国科学家",
							  "帝国高级僧侣",
							  "帝国弩手统领",
							  "帝国弩手精英",
							  "帝国指挥官",
							  "帝国首领",
							  "帝国骑士",
							  "帝国司令",
							  "帝国试验体",
							  "帝国魔佣军",
							  "帝国生化人",
							  "帝国将军",
							  "帝国魔鬼统领",
							  "帝国大梦",
							  "帝国巨人将军"));
		enemys.put("madaram",
				Arrays.asList("马德拉弓箭手",
							  "马德拉骑士",
							  "马德拉士兵",
							  "马德拉医师",
							  "马德拉斧手",
							  "马德拉女王",
							  "马德拉英雄",
							  "马德拉骑兵",
							  "马德拉杀手",
							  "madaram buster blader",
							  "马德拉弓箭手",
							  "马德拉风行者",
							  "卡萨库木蝠"));
		/*
		 * exclude amazoness ( because they dont want to leave their island? )
		enemys.put("amazoness",
				Arrays.asList("亚马逊弓箭手",
						      "亚马逊猎人",
						      "亚马逊海岸警卫队",
						      "亚马逊阿彻指挥官",
						      "亚马逊精英海岸警卫队",
						      "亚马逊保镖",
						      "亚马逊海岸警卫队的情妇",
						      "亚马逊指挥官",
						      "亚马逊警卫",
						      "亚马逊最高统治者",
						      "亚马逊巨人"));
		 */
		enemys.put("oni",
				Arrays.asList("欧尼武士",
							  "欧尼弓箭手",
							  "欧尼祭司",
							  "欧尼国王",
							  "欧尼女王"));
		enemys.put("蛮人",
				Arrays.asList("蛮人",
						      "蛮族人狼",
						      "蛮族精英",
						      "蛮族牧师",
						      "野蛮夏曼人",
						      "蛮族首领",
						      "蛮人王"));
	}

	/**
	 * function for choosing random enemy from map
	 * @return - enemy forces caption
	 */
	protected String chooseRandomEnemys() {
		final List<String> enemyList = new LinkedList<String>(enemyForces.keySet());
		final int enemySize = enemyList.size();
		final int position  = Rand.rand(enemySize);
		return enemyList.get(position);
	}

	/**
	 * function returns difference between recorded number of enemy creatures
	 *     and currently killed creatures numbers.
	 * @param player - player for who we counting this
	 * @return - number of killed enemy creatures
	 */
	private int getKilledCreaturesNumber(final Player player) {
		int count = 0;
		String temp;
		int solo;
		int shared;
		int recsolo;
		int recshared;
		final String enemyType = player.getQuest(QUEST_SLOT,1);
		final List<String> monsters = Arrays.asList(player.getQuest(QUEST_SLOT,2).split(","));
		final List<String> creatures = enemys.get(enemyType);
		for(int i=0; i<creatures.size(); i++) {
			String tempName = creatures.get(i);
			temp = monsters.get(i*5+3);
			if (temp == null) {
				recsolo = 0;
			} else {
				recsolo = Integer.parseInt(temp);
			}
			temp = monsters.get(i*5+4);
			if (temp == null) {
				recshared = 0;
			} else {
				recshared = Integer.parseInt(temp);
			}

			temp = player.getKeyedSlot("!kills", "solo."+tempName);
			if (temp==null) {
				solo = 0;
			} else {
				solo = Integer.parseInt(temp);
			}

			temp = player.getKeyedSlot("!kills", "shared."+tempName);
			if (temp==null) {
				shared = 0;
			} else {
				shared = Integer.parseInt(temp);
			}

			count = count + solo - recsolo + shared - recshared;
		}
		return count;
	}


	class GiveQuestAction implements ChatAction {
		/**
		 * function will update player quest slot.
		 * @param player - player for which we will record quest.
		 */
		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser speakerNPC) {
			final String monstersType = chooseRandomEnemys();
			speakerNPC.say("我需要帮助, 要打败敌军 #enemy " + monstersType +
					" . 下面要发热誓. 杀掉至少 " + enemyForces.get(monstersType).first()+
					" 的任何 "+ monstersType +
					" soldiers ,我会奖励你. ");
			final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
			List<String> sortedcreatures = enemys.get(monstersType);
			player.setQuest(QUEST_SLOT, 0, "start");
			player.setQuest(QUEST_SLOT, 1, monstersType);
			for(int i=0; i<sortedcreatures.size(); i++) {
				toKill.put(sortedcreatures.get(i), new Pair<Integer, Integer>(0,0));
			}
			new StartRecordingKillsAction(QUEST_SLOT, 2, toKill).fire(player, sentence, speakerNPC);
		}
	}

	class RewardPlayerAction implements ChatAction {
		/**
		 * function will complete quest and reward player.
		 * @param player - player to be rewarded.
		 */
		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser speakerNPC) {
			final String monsters = player.getQuest(QUEST_SLOT, 1);
			int killed=getKilledCreaturesNumber(player);
			int killsnumber = enemyForces.get(monsters).first();
			int moneyreward = 10000*Rand.roll1D6();
			if(killed == killsnumber) {
				// player killed no more no less then needed soldiers
				speakerNPC.say("Good work! Take these " + moneyreward + " coins. And if you need an assassin job again, ask me in one week. My advisors tell me they may try to fight me again.");
			} else {
				// player killed more then needed soldiers
				speakerNPC.say("Pretty good! You killed "+(killed-killsnumber)+" extra "+
						 "soldier" + "! Take these " + moneyreward + " coins, and remember, I may wish you to do this job again in one week!");
			}
			int karmabonus = 5*(2*killed/killsnumber-1);
			final StackableItem money = (StackableItem)
					SingletonRepository.getEntityManager().getItem("money");
			money.setQuantity(moneyreward);

			player.equipOrPutOnGround(money);
			player.addKarma(karmabonus);

		}
	}



	/**
	 * class for quest talking.
	 */
	class ExplainAction implements ChatAction {

		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
				final String monsters = player.getQuest(QUEST_SLOT, 1);
				int killed=getKilledCreaturesNumber(player);
				int killsnumber = enemyForces.get(monsters).first();

				if(killed==0) {
					// player killed no creatures but asked about quest again.
					npc.say("I already explained to you what I need. Are you an idiot, as you can't remember this simple thing about the #enemy " + monsters + " armies?");
					return;
				}
				if(killed < killsnumber) {
					// player killed less then needed soldiers.
					npc.say("You killed only "+killed+" "+ killed + player.getQuest(QUEST_SLOT, 1) +
							". You have to kill at least "+killsnumber+" "+killed+  player.getQuest(QUEST_SLOT, 1));
					return;
				}

		}
	}

	/**
	 * class for quest talking.
	 */
	class FixAction implements ChatAction {

		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
				//final String monsters = player.getQuest(QUEST_SLOT, 1);
			    Logger.getLogger(KillEnemyArmy.class).warn("Fixing malformed quest string of player <"+
				                                            player.getName()+
				                                            ">: ("+
				                                            player.getQuest(QUEST_SLOT)+
				                                            ")");
				npc.say("I am sorry, I did not pay attention. " +
						"What I need now:");
				new GiveQuestAction().fire(player, sentence, npc);
		}
	}


	/**
	 * add quest state to npc's fsm.
	 */
	private void step_1() {

		SpeakerNPC npc = npcs.get(QUEST_NPC);

		// quest can be given
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new OrCondition(
					new QuestNotStartedCondition(QUEST_SLOT),
					new AndCondition(
						new QuestCompletedCondition(QUEST_SLOT),
						new TimePassedCondition(QUEST_SLOT, 1, delay))),
				ConversationStates.ATTENDING,
				null,
				new GiveQuestAction());

		// time is not over
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new QuestCompletedCondition(QUEST_SLOT),
						new NotCondition(
								new TimePassedCondition(QUEST_SLOT, 1, delay))),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, delay, "You have to check again in"));

		// explanations
		npc.add(ConversationStates.ATTENDING,
				"enemy",
				new QuestInStateCondition(QUEST_SLOT, 0, "start"),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
						@Override
						public void fire(Player player, Sentence sentence, EventRaiser npc) {
							npc.say(enemyForces.get(player.getQuest(QUEST_SLOT, 1)).second());
						}
				});

		// explanations
		npc.add(ConversationStates.ATTENDING,
				"enemy",
				new QuestNotInStateCondition(QUEST_SLOT, 0, "start"),
				ConversationStates.ATTENDING,
				"Yes, my enemies are everywhere, they want to kill me! I guess you are one of them. Stay away from me!",
				null);

		// update player's quest slot or blank it if failed...
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KillsQuestSlotNeedUpdateCondition(QUEST_SLOT, 1, enemys, true)),
				ConversationStates.ATTENDING,
				null,
				new FixAction());

		// checking for kills
		final List<String> creatures = new LinkedList<String>(enemyForces.keySet());
		for(int i=0; i<enemyForces.size(); i++) {
			final String enemy = creatures.get(i);

			  // player killed enough enemies.
		      npc.add(ConversationStates.ATTENDING,
		    		  ConversationPhrases.QUEST_FINISH_MESSAGES,
		    		  new AndCondition(
		    				  new QuestInStateCondition(QUEST_SLOT, 1, enemy),
		    				  new KilledInSumForQuestCondition(QUEST_SLOT, 2, enemyForces.get(enemy).first())),
		    		  ConversationStates.ATTENDING,
		    		  null,
		    		  new MultipleActions(
		    				  new RewardPlayerAction(),
		    				  new IncreaseXPAction(100000),
		    				  new IncrementQuestAction(QUEST_SLOT,3,1),
		    				  // empty the 2nd index as we use it later
		    				  new SetQuestAction(QUEST_SLOT,2,""),
		    				  new SetQuestToTimeStampAction(QUEST_SLOT,1),
		    				  new SetQuestAction(QUEST_SLOT,0,"done")));

		      // player killed not enough enemies.
		      npc.add(ConversationStates.ATTENDING,
		    		  ConversationPhrases.QUEST_FINISH_MESSAGES,
		    		  new AndCondition(
		    				  new QuestInStateCondition(QUEST_SLOT, 1, enemy),
		    				  new NotCondition(
		    						  new KilledInSumForQuestCondition(QUEST_SLOT, 2, enemyForces.get(enemy).first()))),
		    		  ConversationStates.ATTENDING,
		    		  null,
		    		  new ExplainAction());

		}
	}

	/**
	 * add quest to the Stendhal world.
	 */
	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Kill Enemy Army",
				"Despot Halb Errvl has a vendetta against any army who opposes him.",
				true);
		step_1();
	}

	/**
	 * return name of quest slot.
	 */
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	/**
	 * return name of quest.
	 */
	@Override
	public String getName() {
		return "KillEnemyArmy";
	}

	@Override
	public int getMinLevel() {
		return 80;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return	new AndCondition(new QuestCompletedCondition(QUEST_SLOT),
						 new TimePassedCondition(QUEST_SLOT,1,delay)).fire(player, null, null);
	}

 	@Override
 	public List<String> getHistory(final Player player) {
 		LinkedList<String> history = new LinkedList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return history;
		}

		if(player.getQuest(QUEST_SLOT, 0).equals("start")) {
	        final String givenEnemies = player.getQuest(QUEST_SLOT, 1);
	        final int givenNumber = enemyForces.get(givenEnemies).first();
	        // updating firstly
			if(new KillsQuestSlotNeedUpdateCondition(QUEST_SLOT, 2, enemys.get(givenEnemies), true).fire(player, null, null)) {
				// still need update??
			}
	        final int killedNumber = getKilledCreaturesNumber(player);

			history.add("Despot Halb Errvl asked me to kill "+
					givenNumber+" "+
					givenNumber +  givenEnemies);
			String kn = Integer.valueOf(killedNumber).toString();
			if(killedNumber == 0) {
				kn="no";
			}
			history.add("目前, 你已杀了 "+
					kn +" "+
					killedNumber +" "+ givenEnemies);
			if(new KilledInSumForQuestCondition(QUEST_SLOT, 2, givenNumber).fire(player, null, null)) {
				history.add("现在我已杀了足够的生物去答复. ");
			} else {
				history.add("还有" + (givenNumber-killedNumber) + " 的 "+
						 givenEnemies + " 要杀.");
			}
		}

		if(isCompleted(player)) {
			history.add("I completed Despot's Halb Errvl task and got my reward!");
		}
		if (isRepeatable(player)) {
			history.add("Despot Halb Errvl is getting paranoid again about his safety, I can offer my services now.");
		}
		int repetitions = player.getNumberOfRepetitions(getSlotName(), 3);
		if (repetitions > 0) {
			history.add("I've bloodthirstily slain "
					+ repetitions + "whole army" + " for Despot Halb Errvl.");
		}
		return history;
 	}

	@Override
	public String getNPCName() {
		return "Despot Halb Errvl";
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_SURROUNDS;
	}
}

