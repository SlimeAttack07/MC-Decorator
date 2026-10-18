package slimeattack07.decorator.util;

import net.minecraft.ChatFormatting;

import java.util.stream.Stream;

public enum MachineTier {
    BASIC,
    EXPERIENCED,
    EXPERT,
    MASTER,
    LEGEND;

    public boolean hasReachedTier(int experience){
        return switch(this){
            case BASIC -> true;
            case EXPERIENCED -> experience >= 15;
            case EXPERT -> experience >= 30;
            case MASTER -> experience >= 45;
            case LEGEND -> experience >= 60;
            default -> false;
        };
    }

    public int getMultiplier(){
        return switch(this){
            case BASIC -> 1;
            case EXPERIENCED -> 2;
            case EXPERT -> 4;
            case MASTER -> 8;
            case LEGEND -> 16;
        };
    }

    public int getFocusChance(){
        return switch(this){
            case BASIC -> 20;
            case EXPERIENCED -> 40;
            case EXPERT -> 60;
            case MASTER -> 80;
            case LEGEND -> 100;
        };
    }

    public ChatFormatting getColor(){
        return switch(this){
            case BASIC -> ChatFormatting.WHITE;
            case EXPERIENCED -> ChatFormatting.AQUA;
            case EXPERT -> ChatFormatting.GREEN;
            case MASTER -> ChatFormatting.DARK_PURPLE;
            case LEGEND -> ChatFormatting.GOLD;
        };
    }

    public static MachineTier[] reversed(){
        return new MachineTier[]{LEGEND, MASTER, EXPERT, EXPERIENCED, BASIC};
    }

    public static MachineTier toTier(int experience){
        return Stream.of(MachineTier.reversed()).filter(entry -> entry.hasReachedTier(experience)).findFirst().orElse(MachineTier.BASIC);
    }
}
