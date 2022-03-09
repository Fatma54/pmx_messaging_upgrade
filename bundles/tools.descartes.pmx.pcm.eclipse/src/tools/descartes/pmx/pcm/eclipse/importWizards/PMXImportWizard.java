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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import tools.descartes.pmx.pcm.console.PMXCommandLinePCM;

public class PMXImportWizard extends Wizard implements IImportWizard {
	WizardPage outputPage, inputPage;
	public PMXImportWizard() {
		super();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("PMX Import Wizard"); //NON-NLS-1
		setNeedsProgressMonitor(true);
		inputPage = InputSelctionWizardPage.getSingleton();
		outputPage = OutputSelectionWizardPage.getSingleton();
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public void addPages() {
        super.addPages(); 
        addPage(inputPage);
        addPage(outputPage);
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		String outputDirectory;
		if(null != OutputSelectionWizardPage.getOutputDirectory()){
			outputDirectory = OutputSelectionWizardPage.getOutputDirectory();
		}else{
			System.out.println("Logging into input directory");
			outputDirectory = InputSelctionWizardPage.getInputDirectory()+File.separatorChar+"pcm"+File.separatorChar;
		}
		String[] args = {"-i", InputSelctionWizardPage.inputDirectory.getText(), "-o", outputDirectory};
		

		MessageConsole console = findConsole("Console");
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		String id = IConsoleConstants.ID_CONSOLE_VIEW;
		IConsoleView view;
		try {
			view = (IConsoleView) page.showView(id);
			view.display(console);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		
		MessageConsoleStream out = console.newMessageStream();
		
		out.println("start model extraction");
		PMXCommandLinePCM.execute(args);

		out.println("finished wizard");
		out.println("results available in " + outputDirectory);
        return true;
	}    

   private MessageConsole findConsole(String name) {
	   ConsolePlugin plugin = ConsolePlugin.getDefault();
	   IConsoleManager conMan = plugin.getConsoleManager();
	   IConsole[] existing = conMan.getConsoles();
	   for (int i = 0; i < existing.length; i++){
		   if (name.equals(existing[i].getName())){
			   return (MessageConsole) existing[i];
		   }
	   }
	   //no console found, so create a new one
	   MessageConsole myConsole = new MessageConsole(name, null);
	   conMan.addConsoles(new IConsole[]{myConsole});
	   return myConsole;
   }
}
