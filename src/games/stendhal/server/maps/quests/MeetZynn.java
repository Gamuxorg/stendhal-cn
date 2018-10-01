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
import java.util.List;

import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Speak with Zynn PARTICIPANTS: - Zynn
 *
 * STEPS: - Talk to Zynn to activate the quest and keep speaking with Zynn.
 *
 * REWARD: - 10 XP (check that user's level is lower than 5) - 5 gold
 * REPETITIONS: - As much as wanted.
 */
public class MeetZynn extends AbstractQuest {
	@Override
	public String getSlotName() {
		return "meetzynn";
	}
	private void step_1() {

		final SpeakerNPC npc = npcs.get("震爱武豪斯");

		/**
		 * Quest can always be started again. Just check that no reward is given
		 * for players higher than level 15.
		 */

		npc
				.addReply(
						"history",
						"目前看, 在 Faiumoni 内有两股势力；Deniran 帝国, 和 Blordrough 的黑暗军团. Blordrough 最近征服了岛的南半部, 夺取了几个铁矿和一座大金矿. 现在 Deniran 仍控制着 Faiumoni 的中部和北部, 包括几座金矿和密银矿 mithril mines.");

		npc
				.addReply(
						"news",
						"Deniran 帝国正招募雇佣军以扩充军队, 重新夺回被 Blordrough 抢去的南部地区. 但很不幸 Blordrough 的黑暗军队也在顽强地对抗着帝国的进攻. ");

		npc
				.addReply(
						"geography",
						"接下来谈谈在 Faiumoni #地区 你还可以活动的地方！我可以帮你 #取得 和 #使用 地图, 或者帮你弄清使用心灵 #SPS 系统的方法. ");

		npc
				.addReply(
						"地区",
						"#Faiumoni 最重要的地区包括 #Or'ril 城堡, #塞门镇, #阿多斯, #Nalwor, 也然也包括 #Deniran 城.");

		npc
				.addReply(
						"Faiumoni",
						"Faiumoni 就是你脚下的这片岛！你可能已注意北部的山地, 还有岛中部的沙漠, 还有一条东西走向的大河把岛分成两半, 而河的南只有 #Or'ril 城堡.");

		npc
				.addReply(
						"塞门镇",
						"塞门镇就是你现在的所在地. 我们都在 Faiumoni 的北半部, 人口大约有 40-50 人.");

		npc
				.addReply(
						"阿多斯",
						"阿多斯是一个重要海滨城市, 在 #塞门镇 的东方. 商人再通过那里出口到 #Deniran. 这条通道是帝国最重要的航线之一. ");

		npc
				.addReply(
						"Or'ril",
						"Or'ril Castle is one of a series of such castles built to defend the imperial road between #Ados and #Deniran. When in use, it housed a fighting force of around 60 swordsmen, plus ancillary staff. Now that most of the Empire's army has been sent south to fend off the invaders, the castle has been abandoned by the military. As far as I'm aware, it should be empty; I hear some rumours, though...");

		npc
				.addReply(
						"Nalwor",
						"Nalwor is an ancient elven city, built deep inside the forest to the southeast of us long before humans ever arrived on this island. Elves don't like mixing with other races, but we're given to understand that Nalwor was built to help defend their capital city of Teruykeh against an evil force.");

		npc
				.addReply(
						"Deniran",
						"是帝国的首都, Faiumoni 的核心地带, , and is the main base of operations for the Deniran army. Most of the Empire's main trade routes with other countries originate in this city, then extending north through #Ados, and south to Sikhw. Unfortunately, the southern routes were been destroyed when Blordrough and his armies conquered Sikhw, some time ago now.");

		npc
				.addReply(
						"使用",
						"一旦你 #取得 一张地图, 上面有三种刻度, 一是地图的 #层级, #levels, 然后你要自己弄清楚不同地区的层级的 ＃命名 规则 #naming conventions, 最后你要学会描述某人在某地区的 ＃方位  #positioning . 然后你就可以快速定位!");

		npc
				.addReply(
						"层级",
						"地图会根据地表的上或下划分层级. 地表正常级别是 0. 级别数字在每个地图名字的最前面, 举例说明 #塞门镇 就在地表 0级, 地图表示为 \"0_塞门_镇\". 而地下的地牢一层表示为： \"-1_塞门_地牢\". 一定要记住. 而在建筑物内部也会有层级的名称, 一般在最前面标记为 \"int\" (内部的意思). 举个例子酒馆二楼标记为： \"int_塞门_酒馆_1\".");

		npc
				.addReply(
						"命名",
						"地图通常被分为各个 \"地区\" , 较中心的地点常被用作基准原点. 地区周边地带使用它相对基准原点的方向的方式记录. 举个例子. 有个基准地区叫 \"0_塞门_镇\", 你向西走到旧村庄的名称记为 \"0_塞门_村庄_西\", 或者你往北走两格, 再向西走一格地图到达山区, 名称就记为 \"0_塞门_山_北2_西\".");

		npc
				.addReply(
						"positioning",
						"Positioning within a zone is simply described in terms of co-ordinates. We conventionally take the top-left corner (that is, the northwest corner) to be the origin point with co-ordinates (0, 0). The first co-ordinate ('x') increases as you move to the right (that is, east) within the zone, and the second co-ordinate ('y') increases as you move down (that is, south).");

		npc
				.addReply(
						"取得",
						"你可以在 #https://stendhalgame.org/world/atlas.html 查看到 Stendhal 的世界地图.  Careful you don't spoil any surprises for yourself, though!");

		npc
				.addReply(
						"SPS",
						"SPS 是Stendhal的定位系统；你可以去教堂的 #Io 询问有关这东西工作原理和细节. 基本来说, 它可以在任意时间查到你或朋友的精确位置. ");

		npc
				.addReply(
						"Io",
						"它的全名是 \"艾欧弗鲁托\".她大部分时间都在教堂, 嗯...漂着...她可能有些怪异. 但她的直觉 \"intuition\" 工作远远好过任何指示器, 正如我讲的那样. ");

		/**
		 * I still have to think of a way to reward a good amount of XP to the
		 * most interested player for this long reading... How about keeping a
		 * list of all the things the player has asked and reward him when the
		 * list is complete?
		 */
		npc.add(ConversationStates.ATTENDING, "再见",
			new LevelLessThanCondition(15),
			ConversationStates.IDLE,
			"再见, 如果你去图书馆逛逛, 请保持安静；其他人正在学习！",
			null);

		npc.add(ConversationStates.ATTENDING, "再见",
			new LevelGreaterThanCondition(14),
			ConversationStates.IDLE,
			"再见, 你要知道, 你应该考虑办一张会员卡. ",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Meet 震爱武豪斯",
				"震爱武豪斯, 在塞门镇图书管工作, 掌握了相当多的实用信息资源. ",
				false);
		step_1();
	}

	@Override
	public String getName() {
		return "MeetZynn";
	}

	// no quest slots ever get set so making it visible seems silly
	// however, there is an entry for another quest slot in the games.stendhal.server.maps.semos.library.HistorianGeographerNPC file
	@Override
	public boolean isVisibleOnQuestStatus() {
		return false;
	}

	@Override
	public List<String> getHistory(final Player player) {
		return new ArrayList<String>();
	}

	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}
	@Override
	public String getNPCName() {
		return "震爱武豪斯";
	}
}
