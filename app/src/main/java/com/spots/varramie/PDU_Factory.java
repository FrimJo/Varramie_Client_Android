package com.spots.varramie;


import java.nio.ByteBuffer;


public class PDU_Factory {

	// Int = 4 byte
	// Short = 2 byte
	// unsigned 1 byte = 0 -> 255
	// signed 1 byte = -128 -> 127
	public static byte[] touch_action(final float x, final float y, final byte action, final int id){
		ByteBuffer bb = ByteBuffer.allocate(11);
		bb.put(0, action);
		bb.put(1, (byte) '\0');
		bb.put(2, (byte) id);
		bb.putFloat(3, x);
		bb.putFloat(7, y);
		byte[] bytes = bb.array();
		bb.put(1, Checksum.calc(bytes, bytes.length));
		return bytes;
	}

	public static byte[] join(){
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.put(0, OpCodes.JOIN);
		bb.put(1, (byte) '\0');
		byte[] bytes = bb.array();
		bb.put(1, Checksum.calc(bytes, bytes.length));
		return bb.array();
	}

	public static byte[] join(final int id){
		ByteBuffer bb = ByteBuffer.allocate(3);
		bb.put(0, OpCodes.JOIN);
		bb.put(1, (byte) '\0');
		bb.put(2, (byte) id);
		byte[] bytes = bb.array();
		bb.put(1, Checksum.calc(bytes, bytes.length));
		return bb.array();
	}

	public static byte[] alive(final int id){
		ByteBuffer bb  = ByteBuffer.allocate(3);
		bb.put(0, OpCodes.ALIVE);
		bb.put(1, (byte) '\0');
		bb.put(2, (byte) id);
		byte[] bytes = bb.array();
		bb.put(1, Checksum.calc(bytes, bytes.length));
		return bb.array();
	}


	public static byte[] quit(final int id){
		ByteBuffer bb  = ByteBuffer.allocate(3);
		bb.put(0, OpCodes.QUIT);
		bb.put(1, (byte) '\0');
		bb.put(2, (byte) id);
		byte[] bytes = bb.array();
		bb.put(1, Checksum.calc(bytes, bytes.length));
		return bb.array();
	}

	public static byte[] notreg(){
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.put(0, OpCodes.NOTREG);
		bb.put(1, (byte) '\0');
		byte[] bytes = bb.array();
		bb.put(1, Checksum.calc(bytes, bytes.length));
		return bb.array();
	}
}

