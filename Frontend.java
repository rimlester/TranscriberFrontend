import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JTextPane;
import java.awt.BorderLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.JMenu;
import javax.swing.JFileChooser;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTextField;
import java.io.*;
import javax.swing.JTextArea;
import javax.swing.Box;
import javax.swing.JOptionPane;
import java.awt.GridLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JScrollPane;
import javax.swing.JProgressBar;
public class Frontend {

	private JFrame frmTranscriberGui;
	URL audioValue;
	JFileChooser fileChooser = new JFileChooser();
	private JButton button;
	private JButton button_1;
	private Transcriber trans;
	private Transcriber trans2;
	private JComboBox<String> comboBox;
	public String gramVal;
	private Thread Trns;
	private JScrollPane scrollPane;
	private JScrollPane scrollPane_1;
	private static JTextArea outputArea;
	private static JTextArea outputArea2;
	private static JProgressBar progressBar;
	private static JProgressBar progressBar_1;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Frontend window = new Frontend();
					window.frmTranscriberGui.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Frontend() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	public void initialize() {
		frmTranscriberGui = new JFrame();
		frmTranscriberGui.setTitle("Transcriber GUI");
		frmTranscriberGui.setBounds(100, 100, 542, 517);
		frmTranscriberGui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		frmTranscriberGui.getContentPane().add(panel, BorderLayout.SOUTH);
		
		JLabel lblSelectGrammar = new JLabel("Select Grammar:");
		panel.add(lblSelectGrammar);
		
		FileLister ff = new FileLister();
		
		comboBox = new JComboBox(ff.listOfFiles());
		panel.add(comboBox);
		
		JLabel lblDecodeInArea = new JLabel("Decode in area...");
		panel.add(lblDecodeInArea);
		
		button = new JButton("1");
		panel.add(button);
		button_1 = new JButton("2");
		panel.add(button_1);
		
		bListener buListener = new bListener();
		
		button.addActionListener(buListener);
		button_1.addActionListener(buListener);
		JButton selectAudioButton = new JButton("Select Audio File...");	

		alistener listen = new alistener();
		selectAudioButton.addActionListener(listen);
		frmTranscriberGui.getContentPane().add(selectAudioButton, BorderLayout.NORTH);
		
		JPanel panel_2 = new JPanel();
		frmTranscriberGui.getContentPane().add(panel_2, BorderLayout.CENTER);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{238, 35, 243, 0};
		gbl_panel_2.rowHeights = new int[]{403, 0, 0};
		gbl_panel_2.columnWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);
		
		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panel_2.add(scrollPane, gbc_scrollPane);
		
		outputArea = new JTextArea();
		scrollPane.setViewportView(outputArea);
		
		scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 2;
		gbc_scrollPane_1.gridy = 0;
		panel_2.add(scrollPane_1, gbc_scrollPane_1);
		
		outputArea2 = new JTextArea();
		scrollPane_1.setViewportView(outputArea2);
		
		progressBar = new JProgressBar();
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.insets = new Insets(0, 0, 0, 5);
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 1;
		panel_2.add(progressBar, gbc_progressBar);
		
		progressBar_1 = new JProgressBar();
		GridBagConstraints gbc_progressBar_1 = new GridBagConstraints();
		gbc_progressBar_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar_1.gridx = 2;
		gbc_progressBar_1.gridy = 1;
		panel_2.add(progressBar_1, gbc_progressBar_1);
		
		
	}
	public static void appendArea(String a, int target){
		if(target == 1){
			outputArea.append(a);
		}else if(target == 2){
			outputArea2.append(a);
		}
	}
	public static void setProgress(int value, int targetBar){
		if(targetBar == 1){
				progressBar.setValue(value);
		}else if (targetBar == 2){
				progressBar_1.setValue(value);
		}
	}
	private class alistener implements ActionListener{
			public void actionPerformed(ActionEvent e){
				int retval = fileChooser.showOpenDialog(frmTranscriberGui);
				if(retval == JFileChooser.APPROVE_OPTION){
					File nfile = fileChooser.getSelectedFile();
					try {
						audioValue = nfile.toURL();
					} catch (MalformedURLException e1) {
						e1.printStackTrace();
					}
				}
			}
	}
	private class FileLister{
		private String[] listOfFiles(){	
			File aFile = new File(System.getProperty("user.dir"));
			FilenameFilter ff = new FilenameFilter(){
				public boolean accept(File dir, String name){
					return name.endsWith(".gram");
				}
			};
			String[] retval  = aFile.list(ff);
			return retval;
		}
	}
	private class bListener implements ActionListener{
			public void actionPerformed(ActionEvent e){
				boolean worked = false;
				if (e.getSource() == button){
					worked = initiateTranscriber(1);
					if(worked){
						try{trans.start();
						}catch(Exception ex){
						System.out.println(ex);
						}
					}
				}
				if (e.getSource() == button_1){
					outputArea2.setText((String)comboBox.getSelectedItem());
					worked = initiateTranscriber(2);
					if(worked){
						try{trans2.start();
						}catch(Exception ex){
						System.out.println(ex);
						}
					}
				}
			
			}
	}
	private boolean initiateTranscriber(int targ){
		if(!(audioValue == null)){
			gramVal = (String)comboBox.getSelectedItem();
			if(targ == 1){
			trans = new Transcriber(audioValue,  gramVal, "out.txt", targ);
			}else{
				trans2 = new Transcriber(audioValue,  gramVal, "out.txt", targ);
			}	
			setProgress(5, targ);
			return true;
		}else{
			return false;
		}	
	}
}
	

