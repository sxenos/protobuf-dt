/*
 * Copyright (c) 2011 Google Inc.
 *
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.google.eclipse.protobuf.ui.preferences.pages.paths;

import static com.google.eclipse.protobuf.ui.preferences.binding.BindingToButtonSelection.bindSelectionOf;
import static com.google.eclipse.protobuf.ui.preferences.pages.paths.Messages.*;
import static com.google.eclipse.protobuf.ui.swt.EventListeners.addSelectionListener;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.eclipse.xtext.util.Strings.*;

import java.util.*;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.xtext.ui.PluginImageHelper;

import com.google.eclipse.protobuf.ui.preferences.*;
import com.google.eclipse.protobuf.ui.preferences.binding.*;
import com.google.eclipse.protobuf.ui.preferences.pages.PreferenceAndPropertyPage;
import com.google.inject.Inject;

/**
 * Preference page for import paths.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
public class PathsPreferencePage extends PreferenceAndPropertyPage {

  private static final String COMMA_DELIMITER = ",";
  private static final String PREFERENCE_PAGE_ID = PathsPreferencePage.class.getName();

  private Group grpResolutionOfImported;
  private Button btnOneDirectoryOnly;
  private Button btnMultipleDirectories;
  private DirectoryPathsEditor directoryPathsEditor;

  @Inject private PluginImageHelper imageHelper;

  @Override protected Composite contentParent(Composite parent) {
    Composite contents = new Composite(parent, SWT.NONE);
    contents.setLayout(new GridLayout(3, false));
    return contents;
  }

  @Override protected void doCreateContents(Composite parent) {
    // generated by WindowBuilder
    grpResolutionOfImported = new Group(parent, SWT.NONE);
    grpResolutionOfImported.setLayout(new GridLayout(1, false));
    grpResolutionOfImported.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
    grpResolutionOfImported.setText(importedFilesPathResolution);

    btnOneDirectoryOnly = new Button(grpResolutionOfImported, SWT.RADIO);
    btnOneDirectoryOnly.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
    btnOneDirectoryOnly.setText(filesInOneDirectoryOnly);

    btnMultipleDirectories = new Button(grpResolutionOfImported, SWT.RADIO);
    btnMultipleDirectories.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
    btnMultipleDirectories.setText(filesInMultipleDirectories);

    directoryPathsEditor = new DirectoryPathsEditor(grpResolutionOfImported, project(), imageHelper);
    directoryPathsEditor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
    new Label(parent, SWT.NONE);

    addEventListeners();
  }

  private void addEventListeners() {
    addSelectionListener(new SelectionAdapter() {
      @Override public void widgetSelected(SelectionEvent e) {
        boolean selected = btnMultipleDirectories.getSelection();
        directoryPathsEditor.setEnabled(selected);
        checkState();
      }
    }, asList(btnOneDirectoryOnly, btnMultipleDirectories));
    directoryPathsEditor.setDataChangedListener(new DataChangedListener() {
      public void dataChanged() {
        checkState();
      }
    });
  }

  private void checkState() {
    if (directoryPathsEditor.isEnabled() && directoryPathsEditor.directoryPaths().isEmpty()) {
      pageIsNowInvalid(errorNoDirectoryNames);
      return;
    }
    pageIsNowValid();
  }

  @Override protected void setupBinding(PreferenceBinder preferenceBinder) {
    RawPreferences preferences = new RawPreferences(getPreferenceStore());
    preferenceBinder.addAll(
        bindSelectionOf(btnOneDirectoryOnly).to(preferences.filesInOneDirectoryOnly()),
        bindSelectionOf(btnMultipleDirectories).to(preferences.filesInMultipleDirectories())
      );
    final StringPreference directoryPaths = preferences.directoryPaths();
    preferenceBinder.add(new Binding() {
      public void applyPreferenceValueToTarget() {
        setDirectoryPaths(directoryPaths.value());
      }

      public void applyDefaultPreferenceValueToTarget() {
        setDirectoryPaths(directoryPaths.defaultValue());
      }

      public void savePreferenceValue() {
        directoryPaths.value(directoryNames());
      }
    });
  }

  private String directoryNames() {
    List<DirectoryPath> paths = directoryPathsEditor.directoryPaths();
    if (paths.isEmpty()) return "";
    List<String> pathsAsText = new ArrayList<String>();
    for (DirectoryPath path : paths) {
      pathsAsText.add(path.toString());
    }
    return concat(COMMA_DELIMITER, pathsAsText);
  }

  private void setDirectoryPaths(String directoryPaths) {
    List<DirectoryPath> paths = new ArrayList<DirectoryPath>();
    for (String path : split(directoryPaths, COMMA_DELIMITER)) {
      if (isEmpty(path)) continue;
      paths.add(DirectoryPath.parse(path));
    }
    directoryPathsEditor.directoryPaths(unmodifiableList(paths));
  }

  @Override protected void onPageCreation() {
    defaultsPerformed();
  }

  /** {@inheritDoc} */
  @Override protected void onProjectSettingsActivation(boolean active) {
    enableProjectOptions(active);
  }

  @Override protected void defaultsPerformed() {
    enableProjectOptions(true);
  }

  private void enableProjectOptions(boolean enabled) {
    grpResolutionOfImported.setEnabled(enabled);
    btnOneDirectoryOnly.setEnabled(enabled);
    btnMultipleDirectories.setEnabled(enabled);
    directoryPathsEditor.setEnabled(btnMultipleDirectories.getSelection() && enabled);
  }

  /** {@inheritDoc} */
  @Override protected String preferencePageId() {
    return PREFERENCE_PAGE_ID;
  }
}
