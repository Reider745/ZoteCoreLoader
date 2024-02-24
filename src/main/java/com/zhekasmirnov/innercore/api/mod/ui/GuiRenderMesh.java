package com.zhekasmirnov.innercore.api.mod.ui;

import android.graphics.Bitmap;

@Deprecated(since = "Zote")
public class GuiRenderMesh {
    // Translate params.
    public float x = 0;

    public float y = 0;

    public float z = 0;

    // Rotate params.
    public float rx = 0;

    public float ry = 0;

    public float rz = 0;

    /**
     * Render the mesh.
     *
     * @param gl
     *           the OpenGL context to render to.
     */
    public void draw(/* GL10 */ Object gl) {
    }

    /**
     * Set the vertices.
     *
     * @param vertices
     */
    public void setVertices(float[] vertices) {
    }

    /**
     * Set the indices.
     *
     * @param indices
     */
    public void setIndices(short[] indices) {
    }

    /**
     * Set the texture coordinates.
     *
     * @param textureCoords
     */
    public void setTextureCoordinates(float[] textureCoords) {
    }

    /**
     * Set one flat color on the mesh.
     *
     * @param red
     * @param green
     * @param blue
     * @param alpha
     */
    protected void setColor(float red, float green, float blue, float alpha) {
    }

    /**
     * Set the colors
     *
     * @param colors
     */
    public void setColors(float[] colors) {
    }

    /**
     * Set the bitmap to load into a texture.
     *
     * @param bitmap
     */
    public void loadBitmap(Bitmap bitmap) {
    }
}
