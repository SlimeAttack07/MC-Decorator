package slimeattack07.decorator.util;

import net.minecraft.ChatFormatting;
import slimeattack07.decorator.Decorator;

import java.util.LinkedHashMap;
import java.util.Map;
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
            case EXPERIENCED -> ChatFormatting.GREEN;
            case EXPERT -> ChatFormatting.AQUA;
            case MASTER -> ChatFormatting.DARK_PURPLE;
            case LEGEND -> ChatFormatting.GOLD;
        };
    }

    public LinkedHashMap<DecorationTier, Double> getDistribution(){
        return switch (this){
            case BASIC -> new LinkedHashMap<>(Map.of(DecorationTier.MODEST, 55d, DecorationTier.GRAND, 25d, DecorationTier.SUPERB, 12d, DecorationTier.MAJESTIC, 6d, DecorationTier.SUPREME, 2d));
            case EXPERIENCED -> new LinkedHashMap<>(Map.of(DecorationTier.MODEST, 48d, DecorationTier.GRAND, 24d, DecorationTier.SUPERB, 14d, DecorationTier.MAJESTIC, 9d, DecorationTier.SUPREME, 5d));
            case EXPERT -> new LinkedHashMap<>(Map.of(DecorationTier.MODEST, 40d, DecorationTier.GRAND, 22d, DecorationTier.SUPERB, 16d, DecorationTier.MAJESTIC, 12d, DecorationTier.SUPREME, 10d));
            case MASTER -> new LinkedHashMap<>(Map.of(DecorationTier.MODEST, 30d, DecorationTier.GRAND, 21d, DecorationTier.SUPERB, 18d, DecorationTier.MAJESTIC, 16d, DecorationTier.SUPREME, 15d));
            case LEGEND -> new LinkedHashMap<>(Map.of(DecorationTier.MODEST, 20d, DecorationTier.GRAND, 20d, DecorationTier.SUPERB, 20d, DecorationTier.MAJESTIC, 20d, DecorationTier.SUPREME, 20d));
            default -> new LinkedHashMap<>();
        };
    }

    public static MachineTier[] reversed(){
        return new MachineTier[]{LEGEND, MASTER, EXPERT, EXPERIENCED, BASIC};
    }

    public static MachineTier toTier(int experience){
        return Stream.of(MachineTier.reversed()).filter(entry -> entry.hasReachedTier(experience)).findFirst().orElse(MachineTier.BASIC);
    }
}
