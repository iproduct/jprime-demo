$(document).ready(function() {
   if( ! $('#myCanvas').tagcanvas({
     textColour : '#0000ff',
     outlineThickness : 1,
     maxSpeed : 0.03,
     depth : 0.75,
     weight:true
   })) {
     // TagCanvas failed to load
     console.log("!!!!!!!!!!! Canvas not loaded");
     $('#myCanvasContainer').hide();
   }
   // your other jQuery stuff here...
 });

function updateCloud(tags){
	$("#tagList").empty();
	for(i = 0; i < tags.length; i++) {
		$("#tagList")
		.append('<li> <a style="font-size: ' + tags[i].clicks + 'pt" href="#">' 
			+ tags[i].title + '</a></li>');
	}
	// update tagcanvas
	$('#myCanvas').tagcanvas("update");
}