/*
 *  Copyright 2015 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 *
 */
package scouter.client.counter.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import scouter.client.counter.views.CounterTodayAllView;
import scouter.client.util.ImageUtil;

public class OpenTodayAllAction extends Action {
	public final static String ID = OpenTodayAllAction.class.getName();

	private final IWorkbenchWindow window;
	private String objType;
	private String counter;
	private int serverId;

	public OpenTodayAllAction(IWorkbenchWindow window, String label, String objType, String counter, Image image, int serverId) {
		this.window = window;
		this.objType = objType;
		this.counter = counter;
		this.serverId = serverId;

		setText(label);
		setId(ID);
		setImageDescriptor(ImageUtil.getImageDescriptor(image));

	}

	public void run() {
		if (window != null) {
			try {
				window.getActivePage().showView(
						CounterTodayAllView.ID, serverId + "&" + objType+"&"+counter, IWorkbenchPage.VIEW_ACTIVATE);
			} catch (PartInitException e) {
				MessageDialog.openError(window.getShell(), "Error", "Error opening view:" + e.getMessage());
			}
		}
	}
}
