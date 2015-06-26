package com.spots.liquidfun;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;


import com.spots.varramie.R;
import org.jbox2d.common.Vec2;
import org.jbox2d.particle.ParticleGroup;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Renderer implements GLSurfaceView.Renderer {

    private static int nextId = 0;
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


    public static int mMVPMatrixHandle;
    public static int mMVMatrixHandle;
    public static int mPositionHandle;
    public static int mLightPosHandle;
    public static int mTextureDataHandle;
    public static int mTextureUniformHandle;                  // This will be used to pass in the texture.
    public static int mColorHandle;
    public static int mNormalHandle;
    public static int mTextureCoordinateHandle;               // This will be used to pass in model texture coordinate information.
    public static int mProgramHandle;
    public static int mPointProgramHandle;


    private float[] mLightModelMatrix = new float[16];
    private final float[] mLightPosInWorldSpace = new float[4];
    private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
    public static final float[] mLightPosInEyeSpace = new float[4];
    public static float[] mViewMatrix = new float[16]; // Store the view matrix. This can be thought of as our camera. This matrix transforms world space to eye space it positions things relative to our eye.
    public static float[] mProjectionMatrix = new float[16]; // Store the projection matrix. This is used to project the scene onto a 2D viewport
    public static float[] mMVPMatrix = new float[16]; // Allocate storage for the final combined matrix. This will be passed into the shader program.

    public static int screenW;
    public static int screenH;
    public static float screenRatio;

    public static Context context;

    private Particle p;

    /*
    private static String vertShaderCode =
            "attribute vec3 Position;" +
                    "uniform mat4 Projection;" +
                    "uniform mat4 ModelView;" +
                    "uniform float PointSize;" +
                    "void main() {" +
                    "  mat4 mvp = Projection * ModelView;" +
                    "  gl_Position = mvp * vec4(Position.xyz, 1);" +
                    "  gl_PointSize = PointSize;" +
                    "}\n";

    private static String fragShaderCode =
            "precision mediump float;" +
                    "uniform vec4 Color;" +
                    "void main() {" +
                    "  gl_FragColor = Color;" +
                    "}\n";
    */

    private static String vertShaderCode =
                    "niform mat4 u_MVPMatrix;           // A constant representing the combined model/view/projection matrix." +
                    "uniform mat4 u_MVMatrix;           // A constant representing the combined model/view matrix." +
                    "attribute vec4 a_Position;         // Per-vertex position information we will pass in." +
                    "attribute vec4 a_Color;            // Per-vertex color information we will pass in." +
                    "attribute vec3 a_Normal;           // Per-vertex normal information we will pass in." +
                    "attribute vec2 a_TexCoordinate;    // Per-vertex texture coordinate information we will pass in." +
                    "varying vec3 v_Position;           // This will be passed into the fragment shader." +
                    "varying vec4 v_Color;              // This will be passed into the fragment shader." +
                    "varying vec3 v_Normal;             // This will be passed into the fragment shader." +
                    "varying vec2 v_TexCoordinate;      // This will be passed into the fragment shader." +
                    "void main()" +
                    "   " +
                    "   v_Position = vec3(u_MVMatrix * a_Position); // Transform the vertex into eye space." +
                    "   v_Color = a_Color; // Pass through the color." +
                    "   v_TexCoordinate = a_TexCoordinate; // Pass through the texture coordinate." +
                    "   v_Normal = vec3(u_MVMatrix * vec4(a_Normal, 0.0)); // Transform the normal's orientation into eye space." +
                    "   // gl_Position is a special variable used to store the final position." +
                    "   // Multiply the vertex by the matrix to get the final point in normalized screen coordinates." +
                    "   gl_Position = u_MVPMatrix * a_Position;" +
                    "}";

    private static String fragShaderCode =
            "precision mediump float;               // Set the default precision to medium. We don't need as high of a" +
                    "                               // precision in the fragment shader." +
                    "uniform vec3 u_LightPos;       // The position of the light in eye space." +
                    "uniform sampler2D u_Texture;   // The input texture." +
                    "varying vec3 v_Position;       // Interpolated position for this fragment." +
                    "varying vec4 v_Color;          // This is the color from the vertex shader interpolated across the" +
                    "                               // triangle per fragment." +
                    "varying vec3 v_Normal;         // Interpolated normal for this fragment." +
                    "varying vec2 v_TexCoordinate;  // Interpolated texture coordinate per fragment." +

                    "// The entry point for our fragment shader." +
                    "void main()" +
                    "{" +
                    "   float distance = length(u_LightPos - v_Position);       // Will be used for attenuation." +
                    "   vec3 lightVector = normalize(u_LightPos - v_Position);  // Get a lighting direction vector from the light to the vertex." +
                    "   float diffuse = max(dot(v_Normal, lightVector), 0.0);   // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are" +
                    "                                                           // pointing in the same direction then it will get max illumination."+
                    "   diffuse = diffuse * (1.0 / (1.0 + (0.10 * distance)));  // Add attenuation." +
                    "   diffuse = diffuse + 0.3; // Add ambient lighting" +
                    "   gl_FragColor = (v_Color * diffuse * texture2D(u_Texture, v_TexCoordinate)); // Multiply the color by the diffuse illumination level and texture value to get final output color." +
                    "}";

    final static String pointVertexShader =    "uniform mat4 u_MVPMatrix;" +
            "attribute vec4 a_Position;" +
            "void main()" +
            "{" +
            "gl_Position = u_MVPMatrix * a_Position;" +
            "gl_PointSize = 5.0;" +
            "}";

    final static String pointFragmentShader =  "precision mediump float;" +
            "void main()" +
            "{" +
            "gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);" +
            "} ";


    public Renderer(Context _context){
        context = _context;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig eglConfig) {

        // Set the background clear color to black.
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // The below glEnable() call is a holdover from OpenGL ES 1, and is not needed in OpenGL ES 2.
        // Enable texture mapping
        // GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        // Position the eye in front of the origin.
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = -0.5f;

        // We are looking toward the distance
        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;

        // Set our up vector. This is where our head would be pointing were we holding the camera.
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;

        // Set the view matrix. This matrix can be said to represent the camera position.
        // NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
        // view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);


        // Create program
        mProgramHandle = GLES20.glCreateProgram();

        // Compile shaders
        int vertShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertShader, vertShaderCode);
        GLES20.glCompileShader(vertShader);

        int fragShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragShader, fragShaderCode);
        GLES20.glCompileShader(fragShader);

        // Attach shaders
        GLES20.glAttachShader(mProgramHandle, vertShader);
        GLES20.glAttachShader(mProgramHandle, fragShader);

        GLES20.glBindAttribLocation(mProgramHandle, 0, "a_Position");
        GLES20.glBindAttribLocation(mProgramHandle, 1, "a_Color");
        GLES20.glBindAttribLocation(mProgramHandle, 2, "a_Normal");
        GLES20.glBindAttribLocation(mProgramHandle, 3, "a_TexCoordinate");

        // Link and use the program
        GLES20.glLinkProgram(mProgramHandle);

        // Create program
        mPointProgramHandle = GLES20.glCreateProgram();

        // Define a simple shader program for our point.
        int pointVertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(pointVertexShaderHandle, pointVertexShader);
        GLES20.glCompileShader(pointVertexShaderHandle);

        int pointFragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(pointFragmentShaderHandle, pointFragmentShader);
        GLES20.glCompileShader(pointFragmentShaderHandle);

        // Attach shaders
        GLES20.glAttachShader(mPointProgramHandle, pointVertexShaderHandle);
        GLES20.glAttachShader(mPointProgramHandle, pointFragmentShaderHandle);

        GLES20.glBindAttribLocation(mPointProgramHandle, 0, "a_Position");


        mTextureDataHandle = Renderer.loadTexture(Renderer.context, R.drawable.bumpy_bricks_public_domain);


        //ClusterManager.createNewCluster(123);

        // Creates a particle without body, not connected to the physics world
        p = new Particle(Renderer.screenToWorld(100.0f), Renderer.screenToWorld(new Vec2(150.0f, 150.0f)), 0.0f);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height){

        GLES20.glViewport(0, 0, width, height);

        // Set ortho projection
        int projectionHandle = GLES20.glGetUniformLocation(mProgramHandle, "Projection");
        Matrix.orthoM(mProjectionMatrix, 0, 0, width, height, 0, -10, 10);
        GLES20.glUniformMatrix4fv(projectionHandle, 1, false, mProjectionMatrix, 0);


        // Create a new perspective projection matrix. The height will stay the same
        // while the width will vary as per aspect ratio.
        final float ratio = (float) width / height;
        final float left = -ratio;
        final float right = ratio;
        final float bottom = -1.0f;
        final float top = 1.0f;
        final float near = 1.0f;
        final float far = 10.0f;

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);

        screenW = width;
        screenH = height;
        screenRatio = (float)width/ (float)height;

    }

    @Override
    public void onDrawFrame(GL10 gl) {

        // Note when we begin
        long startTime = System.currentTimeMillis();

        /*if(!groupQ.isEmpty()){
            ParticleGroup grp = groupQ.poll();

            int id = (int) grp.getUserData();

            if(id == ClusterManager.myClusterId)
                ClusterManager.myCluster = new Cluster(grp, (int) grp.getUserData());
            else
                new Cluster(grp, (int) grp.getUserData());
        }*/


        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mProgramHandle);


        // Set our per-vertex lighting program.  (Already done in the renderer)
        // GLES20.glUseProgram(mProgramHandle);

        // Set program handles for cube drawing.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVMatrix");
        mLightPosHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_LightPos");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");
        mNormalHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Normal");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        // Calculate position of the light. Rotate and then push into the distance.
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -5.0f);
        Matrix.rotateM(mLightModelMatrix, 0, 0.0f, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, 2.0f);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);


        p.draw(gl);

        // Draw a point to indicate the light.
        GLES20.glUseProgram(mPointProgramHandle);
        drawLight();


        /*GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glEnable(GLES20.GL_BLEND);
        //GLES20.glEnable(GL10.GL_POINT_SMOOTH);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //b.draw(gl);
        for(int i = 0; i < ClusterManager.allClusters.size(); i++){
            ClusterManager.allClusters.valueAt(i).draw(gl);
        }

        GLES20.glDisable(GLES20.GL_BLEND);*/
        //GLES20.glDisable(GL10.GL_POINT_SMOOTH);

        //p.draw(gl);

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

    public static int loadTexture(final Context context, final int resourceId)
    {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0)
        {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;   // No pre-scaling

            // Read in the resource
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

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

    /**
     * Draws a point representing the position of the light.
     */
    private void drawLight()
    {
        final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
        final int pointPositionHandle = GLES20.glGetAttribLocation(mPointProgramHandle, "a_Position");

        // Pass in the position.
        GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);

        // Since we are not using a buffer object, disable vertex arrays for this attribute.
        GLES20.glDisableVertexAttribArray(pointPositionHandle);

        // Pass in the transformation matrix.
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        // Draw the point.
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);

    }

    public static int getShaderProg(){
        return mProgramHandle;
    }

}
