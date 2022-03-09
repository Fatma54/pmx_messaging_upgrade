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
import java.util.Calendar;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class OutputSelectionWizardPage extends WizardPage {
	private Text outputDirectory;
	private Composite container;
	private static OutputSelectionWizardPage singleton = new OutputSelectionWizardPage();

	public static OutputSelectionWizardPage getSingleton() {
		return singleton;
	}

	private OutputSelectionWizardPage() {
		super("Enter Output Information");
		setTitle("Enter Output Information");
		setDescription("Select the output directory.");
		setControl(outputDirectory);
	}

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 1;
		Label label1 = new Label(container, SWT.NONE);
		label1.setText("Output path");
		outputDirectory = new Text(container, SWT.BORDER | SWT.SINGLE);

		Button newProjectButton = new Button(container, SWT.PUSH);
		newProjectButton.setText("create project in workspace");
		newProjectButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setOutputDirectory(createProjectInWorkspace("PerformanceModeleXtractor") + File.separator + "pcm"
						+ Calendar.getInstance().getTimeInMillis() + File.separator);
				new File(getOutputDirectory()).mkdirs();
				setPageComplete(true);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		outputDirectory.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (!outputDirectory.getText().isEmpty()) {
					setPageComplete(true);
				} else {
					setPageComplete(false);
				}
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		outputDirectory.setLayoutData(gd);

		addAvertisement(container);
		// required to avoid an error in the system
		setControl(container);
		setPageComplete(false);
	}

	private String createProjectInWorkspace(String name) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(name);
		try {
			if(!project.exists()){
				project.create(null);
				project.open(null);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return project.getLocation().toOSString();
	}

	private static void addAvertisement(Composite composite) {
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		Label label = new Label(composite, SWT.NONE);

		String cite = "\n@inproceedings{WaHoKoOkKo2016-ICPE-DPE_Vision, "
				+ "\n  author = {J\"{u}rgen Walter and Andre van Hoorn "
				+ "\n  and Heiko Koziolek and Dusan Okanovic and Samuel Kounev},"
				+ "\n  title = {{Asking ``What?'', Automating the ``How?'':"
				+ "\n  The Vision of Declarative Performance Engineering}},"
				+ "\n  booktitle = {{Proceedings of the 7th ACM/SPEC International"
				+ "\n  Conference on Performance Engineering (ICPE 2016)}}," + "\n  year = {2016},"
				+ "\n  location = {Delft, the Netherlands}," + "\n  month = {March}, day = {12}}";

		label.setText("Please cite us:" + cite);

		label.setLayoutData(gd);

		Browser browser = new Browser(composite, SWT.NONE);
		browser.setSize(1200, 1200);
		browser.setUrl("http://se.informatik.uni-wuerzburg.de/tools/pmx/");
	}

	public static void setOutputDirectory(String dir) {
		(new File(dir)).mkdirs();
		singleton.outputDirectory.setText(dir);
		// outputDirectory.getListeners(0).setPageComplete(true);
		singleton.setPageComplete(true);
	}

	public static String getOutputDirectory() {
		return singleton.outputDirectory.getText();
	}

}