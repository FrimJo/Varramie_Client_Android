package com.spots.varramie;

import android.opengl.GLES20;
import android.util.SparseArray;

public class Spot{


    //private static final SparseArray<Spot> allSpots = new SparseArray<>();

    private static boolean defaultHiden = false;
    private static final int NR_OF_CIRCLES = 10;
    private Circle point;

    private boolean active = false;
    private boolean hidden = defaultHiden;
    private int id;


    public static class SpotManager{


        private static final int NR_OF_ALOWED_CONNECTIONS = 10;

        private static final SparseArray<Spot> allSpots = new SparseArray<>();

        private static final Spot 		mySpot = new Spot();

        private static final float		CIRCLE_RADIUS = 0.05f;
        private static final int		NUM_SEGMENTS = 30;
        private static final float[]	CIRCLE_FRAME = Circle.createCircle(0.0f, 0.0f, CIRCLE_RADIUS, NUM_SEGMENTS);

        private static final String vertexShaderCode =
                // This matrix member variable provides a hook to manipulate
                // the coordinates of the objects that use this vertex shader
                "uniform mat4 uMVPMatrix;" +
                        "attribute vec4 vPosition;" +
                        "void main() {" +
                        // the matrix must be included as a modifier of gl_Position
                        // Note that the uMVPMatrix factor *must be first* in order
                        // for the matrix multiplication product to be correct.
                        "  gl_Position = uMVPMatrix * vPosition;" +
                        "}";

        private static final String fragmentShaderCode =
                "precision mediump float;" +
                        "uniform vec4 vColor;" +
                        "void main() {" +
                        "  gl_FragColor = vColor;" +
                        "}";

        private static int mProgram;

        public static void init(){

            // Generate all spots.
            mySpot.init();

            for(int i = 0; i < NR_OF_ALOWED_CONNECTIONS; i++){
                Spot s = new Spot(i);
                allSpots.append(i, s);
            }
            mProgram = GLES20.glCreateProgram();
            int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
            int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
            GLES20.glAttachShader(mProgram, vertexShader); // add the vertex shader to program
            GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
            GLES20.glLinkProgram(mProgram); // creates OpenGL ES program executables

        }

        public static Spot getSpot(int id){
            return allSpots.get(id);
        }


        public static void setIsHidden(boolean value){
            for(int i = 0; i < allSpots.size(); i++){
                allSpots.get(i).hidden = value;
            }
        }

        public static void drawAllSpots(float[] mvpMatrix){
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            GLES20.glUseProgram(mProgram); // Add program to OpenGL ES environment
            Spot s;
            for(int i = 0; i < allSpots.size(); i++){
                s = allSpots.get(i);
                if(s.isActive())
                    s.draw(mvpMatrix, mProgram);
            }

            if(mySpot != null && mySpot.isActive())
                mySpot.draw(mvpMatrix, mProgram);

            GLES20.glDisable(GLES20.GL_BLEND);
        }

        public static Spot getMySpot(){
            return mySpot;
        }

        public static int getMySpotId(){
            return mySpot.id;
        }

        public static void setMySpotId(final int id){
            mySpot.id = id;
        }

        public static void activateMySpot(final float x, final float y, final float pressure){
            mySpot.activate(x, y, pressure);
        }
        public static void updateMySpot(final float x, final float y, final float pressure){
            mySpot.update(x, y, pressure);
        }
        public static void deactivateMySpot(final float x, final float y, final float pressure) {
            mySpot.deactivate(x, y, pressure);
        }
    }

    public Spot() {
        this.id = '\0';
        this.hidden = false;

    }

    public void init(){
        float[] color = ColorManager.getRealRandomColor();
		/*for(int i = 0; i < pointArray.size(); i++){
			pointArray.add(new Circle(SpotManager.CIRCLE_FRAME, color));
		}*/
        point = new Circle(SpotManager.CIRCLE_FRAME, color);
    }

    public Spot(final int id){
        this.id = id;
        this.hidden = false;
        init();
    }

    public void activate(final float x, final float y, final float pressure){
        update(x, y, pressure);
    }

    public void update(final float x, final float y, final float pressure){
        this.active = true;
        point.set(x, y, pressure);
    }

    public void deactivate(final float x, final float y, final float pressure){
        this.active = false;
    }

    public boolean isActive(){
        return (!this.hidden && (this.active ));
    }

    public int getId(){
        return this.id;
    }


    public static void setDefaultHidden(boolean value){
        defaultHiden = value;
    }

    @Override
    public boolean equals(Object o){
        if(o == null)
            return false;

        if(o.getClass() != this.getClass())
            return false;

        Spot s = (Spot) o;

        if(s.getId() != this.id)
            return false;

        return true;
    }

    public void draw(float[] mvpMatrix, int mProgram) {
        float alpha = 0.8f;
        point.draw(mvpMatrix, alpha, mProgram);

		/*for(Circle c : pointArray){
			c.draw(mvpMatrix, alpha);
			alpha-=1.0f/NR_OF_CIRCLES;
		}*/
    }

}