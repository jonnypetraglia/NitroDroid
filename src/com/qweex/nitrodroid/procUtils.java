/*
Copyright (c) 2012 Qweex
Copyright (c) 2012 Jon Petraglia

This software is provided 'as-is', without any express or implied warranty. In no event will the authors be held liable for any damages arising from the use of this software.

Permission is granted to anyone to use this software for any purpose, including commercial applications, and to alter it and redistribute it freely, subject to the following restrictions:

    1. The origin of this software must not be misrepresented; you must not claim that you wrote the original software. If you use this software in a product, an acknowledgment in the product documentation would be appreciated but is not required.

    2. Altered source versions must be plainly marked as such, and must not be misrepresented as being the original software.

    3. This notice may not be removed or altered from any source distribution.
 */
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
