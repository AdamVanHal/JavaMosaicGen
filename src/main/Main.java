package main;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Main {
	public static final int sizeX = 800;
	public static final int sizeY = 800;
	public static final int seedMax = 90;
	public static final int seedMin = 90;
	public static final int interations = 100;
	public static Color[][] colorArray = new Color[sizeY][sizeX];
	public static Color[][] colorArray2 = new Color[sizeY][sizeX];
	
	public static void main(String[] args) {
		//fill array with initial color
		for(int i=0;i<sizeY;i++){
			for(int j=0;j<sizeX;j++){
				colorArray[i][j] = new Color(0,0,0);
			}
		}
		
		//place initial seeds in the array
		int seedCount = (int)Math.floor(Math.random()*(seedMax-seedMin)+seedMin);
		for(int i=0;i<seedCount;i++){
			int x = (int)(Math.random()*sizeX);
			int y = (int)(Math.random()*sizeY);
//			System.out.println(x);
//			System.out.println(y);
//			System.out.println();
			colorArray[y][x]=new Color((float)Math.random(),(float)Math.random(),(float)Math.random());
		}

		//Run through the iterations averaging/spreading colors
		for(int i=0;i<interations;i++){
			if(i%50==0){System.out.println(i);}
			for(int j=0;j<sizeY;j++){
				for(int k=0;k<sizeX;k++){
					//return neighboring colors, 0 for edges
					Color north=(j==0)?new Color(0,0,0):colorArray[j-1][k];
					Color south=(j==(sizeY-1))?new Color(0,0,0):colorArray[j+1][k];
					Color east=(k==(sizeX-1))?new Color(0,0,0):colorArray[j][k+1];
					Color west=(k==0)?new Color(0,0,0):colorArray[j][k-1];
					Color self=colorArray[j][k];
					
					//get corners too
					Color northE=(j==0||k==(sizeX-1))?new Color(0,0,0):colorArray[j-1][k+1];
					Color southE=(j==(sizeY-1)||k==(sizeX-1))?new Color(0,0,0):colorArray[j+1][k+1];
					Color southW=(j==(sizeY-1)||k==0)?new Color(0,0,0):colorArray[j+1][k-1];
					Color northW=(j==0||k==0)?new Color(0,0,0):colorArray[j-1][k-1];
					
					//place in array
					Color[] inputs = {self,north,east,south,west,northE,southE,southW,northW};
					if(Math.random()>0.3) {//0.3 makes close to circles
						colorArray2[j][k] = betterAverage(self, north, east, south, west);
					}else {
						colorArray2[j][k] = diagonalAverage(inputs);
					}
				}
			}
			//copy array back
			for(int l=0;l<sizeY;l++){
				for(int m=0;m<sizeX;m++){
					colorArray[l][m]=colorArray2[l][m];
				}
			}
		}

		//copy array into image file
		BufferedImage image = new BufferedImage(sizeX,sizeY,BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<sizeY;i++){
			for(int j=0;j<sizeX;j++){
				int color =200*(256*256*256);//set alpha  
				color += colorArray[i][j].getRed()*(256*256); //set red
				color += colorArray[i][j].getGreen()*(256); //set green
				color += colorArray[i][j].getBlue(); //set blue
				image.setRGB(j, i, color);
			}
		}

		//save image file
		try{
			File outputFile = new File("result.png");
			ImageIO.write(image, "png", outputFile);
			Desktop dt = Desktop.getDesktop();
			dt.open(outputFile);
		}catch(IOException e){
			
		}
		
		
		
	}
	
	//color averaging that takes into account gamma
	public static Color betterAverage(Color cSelf, Color north, Color east, Color south, Color west) {
		final double gamma = 2.2; // 2.2 is roughly the value of sRGB gamma
		int count = 0;
		int red = 0;
		int green = 0;
		int blue = 0;
		//find number of non black colors
		if(cSelf.getRed()!=0 && cSelf.getGreen()!=0 && cSelf.getBlue()!=0) {
			count++;
		}
		if(north.getRed()!=0 && north.getGreen()!=0 && north.getBlue()!=0) {
			count++;
		}
		if(east.getRed()!=0 && east.getGreen()!=0 && east.getBlue()!=0) {
			count++;
		}
		if(south.getRed()!=0 && south.getGreen()!=0 && south.getBlue()!=0) {
			count++;
		}
		if(west.getRed()!=0 && west.getGreen()!=0 && west.getBlue()!=0) {
			count++;
		}
		
		//because division by zero is a pain
		if(count == 0) {
			return new Color(0,0,0);
		}
		//red
		//adding and averaging the gamma corrected values
		double temp = (Math.pow(cSelf.getRed(), gamma) + Math.pow(north.getRed(), gamma) + Math.pow(east.getRed(), gamma) + Math.pow(south.getRed(), gamma) + Math.pow(west.getRed(), gamma));
		temp  = temp/count;
		//gamma correcting the new color and saving as an int
		red = (int) Math.round(Math.pow(temp, (1/gamma)));
		//green
		//adding and averaging the gamma corrected values
		temp = (Math.pow(cSelf.getGreen(), gamma) + Math.pow(north.getGreen(), gamma) + Math.pow(east.getGreen(), gamma) + Math.pow(south.getGreen(), gamma) + Math.pow(west.getGreen(), gamma));
		temp  = temp/count;
		//gamma correcting the new color and saving as an int
		green = (int) Math.round( Math.pow(temp, (1/gamma)));
		//blue
		//adding and averaging the gamma corrected values
		temp = (Math.pow(cSelf.getBlue(), gamma) + Math.pow(north.getBlue(), gamma) + Math.pow(east.getBlue(), gamma) + Math.pow(south.getBlue(), gamma) + Math.pow(west.getBlue(), gamma));
		temp  = temp/count;
		//gamma correcting the new color and saving as an int
		blue = (int) Math.round( Math.pow(temp, (1/gamma)));
		
		//in case rounding has placed us above 255 on accident
		red=(red>255)?255:red;
		green=(green>255)?255:green;
		blue=(blue>255)?255:blue;
		//System.out.println(red +" "+ green +" "+ blue);
		Color value = new Color(0,0,0);
		try {
			value = new Color(red,green,blue);
		}catch(Exception e) {
			System.out.println(e);
			System.out.println(red +" "+ green +" "+ blue);
			System.out.println(count);
		}
		return value;
	}
	
	
	
	
	
	//diagonal color averaging with gamma
	//color array should be self, N,E,S,W, NE,SE,SW,NW
	//colors on diagonal are weighted less
	public static Color diagonalAverage(Color[] colorArray) {
		final double gamma = 2.2; // 2.2 is roughly the value of sRGB gamma
		final double diagWeight = 0.707; //value to multiply corner values by
		double count = 0;
		int red = 0;
		int green = 0;
		int blue = 0;
		
		//find number of non-zero colors
		for(int i=0;i<colorArray.length;i++) {
			if(colorArray[i].getRed()!=0 && colorArray[i].getGreen()!=0 && colorArray[i].getBlue()!=0) {
				if(i<5) {
					count++;
				}else {
					count=count+diagWeight;
				}
			}
		}
		if(count==0) {
			return new Color(0,0,0);
		}
		
		//add up weighted colors with gamma correction
		double tempRed=0, tempGreen=0, tempBlue = 0;
		
		for(int i=0; i<colorArray.length; i++) {
			if(i<5) {
				tempRed=tempRed+Math.pow(colorArray[i].getRed(),gamma);
				tempGreen=tempGreen+Math.pow(colorArray[i].getGreen(),gamma);
				tempBlue=tempBlue+Math.pow(colorArray[i].getBlue(),gamma);
			}else {
				tempRed=tempRed+Math.pow(colorArray[i].getRed(),gamma)*diagWeight;
				tempGreen=tempGreen+Math.pow(colorArray[i].getGreen(),gamma)*diagWeight;
				tempBlue=tempBlue+Math.pow(colorArray[i].getBlue(),gamma)*diagWeight;
			}
		}
		//divide by number of colors
		tempRed=tempRed/count;
		tempGreen=tempGreen/count;
		tempBlue=tempBlue/count;
		//convert to ints
		red = (int) Math.round(Math.pow(tempRed, (1/gamma)));
		green = (int) Math.round(Math.pow(tempGreen, (1/gamma)));
		blue = (int) Math.round(Math.pow(tempBlue, (1/gamma)));
		
		//in case rounding has placed us above 255 on accident
		red=(red>255)?255:red;
		green=(green>255)?255:green;
		blue=(blue>255)?255:blue;
		
		return new Color(red,green,blue);
	}

	
	
	
	
	
	
	
	//function to decide new color value from old and surrounding
	public static Color average(Color cSelf, Color north, Color east, Color south, Color west){
		int count = 0;
		int red = 0;// (north.getRed()+east.getRed()+south.getRed()+west.getRed())/4;
		int green =0;// (north.getGreen()+east.getGreen()+south.getGreen()+west.getGreen())/4;
		int blue =0;// (north.getBlue()+east.getBlue()+south.getBlue()+west.getBlue())/4;
		if(cSelf.getRed()!=0){
			count++;
			red+=cSelf.getRed();
		}
		if(north.getRed()!=0){
			count++;
			red+=north.getRed();
		}
		if(east.getRed()!=0){
			count++;
			red+=east.getRed();
		}
		if(south.getRed()!=0){
			count++;
			red+=south.getRed();
		}
		if(west.getRed()!=0){
			count++;
			red+=west.getRed();
		}
		if(count==0){
			red = 0;
		}else{
			red = (int) Math.round(((red+0.1)/(count)));
		}
		
		count = 0;
		if(cSelf.getGreen()!=0){
			count++;
			green+=cSelf.getGreen();
		}
		if(north.getGreen()!=0){
			count++;
			green+=north.getGreen();
		}
		if(east.getGreen()!=0){
			count++;
			green+=east.getGreen();
		}
		if(south.getGreen()!=0){
			count++;
			green+=south.getGreen();
		}
		if(west.getGreen()!=0){
			count++;
			green+=west.getGreen();
		}
		if(count==0){
			green = 0;
		}else{
			green = (int) Math.round(((green+0.1)/(count)));
		}
		
		count = 0;
		if(cSelf.getBlue()!=0){
			count++;
			blue+=cSelf.getBlue();
		}
		if(north.getBlue()!=0){
			count++;
			blue+=north.getBlue();
		}
		if(east.getBlue()!=0){
			count++;
			blue+=east.getBlue();
		}
		if(south.getBlue()!=0){
			count++;
			blue+=south.getBlue();
		}
		if(west.getBlue()!=0){
			count++;
			blue+=west.getBlue();
		}
		if(count==0){
			blue = 0;
		}else{
			blue = (int) Math.round(((blue+0.1)/(count)));
		}
		
		red=(red>255)?255:red;
		green=(green>255)?255:green;
		blue=(blue>255)?255:blue;
		return new Color(red,green,blue);
	}

}
