<!--- 
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 ---><cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testListContains" localMode="modern">

<!--- begin old test code --->
<cfset valueEquals(left="#ListContains('a,b,abba,bcb','bb')#", right="3")>
<cfset valueEquals(left="#ListContains('abba,bb,AABBCC','BB')#", right="3")>
<cfset valueEquals(left="#ListContains('abba,bb,AABBCC','ZZ')#", right="0")>
<cfset valueEquals(left="#ListContains(',,,,,abba,bb,AABBCC,,,,','ZZ')#", right="0")>
<cfset valueEquals(left="#ListContains('abba,,,,,bb,AABBCC','BB')#", right="3")>
<cfset valueEquals(left="#ListContains('abba,,,,,bb,AABBCC','BB',';,.')#", right="3")>



<cfset valueEquals(left="#listcontains("evaluate,expression","")#", right="0")>
<cfset valueEquals(left="#listcontains("evaluate,,expression","")#", right="0")>



<cfset valueEquals(left="#listcontains("evaluate,,expression","expression")#", right="2")>
<cfset valueEquals(left="#listcontains("evaluate, ,expression","expression")#", right="3")>
<cfset valueEquals(left="#listcontains("evaluate, ,expression","expression",',',true)#", right="3")>
<cfset valueEquals(left="#listcontains("evaluate,,expression","expression",',',true)#", right="3")>
<cfset valueEquals(left="#listcontains("evaluate, ,expression","expression",',',false)#", right="3")>
<cfset valueEquals(left="#listcontains("evaluate,,expression","expression",',',false)#", right="2")>

<cfset valueEquals(left="#listcontains("evaluate,,expression","expression",';,',false,false)#", right="2")>
<cfset valueEquals(left="#listcontains("evaluate,,expression","expression",';,',false,true)#", right="1")>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>