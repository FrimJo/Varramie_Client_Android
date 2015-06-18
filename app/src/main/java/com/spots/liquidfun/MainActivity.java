package com.spots.liquidfun;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;

public class MainActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    GLSurfaceView view = new GLSurfaceView(this);
    view.setEGLContextClientVersion(2);
    Renderer renderer = new Renderer();
    view.setRenderer(renderer);

    setContentView(view);
  }
}
