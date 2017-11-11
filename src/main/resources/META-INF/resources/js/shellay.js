(function(){

	jQuery.noConflict();
	
	window.Shellay = {};
	Shellay.websocket = null;
	Shellay.terminal = null;
	
	Shellay.open = function()
	{
		var protocol = (location.protocol === 'https:') ? 'wss://' : 'ws://';
		var socketURL = protocol + location.hostname + ((location.port) ? (':' + location.port) : '') + "/o/ShellSocket/tty";
		Shellay.websocket = new WebSocket(socketURL);
		
		Shellay.websocket.onclose = function (event) 
		{
			Shellay.print("... Trying to reconnect ... to backend ...");
			Shellay.open();
		}
		
		Shellay.websocket.onerror = function(error) 
		{
			Shellay.print("Error : " + error);
		};

		Shellay.websocket.onmessage = function (event) 
		{
			Shellay.terminal.echo(event.data);
		}
	}

	Shellay.print = function(text)
	{
		if(!text) text = '';
		Shellay.terminal.echo(text);
	};

	Shellay.terminal =  jQuery('#term_shellay').terminal(function(command) 
	{
		Shellay.websocket.send(command);
		Shellay.print();
	},
	{
		greetings : ' ... Shellay, http://darrylsite.com ...',
		name : 'Shellay by Darryl, http://darrylsite.com',
		height : 500,
		prompt : 'shl> '
	});

	Shellay.open();
})();