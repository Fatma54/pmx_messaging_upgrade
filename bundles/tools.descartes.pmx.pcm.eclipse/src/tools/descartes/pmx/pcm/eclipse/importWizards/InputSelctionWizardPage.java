/**
 * ==============================================
 *  PMX : Performance Model eXtractor
 * ==============================================
 *
 * (c) Copyright 2014-2015, by Juergen Walter and Contributors.
 *
 * Project Info:   http://descartes.tools/pmx
 *
 * All rights reserved. This software is made available under the terms of the
 * Eclipse Public License (EPL) v1.0 as published by the Eclipse Foundation
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This software is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse Public License (EPL)
 * for more details.
 *
 * You should have received a copy of the Eclipse Public License (EPL)
 * along with this software; if not visit http://www.eclipse.org or write to
 * Eclipse Foundation, Inc., 308 SW First Avenue, Suite 110, Portland, 97204 USA
 * Email: license (at) eclipse.org
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 */
package tools.descartes.pmx.pcm.eclipse.importWizards;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class InputSelctionWizardPage extends WizardPage {
	private static InputSelctionWizardPage singleton = new InputSelctionWizardPage();
	public static Text inputDirectory;
    Display display = Display.getDefault();
    Label label1;
    Label label2;
    Label label3;
    
	private InputSelctionWizardPage() {
		super("Enter Trace Location Information");
		setTitle("Enter Trace Location Information");
		setDescription("Define the directory of the Kieker monitoring logs you want to extract a performance model from.");
	}

	public void createControl_old(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);

		label1 = new Label(container, GridData.FILL_HORIZONTAL);
		label1.setText("Enter inputDirectory to Kieker files here:");

//		label2 = new Label(composite, GridData.FILL_HORIZONTAL);
//		Image logo_image = new Image(display,"C:\\Users\\Jürgen\\git\\pmx\\tools.descartes.pmx.pcm.eclipse"+File.separatorChar+"pmx_logo.png");
//		label2.setImage(logo_image);

		inputDirectory = new Text(container, SWT.BORDER | SWT.SINGLE);

		label3 = new Label(container, SWT.NONE);
		label3.setText("waiting for input ...");
		label3.setLayoutData(gd);
		
		inputDirectory.setLayoutData(gd);

		inputDirectory.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}
			@Override
			public void keyReleased(KeyEvent e) {
				if (!inputDirectory.getText().isEmpty()) {
					if ((new File(inputDirectory.getText())).isDirectory()){
						label3.setText("input is a directory ...");
						OutputSelectionWizardPage.setOutputDirectory(inputDirectory.getText() + File.separatorChar+"pcm"+File.separatorChar);
						setPageComplete(true);
					}else{
						label3.setText("input is no directory ...");
						setPageComplete(false);
					}
		        }else{
		            setPageComplete(false);        	
		        }
			}
		});

		inputDirectory.setText("");
//		inputDirectory.setText("C:\\Users\\Jürgen\\git\\pmx\\tools.descartes.pmx.console\\console\\kieker-20141103-190203561-UTC-j2eeservice-KIEKER-TEST");
//		inputDirectory.setText("C:\\Users\\Jürgen\\Desktop\\eclipse versions\\pcm3.5.0\\workspace\\abc\\testFiles\\kieker-20151008-145457358-UTC-WIN-JQHNDE89VN4-KIEKER");
		
		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);

		label1 = new Label(container, GridData.FILL_HORIZONTAL);
		label1.setText("Enter inputDirectory to Kieker files here:");

//		label2 = new Label(composite, GridData.FILL_HORIZONTAL);
//		Image logo_image = new Image(display,"C:\\Users\\Jürgen\\git\\pmx\\tools.descartes.pmx.pcm.eclipse"+File.separatorChar+"pmx_logo.png");
//		label2.setImage(logo_image);

		inputDirectory = new Text(container, SWT.BORDER | SWT.SINGLE);
		label3 = new Label(container, SWT.NONE);
		label3.setText("waiting for input ...");
		label3.setLayoutData(gd);
		
		inputDirectory.setLayoutData(gd);

		inputDirectory.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}
			@Override
			public void keyReleased(KeyEvent e) {
				if (!inputDirectory.getText().isEmpty()) {
					testDirectoryInput();
		        }else{
		            setPageComplete(false);        	
		        }
			}
			public void testDirectoryInput() {
				if ((new File(inputDirectory.getText())).isDirectory()){
					label3.setText("input is a directory ...");
					OutputSelectionWizardPage.setOutputDirectory(inputDirectory.getText() + File.separatorChar+"pcm"+File.separatorChar);
					setPageComplete(true);
				}else{
					label3.setText("input is no directory ...");
					setPageComplete(false);
				}
			}
		});

		inputDirectory.setText("");

		Button browseButton = new Button(container, SWT.PUSH);
	    browseButton.setText("Browse");
	    browseButton.addSelectionListener(new SelectionListener() {
	    	@Override
	    	public void widgetSelected(SelectionEvent arg0) {
	    		DirectoryDialog fileDialog = new DirectoryDialog(container.getShell(), SWT.OPEN);         
	    		fileDialog.setText("Open directory of Kieker trace file");
	    		inputDirectory.setText(fileDialog.open()+File.separator);
				label3.setText("input is a directory ...");
				OutputSelectionWizardPage.setOutputDirectory(inputDirectory.getText() + File.separatorChar+"pcm"+File.separatorChar);
				setPageComplete(true);	    	}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
	    });
	    
		setControl(container);
		setPageComplete(false);
	}
	
	public static String getInputDirectory() {
		return inputDirectory.getText();
	}

	public static WizardPage getSingleton() {
		return singleton;
	}
	
	

}
