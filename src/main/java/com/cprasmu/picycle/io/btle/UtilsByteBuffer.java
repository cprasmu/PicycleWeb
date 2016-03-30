// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   UtilsByteBuffer.java

package com.cprasmu.picycle.io.btle;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class UtilsByteBuffer
{

    public UtilsByteBuffer()
    {
    }

    public static short getUnsignedByte(ByteBuffer bb)
    {
        return (short)(bb.get() & 0xff);
    }

    public static void putUnsignedByte(ByteBuffer bb, int value)
    {
        bb.put((byte)(value & 0xff));
    }

    public static short getUnsignedByte(ByteBuffer bb, int position)
    {
        return (short)(bb.get(position) & 0xff);
    }

    public static ByteBuffer putUnsignedByte(ByteBuffer bb, int position, int value)
    {
        bb.put(position, (byte)(value & 0xff));
        return bb;
    }

    public static int getUnsignedShort(ByteBuffer bb)
    {
        return bb.getShort() & 0xffff;
    }

    public static ByteBuffer putUnsignedShort(ByteBuffer bb, int value)
    {
        bb.putShort((short)(value & 0xffff));
        return bb;
    }

    public static int getUnsignedShort(ByteBuffer bb, int position)
    {
        return bb.getShort(position) & 0xffff;
    }

    public static ByteBuffer putUnsignedShort(ByteBuffer bb, int position, int value)
    {
        bb.putShort(position, (short)(value & 0xffff));
        return bb;
    }

    public static long getUnsignedInt(ByteBuffer bb)
    {
        return (long)bb.getInt() & 0xffffffffL;
    }

    public static ByteBuffer putUnsignedInt(ByteBuffer bb, long value)
    {
        bb.putInt((int)(value & 0xffffffffL));
        return bb;
    }

    public static long getUnsignedInt(ByteBuffer bb, int position)
    {
        return (long)bb.getInt(position) & 0xffffffffL;
    }

    public static ByteBuffer putUnsignedInt(ByteBuffer bb, int position, long value)
    {
        bb.putInt(position, (int)(value & 0xffffffffL));
        return bb;
    }

    public static void addHexStringToByteArray(String valuesData, int start, int end, ArrayList byteLine)
    {
        for(int i = start; i < end; i += 2)
        {
            byte b = (byte)((Character.digit(valuesData.charAt(i), 16) << 4) + Character.digit(valuesData.charAt(i + 1), 16));
            byteLine.add(Byte.valueOf(b));
        }

    }

    public static byte[] convertHexStringToByteArray(String valuesData)
    {
        byte rawMessage[] = new byte[valuesData.length() / 2];
        for(int i = 0; i < valuesData.length() - 1; i += 2)
        {
            byte b = (byte)((Character.digit(valuesData.charAt(i), 16) << 4) + Character.digit(valuesData.charAt(i + 1), 16));
            rawMessage[i / 2] = b;
        }

        return rawMessage;
    }
}
