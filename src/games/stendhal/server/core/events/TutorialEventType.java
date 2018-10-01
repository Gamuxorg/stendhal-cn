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
 * Event types used in the tutorial.
 *
 * @author hendrik
 */
public enum TutorialEventType {

	FIRST_LOGIN(
			"嗨, 欢迎来到 Stendhal 游戏世界. 您可以使用键盘方向键或点击鼠标移动角色."),
	FIRST_MOVE(
			"想和 海云那冉 说话, 请输入 \"hi\" 或者 \"嗨\" , \"你好\" , \"喂\" ."),
	RETURN_GUARDHOUSE(
			"再次与 海云那冉 再次对话, 请接着说 \"hi\" 或者 \"嗨\" , \"你好\" , \"喂\" ."),
	VISIT_SEMOS_CITY(
			"你可以去 塞门镇 的 梦金斯 要一份地图. 同样也是对着他说 \"hi\" 或者 \"嗨\" , \"你好\" , \"喂\" . 或者你也可以下到地牢中消灭怪兽."),
	VISIT_SEMOS_DUNGEON(
			"记得在你消灭怪兽时要吃正餐, 双点干酪、肉或者你背包中的其它的食物. "),
	VISIT_SEMOS_DUNGEON_2(
			"要小心. 如果你去地下的层次越深, 怪物就越强. 你还可以回到 塞门镇 上找 卡蔓 为你治疗."),
	VISIT_SEMOS_TAVERN(
			"你可以对着 NPC 说 \"hi\" , 然后问他们能提供什么 \"offer\" . 如果你想买一个生命药水, 就对他说 \"buy flask\"."),
	VISIT_SEMOS_PLAINS(
			"吃正餐是回复生命的基本方式, 如果你的食物不够, 可以去东北方向的农场找些吃的. "),
	FIRST_ATTACKED(
			"那个带着黄色光环的怪兽向你发起进攻！鼠标点击即可攻击它."),
	FIRST_KILL(
			"点击尸体中的物品, 把它们放进你的背包."),
    FIRST_PLAYER_KILL(
			"你已被某个玩家打上红色骷髅的标记, 意思是玩家杀手. 然后你会发现其他玩家开始警惕你. 要去除这个标记, 你可以找 塞门镇 神庙的 艾欧弗鲁托 谈谈"),
	FIRST_POISONED(
			"你已中毒. 或许你没有喝毒药, 也可能是野外的带毒生物攻击你造成的. 快杀了这个毒物. 在中毒期间, 你会慢慢失去生命值. "),
	FIRST_PLAYER(
			"你注意到那个带白色名字的人吗? 这是其他真实玩家."),
	FIRST_DEATH(
			"Oh, 你死了. 但很幸运, 在这个世界上死亡只是暂时的. "),
	FIRST_PRIVATE_MESSAGE(
			"你收到一条私信. 回复请打 #/msg #name #message."),
	FIRST_EQUIPPED(
			"你得到某些物品！请检查你的背包和装备. "),
	TIMED_HELP(
			"按 F1 查看图文手册. "),
	TIMED_NAKED(
			"Oh, 你不冷吗？对自已按鼠标右键, 然后选择 \"打扮\" 可以换装."),
	TIMED_PASSWORD(
			"记得可保证你的密码安全. 不要把密码告诉其他的朋友、玩家甚至管理员. "),
	TIMED_OUTFIT(
			"不喜欢你的外形？想改变一下, 对自已按鼠标右键, 然后选择 \"打扮\" 可以换装, 还可以换脸, 头发等等. "),
	TIMED_RULES(
			"感谢你继续支持本游戏. 现在你已玩有些时间, 了解玩法很重要, 请输入 #/rules 在浏览器中查看详细规则！.");
	private String message;

	/**
	 * Creates a new TutorialEventType.
	 *
	 * @param message
	 *            human readable message
	 */
	private TutorialEventType(final String message) {
		this.message = message;
	}

	/**
	 * Gets the descriptive message.
	 *
	 * @return message
	 */
	String getMessage() {
		return message;
	}
}
