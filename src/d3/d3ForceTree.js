
function StaticHolder()
{
	if( !StaticHolder.ranges)
	{
		StaticHolder.ranges ={};
		StaticHolder.ordinalScales={};
		StaticHolder.colorScales = {};
		StaticHolder.labelCheckBoxes=[]; 
		StaticHolder.counter =0;
		StaticHolder.goObjects = {};
	}
	
	this.getRanges = function()
	{
		return StaticHolder.ranges;
	}
	
	this.getOrdinalScales = function()
	{
		return StaticHolder.ordinalScales;
	}
	
	this.getColorScales = function()
	{
		return StaticHolder.colorScales;
	}
	
	this.getLabelCheckBoxes= function()
	{
		return StaticHolder.labelCheckBoxes;
	}
	
	this.addGoObject = function(goObject)
	{
		StaticHolder.counter++;
		
		StaticHolder.goObjects[StaticHolder.counter] = goObject;
		
		return StaticHolder.counter;
	}
	
	this.getGoObjects = function()
	{
		return StaticHolder.goObjects;
	}
} 


// from "Secrets of the JavaScript Ninja" - Resig and Bibeault
function getElement(aDocument,name )
{
	if( ! getElement.cache) getElement.cache = {};
	
	return getElement.cache[name] = getElement.cache[name] ||
		aDocument.getElementById(name);
}

// modded from http://dotnetprof.blogspot.com/2012/11/get-querystring-values-using-javascript.html
function getQueryStrings(aWindow) {
    //Holds key:value pairs
    var queryStringColl = null;
            
    //Get querystring from url
    var requestUrl = aWindow.location.search.toString();

    if (requestUrl != '') {
    	//window.location.search returns the part of the URL 
        //that follows the ? symbol, including the ? symbol
        requestUrl = requestUrl.substring(1);

        queryStringColl = {};

        //Get key:value pairs from querystring
        var kvPairs = requestUrl.split('&');

        for (var i = 0; i < kvPairs.length; i++) {
            var kvPair = kvPairs[i].split('=');
            queryStringColl[kvPair[0]] = kvPair[1];
        }
    }

    return queryStringColl;
}

