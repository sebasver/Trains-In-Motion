package ebf.tim.models;

import ebf.tim.entities.GenericRailTransport;
import ebf.tim.utility.RailUtility;
import fexcraft.tmt.slim.ModelRendererTurbo;
import fexcraft.tmt.slim.Tessellator;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import java.util.*;

import static fexcraft.tmt.slim.ModelRendererTurbo.MR_TOP;
import static fexcraft.tmt.slim.Tessellator.b;
import static org.lwjgl.opengl.GL11.*;

/**
 * <h1>Particle effect</h1>
 * custom particle, fully separate from the vanilla stuff, renders in 3d and still runs significantly better than vanilla.
 * TODO: add support for 2d particles.
 * @author Eternal Blue Flame
 */
public class ParticleFX {
    /*the number of ticks this particle will survive till it needs to reset position.*/
    private int lifespan =-1;
    /*returns if the particle should render or not*/
    public boolean shouldRender = false;
    /*the color to render the particle as*/
    private final int color;
    /*the ticks the particle has existed, float is used so render can divide it into decimals*/
    private Float ticksExisted=null, scale=null;
    /*the offset to tint the particle color*/
    private int colorTint;
    /*the bounding box of the particle to use for rendering and collision, if it's null we render it as a static particle*/
    private final AxisAlignedBB boundingBox;
    /*the motion of the particle*/
    private double motionX=0, motionY=0, motionZ=0;
    /*a random to use for variable generating*/
    private static final Random rand = new Random();
    /*the list of objects in the hitbox*/
    private List list = new ArrayList<>();
    /*the cached motion of the particle*/
    private double oldX, oldY, oldZ;
    /*the host entity*/
    private GenericRailTransport host;
    /*the position offset to move based on the transport's rotation*/
    private double[] offset, pos;
    private int particleID=0, colorTemp;

    /**
     * Initialize the particle, basically for spawning it
     * @param color the color of the particle.
     */
    public ParticleFX(GenericRailTransport transport, int color, float offsetX, float offsetY, float offsetZ, float rotationX, float rotationY, float rotationZ, int id) {
        host = transport;
        particleID=id;
        this.offset = new double[]{offsetX, id==4?transport.posY:offsetY, offsetZ};
        pos = RailUtility.rotatePoint(new double[]{offset[0]*0.0625,offset[1]*-0.0625,offset[2]*0.0625}, transport.rotationPitch, transport.rotationYaw, 0);
        pos= new double[]{pos[0],pos[1],pos[2],rotationX,rotationY,rotationZ};
        this.color = color;

        switch (particleID) {
            case 0:case 1:{//smoke, steam
                motionX = (rand.nextInt(40) - 20) * 0.001f;
                motionY = particleID==0?0.15:0.0005;
                motionZ = (rand.nextInt(40) - 20) * 0.001f;

                this.boundingBox = AxisAlignedBB.getBoundingBox(pos[0] + transport.posX - 0.1, pos[1] + transport.posY - 0.1, pos[2] + transport.posZ - 0.1, pos[0] + transport.posX + 0.1, pos[1] + transport.posY + 0.1, pos[2] + transport.posZ + 0.1);
                break;
            }case 4:{//sparks
                motionX = (rand.nextInt(40) - 20) * 0.0005f;
                motionY = 0.001;
                motionZ = (rand.nextInt(40) - 20) * 0.0005f;


                this.boundingBox = AxisAlignedBB.getBoundingBox(pos[0] + transport.posX - 0.05, pos[1] + transport.posY - 0.05, pos[2] + transport.posZ - 0.05, pos[0] + transport.posX + 0.05, pos[1] + transport.posY + 0.05, pos[2] + transport.posZ + 0.05);
                break;
            }
            case 2:case 3: default:{
                motionX = 1;
                this.boundingBox = null;
                break;
            }
        }
    }

    public ParticleFX(GenericRailTransport transport, int color, float offsetX, float offsetY, float offsetZ, float rotationX, float rotationY, float rotationZ, int id, float scale){
        this(transport, color, offsetX, offsetY, offsetZ, rotationX, rotationY, rotationZ, id);
        this.scale=scale;
    }

    public static int getParticleIDFronName(String name){
        if(name.toLowerCase().contains("smoke")){
            return 0;
        } else if(name.toLowerCase().contains("steam")){
            return 1;
        } else if (name.toLowerCase().contains("lamp")){
            if(name.toLowerCase().contains("cone")){
                return 2;
            }else if(name.toLowerCase().contains("sphere")) {
                return 3;
            }
        }  else if (name.toLowerCase().contains(StaticModelAnimator.tagWheel)){
            return 4;
        }

        return -1;//invalid part
    }

