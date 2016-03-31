/*
 * Copyright (c) 2004-2010, P. Simon Tuffs (simon@simontuffs.com)
 * All rights reserved.
 *
 * See the full license at http://one-jar.sourceforge.net/one-jar-license.html
 * This license is also included in the distributions of this software
 * under doc/one-jar-license.txt
 */
package bd.amazed.pdfscissors.main;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import bd.amazed.pdfscissors.model.Model;
import bd.amazed.pdfscissors.view.MainFrame;
import bd.amazed.pdfscissors.view.OpenDialog;

public class PdfscissorsMain {

	public static void main(String args[]) {
		if (args == null)
			args = new String[0];
		new PdfscissorsMain().run(args);
	}

	public void run(final String args[]) {
		setLookAndFeel();
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				
				//variables for command-line parameters
				boolean isArgsPresent = false;
				boolean isExtOpenMode = false;
				File path = null;
				
				//evaluate command line
				Pattern extModePattern = Pattern.compile("-e", Pattern.CASE_INSENSITIVE);

//				//TODO arguments for setting page-grouping
//				Pattern pageGroupAllModePattern = Pattern.compile(			"-group=all", Pattern.CASE_INSENSITIVE);
//				Pattern pageGroupEvenAndOddModePattern = Pattern.compile(	"-group=evenandodd", Pattern.CASE_INSENSITIVE);
//				Pattern pageGroupSingleModePattern = Pattern.compile(		"-group=single", Pattern.CASE_INSENSITIVE);
				
				for(String str : args){
					
					if(extModePattern.matcher(str).matches()){
						
						isArgsPresent = true;
						
						//enable extended file-open dialog
						isExtOpenMode = true;
						
					}
					else {
						
						//try argument as file-path
						path = new File(str);
						if(!path.exists()){
							path = null;
						} else {
							isArgsPresent = true;
						}
						
					}
				}

//				System.out.printf("args: %s  %n%n", Arrays.toString(args));
//				System.out.printf("args: %s  %n", isExtOpenMode? "EXT_MODE":"NORMAL_MODE");
//				System.out.printf("args: %s  %n", path!=null? "file "+path.getAbsolutePath() : "NO_FILE");
				

				MainFrame main = new MainFrame();
				main.setVisible(true);
				
				//apply command-line options (if present)
				
				if(isArgsPresent){
					
					boolean defaultCreateStackView = OpenDialog.getStackedViewSetting();
					if(path != null){
						
						int defaultGroupType = OpenDialog.getPageGroupTypeSetting();
						
						if(!isExtOpenMode){
							//directly open the file
							main.openFile(path, defaultGroupType, defaultCreateStackView);
						}
						else {
							//open file-dialog with extend options
							Model.getInstance().getProperties().setProperty(Model.PROPERTY_LAST_FILE, path.getAbsolutePath());
							main.showFileOpenDialog(true);
						}
					}
					else if(!isExtOpenMode){
						//open file-dialog with extend options
						main.showFileOpenDialog(true);
					}
				}
			}
		});
	}

	private static void setLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException ex) {
			Logger.getLogger(PdfscissorsMain.class.getName()).log(Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			Logger.getLogger(PdfscissorsMain.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			Logger.getLogger(PdfscissorsMain.class.getName()).log(Level.SEVERE, null, ex);
		} catch (UnsupportedLookAndFeelException ex) {
			Logger.getLogger(PdfscissorsMain.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
