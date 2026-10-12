package com.bewitchment.api.event;

import com.bewitchment.machine.CauldronTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Fired whenever an event involving the witches cauldron occurs
 * <p>
 * If a method utilizes this {@link Event} as its parameter, the method will
 * receive every child event of this class.<br>
 */
public class WitchesCauldronEvent extends Event {
	private final CauldronTile cauldron;
	private final World world;

	public WitchesCauldronEvent(CauldronTile cauldron, World world) {
		this.cauldron = cauldron;
		this.world = world;
	}

	public CauldronTile getCauldron() {
		return cauldron;
	}

	public World getWorld() {
		return world;
	}

	/**
	 * CreatePotionEvent is fired whenever a potion is about to be created and bottled.
	 * <p>
	 * When canceled, no potion will be created and the cauldron will not be drained.
	 */
	@Cancelable
	public static class CreatePotionEvent extends WitchesCauldronEvent {
		private PlayerEntity user;
		private int bottles;
		private boolean boosted;
		private boolean allowHigher;

		public CreatePotionEvent(CauldronTile cauldron, PlayerEntity user, int bottles, boolean boosted) {
			super(cauldron, cauldron == null ? null : cauldron.getLevel());
			this.user = user;
			this.bottles = bottles;
			this.boosted = boosted;
			this.allowHigher = false;
		}

		public PlayerEntity getUser() {
			return user;
		}

		public void setUser(PlayerEntity user) {
			this.user = user;
		}

		public int getBottles() {
			return bottles;
		}

		public void setBottles(int bottles) {
			this.bottles = bottles;
		}

		public boolean isBoosted() {
			return boosted;
		}

		public void setBoosted(boolean boosted) {
			this.boosted = boosted;
		}

		public boolean allowsHigher() {
			return allowHigher;
		}

		public void setAllowHigher(boolean allowHigher) {
			this.allowHigher = allowHigher;
		}
	}

	/**
	 * PotionCreatedEvent is fired AFTER the potion to be created is determined.
	 * <p>
	 * When canceled, no potion will be created.
	 */
	@Cancelable
	public static class PotionCreatedEvent extends WitchesCauldronEvent {
		private PlayerEntity user;
		private int bottles;
		private ItemStack potionStack;

		public PotionCreatedEvent(CauldronTile cauldron, PlayerEntity user, int bottles, ItemStack potionStack) {
			super(cauldron, cauldron == null ? null : cauldron.getLevel());
			this.user = user;
			this.bottles = bottles;
			this.potionStack = potionStack;
		}

		public PlayerEntity getUser() {
			return user;
		}

		public void setUser(PlayerEntity user) {
			this.user = user;
		}

		public int getBottles() {
			return bottles;
		}

		public void setBottles(int bottles) {
			this.bottles = bottles;
		}

		public ItemStack getPotionStack() {
			return potionStack;
		}

		public void setPotionStack(ItemStack potionStack) {
			this.potionStack = potionStack;
		}
	}
}
