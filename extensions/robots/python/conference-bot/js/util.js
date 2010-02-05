wrappy = {}
wrappy.util = {}
wrappy.io = {}
wrappy.ui = {}
wrappy.window = {}

wrappy.ui.createButton = function(opts) {

  function createMiddle(label) {
    var div = document.createElement('div');
    div.style.background = 'transparent url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAAAYCAYAAABKtPtEAAAAeklEQVR42u3YAQpFEQBE0dn/VqyJ5PUkEr2IpOYvZP6ts4kLYwyVgeLh3ktlOOdQGfbeVIa1FpVhzkllGGNQGb7vozL03qkMrTUqQ62VylBKoTLknKkMKSUqQ4yRyvC+L5XheR4qQwiByuC9pzI456gM1loqw/8Ki/cDaJtRS5zeSF8AAAAASUVORK5CYII=) repeat-x scroll 0 0';
    div.style.fontSize = '9pt';
    div.style.height = '24px';
    div.style.lineHeight = '24px';
    div.style.overflow = 'hidden';
    div.style.padding = '0 3px';
    div.style.whiteSpace = 'nowrap';
    div.style.cursor = 'pointer';
    div.innerHTML = label;
    return div;
  }
  function createCorner(width, background) {
    var div = document.createElement("div");
    div.style.background = background;
    div.style.height = '24px';
    div.style.overflow = 'hidden';
    div.style.width = width + 'px';
    return div;
  }

  function createRightCorner()  {
    return createCorner(5, 'transparent url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAYCAYAAAAyJzegAAAA0UlEQVR42kXRvQqDMBSG4VxB6dW3W7cuCuLoqFcgrg4i/iH+oIhiEPVrju05DTxD3kAIOcq2bViWhTAMsa7rE8BdHceBruvged51YPYPte87SFEUcF0X53lqtW0biNYadBUtRRsm0VwOJnFZFjCJ8zyDSZymCUziOI5gEodhAJPY9z2YxLZtwSQ2TQMmsa5rMPqtK1ZVBSaxLEswiXmeg0nMsgxMYpqmYBKTJAGJougf4zi+ZkOjCILgG+nBjuPA9/3NfPRiBvdW5mD/6YyXcfsA6sexoNI2z+EAAAAASUVORK5CYII=) no-repeat scroll 0 0');
  }
  function createLeftCorner()  {
    return createCorner(4, 'transparent url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAQAAAAYCAYAAADd5VyeAAAAuElEQVR42j3RzQmEMBhF0XRgydOBhShuXFiBBczKjSCiKKITFFEi/iST7wkvcMBcQ0hUKT/iOP4kSWKiKHIyCdM0/XVd52QoX2+ttbPWuud53iAP13W58zzfIEuP4wAGYwww7PsODNu2AcO6rsCwLAswzPMMDHI4wTBNEzCM4wgMwzAAQ9/3wCDXFwxt2wJD0zTAUNc1SNBy5KqqAN80yzJbFIUryxIrgjzPv/6FE/IXlN8i8ELv/gP0F0t5Kj1SCQAAAABJRU5ErkJggg==) no-repeat scroll 0 0');
  }

  function createTd() {
    var td = document.createElement('td');
    td.style.fontFamily = 'Arial, sans serif';
    td.style.fontSize = 'small';
    td.style.padding = '0px';
      return td;
  }

  if (opts.id) {
    var currentNode = document.getElementById(opts.id);
    label = currentNode.value;
  } else if (opts.node) {
    var currentNode = opts.node;
    label = currentNode.value;
  } else {
    label = opts.label;
  }
  var table = document.createElement('table');
  table.cellSpacing = '0';
  table.cellPadding = '0';
  table.style.display = 'inline-table';
  table.style.verticalAlign = 'middle';
  var tr = document.createElement('tr');
  var td = createTd();
  td.appendChild(createLeftCorner());
  tr.appendChild(td);  
  var td = createTd();
  td.appendChild(createMiddle(label));
  tr.appendChild(td);  
  var td = createTd();
  td.appendChild(createRightCorner());
  tr.appendChild(td);  
  table.appendChild(tr);

  if (currentNode) {
    var parentDiv = currentNode.parentNode;
    table.onclick = currentNode.onclick;
    table.id = currentNode.id;
    parentDiv.replaceChild(table, currentNode);
    if (currentNode.disabled) {
      table.style.opacity = '.45';
    }
  }
  return table;
}