function GO(parentWindow,thisWindow,isRunFromTopWindow)
{

aDocument = parentWindow.document;
statics = parentWindow.statics;
var thisID = statics.addGoObject(this);
graphType = "scatter"
queryStrings = getQueryStrings(thisWindow)
if( queryStrings ) 
{
	var aGraphType = queryStrings["GraphType"];
	if( aGraphType != null) 
		graphType = aGraphType;
}


this.resort = function()
{
	
  	var compareChoice =  getElement(aDocument,"sortByWhat").value;
  
	nodes.sort( function(a,b) {
 					 if (a[compareChoice]< b[compareChoice])
     						return -1;
  					if (a[compareChoice]> b[compareChoice])
    					return 1;
  					return 0; } );
	
	this.setInitialPositions();
	this.redrawScreen();
	
}

// modded from http://mbostock.github.com/d3/talk/20111116/force-collapsible.html
var w,h,nodes, 
    links,
    link,
    root;
    thisContext = this;
                
    
  var firstUpdate = true;
  var reverse =false;
  var initHasRun = false;
  var firstFlatten = true;
    
  topNodes= [];
  
  var dirty = true;
  
    
var force, drag, vis;


this.reforce = function()
{
	if( force != null ) 
	{
		force.stop();
	}
	
	if( vis != null)
	{
		vis.selectAll("text").remove()
		vis.selectAll("circle.node").remove();
		vis.selectAll("line.link").remove();
	}
	
	this.setWidthAndHeight();
	
    force = d3.layout.force()
    .charge(function(d) { return d._children ? -d.numSeqs / 100 : -30; })
    .linkDistance(function(d) { return d.target._children ? 80 * (d.level-16)/16 : 30; })
    .size([w, h - 60]).gravity(getElement(aDocument,"gravitySlider").value/100)
    
    drag = force.drag().on("dragstart", function(d) { d.fixed=true; thisContext.update();});

	vis = d3.select("body").append("svg:svg")
    .attr("width", w)
    .attr("height", h)
	 
}

this.setWidthAndHeight = function()
{
	if( isRunFromTopWindow ) 
	{
		w =  thisWindow.innerWidth-300,
    	h = thisWindow.innerHeight-100;
	}
	else
	{	
		w =  thisWindow.innerWidth-25,
    	h = thisWindow.innerHeight-25;
	}
	
}


this.reVis = function() 
{
	
	this.checkForStop()
	this.setWidthAndHeight();
	this.setInitialPositions();
	vis.remove();
	this.reforce();
	dirty=true;
    this.update();
}
  
  
  this.setQuantitativeDynamicRanges = function()
  {
  		var chosen = getElement(aDocument,"colorByWhat");	
  		
  		var aRange = statics.getRanges()[chosen.value];
  		
  		if( isRunFromTopWindow ) 
  		{
	  		if( aRange == null)
	  		{
	  			getElement(aDocument,"lowQuantRange").value = "categorical";
	  			getElement(aDocument,"highQuantRange").value = "categorical";
	  			getElement(aDocument,"lowQuantRange").enabled = false;
	  			getElement(aDocument,"highQuantRange").enabled = false;
	  		}
	  		else
	  		{
	  			getElement(aDocument,"lowQuantRange").value = aRange[0];
	  			getElement(aDocument,"highQuantRange").value = aRange[1];
	  			
	  		}
  		}
  		if( ! firstUpdate ) 
			this.redrawScreen();  	
  }

  this.addDynamicMenuContent =function()
  {
  	if( ! firstFlatten || ! isRunFromTopWindow) 
  		return;
  	
  	var mySidebar = getElement(aDocument,"sidebar");
  	
   	mySidebar.innerHTML +=  "<select id=\"sortByWhat\" onchange=myGo.sort())></select>"
	
  	mySidebar.innerHTML += "<h3> Size: <h3>"
  	var selectHTML =  "<select id=\"sizeByWhat\" onchange=myGo.redrawScreen()>"
	selectHTML +=  "</select>"	
	mySidebar.innerHTML += selectHTML
	mySidebar.innerHTML += "<br>Max size: <input type=\"number\"" + 
			 " id=\"maxSize\" min=\"0\" max=\"100\" value=\"30\" onchange=myGo.redrawScreen()></input>" +
			 "<br>Min size: <input type=\"number\"" + 
			 " id=\"minSize\" min=\"0\" max=\"100\"  value=\"1\" onchange=myGo.redrawScreen()></input>"   + 
	"<br><input type=\"checkbox\"" + 
			"id=\"logSize\" onchange=myGo.redrawScreen()>log</input>"
			+"<input type=\"checkbox\"" + 
			"id=\"invertSize\" onchange=myGo.redrawScreen()>invert</input><br>"
	
	for( var propertyName in nodes[0])
  		if( propertyName != "forceTreeNodeID" && propertyName != "x" && propertyName != "y"
  				&& propertyName != "children" && propertyName != "fixed" )
  		{
  			var isNumeric = true;
  			var selectHTML = "<option value=\"" + propertyName
  				+ "\">" + propertyName   +"</option>"
  				
  				var range=[]
  				range[0] = nodes[0][propertyName];
  				range[1] = nodes[0][propertyName];
  							
  				for( var x=0;  isNumeric && x < nodes.length; x++)
  				{
  					var aVal =nodes[x][propertyName]; 
  					
  					if( ! this.isNumber(aVal))
  						isNumeric = false;
  					
  					if( aVal < range[0]) 
  						range[0] = aVal;
  						
  					if( aVal > range[1]) 
  						range[1] = aVal;
  				}
  				
  				if( isNumeric) 
  				{
  					statics.getRanges()[propertyName] = range; 
  				}
  				else
  				{
  					statics.getOrdinalScales()[propertyName] = d3.scale.ordinal();
  					statics.getColorScales()[propertyName] = d3.scale.category20b();
  				}
  				
  				getElement(aDocument,"sizeByWhat").innerHTML += selectHTML
  				getElement(aDocument,"sortByWhat").innerHTML += selectHTML
  				getElement(aDocument,"scatterX").innerHTML += selectHTML
  				getElement(aDocument,"scatterY").innerHTML += selectHTML	
  		}
	
	mySidebar.innerHTML += "<h3> Color: <h3>";
  	selectHTML =  "<select id=\"colorByWhat\" onchange=myGo.setQuantitativeDynamicRanges()>"
  	
  	selectHTML += "<option value=\"nodeDepth" + "\">" + "node depth"+"</option>"
			
	for( var propertyName in nodes[0])
  		if( propertyName != "forceTreeNodeID" && propertyName != "x" && propertyName != "y"
  				&& propertyName != "children" && propertyName != "fixed" && propertyName != "nodeDepth" )
  		{
  			selectHTML += "<option value=\"" + propertyName
  				+ "\">" + propertyName   +"</option>"
  						
  		}
  	
  	selectHTML += "<option value=\"colorByMarked" 
  				+ "\">" + "marked"+"</option>"
  	
  	selectHTML +=  "</select>"
  	mySidebar.innerHTML += selectHTML
  	mySidebar.innerHTML += "<br><input type=\"checkbox\"" + 
			"id=\"logColor\" onchange=myGo.redrawScreen()>log</input>"
	
  	mySidebar.innerHTML += "<input type=\"checkbox\" id=\"textIsBlack\"" + 
				"onchange=myGo.redrawScreen()>text always black</input>";
  	    
  	var labelHTML = "<li><a>Labels</a><ul>";
  	labelHTML += "<li><input type=\"checkbox\" id=\"labelOnlyTNodes\"" + 
			"onchange=myGo.redrawScreen() checked=true> Label only T-Nodes</input></li>"	
				
	for( var propertyName in nodes[0])
  		if( propertyName != "forceTreeNodeID" 
  			&& propertyName != "x" && propertyName != "y")
	  	{
	  		var newHTML = "<li><input type=\"checkbox\" id=\"label" + propertyName + "\"" + 
				"onchange=myGo.redrawScreen()>" + propertyName + "</input></li>";
				
			 labelHTML += newHTML;
			 statics.getLabelCheckBoxes().push("label" + propertyName );
	  	}
	  	
	  	
	labelHTML +="<li>Font Adjust <input type=\"range\" id=\"fontAdjust\""
		 labelHTML += "min=\"5\" max=\"25\" value=\"15\" onchange=myGo.redrawScreen()></input></li>"
			 labelHTML += "</ul></li>"	  	
	  	aDocument.getElementById("nav").innerHTML+= labelHTML;
  	mySidebar.innerHTML += "<h3> Filter: <h3>"
  	
  	mySidebar.innerHTML += "level: <input type=\"number\" id=\"depthFilter\" min=\"2\" " + 
  		"max=\" ranges[\"nodeDepth\"] value=2 onchange=myGo.setTopNodes()></input><br>"; 
  		
  	
  	var rangeHTML = "Depth Filter:<input type=\"range\" id=\"depthFilterRange\" min=\"0\" " + 
  	"max=\"" + topNodes.length + "\" value=\"0\" onchange=myGo.showOnlyMarked()><br></input>";
  	
    mySidebar.innerHTML+= rangeHTML;
  	this.setTopNodes();
  	
  	var aTable =""
  	
  	aTable += "<table border=1 id=\"tNodeTable\">"
	aTable +=		"<tr>"
	aTable +=		"<td>Number of Visible Nodes</td>"
	aTable +=			"<td></td>"
	aTable +=		"</tr>"
	aTable +=		"<tr>"
	aTable +=		"<td>Number of TNodes</td>"
	aTable +=		"<td></td>"
	aTable +=		"</tr>"
	aTable +=	"</table>"
  	
  	mySidebar.innerHTML+= aTable;
  
  }
  
  this.setTopNodes = function()
  {
  	topNodes= [];
  
  	for( var x =0; x < nodes.length; x++)
  	{
  		if( nodes[x].nodeDepth == getElement(aDocument,"depthFilter").value) 
  		{	
  			topNodes.push(nodes[x]);
  		}
  	}
  	
  	if( isRunFromTopWindow ) 
  		getElement(aDocument,"depthFilterRange").max = topNodes.length;
  	
  	this.showOnlyMarked();
  }
  
  this.showOnlyMarked = function()
  {
  	var aVal = getElement(aDocument,"depthFilterRange").value;
  	
  	if( aVal==0)
  	{	
  		for( var x=0; x < nodes.length; x++)
  			nodes[x].doNotShow=false;
  	}
  	else
  	{
  		for( var x=0; x < nodes.length; x++)
  			nodes[x].doNotShow=true;
  			
  		aVal = aVal -1;
  		var myNode = topNodes[aVal];
  		
  		function markSelfAndDaughters(aNode)
  		{
  			aNode.doNotShow=false;
  			
  			if( aNode.children != null)
  			{
  				for( var y=0; y < aNode.children.length;y++)
  				{
  					markSelfAndDaughters(aNode.children[y]);
  				}
  			}
  		}
  		
  		markSelfAndDaughters(myNode);
  	}
  	
  	dirty=true;
  	this.update();
  }
  
  // calls redrawAScreen on all registered listeners
  this.redrawScreen= function()
  {
  	registered = statics.getGoObjects();
  	for (id in registered)
	{	
		registered[id].redrawAScreen();
	}
  }
   
  this.redrawAScreen = function()
  {
  	getElement(aDocument,"logSize").enabled=true;
  	aBox = getElement(aDocument,"logColor").enabled=true;
  
  	/* right now these are getting stuck in the off position
  	// can't log an ordinal color scale...
  	if(  statics.getOrdinalScales()[ getElement(aDocument,"sizeByWhat").value] != null )  
  	{
  		aBox = getElement(aDocument,"logSize");
  		aBox.checked=false;
  		aBox.enabled=false;
  	}
  	else
  	{
  		getElement(aDocument,"logSize").enabled=true;
  	}
  	
  	// can't log an ordinal color scale...
  	if(  statics.getOrdinalScales()[ getElement(aDocument,"colorByWhat").value] != null )  
  	{
  		aBox = getElement(aDocument,"logColor");
  		aBox.checked=false;
  		aBox.enabled=false;
  	}
  	else
  	{
  		aBox = getElement(aDocument,"logColor").enabled=true;
  	}
  	*/
  	
  	dirty = true;
  	this.update()
  }


this.getLabelText = function(d)
{	
	if( d.marked == false && getElement(aDocument,"labelOnlyTNodes").checked  )
		return "";
	
	var returnString ="";
	
	for( var propertyName in nodes[0])
	{
		var aCheckBox = getElement(aDocument,"label" + propertyName);
		if( aCheckBox != null &&  aCheckBox.checked)
		{
			returnString += d[propertyName] + " ";
		}
	}
		
	return returnString;	
}

this.myFilterNodes = function(d)
{
	 if( ! d.doNotShow )
	 	return true;
	 	
	 return false;
}

this.myFilterLinks= function(d)
{
     if( d.source.setVisible  && d.target.setVisible)
      		return true;
      	
      return false;
      		
}

// from http://stackoverflow.com/questions/18082/validate-numbers-in-javascript-isnumeric
this.isNumber = function (n) {
  return !isNaN(parseFloat(n)) && isFinite(n);
}



this.getRadiusVal= function(d)
{
	var propToSize = getElement(aDocument,"sizeByWhat").value
	var returnVal = getElement(aDocument,"maxSize").value;
	
	// quantitative values
	if( statics.getRanges()[propToSize] != null)
	{
		if( getElement(aDocument,"logSize").checked) 
		{
			// d3's log scale yields problems with p=0 in range so we 
			// covert everything to log and feed it to the linear scale..
			// as a nice side effect, you don't have to multiply p-values by negative 1
			if( d[propToSize] >0) // a p-value of zero yields a maximum sized radius
			{
				maxScale = Math.log(statics.getRanges()[propToSize][1]) / Math.LN10; 
			
				var aScale= d3.scale.linear().domain([0,maxScale]).range([getElement(aDocument,"minSize").value,
	  					getElement(aDocument,"maxSize").value]).clamp(true);
	  			returnVal = aScale(Math.log(d[propToSize]) / Math.LN10 );
			}
		}
		else
		{
			var aScale= d3.scale.linear().domain(statics.getRanges()[propToSize]).range([getElement(aDocument,"minSize").value,
	  					getElement(aDocument,"maxSize").value]).clamp(true);
	  		returnVal = aScale(d[propToSize]);
		}
		
		
	}
	else //ordinal values 
	{
		statics.getOrdinalScales()[propToSize].range([getElement(aDocument,"minSize").value
  								,getElement(aDocument,"maxSize").value]); 
  					
		returnVal = statics.getOrdinalScales()[propToSize](d[propToSize]);
		
	}
	
	if( getElement(aDocument,"invertSize").checked ) 
	{
		returnVal = getElement(aDocument,"maxSize").value - returnVal;
	}
	
	return returnVal;
	
}
var updateNum=0;
this.update = function() 
{
	if( ! initHasRun )
		return;
 
	if( dirty ) 
	{
		console.log("Full update " + ++updateNum);
		dirty = false;
		var anyLabels = false;
		
		for( var x=0; ! anyLabels && x < statics.getLabelCheckBoxes().length; x++)
		{
			var aCheckBox = getElement(aDocument,statics.getLabelCheckBoxes()[x]);
			
			if( aCheckBox != null) 
				anyLabels = aCheckBox.checked
		}
		
		
	   	var numMarked =0;
  		var numVisible=0;
	 	for (var i = 0; i < nodes.length; i++)
	 	{
	 		nodes[i].marked= false;
	 		if( ! nodes[i].doNotShow &&  nodes[i].setVisible== true) 
	 		{
	 			nodes[i].marked = true;
		 		numVisible++;
		 		
		 		if( nodes[i].children != null) 
		 		{
		 			for( var j=0; nodes[i].marked && j < nodes[i].children.length; j++ ) 
		 			{
		 				if( ! nodes[i].children[j].doNotShow )
		 				{
		 					nodes[i].marked=false;
		 				}
		 			}
		 		}
		 		
		 		if( nodes[i].marked == true) 
		 			numMarked = numMarked + 1
	 		}
	 	}
	 	
	 	for (var i = 0; i < nodes.length; i++)
	 	{
	 		nodes[i].thisNodeColor = this.color(nodes[i]);
	 		nodes[i].thisNodeRadius = this.getRadiusVal(nodes[i]);
	 	}	
	
		vis.selectAll("text").remove()
		vis.selectAll("circle.node").remove();
		vis.selectAll("line.link").remove();
		for( var z=0; z < nodes.length; z++)
			nodes[z].setVisible=false;
		
		var filteredNodes = nodes.filter(this.myFilterNodes);	
		
		for( z=0; z < filteredNodes .length; z++)
			filteredNodes[z].setVisible=true;
		
		if( graphType == "ForceTree") 
		{
				links = d3.layout.tree().links(nodes);
		}
		
  	// Restart the force layout.
 	 
 	 if( graphType == "ForceTree"  ) 
 	 force
      .nodes(nodes)
      
      if( graphType == "ForceTree" 
      			&& ! getElement(aDocument,"hideLinks").checked )
      force.links(links)
      
      if( graphType == "ForceTree" )
      	force.start().gravity(getElement(aDocument,"gravitySlider").value/100);
  
		
	  var node = vis.selectAll("circle.node")
	      .data(filteredNodes, function(d) {return d.forceTreeNodeID; } )
	      .style("fill", function(d) { return d.thisNodeColor} )
	      .style("opacity",getElement(aDocument,"opacitySlider").value/100 );
	
	  // Enter any new nodes.
	 node.enter().append("svg:circle").on("click", this.myClick)
	      .attr("class", "node")
	      .attr("cx", function(d) { return d.x; })
	      .attr("cy", function(d) { return d.y; })
	      .attr("r", function(d) {  return d.thisNodeRadius})
	      .style("fill", function(d) { return d.thisNodeColor}).
	      style("opacity",getElement(aDocument,"opacitySlider").value/100 ) 
	     .on("mouseenter", this.myMouseEnter)
	      .on("mouseleave", this.myMouseLeave)
	      
	      if( graphType == "ForceTree"  )
	      	node.call(force.drag);
	      
	      function updateNodesLinksText()
	      {
	      
	      if( graphType == "ForceTree"  )
	      {
	      	node.attr("cx", function(d) { return d.x; })
	      .attr("cy", function(d) { return d.y; });
	      }
	      else if( graphType == "scatter"  )
	      {
	      	node.attr("cx", function(d) {   return 400 * Math.random(); })
	      .attr("cy", function(d) {   return 400 * Math.random(); });
	      }
	      	
	      if ( anyLabels )
			text.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });
			
		if( graphType == "ForceTree"  && ! getElement(aDocument,"hideLinks").checked )
		{
				link.attr("x1", function(d) { return d.source.x; })
	      .attr("x1", function(d) { return d.source.x; })
	      .attr("y1", function(d) { return d.source.y; })
	      .attr("x2", function(d) { return d.target.x; })
	      .attr("y2", function(d) { return d.target.y; });
		}
		
		  	thisContext.checkForStop();
	      }
	    
		force.on("tick", updateNodesLinksText);
		
		force.on("end", updateNodesLinksText);
	    
	      
	      	// Update the links
	      	if( graphType == "ForceTree" && ! getElement(aDocument,"hideLinks").checked )
  		link = vis.selectAll("line.link")
      .data(links.filter(this.myFilterLinks), function(d) {  return d.target.forceTreeNodeID; }
      		);
	   
	  // Enter any new links.
	  if( graphType == "ForceTree" && ! getElement(aDocument,"hideLinks").checked )
	  link.enter().insert("svg:line", ".node")
	      .attr("class", "link")
	       
	 	var table = getElement(aDocument,"tNodeTable"); //.rows[0].cells[1].item[0] = "" + numMarked ;
	 	
	 	table.rows[0].cells[1].innerHTML = "" + numVisible;
	 	
	 	var row = table.rows[1];
	 	var cell =row.cells[1];
	 	cell.innerHTML = "" + numMarked;
	 	
	 	if( anyLabels  )
  var text=vis.selectAll("text").data(filteredNodes).enter().append("svg:text").
 	attr("dx", function(d) { return 15; })
                 .attr("dy", function(d) { return ".35em"; })
		 .text( function (d) {  return thisContext.getLabelText(d); })
                 .attr("font-family", "sans-serif")
                 .attr("font-size", getElement(aDocument,"fontAdjust").value + "px")
                 .attr("fill", function(d) {return  thisContext.getTextColor(d) } )	    

 // cleanup
  if( graphType == "ForceTree" && ! getElement(aDocument,"hideLinks").checked )
  link.exit().remove();
  
  node.exit().remove();
	}
  	
  	this.checkForStop();
  	// the color choosers don't work unless they are initialized first
  	// hence they are initialized in the "section" and then moved to the appropriate menu
  	// once everything else has settled in...
	if( firstUpdate && isRunFromTopWindow) 
	{
		this.setQuantitativeDynamicRanges();
  		getElement(aDocument,"ColorSubMenu").appendChild(getElement(aDocument,"color1"));
		getElement(aDocument,"color1").style.visibility="visible";
		
		getElement(aDocument,"ColorSubMenu").appendChild(getElement(aDocument,"color2"));
		getElement(aDocument,"color2").style.visibility="visible";
		
		
	}	
  	
  	firstUpdate = false;
}

