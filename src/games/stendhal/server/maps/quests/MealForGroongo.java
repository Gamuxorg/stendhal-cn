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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.MathHelper;
import games.stendhal.common.Rand;
//import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DecreaseKarmaAction;
import games.stendhal.server.entity.npc.action.DropInfostringItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.InflictStatusOnNPCAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasInfostringItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.ItemCollection;
import games.stendhal.server.util.TimeUtil;
import marauroa.common.Pair;

/**
 * NOTE: quest slot templates for testing
 * ---------------------------
 * fetch_dessert;inprogress;paella;chicken=2,西红柿=3,大蒜=3,trout=1,perch=1,洋葱=2,;gulab;面粉=2,沸水=2,honey=2,sugar=4,;1337207220454
 * deliver_decentmeal;inprogress;paella;chicken=2,西红柿=3,大蒜=3,trout=1,perch=1,洋葱=2,;gulab;面粉=2,沸水=2,honey=2,sugar=4,;1337207289602
 * done;incomplete;paella;chicken=2,西红柿=3,大蒜=3,trout=1,perch=1,洋葱=2,;gulab;面粉=2,沸水=2,honey=2,sugar=4,;1337207484330;1
 * done;complete;paella;chicken=2,西红柿=3,大蒜=3,trout=1,perch=1,洋葱=2,;gulab;面粉=2,沸水=2,honey=2,sugar=4,;1337207484330;1
 * ---------------------------
 */

/**
 * QUEST: Meal for Groongo, The Troublesome Customer
 * <p>
 * PARTICIPANTS:
 * <ul>
 *  <li> Groongo Rahnnt, The Troublesome Customer
 *  <li> Stefan, The Fado's Hotel Restaurant Chef
 * </ul>
 *
 * STEPS:
 * <ul>
 *  <li> Groongo is hungry, asks the player to bring him a decent meal,
 *  <li> The player talks to Stefan and he will tell him what ingredients he's missing,
 *  <li> The player goes fetching the ingredients for the main dish,
 *  <li> The player brings Stefan the ingredients he needs,
 *  <li> Stefan tells the player to ask Groongo which dessert he would like along the main dish,
 *  <li> The player checks back with Groongo to ask for a dessert of his choice,
 *  <li> The player tells Stefan which dessert Groongo wants along with the main dish,
 *  <li> Stefan tells the player which ingredients he's missing for preparing the dessert,
 *  <li> The player goes fetching the ingredients for the dessert and brings them to Stefan,
 *  <li> Stefan tells the player how much time (10-15mins) he requires to prepare Groongo's decent meal,
 *  <li> After enough time has elapsed, the player gets the decent meal from Stefan,
 *  <li> The player may deliver the decent meal to Grongo
 *  <li> Groongo is finaly happy and gives the player a reward of some kind, hints player to say 'thanks' to Stefan
 *  <li> The player then has a limited time to get a better reward
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> both 'normal' and 'better' rewards are for testing purpouses only.
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 *  <li>unlimited
 *  <li>once or twice a day?
 * </ul>
 *
 * @author omero
 */

//TODO: it requires food items that have been reverted from miscs.xml because food needs to be eatable.


public class MealForGroongo extends AbstractQuest {

    private static Logger logger = Logger.getLogger(MealForGroongo.class);

    /**
     * QUEST_SLOT will be used to hold the different states of the quest.
     * Each sub slot will only always serve one purpose as stated below.
     *
     * QUEST_SLOT sub slot 0
     * will hold the main states, which can be:
     * - rejected, the player has refused to undertake the quest
     * - fetch_maindish, the player is collecting ingredients for that
     * - check_dessert, the player needs to ask Groongo which dessert he wants
     * - tell_dessert, the player has to tell Stefan about Groongo's choice
     * - fetch_dessert, the player is collecting the ingredients for that
     * - prepare_decentmeal, the player has to wait a little before the meal is ready
     * - deliver_decentmeal, meal for Groongo is ready
     * - done, the player has completed the quest
     *
     * QUEST_SLOT sub slot 1
     * will hold how the quest was finally ended:
     * - incomplete
     * - complete
     *
     * After the troublesome customer gets delivered his decent meal,
     * the player may check back with Stefan once more and
     * say the trigger word 'thanks' before BRINGTHANKS_DELAY has elapsed.
     *
     * Since we don't want to reuse any of the QUEST_SLOT sub slots,
     * when BRINGTHANKS_DELAY hasn't expired yet, checking QUEST_SLOT sub slot 0 (done)
     * and QUEST_SLOT sub slot 6 (timestamp) or any other combination of sub slots
     * would allow the player to say 'thanks' repeatedly
     * and get 'better reward' multiple times.
     *
     * QUEST_SLOT sub slot 0 gets marked as 'done'
     * as soon as player delivers meal to customer and
     * QUEST_SLOT sub slot 1 gets marked 'incomplete'.
     *
     * If the player checks back with Stefan in time,
     * the QUEST_SLOT sub slot 1 gets marked 'complete'
     * and the quest is finally ended.
     *
     * If the player doesn't check back with Stefan within BRINGTHANKS_DELAY,
     * the 'better reward' from Stefan is lost.
     *
     * QUEST_SLOT sub slot 2
     * - a main dish short name
     *
     * QUEST_SLOT sub slot 3
     * - holds the ingredients to fetch for the main dish
     *
     * QUEST_SLOT sub slot 4
     * - a dessert short name
     *
     * QUEST_SLOT sub slot 5
     * - holds the ingredients to fetch for the dessert
     *
     * QUEST_SLOT sub slot 6
     * - when quest is running, holds a timestamp for waiting the preparation of decent meal
     * - when quest is done, holds the timestamp of when last decent meal was delivered
     *
     * QUEST_SLOT sub slot 7
     * - counts how many times a decent meal has been delivered
     */
    public static final String QUEST_SLOT = "meal_for_groongo";

    //How long it takes Chef Stefan to prepare a decent meal (main dish and dessert)
    // FIXME omero: for testing only
    private static final int MEALREADY_DELAY = 1;
    //private static final int MEALREADY_DELAY = 15;

    //How much time the player has to get a better reward
    private static final int BRINGTHANKS_DELAY = 10;

    //Every when the quest can be repeated
    // FIXME omero: for testing only
    private static final int REPEATQUEST_DELAY = 1;
    //private static final int REPEATQUEST_DELAY = 1 * MathHelper.MINUTES_IN_ONE_DAY;

    // how much XP is given as the reward
    private static final int XP_REWARD = 1000;

    // which main dishes Groongo will ask for the quest
    private static final List<String> REQUIRED_MAIN_DISHES =
            Arrays.asList(
                "paella",
                "ciorba",
                "lasagne",
                "schnitzel",
                "consomme",
                "paidakia",
                "couscous",
                "kushari"
            );

    // which desserts Groongo will ask for the quest
    private static final List<String> REQUIRED_DESSERTS =
            Arrays.asList(
                "macedonia",
                "brigadeiro",
                "vatrushka",
                "cake",
                "tarte",
                "slagroomtart",
                "kirschtorte",
                "gulab"
            );

    @Override
    public void addToWorld() {
        fillQuestInfo(
            "Meal for Groongo Rahnnt",
            "Groongo 饿了, 他想在 Fado 的酒店吃一顿大餐.",
            true);
        stageBeginQuest();
        stageCollectIngredientsForMainDish();
        stageCheckForDessert();
        stageCollectIngredientsForDessert();
        stageWaitForMeal();
        stageDeliverMeal();

    }

