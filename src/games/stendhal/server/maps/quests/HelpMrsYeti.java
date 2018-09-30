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
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.creature.Pet;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasPetOrSheepCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Mrs Yeti Needs Help
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Mrs Yeti, who lives in a snowy dungeon</li>
 * <li>莎娃玛特丽, Healer at magic city</li>
 * <li>Hackim Easso, Blacksmith assistant semos</li>
 * </ul>
 *
 * STEPS:
 * Mrs. Yeti lifes in a cave somewhere in semos mountain. She is mournful, because 耶提先生 turn away from her. Thats why she ask the player for help. She like to have a special 治疗剂 and some other stuff as a present for her husband.
 *
 * There is only one witch who, who can make the special 治疗剂. Mrs. Yeti tell the player where she lives. The player go for the witch. Once he found her, she tell the player, that she will help, but need some ingriedents.
 *
 * When the player is bringing in the collected stuff, she has to tell him, that her magic knife is damaged and she need a new one and send the player to a blacksmith. He has to craft a new magic knife for the witch.
 *
 * The blacksmith is willing to help. But need some stuff too, to craft the magic knife. He sends the player to collect it. The player brings in the needed items and the blacksmith could start make the knife, but he is too hungry to start it right now. Player has to bring him some food and he starts crafting the knife. But the player has to wait a bit until he is ready with it.
 *
 * After bring the knife to the witch, he tell the player that she forgot an important item. The player has to get it and bring it to here. After a while the special 治疗剂 is ready. And the player can bring it to 耶提女士.
 *
 * 耶提女士 is very happy about the special 治疗剂. But she needs some other things to make her husband happy. The player has to collect a baby dragon for her. After player bring the baby dragon to her, she is happy as never befor.
 *
 * REWARD:
 * <ul>
 * <li> 1,000 XP </li>
 * <li> some karma (10 + (10 | -10)) </li>
 * <li> Can buy <item>roach</item> from 耶提女士 </li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>Not repeatable.</li>
 * </ul>
 */

 public class HelpMrsYeti extends AbstractQuest {

 	private static final String QUEST_SLOT = "mrsyeti";
	private static final int DELAY_IN_MINUTES = 60*24;

	private static Logger logger = Logger.getLogger(HelpMrsYeti.class);

 	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void startQuest() {
		final SpeakerNPC npc = npcs.get("耶提女士");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"I am mournful, because Mr Yeti turns away from me. I need a special 治疗剂 to make him happy and some present to please him. Will you help?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Thank you for your help! Now Mr Yeti and I are very happy again.",
				null);


		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Thank you for your help! You need to go to 莎娃玛特丽 in the magic city for the #治疗剂.",
				new SetQuestAction(QUEST_SLOT, "start"));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
				"Oh, you are so heartless.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}

	private void makePotion() {
	// player needs to bring some items to make the 治疗剂:
	// a 'magic' knife from a blacksmith
	// 3 lilia flowers
	// 鼠尾草
	// wine
	// 黑珍珠
	final SpeakerNPC npc = npcs.get("莎娃玛特丽");

    	npc.add(ConversationStates.ATTENDING, "治疗剂",
				new QuestInStateCondition(QUEST_SLOT, "start"),
			    ConversationStates.ATTENDING, "I will help you make this 治疗剂, Mrs Yeti is an old friend of mine. But the blade on "
				+ "my magic knife has snapped yet again. I need another. I get mine from Hackim Easso of 塞门镇, will you go to him and "
				+ "ask him to make another knife? Just say my name: #salva",
				new SetQuestAction(QUEST_SLOT, "hackim"));

		npc.add(
			ConversationStates.ATTENDING, Arrays.asList("salva","小刀"),
			new NotCondition(new QuestInStateCondition(QUEST_SLOT, "小刀")),
			ConversationStates.ATTENDING,
			"You need to go to Hackim Easso and ask him about a magic knife for #salva before I can help you.",
			null);

	    npc.add(ConversationStates.ATTENDING,  Arrays.asList("salva","小刀","治疗剂"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "小刀"),
				new PlayerHasItemWithHimCondition("小刀")),
				ConversationStates.ATTENDING, "Very good! Now I need the items to make the love #治疗剂. I need 3 紫丁香 flowers, 1 sprig of 科科达, 1 glass of 红酒 and 1 黑珍珠. Please bring them all together at once and then ask me to make the #治疗剂.",
				new MultipleActions(new SetQuestAction(QUEST_SLOT, "治疗剂"), new DropItemAction("小刀")));

	    npc.add(ConversationStates.ATTENDING,  Arrays.asList("salva","小刀","治疗剂"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "小刀"),
				new NotCondition(new PlayerHasItemWithHimCondition("小刀"))),
				ConversationStates.ATTENDING, "I see you have been to Hackim, but where is the magic 小刀?",
				null);

		final List<ChatAction> potionactions = new LinkedList<ChatAction>();
		potionactions.add(new DropItemAction("紫丁香",3));
		potionactions.add(new DropItemAction("科科达"));
		potionactions.add(new DropItemAction("红酒"));
		potionactions.add(new DropItemAction("黑珍珠"));
		potionactions.add(new EquipItemAction("爱之治疗剂"));
		potionactions.add(new IncreaseXPAction(100));
		potionactions.add(new SetQuestAction(QUEST_SLOT, "gotpotion"));

		// don't make player wait for 治疗剂 - could add this in later if wanted
		npc.add(ConversationStates.ATTENDING,  Arrays.asList("salva","治疗剂"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "治疗剂"),
								 new PlayerHasItemWithHimCondition("紫丁香",3),
								 new PlayerHasItemWithHimCondition("科科达"),
								 new PlayerHasItemWithHimCondition("红酒"),
								 new PlayerHasItemWithHimCondition("黑珍珠")),
				ConversationStates.ATTENDING, "I see you have all the items for the 治疗剂. *mutters magic words* And now, ta da! You have the 爱之治疗剂. Wish Mrs Yeti good luck from me!",
				new MultipleActions(potionactions));

		npc.add(ConversationStates.ATTENDING,  Arrays.asList("salva","治疗剂"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "治疗剂"),
								 new NotCondition(
												  new AndCondition(new PlayerHasItemWithHimCondition("紫丁香",3),
																   new PlayerHasItemWithHimCondition("科科达"),
																   new PlayerHasItemWithHimCondition("红酒"),
																   new PlayerHasItemWithHimCondition("黑珍珠")))),
				ConversationStates.ATTENDING, "I need 3 紫丁香 flowers, 1 sprig of 科科达, 1 glass of 红酒 and 1 黑珍珠 to make the 爱之治疗剂. Please bring them all together at once. Thanks!", null);


	}

	private void makeMagicKnife() {
		// although the player does end up just taking an ordinary knife to salva, this step must be completed
		// (must be in quest state 'knife' when they take the knife)
	final SpeakerNPC npc = npcs.get("Hackim Easso");
		npc.add(ConversationStates.ATTENDING, "salva",
				new QuestInStateCondition(QUEST_SLOT, "hackim"),
			    ConversationStates.ATTENDING, "Salva needs another magic knife does she? Ok, I can help you but not while I am so hungry. "
				+ "I need food! Bring me 5  #馅饼 s and I will help you!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "pies", 1.0));

	    npc.add(ConversationStates.ATTENDING, Arrays.asList("salva", "pies"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "pies"),
				new PlayerHasItemWithHimCondition("馅饼",5)),
				ConversationStates.ATTENDING, "Ah, thank you very much! Now I will tell you a little secret of mine. I am not a blacksmith, "
				+ "only an assistant. I can't make knives at all! But I sell Salva a normal 小刀 and is happy enough with that! So just take her "
				+ "a plain 小刀 like you could buy from 辛布兰卡 in 塞门镇 Tavern. I'll tell her I made it! Oh and thanks for the pies!!!",
				new MultipleActions(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "小刀", 1.0), new DropItemAction("馅饼",5)));

	    npc.add(ConversationStates.ATTENDING, Arrays.asList("salva", "pies"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "pies"),
				new NotCondition(new PlayerHasItemWithHimCondition("馅饼",5))),
				ConversationStates.ATTENDING, "Arlindo from Ados makes the best meat and vegetable pies. Please remember to bring me 5, I am hungry!",
				null);

	}

	private void bringPotion() {
	final SpeakerNPC npc = npcs.get("耶提女士");
		final String extraTrigger = "治疗剂";
	    List<String> questTrigger;
	    questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

	    final List<ChatAction> tookpotionactions = new LinkedList<ChatAction>();
		tookpotionactions.add(new DropItemAction("爱之治疗剂"));
		tookpotionactions.add(new IncreaseKarmaAction(10.0));
		tookpotionactions.add(new IncreaseXPAction(1000));
		tookpotionactions.add(new SetQuestAction(QUEST_SLOT, "dragon"));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "gotpotion"),
				new PlayerHasItemWithHimCondition("爱之治疗剂")),
				ConversationStates.ATTENDING, "Thank you! That looks so powerful I almost love you from smelling it! But don't worry I will save it for my husband. But he won't take it without some other temptation. I think he'd like a baby #dragon, if you'd be so kind as to bring one.",
				new MultipleActions(tookpotionactions));

		npc.add(
			ConversationStates.ATTENDING, questTrigger,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "gotpotion"), new NotCondition(new PlayerHasItemWithHimCondition("爱之治疗剂"))),
			ConversationStates.ATTENDING,
			"What did you do with the 爱之治疗剂?",
			null);

		npc.add(ConversationStates.ATTENDING,
				questTrigger,
				new OrCondition(new QuestInStateCondition(QUEST_SLOT, "start"),
								new QuestInStateCondition(QUEST_SLOT, "pies"),
								new QuestInStateCondition(QUEST_SLOT, "小刀")),
				ConversationStates.ATTENDING,
				"I am waiting for you to return with a 爱之治疗剂. Please ask 莎娃玛特丽 in the magic city about: #治疗剂.",
				null);
	}

	private void bringDragon() {
	final SpeakerNPC npc = npcs.get("耶提女士");

	    final String extraTrigger = "dragon";
	    List<String> questTrigger;
	    questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		// easy to check if they have a pet or sheep at all
	    npc.add(
			ConversationStates.ATTENDING, questTrigger,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "dragon"),
							 new NotCondition(new PlayerHasPetOrSheepCondition())),
			ConversationStates.ATTENDING,
			"You can get a baby dragon only if you have a 传说之卵. Those, you must get from Morgrin at the wizard school. "
			+ "Then Terry in 塞门洞穴 will hatch it.",
			null);

		// if they have any pet or sheep, then check if it's a baby dragon
		npc.add(
			ConversationStates.ATTENDING, questTrigger,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, "dragon"),
							 new PlayerHasPetOrSheepCondition()),
			ConversationStates.ATTENDING,
			null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence,
								 final EventRaiser npc) {
					if(!player.hasPet()){
						npc.say("That's a cute sheep you have there, but I need a baby dragon for Mr Yeti. Try Morgrin at the magic school.");
						return;
					}
					Pet pet = player.getPet();
					String petType = pet.get("type");
					if("baby_dragon".equals(petType)) {
						player.removePet(pet);
						npc.say("Ah you brought the baby dragon! It will make such a wonderful stew. Baby dragon stew is my speciality and Mr Yeti loves it! You've made us both very happy! Come back in a day to see me for a #reward.");
						player.addKarma(5.0);
						player.addXP(500);
						pet.delayedDamage(pet.getHP(), "耶提女士");
						player.setQuest(QUEST_SLOT,"reward;"+System.currentTimeMillis());
						player.notifyWorldAboutChanges();
					} else {
						npc.say("That's a cute pet you have there, but I need a baby dragon for Mr Yeti. Try Morgrin at the magic school.");
					}
				}
			});

	}

	private void getReward() {

	final SpeakerNPC npc = npcs.get("耶提女士");

	    final String extraTrigger = "reward";
	    List<String> questTrigger;
	    questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

	    npc.add(
			ConversationStates.ATTENDING, questTrigger,
			new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "reward"),
							 // delay is in minutes, last parameter is argument of timestamp
							 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES))),
			ConversationStates.ATTENDING,
			null,
			new SayTimeRemainingAction(QUEST_SLOT,1,DELAY_IN_MINUTES,"Hello I am still busy with that baby dragon stew for Mr Yeti. You can get your reward in"));


		npc.add(
			ConversationStates.ATTENDING, questTrigger,
			new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "reward"),
							 // delay is in minutes, last parameter is argument of timestamp
							 new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES)),
			ConversationStates.ATTENDING,
			"Thank you! To say thank you, I'd like to offer you the chance to always #buy #roach from me cheaply. I have so much of it and perhaps you have a use for it.",
			new MultipleActions(new SetQuestAction(QUEST_SLOT,"done"), new IncreaseXPAction(1000)));

	}


	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Help Mrs Yeti",
				"Mrs Yeti is really unhappy with her current lovelife because her husband turned away from her. Now the couple is in deep trouble. Just a special 爱之治疗剂 can help Mrs Yeti to get her husband back.",
				true);
		startQuest();
		makePotion();
		makeMagicKnife();
		bringPotion();
		bringDragon();
		getReward();
	}

	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			final String questState = player.getQuest(QUEST_SLOT);
			res.add("I met 耶提女士 in icy caves below 塞门镇 Mountain.");
			res.add("耶提女士 asked me to go to 莎娃玛特丽 for a special 爱之治疗剂 for her husband.");
			if ("rejected".equals(questState)) {
				res.add("I don't want to help with soppy love stories..");
				return res;
			}
			if ("start".equals(questState)) {
				return res;
			}
			res.add("莎娃玛特丽 needs a magic knife from Hackim Easso to make her 治疗剂.");
			if ("hackim".equals(questState)) {
				return res;
			}
			res.add("Hackim is hungry and wants 5 meat pies before he helps me.");
			if ("pies".equals(questState)) {
				return res;
			}
			res.add("Hackim said I should go buy a standard knife like from 辛布兰卡!! Apparently he tricked Salva all these years into believing they are magic, I better not let on...");
			if ("小刀".equals(questState)) {
				return res;
			}
			res.add("The 爱之治疗剂 requires 3 紫丁香 flowers, 1 sprig of 科科达, 1 glass of 红酒 and 1 黑珍珠.");
			if ("治疗剂".equals(questState)) {
				return res;
			}
			res.add("I must take the 爱之治疗剂 in its heart shaped bottle, to 耶提女士.");
			if ("gotpotion".equals(questState)) {
				return res;
			}
			res.add("耶提女士 needs something else to tempt her husband with and has asked me to bring a baby dragon.");
			if ("dragon".equals(questState)) {
				return res;
			}
			res.add("Oh my! She killed my dragon to make stew! That wasn't the kind of treat I thought she had in mind!");
			if (questState.startsWith("reward")) {
				if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
					res.add("耶提女士 told me to come back in a day to collect my reward and it's already been long enough.");
				} else {
					res.add("耶提女士 told me to come back in a day to collect my reward so I need to wait.");
				}
				return res;
			}
			res.add("耶提女士 is really pleased with the outcome of my help and now she'll sell me roach very cheaply.");
			if (isCompleted(player)) {
				return res;
			}

			// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
			final List<String> debug = new ArrayList<String>();
			debug.add("Quest state is: " + questState);
			logger.error("History doesn't have a matching quest state for " + questState);
			return debug;
	}

	@Override
	public String getName() {
		return "HelpMrsYeti";
	}

	@Override
	public int getMinLevel() {
		return 60;
	}

	@Override
	public String getNPCName() {
		return "耶提女士";
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_YETI_CAVE;
	}
}

