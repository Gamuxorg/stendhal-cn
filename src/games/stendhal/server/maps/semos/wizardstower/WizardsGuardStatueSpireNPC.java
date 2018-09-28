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
package games.stendhal.server.maps.semos.wizardstower;

import java.util.Arrays;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;

/**
 * Zekiel, the guardian statue of the Wizards Tower (Zekiel in the spire)
 *
 * @see games.stendhal.server.maps.quests.ZekielsPracticalTestQuest
 * @see games.stendhal.server.maps.semos.wizardstower.WizardsGuardStatueNPC
 */
public class WizardsGuardStatueSpireNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildZekielSpire(zone);
	}

	private void buildZekielSpire(final StendhalRPZone zone) {
		final SpeakerNPC zekielspire = new SpeakerNPC("Zekiel") {

			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("Greetings again, adventurer!");
				addHelp("You are stood in the #store. You can enter the spire by the teleporter in front of me. The one behind me teleports you back to the tower entrance.");
				addJob("I am the guardian and #storekeeper of the #wizards tower.");
				addGoodbye("So long!");
				addOffer("I can create #special items with the materials from the store. Just tell me what you want, but for most items I will need extra ingredients.");
				addReply(Arrays.asList("store", "storekeeper"),
				        "I can create #special items with the materials from the store. Just tell me what you want, but for most items I will need extra ingredients.");

				addReply("special",
				        "For now I can create #Demon #Fire #Swords, and #Enhanced #Lion #Shields. I could read in your mind, adventurer, but it is not allowed of me here. So you have to tell me which special item you want and I will tell you, if I can help you.");
//				addReply("special",
//				        "I am sorry, now is not the time. Try again in some weeks, and I may be ready to help you.");

				addReply(Arrays.asList("wizard", "wizards"),
				        "Seven wizards form the wizards circle. These are #伊拉斯塔斯, #Elana, #拉瓦夏克, #Jaer, #Cassandra, #Silvanus and #Malleus");
				addReply("伊拉斯塔斯", "伊拉斯塔斯 is the archmage of the wizards circle. He is the grandmaster of all magics and the wisest person that is known. He is the only one without a part in the practical test.");
				addReply("elana", "Elana is the warmest and friendliest enchantress. She is the protectress of all living creatures and uses divine magic to save and heal them.");
				addReply("拉瓦夏克", "拉瓦夏克 is a very mighty necromancer. He has studied the dark magic for ages. 拉瓦夏克 is a mystery, using dark magic to gain the upper hand on his opponents, but fighting the evil 巫妖es, his arch enemies.");
				addReply("jaer", "Jaer is the master of illusion. Charming and flighty like a breeze on a hot summer day. His domain is Air and he has many allies in the plains of mythical ghosts.");
				addReply("cassandra", "Cassandra is a beautiful woman, but foremost a powerful sorceress. Cassandra's domain is Water and she can be cold like ice to achieve her aim.");
				addReply("silvanus", "Silvanus is a sage druid and perhaps the eldest of all elves. He is a friend of all animals, trees, fairy creatures and ents. His domain is Earth and Nature.");
				addReply("malleus", "Malleus is the powerful archetype of a magician and the master of destructive magics. His domain is Fire and he rambled the plains of demons for ages, to understand their ambitions.");

				//behavior for enhancing lion shield
				add(ConversationStates.ATTENDING,
						Arrays.asList("加强狮盾", "shields", "shield"),
						ConversationStates.INFORMATION_1,
					    "I can turn a 钢盾 into an 加强狮盾 with 铁锭, but I need eight pieces of 铁锭 and the shield to do that. Do you want an 加强狮盾?",
					    null);
				add(ConversationStates.INFORMATION_1,
						ConversationPhrases.YES_MESSAGES,
						new AndCondition(
								new NotCondition(new PlayerHasItemWithHimCondition("铁锭", 8)),
								new PlayerHasItemWithHimCondition("钢盾", 1)),
						ConversationStates.ATTENDING,
						"You don't have enough 铁锭, I will need 8 铁锭s and a 钢盾.",
						null);
				add(ConversationStates.INFORMATION_1,
						ConversationPhrases.YES_MESSAGES,
						new AndCondition(
								new NotCondition(new PlayerHasItemWithHimCondition("钢盾", 1)),
								new PlayerHasItemWithHimCondition("铁锭", 8)),
						ConversationStates.ATTENDING,
						"You do not have a shield for me to enhance, I will need 8 铁锭s and a 钢盾.",
						null);
				add(ConversationStates.INFORMATION_1,
						ConversationPhrases.YES_MESSAGES,
						new AndCondition(
								new PlayerHasItemWithHimCondition("铁锭", 8),
								new PlayerHasItemWithHimCondition("钢盾", 1)),
						ConversationStates.ATTENDING,
						"There is your 加强狮盾.",
						new MultipleActions(
							new DropItemAction("铁锭", 8),
							new DropItemAction("钢盾", 1),
							new EquipItemAction("加强狮盾", 1, true),
							new IncreaseXPAction(250)));
					add(ConversationStates.INFORMATION_1,
						ConversationPhrases.NO_MESSAGES,
						null,
						ConversationStates.ATTENDING,
						"Fine. Just tell me when you want an 加强狮盾.",
						null);

					//behavior for forging a 赤魔剑
					add(ConversationStates.ATTENDING,
							Arrays.asList("赤魔剑", "swords", "铁剑"),
							ConversationStates.INFORMATION_1,
						    "I can craft for you a 赤魔剑 if you can procure a 恶魔剑 and a 烈火剑.",
						    null);
					add(ConversationStates.INFORMATION_1,
							ConversationPhrases.YES_MESSAGES,
							new AndCondition(
									new NotCondition(new PlayerHasItemWithHimCondition("烈火剑", 1)),
									new PlayerHasItemWithHimCondition("恶魔剑", 1)),
							ConversationStates.ATTENDING,
							"You don't have a 烈火剑, I need both a 恶魔剑 and a 烈火剑.",
							null);
					add(ConversationStates.INFORMATION_1,
							ConversationPhrases.YES_MESSAGES,
							new AndCondition(
									new NotCondition(new PlayerHasItemWithHimCondition("恶魔剑", 1)),
									new PlayerHasItemWithHimCondition("烈火剑", 1)),
							ConversationStates.ATTENDING,
							"You don't have a 恶魔剑, I need both a 烈火剑 and a 恶魔剑.",
							null);
					add(ConversationStates.INFORMATION_1,
							ConversationPhrases.YES_MESSAGES,
							new AndCondition(
									new PlayerHasItemWithHimCondition("恶魔剑", 1),
									new PlayerHasItemWithHimCondition("烈火剑", 1)),
							ConversationStates.ATTENDING,
							"There is your 赤魔剑.",
							new MultipleActions(
								new DropItemAction("恶魔剑", 1),
								new DropItemAction("烈火剑", 1),
								new EquipItemAction("赤魔剑", 1, true),
								new IncreaseXPAction(11250)));
						add(ConversationStates.INFORMATION_1,
							ConversationPhrases.NO_MESSAGES,
							null,
							ConversationStates.ATTENDING,
							"Fine. Just tell me when you want to forge a 赤魔剑.",
							null);

/**				// behavior on special item 空白卷轴
				add(ConversationStates.ATTENDING,
				    Arrays.asList("空白卷轴", "scrolls"),
				    ConversationStates.INFORMATION_1,
				    "I will create a 空白卷轴 for you, but I need eight pieces of wood for that. The 空白卷轴 can be enchanted by wizards. Do you want a 空白卷轴?",
				    null);
				add(ConversationStates.INFORMATION_1,
					ConversationPhrases.YES_MESSAGES,
					new NotCondition(new PlayerHasItemWithHimCondition("木头", 8)),
					ConversationStates.ATTENDING,
					"You don't have enough wood, I will need eight pieces.",
					null);
				add(ConversationStates.INFORMATION_1,
					ConversationPhrases.YES_MESSAGES,
					new PlayerHasItemWithHimCondition("木头", 8),
					ConversationStates.ATTENDING,
					"There is your 空白卷轴.",
					new MultipleActions(
						new DropItemAction("木头", 8),
						new EquipItemAction("空白卷轴", 1, true),
						new IncreaseXPAction(250)));
				add(ConversationStates.INFORMATION_1,
					ConversationPhrases.NO_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"Well, maybe later. Just tell me when you want a 空白卷轴.",
					null);

				//behavior on special item 裂缝斗篷
				add(ConversationStates.ATTENDING,
				    Arrays.asList("裂缝斗篷"),
				    ConversationStates.INFORMATION_2,
				    "I will create a 裂缝斗篷 for you, but I have to fuse a 红宝石 and a 蓝宝石 in the magic. The cloak is useless in battle and will protect you only one time, when entering a magical rift."+
					" The rift disintegrates the cloak instead of you. There is no way to get the cloak back. If you want to enter the rift again, you will need a new 裂缝斗篷. Shall I create one for you?",
				     null);
				add(ConversationStates.INFORMATION_2,
					ConversationPhrases.YES_MESSAGES,
					new AndCondition(
							new NotCondition(new PlayerHasItemWithHimCondition("红宝石", 1)),
							new PlayerHasItemWithHimCondition("蓝宝石", 1)),
					ConversationStates.ATTENDING,
					"You don't have a 红宝石, I will need a 蓝宝石 and a 红宝石.",
					null);
				add(ConversationStates.INFORMATION_2,
					ConversationPhrases.YES_MESSAGES,
					new AndCondition(
							new NotCondition(new PlayerHasItemWithHimCondition("蓝宝石", 1)),
							new PlayerHasItemWithHimCondition("红宝石", 1)),
					ConversationStates.ATTENDING,
					"You don't have a 蓝宝石, I will need a 红宝石 and a 蓝宝石.",
					null);
				add(ConversationStates.INFORMATION_2, ConversationPhrases.YES_MESSAGES,
						new AndCondition(
								new PlayerHasItemWithHimCondition("蓝宝石", 1),
								new PlayerHasItemWithHimCondition("红宝石", 1)),
					ConversationStates.ATTENDING,
					"There is your 裂缝斗篷. Don't forget that it protects you only one time, before it is destroyed. So be sure that you are ready for what awaits you in the rift.",
					new MultipleActions(
							new DropItemAction("红宝石", 1),
							new DropItemAction("蓝宝石", 1),
							new EquipItemAction("裂缝斗篷", 1, true),
							new IncreaseXPAction(5000)));
				add(ConversationStates.INFORMATION_2,
					ConversationPhrases.NO_MESSAGES,
					null,
					ConversationStates.ATTENDING,
					"Don't forget that you can't enter a magical rift without a 裂缝斗篷.",
					null);
*/

				//behavior on special item XARUHWAIYZ PHIAL
			} //remaining behavior defined in maps.quests.ZekielsPracticalTestQuest
		};

		zekielspire.setDescription("You see Zekiel, the guardian of this tower.");
		zekielspire.setEntityClass("transparentnpc");
		zekielspire.setAlternativeImage("zekiel");
		zekielspire.setPosition(15, 15);
		zekielspire.initHP(100);
		zone.add(zekielspire);
	}
}
