package mod.azure.doomangelring.compat;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import mod.azure.doomangelring.register.ItemRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.InterModComms;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.type.capability.ICurio;

public class CuriosCompat {
	public static void sendImc() {
		InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE,
				() -> new SlotTypeMessage.Builder("doomangelring").build());
	}

	public static ICapabilityProvider initCapabilities() {
		ICurio curio = new ICurio() {
			@Override
			public boolean canRightClickEquip() {
				return true;
			}

			@Override
			public void onEquip(String identifier, int index, LivingEntity livingEntity) {
				if (livingEntity instanceof PlayerEntity) {
					startFlying((PlayerEntity) livingEntity);
				}
			}

			@Override
			public void onUnequip(String identifier, int index, LivingEntity livingEntity) {
				if (livingEntity instanceof PlayerEntity) {
					stopFlying((PlayerEntity) livingEntity);
				}
			}

			private void startFlying(PlayerEntity player) {
				if (!player.isCreative() && !player.isSpectator()) {
					player.abilities.allowFlying = true;
					player.sendPlayerAbilities();
				}
			}

			private void stopFlying(PlayerEntity player) {
				if (!player.isCreative() && !player.isSpectator()) {
					player.abilities.isFlying = false;
					player.abilities.allowFlying = false;
					player.sendPlayerAbilities();
				}
			}

			@Override
			public void curioTick(String identifier, int index, LivingEntity livingEntity) {
				if (livingEntity instanceof PlayerEntity) {
					PlayerEntity player = ((PlayerEntity) livingEntity);
					if (!player.abilities.allowFlying) {
						startFlying(player);
					}
				}
			}

			@Override
			public boolean canEquip(String identifier, LivingEntity entityLivingBase) {
				return !CuriosApi.getCuriosHelper().findEquippedCurio(ItemRegistry.RING.get(), entityLivingBase)
						.isPresent();
			}
		};

		return new ICapabilityProvider() {
			private final LazyOptional<ICurio> curioOpt = LazyOptional.of(() -> curio);

			@Nonnull
			@Override
			public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {

				return CuriosCapability.ITEM.orEmpty(cap, curioOpt);
			}
		};
	}

	public static boolean isRingInCuriosSlot(ItemStack ring, LivingEntity player) {
		return CuriosApi.getCuriosHelper().findEquippedCurio(ring.getItem(), player).isPresent();
	}
}