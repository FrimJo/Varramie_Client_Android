package com.spots.varramie;

import android.graphics.Color;

import java.util.Random;

public class ColorManager {

	private static Random random = new Random(System.currentTimeMillis());
	
	/*public static int getRandomColor(){
		int index = getRandomNumber(collorArray.length);
		return collorArray[index];
	}*/
	
	public static float[] getRealRandomColor(){
		return new float[] { getRandomNumber(), getRandomNumber(), getRandomNumber(), 0.2f };
	}
	
	/*public static int getRealRandomColorWidthAlpha(int aplpha){
		return Color.argb(aplpha, getRandomNumber(255), getRandomNumber(255), getRandomNumber(255));
	}*/
	
	/*public static int getRandomColorWithAlpha(int aplpha){
		int index = getRandomNumber(collorArray.length);
		return collorArray[index];
	}*/
	
	private static float getRandomNumber(){
		return random.nextFloat();
	}
}
