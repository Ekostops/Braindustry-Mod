const electroContinuousLaserBullet = extend(ContinuousLaserBulletType, {
update(b){
if(b.fin()<0.001){
for(var i = 0; i < this.lightning; i++){
Lightning.create(b, this.lightningColor, this.lightningDamage < 0 ? this.damage : this.lightningDamage, b.x, b.y + 2, b.rotation() + Mathf.range(this.lightningCone) + this.lightningAngle, this.lightningLength + Mathf.random(this.lightningLengthRand));
}
}
this.super$update(b);
}
});
//electroContinuousLaserBullet.colors = [Color.valueOf("c2cc37"), Color.valueOf("f1fc58"), Color.valueOf("fbffcc")];
electroContinuousLaserBullet.hitSize = 14;
electroContinuousLaserBullet.drawSize = 520;
electroContinuousLaserBullet.width = 30;
electroContinuousLaserBullet.length = 390;
electroContinuousLaserBullet.largeHit = true;
electroContinuousLaserBullet.lifetime = 140;
electroContinuousLaserBullet.hitColor = Color.valueOf("f1fc58");
electroContinuousLaserBullet.incendAmount = 4;
electroContinuousLaserBullet.incendSpread = 10;
electroContinuousLaserBullet.incendChance = 0.7;
electroContinuousLaserBullet.lightColor = Color.valueOf("fbffcc");
electroContinuousLaserBullet.keepVelocity = true;
electroContinuousLaserBullet.collides = true;
electroContinuousLaserBullet.pierce = true;
electroContinuousLaserBullet.hittable = true;
electroContinuousLaserBullet.absorbable = false;
electroContinuousLaserBullet.damage = 74;
electroContinuousLaserBullet.shootEffect = Fx.railShoot;
electroContinuousLaserBullet.despawnEffect = Fx.railHit;
electroContinuousLaserBullet.knockback = 1;
electroContinuousLaserBullet.lightning = 24;//количество молний
electroContinuousLaserBullet.lightningLength = 30;//длина молнии
electroContinuousLaserBullet.lightningLengthRand = 30;//рандомное число от 0 до 50 будет прибавляться к длине молнии, то есть рандомизация длины
electroContinuousLaserBullet.lightningDamage = 78;//урон молнии
electroContinuousLaserBullet.lightningAngle = 15;//угол направления молний относительно угла пули
electroContinuousLaserBullet.lightningCone = 50;//рандомизация угла направления молний 
electroContinuousLaserBullet.lightningColor = Color.valueOf("f1fc58");

const electroMeltdown = extendContent(LaserTurret, "electron", {});
electroMeltdown.health = 6460;
electroMeltdown.size = 10;
electroMeltdown.recoilAmount = 8;
electroMeltdown.shootShake = 4;
electroMeltdown.shootCone = 40;
electroMeltdown.rotateSpeed = 0.9;
electroMeltdown.shots = 1;
electroMeltdown.shootSound = loadSound("electronShoot.ogg");
electroMeltdown.loopSound = loadSound("electronCharge.ogg");
electroMeltdown.loopSoundVolume = 3;
electroMeltdown.reloadTime = 190;
electroMeltdown.hasItems = true;
electroMeltdown.hasLiquids = true;
//electroMeltdown.shootX = 10;
//electroMeltdown.shootY = -12;
//electroMeltdown.alternate = true;
electroMeltdown.rotate = true;
electroMeltdown.shootDuration = 220;
electroMeltdown.powerUse = 22;
electroMeltdown.range = 400;
electroMeltdown.firingMoveFract = 0.8;
electroMeltdown.reloadTime = 140;
electroMeltdown.localizedName = "Dendrite";
electroMeltdown.description = "Monstruous turret with Electric Laser.";
electroMeltdown.buildVisibility = BuildVisibility.shown;
electroMeltdown.requirements = ItemStack.with(Items.surgeAlloy, 890, Items.phaseFabric, 640, Vars.content.getByName(ContentType.item,"braindustry-graphenite"), 1400, Items.silicon, 2010, Vars.content.getByName(ContentType.item, "braindustry-exotic-alloy"), 1050);
electroMeltdown.category = Category.turret;
electroMeltdown.shootType = electroContinuousLaserBullet;