package com.bewitchment.potion;

public final class RubedoWork extends SampledTerrainWork {
    public static final String ID="bewitchment_rubedo_work";
    public RubedoWork(){super(ID);}
    public static int radius(int amplifier){return 2*(Math.max(0,Math.min(2,amplifier))+1);}
    @Override protected int radiusForAmplifier(int amplifier){return radius(amplifier);}
    @Override protected boolean validRadius(int radius){return radius==2||radius==4||radius==6;}
}