this.checkForStop =function()
{
	
	if ( graphType != "ForceTree" || ! getElement(aDocument,"animate").checked)
  		force.stop();
	
}

this.getTextColor= function(d)
{
	if(  getElement(aDocument,"textIsBlack").checked ) 
		return "#000000";
		
	var chosen = getElement(aDocument,"colorByWhat").value;
	
	if( statics.getColorScales()[chosen] != null || statics.getRanges()[chosen] != null)
		return this.color(d);
		
}

this.myMouseEnter = function(d)
{
	if (! getElement(aDocument,"mouseOverHighlights").checked)
		return;
	
	function highlightNodeAndChildren(d2)
	{
		d2.highlight=true;
	
		if( d2.children != null ) 
		{
			for(var x=0; x < d2.children.length; x++) 
			{
				highlightNodeAndChildren(d2.children[x]);
			}		
		}
	}
	
	highlightNodeAndChildren(d);
	
	dirty = true;
	thisContext.update();
}

this.myMouseLeave= function ()
{
	
	if (! getElement(aDocument,"mouseOverHighlights").checked)
		return;

	for(var x=0; x < nodes.length; x++) 
	{
		nodes[x].highlight = false;
	}

	dirty = true;
	thisContext.update();
}

this.setInitialPositions = function ()
{
	root.x = w / 2;
  	root.y = h / 2;
	
	var radius = Math.min(w,h)/2;
	
	for( var x=0; x < nodes.length; x++) 
	{
		nodes[x].x = root.x/2 + radius * Math.cos( 360.0 * x/nodes.length) ;
		nodes[x].y = radius * Math.sin( 360.0 * x/nodes.length) + root.y;
		
		if( nodes[x].x <0 ) 
			nodes[x].x =0;
			
		if( nodes[x].x >w ) 
			nodes[x].x =w;
		
		if( nodes[x].y <0 ) 
			nodes[x].y =0;
			
		if( nodes[x].y >h ) 
			nodes[x].y =h;	
		
	}
	
	root.fixed=true;
}


