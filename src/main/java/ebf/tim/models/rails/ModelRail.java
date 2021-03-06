package ebf.tim.models.rails;

import fexcraft.tmt.slim.Tessellator;
import fexcraft.tmt.slim.Vec3f;
import net.minecraft.block.Block;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static ebf.tim.models.rails.Model1x1Rail.addVertexWithOffset;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

public class ModelRail {

    public static void model3DRail(List<float[]> points, float[] railOffsets){

        GL11.glPushMatrix();
        GL11.glDisable(GL_TEXTURE_2D);
        GL11.glTranslated(0, 0.225, 0);
        GL11.glColor4f(
                ((0xCBCDCD >> 16 & 0xFF))* 0.00392156863f,
                ((0xCBCDCD >> 8 & 0xFF))* 0.00392156863f,
                ((0xCBCDCD & 0xFF))* 0.00392156863f, 1);

        for(float rail : railOffsets) {
            Tessellator.getInstance().startDrawing(GL11.GL_QUAD_STRIP);
            for (float[] p : points) {
                addVertexWithOffset(p, 0.0625f+rail, 0, 0);
                addVertexWithOffset(p, -0.0625f+rail, 0, 0);
            }
            Tessellator.getInstance().arrayEnabledDraw();

            Tessellator.getInstance().startDrawing(GL11.GL_QUAD_STRIP);
            for (float[] p : points) {
                addVertexWithOffset(p, 0.0625f+rail, -0.0625f, 0);
                addVertexWithOffset(p, 0.0625f+rail, 0, 0);
            }
            Tessellator.getInstance().arrayEnabledDraw();

            Tessellator.getInstance().startDrawing(GL11.GL_QUAD_STRIP);
            for (float[] p : points) {
                addVertexWithOffset(p, -0.0625f+rail, 0, 0);
                addVertexWithOffset(p, -0.0625f+rail, -0.0625f, 0);
            }
            Tessellator.getInstance().arrayEnabledDraw();


            Tessellator.getInstance().startDrawing(GL11.GL_QUAD_STRIP);

            addVertexWithOffset(points.get(0), -0.0625f+rail, -0.0625f, 0);
            addVertexWithOffset(points.get(0), -0.0625f+rail, 0, 0);
            addVertexWithOffset(points.get(0), 0.0625f+rail, -0.0625f, 0);
            addVertexWithOffset(points.get(0), 0.0625f+rail, 0, 0);

            Tessellator.getInstance().arrayEnabledDraw();

            Tessellator.getInstance().startDrawing(GL11.GL_QUAD_STRIP);

            addVertexWithOffset(points.get(points.size()-1), 0.0625f+rail, -0.0625f, 0);
            addVertexWithOffset(points.get(points.size()-1), 0.0625f+rail, 0, 0);
            addVertexWithOffset(points.get(points.size()-1), -0.0625f+rail, -0.0625f, 0);
            addVertexWithOffset(points.get(points.size()-1), -0.0625f+rail, 0, 0);

            Tessellator.getInstance().arrayEnabledDraw();
        }
        GL11.glEnable(GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }
}
