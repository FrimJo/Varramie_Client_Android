package com.spots.liquidfun;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import org.jbox2d.common.Vec2;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Renderer implements GLSurfaceView.Renderer {

    private static int nextId = 0;
    public static ArrayList<BaseObject> actors = new ArrayList<BaseObject>();

    private BoxObject leftBoundary;
    private BoxObject rightBoundary;
    private BoxObject topBoundary;
    private BoxObject bottomBoundary;

    private BoxObject[] boundraies = new BoxObject[4];  // Left, right, top, bottom
    private final float BOUNDRAIES_FRICTION = 0.2f;

    private static float PPM = 128.0f;

    public static Vec2 screenToWorld(Vec2 cords) {
        return new Vec2(cords.x / PPM, cords.y / PPM);
    }

    public static Vec2 worldToScreen(Vec2 cords) {
        return new Vec2(cords.x * PPM, cords.y * PPM);
    }

    public static float getPPM() {
        return PPM;
    }

    public static float getMPP() {
        return 1.0f / PPM;
    }

    private static long spawnDelay = 0;
    private static int screenW;
    private static int screenH;

    private static String vertShaderCode =
            "attribute vec3 Position;" +
                    "uniform mat4 Projection;" +
                    "uniform mat4 ModelView;" +
                    "void main() {" +
                    "  mat4 mvp = Projection * ModelView;" +
                    "  gl_Position = mvp * vec4(Position.xyz, 1);" +
                    "}\n";

    private static String fragShaderCode =
            "precision mediump float;" +
                    "uniform vec4 Color;" +
                    "void main() {" +
                    "  gl_FragColor = Color;" +
                    "}\n";

    private static int shaderProg;
    private static float[] projection = new float[16];

    public static int getShaderProg() {
        return shaderProg;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {
        // Create program
        shaderProg = GLES20.glCreateProgram();

        // Compile shaders
        int vertShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertShader, vertShaderCode);
        GLES20.glCompileShader(vertShader);

        int fragShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragShader, fragShaderCode);
        GLES20.glCompileShader(fragShader);

        // Attach shaders
        GLES20.glAttachShader(shaderProg, vertShader);
        GLES20.glAttachShader(shaderProg, fragShader);

        // Link and use the program
        GLES20.glLinkProgram(shaderProg);
        GLES20.glUseProgram(shaderProg);

        // Normal stuff
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        boundraies[0] = new BoxObject(10, 800);
        boundraies[0].color = new Color3(0, 255, 0);

        boundraies[1] = new BoxObject(10, 800);
        boundraies[1].color = new Color3(0, 255, 0);

        boundraies[2] = new BoxObject(800, 10);
        boundraies[2].color = new Color3(0, 255, 0);

        boundraies[3] = new BoxObject(800, 10);
        boundraies[3].color = new Color3(0, 255, 0);


        Physics.setGravity(new Vec2(0, -0)); //Physics.setGravity(new Vec2(0, -10));
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        GLES20.glViewport(0, 0, width, height);

        // Set ortho projection
        int projectionHandle = GLES20.glGetUniformLocation(shaderProg, "Projection");
        Matrix.orthoM(projection, 0, 0, width, 0, height, -10, 10);
        GLES20.glUniformMatrix4fv(projectionHandle, 1, false, projection, 0);

        ParticleSystemDef test = new ParticleSystemDef();


        screenW = width;
        screenH = height;

        boundraies[0].setHeight(height);
        boundraies[0].setPosition(new Vec2(0.0f, 0.0f));
        boundraies[0].createPhysicsBody(0, BOUNDRAIES_FRICTION, 0.8f);

        boundraies[1].setHeight(height);
        boundraies[1].setPosition(new Vec2(width - 10.0f, 0.0f));
        boundraies[1].createPhysicsBody(0, BOUNDRAIES_FRICTION, 0.8f);

        boundraies[2].setWidth(width);
        boundraies[2].setPosition(new Vec2(0.0f, 0.0f));
        boundraies[2].createPhysicsBody(0, BOUNDRAIES_FRICTION, 0.8f);

        boundraies[3].setWidth(width);
        boundraies[3].setPosition(new Vec2(0.0f, height - 10.0f));
        boundraies[3].createPhysicsBody(0, BOUNDRAIES_FRICTION, 0.8f);

    }

    @Override
    public void onDrawFrame(GL10 gl) {

        // Note when we begin
        long startTime = System.currentTimeMillis();

        if (System.currentTimeMillis() - spawnDelay > 100) {

            //CircleObject obj = new CircleObject((float)Math.random() * 50, 10);

            BoxObject obj = new BoxObject((float) Math.random() * 50, (float) Math.random() * 50);
            obj.color = new Color3();
            obj.color.r = (int) (Math.random() * 255);
            obj.color.g = (int) (Math.random() * 255);
            obj.color.b = (int) (Math.random() * 255);

            obj.setPosition(new Vec2(screenW / 2, screenH / 2));
            obj.createPhysicsBody(1.0f, 0.2f, 0.5f);

            spawnDelay = System.currentTimeMillis();
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Draw everything
        for (BaseObject obj : actors) {
            obj.draw(gl);
        }

        // Calculate how much time rendering took
        long drawTime = System.currentTimeMillis() - startTime;

        // If we didn't take enough time, sleep for the difference
        // 1.0f / 60.0f ~= 0.016666666f -> 0.016666666f * 1000 = 16.6666666f
        // Since currentTimeMillis() returns a ms value, we convert our elapsed to ms
        //
        // It's also 1000.0f / 60.0f, but meh
        if (drawTime < 16) {
            try {
                Thread.sleep(16 - drawTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static int getNextId() {
        return nextId++;
    }
}
