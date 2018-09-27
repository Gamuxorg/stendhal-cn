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
package games.stendhal.server.core.events;

/**
 * Event types used in the new Zone notifier.
 *
 * @author kymara (based on TutorialEventType by hendrik)
 */
public enum ZoneEventType {

	VISIT_SUB1_SEMOS_CATACOMBS(
			"尖叫和哀号声充斥在这些恐怖的墓室之中..."),
	VISIT_SUB2_SEMOS_CATACOMBS(
			"随着深入这个地下古墓, 你的神经越发紧崩, 你侦测到一些致命的长钉, 决心不能触动这些陷阱!"),
	VISIT_KIKAREUKIN_CAVE(
			"随着传门把你抛向高空, 穿过小鸟和白云, 你晕头转向, 然后被吸进一座空中群岛, 你被拉进层层的山石之中, 最终到达了网状的巨大石洞中. "),
	VISIT_KANMARARN_PRISON(
			"脱狱！你被暗算并遭到抢劫, 而这些 杜加矮人s 却从矮人看管的监狱中救出了他们的领袖和英雄. "),
	VISIT_IMPERIAL_CAVES(
			"远处传来了号令声和列队的声音. 你知道这肯定是很接近某一支部队, 你还听到一些重重的脚步声, 这真让人头疼."),
	VISIT_MAGIC_CITY_N(
			"随着冒险更进一步, 你的肌肤刺痛感硬强, 一定是魔法结界的作用. "),
	VISIT_MAGIC_CITY(
			"你现在感到强烈的魔力波动, 可能有魔法师或一些强力的魔法装置在附近. "),
	VISIT_SEMOS_CAVES(
			"巨兽的脚步造成了山洞地面的强列摇动！洞穴可能要塌, 不能再深入下去了, 撤退！快跑！"),
	VISIT_ADOS_CASTLE(
			  "你感觉这里发生过暴行, 这个城堡一定遭到了邪物的洗劫. 你耳中好像听到了他们临终前的哀号. 也许远离是非才是上策.");

	private String message;

	/**
	 * create a new ZoneEventType.
	 *
	 * @param message
	 *            human readable message
	 */
	private ZoneEventType(final String message) {
		this.message = message;
	}

	/**
	 * get the descriptive message.
	 *
	 * @return message
	 */
	String getMessage() {
		return message;
	}
}
