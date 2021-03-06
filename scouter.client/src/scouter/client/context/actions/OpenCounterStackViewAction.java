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
package scouter.client.context.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import scouter.client.Images;
import scouter.client.util.ImageUtil;
import scouter.client.views.CounterMapStackView;

public class OpenCounterStackViewAction extends Action {
	public final static String ID = OpenCounterStackViewAction.class.getName();

	private final IWorkbenchWindow win;
	private int objHash;
	private int serverId;
	private String counter;
	public OpenCounterStackViewAction(IWorkbenchWindow win, String label,int serverId, int objHash, String counter) {
		this.win = win;
		this.objHash = objHash;
		this.serverId = serverId;
		this.counter = counter;
		setImageDescriptor(ImageUtil.getImageDescriptor(Images.sum));
		setText(label);
	}

	public void run() {
		if (win != null) {
			try {
				win.getActivePage().showView(CounterMapStackView.ID, serverId + "&" + objHash + "&" + counter, IWorkbenchPage.VIEW_ACTIVATE);
			} catch (PartInitException e) {
				MessageDialog.openError(win.getShell(), "Error", "Error opening view:" + e.getMessage());
			}
		}
	}
}
