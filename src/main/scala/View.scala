object View {
  private[this] val style = <style type="text/css"><![CDATA[
    * { margin:0; padding:0; }
    body { font-size: 24px; font-family:helvetica, sans serif; padding:1em; color:#333; }
    input[type='text'] { font-size:24px; } #debug { font-size:16px; color:#ccc; }
    ul { list-style:none; }
    .who { color:#FF00C3; }
  ]]></style>


  private[this] def script(port: Int) = <script type="text/javascript">{
    scala.xml.Unparsed(s"""(function(jq) {
      var socket, host = "ws://localhost:$port/";

      var supported = function() { return window.WebSocket || window.MozWebSocket; }
      , newWebSocket = function(uri) { return window.WebSocket ?
        new WebSocket(uri) : new MozWebSocket(uri)
       }
      , createSocket = function(uri) {
        if(supported()) {
           window.socket = socket = newWebSocket(uri);
           socket.onmessage = function(e) {
             var msg = e.data.split("|"), who = msg.shift(), what =  msg.join("|");
             jq('ul').first().prepend(['<li><span class="who">',who,'</span> ', what, '</li>'].join(""));
           }
           socket.onopen = function(e) {
             debug('connection open')
           }
           socket.onclose = function(e) {
             debug('connection closed');
           }
         } else {
           alert("your browser does not support web sockets. try chrome.");
         }
      }
      , debug = function(msg) { jq("#debug").html(msg); }
      , isOpen = function() { return socket ?
        socket.readyState == (window.WebSocket ? WebSocket.OPEN : MozWebSocket.OPEN) : false;
      }
      , send = function(message) {
        if(!supported()) { return; }
        if(isOpen()) {
          socket.send(message);
        } else {
          alert("socket is not open");
        }
      }
      , closeSocket = function() { if(socket) { socket.close(); } }
      , openSocket = function() {
         if(isOpen()) {
           alert('socket already open');
           return;
         }
         createSocket(host);
      }
      , toggleConnection = function() { if(isOpen()) { closeSocket(); } else { openSocket(); } };

      createSocket(host);

      jq("#message").bind('keydown',function(){
         jq("#submit").removeAttr("disabled");
         jq(this).unbind('keydown');
      });
      jq("#frm").submit(function(e){
        e.preventDefault();
        send(this.message.value);
        this.message.value = '';
        return false;
      });
      jq("#tooglr").click(function(e){
        e.preventDefault();
        toggleConnection();
        return false;
      });
   })(jQuery);""")}</script>

  def apply(port: Int): scala.xml.Node = {
<html>
  <head>
    <title>Unfiltered WebSockets</title>
    {style}
    <script text="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"></script>
  </head>
  <body>
    <p>ws://localhost:{port}/</p>
    <form id="frm">
      <input type="text" name="message" id="message" placeholder="Hey bro" />
      <div><input type="button" value="toogle connection" id="tooglr" /></div>
    </form>
    <div id="debug"></div>
    <ul></ul>
    {script(port)}
  </body>
</html>
  }
}
