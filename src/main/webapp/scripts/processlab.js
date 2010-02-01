// Add a wait icon to a chart DIV
function showWait(n) {
	var image = document.createElement('IMG');
	image.id = 'wait' + n;
	image.src = "images/wait.gif";
	document.getElementById('chartDiv' + n).appendChild(image);
}

// Remove wait icon. wait(n) must have been called first
function hideWait(n) {
	var imageDiv = document.getElementById('chartDiv' + n);
	var image = document.getElementById('wait' + n);
	imageDiv.removeChild(image);
}

// Encode the a link that's the target of a drill-down event
function encodeParameters(source, data, selection, link, parameters) {
	var item = selection[0];
	var row = item.row;
	for(var i = 0; i < parameters.length; i++) {
		var p = parameters[i];
		link = link.replace(
			new RegExp('\\{' + p + '\\}', 'g'),
			escape(data.getFormattedValue(row, p)));
	}
	return link;
}

// When a double click deselects then we use the last selection
function getSelection(source, lastSelection) {
	var s = source.getSelection();
	return s.length > 0 ? s : lastSelection;
}
// Optionally add link to refresh a chart
function refresh(id,page,created,age) {
	var age = eval('data' + id + '.getTableProperty(\'Age\');');

	if(age != null) {
		var parent = 'rf' + id + '_div';
		var text = 'Data from: ' + eval('data' + id + '.getTableProperty(\'Created\');');
		var tip = 'The data for this graph is around ' + age + ' old. Click here to refresh it. ';
		tip += 'Note that regenerating the result set might be hard work!';
	    var url = 'Wiki.jsp?page=' + page + '&rf=t';
		appendLinkNode(parent,text,tip,url,'rf');
	}
}
// Create and append a link node to a given parent 
function appendLinkNode(parent,text,tip,url,cls) {
    var link = document.createElement('a');
    link.appendChild(document.createTextNode(text));
    link.setAttribute('class', cls);
    link.setAttribute('title', tip);
    link.setAttribute('href', url);
	var div = document.getElementById(parent);
	div.appendChild(link);
}
