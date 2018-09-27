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
import java.util.List;

import org.apache.log4j.Logger;

//import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.entity.item.scroll.TwilightMossScroll;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.mithrilcloak.MithrilCloakQuestChain;

/**
 * QUEST: 密银斗篷
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li>Ida, a seamstress in Ados.</li>
 * <li>帝国科学家s, in kalavan basement</li>
 * <li>Mithrilbourgh wizards, in kirdneh and magic city</li>
 * <li>Hogart, a retired master dwarf smith, forgotten below the dwarf mines in
 * Orril.</li>
 * <li>Terry, the dragon hatcher in 塞门洞穴.</li>
 * <li>Ritati Dragontracker, odds and ends buyer in ados abandoned keep</li>
 * <li>Pdiddi, the dodgy dealer from Semos</li>
 * <li>Josephine, young woman from Fado</li>
 * <li>Pedinghaus, the mithril casting wizard in Ados</li>
 * </ul>
 * <p>
 * STEPS:
 * <ul>
 * <li>Ida needs sewing machine fixed, with one of three items from a list</li>
 * <li>Once machine fixed and if you have done 密银盾 quest, Ida offers you cloak</li>
 * <li>Kampusch tells you to how to make the fabric</li>
 * <li>帝国科学家s take 蜘蛛丝腺s and make 丝线</li>
 * <li>Kampusch fuses 密银矿石s into the 丝线</li>
 * <li>Whiggins weaves 密银线 into 密银布</li>
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
public class MithrilCloak extends AbstractQuest {
	private static final String QUEST_SLOT = "mithril_cloak";

	private static Logger logger = Logger.getLogger(MithrilCloak.class);

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"密银斗篷",
				"随着女裁缝Ida给我的的长长的任务列表一件件完成, 一件银亮且防御很高的斗蓬就要完工了, ",
				false);

		// login notifier to teleport away players logging into the twilight zone.
		SingletonRepository.getLoginNotifier().addListener(new LoginListener() {
			@Override
			public void onLoggedIn(final Player player) {
				TwilightMossScroll scroll = (TwilightMossScroll) SingletonRepository.getEntityManager().getItem("暮光苔藓");
				scroll.teleportBack(player);
			}

		});

		MithrilCloakQuestChain mithrilcloak = new MithrilCloakQuestChain();
		mithrilcloak.addToWorld();
	}


	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("我在Ados的裁缝屋见到了Ida. ");
		if (questState.equals("rejected")) {
			res.add("我没兴趣帮助Ida.");
			return res;
		}
		res.add("Ida的缝纫机坏了, 她让我帮忙找丢失的零件.");
		if (questState.startsWith("machine")) {
			res.add("我需要给Ida找到 " + player.getRequiredItemName(QUEST_SLOT,1) + ".");
			return res;
		}
		res.add("我带来了零件修复Ida的缝纫机.");
		if (questState.equals("need_mithril_shield")) {
			res.add("在寻找密银斗蓬的过程中, 我必须先拿到密银斗蓬, 才能更进一步. ");
			return res;
		}
		if (questState.equals("fixed_machine")) {
			return res;
		}
		res.add("我的斗蓬还需要密银丝线 密银布, Kampusch 会帮我弄到, 他知道我需要的东西. ");
		if (questState.equals("need_fabric")) {
			return res;
		}
		res.add("Vincento 价值是由我带给他的丝腺体抽丝带来. ");
		if (questState.startsWith("makingthread;")) {
			// optionally could add if time is still remaining or if it's ready to collect (timestamp in index 1 of questslot)
			return res;
		}
		res.add("我从 Vincento 的学生 Boris Karlova 处拿了一卷丝线, 交给Kampusch. ");
		if (questState.equals("got_thread")) {
			return res;
		}
		res.add("Kampusch 把密银片熔化到丝线上.");
		if (questState.startsWith("fusingthread;")) {
			// optionally could add if time is still remaining or if it's ready to collect (timestamp in index 1 of questslot)
			return res;
		}
		res.add("Whiggins 可以把 mithril 丝编织进 fabric, 我必须找他帮忙. ");
		if (questState.equals("got_mithril_thread")) {
			return res;
		}
		res.add("在 Whiggins 帮我之前, 我必须带着信去 Pedinghaus. Whiggins 好像遇到大麻烦了.");
		if (questState.equals("taking_letter")) {
			return res;
		}
		res.add("我带着信给 Pedinghaus 看完, 我最好还是告诉 Whiggins 事情办好了, 这样我才能拿到我的织物 fabric.");
		if (questState.equals("took_letter")) {
			return res;
		}
		res.add("Whiggins 为我的斗蓬正在编织 密银布!.");
		if (questState.startsWith("weavingfabric;")) {
			// optionally could add if time is still remaining or if it's ready to collect (timestamp in index 1 of questslot)
			return res;
		}
		res.add("我从 Whiggins 那里拿到了 密银布, 然后我要带着它去见Ida. ");
		if (questState.equals("got_fabric")) {
			return res;
		}
		res.add("Ida 不能使用普通的剪刀剪断 fabric 编织物! 我不得不云向 Hogart 做一把魔法剪刀.");
		if (questState.equals("need_scissors")) {
			return res;
		}
		res.add("Hogart 需要我带给他一块铁块 铁锭, 一块密银块 a密银锭, 和一点魔法蛋壳 附魔蛋壳.");
		if (questState.startsWith("need_eggshells;")) {
			// the quest slot knows how many eggshells were needed.
			return res;
		}
		res.add("Hogart 正在用我给他的东西制造魔法剪刀 .");
		if (questState.startsWith("makingscissors;")) {
			// optionally could add if time is still remaining or if it's ready to collect (timestamp in index 1 of questslot)
			return res;
		}
		res.add("我应该把魔法剪刀 魔法剪刀带给 Ida.");
		if (questState.equals("got_scissors")) {
			return res;
		}
		res.add("Ida 需要一把魔法剪刀才能做出斗篷.");
		if (questState.startsWith("need_needle") || questState.startsWith("told_joke;")) {
			//  quest slot knows how many needles are still needed to take and which joke was told last
			return res;
		}
		res.add("Ida 正在为我制作斗篷!");
		if (questState.startsWith("sewing;")) {
			// optionally could add if time is still remaining or if it's ready to collect (timestamp in index 1 of questslot)
			// number of needles still remaining is in slot 2
			// don't bother with adding info about the looping (needle breaking and sewing again)
			return res;
		}
		res.add("Ida 遇到点麻烦, 她不小心用针弄破了手指, 现在她精神失常, 我必须到 twilight 地带拜访她. ");
		if (questState.equals("twilight_zone")) {
			return res;
		}
		res.add("我给I gave Ida the 莹光药剂 to restore her health. But she got behind on her other jobs. Now I must go and find a 蓝色条纹斗篷 to take Josephine before Ida can work for me.");
		res.add("我给了 Ida twilight 药剂恢复她的神智, 但她不敢再去工作, 在 ida 可以工作之前, 我必须离开并找到蓝色丝带斗篷带给 Josephine .");
		if (questState.equals("taking_striped_cloak")) {
			return res;
		}
		res.add("Jospehine 很开发的拿着她的丝带斗篷. 我应该让 Ida 知道.");
		if (questState.equals("gave_striped_cloak")) {
			return res;
		}
		res.add("我的斗篷几近完工, 我现在要的是 Pedinghaus 生产的密银扣子.");
		if (questState.equals("need_clasp")) {
			return res;
		}
		res.add("Pedinghaus 正在锻造密银扣子 密银胸针. 我等不及了!");
		if (questState.startsWith("forgingclasp;")) {
			// optionally could add if time is still remaining or if it's ready to collect (timestamp in index 1 of questslot)
			return res;
		}
		res.add("我得到斗篷上的密银扣子, 我需要把它们带给Ida.");
		if (questState.equals("got_clasp")) {
			return res;
		}
		res.add("最终, 我总算穿上了这华丽丽的密银斗篷!");
		if (questState.equals("done")) {
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
		return "MithrilCloak";
	}

	// it's a long quest so they can always start it before they can necessarily finish all
	@Override
	public int getMinLevel() {
		return 100;
	}

	// Not sure about this one. it would make an achievement for all quests in 阿多斯城, quite hard
	@Override
	public String getRegion() {
		return Region.ADOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Ida";
	}
}
