package com.spots.liquidfun;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;


import com.spots.varramie.R;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleGroupType;
import org.jbox2d.particle.ParticleSystem;
import org.jbox2d.particle.ParticleType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class Renderer implements GLSurfaceView.Renderer {

    public static ArrayList<BaseObject> actors = new ArrayList<BaseObject>();
    public static final LinkedBlockingQueue<ParticleGroup> groupQ = new LinkedBlockingQueue<>();


    private static float PPM = 128.0f;

    public static Vec2 screenToWorld(Vec2 cords) {
        return new Vec2(cords.x / PPM, cords.y / PPM);
    }
    public static float screenToWorld(float val) {
        return val / PPM;
    }

    public static Vec2 worldToScreen(Vec2 cords) {
        return new Vec2(cords.x * PPM, cords.y * PPM);
    }

    public static float worldToScreen(float val){
        return val * PPM;
    }

    public static float getPPM() {
        return PPM;
    }

    public static int screenW;
    public static int screenH;
    public static float screenRatio;

    public static Context context;

    private final static String vertShaderCode =

                    "uniform mat4 Projection;   \n" +
                    "uniform mat4 ModelView;   \n" +
                    "attribute vec3 Position;   \n" +
                    "attribute vec2 a_TexCoordinate;   \n" +
                    "varying vec2 v_TexCoordinate;   \n" +
                    "void main() {   \n" +
                    "  mat4 mvp = Projection * ModelView;   \n" +
                    "  gl_Position = mvp * vec4(Position.xyz, 1);   \n" +
                    "  v_TexCoordinate = a_TexCoordinate;   \n" +
                    "}   \n";


    private final static String fragShaderCode =
                    "uniform sampler2D u_Texture;   \n" +
                    "uniform vec4 Color;   \n" +
                    "varying vec2 v_TexCoordinate; \n" +
                    "void main() {   \n" +
                    "  gl_FragColor = Color * texture2D(u_Texture, v_TexCoordinate);   \n"+
                    "}";

    private static int programHandle;


    public static int getShaderProg() {
        return programHandle;
    }

    public Renderer(Context _context){
        context = _context;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {

        // Compile shaders
        int vertShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertShader, vertShaderCode);
        GLES20.glCompileShader(vertShader);

        int fragShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragShader, fragShaderCode);
        GLES20.glCompileShader(fragShader);

        // Create program
        programHandle = GLES20.glCreateProgram();

        // Attach shaders
        GLES20.glAttachShader(programHandle, vertShader);
        GLES20.glAttachShader(programHandle, fragShader);

        // Link and use the program
        GLES20.glLinkProgram(programHandle);

        GLES20.glUseProgram(programHandle);

        // Normal stuff
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //ClusterManager.createNewCluster("TEST CLUSTER", new Vec2(200,200));
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        GLES20.glViewport(0, 0, width, height);

        float[] projection = new float[16];

        // Set ortho projection
        int projectionHandle =          GLES20.glGetUniformLocation(programHandle, "Projection");
        Matrix.orthoM(projection, 0, 0, width, height, 0, -10, 10);
        GLES20.glUniformMatrix4fv(projectionHandle, 1, false, projection, 0);

        screenW = width;
        screenH = height;
        screenRatio = (float)width/ (float)height;

    }

    @Override
    public void onDrawFrame(GL10 gl) {

        // Note when we begin
        long startTime = System.currentTimeMillis();

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if(!groupQ.isEmpty()){
            ParticleGroup grp = groupQ.poll();

            String id = (String) grp.getUserData();

            if(id == ClusterManager.myClusterId)
                ClusterManager.myCluster = new Cluster(grp, id);
            else
                new Cluster(grp, id);
        }


        for(Cluster c : ClusterManager.allClusters.values()){
            c.draw(gl);
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

}