    @Override
    public List<String> getHistory(final Player player) {

        final List<String> res = new ArrayList<String>();

        if (!player.hasQuest(QUEST_SLOT)) {
            return res;
        }
        final String questState = player.getQuest(QUEST_SLOT, 0);
        //logger.warn("Quest state: <" + player.getQuest(QUEST_SLOT) + ">");

        res.add("我在 Fado的旅馆 见到了 Groongo Rahnnt");

        if ("rejected".equals(questState)) {
            res.add("他让我给他一些肉满足他的食欲, "
                + " 但我没兴趣做这样的事.");
        } else if ("done".equals(questState)) {
            res.add(
                "我给他带来了一大盘的 " +
                getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2)) +
                " 和餐后甜点 " +
                getRequiredDessertFancyName(player.getQuest(QUEST_SLOT, 4)) +
                " ."
            );
            if (isRepeatable(player)) {
                // enough time has passed, inform that the quest is available to be taken.
                res.add("如果他吃的节制一点, 可能我还会问他.");
            } else {
                // inform about how much time has to pass before the quest can be taken again.
                long timestamp;
                try {
                    timestamp = Long.parseLong(player.getQuest(QUEST_SLOT, 6));
                } catch (final NumberFormatException e) {
                    timestamp = 0;
                }
                final long timeBeforeRepeatable = timestamp
                + REPEATQUEST_DELAY * MathHelper.MILLISECONDS_IN_ONE_MINUTE
                - System.currentTimeMillis();
                res.add(
                    "在 " +
                    TimeUtil.approxTimeUntil((int) (timeBeforeRepeatable / 1000L)) + " 内他还能撑住.");
            }
        } else {
            final ItemCollection missingIngredients = new ItemCollection();
            String ingredients = "";
            if ("fetch_maindish".equals(questState)) {
                ingredients = player.getQuest(QUEST_SLOT, 3);
                ingredients = ingredients.replaceAll(",", ";");
                missingIngredients.addFromQuestStateString(ingredients);
                res.add("Groongo 想要 " +
                    getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2)) + 
                    " 当做盘子, 我正帮 Chef Stefan 寻找配作它的零件. ." +
                    " I still have to bring " +
                    missingIngredients.toStringList() + "."
                );
            } else if ("check_dessert".equals(questState)) {
                res.add("Groongo 想要 " +
                    getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2)) +
                    " 作为盘子, Chef Stefan 已经准备好了它."
                );
                res.add("我现在要问 Groongo" +
                    " 接下来他喜欢哪种甜点."
                );
            } else if ("tell_dessert".equals(questState)) {
                res.add("Groongo 想要 " +
                    getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2)) +
                    " 作为盘子."
                );
                res.add("I'm now going to tell Chef Stefan to also prepare " +
                    getRequiredDessertFancyName(player.getQuest(QUEST_SLOT, 4)) +
                    " for dessert."
                );
            } else if ("fetch_dessert".equals(questState)) {
                ingredients = player.getQuest(QUEST_SLOT, 5);
                ingredients = ingredients.replaceAll(",", ";");
                missingIngredients.addFromQuestStateString(ingredients);
                res.add("Groongo wants to have " +
                    getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2)) +
                    " as the main dish and " +
                    getRequiredDessertFancyName(player.getQuest(QUEST_SLOT, 4)) +
                    " for dessert."
                );
                res.add("I'm helping Chef Stefan finding the ingredients to prepare the dessert" +
                    " and I still have to bring " +
                    missingIngredients.toStringList() + "."
                );
            } else if ("prepare_decentmeal".equals(questState)) {
                res.add("Groongo wants to have " +
                    getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2)) +
                    " as the main dish and " +
                    getRequiredDessertFancyName(player.getQuest(QUEST_SLOT, 4)) +
                    " for dessert and I'm waiting that Chef Stefan finishes to prepare them."
                );
            } else if ("deliver_decentmeal".equals(questState)) {
                res.add("Groongo wants to have " +
                    getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2)) +
                    " as the main dish and " +
                    getRequiredDessertFancyName(player.getQuest(QUEST_SLOT, 4)) +
                    " for dessert and I'm going to bring him his decent meal."
                );
            }
        }

        return res;

    }


    @Override
    public String getSlotName() {

        return QUEST_SLOT;

    }

    @Override
    public String getName() {

        return "MealForGroongo";

    }

    @Override
    public int getMinLevel() {
        // TODO omero: minlevel needs to be adjusted
        return 30;

    }

    @Override
    public String getRegion() {

        return Region.FADO_CITY;

    }

    @Override
    public String getNPCName() {

        return "Groongo Rahnnt";

    }

    @Override
    public boolean isRepeatable(final Player player) {

        return new AndCondition(
            new QuestCompletedCondition(QUEST_SLOT),
            new TimePassedCondition(QUEST_SLOT, 6, REPEATQUEST_DELAY)).fire(player,null, null);

    }

    @Override
    public boolean isCompleted(final Player player) {

        return new QuestCompletedCondition(QUEST_SLOT).fire(player, null, null);

    }

    // Groongo uses this to select a random main dish for the quest
    // All main dishes are temporary for developing purposes, subject to change
    private String getRequiredMainDish() {

        return REQUIRED_MAIN_DISHES.get(Rand.rand(REQUIRED_MAIN_DISHES.size()));

    }

    // Groongo uses this to select a random dessert for the quest
    // All desserts are temporary for developing purposes, subject to change
    private String getRequiredDessert() {

        return REQUIRED_DESSERTS.get(Rand.rand(REQUIRED_DESSERTS.size()));

    }

    // used by both Groongo and Stefan to build sentences
    // this avoids requiring the player to type long and complicated fancy dish names
    private String getRequiredMainDishFancyName(final String requiredMainDish) {

        final Map<String, String> requiredMainDishFancyName = new HashMap<String, String>();

        requiredMainDishFancyName.put("paella", "paella de pescado");
        requiredMainDishFancyName.put("ciorba", "ciorba de burta cu smantena");
        requiredMainDishFancyName.put("lasagne", "lasagne alla bolognese");
        requiredMainDishFancyName.put("schnitzel", "jaegerschnitzel mit pilzen");
        requiredMainDishFancyName.put("consomme", "consomme du jour");
        requiredMainDishFancyName.put("paidakia", "paidakia meh piperi");
        requiredMainDishFancyName.put("couscous", "couscous");
        requiredMainDishFancyName.put("kushari", "kushari");

        return requiredMainDishFancyName.get(requiredMainDish);

    }

    // used by both Groongo and Stefan to build sentences
    // this avoids requiring the player to type long and complicated fancy dessert names
    private String getRequiredDessertFancyName(final String requiredDessert) {

        final Map<String, String> requiredDessertFancyName = new HashMap<String, String>();

        requiredDessertFancyName.put("brigadeiro", "brigadeiro a la amparo");
        requiredDessertFancyName.put("macedonia", "macedonia di frutta");
        requiredDessertFancyName.put("vatrushka", "old-fashioned vatrushka with cottage cheese");
        requiredDessertFancyName.put("cake", "classic 胡萝卜 cake with fluffy cream cheese frosting");
        requiredDessertFancyName.put("tarte", "tarte avec la rhubarbe");
        requiredDessertFancyName.put("slagroomtart", "slagroomtart van der boer");
        requiredDessertFancyName.put("kirschtorte", "schwarzwalder kirschtorte");
        requiredDessertFancyName.put("gulab", "gulab jamun");

        return requiredDessertFancyName.get(requiredDessert);

    }

    // used by Stefan
    /**
     * Returns required ingredients and quantities to collect for preparing the main dish
     *
     * @param requiredMainDish
     * @return A string composed of comma separated key=value token pairs.
     */
    private String getRequiredIngredientsForMainDish(final String requiredMainDish) {

        // All not-yet-existing ingredients commented out for testing purposes
        // All ingredients are temporary for developing purposes, subject to change

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_paella = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_paella.put("洋葱", new Pair<Integer, Integer>(1,3));
        requiredIngredients_paella.put("大蒜", new Pair<Integer, Integer>(2,3));
        requiredIngredients_paella.put("西红柿", new Pair<Integer, Integer>(1,5));
        requiredIngredients_paella.put("鸡腿", new Pair<Integer, Integer>(2,3));
        requiredIngredients_paella.put("perch", new Pair<Integer, Integer>(1,1));
        requiredIngredients_paella.put("trout", new Pair<Integer, Integer>(1,1));
        requiredIngredients_paella.put("butter", new Pair<Integer, Integer>(1,2));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_ciorba = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_ciorba.put("tripe", new Pair<Integer, Integer>(1,3));
        requiredIngredients_ciorba.put("pinto beans", new Pair<Integer, Integer>(1,3));
        requiredIngredients_ciorba.put("洋葱", new Pair<Integer, Integer>(2,4));
        requiredIngredients_ciorba.put("大蒜", new Pair<Integer, Integer>(1,3));
        requiredIngredients_ciorba.put("牛奶", new Pair<Integer, Integer>(1,3));
        requiredIngredients_ciorba.put("胡萝卜", new Pair<Integer, Integer>(1,3));
        requiredIngredients_ciorba.put("vinegar", new Pair<Integer, Integer>(1,3));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_lasagne = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_lasagne.put("肉", new Pair<Integer, Integer>(2,4));
        requiredIngredients_lasagne.put("西红柿", new Pair<Integer, Integer>(1,6));
        requiredIngredients_lasagne.put("胡萝卜", new Pair<Integer, Integer>(2,4));
        requiredIngredients_lasagne.put("干酪", new Pair<Integer, Integer>(3,6));
        requiredIngredients_lasagne.put("面粉", new Pair<Integer, Integer>(1,2));
        requiredIngredients_lasagne.put("egg", new Pair<Integer, Integer>(2,4));
        requiredIngredients_lasagne.put("橄榄油", new Pair<Integer, Integer>(1,1));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_schnitzel = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_schnitzel.put("potato", new Pair<Integer, Integer>(3,6));
        requiredIngredients_schnitzel.put("大脚菇", new Pair<Integer, Integer>(2,4));
        requiredIngredients_schnitzel.put("小圆菇", new Pair<Integer, Integer>(4,8));
        requiredIngredients_schnitzel.put("火腿", new Pair<Integer, Integer>(2,3));
        requiredIngredients_schnitzel.put("肉", new Pair<Integer, Integer>(2,3));
        requiredIngredients_schnitzel.put("牛奶", new Pair<Integer, Integer>(1,2));
        requiredIngredients_schnitzel.put("干酪", new Pair<Integer, Integer>(1,4));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_consomme = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_consomme.put("洋葱", new Pair<Integer, Integer>(1,2));
        requiredIngredients_consomme.put("大蒜", new Pair<Integer, Integer>(1,2));
        requiredIngredients_consomme.put("胡萝卜", new Pair<Integer, Integer>(1,4));
        requiredIngredients_consomme.put("鸡腿", new Pair<Integer, Integer>(1,4));
        requiredIngredients_consomme.put("肉", new Pair<Integer, Integer>(2,4));
        requiredIngredients_consomme.put("sclaria", new Pair<Integer, Integer>(1,2));
        requiredIngredients_consomme.put("kekik", new Pair<Integer, Integer>(1,2));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_paidakia = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_paidakia.put("肉", new Pair<Integer, Integer>(1,5));
        requiredIngredients_paidakia.put("vinegar", new Pair<Integer, Integer>(1,4));
        requiredIngredients_paidakia.put("sclaria", new Pair<Integer, Integer>(1,4));
        requiredIngredients_paidakia.put("橄榄油", new Pair<Integer, Integer>(1,2));
        requiredIngredients_paidakia.put("potato", new Pair<Integer, Integer>(1,6));
        requiredIngredients_paidakia.put("kekik", new Pair<Integer, Integer>(1,2));
        requiredIngredients_paidakia.put("lemon", new Pair<Integer, Integer>(1,2));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_kushari = new HashMap<String, Pair<Integer, Integer>>();
        //requiredIngredients_kushari.put("rice", new Pair<Integer, Integer>(1,6));
        //requiredIngredients_kushari.put("lentils", new Pair<Integer, Integer>(1,6));
        requiredIngredients_kushari.put("洋葱", new Pair<Integer, Integer>(1,2));
        requiredIngredients_kushari.put("大蒜", new Pair<Integer, Integer>(1,2));
        requiredIngredients_kushari.put("西红柿", new Pair<Integer, Integer>(1,4));
        //requiredIngredients_kushari.put("jalapeno", new Pair<Integer, Integer>(1,4));
        requiredIngredients_kushari.put("橄榄油", new Pair<Integer, Integer>(1,2));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_couscous = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_couscous.put("面粉", new Pair<Integer, Integer>(1,2));
        requiredIngredients_couscous.put("水", new Pair<Integer, Integer>(1,4));
        requiredIngredients_couscous.put("西葫芦", new Pair<Integer, Integer>(1,6));
        requiredIngredients_couscous.put("洋葱", new Pair<Integer, Integer>(2,4));
        requiredIngredients_couscous.put("大蒜", new Pair<Integer, Integer>(2,3));
        requiredIngredients_couscous.put("vinegar", new Pair<Integer, Integer>(1,2));
        //requiredIngredients_couscous.put("jalapeno", new Pair<Integer, Integer>(1,2));

        //final HashMap<String, HashMap<String, Integer>> requiredIngredientsForMainDish = new HashMap<String, HashMap<String, Integer>>();
        final HashMap<String, HashMap<String, Pair<Integer, Integer>>> requiredIngredientsForMainDish =
                new HashMap<String, HashMap<String, Pair<Integer, Integer>>>();

        requiredIngredientsForMainDish.put("paella", requiredIngredients_paella);
        requiredIngredientsForMainDish.put("ciorba", requiredIngredients_ciorba);
        requiredIngredientsForMainDish.put("lasagne", requiredIngredients_lasagne);
        requiredIngredientsForMainDish.put("schnitzel", requiredIngredients_schnitzel);
        requiredIngredientsForMainDish.put("consomme", requiredIngredients_consomme);
        requiredIngredientsForMainDish.put("paidakia", requiredIngredients_paidakia);
        requiredIngredientsForMainDish.put("couscous", requiredIngredients_couscous);
        requiredIngredientsForMainDish.put("kushari", requiredIngredients_kushari);

        String ingredients = "";
        final HashMap<String, Pair<Integer, Integer>>  requiredIngredients = requiredIngredientsForMainDish.get(requiredMainDish);
        for (final Map.Entry<String, Pair<Integer, Integer>> entry : requiredIngredients.entrySet()) {
            //logger.warn(" ingredient <" + entry.getKey() + "> quantities <" + entry.getValue() + ">");
            ingredients = ingredients + entry.getKey() + "=" + Rand.randUniform(entry.getValue().first(), entry.getValue().second()) + ",";
        }

        //logger.warn(" ingredients <" + ingredients + ">");
        // strip the last comma from the returned string
        return ingredients.substring(0, ingredients.length()-1);

    }

    // used by Stefan
    /**
     * Returns required ingredients and quantities to collect for preparing the dessert
     *
     * @param requiredDessert
     * @return A string composed of semicolon separated key=value token pairs.
     */
    private String getRequiredIngredientsForDessert(final String requiredDessert) {

        // All ingredients are temporary for developing purposes, subject to change
        // All not-yet-existing ingredients commented out for testing purposes

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_brigadeiro = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_brigadeiro.put("牛奶", new Pair<Integer, Integer>(1,4));
        requiredIngredients_brigadeiro.put("sugar", new Pair<Integer, Integer>(2,4));
        requiredIngredients_brigadeiro.put("butter", new Pair<Integer, Integer>(2,4));
        //requiredIngredients_brigadeiro.put("椰子", new Pair<Integer, Integer>(1,2)); // will be cacao pod... monkeys?

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_macedonia = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_macedonia.put("香蕉", new Pair<Integer, Integer>(1,6));
        requiredIngredients_macedonia.put("苹果", new Pair<Integer, Integer>(1,8));
        requiredIngredients_macedonia.put("梨子", new Pair<Integer, Integer>(1,8));
        requiredIngredients_macedonia.put("西瓜", new Pair<Integer, Integer>(1,2));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_slagroomtart = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_slagroomtart.put("牛奶", new Pair<Integer, Integer>(1,4));
        requiredIngredients_slagroomtart.put("sugar", new Pair<Integer, Integer>(1,2));
        requiredIngredients_slagroomtart.put("egg", new Pair<Integer, Integer>(2,4));
        //requiredIngredients_slagroomtart.put("菠萝", new Pair<Integer, Integer>(1,4));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_vatrushka = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_vatrushka.put("面粉", new Pair<Integer, Integer>(1,2));
        requiredIngredients_vatrushka.put("sugar", new Pair<Integer, Integer>(1,4));
        requiredIngredients_vatrushka.put("干酪", new Pair<Integer, Integer>(1,8));
        requiredIngredients_vatrushka.put("樱桃", new Pair<Integer, Integer>(1,10));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_cake = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_cake.put("面粉", new Pair<Integer, Integer>(1,2));
        requiredIngredients_cake.put("sugar", new Pair<Integer, Integer>(1,4));
        requiredIngredients_cake.put("干酪", new Pair<Integer, Integer>(1,8));
        requiredIngredients_cake.put("胡萝卜", new Pair<Integer, Integer>(1,10));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_tarte = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_tarte.put("面粉", new Pair<Integer, Integer>(1,2));
        requiredIngredients_tarte.put("sugar", new Pair<Integer, Integer>(1,4));
        requiredIngredients_tarte.put("chocolate", new Pair<Integer, Integer>(1,6));
        requiredIngredients_tarte.put("牛奶", new Pair<Integer, Integer>(2,4));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_kirschtorte = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_kirschtorte.put("面粉", new Pair<Integer, Integer>(1,2));
        requiredIngredients_kirschtorte.put("sugar", new Pair<Integer, Integer>(1,4));
        requiredIngredients_kirschtorte.put("butter", new Pair<Integer, Integer>(1,4));
        requiredIngredients_kirschtorte.put("牛奶", new Pair<Integer, Integer>(2,4));

        final HashMap<String, Pair<Integer, Integer>> requiredIngredients_gulab = new HashMap<String, Pair<Integer, Integer>>();
        requiredIngredients_gulab.put("面粉", new Pair<Integer, Integer>(1,2));
        requiredIngredients_gulab.put("沸水", new Pair<Integer, Integer>(1,2));
        requiredIngredients_gulab.put("sugar", new Pair<Integer, Integer>(1,4));
        requiredIngredients_gulab.put("honey", new Pair<Integer, Integer>(1,4));

        final HashMap<String, HashMap<String, Pair<Integer, Integer>>> requiredIngredientsForDessert =
                new HashMap<String, HashMap<String, Pair<Integer, Integer>>>();

        requiredIngredientsForDessert.put("brigadeiro", requiredIngredients_brigadeiro);
        requiredIngredientsForDessert.put("macedonia", requiredIngredients_macedonia);
        requiredIngredientsForDessert.put("vatrushka", requiredIngredients_vatrushka);
        requiredIngredientsForDessert.put("cake", requiredIngredients_cake);
        requiredIngredientsForDessert.put("tarte", requiredIngredients_tarte);
        requiredIngredientsForDessert.put("slagroomtart", requiredIngredients_slagroomtart);
        requiredIngredientsForDessert.put("kirschtorte", requiredIngredients_kirschtorte);
        requiredIngredientsForDessert.put("gulab", requiredIngredients_gulab);

        String ingredients = "";
        final HashMap<String, Pair<Integer, Integer>>  requiredIngredients = requiredIngredientsForDessert.get(requiredDessert);
        for (final Map.Entry<String, Pair<Integer, Integer>> entry : requiredIngredients.entrySet()) {
            //logger.warn(" ingredient <" + entry.getKey() + "> quantities <" + entry.getValue() + ">");
            ingredients = ingredients + entry.getKey() + "=" + Rand.randUniform(entry.getValue().first(), entry.getValue().second()) + ",";
        }

        //logger.warn(" ingredients <" + ingredients + ">");
        // strip the last comma from the returned string
        return ingredients.substring(0, ingredients.length()-1);

    }

    // Stefan uses this to advance the quest:
    // - after the player has gathered all of required ingredients for the main dish
    // - after the player has asked Groongo which dessert he'd like along the main dish
    // - after the player has gathered all of required ingredients for the dessert
    class advanceQuestInProgressAction implements ChatAction {

        @Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser SpeakerNPC) {

            if ("fetch_maindish".equals(player.getQuest(QUEST_SLOT, 0))) {
                player.setQuest(QUEST_SLOT, 0, "check_dessert");
                SpeakerNPC.say(
                    "很好！我立即开始准备 " +
                            getRequiredMainDishFancyName(
                                    player.getQuest(QUEST_SLOT, 2)) + " ." +
                    " 同时, 请问下我们的多事的顾客" +
                    " 随后他想要哪种甜点 #dessert !");
            } else if ("tell_dessert".equals(player.getQuest(QUEST_SLOT, 0))) {
                player.setQuest(QUEST_SLOT, 0, "fetch_dessert");
                SpeakerNPC.say("确实是个美味选择!");
            } else if ("fetch_dessert".equals(player.getQuest(QUEST_SLOT, 0))) {
                final long timestamp = Long.parseLong(player.getQuest(QUEST_SLOT, 6));
                final long timeToWaitForMealReady = timestamp
                + MEALREADY_DELAY * MathHelper.MILLISECONDS_IN_ONE_MINUTE
                - System.currentTimeMillis();
                player.setQuest(QUEST_SLOT, 0, "prepare_decentmeal");
                SpeakerNPC.say(
                    "完美! 为了我们的多事的顾客备好了适量的大餐,需要 " +
                    TimeUtil.approxTimeUntil((int) (timeToWaitForMealReady / 1000L)) + " 的时间;");
            } else if ("prepare_decentmeal".equals(player.getQuest(QUEST_SLOT, 0))) {
                final Item decentMeal = SingletonRepository.getEntityManager().getItem("decent meal");
                final String decentMealDescription =
                            getRequiredMainDishFancyName(
                                player.getQuest(QUEST_SLOT, 2)) +
                        " as the main dish and " +
                            getRequiredDessertFancyName(
                                player.getQuest(QUEST_SLOT, 4)) +
                        " for dessert.";
                decentMeal.setInfoString("Decent Meal for Groongo");
                decentMeal.setBoundTo("Groongo Rahnnt");
                decentMeal.setDescription(
                    "You see a dome-covered decent meal which consists of " +
                    decentMealDescription);
                if (player.equipToInventoryOnly(decentMeal)) {
                    player.setQuest(QUEST_SLOT, 0, "deliver_decentmeal");
                    SpeakerNPC.say(
                        "Here you are! I've just finished preparing " + decentMealDescription +
                        " 你现在应该立刻带着这个适量大餐给我们的多事顾客. " +
                        " 你走路时要非常小心, 不能弄坏了或者摔掉地上!"
                    );
                } else {
                    SpeakerNPC.say(
                        "为多事顾客特殊的大餐已备好," +
                        " 当你能够带给他时请回到这里!"
                    );
                }
            }

            logger.warn("Quest state <" + player.getQuest(QUEST_SLOT) + ">");

        }
    }

    // Groongo uses this to select one main dish among the defined ones
    // the quest is initiated
    class chooseMainDishAction implements ChatAction {

        @Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser SpeakerNPC) {

            String requiredMainDish = getRequiredMainDish();
            String requiredIngredientsForMainDish = getRequiredIngredientsForMainDish(requiredMainDish);

            int attempts = 0;
            String requiredOldMainDish;
            try {
                requiredOldMainDish = player.getQuest(QUEST_SLOT, 2);
            } catch (final NullPointerException e) {
                requiredOldMainDish = "none";
            }
            while (requiredMainDish.equals(requiredOldMainDish) && attempts <= 5 ) {
                requiredMainDish = getRequiredMainDish();
                requiredIngredientsForMainDish = getRequiredIngredientsForMainDish(requiredMainDish);
                attempts++;
            }

            //logger.warn("Attempts for new main dish <" + attempts + ">");

            player.setQuest(QUEST_SLOT, 0, "fetch_maindish");
            player.setQuest(QUEST_SLOT, 1, "inprogress");
            player.setQuest(QUEST_SLOT, 2, requiredMainDish);
            player.setQuest(QUEST_SLOT, 3, requiredIngredientsForMainDish);

            SpeakerNPC.say(
                    "今天我感到真的想要 " +
                    getRequiredMainDishFancyName(requiredMainDish) +
                    ". 现在云问 Chef Stefan 准备我的大餐 #" + requiredMainDish + ", 马上!"
            );

            logger.warn("Quest state <" + player.getQuest(QUEST_SLOT) + ">");

        }
    }

    // Groongo uses this to tell the player which dessert he'd like along with the main dish
    // the quest is advanced further
    class chooseDessertAction implements ChatAction {

        @Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser SpeakerNPC) {

            final String requiredMainDish = player.getQuest(QUEST_SLOT, 2);
            String requiredDessert = getRequiredDessert();
            String requiredIngredientsForDessert = getRequiredIngredientsForDessert(requiredDessert);

            int attempts = 0;
            String requiredOldDessert;
            try {
                requiredOldDessert = player.getQuest(QUEST_SLOT, 3);
            } catch (final NullPointerException e) {
                requiredOldDessert = "none";
            }
            while (requiredDessert.equals(requiredOldDessert) && attempts <= 5 ) {
                requiredDessert = getRequiredDessert();
                requiredIngredientsForDessert = getRequiredIngredientsForDessert(requiredDessert);
                attempts++;
            }

            //logger.warn("Attempts for new dessert <" + attempts + ">");

            player.setQuest(QUEST_SLOT, 0, "tell_dessert");
            player.setQuest(QUEST_SLOT, 4, requiredDessert);
            player.setQuest(QUEST_SLOT, 5, requiredIngredientsForDessert);

            SpeakerNPC.say(
                    "确实, 我不应忘记 " +
                    getRequiredMainDishFancyName(requiredMainDish) +
                    " 我会尽量 " +
                    getRequiredDessertFancyName(requiredDessert) +
                    ". 现在去问 Chef Stefan 准备我的甜点 #" + requiredDessert +
                    "  #dessert, 现在就去!"
            );

            logger.warn("Quest state <" + player.getQuest(QUEST_SLOT) + ">");

        }
    }

    // Groongo uses this to remind the player of what he has to bring him
    // depending on which stage the quest currently is
    class checkQuestInProgressAction implements ChatAction {

        @Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser SpeakerNPC) {

            final String questState = player.getQuest(QUEST_SLOT, 0);
            String meal = "";
            String question = "";
            if  (
                    "fetch_maindish".equals(questState)) {
                meal = getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2));
                question = "你给我带来了吗?";
            } else if (
                    "check_dessert".equals(questState)) {
                meal = getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2));
                //question = " Should I also choose some #dessert to go with that?";
                // not a question but a way to give the player a hint about how to 'ask' for which dessert
                question = " 也许餐后我会选择一些甜点 #dessert ...";
            } else if (
                    "tell_dessert".equals(questState) ||
                    "fetch_dessert".equals(questState) ||
                    "prepare_decentmeal".equals(questState) ||
                    "deliver_decentmeal".equals(questState)) {
                meal =
                    getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2)) +
                    " 作为主餐,然后  " +
                    getRequiredDessertFancyName(player.getQuest(QUEST_SLOT, 4)) +
                    " 作为甜点";
                question = "你给我带来了吗?";
            }

            SpeakerNPC.say(
                    "Bah! 我还在等 " + meal + ". 那就是我叫的正餐! " + question
            );

            logger.warn("Quest state <" + player.getQuest(QUEST_SLOT) + ">");

        }
    }

    // Stefan uses this to tell the player what ingredients he needs
    // for preparing the main dish
    class checkIngredientsForMainDishAction implements ChatAction {

        @Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser SpeakerNPC) {

            final ItemCollection missingIngredients = new ItemCollection();
            missingIngredients.addFromQuestStateString(player.getQuest(QUEST_SLOT, 3).replace(",", ";"));

            SpeakerNPC.say(
                    "Ah! 我们的多事顾客要了 " +
                    getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2)) +
                    " this time. For that I'll need some ingredients that at the moment I'm missing: " +
                    missingIngredients.toStringListWithHash() +
                    ". Do you happen to have them all with you already?"
            );

            logger.warn("Quest state <" + player.getQuest(QUEST_SLOT) + ">");

        }
    }

    // Stefan uses this to tell the player what ingredients he needs
    // for preparing the dessert
    class checkIngredientsForDessertAction implements ChatAction {

        @Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser SpeakerNPC) {

            final ItemCollection missingIngredients = new ItemCollection();
            missingIngredients.addFromQuestStateString(player.getQuest(QUEST_SLOT, 5).replace(",", ";"));

            SpeakerNPC.say(
                    "所以！我们的多号顾客坚决要 " +
                    getRequiredDessertFancyName(player.getQuest(QUEST_SLOT, 4)) +
                    " 作为甜点, 为些我还缺少其他的一些配料: " +
                    missingIngredients.toStringListWithHash() +
                    ". 你身上已经带了这些东西吗?"
            );

            logger.warn("Quest state <" + player.getQuest(QUEST_SLOT) + ">");

        }
    }

    // Stefan uses this
    // when the player has said he has all the ingredients
    // for preparing either the main dish or the dessert
    class collectAllRequestedIngredientsAtOnceAction implements ChatAction {

        private final ChatAction triggerActionOnCompletion;
        private final ConversationStates stateAfterCompletion;

        public collectAllRequestedIngredientsAtOnceAction (
                ChatAction completionAction,
                ConversationStates stateAfterCompletion) {

            this.triggerActionOnCompletion = completionAction;
            this.stateAfterCompletion = stateAfterCompletion;

        }

        @Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
            final String questState = player.getQuest(QUEST_SLOT, 0);
            ItemCollection missingIngredients = getMissingIngredients(player, questState);
            ItemCollection missingIngredientsToFetch = getMissingIngredients(player, questState);
            boolean playerHasAllIngredients = true;
            // preliminary check. we don't take anything from the player, yet
            for (final Map.Entry<String, Integer> ingredient : missingIngredients.entrySet()) {
                final int amount = player.getNumberOfEquipped(ingredient.getKey());
                if ( amount < ingredient.getValue()) {
                    playerHasAllIngredients = false;
                } else {
                    missingIngredientsToFetch.removeItem(ingredient.getKey(), ingredient.getValue());
                }
            }

            if (playerHasAllIngredients) {
                for (final Map.Entry<String, Integer> ingredient : missingIngredients.entrySet()) {
                    player.drop(ingredient.getKey(), ingredient.getValue());
                }
                triggerActionOnCompletion.fire(player, sentence, raiser);
                raiser.setCurrentState(this.stateAfterCompletion);
            } else {
                raiser.say(
                    "Uh oh! For preparing " +
                        getWhatToPrepare(player, questState) +
                    " 我现在就要全部的配料, 你好像没有带来 " +
                        missingIngredientsToFetch.toStringListWithHash() +
                    " ."
                );
            }

            logger.warn("Quest state <" + player.getQuest(QUEST_SLOT) + ">");

        }

        String getWhatToPrepare(final Player player, final String questState) {

            String res = "";
            if  ("fetch_maindish".equals(questState)) {
                res =  getRequiredMainDishFancyName(player.getQuest(QUEST_SLOT, 2));
            } else if ("fetch_dessert".equals(questState)) {
                res =  getRequiredDessertFancyName(player.getQuest(QUEST_SLOT, 4));
            }

            return res;

        }

        ItemCollection getMissingIngredients(final Player player, final String questState) {

            final ItemCollection missingIngredients = new ItemCollection();

            String requiredIngredients = "";
            if  ("fetch_maindish".equals(questState)) {
                requiredIngredients = player.getQuest(QUEST_SLOT, 3);
            } else if ("fetch_dessert".equals(questState)) {
                requiredIngredients = player.getQuest(QUEST_SLOT, 5);
            }

            requiredIngredients = requiredIngredients.replaceAll(",", ";");
            missingIngredients.addFromQuestStateString(requiredIngredients);

            return missingIngredients;

        }
    }

    // The quest is started or rejected by first interacting with Groongo Rahnnt
    public void stageBeginQuest() {

        final SpeakerNPC npc = npcs.get("Groongo Rahnnt");

        // Player greets Groongo,
        // quest has been rejected in the past
        npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc.getName()),
                new QuestInStateCondition(QUEST_SLOT, 0, "rejected")),
            ConversationStates.QUEST_OFFERED,
            "Gah! [insults player]" +
            " 等了那么久, 我身上都落满了灰尘..." +
            " 你准备带给我一些正餐 #meal ?",
            null
        );

        // Player has done the quest in the past,
        // time enough has elapsed to take the quest again
        npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new AndCondition(
                new QuestCompletedCondition(QUEST_SLOT),
                new TimePassedCondition(QUEST_SLOT, 6, REPEATQUEST_DELAY)),
            ConversationStates.QUEST_OFFERED,
            "Oh, 你还在绕图, 我明白！你带来了其他正餐 #meal?",
            null
        );

        // Player has done the quest in the past,
        // not enough time has elapsed to take the quest again
        npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new AndCondition(
                new QuestCompletedCondition(QUEST_SLOT),
                new NotCondition(
                    new TimePassedCondition(QUEST_SLOT, 6, REPEATQUEST_DELAY))),
            ConversationStates.ATTENDING,
            null,
            new SayTimeRemainingAction(QUEST_SLOT, 6, REPEATQUEST_DELAY,
                "我太饿了现在. . . 我I'm not so hungry now... I will be fine for at least")
        );

        // Player asks Groongo for a quest, 1st time!
        // quest is not running
        npc.add(ConversationStates.ATTENDING,
            ConversationPhrases.QUEST_MESSAGES,
            new AndCondition(
                new QuestNotStartedCondition(QUEST_SLOT),
                new NotCondition(
                    new QuestCompletedCondition(QUEST_SLOT))),
            ConversationStates.QUEST_OFFERED,
            "It was about time! [insults player]" +
            " I've been waiting here for so long that I've now got cobwebs under my armpits..." +
            " Are you going to bring me a decent #meal now?",
            null
        );

        // Player is curious about meal when offered the quest
        // quest not running yet
        npc.add(ConversationStates.QUEST_OFFERED,
            "meal",
            new OrCondition(
                new QuestNotStartedCondition(QUEST_SLOT),
                new QuestCompletedCondition(QUEST_SLOT)),
            ConversationStates.QUEST_OFFERED,
            "Bah! I want to have a decent meal and try something different than soups or pies!" +
            " Will you bring me what I will ask?",
            null
        );

        // Player has just accepted the quest,
        // Player says 'meal' again,
        // give some hints, quest is running
        npc.add(ConversationStates.QUEST_STARTED,
            "meal",
            new OrCondition(
                new QuestNotStartedCondition(QUEST_SLOT),
                new QuestCompletedCondition(QUEST_SLOT)),
            ConversationStates.IDLE,
            "I've just told you what I want! Now go, and tell Chef Stefan to prepare it at once!",
            null
        );

        // Player has just accepted the quest,
        // Player says short name of a main dish,
        // give final hints, quest is running
        Iterator<String> i = REQUIRED_MAIN_DISHES.iterator();
        while (i.hasNext()) {
            npc.add(ConversationStates.QUEST_STARTED,
                i.next(),
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_maindish"),
                ConversationStates.IDLE,
                "I'm sure Chef Stefan knows how to prepare that, you'll find him in the kitchen. Now go!",
                null
            );
        }
        // Player accepts the quest,
        // quest is started
        npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.YES_MESSAGES,
            new OrCondition(
                new QuestNotStartedCondition(QUEST_SLOT),
                new QuestCompletedCondition(QUEST_SLOT)),
            ConversationStates.QUEST_STARTED,
            null,
            new chooseMainDishAction()
        );

        // Player rejects the quest,
        // has not rejected it last time
        // Groongo turns idle and some Karma is lost.
        npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.NO_MESSAGES,
            new AndCondition(
                new QuestNotActiveCondition(QUEST_SLOT),
                new QuestNotInStateCondition(QUEST_SLOT, 0, "rejected")),
            ConversationStates.IDLE,
            "Stop pestering me and get lost in a dungeon then!",
            new MultipleActions(
                new SetQuestAction(QUEST_SLOT, 0, "rejected"),
                new DecreaseKarmaAction(20.0))
        );

        // Player rejects the quest,
        // Player has rejected the quest last time,
        // Groongo turns idle and some (more) Karma is lost.
        npc.add(ConversationStates.QUEST_OFFERED,
            ConversationPhrases.NO_MESSAGES,
            new QuestInStateCondition(QUEST_SLOT, 0, "rejected"),
            ConversationStates.IDLE,
            "Stat away from me and get lost in a forest then!",
            new MultipleActions(
                new SetQuestAction(QUEST_SLOT, 0, "rejected"),
                new DecreaseKarmaAction(20.0))
        );
    }

    // the quest is advanced by next interacting with Stefan
    public void stageCollectIngredientsForMainDish() {

        final SpeakerNPC npc = npcs.get("Stefan");

        npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc.getName()),
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_maindish")),
            ConversationStates.ATTENDING,
            "Hello!" +
            " I'm so busy that I never get to leave this kitchen..." +
            " Don't tell me I now have to prepare another #meal!",
            null
        );

        // Player remembers generic instructions from Groongo,
        // Player says 'meal'
        // Ask if he has the required ingredients
        npc.add(ConversationStates.ATTENDING,
            "meal",
            new AndCondition(
                new GreetingMatchesNameCondition(npc.getName()),
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_maindish")),
            ConversationStates.QUESTION_1,
            null,
            new checkIngredientsForMainDishAction()
        );

        // Player remembers Groongo asked for a specific main dish
        // Player says one of the known REQUIRED_MAIN_DISHES
        // Add all the main dishes trigger words
        Iterator<String> i = REQUIRED_MAIN_DISHES.iterator();
        while (i.hasNext()) {
            npc.add(ConversationStates.ATTENDING,
                i.next(),
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_maindish"),
                ConversationStates.QUESTION_1,
                null,
                new checkIngredientsForMainDishAction()
            );
        }

        // Player has been asked if he has the ingredients for main dish,
        // Player answers negatively
        npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.NO_MESSAGES,
            new QuestInStateCondition(QUEST_SLOT, 0, "fetch_maindish"),
            ConversationStates.ATTENDING,
            "Too bad! Be sure to bring me those ingredients all at once!",
            null
        );

        // Player has been asked if he has the ingredients for main dish,
        // Player answers affirmatively,
        // the quest is possibly advanced to the next step
        npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.YES_MESSAGES,
            null,
            ConversationStates.IDLE,
            null,
            new collectAllRequestedIngredientsAtOnceAction(
                new advanceQuestInProgressAction(),
                ConversationStates.IDLE)
        );
    }

    // the quest is advanced further by interacting with both Groongo and Stefan again
    public void stageCheckForDessert() {

        final SpeakerNPC npc_chef = npcs.get("Stefan");
        final SpeakerNPC npc_customer = npcs.get("Groongo Rahnnt");

        // Player checks back with Stefan
        // Player doesn't yet know which dessert Groongo would like
        npc_chef.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc_chef.getName()),
                new QuestInStateCondition(QUEST_SLOT, 0, "check_dessert")),
            ConversationStates.ATTENDING,
            "Oh, you're back so soon..." +
            " And you haven't checked which #dessert" +
            " our troublesome customer would like to have!",
            null
        );

        // give some hints about what to do next
        // when the player says dessert
        npc_chef.add(ConversationStates.ATTENDING,
            "dessert",
            new QuestInStateCondition(QUEST_SLOT, 0, "check_dessert"),
            ConversationStates.IDLE,
            "I know how to prepare several kind of desserts..." +
            " You better check with our troublesome customer to know which one he prefers!",
            null
        );

        // give some hints about what to do next
        // when the player says one of the main dishes
        Iterator<String> i = REQUIRED_MAIN_DISHES.iterator();
        while (i.hasNext()) {
            npc_chef.add(ConversationStates.ATTENDING,
                i.next(),
                new QuestInStateCondition(QUEST_SLOT, 0, "check_dessert"),
                ConversationStates.ATTENDING,
                "I'm preparing that already..." +
                " You should now check with our troublesome castomer" +
                " what #dessert he'd like to have with that.",
                null
            );
        }

        // Player knows which dessert Groongo wants
        // quest is running
        npc_chef.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc_chef.getName()),
                new QuestInStateCondition(QUEST_SLOT, 0, "tell_dessert")),
            ConversationStates.ATTENDING,
            "Here you are..." +
            " I still wonder what #dessert our troublesome customer" +
            " would like to have along with his main dish...",
            null
        );

        // Player knows which dessert Groongo wants,
        // Advance the quest
        npc_chef.add(ConversationStates.ATTENDING,
            "dessert",
            new QuestInStateCondition(QUEST_SLOT, 0, "tell_dessert"),
            ConversationStates.QUESTION_1,
            null,
            new MultipleActions (
                new advanceQuestInProgressAction(),
                new checkIngredientsForDessertAction()
            )
        );

        // Player knows which dessert Groongo wants,
        // Add all desserts as triggers
        // Advance the quest
        Iterator<String> j = REQUIRED_DESSERTS.iterator();
        while (j.hasNext()) {
            npc_chef.add(ConversationStates.ATTENDING,
                j.next(),
                new QuestInStateCondition(QUEST_SLOT, 0, "tell_dessert"),
                ConversationStates.QUESTION_1,
                null,
                new MultipleActions (
                    new advanceQuestInProgressAction(),
                    new checkIngredientsForDessertAction()
                )
            );
        }

        // Player checks back with Groongo,
        // quest is running
        npc_customer.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc_customer.getName()),
                new QuestInStateCondition(QUEST_SLOT, 0, "check_dessert")),
            ConversationStates.ATTENDING,
            "Ah, you're back already..." +
            " And I still don't see the #meal I've asked!",
            null
        );

        // Player says meal to be reminded,
        // will trigger checkQuestInProgressAction()
        // that will give hints about dessert
        // quest is running
        npc_customer.add(
            ConversationStates.ATTENDING,
            "meal",
            new AndCondition(
                new QuestActiveCondition(QUEST_SLOT),
                new QuestInStateCondition(QUEST_SLOT, 0, "check_dessert")),
            ConversationStates.ATTENDING,
            null,
            new checkQuestInProgressAction()
        );

        // Player says dessert to have Groongo choose which one he'd like
        // Advance the quest
        npc_customer.add(
            ConversationStates.ATTENDING,
            "dessert",
            new AndCondition(
                new QuestActiveCondition(QUEST_SLOT),
                new QuestInStateCondition(QUEST_SLOT, 0, "check_dessert")),
            ConversationStates.IDLE,
            null,
            new chooseDessertAction()
        );
    }

    // the quest is advanced further again by interacting with Stefan
    public void stageCollectIngredientsForDessert() {

        final SpeakerNPC npc = npcs.get("Stefan");

        npc.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc.getName()),
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert")),
            ConversationStates.ATTENDING,
            "Ah, you're back! I'm afraid that I'm still missing some ingredients for preparing a good #dessert...",
            null
        );

        // Player remembers generic instructions from Groongo,
        // Player says 'dessert'
        npc.add(ConversationStates.ATTENDING,
            "dessert",
            new AndCondition(
                new GreetingMatchesNameCondition(npc.getName()),
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert")),
            ConversationStates.QUESTION_1,
            null,
            new checkIngredientsForDessertAction()
        );

        // Player says one of the defined REQUIRED_MAIN_DISHES
        // when involved in fetching the ingredients for dessert already
        // Add all the main dishes trigger words
        Iterator<String> i = REQUIRED_MAIN_DISHES.iterator();
        while (i.hasNext()) {
            npc.add(ConversationStates.ATTENDING,
                i.next(),
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert"),
                ConversationStates.ATTENDING,
                "I'm preparing that already..." +
                " I now miss some ingredients for preparing the #dessert for our troublesome customer!",
                null
            );
        }

        // Player says one of the defined REQUIRED_DESSERTS
        // remind him of the ingredients he has to fetch
        // Add all the desserts trigger words
        Iterator<String> j = REQUIRED_DESSERTS.iterator();
        while (j.hasNext()) {
            npc.add(ConversationStates.ATTENDING,
                j.next(),
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert"),
                ConversationStates.QUESTION_1,
                null,
                new checkIngredientsForDessertAction()
            );
        }

        // Player has been asked if he has the ingredients for dessert,
        // Player answers negatively
        npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.NO_MESSAGES,
            new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert"),
            ConversationStates.IDLE,
            "Oh, fetch them quickly then! And be sure to bring them to me all at the same time!",
            null
        );

        // Player has been asked if he has the ingredients for dessert,
        // Player answers affirmatively,
        // the quest is possibly advanced to the next step
        npc.add(ConversationStates.QUESTION_1,
            ConversationPhrases.YES_MESSAGES,
            new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert"),
            ConversationStates.IDLE,
            null,
            new collectAllRequestedIngredientsAtOnceAction(
                new MultipleActions(
                    // meal will be ready in MEALREADY_DELAY from now
                    new SetQuestToTimeStampAction(QUEST_SLOT, 6),
                    new advanceQuestInProgressAction()
                ),
                ConversationStates.IDLE)
        );

    }

    // the states for interacting with both Groongo and Stefan
    // when the quest has reached its almost final stage
    public void stageWaitForMeal() {

        final SpeakerNPC npc_chef = npcs.get("Stefan");
        final SpeakerNPC npc_customer = npcs.get("Groongo Rahnnt");

        // Player checks back with Stefan when the decent meal is being prepared
        npc_chef.add(
            ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc_chef.getName()),
                new AndCondition(
                    new QuestInStateCondition(QUEST_SLOT, 0, "prepare_decentmeal"),
                    new NotCondition(
                        new TimePassedCondition(QUEST_SLOT, 6, MEALREADY_DELAY)))),
            ConversationStates.IDLE,
            null,
            new SayTimeRemainingAction(QUEST_SLOT, 6, MEALREADY_DELAY,
                "Please come back in while." +
                " The meal for our troublesome customer won't be ready before")
        );

        // Player checks back with Stefan when the decent meal is ready
        npc_chef.add(
            ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc_chef.getName()),
                new AndCondition(
                    new QuestInStateCondition(QUEST_SLOT, 0, "prepare_decentmeal"),
                    new TimePassedCondition(QUEST_SLOT, 6, MEALREADY_DELAY))),
            ConversationStates.IDLE,
            null,
            new advanceQuestInProgressAction()
        );

        // Player says his greetings to Groongo,
        // the quest is running
        npc_customer.add(
            ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc_customer.getName()),
                new OrCondition(
                    new QuestInStateCondition(QUEST_SLOT, 0, "fetch_maindish"),
                    new QuestInStateCondition(QUEST_SLOT, 0, "tell_dessert"),
                    new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert"),
                    new QuestInStateCondition(QUEST_SLOT, 0, "prepare_decentmeal"))),
            ConversationStates.QUESTION_1,
            "Here you are! Is my #meal finally ready yet?",
            null
        );

        // Player wants to be reminded
        // add trigger words for both 'meal' and 'dessert'
        npc_customer.add(ConversationStates.QUESTION_1,
            Arrays.asList("meal", "dessert"),
            new OrCondition(
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_maindish"),
                new QuestInStateCondition(QUEST_SLOT, 0, "tell_dessert"),
                new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert"),
                new QuestInStateCondition(QUEST_SLOT, 0, "prepare_decentmeal")),
            ConversationStates.QUESTION_1,
            null,
            new checkQuestInProgressAction()
        );

        // Player wants to be reminded
        // add trigger words for each of the short names of a main dish
        Iterator<String> i = REQUIRED_MAIN_DISHES.iterator();
        while (i.hasNext()) {
            npc_customer.add(ConversationStates.QUESTION_1,
                i.next(),
                new OrCondition(
                    new QuestInStateCondition(QUEST_SLOT, 0, "fetch_maindish"),
                    new QuestInStateCondition(QUEST_SLOT, 0, "tell_dessert"),
                    new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert"),
                    new QuestInStateCondition(QUEST_SLOT, 0, "prepare_decentmeal")),
                ConversationStates.QUESTION_1,
                null,
                new checkQuestInProgressAction()
            );
        }

        // Player wants to be reminded
        // add trigger words for each of the short names of a dessert
        Iterator<String> j = REQUIRED_DESSERTS.iterator();
        while (j.hasNext()) {
            npc_customer.add(ConversationStates.QUESTION_1,
                j.next(),
                new OrCondition(
                    new QuestInStateCondition(QUEST_SLOT, 0, "fetch_maindish"),
                    new QuestInStateCondition(QUEST_SLOT, 0, "tell_dessert"),
                    new QuestInStateCondition(QUEST_SLOT, 0, "fetch_dessert"),
                    new QuestInStateCondition(QUEST_SLOT, 0, "prepare_decentmeal")),
                ConversationStates.QUESTION_1,
                null,
                new checkQuestInProgressAction()
            );
        }

        // Player answers no
        // quest running, not completed yet
        npc_customer.add(ConversationStates.QUESTION_1,
            ConversationPhrases.NO_MESSAGES,
            new QuestNotCompletedCondition(QUEST_SLOT),
            ConversationStates.IDLE,
            "GAH! [insults player] Why did you come back then!",
            null
        );

        // Player answers yes (when not having the decent meal with him)
        // quest running
        npc_customer.add(ConversationStates.QUESTION_1,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(
                new QuestNotCompletedCondition(QUEST_SLOT),
                new NotCondition(new PlayerHasItemWithHimCondition("decent meal"))),
            ConversationStates.IDLE,
            "GAAAH! [instults player] Who are you trying to fool?! Go in that kitchen and come back with my meal, NOOOOOW!",
            null
        );
    }

    // the states for interacting with both Groongo and Stefan
    // when the quest is in its final stage
    public void stageDeliverMeal() {

        final SpeakerNPC npc_chef = npcs.get("Stefan");
        final SpeakerNPC npc_customer = npcs.get("Groongo Rahnnt");

        /**
         * This is intended to be the better end of the quest.
         * After getting his decent meal, Groongo hints the player
         * to bring his #thanks to Chef Stefan.
         */
        final List<ChatAction> betterEndQuestActions = new LinkedList<ChatAction>();
        betterEndQuestActions.add(new SetQuestAction(QUEST_SLOT, 0, "done"));
        betterEndQuestActions.add(new SetQuestAction(QUEST_SLOT, 1, "complete"));
        betterEndQuestActions.add(new IncreaseKarmaAction(10.0));
        betterEndQuestActions.add(
            new ChatAction() {
                @Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
                    final int amountOfMoneys = Rand.randUniform(2000, 4000);
                    final int amountOfSandwiches = Rand.randUniform(6, 12);
                    final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
                    final StackableItem sandwich = (StackableItem) SingletonRepository.getEntityManager().getItem("三明治");

                    money.setQuantity(amountOfMoneys);
                    sandwich.setQuantity(amountOfSandwiches);
                    sandwich.setBoundTo(player.getName());
                    sandwich.setDescription("You see an experimental 三明治 made by Chef Stefan.");
                    sandwich.put("amount", player.getBaseHP()/2);
                    sandwich.put("frequency", 10);
                    sandwich.put("regen", 50);
                    sandwich.put("persistent", 1);

                    npc.say("Very well! Your help has been precious to me." +
                        " Please, accept " +
                        amountOfSandwiches + " experimental " +
                        "三明治" +
                        " and " +
                        amountOfMoneys + " " +
                         "money" +
                        " as my reward!"
                    );

                    player.equipOrPutOnGround(money);
                    player.equipOrPutOnGround(sandwich);

                    logger.warn("Quest state <" + player.getQuest(QUEST_SLOT) + ">");

                }
            }
        );

        npc_chef.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new QuestCompletedCondition(QUEST_SLOT),
                new QuestInStateCondition(QUEST_SLOT, 1, "incomplete"),
                new NotCondition(
                    new TimePassedCondition(QUEST_SLOT, 6, BRINGTHANKS_DELAY))
            ),
            ConversationStates.QUESTION_1,
            "So... What did our troublesome customer say about the meal?",
            null
        );

        npc_chef.add(ConversationStates.QUESTION_1,
                "thanks",
                new AndCondition(
                    new QuestCompletedCondition(QUEST_SLOT),
                    new QuestInStateCondition(QUEST_SLOT, 1, "incomplete"),
                    new NotCondition(
                        new TimePassedCondition(QUEST_SLOT, 6, BRINGTHANKS_DELAY))
                ),
                ConversationStates.QUESTION_1,
                " FIXME omero: player gets better reward.",
                new MultipleActions(betterEndQuestActions)
            );

        // Player says his greetings to Groongo,
        // the quest is running
        npc_customer.add(ConversationStates.IDLE,
            ConversationPhrases.GREETING_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc_customer.getName()),
                new QuestActiveCondition(QUEST_SLOT),
                new QuestInStateCondition(QUEST_SLOT, 0, "deliver_decentmeal")),
            ConversationStates.QUESTION_1,
            "Oh, you're back! Do you finally have my #meal?",
            null
        );

        // Player says meal to be reminded of what is still missing
        // quest is running
        npc_customer.add(ConversationStates.QUESTION_1,
            Arrays.asList("meal", "dessert"),
            new AndCondition(
                new GreetingMatchesNameCondition(npc_customer.getName()),
                new QuestActiveCondition(QUEST_SLOT),
                new QuestInStateCondition(QUEST_SLOT, 0, "deliver_decentmeal")),
            ConversationStates.QUESTION_1,
            null,
            new checkQuestInProgressAction()
        );

        // Player answers no
        // waiting for Stefan?
        npc_customer.add(ConversationStates.QUESTION_1,
            ConversationPhrases.NO_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc_customer.getName()),
                new QuestActiveCondition(QUEST_SLOT),
                new QuestInStateCondition(QUEST_SLOT, 0, "deliver_decentmeal")),
            ConversationStates.IDLE,
            "Then hurry up, go and fetch it!",
            null);

        /**
         * This is intended to be the normal end of the quest.
         * After getting his decent meal, Groongo hints the player
         * to bring his #thanks to Chef Stefan.
         */
        final List<ChatAction> normalEndQuestActions = new LinkedList<ChatAction>();
        normalEndQuestActions.add(new DropInfostringItemAction("decent meal","Decent Meal for Groongo"));
        normalEndQuestActions.add(new SetQuestAction(QUEST_SLOT, 0, "done"));
        normalEndQuestActions.add(new SetQuestAction(QUEST_SLOT, 1, "incomplete"));
        normalEndQuestActions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 6));
        normalEndQuestActions.add(new IncrementQuestAction(QUEST_SLOT, 7, 1));
        normalEndQuestActions.add(new IncreaseXPAction(XP_REWARD));
        normalEndQuestActions.add(new IncreaseKarmaAction(50.0));
        normalEndQuestActions.add(new InflictStatusOnNPCAction("三明治"));
        normalEndQuestActions.add(
            new ChatAction() {
                @Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
                    final int amountOfMoneys = Rand.randUniform(1000, 1500);
                    final int amountOfPies = Rand.randUniform(10, 15);
                    final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
                    final StackableItem pie = (StackableItem) SingletonRepository.getEntityManager().getItem("pie");
                    money.setQuantity(amountOfMoneys);
                    pie.setQuantity(amountOfPies);

                    npc.say("Here, take " +
                        amountOfPies + " " +
                         "pie" +
                        " and " +
                        amountOfMoneys + " " +
                         "money" +
                        " as my reward! Please bring my very deserved #thanks to" +
                        " Chef Stefan for preparing such a decent meal!"
                    );

                    player.equipOrPutOnGround(money);
                    player.equipOrPutOnGround(pie);

                    logger.warn("Quest state <" + player.getQuest(QUEST_SLOT) + ">");

                }
            }
        );

        // Player answers yes and he indeed has the meal for Groongo
        npc_customer.add(ConversationStates.QUESTION_1,
            ConversationPhrases.YES_MESSAGES,
            new AndCondition(
                new GreetingMatchesNameCondition(npc_customer.getName()),
                new QuestActiveCondition(QUEST_SLOT),
                new QuestInStateCondition(QUEST_SLOT, 0, "deliver_decentmeal"),
                new PlayerHasInfostringItemWithHimCondition("decent meal", "Decent Meal for Groongo")),
            ConversationStates.IDLE,
            null,
            new MultipleActions(normalEndQuestActions)
        );
    }
}
