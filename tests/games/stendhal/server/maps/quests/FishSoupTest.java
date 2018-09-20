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

import static org.junit.Assert.assertEquals;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.ados.market.FishSoupMakerNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.RPClass.ItemTestHelper;

public class FishSoupTest {

	private static final String QUEST_SLOT = "fishsoup_maker";

	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		ItemTestHelper.generateRPClasses();
	}

	@Before
	public void setUp() {
		final StendhalRPZone zone = new StendhalRPZone("0_阿多斯_城_n2");
		MockStendlRPWorld.get().addRPZone(zone);
		new FishSoupMakerNPC().configureZone(zone, null);


		AbstractQuest quest = new FishSoup();
		quest.addToWorld();

		player = PlayerTestHelper.createPlayer("bob");
	}

	@Test
	public void testQuest() {

		npc = SingletonRepository.getNPCList().get("Florence Boullabaisse");
		en = npc.getEngine();
		player.setXP(100);
		en.step(player, "hi");
		assertEquals("Hello and welcome on Ados market! I have something really tasty and know what would #revive you.", getReply(npc));
		en.step(player, "revive");
		assertEquals("My special 鱼汤 has a magic touch. I need you to bring me the #ingredients.", getReply(npc));
		en.step(player, "ingredients");
		assertEquals("I need 11 ingredients before I make the soup: #surgeonfish, #cod, #char, #roach, #clownfish, #洋葱, #mackerel, #大蒜, #韭菜, #perch, and #西红柿. Will you collect them?", getReply(npc));
		en.step(player, "no");
		assertEquals("Oh, I hope you will change your mind another time. You'd definitely miss out!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Have a nice stay and day on Ados market!", getReply(npc));
		en.step(player, "hi");
		assertEquals("Hello and welcome on Ados market! I have something really tasty and know what would #revive you.", getReply(npc));
		en.step(player, "revive");
		assertEquals("My special 鱼汤 has a magic touch. I need you to bring me the #ingredients.", getReply(npc));
		en.step(player, "ingredients");
		assertEquals("I need 11 ingredients before I make the soup: #surgeonfish, #cod, #char, #roach, #clownfish, #洋葱, #mackerel, #大蒜, #韭菜, #perch, and #西红柿. Will you collect them?", getReply(npc));
		en.step(player, "yes");
		assertEquals("You made a good choice and I bet you'll not be disappointed. Do you have anything I need already?", getReply(npc));
		en.step(player, "yes");
		assertEquals("What did you bring?", getReply(npc));
		en.step(player, "韭菜");
		assertEquals("Oh come on, I don't have time for jokes! You don't have a 韭菜 with you.", getReply(npc));
		en.step(player, "洋葱");
		assertEquals("Oh come on, I don't have time for jokes! You don't have an 洋葱 with you.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Bye.", getReply(npc));
		PlayerTestHelper.equipWithItem(player, "韭菜");
		PlayerTestHelper.equipWithItem(player, "surgeonfish");
		PlayerTestHelper.equipWithItem(player, "cod");
		PlayerTestHelper.equipWithItem(player, "perch");
		PlayerTestHelper.equipWithItem(player, "mackerel");
		PlayerTestHelper.equipWithItem(player, "clownfish");
		PlayerTestHelper.equipWithItem(player, "西红柿");
		PlayerTestHelper.equipWithItem(player, "大蒜");
		PlayerTestHelper.equipWithItem(player, "char");
		en.step(player, "hi");
		assertEquals("Welcome back! I hope you collected some #ingredients for the 鱼汤, or #everything.", getReply(npc));
		en.step(player, "everything");
		assertEquals("You didn't have all the ingredients I need. I still need 2 ingredients: #roach and #洋葱. You'll get bad karma if you keep making mistakes like that!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Bye.", getReply(npc));
		en.step(player, "hi");
		assertEquals("Welcome back! I hope you collected some #ingredients for the 鱼汤, or #everything.", getReply(npc));
		en.step(player, "ingredients");
		assertEquals("I still need 2 ingredients: #roach and #洋葱. Did you bring anything I need?", getReply(npc));
		PlayerTestHelper.equipWithItem(player, "roach");
		PlayerTestHelper.equipWithItem(player, "洋葱");
		en.step(player, "roach");
		assertEquals("Thank you very much! What else did you bring?", getReply(npc));
		en.step(player, "洋葱");
		assertEquals("The soup's on the market table for you. It will heal you. My magical method in making the soup has given you a little karma too.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Have a nice stay and day on Ados market!", getReply(npc));
		assertEquals(player.getXP(), 150);
		en.step(player, "hi");
		assertEquals("Oh I am sorry, I have to wash my cooking pots first before making more soup for you. Please come back in 20 minutes.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Have a nice stay and day on Ados market!", getReply(npc));

		//Test when player has everything
		player.setQuest(QUEST_SLOT, "done;0");
		en.step(player, "hi");
		assertEquals("Hello again. Have you returned for more of my special 鱼汤?", getReply(npc));
		en.step(player, "yes");
		assertEquals("You made a good choice and I bet you'll not be disappointed. Do you have anything I need already?", getReply(npc));
		en.step(player, "yes");
		assertEquals("What did you bring?", getReply(npc));
		en.step(player, "韭菜");
		assertEquals("Oh come on, I don't have time for jokes! You don't have a 韭菜 with you.", getReply(npc));
		en.step(player, "洋葱");
		assertEquals("Oh come on, I don't have time for jokes! You don't have an 洋葱 with you.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Bye.", getReply(npc));
		PlayerTestHelper.equipWithItem(player, "韭菜");
		PlayerTestHelper.equipWithItem(player, "surgeonfish");
		PlayerTestHelper.equipWithItem(player, "cod");
		PlayerTestHelper.equipWithItem(player, "perch");
		PlayerTestHelper.equipWithItem(player, "mackerel");
		PlayerTestHelper.equipWithItem(player, "clownfish");
		PlayerTestHelper.equipWithItem(player, "西红柿");
		PlayerTestHelper.equipWithItem(player, "大蒜");
		PlayerTestHelper.equipWithItem(player, "char");
		PlayerTestHelper.equipWithItem(player, "洋葱");
		PlayerTestHelper.equipWithItem(player, "roach");
		en.step(player, "hi");
		assertEquals("Welcome back! I hope you collected some #ingredients for the 鱼汤, or #everything.", getReply(npc));
		en.step(player, "everything");
		assertEquals("The soup's on the market table for you, it will heal you. Tell me if I can help you with anything else.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Have a nice stay and day on Ados market!", getReply(npc));
		assertEquals(player.getXP(), 180);
		en.step(player, "hi");
		assertEquals("Oh I am sorry, I have to wash my cooking pots first before making more soup for you. Please come back in 20 minutes.", getReply(npc));
		en.step(player, "job");
		assertEquals("I am a trained cook but specialized into soups. My most favourite soup is a 鱼汤 but I also like normal ones...", getReply(npc));
		en.step(player, "offer");
		assertEquals("If you are really hungry or need some food for your travels, I can cook a really tasty 鱼汤 for you after a selfmade receipe.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Have a nice stay and day on Ados market!", getReply(npc));

	}
}
