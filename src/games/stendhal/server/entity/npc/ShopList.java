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
package games.stendhal.server.entity.npc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Singleton class that contains inventory and prices of NPC stores.
 */
public final class ShopList {

	static {
		final ShopList shops = ShopList.get();

		shops.add("food&drinks", "beer", 10);
		shops.add("food&drinks", "wine", 15);
		shops.add("food&drinks", "瓶子", 5);
		shops.add("food&drinks", "干酪", 20);
		shops.add("food&drinks", "apple", 10);
		shops.add("food&drinks", "carrot", 10);
		shops.add("food&drinks", "肉", 40);
		shops.add("food&drinks", "ham", 80);

		shops.add("adosfoodseller", "apple", 50);
		shops.add("adosfoodseller", "carrot", 50);

		shops.add("buyfood", "干酪", 5);
		shops.add("buyfood", "肉", 10);
		shops.add("buyfood", "spinach", 15);
		shops.add("buyfood", "ham", 20);
		shops.add("buyfood", "flour", 25);
		shops.add("buyfood", "porcini", 30);

		shops.add("healing", "antidote", 50);
		shops.add("healing", "minor potion", 100);
		shops.add("healing", "potion", 250);
		shops.add("healing", "greater potion", 500);

		shops.add("superhealing", "antidote", 50);
		shops.add("superhealing", "greater antidote", 100);
		shops.add("superhealing", "potion", 250);
		shops.add("superhealing", "greater potion", 500);
		shops.add("superhealing", "mega potion", 1500);

		shops.add("scrolls", "home scroll", 250);
		shops.add("scrolls", "召唤卷轴", 200);
		shops.add("scrolls", "空白卷轴", 2000);

		shops.add("fadoscrolls", "法多城回城卷", 600);
		shops.add("fadoscrolls", "空白卷轴", 2200);

		shops.add("nalworscrolls", "纳尔沃城回城卷", 400);
		shops.add("nalworscrolls", "空白卷轴", 2000);

		shops.add("adosscrolls", "阿多斯城回城卷", 400);
		shops.add("adosscrolls", "空白卷轴", 2000);

		shops.add("kirdnehscrolls", "kirdneh city scroll", 400);
		shops.add("kirdnehscrolls", "home scroll", 400);
		shops.add("kirdnehscrolls", "空白卷轴", 2000);

		shops.add("allscrolls", "home scroll", 250);
		shops.add("allscrolls", "召唤卷轴", 200);
		shops.add("allscrolls", "空白卷轴", 2000);
		shops.add("allscrolls", "阿多斯城回城卷", 400);
		shops.add("allscrolls", "纳尔沃城回城卷", 400);
		shops.add("allscrolls", "法多城回城卷", 600);
		shops.add("allscrolls", "kirdneh city scroll", 600);

		shops.add("sellstuff", "knife", 15);
		shops.add("sellstuff", "木棍", 10);
		shops.add("sellstuff", "dagger", 25);
		shops.add("sellstuff", "木盾", 25);
		shops.add("sellstuff", "", 25);
		shops.add("sellstuff", "leather helmet", 25);
		shops.add("sellstuff", "cloak", 30);
		shops.add("sellstuff", "leather legs", 35);

		shops.add("sellbetterstuff1", "蓝色盔甲", 14000);
		shops.add("sellbetterstuff1", "蓝靴子", 3000);
		shops.add("sellbetterstuff1", "蓝色条纹斗篷", 5000);
		shops.add("sellbetterstuff1", "blue helmet", 6000);
		shops.add("sellbetterstuff1", "blue legs", 6000);
		shops.add("sellbetterstuff1", "蓝盾", 20000);
		shops.add("sellbetterstuff1", "刺客匕首", 12000);

		shops.add("sellbetterstuff2", "影子铠甲", 18000);
		shops.add("sellbetterstuff2", "影子靴子", 4000);
		shops.add("sellbetterstuff2", "影子斗篷", 7000);
		shops.add("sellbetterstuff2", "shadow helmet", 8000);
		shops.add("sellbetterstuff2", "shadow legs", 10000);
		shops.add("sellbetterstuff2", "影盾", 30000);
		shops.add("sellbetterstuff2", "hell dagger", 20000);

		shops.add("sellrangedstuff", "木弓", 300);
		shops.add("sellrangedstuff", "木箭", 2);

		shops.add("buystuff", "short sword", 15);
		shops.add("buystuff", "sword", 60);
		shops.add("buystuff", "镶嵌盾", 20);
		shops.add("buystuff", "镶嵌甲", 22);
		shops.add("buystuff", "studded helmet", 17);
		shops.add("buystuff", "studded legs", 20);
		shops.add("buystuff", "链甲", 29);
		shops.add("buystuff", "chain helmet", 25);
		shops.add("buystuff", "chain legs", 27);

		shops.add("selltools", "小斧头", 15);
		shops.add("selltools", "手斧", 25);
		shops.add("selltools", "重斧", 40);
		// enable these if you need them for a quest or something
		// shops.add("selltools", "pick", 50);
		// shops.add("selltools", "shovel", 50);
		shops.add("selltools", "hammer", 60);
		// used for harvest grain.
		shops.add("selltools", "旧的大镰刀", 120);
        // for harvesting cane fields
		shops.add("selltools", "镰刀", 80);
		shops.add("selltools", "金盘子", 230);

		shops.add("buyiron", "iron", 75);

		shops.add("buygrain", "grain", 1);

		shops.add("sellrings", "订婚戒指", 5000);
		// gold and gemstones
		shops.add("buyprecious", "gold bar", 250);
		shops.add("buyprecious", "emerald", 200);
		shops.add("buyprecious", "sapphire", 400);
		shops.add("buyprecious", "carbuncle", 600);
		shops.add("buyprecious", "diamond", 800);
		shops.add("buyprecious", "obsidian", 1000);
		shops.add("buyprecious", "mithril bar", 2500);

		// rare weapons shop
		shops.add("buyrare", "scimitar", 65);
		shops.add("buyrare", "katana", 70);
		shops.add("buyrare", "大砍刀", 75);
		shops.add("buyrare", "黄金战锤", 80);

		// rare armor shop
		shops.add("buyrare", "加固锁子甲", 32);
		shops.add("buyrare", "黄金锁子甲", 52);
		shops.add("buyrare", "板甲", 62);
		shops.add("buyrare", "钢盾", 40);
		shops.add("buyrare", "狮盾", 50);

		// rare elf weapons buyer
		shops.add("elfbuyrare", "战斧", 70);
		shops.add("elfbuyrare", "双刃斧", 80);
		shops.add("elfbuyrare", "双刃剑", 90);
		shops.add("elfbuyrare", "阔剑", 100);
		shops.add("elfbuyrare", "staff", 75);
		shops.add("elfbuyrare", "加强狮盾", 100);
		shops.add("elfbuyrare", "王冠之盾", 120);

		// more rare weapons shop (fado)
		shops.add("buyrare2", "战锤", 120);
		shops.add("buyrare2", "突刺剑", 150);
		shops.add("buyrare2", "crossbow", 175);
		shops.add("buyrare2", "巨剑", 250);
		shops.add("buyrare2", "烈火剑", 2000);
		shops.add("buyrare2", "ice sword", 5000);
        shops.add("buyrare2", "hell dagger", 8000);

		// very rare armor shop (ados)
		shops.add("buyrare3", "golden legs", 3000);
		shops.add("buyrare3", "shadow legs", 5000);
		shops.add("buyrare3", "黄金铠甲", 7000);
		shops.add("buyrare3", "影子铠甲", 9000);
		shops.add("buyrare3", "金盾", 10000);
		shops.add("buyrare3", "影盾", 15000);

		// less rare armor shop (kobold city - kobolds drop some of these
		// things)
		shops.add("buystuff2", "鳞甲", 65);
		shops.add("buystuff2", "studded legs", 70);
		shops.add("buystuff2", "钉靴", 75);
		shops.add("buystuff2", "链靴", 100);
		shops.add("buystuff2", "骷髅盾", 100);
		shops.add("buystuff2", "独角盾", 125);
		shops.add("buystuff2", "viking helmet", 250);

		shops.add("sellstuff2", "皮靴", 50);
		shops.add("sellstuff2", "studded helmet", 60);
		shops.add("sellstuff2", "镶嵌盾", 80);
		shops.add("sellstuff2", "sword", 90);
		shops.add("sellstuff2", "矮人斗篷", 230);

		// cloaks shop
		shops.add("buycloaks", "蓝灵斗篷", 300);
		shops.add("buycloaks", "绿龙斗篷", 400);
		shops.add("buycloaks", "蓝龙斗篷", 2000);
		shops.add("buycloaks", "影子斗篷", 3000);
		shops.add("buycloaks", "黑龙斗篷", 4000);
		shops.add("buycloaks", "金斗篷", 5000);
		shops.add("buycloaks", "混沌斗篷", 10000);
		shops.add("buycloaks", "黑斗篷", 20000);

		// boots shop (mithrilbourgh)
		// Note the shop sign is done by hand in
		// games.stendhal.server.maps.mithrilbourgh.stores
		// Because I wanted to split boots and helmets
		// Please if you change anything, change also the sign (by hand)
		shops.add("boots&helm", "铁靴", 1000);
		shops.add("boots&helm", "金靴子", 1500);
		shops.add("boots&helm", "影子靴子", 2000);
		shops.add("boots&helm", "石靴", 2500);
		shops.add("boots&helm", "混沌靴", 4000);
        shops.add("boots&helm", "绿了吧唧的靴子", 6000);
        shops.add("boots&helm", "异种元素靴子", 8000);
        shops.add("boots&helm", "xeno helmet", 8000);

		// helmet shop (mithrilbourgh)
		// Note the shop sign is done by hand in
		// games.stendhal.server.maps.mithrilbourgh.stores
		shops.add("boots&helm", "golden helmet", 3000);
		shops.add("boots&helm", "shadow helmet", 4000);
		shops.add("boots&helm", "horned golden helmet", 5000);
		shops.add("boots&helm", "chaos helmet", 6000);
		shops.add("boots&helm", "magic chain helmet", 8000);
		shops.add("boots&helm", "black helmet", 10000);

		// buy axes (woodcutter)
		shops.add("buyaxe", "战戟", 2000);
		shops.add("buyaxe", "黄金双刃斧", 4000);
		shops.add("buyaxe", "魔法双刃斧", 6000);
		shops.add("buyaxe", "都灵之斧", 8000);
		shops.add("buyaxe", "黑镰刀", 9000);
		shops.add("buyaxe", "混沌斧", 10000);
		shops.add("buyaxe", "黑戟", 12000);

		// buy chaos items (scared dwarf, after quest)
		shops.add("buychaos", "chaos legs", 8000);
		shops.add("buychaos", "混沌之剑", 12000);
		shops.add("buychaos", "混乱之盾", 18000);
		shops.add("buychaos", "浑沌护甲", 20000);

		// buy elvish items (albino elf, after quest)
		shops.add("buyelvish", "精灵靴", 300);
		shops.add("buyelvish", "elvish legs", 300);
		shops.add("buyelvish", "精灵之剑", 800);
		shops.add("buyelvish", "精灵之盾", 1000);
		shops.add("buyelvish", "卓尔之剑", 1200);
		shops.add("buyelvish", "像小精灵的斗篷", 400);
		shops.add("buyelvish", "精灵护甲", 400);

		// magic items or 'relics' (witch in magic city)
		shops.add("buymagic", "恶魔剑", 4000);
		shops.add("buymagic", "暗之匕首", 8000);
		shops.add("buymagic", "liberty helmet", 8000);
		shops.add("buymagic", "immortal sword", 10000);
		shops.add("buymagic", "jewelled legs", 12000);
		shops.add("buymagic", "附魔钢盾", 16000);
		shops.add("buymagic", "附魔甲", 20000);

		// red items (supplier in sedah city)
		shops.add("buyred", "红铠甲", 300);
		shops.add("buyred", "红靴子", 200);
		shops.add("buyred", "红斗篷", 250);
		shops.add("buyred", "red helmet", 200);
		shops.add("buyred", "red legs", 200);
		shops.add("buyred", "红盾", 750);

		// mainio items (despot in mithrilbourgh throne room)
		shops.add("buymainio", "奇妙甲", 22000);
		shops.add("buymainio", "很棒的靴子", 4000);
		shops.add("buymainio", "华丽的斗篷", 12000);
		shops.add("buymainio", "mainio helmet", 8000);
		shops.add("buymainio", "mainio legs", 7000);
		shops.add("buymainio", "奇妙之盾", 16000);

		// assassinhq principal Femme Fatale)
		shops.add("buy4assassins", "小圆盾", 20);
		shops.add("buy4assassins", "aventail", 25);
		shops.add("buy4assassins", "robins hat", 30);
		shops.add("buy4assassins", "皮靴", 30);
		shops.add("buy4assassins", "矮人斗篷", 60);
		shops.add("buy4assassins", "矮人护甲", 17000);
		shops.add("buy4assassins", "dwarvish legs", 15000);
		shops.add("buy4assassins", "刺客匕首", 7000);

		// mountain dwarf buyer of odds and ends -3 ados abandoned keep)
		shops.add("buyoddsandends", "shuriken", 20);
		shops.add("buyoddsandends", "护身符", 800);
		shops.add("buyoddsandends", "black pearl", 100);
		shops.add("buyoddsandends", "幸运石", 60);
		shops.add("buyoddsandends", "knife", 5);
		shops.add("buyoddsandends", "dagger", 20);
		shops.add("buyoddsandends", "skull ring", 250);
		shops.add("buyoddsandends", "greater antidote", 80);
		shops.add("buyoddsandends", "marbles", 80);
		shops.add("buyoddsandends", "magical needle", 1500);
		shops.add("buyoddsandends", "snowglobe", 150);
		shops.add("buyoddsandends", "silk gland", 500);

		// archery shop in nalwor)
		shops.add("buyarcherstuff", "木箭", 1);
		shops.add("buyarcherstuff", "铁箭", 5);
		shops.add("buyarcherstuff", "金箭", 10);
		shops.add("buyarcherstuff", "加强箭", 35);
		shops.add("buyarcherstuff", "木弓", 250);
		shops.add("buyarcherstuff", "crossbow", 400);
		shops.add("buyarcherstuff", "longbow", 300);
		shops.add("buyarcherstuff", "composite bow", 350);
		shops.add("buyarcherstuff", "hunter crossbow", 800);
		shops.add("buyarcherstuff", "mithril bow", 2000);

		// selling arrows
		shops.add("sellarrows", "木箭", 2);
		shops.add("sellarrows", "铁箭", 7);
		shops.add("sellarrows", "金箭", 25);
		shops.add("sellarrows", "加强箭", 45);

		// assassinhq chief falatheen the dishwasher and veggie buyer)
		// sign is hard coded so if you change this change the sign
		shops.add("buyveggiesandherbs", "carrot", 5);
		shops.add("buyveggiesandherbs", "salad", 10);
		shops.add("buyveggiesandherbs", "leek", 25);
		shops.add("buyveggiesandherbs", "broccoli", 30);
		shops.add("buyveggiesandherbs", "courgette", 10);
		shops.add("buyveggiesandherbs", "cauliflower", 30);
		shops.add("buyveggiesandherbs", "tomato", 20);
		shops.add("buyveggiesandherbs", "onion", 20);
		shops.add("buyveggiesandherbs", "arandula", 10);
		shops.add("buyveggiesandherbs", "kokuda", 200);
		shops.add("buyveggiesandherbs", "kekik", 25);
		shops.add("buyveggiesandherbs", "sclaria", 25);

		// gnome village buyer in 0 ados mountain n2 w2)
		shops.add("buy4gnomes", "皮甲", 25);
		shops.add("buy4gnomes", "木棍", 3);
		shops.add("buy4gnomes", "leather helmet", 15);
		shops.add("buy4gnomes", "cloak", 25);
		shops.add("buy4gnomes", "apple", 5);
		shops.add("buy4gnomes", "marbles", 50);
		shops.add("buy4gnomes", "木盾", 20);

		// hotdog lady in athor)
		shops.add("buy4hotdogs", "sausage", 30);
		shops.add("buy4hotdogs", "cheese sausage", 25);
		shops.add("buy4hotdogs", "bread", 15);
		shops.add("buy4hotdogs", "onion", 20);
		shops.add("buy4hotdogs", "canned tuna", 15);
		shops.add("buy4hotdogs", "ham", 15);
		shops.add("buy4hotdogs", "干酪", 5);

		shops.add("sellhotdogs", "hotdog", 160);
		shops.add("sellhotdogs", "cheeseydog", 180);
		shops.add("sellhotdogs", "tuna sandwich", 130);
		shops.add("sellhotdogs", "sandwich", 120);
		shops.add("sellhotdogs", "vanilla shake", 110);
		shops.add("sellhotdogs", "chocolate shake", 110);
		shops.add("sellhotdogs", "巧克力棒", 100);
		shops.add("sellhotdogs", "snowglobe", 200);

		// magic city barmaid)
		shops.add("sellmagic", "hotdog", 160);
		shops.add("sellmagic", "cheeseydog", 180);
		shops.add("sellmagic", "tuna sandwich", 130);
		shops.add("sellmagic", "sandwich", 120);
		shops.add("sellmagic", "vanilla shake", 110);
		shops.add("sellmagic", "chocolate shake", 110);
		shops.add("sellmagic", "巧克力棒", 100);
		shops.add("sellmagic", "licorice", 100);

		// kirdneh city armor)
		shops.add("buykirdneharmor", "蓝色盔甲", 13000);
		shops.add("buykirdneharmor", "石甲", 18000);
		shops.add("buykirdneharmor", "冰护甲", 19000);
		shops.add("buykirdneharmor", "异种元素甲", 21000);
		shops.add("buykirdneharmor", "野蛮人护甲", 5000);
		shops.add("buykirdneharmor", "绿龙盾", 13000);
		shops.add("buykirdneharmor", "异域盾", 20000);


		// amazon cloaks shop
		shops.add("buyamazoncloaks", "吸血鬼斗篷", 14000);
		shops.add("buyamazoncloaks", "异种元素斗篷", 18000);
		shops.add("buyamazoncloaks", "精灵斗篷", 50);
		shops.add("buyamazoncloaks", "巫妖斗篷", 10000);
		shops.add("buyamazoncloaks", "石斗篷", 350);
		shops.add("buyamazoncloaks", "蓝色条纹斗篷", 280);
		shops.add("buyamazoncloaks", "红龙斗篷", 4000);
		shops.add("buyamazoncloaks", "骨龙斗篷", 1500);

		// kirdneh city fishy market)
		shops.add("buyfishes", "perch", 22);
		shops.add("buyfishes", "mackerel", 20);
		shops.add("buyfishes", "roach", 10);
		shops.add("buyfishes", "char", 30);
		shops.add("buyfishes", "clownfish", 30);
		shops.add("buyfishes", "surgeonfish", 15);
		shops.add("buyfishes", "trout", 45);
		shops.add("buyfishes", "cod", 10);

		// semos trading - swords)
		shops.add("tradeswords", "dagger", 10);

		// party time! For maria for example. Bit more expensive than normal
		shops.add("sellparty", "pina colada", 100);
		shops.add("sellparty", "巧克力棒", 100);
		shops.add("sellparty", "beer", 10);
		shops.add("sellparty", "wine", 15);
		shops.add("sellparty", "vanilla shake", 150);
		shops.add("sellparty", "icecream", 50);
		shops.add("sellparty", "hotdog", 180);
		shops.add("sellparty", "sandwich", 140);


		// black items (balduin, when ultimate collector quest completed)
		shops.add("buyblack", "黑色盔甲", 60000);
		shops.add("buyblack", "黑靴子", 10000);
		shops.add("buyblack", "黑斗篷", 20000);
		shops.add("buyblack", "black helmet", 15000);
		shops.add("buyblack", "black legs", 40000);
		shops.add("buyblack", "黑盾", 75000);
		shops.add("buyblack", "black sword", 20000);
		shops.add("buyblack", "黑镰刀", 40000);
		shops.add("buyblack", "黑戟", 30000);

		// ados market
		shops.add("buyadosarmors", "蓝盾", 900);

		// Athor ferry
		shops.add("buypoisons", "poison", 40);
		shops.add("buypoisons", "toadstool", 60);
		shops.add("buypoisons", "greater poison", 60);
		shops.add("buypoisons", "red lionfish", 50);
		shops.add("buypoisons", "deadly poison", 100);
		shops.add("buypoisons", "mega poison", 500);
		shops.add("buypoisons", "disease poison", 2000);

		// Should have its own shop (buytraps)
		shops.add("buypoisons", "rodent trap", 50);

		//Scuba Instructor Edward
		shops.add("sellScubaStuff", "scuba gear", 22000);

		// 矿镇复兴展会周 Caroline
		shops.add("sellrevivalweeks", "cherry pie", 195);
		shops.add("sellrevivalweeks", "apple pie", 195);
		shops.add("sellrevivalweeks", "vanilla shake", 120);
		shops.add("sellrevivalweeks", "chocolate shake", 120);
		shops.add("sellrevivalweeks", "icecream", 60);
		shops.add("sellrevivalweeks", "巧克力棒", 100);
		shops.add("sellrevivalweeks", "grilled steak", 250);
		shops.add("sellrevivalweeks", "hotdog", 170);
		shops.add("sellrevivalweeks", "cheeseydog", 175);
		shops.add("sellrevivalweeks", "tuna sandwich", 140);
		shops.add("sellrevivalweeks", "sandwich", 130);
		shops.add("sellrevivalweeks", "wine", 25);
		shops.add("sellrevivalweeks", "beer", 20);
		shops.add("sellrevivalweeks", "水", 15);

		// for ados botanical gardens or if you like, other cafes.
		// expensive prices to make sure that the npc production of these items isn't compromised
		shops.add("cafe", "tea", 80);
		shops.add("cafe", "水", 50);
		shops.add("cafe", "chocolate shake", 150);
		shops.add("cafe", "sandwich", 170);
		shops.add("cafe", "tuna sandwich", 180);
		shops.add("cafe", "apple pie", 250);

	}

