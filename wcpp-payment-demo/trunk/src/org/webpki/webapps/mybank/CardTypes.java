package org.webpki.webapps.mybank;

import java.awt.Color;

import java.awt.image.BufferedImage;

import java.io.IOException;

import javax.imageio.ImageIO;

public enum CardTypes
  {
	SUPER   ("supercard.png",   Color.BLUE), 
	COOL    ("coolcard.png",    Color.BLACK),
	UNUSUAL ("unusualcard.png", Color.GRAY);
	
	BufferedImage image;
	Color color;
	
	CardTypes (String file, Color color)
	  {
		try 
		  {
			image = ImageIO.read (CardTypes.class.getResourceAsStream (file));
	      }
		catch (IOException e)
		  {
			throw new RuntimeException (e);
		  }
		this.color = color;
	  }
  }