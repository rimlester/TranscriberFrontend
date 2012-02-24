/****************
Wave file Convertor
Author: Rupert Lester
Purpose: Provides utilities to roughly edit wave files

Wave File Format: (letter after size indicate endianess)
Byte Offset:	Size:	Purpose:
RIFF HEADER
0				4b		ChunkID(should be "RIFF" in ASCII)
4				4l		Chunk Size: File size, short 8 bytes
8				4b		Format: Contains 0x57415645 ("WAVE")
12				4b		Subchunk1ID: Contains 0x666d7420
16				4l		Subchunk1Size: Should be 16 for PCM
						Contains the rest of the details
20				2l		AudioFormat: should be 1(PCM)
22				2l		#Channels
24				4l		Sample Rate
28				4l		Byte Rate (Sample Rate * #Channels *
									Bits per Sample/8)
32				2l		Block Align (#Channels *
									Bits per Sample/8)
34				2l		Bits Per Sample
DATA SUBCHUNK
36				4b		Subchunk2ID: Contains 0x64617461
40				4l 		SubChunk2Size:	(#Samples*#Channels *
										Bits per Sample/8)
44				N/Al	Data: Start of sound data

If there are multiple channels, then samples will come in pairs:
e.g. 78 e2 f5 e9 would give a left channel sample of (78 e2)
right of (f5 e9). 

******************/

import java.io.*;

public class Convertor {
	private File inputFile;
	private File ouputFile;
	private byte[] inputArray;
	
	private final static int CHUNK_SIZE_OFFSET = 4;
	private final static int CHUNK_SIZE_SIZE = 4;
	private final static int AUDIO_FORMAT_OFFSET = 20;
	private final static int AUDIO_FORMAT_SIZE = 2;
	private final static int NUM_CHANNELS_OFFSET = 22;
	private final static int NUM_CHANNELS_SIZE = 2;
	private final static int SAMPLE_RATE_OFFSET = 24;
	private final static int SAMPLE_RATE_SIZE = 4;
	private final static int BYTE_RATE_OFFSET = 28;
	private final static int BYTE_RATE_SIZE = 4;
	private final static int BITS_PER_SAMPLE_OFFSET = 34;
	private final static int BITS_PER_SAMPLE_SIZE= 2;
	private final static int DATA_SIZE_OFFSET = 40;
	private final static int DATA_SIZE_SIZE = 4;
	private final static int START_SOUND_DATA = 44;
	
	
	
	public void read(File in) throws IOException{ //read the wave file in to inputArray
		inputFile = in;
		InputStream strm = new FileInputStream(in);
		long sze = in.length();
		inputArray = new byte[(int)sze];
		int cnt = 0;
		while(cnt < sze){
			cnt += strm.read(inputArray, cnt, (int)(sze-cnt));
		}
	}
	public void writeFile(int targetSampleRateHz, int targetBitDepth, int targetChannels, Frame[] source){ //TODO
		
	}
	
	//These methods WILL NOT WORK if read has not been called.
	public int getChunkSize(){
		return byteArraySecToInt(inputArray, CHUNK_SIZE_OFFSET, CHUNK_SIZE_SIZE);
	}
	public int getBitsPerSample(){
		return byteArraySecToInt(inputArray, BITS_PER_SAMPLE_OFFSET, BITS_PER_SAMPLE_SIZE);
	}
	
	public int getSampleRateHz(){
		return byteArraySecToInt(inputArray, SAMPLE_RATE_OFFSET, SAMPLE_RATE_SIZE);
	}
	
	public int getNumChannels(){
		return byteArraySecToInt(inputArray, NUM_CHANNELS_OFFSET, NUM_CHANNELS_SIZE);
	}
	
	public int getByteRate(){
		return byteArraySecToInt(inputArray, BYTE_RATE_OFFSET, BYTE_RATE_SIZE);
	}
	public int getAudioFormat(){
		return byteArraySecToInt(inputArray, AUDIO_FORMAT_OFFSET, AUDIO_FORMAT_SIZE);
	}

	public int getDataSize(){
		return byteArraySecToInt(inputArray, DATA_SIZE_OFFSET, DATA_SIZE_SIZE);
	}
	