wrappy.ui.convertButtons = function() {
  var inputs = [];
  for (i = 0; i < document.getElementsByTagName("input").length; i++) {
    var input = document.getElementsByTagName('input')[i];
    if (input.type == 'button') {
      inputs.push(input);
    }
  }
  for (var i = 0; i < inputs.length; i++) {
    wrappy.ui.createButton({node: inputs[i]});
  }
  for (i = 0; i < document.getElementsByTagName("select").length; i++) {
    var select = document.getElementsByTagName('select')[i];
    select.style.fontFamily = 'Arial, sans serif';
    select.style.fontSize = 'small';
    select.style.verticalAlign = 'middle';
  }
  wrappy.ui.addStyleRule('td', 'font-family:Arial, sans serif; font-size: small');
  wrappy.ui.addStyleRule('body', 'font-family:Arial, sans serif; font-size: small');
  wrappy.ui.addStyleRule('body', 'padding:5px; border: 1px solid #5590D2; background-color:#C9E2FC;');
  wrappy.ui.addStyleRule('.divider', 'height:3px; margin-top:5px; margin-bottom:5px; width:100%; background-color: #5590D2;');
}

wrappy.ui.addStyleRule = function(selector, declaration) {
  // test for IE
  var ua = navigator.userAgent.toLowerCase();
  var isIE = (/msie/.test(ua)) && !(/opera/.test(ua)) && (/win/.test(ua));

  // create the style node for all browsers
  var style_node = document.createElement("style");
  style_node.setAttribute("type", "text/css");
  style_node.setAttribute("media", "screen"); 

  // append a rule for good browsers
  if (!isIE) style_node.appendChild(document.createTextNode(selector + " {" + declaration + "}"));

  // append the style node
  document.getElementsByTagName("head")[0].appendChild(style_node);

  // use alternative methods for IE
  if (isIE && document.styleSheets && document.styleSheets.length > 0) {
    var last_style_node = document.styleSheets[document.styleSheets.length - 1];
    if (typeof(last_style_node.addRule) == "object") last_style_node.addRule(selector, declaration);
  }
}

wrappy.util.isDefined = function(variable) {
  if (typeof variable === 'undefined') {
    return false;
  }
  return true;
}

wrappy.util.inGadget = function() {
  if (wrappy.util.isDefined(window['gadgets']) && gadgets.io) {
    return true;
  }
  return false;
}

wrappy.util.registerOnLoad = function(func) {
  if (wrappy.util.inGadget()) {
   gadgets.util.registerOnLoadHandler(func);
  } else {
    if (window.addEventListener) { // W3C standard 
      window.addEventListener('load', func, false); // NB **not** 'onload'
    } else if (window.attachEvent) { // Microsoft 
      window.attachEvent('onload', func);
    }
  }
}


/**
* Returns an XMLHttp instance to use for asynchronous
* downloading. This method will never throw an exception, but will
* return NULL if the browser does not support XmlHttp for any reason.
* @return {XMLHttpRequest|Null}
*/
wrappy.io.createXmlHttpRequest = function() {
 try {
   if (typeof ActiveXObject != 'undefined') {
     return new ActiveXObject('Microsoft.XMLHTTP');
   } else if (window["XMLHttpRequest"]) {
     return new XMLHttpRequest();
   }
 } catch (e) {
   //changeStatus(e);
 }
 return null;
};

wrappy.io.RequestParameters = {}
wrappy.io.RequestParameters.CONTENT_TYPE = 'CONTENT_TYPE';
wrappy.io.RequestParameters.METHOD = 'METHOD';
wrappy.io.RequestParameters.POST_DATA = 'POST_DATA';
wrappy.io.RequestParameters.REFRESH_INTERVAL = 'REFRESH_INTERVAL';
wrappy.io.ContentType = {}
wrappy.io.ContentType.DOM = 'DOM';
wrappy.io.ContentType.JSON = 'JSON';
wrappy.io.ContentType.TEXT = 'TEXT';
wrappy.io.MethodType = {}
wrappy.io.MethodType.GET = 'GET';
wrappy.io.MethodType.POST = 'POST';

