package net.hearthian.acceleratingHappyGhasts.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HappyGhast.class)
public class HappyGhastMixin extends Animal {
    @Unique
    private double currentSpeedModifier = 1;

    @Unique
    protected void increaseSpeed() {
        if (this.currentSpeedModifier < 2.3) {
            this.currentSpeedModifier += 0.001;
        }
    }

    @Unique
    protected void decreaseSpeed() {
        decreaseSpeed(0.005);
    }
    @Unique
    protected void decreaseSpeed(double ratio) {
        if ((this.currentSpeedModifier - ratio) >= 1) {
            this.currentSpeedModifier -= ratio;
        } else {
            this.currentSpeedModifier = 1;
        }
    }

    protected HappyGhastMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    @Override
    public boolean isFood(ItemStack itemStack) {
        return false;
    }

    @Shadow
    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    @ModifyArg(method = "createAttributes", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier$Builder;add(Lnet/minecraft/core/Holder;D)Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier$Builder;", ordinal = 2), index = 1)
    private static double registerGoals(double d) {
        return 0.05;
    }

    @Inject(at = @At("HEAD"), method = "travel", cancellable = true)
    public void travel(Vec3 vec3, CallbackInfo ci) {
        float f = (float) ((float)this.getAttributeValue(Attributes.FLYING_SPEED) * this.currentSpeedModifier * 5.0f / 3.0f);

        this.travelFlying(vec3, f, f, f);

        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "getRiddenInput")
    protected void getRiddenInput(Player player, Vec3 vec3, CallbackInfoReturnable<Vec3> cir) {
        if (player.zza > 0.0f) {
            increaseSpeed();
        } else if (player.zza < 0.0f) {
            decreaseSpeed(0.01);
        } else {
            decreaseSpeed();
        }
    }

    @ModifyArg(method = "getRiddenInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;scale(D)Lnet/minecraft/world/phys/Vec3;"))
    protected double getRiddenInputReturn(double d) {
        return (double)3.9f * this.getAttributeValue(Attributes.FLYING_SPEED) * this.currentSpeedModifier;
    }
}