	private static ShopList instance;

	/**
	 * Returns the Singleton instance.
	 *
	 * @return The instance
	 */
	public static ShopList get() {
		if (instance == null) {
			instance = new ShopList();
		}
		return instance;
	}

	private final Map<String, Map<String, Integer>> contents;

	private ShopList() {
		contents = new HashMap<String, Map<String, Integer>>();
	}

	/**
	 * gets the items offered by a shop with their prices
	 *
	 * @param name name of shop
	 * @return items and prices
	 */
	public Map<String, Integer> get(final String name) {
		return contents.get(name);
	}

	/**
	 * gets a set of all shops
	 *
	 * @return set of shops
	 */
	public Set<String> getShops() {
		return contents.keySet();
	}

	/**
	 * converts a shop into a human readable form
	 *
	 * @param name   name of shop
	 * @param header prefix
	 * @return human readable description
	 */
	public String toString(final String name, final String header) {
		final Map<String, Integer> items = contents.get(name);

		final StringBuilder sb = new StringBuilder(header + "\n");

		for (final Entry<String, Integer> entry : items.entrySet()) {
			sb.append(entry.getKey() + " \t" + entry.getValue() + "\n");
		}

		return sb.toString();
	}

	/**
	 * Add an item to a shop
	 *
	 * @param name the shop name
	 * @param item the item to add
	 * @param price the price for the item
	 */
	public void add(final String name, final String item, final int price) {
		Map<String, Integer> shop;

		if (contents.containsKey(name)) {
			shop = contents.get(name);
		} else {
			shop = new LinkedHashMap<String, Integer>();
			contents.put(name, shop);
		}

		shop.put(item, Integer.valueOf(price));
	}
}
