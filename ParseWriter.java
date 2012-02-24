
import java.io.*;
import javax.swing.*;

public class ParseWriter{
	PrintWriter myWrite;
	int theArea;
	float progress; //global representing current time in transcription
	public void parse(String inp){
		System.out.println("Called");
		String word = "";
		String nume = "";
		int i = 0, c = 0; //c will be used as a count of elements in the following two arrays
		String[] Nume = new String[64];
		String[] Word = new String[64];
		while(i < inp.length()){
			word = word + inp.charAt(i);
			if (inp.charAt(i+1) == '('){
				Word[c] = new String(word);
				i+=2; /* since typical format is: string(#.##,#.###) string(#.###,#.##) */
				while(inp.charAt(i) != ','){ //dirty parsing of the lines. 
					nume = nume + inp.charAt(i);
					i++;
				}
				while(inp.charAt(i++) != ')');
				Nume[c] = new String(nume);
				c++;
				word = "";
				nume = "";
			}
			i++;
		}
		writelines(Nume, Word, c);
		progress = (c>0 ? new Float(Nume[c-1]) : 1); //c can equal zero if there was no input, giving an
		Nume = new String[64];						//array index of -1
		Word = new String[64];
	}
	ParseWriter(String filename,int area) throws IOException{
		myWrite = new PrintWriter(new FileWriter(filename));
		progress = 1;
		theArea = area;
	}
	private void writelines(String[] N, String[] W, int cnt){ /* Writes to file, formate is "timestamp: worddetected\n"  */
		for(int i = 0; i < cnt; i++){
			myWrite.println(N[i] + " " + W[i]);
			Frontend.appendArea((N[i]+": " + W[i] + "\n"), theArea);
			myWrite.flush();
		}
	}
	public void close(){ //accessor for close method
		myWrite.close();
	}
}