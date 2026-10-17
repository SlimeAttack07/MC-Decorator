package slimeattack07.decorator.util;

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

    public static MachineTier[] reversed(){
        return new MachineTier[]{LEGEND, MASTER, EXPERT, EXPERIENCED, BASIC};
    }
}
