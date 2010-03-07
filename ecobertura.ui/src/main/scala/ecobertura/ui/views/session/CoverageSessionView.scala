/*
 * This file is part of eCobertura.
 * 
 * Copyright (c) 2009, 2010 Joachim Hofer
 * All rights reserved.
 *
 * eCobertura is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * eCobertura is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with eCobertura.  If not, see <http://www.gnu.org/licenses/>.
 */
package ecobertura.ui.views.session

import java.util.logging.Logger

import org.eclipse.swt.widgets._
import org.eclipse.swt.SWT;

import org.eclipse.jface.layout.TreeColumnLayout
import org.eclipse.jface.viewers._

import org.eclipse.ui._
import org.eclipse.ui.part._
import org.eclipse.ui.handlers.IHandlerService

import ecobertura.ui.util.Predef._
import ecobertura.ui.views.session.labels._

object CoverageSessionView {
	/**
	 * The ID of the view as specified by the extension.
	 */
	def ID = "ecobertura.ui.views.session.CoverageSessionView"
		
	private val logger = Logger.getLogger("ecobertura.ui.views.session") //$NON-NLS-1$
}

class CoverageSessionView extends ViewPart {
	import CoverageSessionView.logger
	
	private var viewer: TreeViewer = null

	class NameSorter extends ViewerSorter
	
	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	override def createPartControl(parent: Composite) = {
		viewer = new TreeViewer(parent, SWT.SINGLE)
		val swtTreeTable = viewer.getTree
		swtTreeTable.setHeaderVisible(true)
		swtTreeTable.setLinesVisible(true)
		
		val treeColumnLayout = new TreeColumnLayout
		parent.setLayout(treeColumnLayout)
		
		addTreeColumn("Name", SWT.LEFT, 9).setLabelProvider(new NameLabelProvider)
		addTreeColumn("Lines", SWT.RIGHT, 1).setLabelProvider(new LinesCoveredLabelProvider)
		addTreeColumn("Total", SWT.RIGHT, 1).setLabelProvider(new LinesTotalLabelProvider)
		addTreeColumn("%", SWT.RIGHT, 1).setLabelProvider(new LinesPercentageLabelProvider)
		addTreeColumn("Branches", SWT.RIGHT, 1).setLabelProvider(new BranchesCoveredLabelProvider)
		addTreeColumn("Total", SWT.RIGHT, 1).setLabelProvider(new BranchesTotalLabelProvider)
		addTreeColumn("%", SWT.RIGHT, 1).setLabelProvider(new BranchesPercentageLabelProvider)
		
		viewer.setContentProvider(CoverageSessionModel.get)
		viewer.setSorter(new NameSorter)
		viewer.setInput(CoverageSessionRoot)
		
		viewer.addDoubleClickListener { 
			event: DoubleClickEvent => {
				val handlerService = getSite.getService(classOf[IHandlerService]).asInstanceOf[IHandlerService]
				val ignore = handlerService.executeCommand(
						"ecobertura.ui.views.session.commands.openCoveredClass", null)
			}
		}
		
		CoverageSessionModel.get.addListener(new CoverageSessionListener {
			override def sessionReset = {
				logger.fine("Viewer has received sessionReset event")
				viewer.setInput(CoverageSessionRoot)
			}
		})

		// Create the help context id for the viewer's control
		// PlatformUI.getWorkbench.getHelpSystem.setHelp(viewer.getControl, "ecobertura.ui.viewer")

		def addTreeColumn(name: String, alignment: Int, weight: Int) = {
			val column = new TreeViewerColumn(viewer, alignment)
			column.getColumn.setText(name)
			column.getColumn.setAlignment(alignment)
			treeColumnLayout.setColumnData(column.getColumn, new ColumnWeightData(weight))
			
			column
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	override def setFocus = {
		viewer.getControl.setFocus
	}
	
	def selection = viewer.getSelection
}
