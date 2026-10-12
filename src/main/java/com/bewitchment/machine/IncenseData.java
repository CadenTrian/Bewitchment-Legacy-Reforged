package com.bewitchment.machine;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.*;
import java.util.*;

/** Loaded-world seconds, including the legacy final second (expiry was time > 600). */
public final class IncenseData {
    public static final int BURN_SECONDS=601;
    public enum Kind {
        VITALITY, DEFTNESS, INVIGORATING, CATS_EYE, FULLNESS, INTENSITY, CONCENTRATION;
        public final String id="bewitchment:"+name().toLowerCase(Locale.ROOT);
        public static Kind fromId(String id){for(Kind kind:values())if(kind.id.equals(id))return kind;return null;}
        public List<Effect> effects(){
            switch(this){
                case VITALITY:return Arrays.asList(Effects.DAMAGE_RESISTANCE,Effects.DAMAGE_BOOST);
                case DEFTNESS:return Arrays.asList(Effects.JUMP,Effects.MOVEMENT_SPEED);
                case INVIGORATING:return Arrays.asList(Effects.DIG_SPEED,Effects.ABSORPTION);
                case CATS_EYE:return Collections.singletonList(Effects.NIGHT_VISION);
                case FULLNESS:return Arrays.asList(Effects.ABSORPTION,Effects.SATURATION);
                default:return Collections.emptyList();
            }
        }
    }
    private Kind kind;
    private int remaining;
    public Kind kind(){return kind;}
    public int remaining(){return remaining;}
    public void start(Kind value){kind=value;remaining=value==null?0:BURN_SECONDS;}
    public void clear(){kind=null;remaining=0;}
    public boolean second(){if(kind==null)return false;if(--remaining<=0){clear();return false;}return true;}
    public CompoundNBT save(){CompoundNBT tag=new CompoundNBT();if(kind!=null){tag.putString("kind",kind.id);tag.putInt("remaining",remaining);}return tag;}
    public void load(CompoundNBT tag){clear();Kind value=Kind.fromId(tag.getString("kind"));int seconds=tag.getInt("remaining");if(value!=null && tag.contains("remaining",3) && seconds>0 && seconds<=BURN_SECONDS){kind=value;remaining=seconds;}}
    /** Booster precedence is deterministic; duplicate effect sources cannot stack amplifiers. */
    public static List<EffectInstance> wakingEffects(Collection<Kind> sources){
        int intensity=0;boolean concentration=false;Set<Effect> effects=new LinkedHashSet<>();
        for(Kind source:sources){if(source==null)continue;if(source==Kind.INTENSITY)intensity=Math.min(2,intensity+1);else if(source==Kind.CONCENTRATION)concentration=true;effects.addAll(source.effects());}
        int duration=intensity==0 && concentration?24000:12000;
        List<EffectInstance> result=new ArrayList<>();for(Effect effect:effects)result.add(effect==Effects.SATURATION?new EffectInstance(effect,60,1):new EffectInstance(effect,duration,intensity));return result;
    }
}
