package edu.pitt.sis.paws.cope;

import java.io.UnsupportedEncodingException;
import java.net.*;

public class UnicodeTester
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		String s1 = "Oo\u2019Brian";
		
		System.out.println(s1);
		System.out.println("Position of win quote [\u2019] = " + s1.indexOf("\u2019"));
		try
		{
			String original = "' ` Ž Õ";
			String encoded = URLEncoder.encode(original,"utf-8");
			String decoded = URLDecoder.decode(encoded, "utf-8");
			System.out.println("Original=" + original + " Encoded=" + encoded + " Decoded=" + decoded);
		}
		catch (UnsupportedEncodingException e) {}

	}

}
