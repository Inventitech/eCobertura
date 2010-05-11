/*
 * This file is part of eCobertura.
 * 
 * Copyright (c) 2010 Joachim Hofer
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
package ecobertura.ui.launching.config.filters

import java.util.{ArrayList, List => JavaList}

import scala.collection.JavaConversions._

import org.eclipse.debug.core._

import ecobertura.core.data.filters._

/**
 * Helper object for converting class filters to and from launch configuration
 * properties.
 */
object LaunchConfigurationFilters {
  def readFiltersFromLaunchConfiguration(launchConfiguration: ILaunchConfiguration) : 
      List[ClassFilter] = {
    
    val classFilterList = launchConfiguration.getAttribute("classFilters", 
        new ArrayList[String])
        
    classFilterList.asInstanceOf[List[String]].map(ClassFilter(_))
  }
  
  def addFiltersToLaunchConfiguration(filters: Iterable[ClassFilter],
      launchConfiguration: ILaunchConfigurationWorkingCopy) = {

    val javaList: JavaList[String] = new ArrayList[String]
    filters.foreach(filter => javaList.add(filter.toAttributeString))
    launchConfiguration.setAttribute("classFilters", javaList)
    launchConfiguration.doSave
  }
}