    public static List<ParticleFX> newParticleItterator(String strength, int color, float offsetX, float offsetY, float offsetZ, float rotationX, float rotationY, float rotationZ, GenericRailTransport host, String partname){
        List<ParticleFX> list = new ArrayList<>();
        int id= getParticleIDFronName(partname);
        if(id==0 || id==1) {
            for (int i = 0; i < Integer.parseInt(strength)*20; i++) {
                list.add(new ParticleFX(host, color, offsetX, offsetY, offsetZ, rotationX, rotationY, rotationZ, id));
            }
        } else {
            list.add(new ParticleFX(host, color, offsetX, offsetY, offsetZ, rotationX, rotationY, rotationZ, id, Float.parseFloat(strength)));
        }
        return list;
    }

    public static void updateParticleItterator(List<ParticleFX> particles, boolean hostIsRunning){
        int index=0;
        for (ParticleFX p : particles){
            p.onUpdate(hostIsRunning, index*(150f/particles.size()));
            index++;
        }
    }

    public static String[] parseData(String s){
        if (s.contains("smoke")) {
            return s.substring(s.indexOf("smoke ")+6).split(" ");
        } else if (s.contains("lamp")){
            String[] lamp = s.substring(s.indexOf("lamp ")+5).split(" ");
            return new String[]{lamp[1], lamp[2]};
        } else if (s.contains(StaticModelAnimator.tagWheel)){
            return new String[]{"4", "CCCC00"};
        } else {
            return s.substring(s.indexOf("steam ")+6).split(" ");
        }
    }

