package com.bewitchment.entity;
import com.bewitchment.client.LegacyLizardModel;
import com.bewitchment.client.LegacyToadModel;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.util.registry.Bootstrap;
public final class CreatureCheck {
 private static int checks;
 private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
 public static void main(String[] args){
  Bootstrap.bootStrap();
  for(int i=0;i<4;i++)check(CreatureRules.skin(i)==i,"Valid skin retained");
  for(int i:new int[]{-1,4,255,Integer.MIN_VALUE,Integer.MAX_VALUE})check(CreatureRules.skin(i)==0,"Malformed skin falls back");
  check(CreatureRules.lizardHeals(0) && CreatureRules.lizardHeals(5),"Legacy six-tick healing burst");
  check(!CreatureRules.lizardHeals(6) && !CreatureRules.lizardHeals(199),"No healing outside burst");
  check(CreatureRules.lizardHeals(200) && !CreatureRules.lizardHeals(-1),"Burst repeats");
  int ticks=0;for(int i=0;i<400;i++)if(CreatureRules.lizardHeals(i))ticks++;
  check(ticks==12,"Two cycles contain twelve healing opportunities");
  check(CreatureRules.toadHeals(20,true) && CreatureRules.toadHeals(40,true),"Wet healing each second");
  check(!CreatureRules.toadHeals(21,true) && !CreatureRules.toadHeals(20,false),"Dry/intervening ticks do not heal");
  check(!CreatureRules.toadHeals(-20,true),"Negative ticks cannot heal");
  check(CreatureRules.health(false)==8 && CreatureRules.health(true)==20,"Wild/tame health");
  check(CreatureRules.reducedDamage(5,true)==3 && CreatureRules.reducedDamage(5,false)==5,"Legacy damage policy");
  check(CreatureRules.tameRoll(0),"One-in-five success");
  for(int i=1;i<5;i++)check(!CreatureRules.tameRoll(i),"Other rolls fail");
  check(CreatureRules.hop(1,1,true)==0,"Sitting prevents visual hop");
  check(CreatureRules.hop(1,0,false)==0 && CreatureRules.hop(1,-1,false)==0,"Stationary hop zero");
  boolean bounded=true;for(int i=-1000;i<1000;i++){float hop=CreatureRules.hop(i*.1F,4,false);bounded&=hop>=0 && hop<=1;}
  check(bounded,"Visual hop bounded");
  LegacyLizardModel model=new LegacyLizardModel();
  model.setupAnim(null,1,.7F,0,0,0);
  check(model.leftArm00.yRot==-model.rightArm00.yRot && model.leftLeg01.yRot==-model.rightLeg01.yRot,"Opposing limbs animate");
  model.setupAnim(null,10,0,0,0,0);
  check(model.leftArm00.yRot==0 && model.tail01.yRot==0,"Stationary model resets pose");
  LegacyToadModel toadModel=new LegacyToadModel();
  check(toadModel.body!=null && toadModel.leftfoot!=null && toadModel.rightfoot!=null,"Original geometry constructs");
  for(int i=0;i<6;i++)check(SnakeRules.skin(i)==i,"Six snake skins retained");
  for(int i:new int[]{-1,6,255,Integer.MIN_VALUE,Integer.MAX_VALUE})check(SnakeRules.skin(i)==0,"Malformed snake skin safe");
  check(SnakeRules.cooldown(-1)==0 && SnakeRules.cooldown(Integer.MIN_VALUE)==0,"Negative venom timer repaired");
  check(SnakeRules.cooldown(3601)==3600 && SnakeRules.cooldown(Integer.MAX_VALUE)==3600,"Excessive venom timer bounded");
  check(SnakeRules.cooldown(42)==42 && SnakeRules.cooldown(0)==0,"Valid timer preserved");
  for(boolean baby:new boolean[]{false,true})for(int timer:new int[]{0,1,3600})for(boolean roll:new boolean[]{false,true}){
   check(SnakeRules.canMilk(baby,timer,roll)==(!baby && timer==0 && roll),"Adult-only ready successful milking");
   check(SnakeRules.milkAngers(timer,roll)==(timer>0 || !roll),"Failed/cooldown collection angers snake");
  }
  com.bewitchment.client.LegacySnakeModel snakeModel=new com.bewitchment.client.LegacySnakeModel();
  snakeModel.pose(12,20,10,.6F,true,true);
  check(snakeModel.head.yRot<0 && snakeModel.neck01.xRot<0,"Coiled sitting pose");
  check(snakeModel.tongue.z<-2 && snakeModel.body.z>-3.4F,"Hissing offsets restored");
  snakeModel.pose(0,0,0,0,false,false);
  check(snakeModel.head.xRot==0 && snakeModel.head.yRot==0 && snakeModel.head.zRot==0,"Standing resets seated head");
  check(snakeModel.neck01.xRot==0 && snakeModel.body.yRot==0,"Standing resets coil");
  check(snakeModel.tongue.z==-2 && snakeModel.body.z==-3.4F,"Hiss offsets reset between entities");
  check(SnakeRules.sway(-100)==-1 && SnakeRules.sway(100)==1,"Extreme knockback cannot overtwist model");
  check(SnakeRules.sway(0)==0 && SnakeRules.sway(.25F)==.25F,"Normal sway preserved");
  System.out.println("PASS: "+checks+" creature variant, healing, taming and model checks.");
 }
}
