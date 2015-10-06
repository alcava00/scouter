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
package scouter.client.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import scouter.client.Activator;
import scouter.client.M;
import scouter.client.model.TextProxy;
import scouter.client.util.UIUtil;
import scouter.util.CastUtil;


public class CachingPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	IPreferenceStore store;
	
	GridData gdata;
	
	Text service, sql, method, error, subcall, object, referer, userAgent, group, sql_tables;
	
	int serviceCache, sqlCache, methodCache, errorCache, subcallCache, objectCache, refererCache, userAgentCache, groupCache, sqlTablesCache;
	
	public CachingPreferencePage() {
		super();
		noDefaultAndApplyButton();
		setDescription(M.PREFERENCE_EXPAND_TEXTCACHE);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
	
	@Override
	protected Control createContents(Composite parent) {
		
		((GridLayout)parent.getLayout()).marginBottom = 30;
		
		Group layoutGroup = new Group(parent, SWT.NONE);
	    layoutGroup.setText("Text Cache Size Setting");
	    
		layoutGroup.setLayout(UIUtil.formLayout(5, 5));
		gdata = new GridData();
		gdata.horizontalAlignment = SWT.FILL;		
		layoutGroup.setLayoutData(gdata);
	    
		
		Label serviceLbl = new Label(layoutGroup, SWT.NONE);
		serviceLbl.setText("Service Text Cache :");
		serviceLbl.setLayoutData(UIUtil.formData(null, -1, 0, 10, null, -1, null, -1, 160));
		
		service = new Text(layoutGroup, SWT.BORDER);
		service.setText(""+serviceCache);
		service.setLayoutData(UIUtil.formData(serviceLbl, 10, 0, 8, 100, -5, null, -1));
		service.addFocusListener(listener);
		
		
		Label sqlLbl = new Label(layoutGroup, SWT.NONE);
		sqlLbl.setText("Sql Text Cache :");
		sqlLbl.setLayoutData(UIUtil.formData(null, -1, serviceLbl, 10, null, -1, null, -1, 160));
		
		sql = new Text(layoutGroup, SWT.BORDER);
		sql.setText(""+sqlCache);
		sql.setLayoutData(UIUtil.formData(sqlLbl, 10, serviceLbl, 8, 100, -5, null, -1));
		sql.addFocusListener(listener);
		
		
		Label methodLbl = new Label(layoutGroup, SWT.NONE);
		methodLbl.setText("Method Text Cache :");
		methodLbl.setLayoutData(UIUtil.formData(null, -1, sqlLbl, 10, null, -1, null, -1, 160));
		
		method = new Text(layoutGroup, SWT.BORDER);
		method.setText(""+methodCache);
		method.setLayoutData(UIUtil.formData(methodLbl, 10, sqlLbl, 8, 100, -5, null, -1));
		method.addFocusListener(listener);
		
		
		Label errorLbl = new Label(layoutGroup, SWT.NONE);
		errorLbl.setText("Error Text Cache :");
		errorLbl.setLayoutData(UIUtil.formData(null, -1, methodLbl, 10, null, -1, null, -1, 160));
		
		error = new Text(layoutGroup, SWT.BORDER);
		error.setText(""+errorCache);
		error.setLayoutData(UIUtil.formData(errorLbl, 10, methodLbl, 8, 100, -5, null, -1));
		error.addFocusListener(listener);
		
		
		Label subcallLbl = new Label(layoutGroup, SWT.NONE);
		subcallLbl.setText("Subcall Text Cache :");
		subcallLbl.setLayoutData(UIUtil.formData(null, -1, errorLbl, 10, null, -1, null, -1, 160));
		
		subcall = new Text(layoutGroup, SWT.BORDER);
		subcall.setText(""+subcallCache);
		subcall.setLayoutData(UIUtil.formData(subcallLbl, 10, errorLbl, 8, 100, -5, null, -1));
		subcall.addFocusListener(listener);
		
		
		Label objectLbl = new Label(layoutGroup, SWT.NONE);
		objectLbl.setText("Object Text Cache :");
		objectLbl.setLayoutData(UIUtil.formData(null, -1, subcallLbl, 10, null, -1, null, -1, 160));
		
		object = new Text(layoutGroup, SWT.BORDER);
		object.setText(""+objectCache);
		object.setLayoutData(UIUtil.formData(objectLbl, 10, subcallLbl, 8, 100, -5, null, -1));
		object.addFocusListener(listener);
		
		
		Label refererLbl = new Label(layoutGroup, SWT.NONE);
		refererLbl.setText("Referer Text Cache :");
		refererLbl.setLayoutData(UIUtil.formData(null, -1, objectLbl, 10, null, -1, null, -1, 160));
		
		referer = new Text(layoutGroup, SWT.BORDER);
		referer.setText(""+refererCache);
		referer.setLayoutData(UIUtil.formData(refererLbl, 10, objectLbl, 8, 100, -5, null, -1));
		referer.addFocusListener(listener);
		
		
		Label userAgentLbl = new Label(layoutGroup, SWT.NONE);
		userAgentLbl.setText("User Agent Text Cache :");
		userAgentLbl.setLayoutData(UIUtil.formData(null, -1, refererLbl, 10, null, -1, null, -1, 160));
		
		userAgent = new Text(layoutGroup, SWT.BORDER);
		userAgent.setText(""+userAgentCache);
		userAgent.setLayoutData(UIUtil.formData(userAgentLbl, 10, refererLbl, 8, 100, -5, null, -1));
		userAgent.addFocusListener(listener);
		
		Label groupLbl = new Label(layoutGroup, SWT.NONE);
		groupLbl.setText("Service Group Text Cache :");
		groupLbl.setLayoutData(UIUtil.formData(null, -1, userAgentLbl, 10, null, -1, null, -1, 160));
		
		group = new Text(layoutGroup, SWT.BORDER);
		group.setText(""+groupCache);
		group.setLayoutData(UIUtil.formData(groupLbl, 10, userAgentLbl, 8, 100, -5, null, -1));
		group.addFocusListener(listener);
		
		Label sqlTableLbl = new Label(layoutGroup, SWT.NONE);
		sqlTableLbl.setText("SQL Tables Text Cache :");
		sqlTableLbl.setLayoutData(UIUtil.formData(null, -1, groupLbl, 10, null, -1, null, -1, 160));
		
		sql_tables = new Text(layoutGroup, SWT.BORDER);
		sql_tables.setText(""+sqlTablesCache);
		sql_tables.setLayoutData(UIUtil.formData(sqlTableLbl, 10, groupLbl, 8, 100, -5, null, -1));
		sql_tables.addFocusListener(listener);
		
		return super.createContents(parent);
	}
	
	FocusListener listener = new FocusListener() {
		public void focusLost(FocusEvent e) {
		}
		public void focusGained(FocusEvent e) {
			((Text)e.widget).selectAll();
		}
	};

	public void init(IWorkbench workbench) {
		serviceCache = TextProxy.service.getLimit();
		sqlCache = TextProxy.sql.getLimit();
		methodCache = TextProxy.method.getLimit();
		errorCache = TextProxy.error.getLimit();
		subcallCache = TextProxy.apicall.getLimit();
		objectCache = TextProxy.object.getLimit();
		refererCache = TextProxy.referer.getLimit();
		userAgentCache = TextProxy.userAgent.getLimit();
		groupCache = TextProxy.group.getLimit();
		sqlTablesCache = TextProxy.sql_tables.getLimit(); 
	}
	
	public boolean performOk() {
		TextProxy.service.setLimit(CastUtil.cint(service.getText()));
		TextProxy.sql.setLimit(CastUtil.cint(sql.getText()));
		TextProxy.method.setLimit(CastUtil.cint(method.getText()));
		TextProxy.error.setLimit(CastUtil.cint(error.getText()));
		TextProxy.apicall.setLimit(CastUtil.cint(subcall.getText()));
		TextProxy.object.setLimit(CastUtil.cint(object.getText()));
		TextProxy.referer.setLimit(CastUtil.cint(referer.getText()));
		TextProxy.userAgent.setLimit(CastUtil.cint(userAgent.getText()));
		TextProxy.group.setLimit(CastUtil.cint(group.getText()));
		TextProxy.sql_tables.setLimit(CastUtil.cint(sql_tables.getText()));
		return true;
	}

	protected void createFieldEditors() {
		// TODO Auto-generated method stub
		
	}
	
}