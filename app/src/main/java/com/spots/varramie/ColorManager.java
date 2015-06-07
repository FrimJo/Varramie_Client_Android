package com.spots.varramie;

import java.util.Random;

import android.graphics.Color;

public class ColorManager {

	private static int[] collorArray = {	Color.argb(255, 77, 182, 172),
									Color.argb(255, 229, 115, 115),
									Color.argb(255, 100, 181, 246),
									Color.argb(255, 255, 183, 77),
									Color.argb(255, 186, 104, 200),
									Color.argb(255, 240, 98, 146)
									};
	private static Random random = new Random(System.currentTimeMillis());
	
	public static int getRandomColor(){
		int index = getRandomNumber(collorArray.length);
		return collorArray[index];
	}
	
	public static int getRealRandomColor(){
		return Color.argb(255, getRandomNumber(255), getRandomNumber(255), getRandomNumber(255));
	}
	
	public static int getRealRandomColorWidthAlpha(int aplpha){
		return Color.argb(aplpha, getRandomNumber(255), getRandomNumber(255), getRandomNumber(255));
	}
	
	public static int getRandomColorWithAlpha(int aplpha){
		int index = getRandomNumber(collorArray.length);
		return collorArray[index];
	}
	
	private static int getRandomNumber(int max){
		return random.nextInt(max);
	}
}
