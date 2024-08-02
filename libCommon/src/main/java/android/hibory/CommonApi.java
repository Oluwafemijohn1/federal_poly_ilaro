package android.hibory;

import java.io.FileDescriptor;


public class CommonApi {
	private static CommonApi mMe = null;
	
	public CommonApi() {
	}
	
	public static CommonApi getInstance(){
		if (mMe == null){
			mMe = new CommonApi();
		}
		return mMe;
	}
	
	// gpio
	public native int setGpioMode(int pin, int mode);
	public native int setGpioDir(int pin, int dir);
	public native int setGpioPullEnable(int pin, int enable);
	public native int setGpioPullSelect(int pin, int select);
	public native int setGpioOut(int pin, int out);
	public native int getGpioIn(int pin);
	//vgp
	public native int openLdoDev();
	public native int closeLdoDev();
	public native int setVgp1On(int vol);
	public native int setVgp2On(int vol);
	public native int setVgp3On(int vol);
	public native int setVibrOn(int vol);
	public native int setVgp1Down();
	public native int setVgp2Down();
	public native int setVgp3Down();
	public native int setVibrDown();
	//serialport
	public native int openCom(String port, int baudrate,int bits,char event,int stop);
	public native int openComEx(String port, int baudrate,int bits,char event,int stop, int flags);
	public native int writeCom(int fd, byte[] buf, int sizes);
	public native int readCom(int fd, byte[] buf, int sizes);
	public native int readComEx(int fd, byte[] buf, int sizes, int sec, int usec);
	public native void closeCom(int fd);
	//spi
	public native FileDescriptor openSpi(String port, int speed, int mode, int bits);
	public native int writeSpi(int fd, byte[] buf, int sizes);
	public native int readSpi(int fd, byte[] buf, int sizes);
	public native int readSpiEx(int fd, byte[] buf, int sizes, int sec, int usec);
	public native void closeSpi();
	
	
	static {
		System.loadLibrary("Hibory_CommonApi");
	}
}
