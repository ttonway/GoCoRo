package com.wcare.android.gocoro.bluetooth;

import android.os.Build;
import android.util.Log;

public class DeviceDependency {
	public static boolean shouldUseSecure() {
		if (Build.MANUFACTURER.equals("Xiaomi")) {
			if (Build.MODEL.equals("2013022") && Build.VERSION.RELEASE.equals("4.2.1")) {
				return true;
			}
		}
		if (Build.MODEL.equals("Lenovo A820")) {
			return true;
		}
		return false;
	}
	
	public static boolean isMTK() {
		if (Build.MODEL.equals("Lenovo A820")) {
			return true;
		}
		return false;
	}
	
	public static boolean shouldUseFixChannel() {
		if (Build.VERSION.RELEASE.startsWith("4.0.")) {
			if (Build.MANUFACTURER.equals("samsung")) {
				return true;
			}
			if (Build.MANUFACTURER.equals("HTC")) {
				return true;
			}
			if (Build.MANUFACTURER.equals("Sony")) {
				return true;
			}			
		}
		if (Build.VERSION.RELEASE.startsWith("4.1.")) {
			if (Build.MANUFACTURER.equals("samsung")) {
				return true;
			}
		}
		if (Build.MANUFACTURER.equals("Xiaomi")) {
			if (Build.VERSION.RELEASE.equals("2.3.5")) {
				return true;
			}
		}
		return false;
	}

	public static void Print() {
	    String  ANDROID         =   Build.VERSION.RELEASE;       //The current development codename, or the string "REL" if this is a release build.
	    String  BOARD           =   Build.BOARD;                 //The name of the underlying board, like "goldfish".
	    String  BOOTLOADER      =   Build.BOOTLOADER;            //  The system bootloader version number.
	    String  BRAND           =   Build.BRAND;                 //The brand (e.g., carrier) the software is customized for, if any.
	    String  CPU_ABI         =   Build.CPU_ABI;               //The name of the instruction set (CPU type + ABI convention) of native code.
	    String  CPU_ABI2        =   Build.CPU_ABI2;              //  The name of the second instruction set (CPU type + ABI convention) of native code.
	    String  DEVICE          =   Build.DEVICE;                //  The name of the industrial design.
	    String  DISPLAY         =   Build.DISPLAY;               //A build ID string meant for displaying to the user
	    String  FINGERPRINT     =   Build.FINGERPRINT;           //A string that uniquely identifies this build.
	    String  HARDWARE        =   Build.HARDWARE;              //The name of the hardware (from the kernel command line or /proc).
	    String  HOST            =   Build.HOST;
	    String  ID              =   Build.ID;                    //Either a changelist number, or a label like "M4-rc20".
	    String  MANUFACTURER    =   Build.MANUFACTURER;          //The manufacturer of the product/hardware.
	    String  MODEL           =   Build.MODEL;                 //The end-user-visible name for the end product.
	    String  PRODUCT         =   Build.PRODUCT;               //The name of the overall product.
	    String  TAGS            =   Build.TAGS;                  //Comma-separated tags describing the build, like "unsigned,debug".
	    String  TYPE            =   Build.TYPE;                  //The type of build, like "user" or "eng".
	    String  USER            =   Build.USER;                  //
	    Log.i("Device Information", "ANDROID = " + ANDROID + " BOARD = " + BOARD + " BOOTLOADER = " +BOOTLOADER+
	    		" BRAND = "+BRAND + " CPU_ABI = " + CPU_ABI + " CPU_ABI2 = " + CPU_ABI2 + " DEVICE = " +DEVICE +
	    		" DISPLAY = "+DISPLAY +" FINGERPRINT = "+FINGERPRINT + " HARDWARE = " +HARDWARE + " HOST = "+HOST+
	    		" ID = " +ID + " MANUFACTURER = " + MANUFACTURER + " MODEL = " + MODEL + " PRODUCT = "+PRODUCT +
	    		" TAGS = " +TAGS + " TYPE = " +TYPE +" USER = " +USER);	
	}
}
