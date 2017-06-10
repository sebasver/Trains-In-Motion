package ebf.tim.models.tmt;

import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

/**
 * An extension of TexturedQuad that adds support for more shapes beyond simple quads.
 */
public class TexturedPolygon extends TexturedQuad {
	public TexturedPolygon(PositionTextureVertex apositionTexturevertex[]) {
		super(apositionTexturevertex);
    }
	
	public void setInvertNormal(boolean isSet)
	{
		invertNormal = isSet;
	}
	
	public void setNormals(int x, int y, int z)
	{
		normals = new int[] {x, y, z};
	}
	
	public void setNormals(ArrayList<int[]> vec)
	{
		iNormals = vec;
	}
	
	public void draw(Tessellator tessellator, float f) {
        
        if(nVertices == 3) {
			tessellator.startDrawing(GL11.GL_TRIANGLES);
		}else if (nVertices == 4) {
			tessellator.startDrawing(GL11.GL_QUADS);
		}else {
			tessellator.startDrawing(GL11.GL_POLYGON);
		}
        if(iNormals.size() == 0) {
	        if(normals.length == 3) {
	        	if(invertNormal) {
	        		tessellator.setNormal(-normals[0], -normals[1], -normals[2]);
	        	} else {
	        		tessellator.setNormal(normals[0], normals[1], normals[2]);
	        	}
	        } else if(vertexPositions.length > 3) {
		        Vec3d Vec3d2 = new Vec3d(vertexPositions[1].vector3D.subtract(vertexPositions[2].vector3D)
						.crossProduct(vertexPositions[1].vector3D.subtract(vertexPositions[0].vector3D)).normalize());

		        if(invertNormal) {
		            tessellator.setNormal((int)-Vec3d2.xCoord, (int)-Vec3d2.yCoord, (int)-Vec3d2.zCoord);
		        } else {
		            tessellator.setNormal((int)Vec3d2.xCoord, (int)Vec3d2.yCoord, (int)Vec3d2.zCoord);
		        }
	        } else {
	        	return;
	        }
        }
        for(int i = 0; i < nVertices; i++) {
            PositionTextureVertex positionTexturevertex = vertexPositions[i];
            if(positionTexturevertex instanceof PositionTransformVertex) {
				((PositionTransformVertex) positionTexturevertex).setTransformation();
			}
            if(i < iNormals.size()) {
            	if(invertNormal) {
            		tessellator.setNormal(-iNormals.get(i)[0],-iNormals.get(i)[1],-iNormals.get(i)[2]);
            	} else {
            		tessellator.setNormal(iNormals.get(i)[0], iNormals.get(i)[1], iNormals.get(i)[2]);
            	}
            }
            tessellator.addVertexWithUV(positionTexturevertex.vector3D.xCoord * f, positionTexturevertex.vector3D.yCoord * f, positionTexturevertex.vector3D.zCoord * f, (float)positionTexturevertex.texturePositionX, (float)positionTexturevertex.texturePositionY);
        }

        tessellator.draw();
    }

    private boolean invertNormal = false;
    private int[] normals = new int[0];
    public int[] vertices;
    public float texturePositionX;
	public float texturePositionY;
    private ArrayList<int[]> iNormals = new ArrayList<>();
}
