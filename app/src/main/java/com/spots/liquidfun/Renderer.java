package com.spots.liquidfun;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import com.spots.varramie.Client;
import com.spots.varramie.R;
import org.jbox2d.common.Vec2;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleGroup;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
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
                    "precision mediump float;\n" +
                    "uniform mat4 Projection;   \n" +
                    "uniform mat4 ModelView;   \n" +
                    "uniform float a_PointSize;   \n" +
                    //"uniform float u_FrameBuffer;   \n" + // ?????? float?
                    "attribute vec3 Position;   \n" +
                    "attribute vec2 a_TexCoordinate;   \n" +
                    "attribute vec4 Color;   \n" +
                    "varying vec2 v_TexCoordinate;   \n" +
                    "varying vec4 v_color;   \n" +
                    "void main() {   \n" +
                    "  gl_PointSize = a_PointSize; \n" +
                    "  mat4 mvp = Projection * ModelView;   \n" +
                    "  gl_Position = mvp * vec4(Position.xyz, 1);   \n" +
                    "  v_TexCoordinate = a_TexCoordinate;   \n" +
                    "  v_color = Color;   \n" +
                    "}   \n";


    private final static String fragShaderCode =
                    "precision mediump float;\n" +
                    "uniform sampler2D u_Texture;   \n" +
                    "varying vec4 v_color;   \n" +
                    "varying vec2 v_TexCoordinate; \n" +
                    "void main() {   \n" +
                    "  gl_FragColor = texture2D(u_Texture, gl_PointCoord) * v_color;   \n"+
                    //"  gl_FragColor.rgb *= v_color.a;"+// * texture2D(u_Texture, gl_PointCoord);   \n"+
                    "}";
/*
    private final static String vertShaderCodeScreen =
            "precision mediump float;\n" +
                    "uniform mat4 Projection;   \n" +
                    "uniform mat4 ModelView;   \n" +
                    "attribute vec3 Position;   \n" +
                    "varying vec3 v_Position;   \n" +
                    "void main() {   \n" +

                    "  mat4 mvp = Projection * ModelView;   \n" +
                    "  gl_Position = mvp * vec4(Position.xyz, 1);   \n" +
                    "  v_Position = Position;   \n" +
                    "}   \n";


    private final static String fragShaderCodeScreen =
            "precision mediump float;\n" +
                    "uniform sampler2D u_Texture;   \n" +
                    "varying vec3 v_Position; \n" +
                    "void main() {   \n" +
                    "  gl_FragColor = texture2D(u_Texture, v_Position);   \n"+
                    //"  gl_FragColor.rgb *= v_color.a;"+// * texture2D(u_Texture, gl_PointCoord);   \n"+
                    "}";
*/
    private static int programHandle;
  //  private static int programHandleScreen;


    private final float[] textureCoordinateData = { 0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
    };
    private FloatBuffer mCubeTextureCoordinates;
    private FloatBuffer mPositions;
    private FloatBuffer mColors;

    private int mTextureCoordinateHandle;
    private int mTextureUniformHandle;
    private int mPointSizeHandle;
    private int mModelHandle;
    private int mColorHandle;
    private int mPositionHandle;
    //private int mFramebufferHandle;

    private int mTextureUniformHandleScreen;
    private int mModelHandleScreen;
    private int mPositionHandleScreen;

    int[] mFrameBuffers;
    int[] mRenderTex;

    public static int getShaderProg() {
        return programHandle;
    }

    public Renderer(Context _context){
        context = _context;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {

        int[] linkStatus = new int[1];

        // Compile shaders
        int vertShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertShader, vertShaderCode);
        GLES20.glCompileShader(vertShader);

        GLES20.glGetShaderiv(vertShader, GLES20.GL_LINK_STATUS, linkStatus, 0);
        String l = GLES20.glGetShaderInfoLog(vertShader);
        GLES20.glGetShaderiv(vertShader, GLES20.GL_COMPILE_STATUS, linkStatus, 0);
        String m = GLES20.glGetShaderInfoLog(vertShader);
        GLES20.glGetShaderiv(vertShader, GLES20.GL_VALIDATE_STATUS, linkStatus, 0);
        String n = GLES20.glGetShaderInfoLog(vertShader);

        int fragShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragShader, fragShaderCode);
        GLES20.glCompileShader(fragShader);

        GLES20.glGetShaderiv(fragShader, GLES20.GL_LINK_STATUS, linkStatus, 0);
        String o = GLES20.glGetShaderInfoLog(fragShader);
        GLES20.glGetShaderiv(fragShader, GLES20.GL_COMPILE_STATUS, linkStatus, 0);
        String p = GLES20.glGetShaderInfoLog(fragShader);
        GLES20.glGetShaderiv(fragShader, GLES20.GL_VALIDATE_STATUS, linkStatus, 0);
        String q = GLES20.glGetShaderInfoLog(fragShader);