this.initialize = function () {
   
  
  this.flatten(root),
      
  initHasRun = true;
 	dirty = true;
   this.update();
}

this.getQuantiativeColor= function (d)
{
	var chosen = getElement(aDocument,"colorByWhat").value;
	
	var lowColor = "#" + getElement(aDocument,"quantColorLow").value;
	var highColor ="#" + getElement(aDocument,"quantColorHigh").value; 
		
	var aRange = []
	aRange.push(getElement(aDocument,"lowQuantRange").value);
	aRange.push(getElement(aDocument,"highQuantRange").value);
		
	if( lowColor > highColor) 
	{
		var temp = lowColor
		lowColor=  highColor;
		highColor = temp;
	}
		
	if( getElement(aDocument,"logColor").checked) 
	{
		aVal =d[chosen]; 
		maxScale = Math.log(aRange[1]) / Math.LN10; 
		
		if( aVal ==0)
				aVal = maxScale;
		else
			aVal = Math.log(aVal) / Math.LN10; 
			
		var aScale= d3.scale.linear().domain([0,maxScale]).range([lowColor,highColor]).clamp(true);
	  	return aScale(aVal);
	}
	else
	{
		var aScale= d3.scale.linear().domain(aRange).range([lowColor,highColor]).clamp(true);
		return aScale(d[chosen]);	
	}
		
}

