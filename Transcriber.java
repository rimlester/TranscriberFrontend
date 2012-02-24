/*
 * Copyright 1999-2004 Carnegie Mellon University.
 * Portions Copyright 2004 Sun Microsystems, Inc.
 * Portions Copyright 2004 Mitsubishi Electric Research Laboratories.
 * All Rights Reserved.  Use is subject to license terms.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 */


import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
import edu.cmu.sphinx.recognizer.Recognizer;
import edu.cmu.sphinx.result.*;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.ConfigurationManagerUtils;
import edu.cmu.sphinx.linguist.*;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import javax.swing.*;

/** A simple example that shows how to transcribe a continuous audio file that has multiple utterances in it. */
public class Transcriber extends Thread{
	static ConfigurationManager cm;
	private String sGrammar, sLog; //sAudio defines the audio file to be used, sGrammar the grammar and log the 
	   								//plaintext transcription log
	private URL sAudio;	
	private static int target;
	public Transcriber(URL sourceAudio, String sourceGrammar, String log, int target){
		sAudio = sourceAudio;
		sGrammar = new String();
		Transcriber.target = target;
		for(int i = 0; i < (sourceGrammar.length() - 5); i++){
				Character st = sourceGrammar.charAt(i);
				sGrammar = sGrammar.concat(st.toString());
		}
		sLog = new String(log);
		
		
	}
	public void run(){
		try{decode();}
		catch(Exception e){
			System.out.println(e);
		}
	}
    public void decode() throws Exception {
		/*Timers */
		long sTime = System.currentTimeMillis();
		long eTime = 1;
		URL audioURL;
        audioURL = sAudio;
		String filename;
		filename = (sLog==null?"output.txt":sLog);
        URL configURL = Transcriber.class.getResource("config.xml");
		AudioInputStream as = AudioSystem.getAudioInputStream(audioURL);
		AudioFormat af = as.getFormat();
		long length = 1;
		try{length = new File(audioURL.toURI()).length();
		}catch(Exception e){
			System.out.println(e);
		}
		
		/* Figure out total time of audio track */
		int frameSize = af.getFrameSize();
		float frameRate = af.getFrameRate();
		float durationInSeconds = (length/((float)frameSize*frameRate));
		
		/*Configuration manager setup */
		cm = new ConfigurationManager(configURL);
        Recognizer recognizer = (Recognizer) cm.lookup("recognizer");
        /* allocate the resource necessary for the recognizer */
        recognizer.allocate();
        // configure the audio input for the recognizer
        AudioFileDataSource dataSource = (AudioFileDataSource) cm.lookup("audioFileDataSource");
        dataSource.setAudioFile(audioURL, null);
		Result result;
		ParseWriter mw = new ParseWriter(filename, target);
		float progress = 1;
		//now that the configuration has been loaded, check for user grammar
		chooseGrammar(sGrammar);
        // Loop until last utterance in the audio file has been decoded, in which case the recognizer will return null.
        while ((result = recognizer.recognize())!= null) {
			String resultText = result.getTimedBestResult(false, true); //Changed to getTimedBestResult()
			mw.parse(resultText);
			System.out.println(resultText);
			if(progress < mw.progress){
				progress = mw.progress;
				System.out.println(((progress/durationInSeconds)*100) + " % complete");
				Frontend.setProgress((int)(progress/durationInSeconds*100), target);
			}
		}
		mw.close();
		
		/*Time statistics reporting stuff*/
		eTime = System.currentTimeMillis();
		long totalTime = ((eTime - sTime)/1000); //time of running (in seconds)
		System.out.println("Finished. Total time: " + totalTime + "(" + 
							(totalTime/durationInSeconds) + " times realtime.)");
    }

	static void chooseGrammar(String nName) 
				throws Exception{
		Frontend.setProgress(25, target);
		System.out.println("Choose Grammar was called with " + nName + "...");
		Linguist linguist = (Linguist) cm.lookup("flatLinguist");
		linguist.deallocate();
		Frontend.setProgress(50, target);
		System.out.println("...linguist deallocated...");
		/*setProperty(ConfigurationManager cm, String componentName, String propName, String propValue)*/
		try{
			ConfigurationManagerUtils.setProperty(cm, "jsgfGrammar", "grammarName", nName); //grammar name should be 
		}catch(Exception e){
			System.out.println(e);
		}
		Frontend.setProgress(75, target);
		System.out.println("...Configuration Changed...");								// the filename of the second grammar sans the .gram extension
		try{
			linguist.allocate();
		}catch(Exception e){
			System.out.println(e);
		}
		Frontend.setProgress(100, target);
		System.out.println("...linuguist allocated");
	}
	
}
