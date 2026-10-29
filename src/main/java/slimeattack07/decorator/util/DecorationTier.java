package slimeattack07.decorator.util;

import net.minecraft.ChatFormatting;

import java.util.stream.Stream;

public enum DecorationTier {
    MODEST,
    GRAND,
    SUPERB,
    MAJESTIC,
    SUPREME;

    public ChatFormatting getColor(){
        return switch(this){
            case MODEST -> ChatFormatting.WHITE;
            case GRAND -> ChatFormatting.GREEN;
            case SUPERB -> ChatFormatting.AQUA;
            case MAJESTIC -> ChatFormatting.DARK_PURPLE;
            case SUPREME -> ChatFormatting.GOLD;
        };
    }

    public static DecorationTier parse(String tier){
        return Stream.of(values()).filter(entry -> entry.name().equalsIgnoreCase(tier)).findAny().orElse(null);
    }
}
