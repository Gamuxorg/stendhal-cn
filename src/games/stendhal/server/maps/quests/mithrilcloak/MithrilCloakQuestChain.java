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
package games.stendhal.server.maps.quests.mithrilcloak;

/**
 * QUEST: 密银斗篷
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li>Ida, a seamstress in Ados.</li>
 * <li>Imperial scientists, in kalavan basement</li>
 * <li>Mithrilbourgh wizards, in kirdneh and magic city</li>
 * <li>Hogart, a retired master dwarf smith, forgotten below the dwarf mines in
 * Orril.</li>
 * <li>Terry, the dragon hatcher in 塞门洞穴.</li>
 * <li>Ritati Dragontracker, odds and ends buyer in ados abandoned keep</li>
 * <li>Pdiddi, the dodgy dealer from 塞门镇</li>
 * <li>Josephine, young woman from Fado</li>
 * <li>Pedinghaus, the mithril casting wizard in Ados</li>
 * </ul>
 * <p>
 * STEPS:
 * <ul>
 * <li>Ida needs sewing machine fixed, with one of three items from a list</li>
 * <li>Once machine fixed and if you have done 密银盾 quest, Ida offers you cloak</li>
 * <li>Kampusch tells you to how to make the fabric</li>
 * <li>Imperial scientists take silk glands and make silk thread</li>
 * <li>Kampusch fuses mithril nuggets into the silk thread</li>
 * <li>Whiggins weaves mithril thread into mithril fabric</li>
 * <li>Ida takes fabric then asks for scissors</li>
 * <li>Hogart makes the scissors which need eggshells</li>
 * <li>Terry swaps eggshells for poisons</li>
 * <li>Ida takes the scissors then asks for needles</li>
 * <li>Needles come from Ritati Dragontracker</li>
 * <li>Ida breaks a random number of needles, meaning you need to get more each time</li>
 * <li>Ida pricks her finger on the last needle and goes to twilight zone</li>
 * <li>Pdiddi sells the moss to get to twilight zone</li>
 * <li>A creature in the twilight zone drops the elixir to heal lda</li>
 * <li>After being ill Ida asks you to take a 蓝色条纹斗篷 to Josephine</li>
 * <li>After taking cloak to Josephine and telling Ida she asks for 密银胸针</li>
 * <li>Pedinghaus makes 密银胸针</li>
 * <li>The clasp completes the cloak</li>
 * </ul>
 * <p>
 * REWARD:
 * <ul>
 * <li>密银斗篷</li>
 * <li> XP</li>
 * <li> Karma</li>
 * </ul>
 * <p>
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 *
 * @author kymara
*/

public class MithrilCloakQuestChain  {
	private static MithrilCloakQuestInfo mithrilcloak = new MithrilCloakQuestInfo();

	public void addToWorld() {
		new InitialSteps(mithrilcloak).addToWorld();
		new MakingFabric(mithrilcloak).addToWorld();
		new GettingTools(mithrilcloak).addToWorld();
		new TwilightZone(mithrilcloak).addToWorld();
		new CloakForJosephine(mithrilcloak).addToWorld();
		new MakingClasp(mithrilcloak).addToWorld();
	}

}
