package com.spots.liquidfun;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import org.jbox2d.common.Vec2;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by fredrikjohansson on 15-06-17.
 */
public class Renderer implements GLSurfaceView.Renderer {

    // Public variables
    public static final ArrayList<BaseObject> _actors = new ArrayList<>();

    // Private variables
    private BoxObject _bob;

    // Private static variables
    private static String _vertShaderCode =
        "attribute vec3 Position;" +
        "uniform mat4 Projection;" +
        "uniform mat4 ModelView;" +
        "void main() {" +
        "  mat4 mvp = Projection * ModelView;" +
        "  gl_Position = mvp * vec4(Position.xyz, 1);" +
        "}\n";

    private static String _fragShaderCode =
        "precision mediump float;" +
        "uniform vec4 Color;" +
        "void main() {" +
        "  gl_FragColor = Color;" +
        "}\n";

    private static int _shaderProg;
    private static float[] _projection = new float[16];
    private static float PPM = 64.0f;
    private static int _nextId = 0;
    private static long _spawnDelay = 0;
    private static int _screenW;
    private static int _screenH;



    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // Create program
        _shaderProg = GLES20.glCreateProgram();

        //Compile shaders
        int vertShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertShader, _vertShaderCode);
        GLES20.glCompileShader(vertShader);

        int fragShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragShader, _fragShaderCode);
        GLES20.glCompileShader(fragShader);

        // Attach shaders
        GLES20.glAttachShader(_shaderProg, vertShader);
        GLES20.glAttachShader(_shaderProg, fragShader);

        // Link and use the program
        GLES20.glLinkProgram(_shaderProg);
        GLES20.glUseProgram(_shaderProg);

        // Normal stuff
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        _bob = new BoxObject(800, 50);
        _bob._color = new Color3(255.0f, 0.0f ,0.0f);

        Physics.setGravity(new Vec2(0.0f, -10.0f));
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {

        GLES20.glViewport(0, 0, width, height);

        //Set ortho projection
        int projectionHandle = GLES20.glGetUniformLocation(_shaderProg, "Projection");

        Matrix.orthoM(_projection, 0, 0.0f, width, 0.0f, height, -10.0f, 10.0f);
        GLES20.glUniformMatrix4fv(projectionHandle, 1, false, _projection, 0);

        _screenW = width;
        _screenH = height;

        _bob.setPosition(new Vec2(_screenW / 2.0f, 100.0f));
        _bob.createPhysicsBody(0.0f, 0.5f, 0.8f);
    }

    @Override
    public void onDrawFrame(GL10 unused) {

        // Note when we begin
        long startTime = System.currentTimeMillis();

        if(System.currentTimeMillis() - _spawnDelay > 100){
            BoxObject obj = new BoxObject( (float) Math.random() * 50, (float) Math.random() * 50);
            obj._color = new Color3();
            obj._color._r = (int) (Math.random() * 255.0);
            obj._color._g = (int) (Math.random() * 255.0);
            obj._color._b = (int) (Math.random() * 255.0);

            obj.setPosition(new Vec2(_screenW / 2, _screenH / 2));
            obj.createPhysicsBody(1.0f, 0.2f, 0.5f);

            _spawnDelay = System.currentTimeMillis();
        }


        //gl.glMatrixMode(GL10.GL_MODELVIEW);
        //gl.glLoadIdentity();
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Draw everything
        for(BaseObject obj : _actors)
            obj.draw();

        // Calculate how much time renering took
        long drawTime = System.currentTimeMillis() - startTime;

        // If we didn't take enough time, sleep for the difference
        // 1.0f / 60.0f ~= 0.016666666f -> 0.016666666f * 1000 = 16.6666666f
        // Since currentTimeMills() returns a ms value, we convert our elapsed to ms
        //
        // It's also 1000.0f / 60.0f, but meh
        if(drawTime < 16)
            try{
                Thread.sleep(16 - drawTime);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
    }

    public static int getNextId() { return _nextId++; }

    public static Vec2 screenToWorld(Vec2 cords) { return new Vec2(cords.x / PPM, cords.y / PPM); }
    public static Vec2 worldToScreen(Vec2 cords) { return new Vec2(cords.x * PPM, cords.y * PPM); }
    public static float getPPM() { return PPM; }
    public static float getMPP() { return 1.0f / PPM; }
    public static int getShaderProg() { return _shaderProg; }
}