	private static int byteArrayToInt(byte[] arr){
		int ret = 0;
		for(int i = 0; i < arr.length; i++){
			ret += ((arr[i] & 0xff) << (i*8)); //masks the signing byte in the array and shifts it to the left
		}
		return ret;
	}
	private static int byteArraySecToInt(byte[] inp, int offset, int size){
		byte[] subArr = new byte[size];
		for(int i = 0; i < size; i++){
			subArr[i] = inp[i+offset];
		}
		return byteArrayToInt(subArr);
	}
	public void printResults(){
		System.out.println("Sample Rate: " + getSampleRateHz() + 
							"Hz  #Channels: " + getNumChannels() + 
							"  Bits Per Sample: " + getBitsPerSample()+ 
							"  Byte Rate: " + getByteRate() + 
							"  Format: " + getAudioFormat() +
							"  Chunk Size (first chunk): " + getChunkSize());
	}
	public byte[] sampled(int newRate, int newDepth, int targetChannel, int useChannel){ //New Sample Rate, How many channels post processing,
																		//Which channel to keep if stripping a channel
		int currentRate = getSampleRateHz();
		int currentBitsPerSample = getBitsPerSample();
		int currentChannels = getNumChannels();
		int currentDataSize = getDataSize();
		byte[] dataArr = new byte[getDataSize()]; //sound data
		byte[] headerArr = new byte[44]; //Header and first two fields of data subchunk
		for(int i = 0; i < getChunkSize()+8; i++){
			if(i < START_SOUND_DATA){
				headerArr[i] = inputArray[i];
			}else if (i >= START_SOUND_DATA){
				dataArr[i] = inputArray[i];
			}
		}
		Frame[] sampleArray = null;
		fillFrameArray(sampleArray, dataArr, getBitsPerSample()/8);
		if(targetChannel <	getNumChannels()){
			sampleArray = stripChannel(sampleArray, useChannel);
			currentChannels--;
		}else if(targetChannel > getNumChannels()){
			sampleArray = addChannel(sampleArray); //note that add channel simply duplicates the existing mono
			currentChannels++;
		}
		if(newRate < getSampleRateHz()){
			if(currentChannels == 2){
				Frame[] left = getChannel(sampleArray, 0);
				Frame[] right = getChannel(sampleArray, 0);
				left = downSample(left, (double)getSampleRateHz()/newRate);
				right = downSample(right, (double)getSampleRateHz()/newRate);
				sampleArray = interlace(left, right);
			}else{
				sampleArray = downSample(sampleArray, (double)(getSampleRateHz()/newRate));
			}
		}
		else if(newRate > getSampleRateHz()){
			if(currentChannels==2){
				Frame[] left = getChannel(sampleArray, 0);
				Frame[] right = getChannel(sampleArray, 0);
				left = upSample(left, (double)(newRate/getSampleRateHz()));
				right = upSample(right, (double)(newRate/getSampleRateHz()));
				sampleArray = interlace(left, right);
			}
			sampleArray = upSample(sampleArray, (double)(newRate/getSampleRateHz()));
		}
		int currentByteRate = getSampleRateHz()*(getBitsPerSample()/8);
		setChannelsInfo(headerArr, currentChannels);
		setBitsPerSampleInfo(headerArr, currentBitsPerSample);
		setByteRateInfo(headerArr, currentByteRate);
		setDataSizeInfo(headerArr, currentDataSize);
		setRateInfo(headerArr, currentByteRate);
		return null; //TEMPORARY: fixing compile errors
	}
	
	private Frame[] downSample(Frame[] data, double factor){ 
		Frame[] rVal = new Frame[(int)(data.length/factor)];
		int cnt = 0;
		for(int i = 0; i < data.length; i++){
			if(!(i%factor <= .001)){
				rVal[cnt]=data[i];
				++cnt;
			}
		}
		return rVal;
	}
	private Frame[] upSample(Frame[] data, double factor){
		Frame[] rVal = new Frame[(int)(data.length*factor)];
		int cnt = 0;
		for(int i = 0; i < data.length; i++){
			rVal[cnt] = data[i];
			if(!(i%factor <= .001)){ 
				++cnt;
				rVal[cnt] = averageOfTwo(data[i], data[i+1]);
			}
			++cnt;
		}
		return rVal;
	}
	