/*        int vertShaderScreen = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertShaderScreen, vertShaderCodeScreen);
        GLES20.glCompileShader(vertShaderScreen);

        int fragShaderScreen = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragShaderScreen, fragShaderCodeScreen);
        GLES20.glCompileShader(fragShaderScreen);*/

        // Create programs
        programHandle = GLES20.glCreateProgram();
        //programHandleScreen = GLES20.glCreateProgram();


        GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
        String a = GLES20.glGetProgramInfoLog(programHandle);
        GLES20.glGetProgramiv(programHandle, GLES20.GL_COMPILE_STATUS, linkStatus, 0);
        String b = GLES20.glGetProgramInfoLog(programHandle);
        GLES20.glGetProgramiv(programHandle, GLES20.GL_VALIDATE_STATUS, linkStatus, 0);
        String c = GLES20.glGetProgramInfoLog(programHandle);

        // Attach shaders
        GLES20.glAttachShader(programHandle, vertShader);
        GLES20.glAttachShader(programHandle, fragShader);

        /*GLES20.glAttachShader(programHandleScreen, vertShaderScreen);
        GLES20.glAttachShader(programHandleScreen, fragShaderScreen);*/

        GLES20.glGetShaderiv(vertShader, GLES20.GL_LINK_STATUS, linkStatus, 0);
        String d = GLES20.glGetShaderInfoLog(vertShader);
        GLES20.glGetShaderiv(vertShader, GLES20.GL_COMPILE_STATUS, linkStatus, 0);
        String e = GLES20.glGetShaderInfoLog(vertShader);
        GLES20.glGetShaderiv(vertShader, GLES20.GL_VALIDATE_STATUS, linkStatus, 0);
        String f = GLES20.glGetShaderInfoLog(vertShader);

        GLES20.glGetShaderiv(fragShader, GLES20.GL_LINK_STATUS, linkStatus, 0);
        String g = GLES20.glGetShaderInfoLog(fragShader);
        GLES20.glGetShaderiv(fragShader, GLES20.GL_COMPILE_STATUS, linkStatus, 0);
        String h = GLES20.glGetShaderInfoLog(fragShader);
        GLES20.glGetShaderiv(fragShader, GLES20.GL_VALIDATE_STATUS, linkStatus, 0);
        String i = GLES20.glGetShaderInfoLog(fragShader);

        // Link and use the program
        GLES20.glLinkProgram(programHandle);
        //GLES20.glLinkProgram(programHandleScreen);

        GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
        String j = GLES20.glGetProgramInfoLog(programHandle);
        GLES20.glGetProgramiv(programHandle, GLES20.GL_COMPILE_STATUS, linkStatus, 0);
        String k = GLES20.glGetProgramInfoLog(programHandle);
        GLES20.glGetProgramiv(programHandle, GLES20.GL_VALIDATE_STATUS, linkStatus, 0);
        String r = GLES20.glGetProgramInfoLog(programHandle);


        GLES20.glUseProgram(programHandle);


        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        mTextureUniformHandle =     GLES20.glGetUniformLocation(Renderer.getShaderProg(), "u_Texture");
        mModelHandle =               GLES20.glGetUniformLocation(Renderer.getShaderProg(), "ModelView");
        mPointSizeHandle =          GLES20.glGetUniformLocation(Renderer.getShaderProg(), "a_PointSize");

        mTextureCoordinateHandle =  GLES20.glGetAttribLocation(Renderer.getShaderProg(), "a_TexCoordinate");
        mColorHandle =               GLES20.glGetAttribLocation(Renderer.getShaderProg(), "Color");
        mPositionHandle =            GLES20.glGetAttribLocation(Renderer.getShaderProg(), "Position");

        //mFramebufferHandle =        GLES20.glGetUniformLocation(Renderer.getShaderProg(), "u_FrameBuffer");

        /*mTextureUniformHandleScreen =     GLES20.glGetUniformLocation(programHandleScreen, "u_Texture");
        mModelHandleScreen =               GLES20.glGetUniformLocation(programHandleScreen, "ModelView");
        mPositionHandleScreen =            GLES20.glGetAttribLocation(programHandleScreen, "Position");*/

        mCubeTextureCoordinates = ByteBuffer.allocateDirect(textureCoordinateData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(textureCoordinateData);
        mCubeTextureCoordinates.position(0);


        //GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
        int sTextureIndex = loadTexture(Renderer.context, R.drawable.white_point);

        GLES20.glUniform1i(mTextureUniformHandle, sTextureIndex);                       // u_texture
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, 2, GLES20.GL_FLOAT, false, 0, mCubeTextureCoordinates);  // a_texture
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        //boolean ok = loadFBO();


        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        Client.INSTANCE.println("End of Renderer onSurfaceCreated");


    }

    public boolean loadFBO(){
        mFrameBuffers = new int[1];
        mRenderTex = new int[1];
        int[] depthRenderbuffers = new int[1];

        GLES20.glGenFramebuffers(1, mFrameBuffers, 0);                       // Generate a framebuffer object name
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[0]);   // Bind the framebuffer object name to a framebuffer target
        GLES20.glGenTextures(1, mRenderTex, 0);                              // Generate a texture name.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mRenderTex[0]);           // Bind the texture name to a texture target.
        GLES20.glGenRenderbuffers(1, depthRenderbuffers, 0);

        //Define texture parameters
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, screenW, screenH, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        //Bind render buffer and define buffer dimension
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, depthRenderbuffers[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, screenW, screenH);

        // Attach the texture to the framebuffer
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mRenderTex[0], 0);

        //Attach render buffer to depth attachment
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, depthRenderbuffers[0]);

        //GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mRenderTex[0], 0);
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);

        //we are done, reset
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glUniform1i(mTextureUniformHandleScreen, mRenderTex[0]);                       // u_texture

        return status != GLES20.GL_FRAMEBUFFER_COMPLETE;
    }


    public int loadTexture(final Context context, final int resourceId)
    {
        // One texture
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;	// No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
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
        Physics.GROUP_RADIUS = Renderer.screenW / 10.0f;
        Physics.PARTICLE_RADIUS = Physics.GROUP_RADIUS / 10.0f;

        Physics.start();

    }

    public static Object lock = new Object();

    @Override
    public void onDrawFrame(GL10 gl) {

        synchronized (lock){
            try {
                lock.wait();
            } catch (InterruptedException e) {

            }
        }


        // Note when we begin
        long startTime = System.currentTimeMillis();

        // Wait for Physicsworld

        GLES20.glUseProgram(programHandle);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        float[] modelView = new float[16];

        /*if(Physics.physicsWorld == null)
            return;*/
        ParticleColor[] colorBuff = Physics.physicsWorld.getParticleColorBuffer();
        Vec2[] posBuff = Physics.physicsWorld.getParticlePositionBuffer();
        //ParticleGroup[] list = Physics.mParticleSystem.getParticleGroupList();

        /*int _firstIndex = list[0].getBufferIndex();
        int _lastIndex = _firstIndex + list[0].getParticleCount();

        for(int i = _firstIndex; i < _lastIndex; ++i) {
            int[] particleGlagsBuffer = Physics.mParticleSystem.getParticleFlagsBuffer();
        }*/

        int particleCount = Physics.physicsWorld.getParticleCount();

        if(posBuff == null || colorBuff == null)
            return;

        if(colorBuff.length != posBuff.length)
            return;

        float[] positionData = new float[particleCount * 3];
        float[] colorData = new float[particleCount * 4];

        for(int i = 0; i < particleCount; i++){
            int u = i * 3;
            Vec2 pos = worldToScreen(posBuff[i]);
            positionData[u] = pos.x;
            positionData[u + 1] = pos.y;
            positionData[u + 2] = 0.0f;

            int v = i * 4;
            colorData[v] = (colorBuff[i].r & 0xff)/255.0f;
            colorData[v + 1] = (colorBuff[i].g & 0xff)/255.0f;
            colorData[v + 2] = (colorBuff[i].b & 0xff)/255.0f;
            colorData[v + 3] = (colorBuff[i].a & 0xff)/255.0f;
        }

        mPositions = ByteBuffer.allocateDirect(positionData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(positionData);
        mPositions.position(0);
        mColors = ByteBuffer.allocateDirect(colorData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(colorData);
        mColors.position(0);

        Matrix.setIdentityM(modelView, 0);
        GLES20.glUniformMatrix4fv(mModelHandle, 1, false, modelView, 0);     // ModelVie

        GLES20.glUniform1f(mPointSizeHandle, worldToScreen(Physics.physicsWorld.getParticleRadius()));

        // Atribute pointers
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 0, mColors);                         // Color
        GLES20.glEnableVertexAttribArray(mColorHandle);

        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mPositions);                         // Position
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, positionData.length / 3);


        /**
         * Draw on screen code below
         * */
 /*       GLES20.glUseProgram(programHandleScreen);


        Matrix.setIdentityM(modelView, 0);
        GLES20.glUniformMatrix4fv(mModelHandleScreen, 1, false, modelView, 0);     // ModelVie


        float[] mVerticesData =
                {
                        0.0f, 0.0f, 0.0f, // Position 0
                        0.0f, screenH, 0.0f, // Position 1
                        screenW, 0.0f, 0.0f, // Position 2
                        screenW, screenH, 0.0f, // Position 3

                };
        FloatBuffer mPos = ByteBuffer.allocateDirect(mVerticesData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(mVerticesData);
        mPos.position(0);

        GLES20.glVertexAttribPointer(mPositionHandleScreen, 3, GLES20.GL_FLOAT, false, 0, mPos);                         // Position
        GLES20.glEnableVertexAttribArray(mPositionHandleScreen);


        short[] mIndicesData =
        {
                0, 1, 2, 0, 2, 3
        };

        ShortBuffer mIndices = ByteBuffer.allocateDirect(mIndicesData.length * 2).order(ByteOrder.nativeOrder()).asShortBuffer().put(mIndicesData);
        mIndices.position(0);



        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mPos);                         // Position
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Bind the default framebuffer (to render to the screen) - indicated by '0'
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBuffers[0]);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        GLES20.glUniform1i(GLES20.glGetUniformLocation(programHandle, "u_Texture"), 0);

        GLES20.glDrawElements ( GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, mIndices );
*/


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
