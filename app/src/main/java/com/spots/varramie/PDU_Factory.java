package com.spots.varramie;


import java.nio.ByteBuffer;


public class PDU_Factory {

    // Int = 4 byte
    // Short = 2 byte
    // unsigned 1 byte = 0 -> 255
    // signed 1 byte = -128 -> 127

    public static byte[] touch_action(final float x, final float y, final float pressure, final byte action, final String user_id, final float vel_x, final float vel_y){
        final byte[] user_id_bytes = user_id.getBytes();
        ByteBuffer bb = ByteBuffer.allocate(23 + user_id_bytes.length);
        bb.put(action);
        bb.put((byte) '\0');
        bb.put((byte) user_id_bytes.length);
        bb.put(user_id_bytes);
        bb.putFloat(x);
        bb.putFloat(y);
        bb.putFloat(pressure);
        bb.putFloat(vel_x);
        bb.putFloat(vel_y);
        byte[] bytes = bb.array();
        bb.put(1, Checksum.calc(bytes, bytes.length));
        return bytes;
    }

    public static byte[] joinNoId(){
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.put(OpCodes.JOIN_NO_ID);
        bb.put((byte) '\0');
        byte[] bytes = bb.array();
        bb.put(1, Checksum.calc(bytes, bytes.length));
        return bb.array();
    }

    public static byte[] join(final String user_id){
        final byte[] user_id_bytes = user_id.getBytes();
        final ByteBuffer bb = ByteBuffer.allocate(3 + user_id_bytes.length );
        bb.put(OpCodes.JOIN);
        bb.put((byte) '\0');
        bb.put((byte) user_id_bytes.length);
        bb.put(user_id_bytes);
        byte[] bytes = bb.array();
        bb.put(1, Checksum.calc(bytes, bytes.length));
        return bb.array();
    }

    public static byte[] alive(final String user_id){
        final byte[] user_id_bytes = user_id.getBytes();
        final ByteBuffer bb = ByteBuffer.allocate(3 + user_id_bytes.length );
        bb.put(OpCodes.ALIVE);
        bb.put((byte) '\0');
        bb.put((byte) user_id_bytes.length);
        bb.put(user_id_bytes);
        byte[] bytes = bb.array();
        bb.put(1, Checksum.calc(bytes, bytes.length));
        return bb.array();
    }

    public static byte[] quit(final String user_id){
        final byte[] user_id_bytes = user_id.getBytes();
        final ByteBuffer bb = ByteBuffer.allocate(3 + user_id_bytes.length );
        bb.put(OpCodes.QUIT);
        bb.put((byte) '\0');
        bb.put((byte) user_id_bytes.length);
        bb.put(user_id_bytes);
        byte[] bytes = bb.array();
        bb.put(1, Checksum.calc(bytes, bytes.length));
        return bb.array();
    }

    public static byte[] notreg(){
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.put(OpCodes.NOTREG);
        bb.put((byte) '\0');
        byte[] bytes = bb.array();
        bb.put(1, Checksum.calc(bytes, bytes.length));
        return bb.array();
    }
}
