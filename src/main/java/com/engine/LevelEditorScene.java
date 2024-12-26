package com.engine;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Vector2f;
import org.lwjgl.BufferUtils;

import com.renderer.Shader;
import com.renderer.Texture;
import com.util.Time;


public class LevelEditorScene extends Scene{

    // private String vertexShaderSrc = "#version 330 core\n" +
    // "layout (location=0) in vec3 aPos;\n" +
    // "layout (location=1) in vec4 aColor;\n" +
    // "out vec4 fColor;\n" +
    // "void main()\n" +
    // "{\n" +
    //     "fColor = aColor;\n" +
    //     "gl_Position = vec4(aPos, 1.0);\n" +
    // "}";

    // private String fragmentShaderSrc = "#version 330 core\n" +
    // "in vec4 fColor;\n" +
    // "out vec4 color;\n" +
    // "void main()\n" +
    // "{\n" +
    //     "color = fColor;\n" +
    // "}";
    
    private int vertexID, fragmentID, shaderProgram;

    private float[] vertexArray = {
         // position                    // color                   // UV Coordinates 
           100f,     0f,  0.0f,          1.0f, 0.0f, 0.0f, 1.0f,     1, 1,  // Bottom Right  (0)
             0f,   100f,  0.0f,          0.0f, 1.0f, 0.0f, 1.0f,     0, 0,  // Top left      (1)
           100f,   100f,  0.0f,          0.0f, 0.0f, 1.0f, 1.0f,     1, 0,  // Top Right     (2)
             0f,     0f,  0.0f,          1.0f, 1.0f, 0.0f, 1.0f,     0, 1,  // Bottom left  (3)
    };

    // IMPORTANT: Must be in counter-clockwise order
    private int[] elementArray = {
        /*
         *   x    x
         * 
         *   x    x
         */
        2, 1, 0, // Top right triangle
        0, 1, 3 // Bottom left triangle 
    };

    private int vaoID, vboID, eboID;
    private Shader defaultShader;
    private Texture testTexture;

    GameObject testOBJ;


    public LevelEditorScene() {
    }

    @Override 
    public void init() {
        this.testOBJ = new GameObject("test object");

        this.camera = new Camera(new Vector2f());
        defaultShader = new Shader("assets/shaders/default.glsl");
        defaultShader.compile();
        this.testTexture = new Texture("assets/images/testImage.png");


        // ===========================================================
        // Generate VAO, VBO, and EBO buffer objects, and send to GPU.
        // ===========================================================
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID); 

        // Create a float buffer of vertices
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexArray.length);
        vertexBuffer.put(vertexArray).flip();

        // Create VBO upload the vertex buffer 
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID); 
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Create the indices and upload 
        IntBuffer elementBuffer = BufferUtils.createIntBuffer(elementArray.length);
        elementBuffer.put(elementArray).flip();

        eboID = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);

        // Add the vertex attribute pointers
        int positionsSize = 3;
        int colorSize = 4;
        int uvSize = 2;
        int vertexSizeBytes = (positionsSize + colorSize + uvSize) * Float.BYTES;
         
        glVertexAttribPointer(0, positionsSize, GL_FLOAT, false, vertexSizeBytes, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, colorSize, GL_FLOAT, false, vertexSizeBytes, positionsSize * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, uvSize, GL_FLOAT, false, vertexSizeBytes, (positionsSize + colorSize) * Float.BYTES);
        glEnableVertexAttribArray(2);
    }

    @Override
    public void update(float dt) {
        defaultShader.use();

        // Upload texture to shader
        defaultShader.uploadTexture("TEX_SAMPLER", 0);
        glActiveTexture(GL_TEXTURE0);
        testTexture.bind();

        defaultShader.uploadMat4f("uProjection", camera.getProjectionMatrix());
        defaultShader.uploadMat4f("uView", camera.getViewMatrix());
        defaultShader.uploadFloat("uTime", Time.getTime());
        // Bind the VAO that we're using
        glBindVertexArray(vaoID);

        // Enable vertex attribute poiunters
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        glDrawElements(GL_TRIANGLES, elementArray.length, GL_UNSIGNED_INT, 0);

        // Unbind everything 
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        glBindVertexArray(0);

        defaultShader.detach();
    }
}