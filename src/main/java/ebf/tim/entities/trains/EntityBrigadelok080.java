package ebf.tim.entities.trains;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ebf.tim.TrainsInMotion;
import ebf.tim.entities.EntityTrainCore;
import ebf.tim.items.ItemTransport;
import ebf.tim.models.tmt.Vec3d;
import ebf.tim.registry.TransportRegistry;
import ebf.tim.registry.URIRegistry;
import ebf.tim.utility.FuelHandler;
import ebf.tim.utility.LiquidManager;
import net.minecraft.client.audio.ISound;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * <h1>Brigadelok 0-8-0 entity</h1>
 * This class is intended to serve as the primary example for API use.
 * @author Eternal Blue Flame
 */
public class EntityBrigadelok080 extends EntityTrainCore {
    /**
     * <h2>Basic Train Constructor</h2>
     * To make your own custom train or rollingstock, create a new class that is a copy of the train or rollingstock that is closest to what you are adding,
     *     in that copy, you will need to go through the variables and overrides and change them to match the class/transport.
     * lastly you have to register the class in
     * @see TransportRegistry#listTrains(int)
     *
     * The fluid tank has 2 values, one for water/RF/fuel/uranium and another for steam, the second one is ONLY used with steam and nuclear steam trains.
     *     The first part is how much fluid it can store, a bucket is worth 1000.
     *     The second part is the fluid filter, this is the list of fluids to either use specifically, or never use.
     *     The third part is the blacklist/whitelist. True means it will ONLY use the fluids defined in the array, false means it will use any fluids EXCEPT the ones in the array.
     *
     * SOUND_HORN and SOUND_RUNNING are refrences to the sound files for the train horn and running sounds.
     *
     * thisItem is the item for this train that will get registered.
     *     The String array defines the extra text added to the item description, each entry makes a new line
     *     The second variable is the class constructor, they are defined from top to bottom in order they are written, and the one used for this function must ALWAYS be the one that is like this
     *     @see #EntityBrigadelok080(UUID, World, double, double, double)
     *     The last part defines the unlocalized name, this is used for the item name, entity name, and language file entries.
     */
    private LiquidManager tank = new LiquidManager(1100,1100, new Fluid[]{FluidRegistry.WATER},new Fluid[]{FluidRegistry.WATER},true,true);
    private static final ResourceLocation horn = URIRegistry.SOUND_HORN.getResource("h080brigadelok.ogg");
    private static final ResourceLocation running = URIRegistry.SOUND_RUNNING.getResource("r080brigadelok.ogg");

    private static final String[] itemDescription = new String[]{
            "\u00A77" + StatCollector.translateToLocal("menu.item.era") +  ": " + StatCollector.translateToLocal("menu.steam"),
            "\u00A77" + StatCollector.translateToLocal("menu.item.year") +": 1918",
            "\u00A77" + StatCollector.translateToLocal("menu.item.country") + ": " + StatCollector.translateToLocal("menu.item.germany"),
            "\u00A77" + StatCollector.translateToLocal("menu.item.speed") +": 70.81km",
            "\u00A77" + StatCollector.translateToLocal("menu.item.pullingpower") +": ??? " + StatCollector.translateToLocal("menu.item.tons")};
    public static final Item thisItem = new ItemTransport(itemDescription, EntityBrigadelok080.class).setUnlocalizedName("brigadelok080");

    /**
     * these basic constructors only need to have their names changed to that of this class, that is assuming your editor doesn't automatically do that.
     * Be sure the one that takes more than a world is always first, unless you wanna compensate for that in the item declaration.
     * @see EntityTrainCore
     */
    public EntityBrigadelok080(UUID owner, World world, double xPos, double yPos, double zPos) {
        super(owner, world, xPos, yPos, zPos);
    }
    public EntityBrigadelok080(World world){
        super(world);
    }

    /**
     * <h1>Variable Overrides</h1>
     * We override the functions defined in the super here, to give them different values.
     * This is more efficient than having to store them in the super, or actual variables, because we won't have to store them in the NBT or RAM.
     */