    /**
     * <h2>movement calculations</h2>
     * call this from the host's onUpdate to update the position of the particle.
     */
    public void onUpdate(boolean hostIsRunning, float count){

        if(ticksExisted==null){
            ticksExisted = -count;
        } else if (ticksExisted<=1){
            ticksExisted++;
            return;
        }


        //update lamps
        if (particleID==3 || particleID==2){
            shouldRender=host.getBoolean(GenericRailTransport.boolValues.LAMP);
            oldX=pos[3];
            oldY=pos[4];
            oldZ=pos[5];
            pos = RailUtility.rotatePoint(new double[]{offset[0]*0.0625,offset[1]*-0.0625,offset[2]*0.0625}, host.rotationPitch, host.rotationYaw, 0);
            pos= new double[]{pos[0],pos[1],pos[2],oldX,oldY,oldZ};
            //in this case color is used as an ID of sorts.
            colorTint=Integer.parseInt(host.renderData.particleRecolors[color],16);
            return;
        } else if(particleID==4 && this.ticksExisted > this.lifespan){
            if(host.vectorCache[7][1]>0.005){
                colorTint = (rand.nextInt(75) - 30);
                lifespan = rand.nextInt(80) +140;
                ticksExisted =0f;
                //recalculating it throws away the rotation value, but that's only used for the cone lamp, which doesn't even run this, so we don't need it anyway.
                pos = RailUtility.rotatePoint(new double[]{offset[0]*0.0625,offset[1]*-0.0625,offset[2]*0.0625}, host.rotationPitch, host.rotationYaw, 0);
                this.boundingBox.setBounds(host.posX+pos[0] -0.05, host.posY+pos[1] -0.05, host.posZ+pos[2] -0.05, host.posX+pos[0] +0.05,  host.posY+pos[1] +0.05, host.posZ+pos[2] +0.05);
                motionX = (rand.nextInt(40) - 20) * 0.001f;
                motionY = rand.nextInt(15)*-0.003;
                motionZ = (rand.nextInt(40) - 20) * 0.001f;
                shouldRender = true;
            }

        }else if (hostIsRunning && this.ticksExisted > this.lifespan) {
            //if the lifespan is out we reset the information, as if we just spawned a new particle.
            colorTint = (rand.nextInt(75) - 30);
            colorTemp=Integer.parseInt(host.renderData.particleRecolors[color],16);
            lifespan = rand.nextInt(80) +140;
            ticksExisted =0f;
            //recalculating it throws away the rotation value, but that's only used for the cone lamp, which doesn't even run this, so we don't need it anyway.
            pos = RailUtility.rotatePoint(new double[]{offset[0]*0.0625,offset[1]*-0.0625,offset[2]*0.0625}, host.rotationPitch, host.rotationYaw, 0);
            this.boundingBox.setBounds(host.posX+pos[0] -0.1, host.posY+pos[1] -0.1, host.posZ+pos[2] -0.1, host.posX+pos[0] +0.1,  host.posY+pos[1] +0.1, host.posZ+pos[2] +0.1);
            motionX = (rand.nextInt(40) - 20) * 0.001f;
            if(particleID==0) {
                motionY = rand.nextInt(15)*0.003;
            } else if (particleID==1){
                motionY = rand.nextInt(15)*0.00005;
            }
            motionZ = (rand.nextInt(40) - 20) * 0.001f;
            shouldRender = true;
        } else if (this.ticksExisted > this.lifespan) {
            //if the transport isn't running and this has finished it's movement, set it' position to the transport and set that it shouldn't render.
            this.boundingBox.setBounds(host.posX, host.posY, host.posZ, host.posX,  host.posY, host.posZ);
            shouldRender = false;
            return;
        }


        if(particleID==4){

            boundingBox.offset(motionX,motionY,motionZ);
            ticksExisted++;

            return;
        }

        //set the old motion values so we can compare them later.
        oldX = motionX;
        oldY = motionY;
        oldZ = motionZ;
        //instance a bounding box variable now so we won't have to cast as much later.
        AxisAlignedBB box;

        //todo: should be able to just check movement and replicate bounding box functions without bounding box, use pos for the temp
        list = host.worldObj.getCollidingBoundingBoxes(host, this.boundingBox.addCoord(motionX, motionY, motionZ));
        //iterate the list and check for collisions
        for (Object obj : list) {
            box = ((AxisAlignedBB) obj);
            if (motionY <0.001 || motionY >-0.001) {
                motionY = box.calculateYOffset(this.boundingBox, motionY);
            }
            if (motionX <0.0001 || motionX >-0.0001) {
                motionX = box.calculateXOffset(this.boundingBox, motionX);
            }
            if (motionZ <0.0001 || motionZ >-0.0001) {
                motionZ = box.calculateZOffset(this.boundingBox, motionZ);
            }
        }

        //check for collisions on the Y vector and apply movement accordingly, also always keep it attempting to float up.
        if (motionY <0.001 || motionY >-0.001) {
            this.boundingBox.offset(0.0D, motionY, 0.0D);

            if (oldY != motionY) {
                motionZ *=1.5d; motionZ +=rand.nextBoolean()?oldY:rand.nextBoolean()?0:-oldY;
                motionX *=1.5d; motionX +=rand.nextBoolean()?oldY:rand.nextBoolean()?0:-oldY;
                motionY = oldY * -0.4d;
            }
            if (motionY<0.005){
                motionY += 0.00075;
            }
        }else {
            motionY += 0.00005;
        }

        //check for collisions on the x axis.
        if (motionX <0.0001 || motionX >-0.0001) {
            this.boundingBox.offset(motionX, 0.0D, 0.0D);
            if (oldX != motionX) {
                motionX = this.motionX*0.75d;
            }
            motionX *=0.975;
        }

        //check for collisions on the Z axis.
        if (motionZ <0.0001 || motionZ >-0.0001) {
            this.boundingBox.offset(0.0D, 0.0D, motionZ);
            if (oldZ != motionZ) {
                motionZ = this.motionZ*0.75d;
            }
            motionZ *=0.975;
        }

        ticksExisted++;
    }