this.color= function (d) 
{
	var chosen = getElement(aDocument,"colorByWhat").value;
	
	if( statics.getRanges()[chosen] != null)
		return this.getQuantiativeColor(d);
	
	if( statics.getColorScales()[chosen] != null) 
		return statics.getColorScales()[chosen]( d[chosen] );
		
	if( d._children != null)
		return  "#3182bd";  // bright blue
	
	if ( d.highlight == true) 
		return "#fd8d3c"; // orange

	if(  d.marked )
		return "#000000";  // black
		
		
	return d._children ? "#3182bd" : d.children ? "#c6dbef" : "#fd8d3c";
		
}

// Toggle children on click.
this.myClick= function (d) {

	var aValue =getElement(aDocument,"clickDoesWhat").value;
	
	if ( aValue == "deletes")
	{
		initHasRun = false;
		d.children=null;
		d._children=null;
		thisContext.initialize();
	}
	if (aValue=="collapses")
	{
		initHasRun = false;
		
		if( d._children == null)
		{
			d._children = d.children;
			d.children =null;
		}
		else
		{
			d.children = d._children 
			d._children = null;
		}
		
		thisContext.initialize();
	}
	else if ( aValue == "hides")
	{
		reverse = ! reverse;
		
		if( reverse == false)
		{
			for( var x =0; x < nodes.length; x++)
				nodes[x].doNotShow=false;
		}
		else
		{
			for( var x =0; x < nodes.length; x++)
				nodes[x].doNotShow=true;
		
			thisContext.highlightAllChildren(d);
			thisContext.highlightAllParents(d);
		}
		
	}
	
	dirty=true;
	thisContext.update();	
}



