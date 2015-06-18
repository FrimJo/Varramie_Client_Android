package com.spots.liquidfun;

import org.jbox2d.dynamics.BodyDef;

public class BodyQueueDef {

  private int actorID;
  private BodyDef bd;

  public BodyQueueDef(int _actorID, BodyDef _bd) {
    bd = _bd;
    actorID = _actorID;
  }

  public int getActorID() { return actorID; }
  public BodyDef getBd() { return bd; }
}