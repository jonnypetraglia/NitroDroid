package com.qweex.nitrodroid;

public class procUtils
{
	private static double CPUFreq = -2;
	public static double getCPUFreq()
	{
		if(CPUFreq!=-2)
			return CPUFreq;
		try {
		byte buff[] = new byte[80];
		Runtime.getRuntime().exec("cat /proc/cpuinfo").getInputStream().read(buff);
		for(int i=0; i<buff.length; i++)
			if(buff[i]=='B')
			{
				while(buff[i++]!=':' && i<80);
				int j=i+1;
				while(buff[i++]!='\n');
				return CPUFreq=Double.parseDouble(new String(buff, j, i-j));
			}
	} catch(Exception e) {}
		return -1;
	}
}