    /**
     * <h2>Max speed</h2>
     * <h4>TRAINS ONLY.</h4>
     * @return the value of the max speed in blocks per second(km/h * 0.277778) scaled to the train size (1/7.68)
     * in this case the train speed is 25, so the calculation would be ((25*0.277778)* 0.130208333) = 0.9042252581
     */
    @Override
    public float getMaxSpeed(){return 0.9042252581f;}
    /**
     * <h2>Bogie Offset</h2>
     * @return the list of offsets for the bogies, 0 being the center. negative values are towards the front of the train.
     * Must always go from front to back. First and last values must always be exact opposites.
     * TODO: in alpha 3 this should be client only, for the bogie render only, and have a second function to get the actual offset which only needs 1 double, because the values have to be perfectly opposite.
     */
    @Override
    public List<Double> getRenderBogieOffsets(){return  Arrays.asList(-0.75, 0.75);}
    /**
     * <h2>Inventory Size</h2>
     * @return the size of the inventory not counting any fuel or crafting slots, those are defined by the type.
     */
    @Override
    public TrainsInMotion.inventorySizes getInventorySize(){return TrainsInMotion.inventorySizes.THREExTHREE;}
    /**
     * <h2>Type</h2>
     * @return the type which will define it's features, GUI, a degree of storage (like crafting slots), and a number of other things.
     */
    @Override
    public TrainsInMotion.transportTypes getType(){return TrainsInMotion.transportTypes.STEAM;}
    /**
     * <h2>Max Fuel</h2>
     * <h4>STEAM ONLY.</h4>
     * This is only used for steam locomotives because it defines the max burnable fuel, other locomotives only use the tanks.
     * @return the max burnable fuel the train can store, one ton is considered a stack.
     * In the case of coal which is worth 1600 would be 102400 (1600 * 64 = 102400)
     * @see FuelHandler for more information on fuel consumption
     */
    @Override
    public int getMaxFuel(){return 71680;}
    /**
     * <h2>Rider offset</h2>
     * @return defines the offsets of the riders in blocks, the first value is how far back, and the second is how high.
     *     Negative values are towards the front, ground, or right. In that order.
     *     Each set of floats represents a different rider.
     *     Only the first 3 values of each set of floats are actually used.
     */
    @Override
    public double[][] getRiderOffsets(){return new double[][]{{1.3f,1.1f, 0f}};}
    /**
     * <h2>Acceleration</h2>
     * <h4>TRAINS ONLY.</h4>
     * @return defines the acceleration that is applied to the train in blocks per second.
     * TODO: might not be a bad idea to replace this with a calculation based on metric horse power, pulled weight, the train's weight, and max speed
     */
    @Override
    public float getAcceleration(){return 0.4f;}
    /**
     * <h2>Hitbox offsets</h2>
     * @return defines the positions for the hitboxes in blocks. 0 being the center, negative values being towards the front.
     */
    @Override
    public float[] getHitboxPositions(){return new float[]{-1.15f,0f,1.15f};}
    /**
     * <h2>Lamp offset</h2>
     * @return defines the offset for the lamp in blocks.
     * negative X values are towards the front of the train.
     * a Y value of less than 2 will register the lamp as null (this is to allow for null lamps, and to prevent from breaking the rails the train is on).
     *     NOTE: values should never be higher than the hitboxes, or it may not work. (it is managed through a block after all)
     */
    @Override
    public Vec3d getLampOffset(){return new Vec3d(-3.5,2,0);}
    /**
     * <h2>Animation radius</h2>
     * @return defines the radius in microblocks (1/16 of a block) for the piston rotations.
     */
    @Override
    public float getPistonOffset(){return 0.5f;}
    /**
     * <h2>Smoke offset</h2>
     * @return defines the array of positions in blocks for smoke.
     * the first number in the position defines the X/Z axis, negative values are towards the front of the train.
     * the second number defines the Y position, 0 is the rails, higher values are towards the sky.
     * the third number defines left to right, negative values are towards the right.
     * the forth number defines the grayscale color from 255 (white) to 0 (black)
     * the 5th number is for density, there's no min/max but larger numbers will create more lag.
     */
    @Override
    public float[][] getSmokeOffset(){return new float[][]{{-1.40f,2,0,0.2f,12},{-1,0,0.5f,0.9f,1},{-1,0,-0.5f,0.9f,1}};}

    /**
     * <h2>rider sit or stand</h2>
     * @return true if the rider(s) should be sitting, false if the rider should be standing.
     */
    @Override
    public boolean shouldRiderSit(){
        return false;
    }


    /**
     * <h2>pre-assigned values</h2>
     * These return values are defined from the top of the class.
     * These should only need modification for advanced users, and even that's a stretch.
     */
    @Override
    public LiquidManager getTank(){return tank;}
    @Override
    public Item getItem(){
        return thisItem;
    }

    /**
     * <h2>Train only pre-assigned values</h2>
     */
    @SideOnly(Side.CLIENT)
    @Override
    public ISound getHorn(){
        return new ISound() {
            @Override
            public ResourceLocation getPositionedSoundLocation() {
                return horn;
            }

            @Override
            public boolean canRepeat() {
                return false;
            }

            @Override
            public int getRepeatDelay() {
                return 0;
            }

            @Override
            public float getVolume() {
                return 100;
            }

            @Override
            public float getPitch() {
                return 0;
            }

            @Override
            public float getXPosF() {
                return (float) posX;
            }

            @Override
            public float getYPosF() {
                return (float) posY;
            }

            @Override
            public float getZPosF() {
                return (float) posZ;
            }

            @Override
            public AttenuationType getAttenuationType() {
                return null;
            }
        };
    }
    @SideOnly(Side.CLIENT)
    @Override
    public ISound getRunning(){
        return new ISound() {
            @Override
            public ResourceLocation getPositionedSoundLocation() {
                return running;
            }

            @Override
            public boolean canRepeat() {
                return true;
            }

            @Override
            public int getRepeatDelay() {
                return 0;
            }

            @Override
            public float getVolume() {
                return 100;
            }

            @Override
            public float getPitch() {
                return 0;
            }

            @Override
            public float getXPosF() {
                return (float) posX;
            }

            @Override
            public float getYPosF() {
                return (float) posY;
            }

            @Override
            public float getZPosF() {
                return (float) posZ;
            }

            @Override
            public AttenuationType getAttenuationType() {
                return null;
            }
        };
    }
}