	private Frame[] stripChannel(Frame[] source, int channelToStrip){ //0 for left, 1 for right
		Frame[] returnArr = new Frame[source.length/2];
		int cnt=0;
		for(int i = 1*channelToStrip; i < source.length; i+=2){
			returnArr[cnt] = source[i];
			++cnt;
		}
		return returnArr;
	}
	private Frame[] addChannel(Frame[] source){
		Frame[] returnArr = new Frame[source.length*2];
		for(int i = 0; i < source.length; i+=2){
			returnArr[i*2] = source[i];
			returnArr[i*2+1] = source[i];
		}
		return returnArr;
	}
	private Frame[] getChannel(Frame[] inp, int lr){ //0 for left, 1 for right
		Frame[] returnVal = new Frame[inp.length/2];
		int cnt = 0;
		for(int i = lr; i < returnVal.length; i+=2){
			returnVal[cnt] = inp[i];
			++cnt;
		}
		return returnVal;
	}
	private Frame[] interlace(Frame[] left, Frame right[]){
		Frame[] retVal = new Frame[left.length*2];
		for(int i = 0; i < left.length; i++){
			retVal[i*2] = left[i];
			retVal[i*2+1] = right[i];
		}
		return retVal;
	}
	
	private class Frame{
		private byte[] frame;
		private int sof;
		public Frame(int sizeOfFrame){
			frame = new byte[sizeOfFrame];
			sof=sizeOfFrame;
		}
		public Frame(int sizeOfFrame, byte[] input){
			frame = new byte[sizeOfFrame];
			setFrame(input);
			sof = sizeOfFrame;
		}
		public void setFrame(byte[] nVal){
			for(int i = 0; i < frame.length; i++){
				frame[i] = nVal[i];
			}
		}
		public byte[] getByteArray(){
			return frame;
		}
		public int getFrameSize(){
			return sof;
		}
	}
	
	private Frame averageOfTwo(Frame first, Frame second){
		byte[] f = first.getByteArray();
		byte[] s = second.getByteArray();
		byte[] rVal = new byte[f.length];
		int sizeOfFrame = first.getFrameSize();
		for(int i = 0; i < rVal.length; i++){
			rVal[i] = (byte)((int)(f[i]&0xff + s[i]&0xff)/2);
		}
		Frame r = new Frame(first.getFrameSize(), rVal);
		return r;
	}
	
	private void setChannelsInfo(byte[] input, int nval){
		setSection(input, nval, NUM_CHANNELS_OFFSET, NUM_CHANNELS_SIZE);
	}
	private void setRateInfo(byte[] input, int nval){
		setSection(input, nval, SAMPLE_RATE_OFFSET, SAMPLE_RATE_SIZE);
	}
	private void setBitsPerSampleInfo(byte[] input, int nval){
		setSection(input, nval, BITS_PER_SAMPLE_OFFSET, BITS_PER_SAMPLE_SIZE);
	}
	private void setDataSizeInfo(byte[] input, int nval){
		setSection(input, nval, DATA_SIZE_OFFSET, DATA_SIZE_SIZE);
	}
	private void setFileSizeInfo(byte[] input, int nval){
		setSection(input, nval, CHUNK_SIZE_OFFSET, CHUNK_SIZE_SIZE);
	}
	private void setByteRateInfo(byte[] input, int nval){
		setSection(input, nval, BYTE_RATE_OFFSET, BYTE_RATE_SIZE);
	}	
	private void setSection(byte[] input, int val, int offset, int size){
		byte[] nVal = new byte[size];
		for(int i = 0; i < size; i++){
			nVal[i] = (byte)(val >>> (size-(i+1))*8);
		}	
		for(int i = 0; i < size; i++){
			input[i+offset] = nVal[i];
		}
	}
	
	
	private void fillFrameArray(Frame[] frameArray, byte[] dataArray, int frameSize){
		for(int i = 0; i < dataArray.length/frameSize; i++){
			byte[] tempRead = new byte[frameSize];
			for(int j = 0; j < 3; j++){
				tempRead[j] = dataArray[i*3 + j];
			}
			frameArray[i] = new Frame(frameSize, tempRead);
		}
	}
	private byte[] frameArrayToByteArray(Frame[] input){
		int numFrames = input.length;
		int sizeOfFrame = input[0].getByteArray().length;
		byte[] ret = new byte[numFrames*sizeOfFrame];
		for(int i = 0; i < numFrames; i++){
			for(int j = 0; j < sizeOfFrame; j++){
					byte[] temp = input[i].getByteArray();
					ret[(i*sizeOfFrame) + j] = temp[j];
			}
		}
		return ret;
	}
	
}

