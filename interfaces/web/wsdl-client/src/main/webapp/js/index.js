$(document).ready(function(){
	  $("#msgid1").hide();
	  $("#ocultar").click(function(){
		  							  $("#msgid1").hide("slow");
		  							  $("#mostrar").show();
		  							  $("#ocultar").hide();
		  	});
	  $("#mostrar").click(function(){
		  							  $("#msgid1").show("slow");
		  							  $("#mostrar").hide();
		  							  $("#ocultar").show();
		  	});
	  
	  $("#msgid1Log").hide();
	  $("#ocultarLog").click(function(){
		  							  $("#msgid1Log").hide("slow");
		  							  $("#mostrarLog").show();
		  							  $("#ocultarLog").hide();
		  	});
	  $("#mostrarLog").click(function(){
		  							  $("#msgid1Log").show("slow");
		  							  $("#mostrarLog").hide();
		  							  $("#ocultarLog").show();
		  	});
});