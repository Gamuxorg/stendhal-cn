package games.stendhal.server.maps.semos.city;

import java.util.Collection;
import java.util.Map;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.config.annotations.TestServerOnly;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.spell.Spell;
import marauroa.common.game.RPSlot;

/**
 * An NPC for testing purposes, that easily enables a player to play around with magic
 *
 * @author madmetzger
 */
@TestServerOnly
public class MagicTeacherNPC implements ZoneConfigurator {

	/**
	 * This ChatAction prepares a player with all he needs to play around with magic
	 *
	 * @author madmetzger
	 */
	private final class TeachMagicAction implements ChatAction {

		@Override
		public void fire(Player player, Sentence sentence, EventRaiser npc) {
			enableSpellsFeature(player);
			boostMana(player);
			equipSpells(player);
			equipManaPotions(player);
		}

		private void equipManaPotions(Player player) {
			StackableItem potion = (StackableItem) SingletonRepository.getEntityManager().getItem("mana");
			potion.setQuantity(1000);
			player.equipOrPutOnGround(potion);
		}

		private void equipSpells(Player player) {
			EntityManager em = SingletonRepository.getEntityManager();
			RPSlot slot = player.getSlot("spells");
			Collection<String> spells = em.getConfiguredSpells();
			for (String spellName : spells) {
				Spell s = em.getSpell(spellName);
				slot.add(s);
			}
		}

		private void boostMana(Player player) {
			player.setBaseMana(1000);
			player.setMana(1000);
		}

		private void enableSpellsFeature(Player player) {
			player.setFeature("spells", true);
		}

	}

	@Override
	public void configureZone(StendhalRPZone zone,
		Map<String, String> attributes) {
		SpeakerNPC npc = new SpeakerNPC("梅林") {

			@Override
			protected void createDialog() {
				add(ConversationStates.ATTENDING, "传授", null, ConversationStates.SERVICE_OFFERED, "你想学习魔法吗?", null);
				add(ConversationStates.SERVICE_OFFERED, ConversationPhrases.YES_MESSAGES, null, ConversationStates.ATTENDING, null, new TeachMagicAction());
			}

		};
		npc.addGreeting("Hello, 我是这里的魔法老师！可以 #传授 给你一些魔法技能.");
		npc.addJob("我的工作是 #传授 基础魔法, 所以你可以在这里学习.");
		npc.addHelp("如果你需要更多的帮助, 请查看 #https://stendhalgame.org/wiki/Ideas_for_Stendhal/Magic , 或者, 你可以向 '#arianne' 寻求帮助.");
		npc.addOffer("我能 #传授 给你一些魔法知识.");
		npc.addGoodbye("要坚持训练!");
		npc.setPosition(20, 26);
		npc.setEntityClass("blueoldwizardnpc");
		zone.add(npc);
	}

}
