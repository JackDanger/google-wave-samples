function identify(element) {
  var classification = element.attr('class');
  return classification != 'default' && classification.split(' ', 1) ||
    $.trim(element.text());
}

function getSelection(menu) { return menu.find(':selected'); }

function evaluate(name, selection) {
  return $('.' + name).text(selection.val());
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

    var name = menu.attr('name');

    menu.change(function() {
      var selection = getSelection(menu);
      var id = identify(selection);
      menu.css({
        'background-color': colors[id].background,
        'font-family': selection.css('font-family'),
        'font-size': selection.css('font-size'),
        color: colors[id].text
      });
      evaluate(name, selection);
    });

    evaluate(name, getSelection(menu)).css('font-style', 'normal');
  });
}

$(function() {
  var isInWave = wave.isInWaveContainer();
  var io = $('#io').append('<div id="buffer" style="display: none" />');
  var buffer = $('#buffer');

  if (isInWave) {
    var state;

    wave.setStateCallback(function() {
      state = wave.getState();
      buffer.
        html($('<select name="id" />').
          html($('<option selected="selected" />').val(state.get('id'))));
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

  io.submit(function() {
    if (isInWave) { state.submitValue('is-closed', 'true'); }

    return false;
  });
});