this.highlightAllChildren = function (d)
{
	if( d== null)
		return;

	if( ! d.children || d.children == null)
		return;	

	d.doNotShow = false;
	for( var x=0; x < d.children.length; x++) 
	{
		this.highlightAllChildren(d.children[x]);
	}
}

this.highlightAllParents = function (d)
{
	if ( d== null)
		return;

	d.doNotShow = false;
	if( ! d.aParentNode ||  d.aParentNode != null)
	{
		thisContext.highlightAllParents(d.aParentNode);
	}
}


// Returns a list of all nodes under the root.
this.flatten= function () 
{
  var myNodes = [];
  var level =0;
  
  function addNodeAndChildren( aNode) 
	{
		level++;
		if( aNode != null) 
		{
			aNode.nodeDepth = level;
			myNodes.push(aNode);
	
			if( aNode.children != null)
				for( var x=0; x < aNode.children.length; x++)
				{
					addNodeAndChildren(aNode.children[x])
					aNode.children[x].aParentNode = aNode;
				}
					
		}
		level--;
			
	}
  
  addNodeAndChildren(root);
  
  for( var i=0; i < myNodes.length; i++)
  {
  	if (!myNodes[i].forceTreeNodeID) myNodes[i].forceTreeNodeID = i+1;
  }
  
  nodes = myNodes;  
  
  if( firstFlatten) 
  {
  		this.setInitialPositions();
  		this.addDynamicMenuContent();
  		firstFlatten = false;
  		
  }
}

if( isRunFromTopWindow ) 
{

	getElement(aDocument,"color1").style.visibility="hidden";
	getElement(aDocument,"color2").style.visibility="hidden";
}

this.reforce();

d3.json("testOperon.json", function(json) 
{
  root = json;
  root.fixed = true;
  thisContext.initialize();
});
}