wrappy.io.makeRequest = function(url, callback, params) {
  if (wrappy.util.inGadget()) {
    gadgets.io.makeRequest(url, callback, params);
  } else {
    wrappy.io.makeAjaxRequest(url, callback, params);
  }
}

/**
* This functions wraps XMLHttpRequest open/send function.
* It lets you specify a URL and will call the callback if
* it gets a status code of 200.
* @param {String} url The URL to retrieve
* @param {Function} callback The function to call once retrieved.
*/
wrappy.io.makeAjaxRequest = function(url, callback, params) {
 params = params || {};
 var contentType = params[wrappy.io.RequestParameters.CONTENT_TYPE] || wrappy.io.ContentType.TEXT;
 var method = params[wrappy.io.RequestParameters.METHOD] || wrappy.io.MethodType.GET;
 var data = params[wrappy.io.RequestParameters.POST_DATA] || null;
 var status = -1;
 var request = wrappy.io.createXmlHttpRequest();
 if (!request) {
   return false;
 }
 request.onreadystatechange = function() {
   if (request.readyState == 4) {
     try {
       status = request.status;
     } catch (e) {
       // Usually indicates request timed out in FF.
     }
     if (status == 200) {
       obj = request.responseXML;
       switch (contentType) {
         case wrappy.io.ContentType.TEXT:
           obj = request.responseText;
           break;
         case wrappy.io.ContentType.JSON:
           obj = {}
           obj.text = request.responseText;
           obj.data = eval('(' + request.responseText + ')');
           break;
       }
       callback(obj, request.status);
       request.onreadystatechange = function() {};
     }
   }
 }
 request.open(method, url, true);
 if (method == wrappy.io.MethodType.POST) { 
   request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
   request.setRequestHeader("Content-length", data.length);
   request.setRequestHeader("Connection", "close");
 }
 try {
   request.send(data);
 } catch (e) {
   //changeStatus(e);
 }
};

wrappy.window.adjustHeight = function() {
  if (wrappy.util.inGadget()) {
    gadgets.window.adjustHeight();
  }
}

wrappy.io.getFormData = function(form) {
  var formData = {};
  for (i = 0; i < form.getElementsByTagName("input").length; i++) {
    elem = form.getElementsByTagName('input')[i];
    if (elem.type == "text" || elem.type == "hidden") {
      formData[elem.name] = elem.value;
    }
    if (elem.type == "checkbox") {
      if (elem.checked) {
        formData[elem.name] = elem.value;
      } else {
        formData[elem.name] = "";
      }
    }
    if (elem.type == "radio") {
      if (elem.checked) {
        formData[elem.name] = elem.value;
      }
    }
  }
  for (i = 0; i < form.getElementsByTagName("select").length; i++) {
     var sel = form.getElementsByTagName("select")[i];
     formData[sel.name] = sel.options[sel.selectedIndex].value;
  }
  return formData;
}

wrappy.io.waveEnableForm = function(form) {
  for (i = 0; i < form.getElementsByTagName("input").length; i++) {
    var input = form.getElementsByTagName('input')[i];
    input.onchanged = function() {
      wrappy.io.submitFormDelta(form);
    }
  }
  for (i = 0; i < form.getElementsByTagName("select").length; i++) {
    var select = form.getElementsByTagName("select")[i];
    select.onchanged = function() {
      wrappy.io.submitFormDelta(form);
    }
  }
};

wrappy.io.submitFormDelta = function(form) {
  formData = wrappy.io.getFormData(form);
  console.log(formData);
  if (wrappy.util.inGadget() && wave) {
    wave.submitDelta(formData);
  }
}


function submitForm(form, url, callback) {
  var params = {};
  params['METHOD'] = 'POST';
  params['POST_DATA'] = getFormData(form);
  makeRequest(url, callback, params);
}