    /**
     * <h2>Render particle</h2>
     * actually renders the particle, unless it's on tick 0, we skip rendering that tick since it won't have a motion yet.
     * @param entity the particle variable to render
     * @param x the x position of the renderer
     * @param y the y position of the renderer
     * @param z the z position of the renderer
     */
    public static void doRender(ParticleFX entity, double x, double y, double z) {
        if(!entity.shouldRender || entity.ticksExisted==null || entity.ticksExisted<1){
            return;
        }

        GL11.glPushMatrix();
        //DebugUtil.println(entity.host.rotationYaw);

        if (entity.particleID==2) {//cone lamps
            GL11.glTranslated(x+entity.pos[0] , y+entity.pos[1]+0.3, z+entity.pos[2]);
            GL11.glRotated(90+entity.pos[4]+entity.host.rotationPitch,1,0,0);
            GL11.glRotated(entity.pos[5],0,1,0);
            GL11.glRotated(270-entity.pos[3]+entity.host.rotationYaw,0,0,1);
            GL11.glScalef(5.5f,5.5f,5.5f);
            GL11.glDisable(GL11.GL_LIGHTING);
            Minecraft.getMinecraft().entityRenderer.disableLightmap(1D);
            glAlphaFunc(GL_LEQUAL, 1f);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            GL11.glDepthMask(false);
            GL11.glDisable(GL_CULL_FACE);
            drawLightTexture(entity, true);
            for (int i=0; i<5; i++) {
                GL11.glScalef(1-(i*0.04f),1-(i*0.01f),1-(i*0.04f));
                lampCone.render(0.625f);
            }
            GL11.glEnable(GL_CULL_FACE);
            GL11.glEnable(GL11.GL_LIGHTING);
            glAlphaFunc(GL_GREATER, 0.1f);
            Minecraft.getMinecraft().entityRenderer.enableLightmap(1D);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDepthMask(true);
        } else if (entity.particleID==3) {//sphere lamps
            GL11.glTranslated(x+entity.pos[0] , y+entity.pos[1]+0.3, z+entity.pos[2]);
            GL11.glRotated(270-entity.pos[3]+entity.host.rotationYaw,0,0,1);
            GL11.glScalef(entity.scale,entity.scale,entity.scale);
            GL11.glDisable(GL11.GL_LIGHTING);
            Minecraft.getMinecraft().entityRenderer.disableLightmap(1D);
            glAlphaFunc(GL_LEQUAL, 1f);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
            GL11.glDepthMask(false);
            GL11.glDisable(GL_CULL_FACE);
            drawLightTexture(entity, true);
            for (int i=0; i<5; i++) {
                GL11.glScalef(1-(i*0.04f),1-(i*0.01f),1-(i*0.04f));
                lampSphere.render(0.625f);
            }
            GL11.glEnable(GL_CULL_FACE);
            GL11.glEnable(GL11.GL_LIGHTING);
            glAlphaFunc(GL_GREATER, 0.1f);
            Minecraft.getMinecraft().entityRenderer.enableLightmap(1D);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glDepthMask(true);

        } else {


            GL11.glDisable(GL11.GL_TEXTURE_2D);
            //set the color with the tint.   * 0.00392156863 is the same as /255, but multiplication is more efficient than division.
            GL11.glColor4f(((entity.colorTemp >> 16 & 0xFF)-entity.colorTint)* 0.00392156863f,
                    ((entity.colorTemp >> 8 & 0xFF)-entity.colorTint)* 0.00392156863f,
                    ((entity.colorTemp & 0xFF)-entity.colorTint)* 0.00392156863f,
                    1f-(entity.ticksExisted/entity.lifespan));
            //set the position
            GL11.glTranslated( x + entity.boundingBox.minX - entity.host.posX, y+ entity.boundingBox.minY-entity.host.posY, z+ entity.boundingBox.minZ - entity.host.posZ);
            if(entity.particleID==4){
                GL11.glScalef(0.5f,0.5f,0.5f);
            }
            particle.render(0.0625f);

            //before we end this be sure to re-enabling texturing for other things.
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        GL11.glPopMatrix();
    }


    public static void drawLightTexture(ParticleFX entity, boolean isCone){
        int pos=0;
        for(int i=0; i<8192; i+=4) {
            if(!isCone || getY(pos)>7) {
                Tessellator.renderPixels.put(i, b(entity.colorTint >> 16 & 0xFF));
                Tessellator.renderPixels.put(i + 1, b(entity.colorTint >> 8 & 0xFF));
                Tessellator.renderPixels.put(i + 2, b(entity.colorTint & 0xFF));
                Tessellator.renderPixels.put(i + 3, b(getY(pos)-7));
            } else {
                Tessellator.renderPixels.put(i+3,b(0));
            }

            pos++;
        }

        glTexSubImage2D (GL_TEXTURE_2D, 0, 0, 0, 32, 64, GL_RGBA, GL_UNSIGNED_BYTE, Tessellator.renderPixels);
        Tessellator.renderPixels.clear();//reset the buffer to all 0's.
    }

    public static int getY(int pos){
        int y=0;
        while ((y+1)*32<pos) {
            y++;
        }
        return y;
    }


    public static ModelRendererTurbo particle = new ModelRendererTurbo(null, 0, 0, 16, 16)
            .addBox(0,0,0, 4, 4, 4).setRotationPoint(-2F, 2F, -1F);
    public static ModelRendererTurbo lampCone = new ModelRendererTurbo(null, 0, 0, 32, 64)
            .addCylinder(0, -4, 0, 1, 4, 16, 1F, 0.01F, MR_TOP, 1,1, 6);
    public static ModelRendererTurbo lampSphere = new ModelRendererTurbo(null, 0, 0, 64, 64)
            .addSphere(0,0,0, 2, 8, 8,1,1);

}
