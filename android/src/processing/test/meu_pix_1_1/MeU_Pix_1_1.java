package processing.test.meu_pix_1_1;

import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ketai.ui.*; 
import android.content.Intent; 
import android.os.Bundle; 
import apwidgets.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class MeU_Pix_1_1 extends PApplet {

//////////////////////////////////////////////////////////////////////////
//Filenames: MeU.pde
//Authors: Robert Tu
//Date Created: January 23, 2014
//Notes:
/*
  
  This is the Processing Android program that controls the MeU panel. It
  utilizes the android processing library, ketai library for the selection, 
  list, apwidgets for UI devices such as buttons and input boxes and Btserial
  to communicate with the arduino bluetooth. 
  
  In the main loop the program waits for a serial message sent from a mobile
  device via bluetooth. The message is then parsed for specific commands. 
  
  The message protocol is as follows:
  
  "bFFFFFFmessage\n\r"
  
  The first character is the mode command and will determine what kind of
  information will be displayed on the LED panel. Due to memory limitations
  of the Arduino Mini Pro, the only mode available is text display which is
  initiated by prefixing each command with "b". If you are sending a command 
  to the Arduino always prefix the message with "b".
  
  The next six characters are colour values in hex (RGB notation). The colour 
  of the text is determined by this value.
  
  The rest of the following characters is the actual text to be displayed on 
  the LED panel. 
  
  The \n\r characters are for the serial read andparsing function to determine 
  the end of the message.
  
  For example if the Arduino receives the following command:
  "bFF00FFHello there how are you?\n\r"
  
  MeU will display "Hello there how are you?" in magenta colour.
  
  PLEASE NOTE: The Arduino Mini only has 2K of SRAM and since the Adafruit library
  uses much of that, any remaining dynamic variables must be used
  very wisely.


*/

//////////////////////////////////////////////////////////////////////////


//import libraries


//Ketai Sensor Library for Android: http://KetaiProject.org by Daniel Sauter





//APwidgets library https://code.google.com/p/apwidgets/ by Rikard Lundstedt

//import cc.arduino.btserial.*;



//selected bluetooth mac address to communicate with
String remoteAddress = "";

BtSerial bt;

String MessageToSend = " ";
boolean SendFlag;

APWidgetContainer widgetContainer; 

APButton SendBtn;
APButton DeviceBtn;
APButton LeftBtn;
APButton RightBtn;

KetaiList selectionList;

//constants for button width and sizes
//these sizes should fit within Galaxy Note 3 resolution size


final int CTRL_BTN_W = 600;
final int BTN_H = 175;
final int DIR_BTN = 250;

//x y positions for buttons


final int X_LEFT = 240;
final int X_RIGHT = 590;

final int Y_DEV_1 = 265;
final int Y_IMG_1 = 525;
final int Y_CTRL_1 = 1195;
final int Y_SEND_1 = 1560;

final int Y_STATUS = 130;
final int Y_LABEL = 435;

String [] deviceList;
StringDict imageList;

String selectedFile= "";

// timer variables
long savedTime;
int delayValue = 500;

PImage DisplayImage;
PImage tmp;
int ImageIndex = 0;

//APToggleButton blueToothConnect;
String[] theKeys;

public void setup()
{   
  orientation(PORTRAIT);
  
  stroke(255);
  textSize(40);
  frameRate(4);
  bt = new BtSerial(this);

  imageList = new StringDict();
  imageList.set("  1 UP Mushroom","1upmushroom.png");
  imageList.set("  Twitter Logo","b-twitter-logo.gif");
  imageList.set("Boo","boo.png");
  imageList.set("Circle 1","circle1.png");
  imageList.set("Circle 2","circle2.png");
  imageList.set("Circle 3","circle3.png");
  imageList.set("Eddie","eddie.png");
  imageList.set("Facebook Logo","facebook.gif");
  imageList.set("Fire Flower","fireflower.png");
  imageList.set("Goomba","goomba.png");
  imageList.set("Green Flower","greenflower.png");
  imageList.set("Gremlin","gremlin.png");
  imageList.set("Half Moon","half-moon-icon.png");
  imageList.set("Hearts","HeartFrame4.png");
  imageList.set("Yellow Koopa","koopayellow.PNG");
  imageList.set("Mario 1","mario.gif");
  imageList.set("Mario 2","mario.png");
  imageList.set("Mega Man","megaman_1_2.gif");
  imageList.set("Mushroom","mushroom.png");
  imageList.set("Red Flower","redflower.png");
  imageList.set("Shy Guy","shyguy.png");
  imageList.set("Skull","skull.gif");
  imageList.set("Sun","sun-16.png");
  imageList.set("Twitter","twitter.gif");
  imageList.set("WWTO","WeAreWearables.gif");
  imageList.set("Yohani","yohani.png");
  
  theKeys = imageList.keyArray();
  selectedFile = "1upmushroom.png";
  
  
  tmp = loadImage(selectedFile);
  DisplayImage = new PImage(600,600);
  DisplayImage.copy(tmp, 0, 0, tmp.width, tmp.height, 0, 0, 600, 600);
  //DisplayImage.loadPixels();
  
  
  widgetContainer = new APWidgetContainer(this); //create new container for widgets
  
  DeviceBtn = new APButton(X_LEFT, Y_DEV_1, CTRL_BTN_W, BTN_H, "Device");
  LeftBtn = new APButton(X_LEFT, Y_CTRL_1, DIR_BTN, DIR_BTN, "<<");
  RightBtn = new APButton(X_RIGHT, Y_CTRL_1, DIR_BTN, DIR_BTN, ">>");
  SendBtn = new APButton(X_LEFT, Y_SEND_1, CTRL_BTN_W, BTN_H, "Send Image");
  
  widgetContainer.addWidget(DeviceBtn);
  widgetContainer.addWidget(LeftBtn);
  widgetContainer.addWidget(RightBtn);
  widgetContainer.addWidget(SendBtn);
  
  //Load drop down list
  deviceList = bt.list(true); 

}

public void draw() {
  
  background(192, 241, 252);
  fill(0);
  //println("drawing image...");
  image(DisplayImage, X_LEFT, Y_IMG_1);
  //println("image drawn");
  if (remoteAddress.equals("")){
    fill(255, 0, 0);
    text("Choose a MeU address.", X_LEFT, Y_STATUS);
  } else {
    if (SendFlag == true) {
      
      if(bt.isConnected() == true) {
        bt.write(MessageToSend);
        fill(0);
        text("Image sent!", X_LEFT, Y_STATUS);
        if ((millis() - savedTime) > delayValue) {
          //bt.disconnect();
          SendFlag = false;
        }
        
        
      } else {
        if ((millis() - savedTime) > delayValue) {
          fill(255,0,0);
          text ("Cannot connect! Check if MeU is on.", X_LEFT, Y_STATUS);
          
        } else {
          fill(0);
          text ("Sending...", X_LEFT, Y_STATUS);
          
        }
      }
    } else {
      fill(0);
      text("Choose an Image", X_LEFT, Y_STATUS);
    }
  }
  
}
  
public void onClickWidget(APWidget widget){
  
  if (widget == SendBtn) {
    if (selectedFile != "") {
      //bt.connect(remoteAddress);
      //MessageToSend ="";
      MessageToSend = assembleMessage(selectedFile);
      //MessageToSend = "b" + selectedColour + InputField.getText() + '\r' + '\n';
      SendFlag = true;
      StartTimer();
    } else {
      fill(255, 0, 0);
      text("Choose an Image", X_LEFT, Y_STATUS);
    }
    
  } else if (widget == DeviceBtn) {
    
    deviceList = bt.list(true);
    selectionList = new KetaiList(this, deviceList); 
    
  } else if (widget == LeftBtn) {
    if (ImageIndex <= 0) {
      ImageIndex = theKeys.length - 1;
    } else {
      ImageIndex--;
    }
    
    
    
  } else if (widget == RightBtn) {
    if (ImageIndex >= (theKeys.length - 1)) {
      ImageIndex = 0;
    } else {
      ImageIndex++;
    }
    
  }
  
  println(ImageIndex);
  selectedFile = imageList.get(theKeys[ImageIndex]);
  
  println(selectedFile);
  tmp = loadImage(selectedFile);
  DisplayImage.copy(tmp, 0, 0, tmp.width, tmp.height, 0, 0, 600, 600);
  
  
}

public void onKetaiListSelection(KetaiList kList) {
  //println(kList.getSelection());
  if (kList.getSelection().indexOf(":") != -1) {
    println(kList.getSelection());
    String[] DeviceInfo = split(kList.getSelection(), ',');
    remoteAddress = DeviceInfo[0];
    println(remoteAddress);
    String DeviceName = DeviceInfo[1];
    println(DeviceName);
    DeviceBtn.setText(DeviceName);
    
    if(bt.isConnected() == true) {
      bt.disconnect();
      DeviceBtn.setText("No Device");
    } else {
      int Attempts = 0;
      while (Attempts < 6) {
        try {
          bt.connect(remoteAddress);
        } catch (Exception ex) {
          println("Trying to connect...");
        }
        if (bt.isConnected() == true) {
          println("Connected");
          break;
        } else {
          Attempts++;
        }
      }
      if (bt.isConnected() == false) {
        fill(255,0,0);
        text ("Cannot connect! Check if MeU is on.", X_LEFT, Y_STATUS);
        DeviceBtn.setText("No Device");
      }
        
    }
    //conne
  } 
}

public String assembleMessage(String filename) {
  PImage img;
  
  String Message ="";
 
  img = loadImage(filename);
  img.loadPixels();
  
  for (int y = 0; y < img.height; y++) {
    for (int x = 0; x < img.width; x++) {
      int loc = x + y*img.width;
      
      // The functions red(), green(), and blue() pull out the 3 color components from a pixel.
      int r = PApplet.parseInt(red(img.pixels[loc]));
      int g = PApplet.parseInt(green(img.pixels[loc]));
      int b = PApplet.parseInt(blue(img.pixels[loc]));
      
      //build the message to send to Arduino here
      //Format will be "RRGGBBRRGGBB....RRGGBB" which will be 1536 (256*6) characters
      Message += hex(r,2) + hex(g,2) + hex(b,2);
      //println("loc: " + loc);

    }
  }
  
  
  return Message;
}
public void StartTimer() {
  savedTime = millis();
}


}
