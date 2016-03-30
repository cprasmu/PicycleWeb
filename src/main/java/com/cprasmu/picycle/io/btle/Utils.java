// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Utils.java

package com.cprasmu.picycle.io.btle;

import java.io.*;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils
{

    public Utils()
    {
    }

    public static Integer getProcessID(Process p)
    {
        try
        {
            Field f = p.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            Integer s = (Integer)f.get(p);
            return s;
        }
        catch(NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch(SecurityException e)
        {
            e.printStackTrace();
        }
        catch(IllegalArgumentException e)
        {
            e.printStackTrace();
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static final void DEBUG(Logger log, StringBuffer s, Object args[])  {
        if (log.isDebugEnabled()) {
            Object aobj[];
            int j = (aobj = args).length;
            for(int i = 0; i < j; i++)
            {
                Object o = aobj[i];
                s.append(String.valueOf(o));
            }

        }
    }

    public static final void INFO(Logger log, StringBuffer s, Object args[]) {
        if(log.isInfoEnabled())
        {
            Object aobj[];
            int j = (aobj = args).length;
            for(int i = 0; i < j; i++)
            {
                Object o = aobj[i];
                s.append(String.valueOf(o));
            }

        }
    }

    public static final String formatDate(Date d)
    {
        return formater.format(d);
    }

    public static String readStreamToString(InputStream stream)
        throws IOException
    {
        BufferedReader readerStderr = new BufferedReader(new InputStreamReader(stream));
        StringBuffer error = new StringBuffer();
        String line;
        while((line = readerStderr.readLine()) != null) 
            error.append(line);
        return error.toString();
    }

    public static void sleepN(long ms)
    {
        sleepN(ms, 1000L, "");
    }

    public static void sleepN(long ms, long logTime, String msg)
    {
        boolean interrupted = false;
        for(int i = 0; (long)i * logTime < ms && !interrupted; i++)
        {
            try
            {
                Thread.sleep(logTime);
            }
            catch(InterruptedException e)
            {
                interrupted = true;
            }
            if(log.isDebugEnabled() && interrupted)
                log.debug((new StringBuilder("interrupted ")).append(msg).append(" ").append(i).append("/").append((float)ms / (float)logTime).toString());
        }

    }

    public static void sleep(long ms, long logTime)
        throws InterruptedException
    {
        for(int i = 0; (long)i < ms; i = (int)((long)i + logTime))
        {
            Thread.sleep(logTime);
            if(log.isDebugEnabled())
                log.debug((new StringBuilder("sleeping ")).append(i).append("/").append((float)ms / (float)logTime).toString());
        }

    }

    static Logger log = LoggerFactory.getLogger(com.cprasmu.picycle.io.btle.Utils.class);
    private static final SimpleDateFormat formater = new SimpleDateFormat("yy-MM-dd HH:mm:ss");

}
