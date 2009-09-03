function identify(element) {
  var classification = element.attr('class');
  return classification != 'default' && classification.split(' ', 1) ||
    $.trim(element.text());
}

function getSelection(menu) { return menu.find(':selected'); }

function evaluate(menu, selection) {
  var name = menu.attr('name');
  return $('.' + name.slice(0, name.indexOf('-') + 1) + 'evaluated').
    text(selection.val());
}

function initialize(panel) {
  panel.find('select').each(function() {
    var menu = $(this);
    var colors = {};

    menu.children().each(function() {
      var item = $(this);
      var background = item.css('background-color');
      colors[identify(item)] = {
        background:
          background != 'rgba(0, 0, 0, 0)' && background != 'transparent' ?
            background :
            menu.css('background-color'),
        text: item.css('color')
      };
    });

    menu.change(function() {
      var selection = getSelection(menu);
      var id = identify(selection);
      menu.css({
        'background-color': colors[id].background,
        'font-family': selection.css('font-family'),
        'font-size': selection.css('font-size'),
        color: colors[id].text
      });
      evaluate(menu, selection);
    });

    evaluate(menu, getSelection(menu)).css('font-style', 'normal');
  });
}

$(function() {
  var io = $('#io').append('<div id="buffer" style="display: none" />');
  var buffer = $('#buffer');

  if (wave.isInWaveContainer()) {
    wave.setStateCallback(function() {
      buffer.
        html($('<select name="id-defined" />').
          html($('<option selected="selected" />').
            val(wave.getState().get('id-defined'))));
      initialize(io);
    });
  } else { initialize(io); }

  buffer.append(
    '<img src="http://embeddy.appspot.com/gadget/close-over.gif" alt="" />'
  );

  var close = $('#close').bind('mouseover focus', function() {
    close.attr('src', 'http://embeddy.appspot.com/gadget/close-over.gif');
  }).bind('mouseout blur', function() {
    close.attr('src', 'http://embeddy.appspot.com/gadget/close.gif');
  });

  io.submit(function() { return false; });
});
